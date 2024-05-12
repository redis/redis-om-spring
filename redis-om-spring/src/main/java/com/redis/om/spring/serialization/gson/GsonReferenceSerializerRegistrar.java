package com.redis.om.spring.serialization.gson;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.ops.json.JSONOperations;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Reference;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.redis.om.spring.util.ObjectUtils.*;

@Component
public class GsonReferenceSerializerRegistrar {
  private static final Log logger = LogFactory.getLog(GsonReferenceSerializerRegistrar.class);
  private final GsonBuilder builder;
  private JSONOperations<?> ops;
  private final ApplicationContext ac;
  private static final String SKIPPING_REFERENCE_SEARCH = "Skipping @Reference search for %s because %s";

  public GsonReferenceSerializerRegistrar(GsonBuilder builder, ApplicationContext ac) {
    this.builder = builder;
    this.ac = ac;
  }

  public void registerReferencesFor(Class<?> cls) {
    Set<BeanDefinition> beanDefs = new HashSet<>(getBeanDefinitionsFor(ac, cls));

    logger.info(String.format("Found %s @%s annotated Beans...", beanDefs.size(), cls.getSimpleName()));

    for (BeanDefinition beanDef : beanDefs) {
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        processEntity(cl);
      } catch (ClassNotFoundException e) {
        logger.warn(String.format(SKIPPING_REFERENCE_SEARCH, beanDef.getBeanClassName(), e.getMessage()));
      }
    }
  }

  private void processEntity(Class<?> clazz) {
    ops = ac.getBean("redisJSONOperations", JSONOperations.class);
    final List<java.lang.reflect.Field> allClassFields = getDeclaredFieldsTransitively(clazz);
    for (java.lang.reflect.Field field : allClassFields) {
      if (field.isAnnotationPresent(Reference.class)) {
        logger.info(String.format("🪧Registering reference type adapter for %s", field.getType().getName()));
        processField(field);
      }
    }
  }

  private void processField(Field field) {
    TypeToken<?> typeToken;
    if (isCollection(field)) {
      var maybeCollectionElementType = getCollectionElementType(field);
      if (maybeCollectionElementType.isPresent()) {
        typeToken = TypeToken.getParameterized(field.getType(), maybeCollectionElementType.get());
      } else {
        typeToken = TypeToken.get(field.getType());
      }
    } else {
      typeToken = TypeToken.get(field.getType());
    }

    builder.registerTypeAdapter(
      typeToken.getType(),
      new ReferenceDeserializer(
        field,
        ops,
        ac.getBean(RedisOMProperties.class),
        ac.getBean("redisOMCacheManager", CacheManager.class))
    );
    processEntity(field.getType());
  }

}
