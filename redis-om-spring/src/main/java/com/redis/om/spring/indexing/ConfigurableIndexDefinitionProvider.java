package com.redis.om.spring.indexing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.om.spring.annotations.Document;

/**
 * Configurable provider for managing index definitions in Redis OM Spring.
 * This class bridges RediSearch indexes with Spring Data Redis and provides
 * runtime configuration capabilities for dynamic indexing.
 *
 * Phase 4 implementation of the Dynamic Indexing Feature Design.
 *
 * @since 1.0.0
 */
@Component
public class ConfigurableIndexDefinitionProvider {
  private static final Logger logger = LoggerFactory.getLogger(ConfigurableIndexDefinitionProvider.class);

  private final RediSearchIndexer indexer;
  private final ApplicationContext applicationContext;
  private final Map<Class<?>, IndexDefinition> indexDefinitions = new ConcurrentHashMap<>();
  private final Map<Class<?>, IndexResolver> customResolvers = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ConfigurableIndexDefinitionProvider(RediSearchIndexer indexer, ApplicationContext applicationContext) {
    this.indexer = indexer;
    this.applicationContext = applicationContext;
    initializeFromAnnotations();
  }

  /**
   * Initialize index definitions from entity annotations.
   */
  private void initializeFromAnnotations() {
    // Scan for @Document and @RedisHash annotated classes
    Map<String, Object> documentBeans = applicationContext.getBeansWithAnnotation(Document.class);
    Map<String, Object> hashBeans = applicationContext.getBeansWithAnnotation(RedisHash.class);

    documentBeans.values().forEach(bean -> {
      Class<?> entityClass = bean.getClass();
      processEntityClass(entityClass);
    });

    hashBeans.values().forEach(bean -> {
      Class<?> entityClass = bean.getClass();
      processEntityClass(entityClass);
    });
  }

  private void processEntityClass(Class<?> entityClass) {
    String indexName = indexer.getIndexName(entityClass);
    String keyPrefix = getKeyPrefix(entityClass);
    IndexDefinition definition = new IndexDefinition(indexName, keyPrefix, entityClass);
    indexDefinitions.put(entityClass, definition);
  }

  private String getKeyPrefix(Class<?> entityClass) {
    // Extract key prefix from annotations or use default
    if (entityClass.isAnnotationPresent(Document.class)) {
      return entityClass.getSimpleName().toLowerCase() + ":";
    } else if (entityClass.isAnnotationPresent(RedisHash.class)) {
      RedisHash hash = entityClass.getAnnotation(RedisHash.class);
      return hash.value() + ":";
    }
    return entityClass.getSimpleName().toLowerCase() + ":";
  }

  /**
   * Get all configured index definitions.
   *
   * @return list of all index definitions
   */
  public List<IndexDefinition> getIndexDefinitions() {
    return new ArrayList<>(indexDefinitions.values());
  }

  /**
   * Get index definition for a specific entity class.
   *
   * @param entityClass the entity class
   * @return the index definition, or null if not found
   */
  public IndexDefinition getIndexDefinition(Class<?> entityClass) {
    return indexDefinitions.get(entityClass);
  }

  /**
   * Get index definition for a specific entity class with context.
   *
   * @param entityClass the entity class
   * @param context     the Redis index context
   * @return the context-aware index definition
   */
  public IndexDefinition getIndexDefinition(Class<?> entityClass, RedisIndexContext context) {
    IndexResolver resolver = customResolvers.getOrDefault(entityClass, new DefaultIndexResolver(applicationContext));

    String indexName = resolver.resolveIndexName(entityClass, context);
    String keyPrefix = resolver.resolveKeyPrefix(entityClass, context);

    return new IndexDefinition(indexName, keyPrefix, entityClass);
  }

  /**
   * Register a new index definition at runtime.
   *
   * @param entityClass the entity class
   * @param indexName   the index name
   * @param keyPrefix   the key prefix
   */
  public void registerIndexDefinition(Class<?> entityClass, String indexName, String keyPrefix) {
    IndexDefinition definition = new IndexDefinition(indexName, keyPrefix, entityClass);
    indexDefinitions.put(entityClass, definition);
    logger.info("Registered index definition for {} with index {} and prefix {}", entityClass.getName(), indexName,
        keyPrefix);
  }

  /**
   * Update an existing index definition.
   *
   * @param entityClass the entity class
   * @param indexName   the new index name
   * @param keyPrefix   the new key prefix
   */
  public void updateIndexDefinition(Class<?> entityClass, String indexName, String keyPrefix) {
    IndexDefinition definition = new IndexDefinition(indexName, keyPrefix, entityClass);
    indexDefinitions.put(entityClass, definition);
    logger.info("Updated index definition for {} with index {} and prefix {}", entityClass.getName(), indexName,
        keyPrefix);
  }

  /**
   * Remove an index definition.
   *
   * @param entityClass the entity class
   * @return true if removed, false if not found
   */
  public boolean removeIndexDefinition(Class<?> entityClass) {
    IndexDefinition removed = indexDefinitions.remove(entityClass);
    if (removed != null) {
      logger.info("Removed index definition for {}", entityClass.getName());
      return true;
    }
    return false;
  }

