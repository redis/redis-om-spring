package com.redis.om.spring;

import com.google.gson.annotations.JsonAdapter;
import com.redis.om.spring.annotations.*;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.query.QueryUtils;
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
import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.Schema.*;
import redis.clients.jedis.search.Schema.VectorField.VectorAlgo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.redis.om.spring.util.ObjectUtils.*;

@Component
public class RediSearchIndexer {
  private final Map<String, Class<?>> keyspaceToEntityClass = new ConcurrentHashMap<>();
  private final Map<Class<?>, String> entityClassToKeySpace = new ConcurrentHashMap<>();
  private final List<Class<?>> indexedEntityClasses = new ArrayList<>();
  private final Map<Class<?>, Schema> entityClassToSchema = new ConcurrentHashMap<>();

  private static final Log logger = LogFactory.getLog(RediSearchIndexer.class);

  private final ApplicationContext ac;
  private final RedisModulesOperations<String> rmo;
  private final RedisMappingContext mappingContext;

  private static final String SKIPPING_INDEX_CREATION = "Skipping index creation for %s because %s";

  @SuppressWarnings("unchecked")
  public RediSearchIndexer(ApplicationContext ac) {
    this.ac = ac;
    rmo = (RedisModulesOperations<String>) ac.getBean("redisModulesOperations");
    mappingContext = (RedisMappingContext) ac.getBean("keyValueMappingContext");
  }

