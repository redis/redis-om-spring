package com.redis.om.spring.serialization.gson;

import com.google.gson.*;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.RedisOMProperties.References;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class ReferenceDeserializer implements JsonDeserializer<Object> {
  private static final Log logger = LogFactory.getLog(ReferenceDeserializer.class);

  private final Class<?> type;
  private final ObjectConstructor<?> objectConstructor;
  private final JSONOperations<String> ops;
  private final Gson gson = new Gson();
  private final Cache referenceCache;
  private final List<String> cachedReferenceClasses;

  @SuppressWarnings("unchecked")
  public ReferenceDeserializer(Field field, JSONOperations<?> ops, RedisOMProperties properties, CacheManager cacheManager) {
    this.ops = (JSONOperations<String>) ops;
    Map<Type, InstanceCreator<?>> instanceCreators = new HashMap<>();
    ConstructorConstructor constructorConstructor = new ConstructorConstructor(instanceCreators, true,
        Collections.emptyList());
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
  @SuppressWarnings("unchecked")
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
        if (referenceCache != null && shouldCache(type)) {
          referenceCache.put(referenceKey, referenceJSON);
        }
      }
      jsonObject = gson.fromJson(referenceJSON, JsonObject.class);
      reference = deserializeEntity(jsonObject, context);
    } else if (json.isJsonObject()) {
      jsonObject = json.getAsJsonObject();
      reference = deserializeEntity(jsonObject, context);
    } else if (json.isJsonArray()) {
      JsonArray jsonArray = json.getAsJsonArray();
      reference = instantiateCollection(typeOfT);

      String[] keys = jsonArray.asList().stream()
        .filter(JsonElement::isJsonPrimitive)
        .map(jsonElement -> ObjectUtils.unQuote(jsonElement.toString()))
        .toArray(String[]::new);

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
        ((Collection) reference).addAll(
          values.stream()
            .map(raw -> gson.fromJson(raw, JsonObject.class))
            .map(jo -> deserializeEntity(jo, context))
            .toList());
      }
    }

    return reference;
  }

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
