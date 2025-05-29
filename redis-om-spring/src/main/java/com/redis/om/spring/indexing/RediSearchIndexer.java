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
import org.springframework.data.annotation.Reference;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration.KeyspaceSettings;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.util.TypeInformation;
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
  private final ApplicationContext ac;
  private final RedisModulesOperations<String> rmo;
  private final RedisMappingContext mappingContext;
  private final GsonBuilder gsonBuilder;
  private final RedisOMProperties properties;

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
    Optional<String> maybeScoreField;
    try {
      if (isDocument) {
        // IndexingOptions overrides Document#
        if (maybeIndexingOptions.isPresent()) {
          indexName = maybeIndexingOptions.get().indexName();
        }
        indexName = indexName.isBlank() ? document.get().indexName() : indexName;
        indexName = indexName.isBlank() ? cl.getName() + "Idx" : indexName;
      } else {
        if (maybeIndexingOptions.isPresent()) {
          indexName = maybeIndexingOptions.get().indexName();
          indexName = indexName.isBlank() ? cl.getName() + "Idx" : indexName;
        } else {
          indexName = cl.getName() + "Idx";
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

      String entityPrefix = maybeEntityPrefix.orElse(getEntityPrefix(cl));
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
            break;
          case DROP_AND_RECREATE:
            if (indexExistsFor(cl)) {
              opsForSearch.dropIndex();
              logger.info(String.format("Dropped index %s", indexName));
            }
            opsForSearch.createIndex(params, fields);
            logger.info(String.format("Created index %s", indexName));
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
   * @param entityClass the entity class to get the index name for
   * @return the index name for the entity class, or a default name based on the class name
   */
  public String getIndexName(Class<?> entityClass) {
    if (entityClass != null && entityClassToIndexName.containsKey(entityClass)) {
      return entityClassToIndexName.get(entityClass);
    } else {
      return entityClass.getName() + "Idx";
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
   * If no explicit mapping exists, derives the keyspace from the entity's persistent configuration
   * or uses the class name as fallback.
   *
   * @param entityClass the entity class to get the keyspace for
   * @return the Redis keyspace associated with the entity class
   */
  public String getKeyspaceForEntityClass(Class<?> entityClass) {
    String keyspace = entityClassToKeySpace.get(entityClass);
    if (keyspace == null) {
      var persistentEntity = mappingContext.getPersistentEntity(entityClass);
      if (persistentEntity != null) {
        String entityKeySpace = persistentEntity.getKeySpace();
        keyspace = (entityKeySpace != null ? entityKeySpace : entityClass.getName()) + ":";
      }
    }
    return keyspace;
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
   * @throws JedisDataException if a Redis error occurs other than "Unknown index name"
   */
  public boolean indexExistsFor(Class<?> entityClass) {
    try {
      return getIndexInfo(entityClass) != null;
    } catch (JedisDataException jde) {
      if (jde.getMessage().contains("Unknown index name")) {
        return false;
      } else {
        throw jde;
      }
    }
  }

  Map<String, Object> getIndexInfo(Class<?> entityClass) {
    String indexName = entityClassToIndexName.get(entityClass);
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

      Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(field.getType());

      if (field.isAnnotationPresent(Reference.class)) {
        //
        // @Reference @Indexed fields: Create schema field for the reference entity @Id
        // field
        //
        logger.debug("🪲Found @Reference field " + field.getName() + " in " + field.getDeclaringClass()
            .getSimpleName());
        createIndexedFieldForReferenceIdField(field, isDocument).ifPresent(fields::add);
      } else if (indexed.schemaFieldType() == SchemaFieldType.AUTODETECT) {
        //
        // Any Character class, Boolean or Enum with AUTODETECT -> Tag Search Field
        // Also UUID and Ulid (classes whose toString() is a valid text representation
        // of the value)
        //
        if (CharSequence.class.isAssignableFrom(fieldType) || //
            (fieldType == Boolean.class) || (fieldType == UUID.class) || (fieldType == Ulid.class)) {
          fields.add(SearchField.of(field, indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(), indexed
              .separator(), indexed.arrayIndex(), indexed.alias())));
        } else if (fieldType.isEnum()) {
          if (Objects.requireNonNull(indexed.serializationHint()) == SerializationHint.ORDINAL) {
            fields.add(SearchField.of(field, indexAsNumericFieldFor(field, isDocument, prefix, indexed.sortable(),
                indexed.noindex(), indexed.alias())));
            gsonBuilder.registerTypeAdapter(fieldType, EnumTypeAdapter.of(fieldType));
          } else {
            fields.add(SearchField.of(field, indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(), indexed
                .separator(), indexed.arrayIndex(), indexed.alias())));
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
              .noindex(), indexed.alias())));
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
                  .separator(), indexed.arrayIndex(), indexed.alias())));
            } else if (isDocument) {
              if (Number.class.isAssignableFrom(collectionType)) {
                fields.add(SearchField.of(field, indexAsNumericFieldFor(field, true, prefix, indexed.sortable(), indexed
                    .noindex(), indexed.alias())));
              } else if (collectionType == Point.class) {
                fields.add(SearchField.of(field, indexAsGeoFieldFor(field, true, prefix, indexed.alias())));
              } else if (collectionType == UUID.class || collectionType == Ulid.class) {
                fields.add(SearchField.of(field, indexAsTagFieldFor(field, true, prefix, indexed.sortable(), indexed
                    .separator(), 0, indexed.alias())));
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
              indexed.separator(), indexed.arrayIndex(), indexed.alias())));
          case NUMERIC -> fields.add(SearchField.of(field, indexAsNumericFieldFor(field, isDocument, prefix, indexed
              .sortable(), indexed.noindex(), indexed.alias())));
          case GEO -> fields.add(SearchField.of(field, indexAsGeoFieldFor(field, true, prefix, indexed.alias())));
          case VECTOR -> fields.add(SearchField.of(field, indexAsVectorFieldFor(field, isDocument, prefix, indexed)));
          case NESTED -> {
            for (java.lang.reflect.Field subfield : com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(
                field.getType())) {
              String subfieldPrefix = (prefix == null || prefix.isBlank()) ?
                  field.getName() :
                  String.join(".", prefix, field.getName());
              fields.addAll(findIndexFields(subfield, subfieldPrefix, isDocument));
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
    FieldName fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(annotationAlias), Optional.of(
        arrayIndex));
    return getTagField(fieldName, separator, sortable);
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
    var fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(annotationAlias), Optional.empty());

    NumericField num = NumericField.of(fieldName);
    if (sortable)
      num.sortable();
    if (noIndex)
      num.noIndex();
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

  private TagField getTagField(FieldName fieldName, String separator, boolean sortable) {
    TagField tag = TagField.of(fieldName);
    if (separator != null) {
      if (separator.length() != 1) {
        throw new IllegalArgumentException("Separator '" + separator + "' is not of length 1.");
      }
      tag.separator(separator.charAt(0));
    }
    if (sortable)
      tag.sortable();
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
        .isAnnotationPresent(TagIndexed.class) && !idField.isAnnotationPresent(TextIndexed.class) && (fields.stream()
            .noneMatch(f -> f.getName().equals(idField.getName()))));
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

      mappingContext.getMappingConfiguration().getKeyspaceConfiguration().addKeyspaceSettings(setting);
    }
  }

  private String getKeyspace(String keyspace) {
    return keyspace.endsWith(":") ? keyspace : keyspace + ":";
  }

  private String getFieldPrefix(String prefix, boolean isDocument) {
    String chain = (prefix == null || prefix.isBlank()) ? "" : prefix + ".";
    return isDocument ? "$." + chain : chain;
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
}