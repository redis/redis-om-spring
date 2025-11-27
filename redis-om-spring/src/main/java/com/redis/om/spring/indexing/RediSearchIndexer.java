package com.redis.om.spring.indexing;

import static com.redis.om.spring.util.ObjectUtils.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration.KeyspaceSettings;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.annotations.*;
import com.redis.om.spring.id.IdFilter;
import com.redis.om.spring.id.IdentifierFilter;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.serialization.gson.EnumTypeAdapter;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.tuple.Tuples;

import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.schemafields.*;

/**
 * Component responsible for creating and managing RediSearch indices for Redis OM Spring entities.
 * 
 * This class handles the automatic creation of search indices for entities annotated with {@link Document}
 * or {@link RedisHash}. It processes field annotations like {@link Indexed}, {@link Searchable},
 * {@link TextIndexed}, {@link TagIndexed}, {@link NumericIndexed}, {@link GeoIndexed}, and
 * {@link VectorIndexed} to configure appropriate search schema fields.
 * 
 * <p>The indexer maintains mappings between:
 * <ul>
 * <li>Redis keyspaces and entity classes</li>
 * <li>Entity classes and their corresponding search index names</li>
 * <li>Entity fields and their search index aliases</li>
 * <li>Entity classes and their identifier filters</li>
 * </ul>
 * 
 * <p>Key features include:
 * <ul>
 * <li>Automatic index creation with configurable creation modes</li>
 * <li>Support for JSON documents and hash structures</li>
 * <li>Vector similarity search capabilities</li>
 * <li>Nested object indexing</li>
 * <li>Reference field indexing</li>
 * <li>TTL (Time To Live) configuration</li>
 * </ul>
 * 
 * @see Document
 * @see RedisHash
 * @see IndexingOptions
 * @see Indexed
 * @see Searchable
 * @since 1.0.0
 */
@Component
public class RediSearchIndexer {
  private static final Log logger = LogFactory.getLog(RediSearchIndexer.class);
  private static final String SKIPPING_INDEX_CREATION = "Skipping index creation for %s because %s";
  private final Map<String, Class<?>> keyspaceToEntityClass = new ConcurrentHashMap<>();
  private final Map<Class<?>, String> entityClassToKeySpace = new ConcurrentHashMap<>();
  private final Map<Class<?>, String> entityClassToIndexName = new ConcurrentHashMap<>();
  private final Map<Class<?>, IdentifierFilter<?>> entityClassToIdentifierFilter = new ConcurrentHashMap<>();
  private final List<Class<?>> indexedEntityClasses = new ArrayList<>();
  private final Map<Class<?>, List<SearchField>> entityClassToSchema = new ConcurrentHashMap<>();
  private final Map<Pair<Class<?>, String>, String> entityClassFieldToAlias = new ConcurrentHashMap<>();
  private final Map<Class<?>, Set<String>> entityClassToLexicographicFields = new ConcurrentHashMap<>();
  private final ApplicationContext ac;
  private final RedisModulesOperations<String> rmo;
  private final RedisMappingContext mappingContext;
  private final GsonBuilder gsonBuilder;
  private final RedisOMProperties properties;
  private final ExpressionParser spelParser = new SpelExpressionParser();

  /**
   * Constructs a new RediSearchIndexer with the required dependencies.
   * Initializes Redis modules operations and mapping context from the application context.
   *
   * @param ac          the Spring application context for accessing beans
   * @param properties  the Redis OM configuration properties
   * @param gsonBuilder the Gson builder for JSON serialization configuration
   */
  @SuppressWarnings(
    "unchecked"
  )
  public RediSearchIndexer(ApplicationContext ac, RedisOMProperties properties, GsonBuilder gsonBuilder) {
    this.ac = ac;
    this.properties = properties;
    rmo = (RedisModulesOperations<String>) ac.getBean("redisModulesOperations");
    mappingContext = (RedisMappingContext) ac.getBean("redisEnhancedMappingContext");
    this.gsonBuilder = gsonBuilder;
  }

  /**
   * Checks if a string contains SpEL expression markers (#{...}).
   * This is used to determine if an expression needs dynamic re-evaluation
   * at runtime versus using a cached static value.
   *
   * @param expression the expression to check
   * @return true if the expression contains SpEL markers
   */
  private boolean containsSpelExpression(String expression) {
    return expression != null && expression.contains("#{") && expression.contains("}");
  }

  /**
   * Evaluates a SpEL expression if it's detected, otherwise returns the original value.
   *
   * @param expression   the expression to evaluate (may contain SpEL syntax)
   * @param defaultValue the default value to use if evaluation fails or expression is empty
   * @return the evaluated expression result or the original value
   */
  private String evaluateExpression(String expression, String defaultValue) {
    if (expression == null) {
      return defaultValue;
    }

    // Check if the string contains SpEL expression markers
    if (!expression.contains("#{")) {
      return expression;
    }

    // Check for malformed expressions (has opening but no closing bracket)
    if (!expression.contains("}")) {
      logger.warn(String.format("Malformed SpEL expression '%s': missing closing bracket. Using default value.",
          expression));
      return defaultValue;
    }

    try {
      // Create evaluation context with Spring beans
      StandardEvaluationContext context = new StandardEvaluationContext();
      context.setBeanResolver(new BeanFactoryResolver(ac));
      context.setVariable("environment", ac.getEnvironment());
      context.setVariable("systemProperties", System.getProperties());

      // Process template expressions - replace #{...} with evaluated values
      String processedExpression = expression;
      int startIndex = 0;
      boolean hasFailedExpressions = false;

      while ((startIndex = processedExpression.indexOf("#{", startIndex)) != -1) {
        int endIndex = processedExpression.indexOf("}", startIndex);
        if (endIndex == -1) {
          break;
        }

        String spelPart = processedExpression.substring(startIndex + 2, endIndex);
        try {
          Expression spelExpression = spelParser.parseExpression(spelPart);
          Object result = spelExpression.getValue(context);
          if (result == null) {
            // Null results should trigger fallback to default
            logger.warn(String.format("SpEL expression part '%s' returned null. Using default value.", spelPart));
            hasFailedExpressions = true;
            startIndex = endIndex + 1;
          } else {
            String resultStr = result.toString();
            processedExpression = processedExpression.substring(0, startIndex) + resultStr + processedExpression
                .substring(endIndex + 1);
          }
        } catch (Exception e) {
          // If any expression fails to evaluate, we should use the default fallback
          logger.warn(String.format("Failed to evaluate SpEL expression part '%s': %s", spelPart, e.getMessage()));
          hasFailedExpressions = true;
          startIndex = endIndex + 1;
        }
      }

      // If any expressions failed, return the default value
      if (hasFailedExpressions) {
        return defaultValue;
      }

      return processedExpression;
    } catch (Exception e) {
      logger.warn(String.format("Failed to evaluate SpEL expression '%s': %s. Using default value.", expression, e
          .getMessage()));
      return defaultValue;
    }
  }

