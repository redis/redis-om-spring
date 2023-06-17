package com.redis.om.spring.serialization.gson;

import com.google.gson.*;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

public class ReferenceDeserializer implements JsonDeserializer<Object> {
  private static final Log logger = LogFactory.getLog(ReferenceDeserializer.class);

  private final Class<?> type;
  private final ApplicationContext ac;
  private final ObjectConstructor<?> objectConstructor;

  public ReferenceDeserializer(Field field, ApplicationContext ac) {
    this.ac = ac;
    Map<Type, InstanceCreator<?>> instanceCreators = new HashMap<>();
    ConstructorConstructor constructorConstructor = new ConstructorConstructor(instanceCreators, true,
        Collections.<ReflectionAccessFilter>emptyList());
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
  }

  @Override
  public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Object reference = null;
    JsonObject jsonObject;
    JSONOperations<String> ops = (JSONOperations<String>) ac.getBean("redisJSONOperations");
    if (json.isJsonPrimitive()) {
      String referenceKey = ObjectUtils.unQuote(json.toString());
      String referenceJSON = ops.get(referenceKey);
      jsonObject = new Gson().fromJson(referenceJSON, JsonObject.class);
      reference = deserializeEntity(jsonObject, context);
    } else if (json.isJsonObject()) {
      jsonObject = json.getAsJsonObject();
      reference = deserializeEntity(jsonObject, context);
    } else if (json.isJsonArray()) {
      JsonArray jsonArray = json.getAsJsonArray();
      reference = ObjectUtils.instantiateCollection(typeOfT);

      String[] keys = jsonArray.asList().stream().filter(JsonElement::isJsonPrimitive).map(jsonElement -> ObjectUtils.unQuote(jsonElement.toString())).toArray(String[]::new);
      if (keys.length > 0) {
        var values = ops.mget(keys);
        ((Collection) reference).addAll( //
            values.stream().map(raw -> new Gson().fromJson(raw, JsonObject.class)).map(jo -> deserializeEntity(jo, context)).toList() //
        );
      }
    }

    return reference;
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
}
