package com.redis.om.spring.serialization.gson;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.google.gson.*;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.RedisOMProperties.References;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;

/**
 * Custom Gson deserializer for handling Redis entity references during JSON deserialization.
 * <p>
 * This deserializer is responsible for resolving Redis entity references by their keys and
 * reconstructing the full object graph. It supports both individual references and collections
 * of references, with optional caching to improve performance for frequently accessed entities.
 * <p>
 * The deserializer handles three types of JSON input:
 * <ul>
 * <li>Primitive strings representing Redis keys - fetched and deserialized from Redis</li>
 * <li>JSON objects - directly deserialized as embedded entities</li>
 * <li>JSON arrays - deserialized as collections of references or embedded entities</li>
 * </ul>
 * <p>
 * Caching is configurable per entity type through {@link RedisOMProperties.References}
 * configuration, allowing selective caching of frequently accessed reference types.
 * 
 * @see JsonDeserializer
 * @see RedisOMProperties.References
 * @see JSONOperations
 */
public class ReferenceDeserializer implements JsonDeserializer<Object> {
  private static final Log logger = LogFactory.getLog(ReferenceDeserializer.class);

  private final Class<?> type;
  private final ObjectConstructor<?> objectConstructor;
  private final JSONOperations<String> ops;
  private final Gson gson = new Gson();
  private final Cache referenceCache;
  private final List<String> cachedReferenceClasses;

  /**
   * Constructs a new ReferenceDeserializer for the specified field.
   * <p>
   * Initializes the deserializer with the target field's type information, Redis operations
   * client, and caching configuration. For collection fields, the element type is extracted
   * and used as the target deserialization type.
   * 
   * @param field        the field being deserialized, used to determine the target type
   * @param ops          the JSON operations client for Redis interactions
   * @param properties   Redis OM configuration properties, including reference caching settings
   * @param cacheManager Spring cache manager for reference caching
   */
  @SuppressWarnings(
    "unchecked"
  )
  public ReferenceDeserializer(Field field, JSONOperations<?> ops, RedisOMProperties properties,
      CacheManager cacheManager) {
    this.ops = (JSONOperations<String>) ops;
    Map<Type, InstanceCreator<?>> instanceCreators = new HashMap<>();
    ConstructorConstructor constructorConstructor = new ConstructorConstructor(instanceCreators, true, Collections
        .emptyList());
    if (ObjectUtils.isCollection(field)) {
      Optional<Class<?>> collectionType = ObjectUtils.getCollectionElementClass(field);
      if (collectionType.isPresent()) {
        this.type = collectionType.get();
      } else {
        this.type = field.getType();
      }
    } else {
      this.type = field.getType();
    }
    this.objectConstructor = constructorConstructor.get(TypeToken.get(type));

    References referencesConfig = properties.getReferences();
    referenceCache = cacheManager.getCache(referencesConfig.getCacheName());
    this.cachedReferenceClasses = referencesConfig.getCachedReferenceClasses();
  }