  public void createIndicesFor(Class<?> cls) {
    Set<BeanDefinition> beanDefs = new HashSet<>(getBeanDefinitionsFor(ac, cls));

    logger.info(String.format("Found %s @%s annotated Beans...", beanDefs.size(), cls.getSimpleName()));

    for (BeanDefinition beanDef : beanDefs) {
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        logger.info(String.format("Creating index for %s annotated Entity...", cl.getSimpleName()));
        createIndexFor(cl);
      } catch (ClassNotFoundException e) {
        logger.warn(
            String.format(SKIPPING_INDEX_CREATION, beanDef.getBeanClassName(), e.getMessage()));
      }
    }
  }

  public void createIndexFor(Class<?> cl) {
    Optional<IndexDefinition.Type> maybeType = determineIndexTarget(cl);
    IndexDefinition.Type idxType;
    if (maybeType.isPresent()) {
      idxType = maybeType.get();
    } else {
      return;
    }
    boolean isDocument = idxType == IndexDefinition.Type.JSON;
    Optional<Document> document = isDocument ? Optional.of(cl.getAnnotation(Document.class)) : Optional.empty();
    Optional<RedisHash> hash = !isDocument ? Optional.of(cl.getAnnotation(RedisHash.class)) : Optional.empty();

    String indexName = "";
    Optional<String> maybeScoreField;
    try {
      indexName = cl.getName() + "Idx";
      logger.info(String.format("Found @%s annotated class: %s", idxType, cl.getName()));

      final List<java.lang.reflect.Field> allClassFields = getDeclaredFieldsTransitively(cl);

      List<Field> fields = processIndexedFields(allClassFields, isDocument);
      maybeScoreField = getDocumentScoreField(allClassFields, isDocument);
      createIndexedFieldForIdField(cl, fields, isDocument).ifPresent(fields::add);

      Schema schema = new Schema();
      SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
      fields.forEach(schema::addField);

      IndexDefinition index = createIndexDefinition(cl, idxType);

      Optional<String> maybeEntityPrefix;
      if (isDocument) {
        maybeEntityPrefix = document.map(Document::value).filter(ObjectUtils::isNotEmpty);
        maybeScoreField.ifPresent(index::setScoreFiled);
      } else {
        maybeEntityPrefix = hash.map(RedisHash::value).filter(ObjectUtils::isNotEmpty);
      }

      String entityPrefix = maybeEntityPrefix.orElse(getEntityPrefix(cl));
      index.setPrefixes(entityPrefix);
      IndexOptions ops = IndexOptions.defaultOptions().setDefinition(index);
      addKeySpaceMapping(entityPrefix, cl);
      updateTTLSettings(cl, entityPrefix, isDocument, document, allClassFields);
      opsForSearch.createIndex(schema, ops);
      entityClassToSchema.put(cl, schema);
    } catch (Exception e) {
      logger.warn(String.format(SKIPPING_INDEX_CREATION, indexName, e.getMessage()));
    }
  }

  public void dropIndexAndDocumentsFor(Class<?> cl) {
    dropIndex(cl, true, false);
  }

  public void dropAndRecreateIndexFor(Class<?> cl) {
    dropIndex(cl, false, true);
  }

  public void dropIndexFor(Class<?> cl) {
    dropIndex(cl, false, false);
  }

  public Optional<String> getIndexName(String keyspace) {
    return getIndexName(keyspaceToEntityClass.get(getKeyspace(keyspace)));
  }

  public Optional<String> getIndexName(Class<?> entityClass) {
    if (entityClass != null && entityClassToKeySpace.containsKey(entityClass)) {
      return Optional.of(entityClass.getName() + "Idx");
    } else {
      return Optional.empty();
    }
  }

  public void addKeySpaceMapping(String keyspace, Class<?> entityClass) {
    String key = getKeyspace(keyspace);
    keyspaceToEntityClass.put(key, entityClass);
    entityClassToKeySpace.put(entityClass, key);
    indexedEntityClasses.add(entityClass);
  }

  public void removeKeySpaceMapping(String keyspace, Class<?> entityClass) {
    String key = getKeyspace(keyspace);
    keyspaceToEntityClass.remove(key);
    entityClassToKeySpace.remove(entityClass);
    indexedEntityClasses.remove(entityClass);
  }

  public Class<?> getEntityClassForKeyspace(String keyspace) {
    return keyspaceToEntityClass.get(getKeyspace(keyspace));
  }

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

  public boolean indexExistsFor(Class<?> entityClass) {
    return indexedEntityClasses.contains(entityClass);
  }

  private List<Field> findIndexFields(java.lang.reflect.Field field, String prefix, boolean isDocument) {
    List<Field> fields = new ArrayList<>();

    if (field.isAnnotationPresent(Indexed.class)) {
      logger.info(String.format("Found @Indexed annotation on field of type: %s", field.getType()));

      Indexed indexed = field.getAnnotation(Indexed.class);

      Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(field.getType());

      if (field.isAnnotationPresent(Reference.class)) {
        //
        // @Reference @Indexed fields: Create schema field for the reference entity @Id field
        //
        logger.debug("🪲Found @Reference field " + field.getName() + " in " + field.getDeclaringClass().getSimpleName());
        var maybeReferenceIdField = getIdFieldForEntityClass(fieldType);
        if (maybeReferenceIdField.isPresent()) {
          var idFieldToIndex = maybeReferenceIdField.get();
          createIndexedFieldForReferenceIdField(field, idFieldToIndex, isDocument).ifPresent(fields::add);
        } else {
          logger.warn("Couldn't find ID field for reference" + field.getName() + " in " + field.getDeclaringClass().getSimpleName());
        }
      } else if (indexed.schemaFieldType() == SchemaFieldType.AUTODETECT) {
        //
        // Any Character class, Enums or Boolean -> Tag Search Field
        //
        if (CharSequence.class.isAssignableFrom(fieldType) || (fieldType == Boolean.class) || (fieldType.isEnum())) {
          fields.add(indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.separator(),
              indexed.arrayIndex()));
        }
        //
        // Any Numeric class -> Numeric Search Field
        //
        else if (Number.class.isAssignableFrom(fieldType) || (fieldType == LocalDateTime.class)
            || (field.getType() == LocalDate.class) || (field.getType() == Date.class)
            || (field.getType() == Instant.class) || (field.getType() == OffsetDateTime.class)) {
          fields.add(indexAsNumericFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.noindex()));
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
              fields.add(indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.separator(),
                  indexed.arrayIndex()));
              // Index nested fields
            } else if (isDocument) {
              if (Number.class.isAssignableFrom(collectionType)) {
                fields.add(indexAsNumericFieldFor(field, true, prefix, indexed.sortable(), indexed.noindex()));
              } else if (collectionType == Point.class) {
                fields.add(indexAsGeoFieldFor(field, true, prefix));
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
          fields.add(indexAsGeoFieldFor(field, isDocument, prefix));
        }
        //
        // Recursively explore the fields for Index annotated fields
        //
        else {
          for (java.lang.reflect.Field subfield : getDeclaredFieldsTransitively(field.getType())) {
            String subfieldPrefix = (prefix == null || prefix.isBlank()) ? field.getName()
                : String.join(".", prefix, field.getName());
            fields.addAll(findIndexFields(subfield, subfieldPrefix, isDocument));
          }
        }
      } else { // Schema field type hardcoded/set in @Indexed
        switch (indexed.schemaFieldType()) {
          case TAG ->
            fields.add(indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.separator(),
                indexed.arrayIndex()));
          case NUMERIC ->
            fields.add(indexAsNumericFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.noindex()));
          case GEO -> fields.add(indexAsGeoFieldFor(field, true, prefix));
          case VECTOR -> fields.add(indexAsVectorFieldFor(field, isDocument, prefix, indexed));
          case NESTED -> {
            for (java.lang.reflect.Field subfield : com.redis.om.spring.util.ObjectUtils
                .getDeclaredFieldsTransitively(field.getType())) {
              String subfieldPrefix = (prefix == null || prefix.isBlank()) ? field.getName()
                  : String.join(".", prefix, field.getName());
              fields.addAll(findIndexFields(subfield, subfieldPrefix, isDocument));
            }
          }
        }
      }
    }

    // Searchable - behaves like Text indexed
    else if (field.isAnnotationPresent(Searchable.class)) {
      logger.info(String.format("Found @Searchable annotation on field of type: %s", field.getType()));
      Searchable searchable = field.getAnnotation(Searchable.class);
      fields.add(indexAsTextFieldFor(field, isDocument, prefix, searchable));
    }
    // Text
    else if (field.isAnnotationPresent(TextIndexed.class)) {
      TextIndexed ti = field.getAnnotation(TextIndexed.class);
      fields.add(indexAsTextFieldFor(field, isDocument, prefix, ti));
    }
    // Tag
    else if (field.isAnnotationPresent(TagIndexed.class)) {
      TagIndexed ti = field.getAnnotation(TagIndexed.class);
      fields.add(indexAsTagFieldFor(field, isDocument, prefix, ti));
    }
    // Geo
    else if (field.isAnnotationPresent(GeoIndexed.class)) {
      GeoIndexed gi = field.getAnnotation(GeoIndexed.class);
      fields.add(indexAsGeoFieldFor(field, isDocument, prefix, gi));
    }
    // Numeric
    else if (field.isAnnotationPresent(NumericIndexed.class)) {
      NumericIndexed ni = field.getAnnotation(NumericIndexed.class);
      fields.add(indexAsNumericFieldFor(field, isDocument, prefix, ni));
    }
    // Vector
    else if (field.isAnnotationPresent(VectorIndexed.class)) {
      VectorIndexed vi = field.getAnnotation(VectorIndexed.class);
      fields.add(indexAsVectorFieldFor(field, isDocument, prefix, vi));
    }

    return fields;
  }

  private Field indexAsTagFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, TagIndexed ti) {
    TypeInformation<?> typeInfo = TypeInformation.of(field.getType());
    String fieldPrefix = getFieldPrefix(prefix, isDocument);

    String fieldPostfix = (isDocument && typeInfo.isCollectionLike() && !field.isAnnotationPresent(JsonAdapter.class))
        ? "[*]"
        : "";
    FieldName fieldName = FieldName.of(fieldPrefix + field.getName() + fieldPostfix);

    if (!ObjectUtils.isEmpty(ti.alias())) {
      fieldName = fieldName.as(ti.alias());
    } else {
      fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));
    }

    return new TagField(fieldName, ti.separator(), false);
  }

  private Field indexAsVectorFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      Indexed indexed) {
    TypeInformation<?> typeInfo = TypeInformation.of(field.getType());
    String fieldPrefix = getFieldPrefix(prefix, isDocument);

    String fieldPostfix = (isDocument && typeInfo.isCollectionLike() && !field.isAnnotationPresent(JsonAdapter.class))
        ? "[*]"
        : "";
    String fieldName = fieldPrefix + field.getName() + fieldPostfix;

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("TYPE", indexed.type().toString());
    attributes.put("DIM", indexed.dimension());
    attributes.put("DISTANCE_METRIC", indexed.distanceMetric());

    if (indexed.initialCapacity() > 0) {
      attributes.put("INITIAL_CAP", indexed.initialCapacity());
    }

    // Optional parameters for FLAT
    if (indexed.algorithm().equals(VectorAlgo.FLAT) && (indexed.blockSize() > 0)) {
      attributes.put("BLOCK_SIZE", indexed.blockSize());
    }

    if (indexed.algorithm().equals(VectorAlgo.HNSW)) {
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

    VectorField vectorField = new VectorField(fieldName, indexed.algorithm(), attributes);

    if (!ObjectUtils.isEmpty(indexed.alias())) {
      vectorField.as(indexed.alias());
    } else {
      vectorField.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));
    }

    return vectorField;
  }

  private Field indexAsVectorFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      VectorIndexed vi) {
    TypeInformation<?> typeInfo = TypeInformation.of(field.getType());
    String fieldPrefix = getFieldPrefix(prefix, isDocument);

    String fieldPostfix = (isDocument && typeInfo.isCollectionLike() && !field.isAnnotationPresent(JsonAdapter.class))
        ? "[*]"
        : "";
    String fieldName = fieldPrefix + field.getName() + fieldPostfix;

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("TYPE", vi.type().toString());
    attributes.put("DIM", vi.dimension());
    attributes.put("DISTANCE_METRIC", vi.distanceMetric());

    if (vi.initialCapacity() > 0) {
      attributes.put("INITIAL_CAP", vi.initialCapacity());
    }

    // Optional parameters for FLAT
    if (vi.algorithm().equals(VectorAlgo.FLAT) && (vi.blockSize() > 0)) {
      attributes.put("BLOCK_SIZE", vi.blockSize());
    }

    if (vi.algorithm().equals(VectorAlgo.HNSW)) {
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

    VectorField vectorField = new VectorField(fieldName, vi.algorithm(), attributes);

    if (!ObjectUtils.isEmpty(vi.alias())) {
      vectorField.as(vi.alias());
    } else {
      vectorField.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));
    }

    return vectorField;
  }

  private Field indexAsTagFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, boolean sortable,
      String separator, int arrayIndex) {
    TypeInformation<?> typeInfo = TypeInformation.of(field.getType());
    String fieldPrefix = getFieldPrefix(prefix, isDocument);
    String index = (arrayIndex != Integer.MIN_VALUE) ? ".[" + arrayIndex + "]" : "[*]";
    String fieldPostfix = (isDocument && typeInfo.isCollectionLike() && !field.isAnnotationPresent(JsonAdapter.class))
        ? index
        : "";
    FieldName fieldName = FieldName.of(fieldPrefix + field.getName() + fieldPostfix);

    fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));

    return new TagField(fieldName, separator.isBlank() ? null : separator, sortable);
  }

  private Field indexAsTextFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, TextIndexed ti) {
    String fieldPrefix = getFieldPrefix(prefix, isDocument);
    FieldName fieldName = FieldName.of(fieldPrefix + field.getName());

    if (!ObjectUtils.isEmpty(ti.alias())) {
      fieldName = fieldName.as(ti.alias());
    } else {
      fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));
    }

    String phonetic = ObjectUtils.isEmpty(ti.phonetic()) ? null : ti.phonetic();

    return new TextField(fieldName, ti.weight(), ti.sortable(), ti.nostem(), ti.noindex(), phonetic);
  }

  private Field indexAsTextFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, Searchable ti) {
    String fieldPrefix = getFieldPrefix(prefix, isDocument);
    FieldName fieldName = FieldName.of(fieldPrefix + field.getName());

    if (!ObjectUtils.isEmpty(ti.alias())) {
      fieldName = fieldName.as(ti.alias());
    } else {
      fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));
    }
    String phonetic = ObjectUtils.isEmpty(ti.phonetic()) ? null : ti.phonetic();

    return new TextField(fieldName, ti.weight(), ti.sortable(), ti.nostem(), ti.noindex(), phonetic);
  }

  private Field indexAsGeoFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, GeoIndexed gi) {
    String fieldPrefix = getFieldPrefix(prefix, isDocument);
    FieldName fieldName = FieldName.of(fieldPrefix + field.getName());

    if (!ObjectUtils.isEmpty(gi.alias())) {
      fieldName = fieldName.as(gi.alias());
    } else {
      fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));
    }

    return new Field(fieldName, FieldType.GEO);
  }

  private Field indexAsNumericFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      NumericIndexed ni) {
    String fieldPrefix = getFieldPrefix(prefix, isDocument);
    FieldName fieldName = FieldName.of(fieldPrefix + field.getName());

    if (!ObjectUtils.isEmpty(ni.alias())) {
      fieldName = fieldName.as(ni.alias());
    } else {
      fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));
    }

    return new Field(fieldName, FieldType.NUMERIC);
  }

  private Field indexAsNumericFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      boolean sortable, boolean noIndex) {
    String fieldPrefix = getFieldPrefix(prefix, isDocument);
    FieldName fieldName = FieldName.of(fieldPrefix + field.getName());

    fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));

    return new Field(fieldName, FieldType.NUMERIC, sortable, noIndex);
  }

  private Field indexAsGeoFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix) {
    String fieldPrefix = getFieldPrefix(prefix, isDocument);
    FieldName fieldName = FieldName.of(fieldPrefix + field.getName());

    fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));

    return new Field(fieldName, FieldType.GEO);
  }

  private List<Field> indexAsNestedFieldFor(java.lang.reflect.Field field, String prefix) {
    String fieldPrefix = getFieldPrefix(prefix, true);
    return getNestedField(fieldPrefix, field, prefix, null);
  }

  private List<Field> getNestedField(String fieldPrefix, java.lang.reflect.Field field, String prefix,
      List<Field> fieldList) {
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

        if (subField.isAnnotationPresent(TagIndexed.class)) {
          TagIndexed ti = subField.getAnnotation(TagIndexed.class);
          tempPrefix = field.getName() + "[0:].";

          FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName());
          fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(subField, prefix));

          logger.info(String.format("Creating nested relationships: %s -> %s", field.getName(), subField.getName()));
          fieldList.add(new TagField(fieldName, ti.separator(), false));
          continue;
        } else if (subField.isAnnotationPresent(Indexed.class)) {
          boolean subFieldIsTagField = (subField.isAnnotationPresent(Indexed.class)
              && (CharSequence.class.isAssignableFrom(subField.getType()) || (subField.getType() == Boolean.class)
                  || (maybeCollectionType.isPresent() && (CharSequence.class.isAssignableFrom(maybeCollectionType.get())
                      || (maybeCollectionType.get() == Boolean.class)))));
          if (subFieldIsTagField) {
            Indexed indexed = subField.getAnnotation(Indexed.class);
            tempPrefix = field.getName() + "[0:].";

            FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName());
            fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(subField, prefix));

            logger.info(String.format("Creating nested relationships: %s -> %s", field.getName(), subField.getName()));
            fieldList.add(new TagField(fieldName, indexed.separator(), false));
            continue;
          }

          else if (Number.class.isAssignableFrom(subField.getType()) || (subField.getType() == LocalDateTime.class)
              || (subField.getType() == LocalDate.class) || (subField.getType() == Date.class)) {

            FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName());
            fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(subField, prefix));
            logger.info(String.format("Creating nested relationships: %s -> %s", field.getName(), subField.getName()));
            fieldList.add(new Field(fieldName, FieldType.NUMERIC));
          }
        } else if (subField.isAnnotationPresent(Searchable.class)) {
          Searchable searchable = subField.getAnnotation(Searchable.class);
          tempPrefix = field.getName() + "[0:].";

          FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName());
          fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(subField, prefix));

          logger
              .info(String.format("Creating TEXT nested relationships: %s -> %s", field.getName(), subField.getName()));

          String phonetic = ObjectUtils.isEmpty(searchable.phonetic()) ? null : searchable.phonetic();

          fieldList.add(new TextField(fieldName, searchable.weight(), searchable.sortable(), searchable.nostem(),
              searchable.noindex(), phonetic));

          continue;
        }
        if (subField.isAnnotationPresent(Indexed.class)) {
          getNestedField(fieldPrefix + tempPrefix, subField, prefix, fieldList);
        }
      }
    }
    return fieldList;
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
    String indexName = generateIndexName(cl);
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

  private String generateIndexName(Class<?> cl) {
    String indexName = cl.getName() + "Idx";
    logger.info(String.format("Dropping index @%s for class: %s", indexName, cl.getName()));
    return indexName;
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

  private Optional<IndexDefinition.Type> determineIndexTarget(Class<?> cl) {
    if (cl.isAnnotationPresent(Document.class)) {
      return Optional.of(IndexDefinition.Type.JSON);
    } else if (cl.isAnnotationPresent(RedisHash.class)) {
      return Optional.of(IndexDefinition.Type.HASH);
    } else {
      return Optional.empty();
    }
  }

  private List<Field> processIndexedFields(List<java.lang.reflect.Field> allClassFields, boolean isDocument) {
    List<Field> fields = new ArrayList<>();
    for (java.lang.reflect.Field field : allClassFields) {
      fields.addAll(findIndexFields(field, null, isDocument));
    }
    return fields;
  }

  private Optional<String> getDocumentScoreField(List<java.lang.reflect.Field> allClassFields, boolean isDocument) {
    return allClassFields.stream()
        .filter(field -> field.isAnnotationPresent(DocumentScore.class))
        .findFirst()
        .map(field -> (isDocument ? "$." : "") + field.getName());
  }

  private Optional<Field> createIndexedFieldForIdField(Class<?> cl, List<Field> fields, boolean isDocument) {
    Optional<Field> result = Optional.empty();
    Optional<java.lang.reflect.Field> maybeIdField = getIdFieldForEntityClass(cl);
    if (maybeIdField.isPresent()) {
      java.lang.reflect.Field idField = maybeIdField.get();
      // Only auto-index the @Id if not already indexed by the user (gh-135)
      if (!idField.isAnnotationPresent(Indexed.class)
          && !idField.isAnnotationPresent(Searchable.class)
          && !idField.isAnnotationPresent(TagIndexed.class)
          && !idField.isAnnotationPresent(TextIndexed.class)
          && (fields.stream().noneMatch(f -> f.name.equals(idField.getName())))) {
        if (Number.class.isAssignableFrom(idField.getType())) {
          result = Optional.of(indexAsNumericFieldFor(maybeIdField.get(), isDocument, "", true, false));
        } else {
          result = Optional.of(indexAsTagFieldFor(maybeIdField.get(), isDocument, "", false, "|", Integer.MIN_VALUE));
        }
      }
    }
    return result;
  }

  private Optional<Field> createIndexedFieldForReferenceIdField( //
      java.lang.reflect.Field referenceIdField, //
      java.lang.reflect.Field idFieldToIndex, boolean isDocument) {
    Optional<Field> result;

    String fieldPrefix = getFieldPrefix("", isDocument);
    FieldName fieldName = FieldName.of(fieldPrefix + referenceIdField.getName());

    fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(referenceIdField, ""));

    if (Number.class.isAssignableFrom(referenceIdField.getType())) {
      result = Optional.of(new Field(fieldName, FieldType.NUMERIC, true, false));
    } else {
      result = Optional.of(new TagField(fieldName, "|", true));
    }

    return result;
  }

  private IndexDefinition createIndexDefinition(Class<?> cl, IndexDefinition.Type idxType) {
    IndexDefinition index = new IndexDefinition(idxType);

    if (cl.isAnnotationPresent(Document.class)) {
      Document document = cl.getAnnotation(Document.class);
      index.setAsync(document.async());
      Optional.ofNullable(document.filter()).filter(ObjectUtils::isNotEmpty).ifPresent(index::setFilter);
      Optional.ofNullable(document.language()).filter(ObjectUtils::isNotEmpty).ifPresent(lang -> index.setLanguage(lang.getValue()));
      Optional.ofNullable(document.languageField()).filter(ObjectUtils::isNotEmpty).ifPresent(index::setLanguageField);
      index.setScore(document.score());
    }

    return index;
  }

  private void updateTTLSettings(Class<?> cl, String entityPrefix, boolean isDocument, Optional<Document> document, List<java.lang.reflect.Field> allClassFields) {
    if (isDocument) {
      KeyspaceSettings setting = new KeyspaceSettings(cl, entityPrefix);

      // Default TTL
      document.filter(doc -> doc.timeToLive() > 0)
          .ifPresent(doc -> setting.setTimeToLive(doc.timeToLive()));

      allClassFields.stream()
          .filter(field -> field.isAnnotationPresent(TimeToLive.class))
          .findFirst()
          .ifPresent(field -> setting.setTimeToLivePropertyName(field.getName()));

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
}