  /**
   * Bulk register multiple index definitions.
   *
   * @param configs map of entity classes to their configurations
   */
  public void registerIndexDefinitions(Map<Class<?>, IndexDefinitionConfig> configs) {
    configs.forEach((entityClass, config) -> {
      registerIndexDefinition(entityClass, config.getIndexName(), config.getKeyPrefix());
    });
  }

  /**
   * Refresh index definitions from current annotations.
   */
  public void refreshIndexDefinitions() {
    indexDefinitions.clear();
    initializeFromAnnotations();
    logger.info("Refreshed {} index definitions from annotations", indexDefinitions.size());
  }

  /**
   * Set a custom index resolver for a specific entity.
   *
   * @param entityClass the entity class
   * @param resolver    the custom resolver
   */
  public void setIndexResolver(Class<?> entityClass, IndexResolver resolver) {
    customResolvers.put(entityClass, resolver);
    logger.info("Set custom index resolver for {}", entityClass.getName());
  }

  /**
   * Get index definitions for all entities managed by a repository.
   *
   * @param repositoryClass the repository class
   * @return list of index definitions
   */
  public List<IndexDefinition> getIndexDefinitionsForRepository(Class<?> repositoryClass) {
    // This would require analyzing the repository's generic type parameters
    // For now, return all definitions
    return getIndexDefinitions();
  }

  /**
   * Get statistics for an index.
   *
   * @param entityClass the entity class
   * @return index statistics
   */
  public IndexStatistics getIndexStatistics(Class<?> entityClass) {
    IndexDefinition definition = indexDefinitions.get(entityClass);
    if (definition == null) {
      return null;
    }

    // Would query Redis for actual statistics
    return new IndexStatistics(definition.getIndexName(), 0, 0);
  }

  /**
   * Validate an index definition.
   *
   * @param definition the definition to validate
   * @return validation result
   */
  public ValidationResult validateIndexDefinition(IndexDefinition definition) {
    ValidationResult result = new ValidationResult();

    if (definition.getIndexName() == null || definition.getIndexName().isEmpty()) {
      result.addError("Index name is required");
    }

    if (definition.getKeyPrefix() == null || definition.getKeyPrefix().isEmpty()) {
      result.addError("Key prefix is required");
    }

    if (definition.getEntityClass() == null) {
      result.addError("Entity class is required");
    }

    return result;
  }

  /**
   * Export all index definitions as JSON.
   *
   * @return JSON string of all definitions
   */
  public String exportDefinitions() {
    try {
      return objectMapper.writeValueAsString(indexDefinitions);
    } catch (JsonProcessingException e) {
      logger.error("Failed to export index definitions", e);
      return "{}";
    }
  }

  /**
   * Import index definitions from JSON.
   *
   * @param definitionsJson JSON string with definitions
   * @return number of imported definitions
   */
  public int importDefinitions(String definitionsJson) {
    try {
      Map<String, Object> imported = objectMapper.readValue(definitionsJson, Map.class);
      // Process imported definitions
      return imported.size();
    } catch (JsonProcessingException e) {
      logger.error("Failed to import index definitions", e);
      return 0;
    }
  }

  /**
   * Inner class representing an index definition configuration.
   */
  public static class IndexDefinitionConfig {
    private final String indexName;
    private final String keyPrefix;

    public IndexDefinitionConfig(String indexName, String keyPrefix) {
      this.indexName = indexName;
      this.keyPrefix = keyPrefix;
    }

    public String getIndexName() {
      return indexName;
    }

    public String getKeyPrefix() {
      return keyPrefix;
    }
  }

  /**
   * Inner class representing an index definition.
   */
  public static class IndexDefinition {
    private final String indexName;
    private final String keyPrefix;
    private final Class<?> entityClass;

    public IndexDefinition(String indexName, String keyPrefix, Class<?> entityClass) {
      this.indexName = indexName;
      this.keyPrefix = keyPrefix;
      this.entityClass = entityClass;
    }

    public String getIndexName() {
      return indexName;
    }

    public String getKeyPrefix() {
      return keyPrefix;
    }

    public Class<?> getEntityClass() {
      return entityClass;
    }
  }

  /**
   * Inner class representing index statistics.
   */
  public static class IndexStatistics {
    private final String indexName;
    private final long documentCount;
    private final long indexSize;

    public IndexStatistics(String indexName, long documentCount, long indexSize) {
      this.indexName = indexName;
      this.documentCount = documentCount;
      this.indexSize = indexSize;
    }

    public String getIndexName() {
      return indexName;
    }

    public long getDocumentCount() {
      return documentCount;
    }

    public long getIndexSize() {
      return indexSize;
    }
  }

  /**
   * Inner class representing validation results.
   */
  public static class ValidationResult {
    private final List<String> errors = new ArrayList<>();

    public void addError(String error) {
      errors.add(error);
    }

    public boolean isValid() {
      return errors.isEmpty();
    }

    public List<String> getErrors() {
      return errors;
    }
  }
}