  @Override
  @SuppressWarnings(
    { "unchecked", "rawtypes" }
  )
  public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Object reference = null;
    JsonObject jsonObject;
    if (json.isJsonPrimitive()) {
      String referenceKey = ObjectUtils.unQuote(json.toString());
      String referenceJSON = null;
      if (shouldCache(type)) {
        referenceJSON = referenceCache.get(referenceKey, String.class);
      }
      if (referenceJSON == null) {
        referenceJSON = ops.get(referenceKey);
        if (referenceJSON != null && referenceCache != null && shouldCache(type)) {
          referenceCache.put(referenceKey, referenceJSON);
        }
      }

      // Handle missing reference gracefully
      if (referenceJSON == null) {
        logger.warn(String.format("Referenced entity with key '%s' not found for type %s", referenceKey, type
            .getName()));
        return null;
      }

      jsonObject = gson.fromJson(referenceJSON, JsonObject.class);
      reference = deserializeEntity(jsonObject, context);
    } else if (json.isJsonObject()) {
      jsonObject = json.getAsJsonObject();
      reference = deserializeEntity(jsonObject, context);
    } else if (json.isJsonArray()) {
      JsonArray jsonArray = json.getAsJsonArray();
      reference = instantiateCollection(typeOfT);

      String[] keys = jsonArray.asList().stream().filter(JsonElement::isJsonPrimitive).map(jsonElement -> ObjectUtils
          .unQuote(jsonElement.toString())).toArray(String[]::new);

      List<String> values;
      if (keys.length > 0) {
        if (shouldCache(type)) {
          values = Arrays.stream(keys).map(key -> referenceCache.get(key, String.class)).filter(Objects::nonNull)
              .collect(Collectors.toList());
          if (values.size() < keys.length) {
            String[] missingKeys = Arrays.stream(keys).filter(key -> referenceCache.get(key, String.class) == null)
                .toArray(String[]::new);
            List<String> fetchedValues = ops.mget(missingKeys);
            for (int i = 0; i < missingKeys.length; i++) {
              referenceCache.put(missingKeys[i], fetchedValues.get(i));
            }
            values.addAll(fetchedValues);
          }
        } else {
          values = ops.mget(keys);
        }
        // Filter out null values (missing references) and log warnings
        List<Object> deserializedReferences = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
          String raw = values.get(i);
          if (raw != null) {
            JsonObject jo = gson.fromJson(raw, JsonObject.class);
            deserializedReferences.add(deserializeEntity(jo, context));
          } else {
            logger.warn(String.format("Referenced entity with key '%s' not found for type %s", keys[i], type
                .getName()));
          }
        }
        ((Collection) reference).addAll(deserializedReferences);
      }
    }

    return reference;
  }

  /**
   * Creates and returns an appropriate collection instance based on the given type.
   * <p>
   * This method handles the instantiation of collection types during deserialization.
   * For interface types, it provides default implementations:
   * <ul>
   * <li>{@link List} → {@link ArrayList}</li>
   * <li>{@link Set} → {@link HashSet}</li>
   * <li>{@link Queue} → {@link LinkedList}</li>
   * </ul>
   * For concrete collection classes, it attempts to create an instance using the
   * default constructor.
   * 
   * @param type the parameterized collection type to instantiate
   * @return a new collection instance appropriate for the given type
   * @throws IllegalArgumentException if the type is an unsupported interface or
   *                                  cannot be instantiated
   */
  public Collection<?> instantiateCollection(Type type) {
    Class<?> rawType = (Class<?>) ((ParameterizedType) type).getRawType();
    if (rawType.isInterface()) {
      if (List.class.isAssignableFrom(rawType)) {
        return new ArrayList<>();
      } else if (Set.class.isAssignableFrom(rawType)) {
        return new HashSet<>();
      } else if (Queue.class.isAssignableFrom(rawType)) {
        return new LinkedList<>();
      } else {
        throw new IllegalArgumentException("Unsupported interface: " + rawType);
      }
    } else {
      try {
        return (Collection<?>) rawType.getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        throw new IllegalArgumentException("Type not instantiatable: " + rawType);
      }
    }
  }

  private Object deserializeEntity(JsonObject jsonObject, JsonDeserializationContext context) {
    Object reference = objectConstructor.construct();
    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
      String key = entry.getKey();
      JsonElement value = entry.getValue();

      try {
        Field field = type.getDeclaredField(key);
        var setter = ObjectUtils.getSetterForField(type, field);

        Class<?> fieldType = field.getType();
        Object elementValue = context.deserialize(value, fieldType);
        if (setter != null) {
          setter.invoke(reference, elementValue);
        } else {
          field.setAccessible(true);
          field.set(reference, elementValue);
        }
      } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
        logger.error(String.format("Error while deserializing reference of type %s", type), e);
      }
    }
    return reference;
  }

  private boolean shouldCache(Class<?> referenceClass) {
    return cachedReferenceClasses.contains(referenceClass.getName());
  }
}