  /**
   * Creates search indices for all entities annotated with the specified annotation class.
   * Scans for bean definitions and creates indices for each discovered entity class.
   *
   * @param cls the annotation class to search for (e.g., Document.class or RedisHash.class)
   */
  public void createIndicesFor(Class<?> cls) {
    Set<BeanDefinition> beanDefs = new HashSet<>(getBeanDefinitionsFor(ac, cls));

    logger.info(String.format("Found %s @%s annotated Beans...", beanDefs.size(), cls.getSimpleName()));

    for (BeanDefinition beanDef : beanDefs) {
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        logger.info(String.format("Creating index for %s annotated Entity...", cl.getSimpleName()));
        createIndexFor(cl);
      } catch (ClassNotFoundException e) {
        logger.warn(String.format(SKIPPING_INDEX_CREATION, beanDef.getBeanClassName(), e.getMessage()));
      }
    }
  }

  /**
   * Creates a RediSearch index for the specified entity class.
   * Processes all indexed fields, configures index parameters, and creates the search index
   * with appropriate schema fields based on the entity's annotations.
   *
   * @param cl the entity class to create an index for
   */
  public void createIndexFor(Class<?> cl) {
    Optional<IndexDataType> maybeType = determineIndexTarget(cl);
    IndexDataType idxType;
    if (maybeType.isPresent()) {
      idxType = maybeType.get();
    } else {
      return;
    }
    boolean isDocument = idxType == IndexDataType.JSON;
    Optional<Document> document = isDocument ? Optional.of(cl.getAnnotation(Document.class)) : Optional.empty();
    Optional<RedisHash> hash = !isDocument ? Optional.of(cl.getAnnotation(RedisHash.class)) : Optional.empty();
    Optional<IndexingOptions> maybeIndexingOptions = Optional.ofNullable(cl.getAnnotation(IndexingOptions.class));

    String indexName = "";
    String defaultIndexName = cl.getName() + "Idx";
    Optional<String> maybeScoreField;
    try {
      if (isDocument) {
        // IndexingOptions overrides Document#
        if (maybeIndexingOptions.isPresent()) {
          String rawIndexName = maybeIndexingOptions.get().indexName();
          indexName = evaluateExpression(rawIndexName, defaultIndexName);
        } else {
          indexName = document.get().indexName();
        }
        indexName = indexName.isBlank() ? defaultIndexName : indexName;
      } else {
        if (maybeIndexingOptions.isPresent()) {
          String rawIndexName = maybeIndexingOptions.get().indexName();
          indexName = evaluateExpression(rawIndexName, defaultIndexName);
        } else {
          indexName = defaultIndexName;
        }
      }

      logger.info(String.format("Found @%s annotated class: %s", idxType, cl.getName()));

      final List<java.lang.reflect.Field> allClassFields = getDeclaredFieldsTransitively(cl);

      List<SearchField> searchFields = processIndexedFields(allClassFields, isDocument);

      for (SearchField field : searchFields) {
        registerAlias(cl, field.getField().getName(), field.getSchemaField().getFieldName().getAttribute());
      }

      maybeScoreField = getDocumentScoreField(allClassFields, isDocument);
      createIndexedFieldsForIdFields(cl, searchFields.stream().map(SearchField::getSchemaField).toList(), isDocument)
          .forEach(searchFields::add);

      SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);

      FTCreateParams params = createIndexDefinition(cl, idxType);

      Optional<String> maybeEntityPrefix;
      if (isDocument) {
        maybeEntityPrefix = document.map(Document::value).filter(ObjectUtils::isNotEmpty);
        maybeScoreField.ifPresent(params::scoreField);
      } else {
        maybeEntityPrefix = hash.map(RedisHash::value).filter(ObjectUtils::isNotEmpty);
      }

      // Check for dynamic key prefix in IndexingOptions
      String entityPrefix;
      if (maybeIndexingOptions.isPresent() && !maybeIndexingOptions.get().keyPrefix().isBlank()) {
        String rawKeyPrefix = maybeIndexingOptions.get().keyPrefix();
        String defaultPrefix = maybeEntityPrefix.orElse(getEntityPrefix(cl));
        entityPrefix = evaluateExpression(rawKeyPrefix, defaultPrefix);
      } else {
        entityPrefix = maybeEntityPrefix.orElse(getEntityPrefix(cl));
      }
      entityPrefix = entityPrefix.endsWith(":") ? entityPrefix : entityPrefix + ":";
      params.prefix(entityPrefix);
      addKeySpaceMapping(entityPrefix, cl);
      updateTTLSettings(cl, entityPrefix, isDocument, document, allClassFields);
      List<SchemaField> fields = searchFields.stream().map(SearchField::getSchemaField).toList();
      entityClassToSchema.put(cl, searchFields);
      entityClassToIndexName.put(cl, indexName);
      if (maybeIndexingOptions.isPresent()) {
        IndexingOptions options = maybeIndexingOptions.get();
        switch (options.creationMode()) {
          case SKIP_IF_EXIST:
            opsForSearch.createIndex(params, fields);
            logger.info(String.format("Created index %s...", indexName));
            // Create sorted sets for lexicographic fields
            createSortedSetsForLexicographicFields(cl, entityPrefix);
            break;
          case DROP_AND_RECREATE:
            if (indexExistsFor(cl)) {
              opsForSearch.dropIndex();
              logger.info(String.format("Dropped index %s", indexName));
            }
            opsForSearch.createIndex(params, fields);
            logger.info(String.format("Created index %s", indexName));
            // Create sorted sets for lexicographic fields
            createSortedSetsForLexicographicFields(cl, entityPrefix);
            break;
          case SKIP_ALWAYS:
            // do nothing and like it!
            logger.info(String.format("Skipped index creation for %s", cl.getSimpleName()));
            break;
        }
      } else {
        opsForSearch.createIndex(params, fields);
        logger.info(String.format("Created index %s", indexName));
      }

      // Create sorted sets for lexicographic fields
      createSortedSetsForLexicographicFields(cl, entityPrefix);
    } catch (Exception e) {
      logger.warn(String.format(SKIPPING_INDEX_CREATION, indexName, e.getMessage()));
    }
  }

  /**
   * Drops the search index and all associated documents for the specified entity class.
   * This is a destructive operation that removes both the index definition and all indexed data.
   *
   * @param cl the entity class whose index and documents should be dropped
   */
  public void dropIndexAndDocumentsFor(Class<?> cl) {
    dropIndex(cl, true, false);
  }

  /**
   * Drops the existing search index for the specified entity class and recreates it.
   * The documents remain intact, only the index definition is recreated.
   *
   * @param cl the entity class whose index should be dropped and recreated
   */
  public void dropAndRecreateIndexFor(Class<?> cl) {
    dropIndex(cl, false, true);
  }

  /**
   * Drops the search index for the specified entity class.
   * The documents remain in Redis, only the index definition is removed.
   *
   * @param cl the entity class whose index should be dropped
   */
  public void dropIndexFor(Class<?> cl) {
    dropIndex(cl, false, false);
  }

  /**
   * Retrieves the index name for the specified keyspace.
   * Looks up the entity class associated with the keyspace and returns its index name.
   *
   * @param keyspace the Redis keyspace to look up
   * @return the index name associated with the keyspace
   */
  public String getIndexName(String keyspace) {
    return getIndexName(keyspaceToEntityClass.get(getKeyspace(keyspace)));
  }

  /**
   * Retrieves the index name for the specified entity class.
   * Returns the configured index name or generates a default name if not found.
   *
   * <p>If the entity's {@code @IndexingOptions} annotation contains a dynamic SpEL expression
   * (e.g., {@code "products_#{@tenantService.getCurrentTenant()}_idx"}), the expression is
   * re-evaluated on each call to support runtime context changes like multi-tenancy.
   * Static index names are cached for performance.
   *
   * @param entityClass the entity class to get the index name for
   * @return the index name for the entity class, or a default name based on the class name
   */
  public String getIndexName(Class<?> entityClass) {
    // Check if the entity has @IndexingOptions with a dynamic SpEL expression
    Optional<IndexingOptions> maybeIndexingOptions = Optional.ofNullable(entityClass != null ?
        entityClass.getAnnotation(IndexingOptions.class) :
        null);
    String defaultIndexName = entityClass != null ? entityClass.getName() + "Idx" : "Idx";

    if (maybeIndexingOptions.isPresent()) {
      String rawIndexName = maybeIndexingOptions.get().indexName();
      // If the index name contains SpEL, always re-evaluate it (don't use cache)
      if (containsSpelExpression(rawIndexName)) {
        return evaluateExpression(rawIndexName, defaultIndexName);
      }
    }

    // For static names, use the cached value if available
    if (entityClass != null && entityClassToIndexName.containsKey(entityClass)) {
      return entityClassToIndexName.get(entityClass);
    } else {
      // Evaluate the expression (which may be static or SpEL)
      if (maybeIndexingOptions.isPresent()) {
        String rawIndexName = maybeIndexingOptions.get().indexName();
        return evaluateExpression(rawIndexName, defaultIndexName);
      }
      return defaultIndexName;
    }
  }

  /**
   * Adds a mapping between a Redis keyspace and an entity class.
   * This establishes the relationship between Redis key prefixes and Java entity types
   * for search index management.
   *
   * @param keyspace    the Redis keyspace (key prefix)
   * @param entityClass the entity class associated with the keyspace
   */
  public void addKeySpaceMapping(String keyspace, Class<?> entityClass) {
    String key = getKeyspace(keyspace);
    keyspaceToEntityClass.put(key, entityClass);
    entityClassToKeySpace.put(entityClass, key);
    indexedEntityClasses.add(entityClass);
  }

  /**
   * Removes the mapping between a Redis keyspace and an entity class.
   * This method cleans up the internal mappings when an entity class is no longer
   * associated with a particular keyspace, typically during index cleanup operations.
   *
   * @param keyspace    the Redis keyspace (key prefix) to remove from mappings
   * @param entityClass the entity class to disassociate from the keyspace
   */
  public void removeKeySpaceMapping(String keyspace, Class<?> entityClass) {
    String key = getKeyspace(keyspace);
    keyspaceToEntityClass.remove(key);
    entityClassToKeySpace.remove(entityClass);
    entityClassToIndexName.remove(entityClass);
    indexedEntityClasses.remove(entityClass);
  }

  /**
   * Retrieves the entity class associated with the specified keyspace.
   *
   * @param keyspace the Redis keyspace to look up
   * @return the entity class mapped to the keyspace, or null if no mapping exists
   */
  public Class<?> getEntityClassForKeyspace(String keyspace) {
    return keyspaceToEntityClass.get(getKeyspace(keyspace));
  }

  /**
   * Retrieves the identifier filter for the specified entity class.
   * Identifier filters are used to process entity IDs before indexing or querying.
   *
   * @param entityClass the entity class to get the identifier filter for
   * @return an Optional containing the identifier filter, or empty if none is configured
   */
  public Optional<IdentifierFilter<?>> getIdentifierFilterFor(Class<?> entityClass) {
    if (entityClass != null && entityClassToIdentifierFilter.containsKey(entityClass)) {
      return Optional.of(entityClassToIdentifierFilter.get(entityClass));
    } else {
      return Optional.empty();
    }
  }

  /**
   * Retrieves the identifier filter for the specified keyspace.
   * Looks up the entity class for the keyspace and returns its identifier filter.
   *
   * @param keyspace the Redis keyspace to get the identifier filter for
   * @return an Optional containing the identifier filter, or empty if none is configured
   */
  public Optional<IdentifierFilter<?>> getIdentifierFilterFor(String keyspace) {
    return getIdentifierFilterFor(keyspaceToEntityClass.get(keyspace.endsWith(":") ? keyspace : keyspace + ":"));
  }

  /**
   * Retrieves the Redis keyspace (key prefix) for the specified entity class.
   *
   * <p>If the entity's {@code @IndexingOptions} annotation contains a dynamic SpEL expression
   * in the keyPrefix (e.g., {@code "#{@tenantService.getCurrentTenant()}:products:"}), the expression
   * is re-evaluated on each call to support runtime context changes like multi-tenancy.
   * Static keyspaces are cached for performance.
   *
   * <p>If no explicit mapping exists, derives the keyspace from the entity's persistent configuration
   * or uses the class name as fallback.
   *
   * @param entityClass the entity class to get the keyspace for
   * @return the Redis keyspace associated with the entity class
   */
  public String getKeyspaceForEntityClass(Class<?> entityClass) {
    if (entityClass == null) {
      return null;
    }

    // Check if the entity has @IndexingOptions with a dynamic SpEL expression for keyPrefix
    Optional<IndexingOptions> maybeIndexingOptions = Optional.ofNullable(entityClass.getAnnotation(
        IndexingOptions.class));

    if (maybeIndexingOptions.isPresent()) {
      String rawKeyPrefix = maybeIndexingOptions.get().keyPrefix();
      // If the key prefix contains SpEL, always re-evaluate it (don't use cache)
      if (!rawKeyPrefix.isBlank() && containsSpelExpression(rawKeyPrefix)) {
        String defaultKeyspace = deriveDefaultKeyspace(entityClass);
        String keyspace = evaluateExpression(rawKeyPrefix, defaultKeyspace);
        // Ensure keyspace ends with ":"
        return keyspace.endsWith(":") ? keyspace : keyspace + ":";
      }
    }

    // For static names, use the cached value if available
    String keyspace = entityClassToKeySpace.get(entityClass);
    if (keyspace == null) {
      keyspace = deriveDefaultKeyspace(entityClass);
    }
    return keyspace;
  }

  /**
   * Derives the default keyspace for an entity class from the mapping context.
   *
   * @param entityClass the entity class
   * @return the derived keyspace with trailing colon
   */
  private String deriveDefaultKeyspace(Class<?> entityClass) {
    var persistentEntity = mappingContext.getPersistentEntity(entityClass);
    if (persistentEntity != null) {
      String entityKeySpace = persistentEntity.getKeySpace();
      return (entityKeySpace != null ? entityKeySpace : entityClass.getName()) + ":";
    }
    return entityClass.getName() + ":";
  }

  /**
   * Gets the key prefix for a given entity class.
   *
   * @param entityClass the entity class
   * @return the key prefix used for this entity class
   */
  public String getKeyspacePrefix(Class<?> entityClass) {
    return getKeyspaceForEntityClass(entityClass);
  }

  /**
   * Checks whether an index definition exists for the specified entity class.
   * This method verifies if the entity class has been registered and processed
   * for index creation, regardless of whether the actual Redis index exists.
   * 
   * @param entityClass the entity class to check for index definition
   * @return true if an index definition exists for the entity class, false otherwise
   */
  public boolean indexDefinitionExistsFor(Class<?> entityClass) {
    return indexedEntityClasses.contains(entityClass);
  }

  /**
   * Checks whether a RediSearch index actually exists in Redis for the specified entity class.
   * This method queries Redis directly to verify the existence of the search index,
   * unlike {@link #indexDefinitionExistsFor(Class)} which only checks internal mappings.
   * 
   * @param entityClass the entity class to check for index existence in Redis
   * @return true if the search index exists in Redis, false otherwise
   * @throws JedisDataException if a Redis error occurs other than index not found errors
   */
  public boolean indexExistsFor(Class<?> entityClass) {
    try {
      return getIndexInfo(entityClass) != null;
    } catch (JedisDataException jde) {
      String errorMessage = jde.getMessage();
      if (errorMessage != null) {
        String lowerCaseMessage = errorMessage.toLowerCase();
        // Handle various error messages for missing index across different Redis versions
        // - "Unknown index name" or "Unknown Index name" - Redis Stack / Redis 7.x  
        // - Potentially other variations in Redis 8.0+
        if (lowerCaseMessage.contains("unknown index") || lowerCaseMessage.contains("no such index") || lowerCaseMessage
            .contains("index does not exist") || lowerCaseMessage.contains("not found")) {
          return false;
        }
      }
      throw jde;
    }
  }

  Map<String, Object> getIndexInfo(Class<?> entityClass) {
    String indexName = entityClassToIndexName.get(entityClass);
    if (indexName == null) {
      // No index mapping exists for this entity class
      return null;
    }
    SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
    return opsForSearch.getInfo();
  }

  /**
   * Retrieves the search schema fields for the specified entity class.
   * The schema contains the complete field definitions used to create the RediSearch index,
   * including field names, types, and indexing options.
   * 
   * @param entityClass the entity class to get the search schema for
   * @return a list of SearchField objects representing the index schema,
   *         or null if no schema has been generated for the entity class
   */
  public List<SearchField> getSchemaFor(Class<?> entityClass) {
    return entityClassToSchema.get(entityClass);
  }

  private List<SearchField> findIndexFields(java.lang.reflect.Field field, String prefix, boolean isDocument) {
    List<SearchField> fields = new ArrayList<>();

    if (field.isAnnotationPresent(Indexed.class)) {
      logger.info(String.format("Found @Indexed annotation on field of type: %s", field.getType()));

      Indexed indexed = field.getAnnotation(Indexed.class);

      // Track lexicographic fields
      if (indexed.lexicographic()) {
        Class<?> entityClass = field.getDeclaringClass();
        entityClassToLexicographicFields.computeIfAbsent(entityClass, k -> new HashSet<>()).add(field.getName());
        logger.info(String.format("Tracked lexicographic field %s on class %s", field.getName(), entityClass
            .getName()));
      }

      Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(field.getType());

      if (field.isAnnotationPresent(Reference.class)) {
        //
        // @Reference @Indexed fields: Create schema field for the reference entity @Id
        // field
        //
        logger.debug("🪲Found @Reference field " + field.getName() + " in " + field.getDeclaringClass()
            .getSimpleName());
        createIndexedFieldForReferenceIdField(field, isDocument).ifPresent(fields::add);

        // Also create index fields for the referenced entity's indexed/searchable fields
        // This enables searching like RefVehicle$.OWNER_NAME.eq("John")
        createIndexedFieldsForReferencedEntity(field, isDocument, prefix).forEach(fields::add);
      } else if (indexed.schemaFieldType() == SchemaFieldType.AUTODETECT) {
        //
        // Any Character class, Boolean or Enum with AUTODETECT -> Tag Search Field
        // Also UUID and Ulid (classes whose toString() is a valid text representation
        // of the value)
        //
        if (CharSequence.class.isAssignableFrom(fieldType) || //
            (fieldType == Boolean.class) || (fieldType == UUID.class) || (fieldType == Ulid.class)) {
          fields.add(SearchField.of(field, indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(), indexed
              .separator(), indexed.arrayIndex(), indexed.alias(), indexed.indexMissing(), indexed.indexEmpty())));
        } else if (fieldType.isEnum()) {
          if (Objects.requireNonNull(indexed.serializationHint()) == SerializationHint.ORDINAL) {
            fields.add(SearchField.of(field, indexAsNumericFieldFor(field, isDocument, prefix, indexed.sortable(),
                indexed.noindex(), indexed.alias(), indexed.indexMissing(), indexed.indexEmpty())));
            gsonBuilder.registerTypeAdapter(fieldType, EnumTypeAdapter.of(fieldType));
          } else {
            fields.add(SearchField.of(field, indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(), indexed
                .separator(), indexed.arrayIndex(), indexed.alias(), indexed.indexMissing(), indexed.indexEmpty())));
          }
        }
        //
        // Any Numeric class -> Numeric Search Field
        //
        else if ( //
        Number.class.isAssignableFrom(fieldType) || //
            (fieldType == LocalDateTime.class) || //
            (field.getType() == LocalDate.class) || //
            (field.getType() == Date.class) || //
            (field.getType() == Instant.class) || //
            (field.getType() == OffsetDateTime.class) //
        ) {
          fields.add(SearchField.of(field, indexAsNumericFieldFor(field, isDocument, prefix, indexed.sortable(), indexed
              .noindex(), indexed.alias(), indexed.indexMissing(), indexed.indexEmpty())));
        }
        //
        // Set / List
        //
        else if (Set.class.isAssignableFrom(fieldType) || List.class.isAssignableFrom(fieldType)) {
          Optional<Class<?>> maybeCollectionType = getCollectionElementClass(field);

          if (maybeCollectionType.isPresent()) {
            // https://redis.io/docs/stack/search/indexing_json/#index-limitations
            // JSON array:
            // - Array of strings as TAG or TEXT.
            // - Array of numbers as NUMERIC or VECTOR.
            // - Array of geo coordinates as GEO.
            // - null values in such arrays are ignored.
            Class<?> collectionType = maybeCollectionType.get();

            if (CharSequence.class.isAssignableFrom(collectionType) || (collectionType == Boolean.class)) {
              fields.add(SearchField.of(field, indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(), indexed
                  .separator(), indexed.arrayIndex(), indexed.alias(), indexed.indexMissing(), indexed.indexEmpty())));
            } else if (isDocument) {
              if (Number.class.isAssignableFrom(collectionType)) {
                fields.add(SearchField.of(field, indexAsNumericFieldFor(field, true, prefix, indexed.sortable(), indexed
                    .noindex(), indexed.alias(), indexed.indexMissing(), indexed.indexEmpty())));
              } else if (collectionType == Point.class) {
                fields.add(SearchField.of(field, indexAsGeoFieldFor(field, true, prefix, indexed.alias())));
              } else if (collectionType == UUID.class || collectionType == Ulid.class) {
                fields.add(SearchField.of(field, indexAsTagFieldFor(field, true, prefix, indexed.sortable(), indexed
                    .separator(), 0, indexed.alias(), indexed.indexMissing(), indexed.indexEmpty())));
              } else {
                // Index nested JSON fields
                logger.debug(String.format("Found nested field on field of type: %s", field.getType()));
                fields.addAll(indexAsNestedFieldFor(field, prefix));
              }
            }
          } else {
            logger.debug(String.format("Could not determine the type of elements in the collection %s in entity %s",
                field.getName(), field.getDeclaringClass().getSimpleName()));
          }
        }
        //
        // Map fields (only for JSON documents)
        //
        else if (Map.class.isAssignableFrom(fieldType) && isDocument) {
          logger.info(String.format("Processing Map field: %s of type %s", field.getName(), fieldType));
          Optional<Class<?>> maybeValueType = getMapValueClass(field);
          if (maybeValueType.isPresent()) {
            Class<?> valueType = maybeValueType.get();
            logger.info(String.format("Map field %s has value type: %s", field.getName(), valueType));

            // Use the Map field's alias if specified, otherwise use the field name
            String mapFieldNameForIndex = (indexed.alias() != null && !indexed.alias().isEmpty()) ?
                indexed.alias() :
                field.getName();

            String mapJsonPath = (prefix == null || prefix.isBlank()) ?
                "$." + field.getName() + ".*" :
                "$." + prefix + "." + field.getName() + ".*";
            String mapFieldAlias = mapFieldNameForIndex + "_values";

            // Support all value types that we support for regular fields
            if (CharSequence.class.isAssignableFrom(
                valueType) || valueType == UUID.class || valueType == Ulid.class || valueType.isEnum()) {
              // Index as TAG field
              TagField tagField = TagField.of(FieldName.of(mapJsonPath).as(mapFieldAlias));
              if (indexed.sortable())
                tagField.sortable();
              if (indexed.indexMissing())
                tagField.indexMissing();
              if (indexed.indexEmpty())
                tagField.indexEmpty();
              if (!indexed.separator().isEmpty()) {
                tagField.separator(indexed.separator().charAt(0));
              }
              fields.add(SearchField.of(field, tagField));
              logger.info(String.format("Added TAG field for Map: %s as %s", field.getName(), mapFieldAlias));
            } else if (Number.class.isAssignableFrom(
                valueType) || valueType == Boolean.class || valueType == LocalDateTime.class || valueType == LocalDate.class || valueType == Date.class || valueType == Instant.class || valueType == OffsetDateTime.class) {
              // Index as NUMERIC field
              NumericField numField = NumericField.of(FieldName.of(mapJsonPath).as(mapFieldAlias));
              if (indexed.sortable())
                numField.sortable();
              if (indexed.noindex())
                numField.noIndex();
              if (indexed.indexMissing())
                numField.indexMissing();
              // NumericField doesn't have indexEmpty() method
              fields.add(SearchField.of(field, numField));
              logger.info(String.format("Added NUMERIC field for Map: %s as %s", field.getName(), mapFieldAlias));
            } else if (valueType == Point.class) {
              // Index as GEO field
              GeoField geoField = GeoField.of(FieldName.of(mapJsonPath).as(mapFieldAlias));
              fields.add(SearchField.of(field, geoField));
              logger.info(String.format("Added GEO field for Map: %s as %s", field.getName(), mapFieldAlias));
            } else {
              // Handle complex object values in Map by recursively indexing their @Indexed fields
              logger.info(String.format("Processing complex object Map field: %s with value type %s", field.getName(),
                  valueType.getName()));

              // Recursively process @Indexed fields within the Map value type
              for (java.lang.reflect.Field subfield : getDeclaredFieldsTransitively(valueType)) {
                if (subfield.isAnnotationPresent(Indexed.class)) {
                  Indexed subfieldIndexed = subfield.getAnnotation(Indexed.class);
                  // Get the actual JSON field name (check for @JsonProperty or @SerializedName)
                  String jsonFieldName = getJsonFieldName(subfield);
                  String nestedJsonPath = (prefix == null || prefix.isBlank()) ?
                      "$." + field.getName() + ".*." + jsonFieldName :
                      "$." + prefix + "." + field.getName() + ".*." + jsonFieldName;
                  // Respect the alias annotation on the nested field
                  String subfieldAlias = (subfieldIndexed.alias() != null && !subfieldIndexed.alias().isEmpty()) ?
                      subfieldIndexed.alias() :
                      subfield.getName();
                  // Use the Map field's alias (if present) for the nested field alias prefix
                  String nestedFieldAlias = mapFieldNameForIndex + "_" + subfieldAlias;

                  logger.info(String.format("Processing nested field %s in Map value type, path: %s, alias: %s",
                      subfield.getName(), nestedJsonPath, nestedFieldAlias));

                  Class<?> subfieldType = subfield.getType();

                  // Create appropriate index field based on subfield type
                  if (CharSequence.class.isAssignableFrom(
                      subfieldType) || subfieldType == UUID.class || subfieldType == Ulid.class || subfieldType
                          .isEnum()) {
                    // Index as TAG field
                    TagField tagField = TagField.of(FieldName.of(nestedJsonPath).as(nestedFieldAlias));
                    if (subfieldIndexed.sortable())
                      tagField.sortable();
                    if (subfieldIndexed.indexMissing())
                      tagField.indexMissing();
                    if (subfieldIndexed.indexEmpty())
                      tagField.indexEmpty();
                    if (!subfieldIndexed.separator().isEmpty()) {
                      tagField.separator(subfieldIndexed.separator().charAt(0));
                    }
                    fields.add(SearchField.of(subfield, tagField));
                    logger.info(String.format("Added nested TAG field for Map value: %s", nestedFieldAlias));
                  } else if (Number.class.isAssignableFrom(
                      subfieldType) || subfieldType == Boolean.class || subfieldType == LocalDateTime.class || subfieldType == LocalDate.class || subfieldType == Date.class || subfieldType == Instant.class || subfieldType == OffsetDateTime.class) {
                    // Index as NUMERIC field
                    NumericField numField = NumericField.of(FieldName.of(nestedJsonPath).as(nestedFieldAlias));
                    if (subfieldIndexed.sortable())
                      numField.sortable();
                    if (subfieldIndexed.noindex())
                      numField.noIndex();
                    if (subfieldIndexed.indexMissing())
                      numField.indexMissing();
                    fields.add(SearchField.of(subfield, numField));
                    logger.info(String.format("Added nested NUMERIC field for Map value: %s", nestedFieldAlias));
                  } else if (subfieldType == Point.class) {
                    // Index as GEO field
                    GeoField geoField = GeoField.of(FieldName.of(nestedJsonPath).as(nestedFieldAlias));
                    fields.add(SearchField.of(subfield, geoField));
                    logger.info(String.format("Added nested GEO field for Map value: %s", nestedFieldAlias));
                  }
                }
              }
            }
          }
        }
        //
        // Point
        //
        else if (fieldType == Point.class) {
          fields.add(SearchField.of(field, indexAsGeoFieldFor(field, isDocument, prefix, indexed.alias())));
        }
        //
        // Recursively explore the fields for Index annotated fields
        //
        else {
          for (java.lang.reflect.Field subfield : getDeclaredFieldsTransitively(field.getType())) {
            String subfieldPrefix = (prefix == null || prefix.isBlank()) ?
                field.getName() :
                String.join(".", prefix, field.getName());
            fields.addAll(findIndexFields(subfield, subfieldPrefix, isDocument));
          }
        }
      } else { // Schema field type hardcoded/set in @Indexed
        switch (indexed.schemaFieldType()) {
          case TAG -> fields.add(SearchField.of(field, indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(),
              indexed.separator(), indexed.arrayIndex(), indexed.alias(), indexed.indexMissing(), indexed
                  .indexEmpty())));
          case NUMERIC -> fields.add(SearchField.of(field, indexAsNumericFieldFor(field, isDocument, prefix, indexed
              .sortable(), indexed.noindex(), indexed.alias(), indexed.indexMissing(), indexed.indexEmpty())));
          case GEO -> fields.add(SearchField.of(field, indexAsGeoFieldFor(field, true, prefix, indexed.alias())));
          case VECTOR -> fields.add(SearchField.of(field, indexAsVectorFieldFor(field, isDocument, prefix, indexed)));
          case NESTED -> {
            Class<?> nestedType = field.getType();

            // Handle List<Model> fields by extracting the element type
            if (List.class.isAssignableFrom(nestedType) || Set.class.isAssignableFrom(nestedType)) {
              Optional<Class<?>> maybeCollectionType = getCollectionElementClass(field);
              if (maybeCollectionType.isPresent()) {
                nestedType = maybeCollectionType.get();
                logger.info(String.format("Processing nested array field %s with element type %s", field.getName(),
                    nestedType.getSimpleName()));
              } else {
                logger.warn(String.format("Could not determine element type for nested field %s", field.getName()));
                break;
              }
            }

            // Process all fields of the nested type automatically
            for (java.lang.reflect.Field subfield : com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(
                nestedType)) {
              String subfieldPrefix = (prefix == null || prefix.isBlank()) ?
                  field.getName() :
                  String.join(".", prefix, field.getName());

              // For nested fields, automatically create index fields when the parent field is annotated with @Indexed(schemaFieldType = SchemaFieldType.NESTED)
              fields.addAll(createNestedIndexFields(field, subfield, subfieldPrefix, isDocument));
            }
          }
          default -> {
          } // NOOP
        }
      }
    }

    // Searchable - behaves like Text indexed
    else if (field.isAnnotationPresent(Searchable.class)) {
      logger.info(String.format("Found @Searchable annotation on field of type: %s", field.getType()));
      Searchable searchable = field.getAnnotation(Searchable.class);

      // Track lexicographic fields
      if (searchable.lexicographic()) {
        Class<?> entityClass = field.getDeclaringClass();
        entityClassToLexicographicFields.computeIfAbsent(entityClass, k -> new HashSet<>()).add(field.getName());
        logger.info(String.format("Tracked lexicographic field %s on class %s", field.getName(), entityClass
            .getName()));
      }

      fields.add(SearchField.of(field, indexAsTextFieldFor(field, isDocument, prefix, searchable)));
    }
    // Text
    else if (field.isAnnotationPresent(TextIndexed.class)) {
      TextIndexed ti = field.getAnnotation(TextIndexed.class);
      fields.add(SearchField.of(field, indexAsTextFieldFor(field, isDocument, prefix, ti)));
    }
    // Tag
    else if (field.isAnnotationPresent(TagIndexed.class)) {
      TagIndexed ti = field.getAnnotation(TagIndexed.class);
      fields.add(SearchField.of(field, indexAsTagFieldFor(field, isDocument, prefix, ti)));
    }
    // Geo
    else if (field.isAnnotationPresent(GeoIndexed.class)) {
      GeoIndexed gi = field.getAnnotation(GeoIndexed.class);
      fields.add(SearchField.of(field, indexAsGeoFieldFor(field, isDocument, prefix, gi)));
    }
    // Numeric
    else if (field.isAnnotationPresent(NumericIndexed.class)) {
      NumericIndexed ni = field.getAnnotation(NumericIndexed.class);
      fields.add(SearchField.of(field, indexAsNumericFieldFor(field, isDocument, prefix, ni)));
    }
    // Vector
    else if (field.isAnnotationPresent(VectorIndexed.class)) {
      VectorIndexed vi = field.getAnnotation(VectorIndexed.class);
      fields.add(SearchField.of(field, indexAsVectorFieldFor(field, isDocument, prefix, vi)));
    }

    return fields;
  }

  private TagField indexAsTagFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, TagIndexed ti) {
    FieldName fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(ti.alias()), Optional.empty());

    return getTagField(fieldName, ti.separator(), false);
  }

  private VectorField indexAsVectorFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      Indexed indexed) {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("TYPE", indexed.type().toString());
    attributes.put("DIM", indexed.dimension());
    attributes.put("DISTANCE_METRIC", indexed.distanceMetric());

    if (indexed.initialCapacity() > 0) {
      attributes.put("INITIAL_CAP", indexed.initialCapacity());
    }

    // Optional parameters for FLAT
    if (indexed.algorithm().equals(VectorField.VectorAlgorithm.FLAT) && (indexed.blockSize() > 0)) {
      attributes.put("BLOCK_SIZE", indexed.blockSize());
    }

    if (indexed.algorithm().equals(VectorField.VectorAlgorithm.HNSW)) {
      // Optional parameters for HNSW
      attributes.put("M", indexed.m());
      attributes.put("EF_CONSTRUCTION", indexed.efConstruction());
      if (indexed.efRuntime() != 10) {
        attributes.put("EF_RUNTIME", indexed.efRuntime());
      }
      if (indexed.epsilon() != 0.01) {
        attributes.put("EPSILON", indexed.epsilon());
      }
    }

    FieldName fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(indexed.alias()), Optional
        .empty());

    return new VectorField(fieldName, indexed.algorithm(), attributes);
  }

  private VectorField indexAsVectorFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      VectorIndexed vi) {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("TYPE", vi.type().toString());
    attributes.put("DIM", vi.dimension());
    attributes.put("DISTANCE_METRIC", vi.distanceMetric());

    if (vi.initialCapacity() > 0) {
      attributes.put("INITIAL_CAP", vi.initialCapacity());
    }

    // Optional parameters for FLAT
    if (vi.algorithm().equals(VectorField.VectorAlgorithm.FLAT) && (vi.blockSize() > 0)) {
      attributes.put("BLOCK_SIZE", vi.blockSize());
    }

    if (vi.algorithm().equals(VectorField.VectorAlgorithm.HNSW)) {
      // Optional parameters for HNSW
      attributes.put("M", vi.m());
      attributes.put("EF_CONSTRUCTION", vi.efConstruction());
      if (vi.efRuntime() != 10) {
        attributes.put("EF_RUNTIME", vi.efRuntime());
      }
      if (vi.epsilon() != 0.01) {
        attributes.put("EPSILON", vi.epsilon());
      }
    }

    FieldName fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(vi.alias()), Optional.empty());

    return new VectorField(fieldName, vi.algorithm(), attributes);
  }

  private SchemaField indexAsTagFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      boolean sortable, String separator, int arrayIndex, String annotationAlias) {
    return indexAsTagFieldFor(field, isDocument, prefix, sortable, separator, arrayIndex, annotationAlias, false,
        false);
  }

  private SchemaField indexAsTagFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      boolean sortable, String separator, int arrayIndex, String annotationAlias, boolean indexMissing,
      boolean indexEmpty) {
    FieldName fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(annotationAlias), Optional.of(
        arrayIndex));
    return getTagField(fieldName, separator, sortable, indexMissing, indexEmpty);
  }

  private TextField indexAsTextFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      TextIndexed ti) {
    var fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(ti.alias()), Optional.empty());
    String phonetic = ObjectUtils.isEmpty(ti.phonetic()) ? null : ti.phonetic();
    return getTextField(fieldName, ti.weight(), ti.sortable(), ti.nostem(), ti.noindex(), phonetic, ti.indexMissing(),
        ti.indexEmpty());
  }

  private TextField indexAsTextFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      Searchable ti) {
    var fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(ti.alias()), Optional.empty());
    String phonetic = ObjectUtils.isEmpty(ti.phonetic()) ? null : ti.phonetic();
    return getTextField(fieldName, ti.weight(), ti.sortable(), ti.nostem(), ti.noindex(), phonetic, ti.indexMissing(),
        ti.indexEmpty());
  }

  private GeoField indexAsGeoFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, GeoIndexed gi) {
    var fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(gi.alias()), Optional.empty());
    return GeoField.of(fieldName);
  }

  private NumericField indexAsNumericFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      NumericIndexed ni) {
    var fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(ni.alias()), Optional.empty());
    return NumericField.of(fieldName);
  }

  private NumericField indexAsNumericFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      boolean sortable, boolean noIndex, String annotationAlias) {
    return indexAsNumericFieldFor(field, isDocument, prefix, sortable, noIndex, annotationAlias, false, false);
  }

  private NumericField indexAsNumericFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      boolean sortable, boolean noIndex, String annotationAlias, boolean indexMissing, boolean indexEmpty) {
    var fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(annotationAlias), Optional.empty());

    NumericField num = NumericField.of(fieldName);
    if (sortable)
      num.sortable();
    if (noIndex)
      num.noIndex();
    if (indexMissing)
      num.indexMissing();
    // Note: NumericField doesn't support indexEmpty() in current Jedis version
    // if (indexEmpty)
    //   num.indexEmpty();
    return num;
  }

  private GeoField indexAsGeoFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      String annotationAlias) {
    var fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(annotationAlias), Optional.empty());
    return GeoField.of(fieldName);
  }

  private List<SearchField> indexAsNestedFieldFor(java.lang.reflect.Field field, String prefix) {
    String fieldPrefix = getFieldPrefix(prefix, true);
    return getNestedField(fieldPrefix, field, prefix, null);
  }

  private List<SearchField> getNestedField(String fieldPrefix, java.lang.reflect.Field field, String prefix,
      List<SearchField> fieldList) {
    if (fieldList == null) {
      fieldList = new ArrayList<>();
    }
    Type genericType = field.getGenericType();
    if (genericType instanceof ParameterizedType pt) {
      Class<?> actualTypeArgument = (Class<?>) pt.getActualTypeArguments()[0];
      List<java.lang.reflect.Field> subDeclaredFields = com.redis.om.spring.util.ObjectUtils
          .getDeclaredFieldsTransitively(actualTypeArgument);
      String tempPrefix = "";
      if (prefix == null) {
        prefix = field.getName();
      } else {
        prefix += "." + field.getName();
      }
      for (java.lang.reflect.Field subField : subDeclaredFields) {

        Optional<Class<?>> maybeCollectionType = getCollectionElementClass(subField);

        String suffix = (maybeCollectionType.isPresent() && (CharSequence.class.isAssignableFrom(maybeCollectionType
            .get()) || (maybeCollectionType.get() == Boolean.class))) ? "[*]" : "";

        if (subField.isAnnotationPresent(TagIndexed.class)) {
          TagIndexed ti = subField.getAnnotation(TagIndexed.class);
          tempPrefix = field.getName() + "[0:].";

          FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName() + suffix);
          fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(subField, prefix));

          logger.info(String.format("Creating nested relationships: %s -> %s", field.getName(), subField.getName()));
          fieldList.add(SearchField.of(field, getTagField(fieldName, ti.separator(), false)));
          continue;
        } else if (subField.isAnnotationPresent(Indexed.class)) {
          boolean subFieldIsTagField = (subField.isAnnotationPresent(Indexed.class) && ( //
          CharSequence.class.isAssignableFrom(subField.getType()) || //
              (subField.getType() == Boolean.class) || (subField.getType() == UUID.class) || //
              ( //
              maybeCollectionType.isPresent() && //
                  ( //
                  CharSequence.class.isAssignableFrom(maybeCollectionType.get()) || //
                      (maybeCollectionType.get() == Boolean.class) //
                  ) //
              ) //
          ) //
          );
          if (subFieldIsTagField) {
            Indexed indexed = subField.getAnnotation(Indexed.class);
            tempPrefix = field.getName() + "[0:].";

            FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName() + suffix);
            String alias = QueryUtils.searchIndexFieldAliasFor(subField, prefix);
            fieldName = fieldName.as(alias);

            logger.info(String.format("Creating nested relationships: %s -> %s", field.getName(), subField.getName()));
            fieldList.add(SearchField.of(field, getTagField(fieldName, indexed.separator(), false)));
            continue;
          } else if (Number.class.isAssignableFrom(subField.getType()) || (subField
              .getType() == LocalDateTime.class) || (subField.getType() == LocalDate.class) || (subField
                  .getType() == Date.class)) {

            FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName() + suffix);
            String alias = QueryUtils.searchIndexFieldAliasFor(subField, prefix);
            fieldName = fieldName.as(alias);

            logger.info(String.format("Creating nested relationships: %s -> %s", field.getName(), subField.getName()));
            fieldList.add(SearchField.of(field, NumericField.of(fieldName)));
          }
        } else if (subField.isAnnotationPresent(Searchable.class)) {
          Searchable searchable = subField.getAnnotation(Searchable.class);
          tempPrefix = field.getName() + "[0:].";

          FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName() + suffix);
          String alias = QueryUtils.searchIndexFieldAliasFor(subField, prefix);
          fieldName = fieldName.as(alias);

          logger.info(String.format("Creating TEXT nested relationships: %s -> %s", field.getName(), subField
              .getName()));

          String phonetic = ObjectUtils.isEmpty(searchable.phonetic()) ? null : searchable.phonetic();

          fieldList.add(SearchField.of(field, getTextField(fieldName, searchable.weight(), searchable.sortable(),
              searchable.nostem(), searchable.noindex(), phonetic, searchable.indexMissing(), searchable
                  .indexEmpty())));

          continue;
        }
        if (subField.isAnnotationPresent(Indexed.class)) {
          getNestedField(fieldPrefix + tempPrefix, subField, prefix, fieldList);
        }
      }
    }
    return fieldList;
  }

  /**
   * Determines the appropriate FieldType for a given Java class.
   * This utility method centralizes the logic for mapping Java types to Redis field types.
   */
  private enum FieldTypeMapper {
    TAG,
    NUMERIC,
    GEO,
    UNSUPPORTED;

    static FieldTypeMapper getFieldType(Class<?> fieldType) {
      if (CharSequence.class.isAssignableFrom(
          fieldType) || fieldType == Boolean.class || fieldType == UUID.class || fieldType == Ulid.class || fieldType
              .isEnum()) {
        return TAG;
      } else if (Number.class.isAssignableFrom(
          fieldType) || fieldType == LocalDateTime.class || fieldType == LocalDate.class || fieldType == Date.class || fieldType == Instant.class || fieldType == OffsetDateTime.class) {
        return NUMERIC;
      } else if (fieldType == Point.class) {
        return GEO;
      } else {
        return UNSUPPORTED;
      }
    }
  }

  /**
   * Creates index fields for nested array elements automatically.
   * This method handles automatic indexing of all fields within nested objects
   * when @Indexed(schemaFieldType = SchemaFieldType.NESTED) is used.
   */
  private List<SearchField> createNestedIndexFields(java.lang.reflect.Field arrayField,
      java.lang.reflect.Field nestedField, String prefix, boolean isDocument) {
    List<SearchField> fields = new ArrayList<>();

    Class<?> nestedFieldType = ClassUtils.resolvePrimitiveIfNecessary(nestedField.getType());

    // For nested arrays, the path should be: $.arrayField[*].nestedField
    // The prefix already contains the array field name, so we just need [*].nestedField
    // JSON documents use a "$" prefix to denote the root of the document, while hash structures do not.
    // The `isDocument` flag determines whether the entity is stored as a JSON document or a hash structure in Redis.
    // This affects the field path format: JSON documents require "$." as the prefix, while hash structures do not.
    String fullFieldPath = isDocument ?
        "$." + arrayField.getName() + "[*]." + nestedField.getName() :
        arrayField.getName() + "[*]." + nestedField.getName();

    logger.info(String.format("Creating automatic nested field index: %s -> %s", arrayField.getName(), fullFieldPath));

    // Determine field type and create appropriate index field
    FieldTypeMapper fieldTypeMapper = FieldTypeMapper.getFieldType(nestedFieldType);

    switch (fieldTypeMapper) {
      case TAG -> {
        // Create TAG field for strings, booleans, UUIDs, ULIDs, and enums
        FieldName fieldName = FieldName.of(fullFieldPath);
        String alias = QueryUtils.searchIndexFieldAliasFor(nestedField, prefix);
        if (alias != null && !alias.isEmpty()) {
          fieldName = fieldName.as(alias);
        }
        fields.add(SearchField.of(arrayField, getTagField(fieldName, "|", false)));
      }
      case NUMERIC -> {
        // Create NUMERIC field for numbers and dates
        FieldName fieldName = FieldName.of(fullFieldPath);
        String alias = QueryUtils.searchIndexFieldAliasFor(nestedField, prefix);
        if (alias != null && !alias.isEmpty()) {
          fieldName = fieldName.as(alias);
        }
        fields.add(SearchField.of(arrayField, NumericField.of(fieldName)));
      }
      case GEO -> {
        // Create GEO field for Point objects
        FieldName fieldName = FieldName.of(fullFieldPath);
        String alias = QueryUtils.searchIndexFieldAliasFor(nestedField, prefix);
        if (alias != null && !alias.isEmpty()) {
          fieldName = fieldName.as(alias);
        }
        fields.add(SearchField.of(arrayField, GeoField.of(fieldName)));
      }
      case UNSUPPORTED -> logger.debug(String.format("Skipping nested field %s of unsupported type %s", nestedField
          .getName(), nestedFieldType.getSimpleName()));
    }

    return fields;
  }

  private TagField getTagField(FieldName fieldName, String separator, boolean sortable) {
    return getTagField(fieldName, separator, sortable, false, false);
  }

  private TagField getTagField(FieldName fieldName, String separator, boolean sortable, boolean indexMissing,
      boolean indexEmpty) {
    TagField tag = TagField.of(fieldName);
    if (separator != null) {
      if (separator.length() != 1) {
        throw new IllegalArgumentException("Separator '" + separator + "' is not of length 1.");
      }
      tag.separator(separator.charAt(0));
    }
    if (sortable)
      tag.sortable();
    if (indexMissing)
      tag.indexMissing();
    if (indexEmpty)
      tag.indexEmpty();
    return tag;
  }

  private TextField getTextField(FieldName fieldName, double weight, boolean sortable, boolean noStem, boolean noIndex,
      String phonetic, boolean indexMissing, boolean indexEmpty) {
    TextField text = TextField.of(fieldName);
    text.weight(weight);
    if (sortable)
      text.sortable();
    if (noStem)
      text.noStem();
    if (noIndex)
      text.noIndex();
    if (phonetic != null)
      text.phonetic(phonetic);
    if (indexMissing)
      text.indexMissing();
    if (indexEmpty)
      text.indexEmpty();
    return text;
  }

  private String getEntityPrefix(Class<?> cl) {
    String entityPrefix = cl.getName() + ":";
    if (mappingContext.hasPersistentEntityFor(cl)) {
      RedisPersistentEntity<?> persistentEntity = mappingContext.getRequiredPersistentEntity(cl);
      entityPrefix = persistentEntity.getKeySpace() != null ? persistentEntity.getKeySpace() + ":" : entityPrefix;
      logger.info(String.format("Using entity prefix '%s' as keyspace for type : %s", entityPrefix, cl));
    }
    return entityPrefix;
  }

  private void dropIndex(Class<?> cl, boolean dropDocuments, boolean recreateIndex) {
    String indexName = entityClassToIndexName.get(cl);
    try {
      SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
      if (dropDocuments) {
        opsForSearch.dropIndexAndDocuments();
      } else {
        opsForSearch.dropIndex();
      }
      String entityPrefix = generateEntityPrefix(cl);
      removeKeySpaceMapping(entityPrefix, cl);
      if (recreateIndex) {
        createIndexFor(cl);
      }
    } catch (Exception e) {
      logger.warn(String.format(SKIPPING_INDEX_CREATION, indexName, e.getMessage()));
    }
  }

  private String generateEntityPrefix(Class<?> cl) {
    String entityPrefix = getEntityPrefix(cl);
    if (cl.isAnnotationPresent(Document.class)) {
      Document document = cl.getAnnotation(Document.class);
      if (ObjectUtils.isNotEmpty(document.value())) {
        entityPrefix = document.value();
      }
    } else if (cl.isAnnotationPresent(RedisHash.class)) {
      RedisHash hash = cl.getAnnotation(RedisHash.class);
      if (ObjectUtils.isNotEmpty(hash.value())) {
        entityPrefix = hash.value();
      }
    }
    return entityPrefix;
  }

  private Optional<IndexDataType> determineIndexTarget(Class<?> cl) {
    if (cl.isAnnotationPresent(Document.class)) {
      return Optional.of(IndexDataType.JSON);
    } else if (cl.isAnnotationPresent(RedisHash.class)) {
      return Optional.of(IndexDataType.HASH);
    } else {
      return Optional.empty();
    }
  }

  private List<SearchField> processIndexedFields(List<java.lang.reflect.Field> allClassFields, boolean isDocument) {
    List<SearchField> fields = new ArrayList<>();
    for (java.lang.reflect.Field field : allClassFields) {
      fields.addAll(findIndexFields(field, null, isDocument));
    }
    return fields;
  }

  private Optional<String> getDocumentScoreField(List<java.lang.reflect.Field> allClassFields, boolean isDocument) {
    return allClassFields.stream().filter(field -> field.isAnnotationPresent(DocumentScore.class)).findFirst().map(
        field -> (isDocument ? "$." : "") + field.getName());
  }

  private boolean isAnnotationPreset(java.lang.reflect.Field idField, List<SchemaField> fields) {
    return (!idField.isAnnotationPresent(Indexed.class) && !idField.isAnnotationPresent(Searchable.class) && !idField
        .isAnnotationPresent(TagIndexed.class) && !idField.isAnnotationPresent(TextIndexed.class) && !idField
            .isAnnotationPresent(NumericIndexed.class) && (fields.stream().noneMatch(f -> f.getName().equals(idField
                .getName()))));
  }

  private List<SearchField> createIndexedFieldsForIdFields(Class<?> cl, List<SchemaField> fields, boolean isDocument) {
    List<SearchField> results = new ArrayList<>();
    List<java.lang.reflect.Field> idFields = getIdFieldsForEntityClass(cl);
    for (java.lang.reflect.Field idField : idFields) {
      // Only auto-index the @Id if not already indexed by the user (gh-135)
      if (isAnnotationPreset(idField, fields)) {
        Class<?> idClass = idField.getType();
        if (idField.getType().isPrimitive()) {
          String cls = com.redis.om.spring.util.ObjectUtils.getTargetClassName(idClass.getName());
          Class<?> primitive = ClassUtils.resolvePrimitiveClassName(cls);
          if (primitive != null) {
            idClass = ClassUtils.resolvePrimitiveIfNecessary(primitive);
          }
        }

        // TODO: determine if we need to pass the alias
        if (Number.class.isAssignableFrom(idClass)) {
          results.add(SearchField.of(idField, indexAsNumericFieldFor(idField, isDocument, "", true, false, null)));
        } else {
          results.add(SearchField.of(idField, indexAsTagFieldFor(idField, isDocument, "", false, "|", Integer.MIN_VALUE,
              null)));
        }
      }
    }

    java.lang.reflect.Field idField = (!idFields.isEmpty()) ? idFields.get(0) : null;

    // register any @IdFilter annotation
    // TODO If multiple IDs which one does the IdFilter applies to? - for now the first
    if (idField != null && idField.isAnnotationPresent(IdFilter.class)) {
      IdFilter idFilter = idField.getAnnotation(IdFilter.class);
      var identifierFilterClass = idFilter.value();
      try {
        var identifierFilter = (IdentifierFilter<?>) identifierFilterClass.getDeclaredConstructor().newInstance();
        entityClassToIdentifierFilter.put(cl, identifierFilter);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
               NoSuchMethodException idFilterInstantiationException) {
        logger.error(String.format("Could not instantiate IdFilter of type %s applied to class %s",
            identifierFilterClass.getSimpleName(), cl), idFilterInstantiationException);
      }
    }

    return results;
  }

  private Optional<SearchField> createIndexedFieldForReferenceIdField( //
      java.lang.reflect.Field referenceIdField, //
      boolean isDocument) {
    SerializedName serializedName = referenceIdField.getAnnotation(SerializedName.class);
    String fname = (serializedName != null) ? serializedName.value() : referenceIdField.getName();

    String fieldPrefix = getFieldPrefix("", isDocument);
    FieldName fieldName = FieldName.of(fieldPrefix + fname);
    String alias = QueryUtils.searchIndexFieldAliasFor(referenceIdField, "");
    fieldName = fieldName.as(alias);

    return Optional.of(SearchField.of(referenceIdField, isDocument ?
        TagField.of(fieldName).separator('|') :
        TagField.of(fieldName).separator('|').sortable()));
  }

  /**
   * Creates index fields for the indexed/searchable fields of a referenced entity.
   * This enables searching on referenced entity properties, e.g., RefVehicle$.OWNER_NAME.eq("John").
   *
   * @param referenceField the @Reference field
   * @param isDocument     whether this is a JSON document (vs Hash)
   * @param prefix         the field prefix
   * @return list of search fields for the referenced entity's indexed properties
   */
  private List<SearchField> createIndexedFieldsForReferencedEntity(java.lang.reflect.Field referenceField,
      boolean isDocument, String prefix) {

    List<SearchField> fields = new ArrayList<>();
    Class<?> referencedType = referenceField.getType();
    String referenceFieldName = referenceField.getName();

    logger.debug(
        "Processing indexed subfields for @Reference field " + referenceFieldName + " of type " + referencedType
            .getSimpleName());

    // Get all fields from the referenced entity that have indexing annotations
    List<java.lang.reflect.Field> referencedFields = new ArrayList<>();
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        Indexed.class));
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        Searchable.class));
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        TagIndexed.class));
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        TextIndexed.class));
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        NumericIndexed.class));
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        GeoIndexed.class));
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        VectorIndexed.class));
    // Remove duplicates (a field might have multiple annotations)
    referencedFields = referencedFields.stream().distinct().toList();

    for (java.lang.reflect.Field subField : referencedFields) {
      // Skip @Id fields - they're handled separately by createIndexedFieldForReferenceIdField
      if (subField.isAnnotationPresent(Id.class)) {
        continue;
      }
      // Skip @Reference fields to avoid infinite recursion
      if (subField.isAnnotationPresent(Reference.class)) {
        continue;
      }

      Class<?> subFieldType = ClassUtils.resolvePrimitiveIfNecessary(subField.getType());
      String subFieldName = subField.getName();

      // Build the nested field path: referenceField.subField
      String fieldPath = isDocument ?
          getFieldPrefix(prefix, true) + referenceFieldName + "." + subFieldName :
          referenceFieldName + "_" + subFieldName;

      // Build the alias: referenceField_subField
      String alias = referenceFieldName + "_" + subFieldName;

      FieldName fieldName = FieldName.of(fieldPath).as(alias);

      logger.debug(
          "Creating index field for " + referenceFieldName + "." + subFieldName + " with path " + fieldPath + " and alias " + alias);

      // Handle @Searchable fields (full-text search)
      Searchable searchable = subField.getAnnotation(Searchable.class);
      if (searchable != null) {
        TextField textField = TextField.of(fieldName);
        if (searchable.weight() != 1.0) {
          textField.weight(searchable.weight());
        }
        if (searchable.sortable()) {
          textField.sortable();
        }
        if (searchable.nostem()) {
          textField.noStem();
        }
        if (searchable.noindex()) {
          textField.noIndex();
        }
        String phonetic = searchable.phonetic();
        if (phonetic != null && !phonetic.isEmpty()) {
          textField.phonetic(phonetic);
        }
        if (searchable.indexMissing()) {
          textField.indexMissing();
        }
        if (searchable.indexEmpty()) {
          textField.indexEmpty();
        }
        fields.add(SearchField.of(subField, textField));
        continue;
      }

      // Handle @TextIndexed fields
      TextIndexed textIndexed = subField.getAnnotation(TextIndexed.class);
      if (textIndexed != null) {
        TextField textField = TextField.of(fieldName);
        if (textIndexed.weight() != 1.0) {
          textField.weight(textIndexed.weight());
        }
        if (textIndexed.sortable()) {
          textField.sortable();
        }
        if (textIndexed.nostem()) {
          textField.noStem();
        }
        if (textIndexed.noindex()) {
          textField.noIndex();
        }
        String phonetic = textIndexed.phonetic();
        if (phonetic != null && !phonetic.isEmpty()) {
          textField.phonetic(phonetic);
        }
        if (textIndexed.indexMissing()) {
          textField.indexMissing();
        }
        if (textIndexed.indexEmpty()) {
          textField.indexEmpty();
        }
        fields.add(SearchField.of(subField, textField));
        continue;
      }

      // Handle @Indexed or @TagIndexed fields
      Indexed indexed = subField.getAnnotation(Indexed.class);
      TagIndexed tagIndexed = subField.getAnnotation(TagIndexed.class);
      NumericIndexed numericIndexed = subField.getAnnotation(NumericIndexed.class);
      GeoIndexed geoIndexed = subField.getAnnotation(GeoIndexed.class);

      if (tagIndexed != null || (indexed != null && (CharSequence.class.isAssignableFrom(
          subFieldType) || subFieldType == java.util.UUID.class || subFieldType == com.github.f4b6a3.ulid.Ulid.class))) {
        // Tag field for strings, UUID, and Ulid
        String separatorStr = tagIndexed != null ?
            tagIndexed.separator() :
            (indexed != null ? indexed.separator() : "|");
        char separator = separatorStr != null && !separatorStr.isEmpty() ? separatorStr.charAt(0) : '|';
        TagField tagField = TagField.of(fieldName).separator(separator);
        if (indexed != null && indexed.sortable()) {
          tagField.sortable();
        }
        if (tagIndexed != null && tagIndexed.indexMissing()) {
          tagField.indexMissing();
        } else if (indexed != null && indexed.indexMissing()) {
          tagField.indexMissing();
        }
        if (tagIndexed != null && tagIndexed.indexEmpty()) {
          tagField.indexEmpty();
        } else if (indexed != null && indexed.indexEmpty()) {
          tagField.indexEmpty();
        }
        fields.add(SearchField.of(subField, tagField));
      } else if (numericIndexed != null || (indexed != null && (Number.class.isAssignableFrom(
          subFieldType) || subFieldType == java.time.LocalDateTime.class || subFieldType == java.time.LocalDate.class || subFieldType == java.util.Date.class || subFieldType == java.time.Instant.class || subFieldType == java.time.OffsetDateTime.class))) {
        // Numeric field
        NumericField numField = NumericField.of(fieldName);
        if ((numericIndexed != null && numericIndexed.sortable()) || (indexed != null && indexed.sortable())) {
          numField.sortable();
        }
        if ((numericIndexed != null && numericIndexed.noindex()) || (indexed != null && indexed.noindex())) {
          numField.noIndex();
        }
        if (indexed != null && indexed.indexMissing()) {
          numField.indexMissing();
        }
        // Note: NumericField doesn't support indexEmpty() in current Jedis version
        fields.add(SearchField.of(subField, numField));
      } else if (geoIndexed != null || (indexed != null && Point.class.isAssignableFrom(subFieldType))) {
        // Geo field
        GeoField geoField = GeoField.of(fieldName);
        fields.add(SearchField.of(subField, geoField));
      } else if (indexed != null && subFieldType.isEnum()) {
        // Enum as tag field
        String separatorStr = indexed.separator();
        char separator = separatorStr != null && !separatorStr.isEmpty() ? separatorStr.charAt(0) : '|';
        TagField tagField = TagField.of(fieldName).separator(separator);
        if (indexed.sortable()) {
          tagField.sortable();
        }
        if (indexed.indexMissing()) {
          tagField.indexMissing();
        }
        if (indexed.indexEmpty()) {
          tagField.indexEmpty();
        }
        fields.add(SearchField.of(subField, tagField));
      } else if (indexed != null && (subFieldType == Boolean.class || subFieldType == boolean.class)) {
        // Boolean as tag field
        TagField tagField = TagField.of(fieldName);
        if (indexed.sortable()) {
          tagField.sortable();
        }
        if (indexed.indexMissing()) {
          tagField.indexMissing();
        }
        if (indexed.indexEmpty()) {
          tagField.indexEmpty();
        }
        fields.add(SearchField.of(subField, tagField));
      }

      // Handle @VectorIndexed fields
      VectorIndexed vectorIndexed = subField.getAnnotation(VectorIndexed.class);
      if (vectorIndexed != null) {
        VectorField.VectorAlgorithm algorithm = vectorIndexed.algorithm();
        VectorType vectorType = vectorIndexed.type();
        int dimension = vectorIndexed.dimension();
        DistanceMetric distanceMetric = vectorIndexed.distanceMetric();
        int initialCap = vectorIndexed.initialCapacity();

        Map<String, Object> vectorAttrs = new HashMap<>();
        vectorAttrs.put("TYPE", vectorType.toString());
        vectorAttrs.put("DIM", dimension);
        vectorAttrs.put("DISTANCE_METRIC", distanceMetric.toString());
        if (initialCap > 0) {
          vectorAttrs.put("INITIAL_CAP", initialCap);
        }

        if (algorithm == VectorField.VectorAlgorithm.HNSW) {
          int m = vectorIndexed.m();
          int efConstruction = vectorIndexed.efConstruction();
          int efRuntime = vectorIndexed.efRuntime();
          double epsilon = vectorIndexed.epsilon();
          if (m > 0)
            vectorAttrs.put("M", m);
          if (efConstruction > 0)
            vectorAttrs.put("EF_CONSTRUCTION", efConstruction);
          if (efRuntime > 0)
            vectorAttrs.put("EF_RUNTIME", efRuntime);
          if (epsilon > 0)
            vectorAttrs.put("EPSILON", epsilon);
        } else if (algorithm == VectorField.VectorAlgorithm.FLAT) {
          int blockSize = vectorIndexed.blockSize();
          if (blockSize > 0)
            vectorAttrs.put("BLOCK_SIZE", blockSize);
        }

        VectorField vectorField = VectorField.builder().fieldName(fieldName).algorithm(algorithm).attributes(
            vectorAttrs).build();

        fields.add(SearchField.of(subField, vectorField));
      }
    }

    return fields;
  }

  private FTCreateParams createIndexDefinition(Class<?> cl, IndexDataType idxType) {
    FTCreateParams params = FTCreateParams.createParams();
    params.on(idxType);

    if (cl.isAnnotationPresent(Document.class)) {
      Document document = cl.getAnnotation(Document.class);
      Optional.ofNullable(document.filter()).filter(ObjectUtils::isNotEmpty).ifPresent(params::filter);
      Optional.ofNullable(document.language()).filter(ObjectUtils::isNotEmpty).ifPresent(lang -> params.language(lang
          .getValue()));
      Optional.ofNullable(document.languageField()).filter(ObjectUtils::isNotEmpty).ifPresent(params::languageField);
      params.score(document.score());
    }

    return params;
  }

  private void updateTTLSettings(Class<?> cl, String entityPrefix, boolean isDocument, Optional<Document> document,
      List<java.lang.reflect.Field> allClassFields) {
    if (isDocument) {
      KeyspaceSettings setting = new KeyspaceSettings(cl, entityPrefix);

      // Default TTL
      document.filter(doc -> doc.timeToLive() > 0).ifPresent(doc -> setting.setTimeToLive(doc.timeToLive()));

      allClassFields.stream().filter(field -> field.isAnnotationPresent(TimeToLive.class)).findFirst().ifPresent(
          field -> setting.setTimeToLivePropertyName(field.getName()));

      // Use the resolver if the mapping context is enhanced
      if (mappingContext instanceof com.redis.om.spring.mapping.RedisEnhancedMappingContext) {
        ((com.redis.om.spring.mapping.RedisEnhancedMappingContext) mappingContext).getKeyspaceResolver()
            .addKeyspaceSettings(cl, setting);
      } else {
        mappingContext.getMappingConfiguration().getKeyspaceConfiguration().addKeyspaceSettings(setting);
      }
    }
  }

  private String getKeyspace(String keyspace) {
    return keyspace.endsWith(":") ? keyspace : keyspace + ":";
  }

  private String getFieldPrefix(String prefix, boolean isDocument) {
    String chain = (prefix == null || prefix.isBlank()) ? "" : prefix + ".";
    return isDocument ? "$." + chain : chain;
  }

  private String getJsonFieldName(java.lang.reflect.Field field) {
    // Check for @JsonProperty annotation first
    if (field.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonProperty.class)) {
      com.fasterxml.jackson.annotation.JsonProperty jsonProperty = field.getAnnotation(
          com.fasterxml.jackson.annotation.JsonProperty.class);
      if (jsonProperty.value() != null && !jsonProperty.value().isEmpty()) {
        return jsonProperty.value();
      }
    }

    // Check for @SerializedName annotation (Gson)
    if (field.isAnnotationPresent(com.google.gson.annotations.SerializedName.class)) {
      com.google.gson.annotations.SerializedName serializedName = field.getAnnotation(
          com.google.gson.annotations.SerializedName.class);
      if (serializedName.value() != null && !serializedName.value().isEmpty()) {
        return serializedName.value();
      }
    }

    // Default to field name
    return field.getName();
  }

  private void registerAlias(Class<?> cl, String fieldName, String alias) {
    entityClassFieldToAlias.put(Tuples.of(cl, fieldName), alias);
  }

  private FieldName buildFieldName( //
      java.lang.reflect.Field field, String prefix, boolean isDocument, Optional<String> maybeAlias,
      Optional<Integer> maybeArrayIndex) {
    SerializedName serializedName = field.getAnnotation(SerializedName.class);
    Indexed indexed = field.getAnnotation(Indexed.class);
    String fname = (serializedName != null) ? serializedName.value() : field.getName();

    TypeInformation<?> typeInfo = TypeInformation.of(field.getType());
    String fieldPrefix = getFieldPrefix(prefix, isDocument);

    String index = maybeArrayIndex.isPresent() && (maybeArrayIndex.get() != Integer.MIN_VALUE) ?
        ".[" + maybeArrayIndex.get() + "]" :
        "[*]";

    boolean needsPostfix = (isDocument && typeInfo.isCollectionLike() && !field.isAnnotationPresent(
        JsonAdapter.class) && (indexed != null && !indexed.schemaFieldType().equals(SchemaFieldType.VECTOR)));
    String fieldPostfix = needsPostfix ? index : "";

    String name = fieldPrefix + fname + fieldPostfix;

    String alias = maybeAlias.isEmpty() || maybeAlias.get().isBlank() ?
        QueryUtils.searchIndexFieldAliasFor(field, prefix) :
        maybeAlias.get();

    return FieldName.of(name).as(alias);
  }

  /**
   * Retrieves the search index field alias for the specified entity class and field name.
   * Aliases are used to map entity field names to their corresponding search index field names.
   *
   * @param cl        the entity class containing the field
   * @param fieldName the name of the field to get the alias for
   * @return the search index alias for the field, or the original field name if no alias exists
   */
  public String getAlias(Class<?> cl, String fieldName) {
    var alias = entityClassFieldToAlias.get(Tuples.of(cl, fieldName));
    return alias != null ? alias : fieldName;
  }

  /**
   * Retrieves the Redis OM configuration properties.
   *
   * @return the RedisOMProperties instance containing configuration settings
   */
  public RedisOMProperties getProperties() {
    return properties;
  }

  /**
   * Retrieves the set of lexicographic fields for the given entity class.
   *
   * @param entityClass the entity class to get lexicographic fields for
   * @return a set of field names that have lexicographic indexing enabled, or an empty set if none exist
   */
  public Set<String> getLexicographicFields(Class<?> entityClass) {
    return entityClassToLexicographicFields.getOrDefault(entityClass, Collections.emptySet());
  }

  /**
   * Creates sorted sets for fields marked with lexicographic=true.
   * These sorted sets enable efficient lexicographic range queries.
   *
   * @param entityClass  the entity class to process
   * @param entityPrefix the Redis key prefix for the entity
   */
  private void createSortedSetsForLexicographicFields(Class<?> entityClass, String entityPrefix) {
    Set<String> lexicographicFields = entityClassToLexicographicFields.get(entityClass);
    if (lexicographicFields != null && !lexicographicFields.isEmpty()) {
      for (String fieldName : lexicographicFields) {
        String sortedSetKey = entityPrefix + fieldName + ":lex";
        // Just log that we're tracking this field - the sorted set will be created on first insert
        logger.info(String.format("Tracking lexicographic field %s with sorted set key %s", fieldName, sortedSetKey));
      }
    }
  }

  // Context-aware indexing methods

  /**
   * Creates an index for the specified entity class using the provided context and resolver.
   *
   * @param entityClass the entity class to create an index for
   * @param context     the Redis index context containing tenant and environment information
   * @param resolver    the index resolver for determining index names and key prefixes
   * @return true if the index was created or already exists, false otherwise
   */
  public boolean createIndexFor(Class<?> entityClass, RedisIndexContext context, IndexResolver resolver) {
    String indexName = resolver.resolveIndexName(entityClass, context);
    String keyPrefix = resolver.resolveKeyPrefix(entityClass, context);

    if (indexExistsFor(entityClass, indexName)) {
      return true;
    }

    return createIndexFor(entityClass, indexName, keyPrefix);
  }

  /**
   * Creates an index for the specified entity class using the current thread-local context.
   *
   * @param entityClass the entity class to create an index for
   * @param resolver    the index resolver for determining index names and key prefixes
   * @return true if the index was created or already exists, false otherwise
   */
  public boolean createIndexForContext(Class<?> entityClass, IndexResolver resolver) {
    RedisIndexContext context = RedisIndexContext.getContext();
    return createIndexFor(entityClass, context, resolver);
  }

  /**
   * Drops an index for the specified entity class using the provided context and resolver.
   *
   * @param entityClass the entity class to drop the index for
   * @param context     the Redis index context containing tenant and environment information
   * @param resolver    the index resolver for determining index names
   * @return true if the index was dropped successfully, false otherwise
   */
  public boolean dropIndexFor(Class<?> entityClass, RedisIndexContext context, IndexResolver resolver) {
    String indexName = resolver.resolveIndexName(entityClass, context);
    return dropIndexFor(entityClass, indexName);
  }

  /**
   * Checks if an index exists for the specified entity class using the provided context and resolver.
   *
   * @param entityClass the entity class to check
   * @param context     the Redis index context containing tenant and environment information
   * @param resolver    the index resolver for determining index names
   * @return true if the index exists, false otherwise
   */
  public boolean indexExistsFor(Class<?> entityClass, RedisIndexContext context, IndexResolver resolver) {
    String indexName = resolver.resolveIndexName(entityClass, context);
    return indexExistsFor(entityClass, indexName);
  }

  /**
   * Gets the index name for the specified entity class using the provided context and resolver.
   *
   * @param entityClass the entity class
   * @param context     the Redis index context containing tenant and environment information
   * @param resolver    the index resolver for determining index names
   * @return the resolved index name
   */
  public String getIndexName(Class<?> entityClass, RedisIndexContext context, IndexResolver resolver) {
    return resolver.resolveIndexName(entityClass, context);
  }

  /**
   * Gets the keyspace prefix for the specified entity class using the provided context and resolver.
   *
   * @param entityClass the entity class
   * @param context     the Redis index context containing tenant and environment information
   * @param resolver    the index resolver for determining key prefixes
   * @return the resolved keyspace prefix
   */
  public String getKeyspacePrefix(Class<?> entityClass, RedisIndexContext context, IndexResolver resolver) {
    return resolver.resolveKeyPrefix(entityClass, context);
  }

  /**
   * Creates an index with the specified name and key prefix.
   * This is a helper method for context-aware index creation.
   *
   * @param entityClass the entity class
   * @param indexName   the name of the index to create
   * @param keyPrefix   the key prefix for the index
   * @return true if successful, false otherwise
   */
  public boolean createIndexFor(Class<?> entityClass, String indexName, String keyPrefix) {
    try {
      Optional<IndexDataType> maybeType = determineIndexTarget(entityClass);
      IndexDataType idxType;
      if (maybeType.isPresent()) {
        idxType = maybeType.get();
      } else {
        return false;
      }

      boolean isDocument = idxType == IndexDataType.JSON;
      Optional<IndexingOptions> maybeIndexingOptions = Optional.ofNullable(entityClass.getAnnotation(
          IndexingOptions.class));

      logger.info(String.format("Found @%s annotated class: %s", idxType, entityClass.getName()));

      final List<java.lang.reflect.Field> allClassFields = getDeclaredFieldsTransitively(entityClass);

      List<SearchField> searchFields = processIndexedFields(allClassFields, isDocument);

      for (SearchField field : searchFields) {
        registerAlias(entityClass, field.getField().getName(), field.getSchemaField().getFieldName().getAttribute());
      }

      Optional<String> maybeScoreField = getDocumentScoreField(allClassFields, isDocument);
      createIndexedFieldsForIdFields(entityClass, searchFields.stream().map(SearchField::getSchemaField).toList(),
          isDocument).forEach(searchFields::add);

      SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);

      FTCreateParams params = createIndexDefinition(entityClass, idxType);

      if (isDocument) {
        maybeScoreField.ifPresent(params::scoreField);
      }

      // Ensure keyPrefix ends with colon
      String entityPrefix = keyPrefix.endsWith(":") ? keyPrefix : keyPrefix + ":";
      params.prefix(entityPrefix);
      addKeySpaceMapping(entityPrefix, entityClass);

      // Update TTL settings
      Optional<Document> document = isDocument ?
          Optional.of(entityClass.getAnnotation(Document.class)) :
          Optional.empty();
      updateTTLSettings(entityClass, entityPrefix, isDocument, document, allClassFields);

      List<SchemaField> fields = searchFields.stream().map(SearchField::getSchemaField).toList();

      // Note: We don't update entityClassToSchema or entityClassToIndexName here
      // because this method is for creating indexes with custom names/prefixes.
      // The global mappings should only be updated by the main createIndexFor(Class) method.

      if (maybeIndexingOptions.isPresent()) {
        IndexingOptions options = maybeIndexingOptions.get();
        switch (options.creationMode()) {
          case SKIP_IF_EXIST:
            opsForSearch.createIndex(params, fields);
            logger.info(String.format("Created index %s...", indexName));
            // Create sorted sets for lexicographic fields
            createSortedSetsForLexicographicFields(entityClass, entityPrefix);
            break;
          case DROP_AND_RECREATE:
            if (indexExistsFor(entityClass, indexName)) {
              opsForSearch.dropIndex();
              logger.info(String.format("Dropped index %s", indexName));
            }
            opsForSearch.createIndex(params, fields);
            logger.info(String.format("Created index %s", indexName));
            // Create sorted sets for lexicographic fields
            createSortedSetsForLexicographicFields(entityClass, entityPrefix);
            break;
          case SKIP_ALWAYS:
            // do nothing and like it!
            logger.info(String.format("Skipped index creation for %s", entityClass.getSimpleName()));
            break;
        }
      } else {
        opsForSearch.createIndex(params, fields);
        logger.info(String.format("Created index %s", indexName));
        // Create sorted sets for lexicographic fields
        createSortedSetsForLexicographicFields(entityClass, entityPrefix);
      }

      return true;
    } catch (Exception e) {
      logger.warn(String.format(SKIPPING_INDEX_CREATION, indexName, e.getMessage()));
      return false;
    }
  }

  /**
   * Drops an index with the specified name.
   * This is a helper method for context-aware index dropping.
   *
   * @param entityClass the entity class
   * @param indexName   the name of the index to drop
   * @return true if successful, false otherwise
   */
  public boolean dropIndexFor(Class<?> entityClass, String indexName) {
    try {
      if (indexExistsFor(entityClass, indexName)) {
        SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
        opsForSearch.dropIndex();
        return true;
      }
      return true; // Already doesn't exist
    } catch (Exception e) {
      logger.error("Failed to drop index " + indexName + " for " + entityClass.getSimpleName(), e);
      return false;
    }
  }

  /**
   * Checks if an index with the specified name exists.
   * This is a helper method for context-aware index existence checking.
   *
   * @param entityClass the entity class
   * @param indexName   the name of the index to check
   * @return true if the index exists, false otherwise
   */
  public boolean indexExistsFor(Class<?> entityClass, String indexName) {
    try {
      SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
      opsForSearch.getInfo();
      return true; // If no exception, index exists
    } catch (Exception e) {
      logger.debug("Error checking if index exists: " + indexName, e);
      return false;
    }
  }

  /**
   * Creates an alias for a Redis search index.
   *
   * @param indexName the name of the index to create an alias for
   * @param aliasName the name of the alias to create
   * @return true if the alias was created successfully, false otherwise
   */
  public boolean createAlias(String indexName, String aliasName) {
    try {
      SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
      opsForSearch.addAlias(aliasName);
      logger.info(String.format("Created alias %s for index %s", aliasName, indexName));
      return true;
    } catch (Exception e) {
      logger.error(String.format("Failed to create alias %s for index %s: %s", aliasName, indexName, e.getMessage()));
      return false;
    }
  }

  /**
   * Removes an alias from a Redis search index.
   *
   * @param indexName the name of the index to remove the alias from (not used by Redis, kept for API consistency)
   * @param aliasName the name of the alias to remove
   * @return true if the alias was removed successfully, false otherwise
   */
  public boolean removeAlias(String indexName, String aliasName) {
    try {
      // Note: deleteAlias doesn't need the index name, it just needs the alias
      SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
      opsForSearch.deleteAlias(aliasName);
      logger.info(String.format("Removed alias %s", aliasName));
      return true;
    } catch (Exception e) {
      // Alias might not exist, which is fine
      logger.debug(String.format("Failed to remove alias %s: %s", aliasName, e.getMessage()));
      return false;
    }
  }

  /**
   * Updates an alias to point to a new index.
   *
   * @param oldIndexName the current index the alias points to (not used by Redis, kept for API consistency)
   * @param newIndexName the new index the alias should point to
   * @param aliasName    the name of the alias to update
   * @return true if the alias was updated successfully, false otherwise
   */
  public boolean updateAlias(String oldIndexName, String newIndexName, String aliasName) {
    try {
      SearchOperations<String> opsForSearch = rmo.opsForSearch(newIndexName);
      opsForSearch.updateAlias(aliasName);
      logger.info(String.format("Updated alias %s to point to index %s", aliasName, newIndexName));
      return true;
    } catch (Exception e) {
      logger.error(String.format("Failed to update alias %s to %s: %s", aliasName, newIndexName, e.getMessage()));
      return false;
    }
  }

  /**
   * Creates indexes for all registered entity classes.
   * This is a bulk operation that iterates through all entity classes
   * that have been registered with the indexer and creates their indexes.
   * If an index already exists, it will be skipped based on the entity's
   * {@link IndexCreationMode} configuration.
   */
  public void createIndexes() {
    // Create a copy to avoid ConcurrentModificationException since createIndexFor may modify the list
    List<Class<?>> entitiesToProcess = new ArrayList<>(indexedEntityClasses);
    logger.info(String.format("Creating indexes for %d registered entity classes", entitiesToProcess.size()));
    for (Class<?> entityClass : entitiesToProcess) {
      try {
        createIndexFor(entityClass);
      } catch (Exception e) {
        logger.warn(String.format("Failed to create index for %s: %s", entityClass.getName(), e.getMessage()));
      }
    }
    logger.info("Finished creating indexes");
  }

  /**
   * Drops all indexes managed by this indexer.
   * This is a bulk operation that iterates through all registered entity classes
   * and drops their associated indexes. The underlying documents are preserved,
   * only the index definitions are removed.
   */
  public void dropIndexes() {
    // Create a copy to avoid ConcurrentModificationException since dropIndexFor modifies the list
    List<Class<?>> entitiesToProcess = new ArrayList<>(indexedEntityClasses);
    logger.info(String.format("Dropping indexes for %d registered entity classes", entitiesToProcess.size()));
    for (Class<?> entityClass : entitiesToProcess) {
      try {
        dropIndexFor(entityClass);
      } catch (Exception e) {
        logger.warn(String.format("Failed to drop index for %s: %s", entityClass.getName(), e.getMessage()));
      }
    }
    logger.info("Finished dropping indexes");
  }

  /**
   * Returns a set of all index names currently managed by this indexer.
   * The returned set is a snapshot copy and modifications to it will not
   * affect the internal state of the indexer.
   *
   * @return a set of index names for all registered entity classes
   */
  public Set<String> listIndexes() {
    return new HashSet<>(entityClassToIndexName.values());
  }
}