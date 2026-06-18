package com.redis.om.spring.indexing;

import static com.redis.om.spring.util.ObjectUtils.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration.KeyspaceSettings;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.annotations.*;
import com.redis.om.spring.id.IdentifierFilter;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.tuple.Tuples;

import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.FTCreateParams;
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
public class RediSearchIndexer {
  private static final Log logger = LogFactory.getLog(RediSearchIndexer.class);
  private static final String SKIPPING_INDEX_CREATION = "Skipping index creation for %s because %s";
  private final Map<String, Class<?>> keyspaceToEntityClass = new ConcurrentHashMap<>();
  private final Map<Class<?>, String> entityClassToKeySpace = new ConcurrentHashMap<>();
  private final Map<Class<?>, String> entityClassToIndexName = new ConcurrentHashMap<>();
  private final Map<Class<?>, IdentifierFilter<?>> entityClassToIdentifierFilter = new ConcurrentHashMap<>();
  private final List<Class<?>> indexedEntityClasses = new ArrayList<>();
  private final Set<String> allCreatedIndexNames = ConcurrentHashMap.newKeySet();
  private final Map<Class<?>, List<SearchField>> entityClassToSchema = new ConcurrentHashMap<>();
  private final Map<Pair<Class<?>, String>, String> entityClassFieldToAlias = new ConcurrentHashMap<>();
  private final Map<Class<?>, Set<String>> entityClassToLexicographicFields = new ConcurrentHashMap<>();
  private final ApplicationContext ac;
  private final RedisModulesOperations<String> rmo;
  private final RedisMappingContext mappingContext;
  private final GsonBuilder gsonBuilder;
  private final RedisOMProperties properties;
  private final ExpressionParser spelParser = new SpelExpressionParser();
  private final SchemaFieldFactory schemaFieldFactory;
  private final IndexDefinitionBuilder indexDefinitionBuilder;

