package com.redis.om.spring;

import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.JsonAdapter;
import com.redis.om.spring.annotations.*;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.serialization.gson.EnumTypeAdapter;
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
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.schemafields.*;

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
  private final Map<Class<?>, List<SchemaField>> entityClassToSchema = new ConcurrentHashMap<>();

  private static final Log logger = LogFactory.getLog(RediSearchIndexer.class);

  private final ApplicationContext ac;
  private final RedisModulesOperations<String> rmo;
  private final RedisMappingContext mappingContext;
  private final GsonBuilder gsonBuilder;

  private static final String SKIPPING_INDEX_CREATION = "Skipping index creation for %s because %s";

  @SuppressWarnings("unchecked")
  public RediSearchIndexer(ApplicationContext ac, GsonBuilder gsonBuilder) {
    this.ac = ac;
    rmo = (RedisModulesOperations<String>) ac.getBean("redisModulesOperations");
    mappingContext = (RedisMappingContext) ac.getBean("keyValueMappingContext");
    this.gsonBuilder = gsonBuilder;
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

    String indexName = "";
    Optional<String> maybeScoreField;
    try {
      indexName = cl.getName() + "Idx";
      logger.info(String.format("Found @%s annotated class: %s", idxType, cl.getName()));

      final List<java.lang.reflect.Field> allClassFields = getDeclaredFieldsTransitively(cl);

      List<SchemaField> fields = processIndexedFields(allClassFields, isDocument);
      maybeScoreField = getDocumentScoreField(allClassFields, isDocument);
      createIndexedFieldForIdField(cl, fields, isDocument).ifPresent(fields::add);

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
      entityClassToSchema.put(cl, fields);
      opsForSearch.createIndex(params, fields);
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

  public List<SchemaField> getSchemaFor(Class<?> entityClass) {
    return entityClassToSchema.get(entityClass);
  }

  private List<SchemaField> findIndexFields(java.lang.reflect.Field field, String prefix, boolean isDocument) {
    List<SchemaField> fields = new ArrayList<>();

    if (field.isAnnotationPresent(Indexed.class)) {
      logger.info(String.format("Found @Indexed annotation on field of type: %s", field.getType()));

      Indexed indexed = field.getAnnotation(Indexed.class);

      Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(field.getType());

      if (field.isAnnotationPresent(Reference.class)) {
        //
        // @Reference @Indexed fields: Create schema field for the reference entity @Id field
        //
        logger.debug("🪲Found @Reference field " + field.getName() + " in " + field.getDeclaringClass().getSimpleName());
        createIndexedFieldForReferenceIdField(field, isDocument).ifPresent(fields::add);
      } else if (indexed.schemaFieldType() == SchemaFieldType.AUTODETECT) {
        //
        // Any Character class, Boolean or Enum with AUTODETECT -> Tag Search Field
        // Also UUID and Ulid (classes whose toString() is a valid text representation of the value)
        //
        if (CharSequence.class.isAssignableFrom(fieldType) || (fieldType == Boolean.class) || (fieldType == UUID.class) || (fieldType == Ulid.class)) {
          fields.add(indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.separator(),
            indexed.arrayIndex(), indexed.alias()));
        } else if (fieldType.isEnum()) {
          if (Objects.requireNonNull(indexed.serializationHint()) == SerializationHint.ORDINAL) {
            fields.add(indexAsNumericFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.noindex(),
              indexed.alias()));
            gsonBuilder.registerTypeAdapter(fieldType, EnumTypeAdapter.of(fieldType));
          } else {
            fields.add(indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.separator(),
              indexed.arrayIndex(), indexed.alias()));
          }
        }
        //
        // Any Numeric class -> Numeric Search Field
        //
        else if (Number.class.isAssignableFrom(fieldType) || (fieldType == LocalDateTime.class)
          || (field.getType() == LocalDate.class) || (field.getType() == Date.class)
          || (field.getType() == Instant.class) || (field.getType() == OffsetDateTime.class)) {
          fields.add(indexAsNumericFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.noindex(), indexed.alias()));
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
                indexed.arrayIndex(), indexed.alias()));
            } else if (isDocument) {
              if (Number.class.isAssignableFrom(collectionType)) {
                fields.add(indexAsNumericFieldFor(field, true, prefix, indexed.sortable(), indexed.noindex(), indexed.alias()));
              } else if (collectionType == Point.class) {
                fields.add(indexAsGeoFieldFor(field, true, prefix, indexed.alias()));
              } else if (collectionType == UUID.class || collectionType == Ulid.class) {
                fields.add(indexAsTagFieldFor(field, true, prefix, indexed.sortable(), indexed.separator(), 0, indexed.alias()));
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
          fields.add(indexAsGeoFieldFor(field, isDocument, prefix, indexed.alias()));
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
              indexed.arrayIndex(), indexed.alias()));
          case NUMERIC ->
            fields.add(indexAsNumericFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.noindex(), indexed.alias()));
          case GEO -> fields.add(indexAsGeoFieldFor(field, true, prefix, indexed.alias()));
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

  private TagField indexAsTagFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, TagIndexed ti) {
    TypeInformation<?> typeInfo = TypeInformation.of(field.getType());
    String fieldPrefix = getFieldPrefix(prefix, isDocument);

    String fieldPostfix = (isDocument && typeInfo.isCollectionLike() && !field.isAnnotationPresent(JsonAdapter.class))
      ? "[*]"
      : "";
    String name = fieldPrefix + field.getName() + fieldPostfix;
    String alias = ObjectUtils.isEmpty(ti.alias()) ? QueryUtils.searchIndexFieldAliasFor(field, prefix) : ti.alias();

    FieldName fieldName = FieldName.of(name);
    fieldName = fieldName.as(alias);

    return getTagField(fieldName, ti.separator(), false);
  }

  private VectorField indexAsVectorFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
    Indexed indexed) {
    TypeInformation<?> typeInfo = TypeInformation.of(field.getType());
    String fieldPrefix = getFieldPrefix(prefix, isDocument);
    String fieldName = fieldPrefix + field.getName();

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

    String alias = ObjectUtils.isEmpty(indexed.alias()) ? QueryUtils.searchIndexFieldAliasFor(field, prefix) : indexed.alias();

    VectorField vectorField = new VectorField(fieldName, indexed.algorithm(), attributes);
    vectorField.as(alias);

    return vectorField;
  }

  private VectorField indexAsVectorFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
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

    String alias = ObjectUtils.isEmpty(vi.alias()) ? QueryUtils.searchIndexFieldAliasFor(field, prefix) : vi.alias();

    VectorField vectorField = new VectorField(fieldName, vi.algorithm(), attributes);
    vectorField.as(alias);

    return vectorField;
  }

  private SchemaField indexAsTagFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, boolean sortable,
    String separator, int arrayIndex, String annotationAlias) {
    TypeInformation<?> typeInfo = TypeInformation.of(field.getType());
    String fieldPrefix = getFieldPrefix(prefix, isDocument);
    String index = (arrayIndex != Integer.MIN_VALUE) ? ".[" + arrayIndex + "]" : "[*]";
    String fieldPostfix = (isDocument && typeInfo.isCollectionLike() && !field.isAnnotationPresent(JsonAdapter.class))
      ? index
      : "";
    String name = fieldPrefix + field.getName() + fieldPostfix;
    String alias = (annotationAlias == null || annotationAlias.isBlank()) ? QueryUtils.searchIndexFieldAliasFor(field, prefix) : annotationAlias;
    FieldName fieldName = FieldName.of(name);
    fieldName = fieldName.as(alias);

    return getTagField(fieldName, separator, sortable);
  }

  private TextField indexAsTextFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, TextIndexed ti) {
    var fieldName = getFieldName(field, isDocument, prefix, ObjectUtils.isEmpty(ti.alias())
      ? QueryUtils.searchIndexFieldAliasFor(field, prefix) : ti.alias());

    String phonetic = ObjectUtils.isEmpty(ti.phonetic()) ? null : ti.phonetic();

    return getTextField(fieldName, ti.weight(), ti.sortable(), ti.nostem(), ti.noindex(), phonetic);
  }

  private TextField indexAsTextFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, Searchable ti) {
    var fieldName = getFieldName(field, isDocument, prefix,  ObjectUtils.isEmpty(ti.alias())
      ? QueryUtils.searchIndexFieldAliasFor(field, prefix) : ti.alias());

    String phonetic = ObjectUtils.isEmpty(ti.phonetic()) ? null : ti.phonetic();

    return getTextField(fieldName, ti.weight(), ti.sortable(), ti.nostem(), ti.noindex(), phonetic);
  }

  private GeoField indexAsGeoFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, GeoIndexed gi) {
    var fieldName = getFieldName(field, isDocument, prefix, ObjectUtils.isEmpty(gi.alias())
      ? QueryUtils.searchIndexFieldAliasFor(field, prefix) : gi.alias());

    return GeoField.of(fieldName);
  }

  private NumericField indexAsNumericFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
    NumericIndexed ni) {
    var fieldName = getFieldName(field, isDocument, prefix, ObjectUtils.isEmpty(ni.alias())
      ? QueryUtils.searchIndexFieldAliasFor(field, prefix) : ni.alias());

    return NumericField.of(fieldName);
  }

  private NumericField indexAsNumericFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
    boolean sortable, boolean noIndex, String annotationAlias) {
    String alias = (annotationAlias == null || annotationAlias.isBlank()) ? QueryUtils.searchIndexFieldAliasFor(field, prefix) : annotationAlias;
    var fieldName = getFieldName(field, isDocument, prefix, alias);

    NumericField num = NumericField.of(fieldName);
    if (sortable) num.sortable();
    if (noIndex) num.noIndex();
    return num;
  }

  private GeoField indexAsGeoFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, String annotationAlias) {
    String alias = (annotationAlias == null || annotationAlias.isBlank()) ? QueryUtils.searchIndexFieldAliasFor(field, prefix) : annotationAlias;
    var fieldName = getFieldName(field, isDocument, prefix, alias);

    return GeoField.of(fieldName);
  }

  private List<SchemaField> indexAsNestedFieldFor(java.lang.reflect.Field field, String prefix) {
    String fieldPrefix = getFieldPrefix(prefix, true);
    return getNestedField(fieldPrefix, field, prefix, null);
  }

  private FieldName getFieldName(java.lang.reflect.Field field, boolean isDocument, String prefix,
    String alias) {
    String fieldPrefix = getFieldPrefix(prefix, isDocument);
    String name = fieldPrefix + field.getName();
    FieldName fieldName = FieldName.of(name);
    fieldName = fieldName.as(alias);
    return fieldName;
  }

  private List<SchemaField> getNestedField(String fieldPrefix, java.lang.reflect.Field field, String prefix,
    List<SchemaField> fieldList) {
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

        String suffix = (maybeCollectionType.isPresent() && (CharSequence.class.isAssignableFrom(maybeCollectionType.get())
          || (maybeCollectionType.get() == Boolean.class))) ? "[*]" : "";

        if (subField.isAnnotationPresent(TagIndexed.class)) {
          TagIndexed ti = subField.getAnnotation(TagIndexed.class);
          tempPrefix = field.getName() + "[0:].";

          FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName() + suffix);
          fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(subField, prefix));

          logger.info(String.format("Creating nested relationships: %s -> %s", field.getName(), subField.getName()));
          fieldList.add(getTagField(fieldName, ti.separator(), false));
          continue;
        } else if (subField.isAnnotationPresent(Indexed.class)) {
          boolean subFieldIsTagField = (subField.isAnnotationPresent(Indexed.class)
            && (CharSequence.class.isAssignableFrom(subField.getType())
            || (subField.getType() == Boolean.class)
            || (subField.getType() == UUID.class)
            || (maybeCollectionType.isPresent() && (CharSequence.class.isAssignableFrom(maybeCollectionType.get())
            || (maybeCollectionType.get() == Boolean.class)))));
          if (subFieldIsTagField) {
            Indexed indexed = subField.getAnnotation(Indexed.class);
            tempPrefix = field.getName() + "[0:].";

            FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName() + suffix);
            fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(subField, prefix));

            logger.info(String.format("Creating nested relationships: %s -> %s", field.getName(), subField.getName()));
            fieldList.add(getTagField(fieldName, indexed.separator(), false));
            continue;
          }

          else if (Number.class.isAssignableFrom(subField.getType()) || (subField.getType() == LocalDateTime.class)
            || (subField.getType() == LocalDate.class) || (subField.getType() == Date.class)) {

            FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName() + suffix);
            fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(subField, prefix));
            logger.info(String.format("Creating nested relationships: %s -> %s", field.getName(), subField.getName()));
            fieldList.add(NumericField.of(fieldName));
          }
        } else if (subField.isAnnotationPresent(Searchable.class)) {
          Searchable searchable = subField.getAnnotation(Searchable.class);
          tempPrefix = field.getName() + "[0:].";

          FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName() + suffix);
          fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(subField, prefix));

          logger
            .info(String.format("Creating TEXT nested relationships: %s -> %s", field.getName(), subField.getName()));

          String phonetic = ObjectUtils.isEmpty(searchable.phonetic()) ? null : searchable.phonetic();

          fieldList.add(getTextField(fieldName, searchable.weight(), searchable.sortable(), searchable.nostem(),
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

  private TagField getTagField(FieldName fieldName, String separator, boolean sortable) {
    TagField tag = TagField.of(fieldName);
    if (separator != null) {
      if (separator.length() != 1) {
        throw new IllegalArgumentException("Separator '" + separator + "' is not of length 1.");
      }
      tag.separator(separator.charAt(0));
    }
    if (sortable) tag.sortable();
    return tag;
  }

  private TextField getTextField(FieldName fieldName, double weight,
    boolean sortable, boolean noStem, boolean noIndex,
    String phonetic) {
    TextField text = TextField.of(fieldName);
    text.weight(weight);
    if (sortable) text.sortable();
    if (noStem) text.noStem();
    if (noIndex) text.noIndex();
    if (phonetic != null) text.phonetic(phonetic);
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

  private Optional<IndexDataType> determineIndexTarget(Class<?> cl) {
    if (cl.isAnnotationPresent(Document.class)) {
      return Optional.of(IndexDataType.JSON);
    } else if (cl.isAnnotationPresent(RedisHash.class)) {
      return Optional.of(IndexDataType.HASH);
    } else {
      return Optional.empty();
    }
  }

  private List<SchemaField> processIndexedFields(List<java.lang.reflect.Field> allClassFields, boolean isDocument) {
    List<SchemaField> fields = new ArrayList<>();
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

  private boolean isAnnotationPreset(java.lang.reflect.Field idField, List<SchemaField> fields){
    return (!idField.isAnnotationPresent(Indexed.class)
            && !idField.isAnnotationPresent(Searchable.class)
            && !idField.isAnnotationPresent(TagIndexed.class)
            && !idField.isAnnotationPresent(TextIndexed.class)
            && (fields.stream().noneMatch(f -> f.getName().equals(idField.getName()))));
  }

  private Optional<SchemaField> createIndexedFieldForIdField(Class<?> cl, List<SchemaField> fields, boolean isDocument) {
    Optional<SchemaField> result = Optional.empty();
    Optional<java.lang.reflect.Field> maybeIdField = getIdFieldForEntityClass(cl);
    if (maybeIdField.isPresent()) {
      java.lang.reflect.Field idField = maybeIdField.get();
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

        //TODO: determine if we need to pass the alias
        if (Number.class.isAssignableFrom(idClass)) {
          result = Optional.of(indexAsNumericFieldFor(maybeIdField.get(), isDocument, "", true, false, null));
        } else {
          result = Optional.of(indexAsTagFieldFor(maybeIdField.get(), isDocument, "", false, "|", Integer.MIN_VALUE, null));
        }
      }
    }
    return result;
  }

  private Optional<SchemaField> createIndexedFieldForReferenceIdField( //
    java.lang.reflect.Field referenceIdField, //
    boolean isDocument) {
    Optional<SchemaField> result;

    String fieldPrefix = getFieldPrefix("", isDocument);
    FieldName fieldName = FieldName.of(fieldPrefix + referenceIdField.getName());

    fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(referenceIdField, ""));

    Class<?> refClass = referenceIdField.getType();
    Optional<java.lang.reflect.Field> maybeIdField = getIdFieldForEntityClass(refClass);

    if (maybeIdField.isPresent()) {
      java.lang.reflect.Field idField = maybeIdField.get();
      Class<?> idClass = idField.getType();
      if (idField.getType().isPrimitive()) {
        String cls = com.redis.om.spring.util.ObjectUtils.getTargetClassName(idClass.getName());
        Class<?> primitive = ClassUtils.resolvePrimitiveClassName(cls);
        if (primitive != null) {
          idClass = ClassUtils.resolvePrimitiveIfNecessary(primitive);
        }
      }

      if (Number.class.isAssignableFrom(idClass)) {
        result = Optional.of(isDocument ? NumericField.of(fieldName) : NumericField.of(fieldName).sortable());
      } else {
        result = Optional.of(isDocument ? TagField.of(fieldName).separator('|') : TagField.of(fieldName).separator('|').sortable());
      }
    } else {
      result = Optional.of(isDocument ? TagField.of(fieldName).separator('|') : TagField.of(fieldName).separator('|').sortable());
    }

    return result;
  }

  private FTCreateParams createIndexDefinition(Class<?> cl, IndexDataType idxType) {
    FTCreateParams params = FTCreateParams.createParams();
    params.on(idxType);

    if (cl.isAnnotationPresent(Document.class)) {
      Document document = cl.getAnnotation(Document.class);
      Optional.ofNullable(document.filter()).filter(ObjectUtils::isNotEmpty).ifPresent(params::filter);
      Optional.ofNullable(document.language()).filter(ObjectUtils::isNotEmpty).ifPresent(lang -> params.language(lang.getValue()));
      Optional.ofNullable(document.languageField()).filter(ObjectUtils::isNotEmpty).ifPresent(params::languageField);
      params.score(document.score());
    }

    return params;
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