package com.redis.om.spring.serialization.gson;

import static com.redis.om.spring.util.ObjectUtils.*;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Reference;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.ops.json.JSONOperations;

/**
 * A Spring component responsible for registering Gson type adapters for fields annotated with @Reference.
 * This registrar scans for entities with @Reference fields and configures appropriate serialization/deserialization
 * handling for those reference relationships.
 */
@Component
public class GsonReferenceSerializerRegistrar {
  private static final Log logger = LogFactory.getLog(GsonReferenceSerializerRegistrar.class);
  private static final String SKIPPING_REFERENCE_SEARCH = "Skipping @Reference search for %s because %s";
  private final GsonBuilder builder;
  private final ApplicationContext ac;
  private JSONOperations<?> ops;

  /**
   * Constructs a new GsonReferenceSerializerRegistrar.
   * 
   * @param builder the GsonBuilder to register type adapters with
   * @param ac      the Spring ApplicationContext for bean lookups
   */
  public GsonReferenceSerializerRegistrar(GsonBuilder builder, ApplicationContext ac) {
    this.builder = builder;
    this.ac = ac;
  }

  /**
   * Registers reference type adapters for all beans of the specified class type that contain @Reference fields.
   * 
   * @param cls the class type to scan for @Reference annotated fields
   */
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

  /**
   * Processes an entity class to find and register type adapters for @Reference fields.
   * 
   * @param clazz the entity class to process
   */
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

  /**
   * Processes a specific field annotated with @Reference and registers the appropriate type adapter.
   * 
   * @param field the field to process
   */
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

    builder.registerTypeAdapter(typeToken.getType(), new ReferenceDeserializer(field, ops, ac.getBean(
        RedisOMProperties.class), ac.getBean("redisOMCacheManager", CacheManager.class)));
    processEntity(field.getType());
  }

}