  /**
   * Constructs a new RediSearchIndexer with the required dependencies.
   *
   * @param ac             the Spring application context for SpEL expression evaluation
   * @param properties     the Redis OM configuration properties
   * @param gsonBuilder    the Gson builder for JSON serialization configuration
   * @param rmo            the Redis modules operations for search index management
   * @param mappingContext the Redis mapping context for entity metadata
   */
  public RediSearchIndexer(ApplicationContext ac, RedisOMProperties properties, GsonBuilder gsonBuilder,
      RedisModulesOperations<String> rmo, RedisMappingContext mappingContext) {
    this.ac = ac;
    this.properties = properties;
    this.gsonBuilder = gsonBuilder;
    this.rmo = rmo;
    this.mappingContext = mappingContext;
    this.schemaFieldFactory = new SchemaFieldFactory();
    this.indexDefinitionBuilder = new IndexDefinitionBuilder(schemaFieldFactory, gsonBuilder,
        pair -> entityClassToIdentifierFilter.put(pair.getFirst(), pair.getSecond()), (entityClass,
            fieldName) -> entityClassToLexicographicFields.computeIfAbsent(entityClass, k -> new HashSet<>()).add(
                fieldName));
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
   * Evaluates a string that may contain Spring Expression Language (SpEL) template
   * expressions of the form {@code #{...}}. If the expression is {@code null}, does
   * not contain a SpEL marker, or successfully evaluates, the resulting string is
   * returned. If any SpEL part fails to evaluate or the expression is malformed,
   * {@code defaultValue} is returned instead.
   *
   * @param expression   the raw string, possibly containing SpEL templates
   * @param defaultValue the value to return if evaluation fails or yields null parts
   * @return the evaluated string, or {@code defaultValue} on failure
   */
  public String evaluateExpression(String expression, String defaultValue) {
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
   * Reads a repository interface's {@link IndexingOptions} annotation and resolves the
   * declared {@code indexName}, evaluating any SpEL template expressions. Returns
   * {@code null} when the interface is {@code null}, has no annotation, or declares a
   * blank index name.
   *
   * @param repositoryInterface the repository interface class to inspect, may be {@code null}
   * @return the resolved index name, or {@code null} if none is configured
   */
  public String resolveRepositoryIndexName(Class<?> repositoryInterface) {
    if (repositoryInterface == null) {
      return null;
    }
    IndexingOptions repoOptions = repositoryInterface.getAnnotation(IndexingOptions.class);
    if (repoOptions == null || repoOptions.indexName().isBlank()) {
      return null;
    }
    String rawIndexName = repoOptions.indexName();
    String resolved = evaluateExpression(rawIndexName, rawIndexName);
    return (resolved == null || resolved.isBlank()) ? null : resolved;
  }

  // ---------------------------------------------------------------------------
  // Shared helpers for the createIndexFor(...) family of methods.
  // ---------------------------------------------------------------------------

  /**
   * Runs the common field processing pipeline for an entity class: delegates to
   * {@link IndexDefinitionBuilder#buildSearchFields} and registers per-field aliases.
   * The returned list contains every {@link SearchField} the caller needs to turn into
   * {@link SchemaField}s.
   *
   * @param entityClass    the entity class being indexed
   * @param isDocument     whether the entity targets a JSON document index
   * @param allClassFields transitively resolved declared fields for the entity class
   * @return the full list of search fields, already enriched with ID-field entries
   */
  private List<SearchField> prepareSearchFields(Class<?> entityClass, boolean isDocument,
      List<java.lang.reflect.Field> allClassFields) {
    List<SearchField> searchFields = indexDefinitionBuilder.buildSearchFields(entityClass, isDocument, allClassFields);
    for (SearchField field : searchFields) {
      registerAlias(entityClass, field.getField().getName(), field.getSchemaField().getFieldName().getAttribute());
    }
    return searchFields;
  }

  /**
   * Evaluates any SpEL templates in each raw prefix and normalizes the result with a
   * trailing colon via {@link #getKeyspace(String)}. Used when an
   * {@link IndexingOptions#prefixes()} array is present.
   */
  private String[] resolveAndNormalizePrefixArray(String[] rawPrefixes) {
    String[] resolved = new String[rawPrefixes.length];
    for (int i = 0; i < rawPrefixes.length; i++) {
      resolved[i] = getKeyspace(evaluateExpression(rawPrefixes[i], rawPrefixes[i]));
    }
    return resolved;
  }

  /**
   * Executes the {@link IndexCreationMode} switch shared by every {@code createIndexFor}
   * variant that honors {@link IndexingOptions#creationMode()}. The variance between
   * callers — whether to check a specific index name for existence, and what side-effects
   * to run after a successful create — is supplied via {@code indexExists} and
   * {@code postCreateHook} respectively.
   *
   * @param opsForSearch   the search operations bound to {@code indexName}
   * @param params         the fully-configured {@link FTCreateParams}
   * @param fields         the schema fields to declare on the index
   * @param creationMode   the mode declared by the owning {@link IndexingOptions}
   * @param indexName      the resolved index name (for logging)
   * @param entityClass    the entity class (for logging)
   * @param indexExists    supplier that returns {@code true} iff the index already exists
   * @param postCreateHook side-effect to run after a successful create (e.g. lex sorted sets)
   */
  private void applyCreationMode(SearchOperations<String> opsForSearch, FTCreateParams params, List<SchemaField> fields,
      IndexCreationMode creationMode, String indexName, Class<?> entityClass, BooleanSupplier indexExists,
      Runnable postCreateHook) {
    switch (creationMode) {
      case SKIP_IF_EXIST:
        if (!indexExists.getAsBoolean()) {
          opsForSearch.createIndex(params, fields);
          logger.info(String.format("Created index %s", indexName));
          postCreateHook.run();
        }
        break;
      case DROP_AND_RECREATE:
        if (indexExists.getAsBoolean()) {
          opsForSearch.dropIndex();
          logger.info(String.format("Dropped index %s", indexName));
        }
        opsForSearch.createIndex(params, fields);
        logger.info(String.format("Created index %s", indexName));
        postCreateHook.run();
        break;
      case SKIP_ALWAYS:
        logger.info(String.format("Skipped index creation for %s", entityClass.getSimpleName()));
        break;
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
    if (maybeType.isEmpty()) {
      return;
    }
    IndexDataType idxType = maybeType.get();
    boolean isDocument = idxType == IndexDataType.JSON;
    Optional<Document> document = isDocument ? Optional.of(cl.getAnnotation(Document.class)) : Optional.empty();
    Optional<RedisHash> hash = !isDocument ? Optional.of(cl.getAnnotation(RedisHash.class)) : Optional.empty();
    Optional<IndexingOptions> maybeIndexingOptions = Optional.ofNullable(cl.getAnnotation(IndexingOptions.class));

    String indexName = "";
    String defaultIndexName = cl.getName() + "Idx";
    try {
      if (isDocument) {
        // IndexingOptions overrides Document#
        if (maybeIndexingOptions.isPresent()) {
          indexName = evaluateExpression(maybeIndexingOptions.get().indexName(), defaultIndexName);
        } else {
          indexName = document.get().indexName();
        }
        indexName = indexName.isBlank() ? defaultIndexName : indexName;
      } else {
        if (maybeIndexingOptions.isPresent()) {
          indexName = evaluateExpression(maybeIndexingOptions.get().indexName(), defaultIndexName);
        } else {
          indexName = defaultIndexName;
        }
      }

      logger.info(String.format("Found @%s annotated class: %s", idxType, cl.getName()));

      final List<java.lang.reflect.Field> allClassFields = getDeclaredFieldsTransitively(cl);
      List<SearchField> searchFields = prepareSearchFields(cl, isDocument, allClassFields);
      Optional<String> maybeScoreField = indexDefinitionBuilder.getDocumentScoreField(allClassFields, isDocument);

      SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
      FTCreateParams params = createIndexDefinition(cl, idxType);

      Optional<String> maybeEntityPrefix;
      if (isDocument) {
        maybeEntityPrefix = document.map(Document::value).filter(ObjectUtils::isNotEmpty);
        maybeScoreField.ifPresent(params::scoreField);
      } else {
        maybeEntityPrefix = hash.map(RedisHash::value).filter(ObjectUtils::isNotEmpty);
      }

      // Resolve prefixes (entity-level @IndexingOptions may override the @Document/@RedisHash default)
      String entityPrefix;
      if (maybeIndexingOptions.isPresent() && maybeIndexingOptions.get().prefixes().length > 0) {
        String[] resolvedPrefixes = resolveAndNormalizePrefixArray(maybeIndexingOptions.get().prefixes());
        params.prefix(resolvedPrefixes);
        // The first prefix is canonical: it populates both the forward and reverse
        // mappings (so updateTTLSettings / getKeyspaceForEntityClass stay consistent).
        // Any additional prefixes only populate the forward map, never overwriting the
        // canonical reverse entry for this entity class.
        entityPrefix = resolvedPrefixes[0];
        addKeySpaceMapping(resolvedPrefixes[0], cl);
        for (int i = 1; i < resolvedPrefixes.length; i++) {
          registerSecondaryKeyspace(resolvedPrefixes[i], cl);
        }
      } else if (maybeIndexingOptions.isPresent() && !maybeIndexingOptions.get().keyPrefix().isBlank()) {
        String defaultPrefix = maybeEntityPrefix.orElse(getEntityPrefix(cl));
        entityPrefix = getKeyspace(evaluateExpression(maybeIndexingOptions.get().keyPrefix(), defaultPrefix));
        params.prefix(entityPrefix);
        addKeySpaceMapping(entityPrefix, cl);
      } else {
        entityPrefix = getKeyspace(maybeEntityPrefix.orElse(getEntityPrefix(cl)));
        params.prefix(entityPrefix);
        addKeySpaceMapping(entityPrefix, cl);
      }

      updateTTLSettings(cl, entityPrefix, isDocument, document, allClassFields);
      List<SchemaField> fields = searchFields.stream().map(SearchField::getSchemaField).toList();
      entityClassToSchema.put(cl, searchFields);
      entityClassToIndexName.put(cl, indexName);
      allCreatedIndexNames.add(indexName);

      final String capturedPrefix = entityPrefix;
      if (maybeIndexingOptions.isPresent()) {
        applyCreationMode(opsForSearch, params, fields, maybeIndexingOptions.get().creationMode(), indexName, cl,
            () -> indexExistsFor(cl), () -> createSortedSetsForLexicographicFields(cl, capturedPrefix));
      } else {
        opsForSearch.createIndex(params, fields);
        logger.info(String.format("Created index %s", indexName));
      }

      // Always (re)create sorted sets for lexicographic fields at the end
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
   * Registers a secondary key prefix → entity class mapping without disturbing the
   * entity's canonical keyspace. Unlike {@link #addKeySpaceMapping(String, Class)},
   * this method only populates the forward {@code keyspace → entityClass} lookup via
   * {@code putIfAbsent} and never overwrites {@code entityClassToKeySpace} (the canonical
   * reverse mapping used by {@link #getKeyspaceForEntityClass(Class)} and friends).
   * <p>
   * Used both for repository-level indexes (where the entity already has its own
   * canonical keyspace declared on {@code @Document}/{@code @RedisHash}) and for
   * entity-level {@code @IndexingOptions.prefixes()} where the first element is the
   * canonical prefix and any remaining elements are additional coverage.
   *
   * @param keyspace    the additional key prefix covered by an index
   * @param entityClass the entity class the index is defined for
   */
  private void registerSecondaryKeyspace(String keyspace, Class<?> entityClass) {
    String key = getKeyspace(keyspace);
    keyspaceToEntityClass.putIfAbsent(key, entityClass);
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
      if (!rawKeyPrefix.isBlank()) {
        if (containsSpelExpression(rawKeyPrefix)) {
          // SpEL: always re-evaluate — result varies per tenant / environment
          String defaultKeyspace = deriveDefaultKeyspace(entityClass);
          String keyspace = evaluateExpression(rawKeyPrefix, defaultKeyspace);
          return keyspace.endsWith(":") ? keyspace : keyspace + ":";
        } else {
          // Static literal keyPrefix in @IndexingOptions — use it directly without
          // waiting for the index-creation path to fill the cache.
          return rawKeyPrefix.endsWith(":") ? rawKeyPrefix : rawKeyPrefix + ":";
        }
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
    String indexName = getIndexName(entityClass);
    if (indexName == null) {
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
    String indexName = getIndexName(cl);
    try {
      SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
      if (dropDocuments) {
        opsForSearch.dropIndexAndDocuments();
      } else {
        opsForSearch.dropIndex();
      }
      String entityPrefix = getKeyspaceForEntityClass(cl);
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

  private FTCreateParams createIndexDefinition(Class<?> cl, IndexDataType idxType) {
    return createIndexDefinition(cl, idxType, null);
  }

  private FTCreateParams createIndexDefinition(Class<?> cl, IndexDataType idxType,
      IndexingOptions repoIndexingOptions) {
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

    // Entity-level @IndexingOptions filter overrides @Document filter
    IndexingOptions entityOptions = cl.getAnnotation(IndexingOptions.class);
    if (entityOptions != null && !entityOptions.filter().isBlank()) {
      String filter = evaluateExpression(entityOptions.filter(), "");
      if (!filter.isBlank()) {
        params.filter(filter);
      }
    }

    // Repository-level @IndexingOptions filter overrides everything
    if (repoIndexingOptions != null && !repoIndexingOptions.filter().isBlank()) {
      String filter = evaluateExpression(repoIndexingOptions.filter(), "");
      if (!filter.isBlank()) {
        params.filter(filter);
      }
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

  private void registerAlias(Class<?> cl, String fieldName, String alias) {
    entityClassFieldToAlias.put(Tuples.of(cl, fieldName), alias);
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
      List<SearchField> searchFields = prepareSearchFields(entityClass, isDocument, allClassFields);
      Optional<String> maybeScoreField = indexDefinitionBuilder.getDocumentScoreField(allClassFields, isDocument);

      SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
      FTCreateParams params = createIndexDefinition(entityClass, idxType);

      if (isDocument) {
        maybeScoreField.ifPresent(params::scoreField);
      }

      String entityPrefix = getKeyspace(keyPrefix);
      params.prefix(entityPrefix);
      // Do NOT call addKeySpaceMapping here — this method creates ephemeral / migration
      // indexes with ad-hoc prefixes and must not overwrite the entity's canonical keyspace
      // mapping that was established by the main createIndexFor(Class) path.

      Optional<Document> document = isDocument ?
          Optional.of(entityClass.getAnnotation(Document.class)) :
          Optional.empty();
      updateTTLSettings(entityClass, entityPrefix, isDocument, document, allClassFields);

      List<SchemaField> fields = searchFields.stream().map(SearchField::getSchemaField).toList();

      // Note: We don't update entityClassToSchema or entityClassToIndexName here
      // because this method is for creating indexes with custom names/prefixes.
      // The global mappings should only be updated by the main createIndexFor(Class) method.

      if (maybeIndexingOptions.isPresent()) {
        applyCreationMode(opsForSearch, params, fields, maybeIndexingOptions.get().creationMode(), indexName,
            entityClass, () -> indexExistsFor(entityClass, indexName), () -> createSortedSetsForLexicographicFields(
                entityClass, entityPrefix));
      } else {
        opsForSearch.createIndex(params, fields);
        logger.info(String.format("Created index %s", indexName));
        createSortedSetsForLexicographicFields(entityClass, entityPrefix);
      }

      return true;
    } catch (Exception e) {
      logger.warn(String.format(SKIPPING_INDEX_CREATION, indexName, e.getMessage()));
      return false;
    }
  }

  /**
   * Creates an index for the specified entity class using repository-level {@link IndexingOptions}.
   * This method reads all settings from the annotation, including index name, key prefix,
   * filter expression, and creation mode.
   *
   * @param entityClass         the entity class
   * @param repoIndexingOptions the {@link IndexingOptions} annotation from the repository interface
   * @return true if successful, false otherwise
   */
  public boolean createIndexFor(Class<?> entityClass, IndexingOptions repoIndexingOptions) {
    String defaultIndexName = entityClass.getName() + "Idx";
    String indexName = repoIndexingOptions.indexName().isBlank() ?
        defaultIndexName :
        evaluateExpression(repoIndexingOptions.indexName(), defaultIndexName);

    try {
      Optional<IndexDataType> maybeType = determineIndexTarget(entityClass);
      if (maybeType.isEmpty()) {
        return false;
      }
      IndexDataType idxType = maybeType.get();
      boolean isDocument = idxType == IndexDataType.JSON;

      logger.info(String.format("Creating repo-level index %s for class: %s", indexName, entityClass.getName()));

      final List<java.lang.reflect.Field> allClassFields = getDeclaredFieldsTransitively(entityClass);
      List<SearchField> searchFields = prepareSearchFields(entityClass, isDocument, allClassFields);
      Optional<String> maybeScoreField = indexDefinitionBuilder.getDocumentScoreField(allClassFields, isDocument);

      SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
      FTCreateParams params = createIndexDefinition(entityClass, idxType, repoIndexingOptions);

      if (isDocument) {
        maybeScoreField.ifPresent(params::scoreField);
      }

      // Resolve prefixes — also register each resolved prefix in the keyspace→entity map
      // so lookups (e.g. getEntityClassForKeyspace) know about every prefix the repo-level
      // index covers, not just the entity-level one.
      if (repoIndexingOptions.prefixes().length > 0) {
        String[] resolvedPrefixes = resolveAndNormalizePrefixArray(repoIndexingOptions.prefixes());
        params.prefix(resolvedPrefixes);
        for (String p : resolvedPrefixes) {
          registerSecondaryKeyspace(p, entityClass);
        }
      } else {
        String defaultPrefix = getKeyspaceForEntityClass(entityClass);
        if (defaultPrefix == null || defaultPrefix.isBlank()) {
          defaultPrefix = getKeyspace(getEntityPrefix(entityClass));
        }
        String entityPrefix = repoIndexingOptions.keyPrefix().isBlank() ?
            defaultPrefix :
            getKeyspace(evaluateExpression(repoIndexingOptions.keyPrefix(), defaultPrefix));
        params.prefix(entityPrefix);
        // If the entity class has no @IndexingOptions of its own, promote the repo-level
        // keyPrefix as the canonical keyspace so that resolveDynamicKeyspace (and therefore
        // writes/reads) uses the same prefix as the index rather than the Spring default.
        if (entityClass.getAnnotation(IndexingOptions.class) == null) {
          entityClassToKeySpace.putIfAbsent(entityClass, entityPrefix);
        }
        registerSecondaryKeyspace(entityPrefix, entityClass);
      }

      List<SchemaField> fields = searchFields.stream().map(SearchField::getSchemaField).toList();

      applyCreationMode(opsForSearch, params, fields, repoIndexingOptions.creationMode(), indexName, entityClass,
          () -> indexExistsFor(entityClass, indexName), () -> {
          });

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
    // Snapshot to avoid ConcurrentModificationException; allCreatedIndexNames accumulates every
    // index name resolved at create time (including per-tenant SpEL variants), so we drop them
    // all rather than re-evaluating SpEL once in the current thread's context.
    Set<String> namesToDrop = new HashSet<>(allCreatedIndexNames);
    logger.info(String.format("Dropping %d tracked indexes", namesToDrop.size()));
    for (String indexName : namesToDrop) {
      try {
        SearchOperations<String> ops = rmo.opsForSearch(indexName);
        ops.dropIndex();
      } catch (Exception e) {
        logger.warn(String.format("Failed to drop index %s: %s", indexName, e.getMessage()));
      }
    }
    // Clear only the resolved-name caches so listIndexes() returns empty and
    // subsequent SpEL re-evaluation picks up fresh names. indexedEntityClasses
    // is intentionally kept intact so createIndexes() can re-register them all.
    allCreatedIndexNames.clear();
    entityClassToIndexName.clear();
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