package com.redis.om.spring;

import static com.redis.om.spring.util.ObjectUtils.getBeanDefinitionsFor;
import static com.redis.om.spring.util.ObjectUtils.getIdFieldForEntityClass;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration.KeyspaceSettings;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.google.gson.annotations.JsonAdapter;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.DocumentScore;
import com.redis.om.spring.annotations.GeoIndexed;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.annotations.TextIndexed;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.query.QueryUtils;
import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.Schema.Field;
import redis.clients.jedis.search.Schema.FieldType;
import redis.clients.jedis.search.Schema.TagField;
import redis.clients.jedis.search.Schema.TextField;

@Component
public class RediSearchIndexer {
  private final Map<String, Class<?>> keyspaceToEntityClass = new ConcurrentHashMap<>();
  private final Map<Class<?>, String> entityClassToKeySpace = new ConcurrentHashMap<>();
  private final List<Class<?>> indexedEntityClasses = new ArrayList<>();
  private final Map<Class<?>,Schema> entityClassToSchema = new ConcurrentHashMap<>();

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
        createIndexFor(cl);
      } catch (ClassNotFoundException e) {
        logger.warn(
            String.format(SKIPPING_INDEX_CREATION, beanDef.getBeanClassName(), e.getMessage()));
      }
    }
  }

  @SuppressWarnings("deprecation")
  public void createIndexFor(Class<?> cl) {
    IndexDefinition.Type idxType;
    if (cl.isAnnotationPresent(Document.class)) {
      idxType = IndexDefinition.Type.JSON;
    } else if (cl.isAnnotationPresent(RedisHash.class)) {
      idxType = IndexDefinition.Type.HASH;
    } else {
      return;
    }

    String indexName = "";
    String scoreField = null;
    try {
      indexName = cl.getName() + "Idx";
      logger.info(String.format("Found @%s annotated class: %s", idxType, cl.getName()));

      List<Field> fields = new ArrayList<>();

      for (java.lang.reflect.Field field : cl.getDeclaredFields()) {
        fields.addAll(findIndexFields(field, null, idxType == IndexDefinition.Type.JSON));

        // @DocumentScore
        if (field.isAnnotationPresent(DocumentScore.class)) {
          String fieldPrefix = idxType == IndexDefinition.Type.JSON ? "$." : "";
          scoreField = fieldPrefix + field.getName();
        }
      }

      Optional<java.lang.reflect.Field> maybeIdField = getIdFieldForEntityClass(cl);
      if (maybeIdField.isPresent()) {
        java.lang.reflect.Field idField = maybeIdField.get();
        // Only auto-index the @Id if not already indexed by the user (gh-135)
        if (!idField.isAnnotationPresent(Indexed.class) && !idField.isAnnotationPresent(Searchable.class) && (fields.stream().noneMatch(f -> f.name.equals(idField.getName())))) {
          if (Number.class.isAssignableFrom(idField.getType())) {
            fields
                .add(indexAsNumericFieldFor(maybeIdField.get(), idxType == IndexDefinition.Type.JSON, "", true,
                    false));
          } else {
            fields.add(
                indexAsTagFieldFor(maybeIdField.get(), idxType == IndexDefinition.Type.JSON, "", false, "|",
                    Integer.MIN_VALUE));
          }
        }
      }

      String entityPrefix = getEntityPrefix(cl);

      Schema schema = new Schema();
      SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
      for (Field field : fields) {
        schema.addField(field);
      }

      IndexDefinition index = new IndexDefinition(idxType);

      if (cl.isAnnotationPresent(Document.class)) {
        Document document = cl.getAnnotation(Document.class);
        index.setAsync(document.async());
        if (ObjectUtils.isNotEmpty(document.value())) {
          entityPrefix = document.value();
        }
        if (ObjectUtils.isNotEmpty(document.filter())) {
          index.setFilter(document.filter());
        }
        if (ObjectUtils.isNotEmpty(document.language())) {
          index.setLanguage(document.language().getValue());
        }
        if (ObjectUtils.isNotEmpty(document.languageField())) {
          index.setLanguageField(document.languageField());
        }
        index.setScore(document.score());
        if (scoreField != null) {
          index.setScoreFiled(scoreField);
        }
      } else if (cl.isAnnotationPresent(RedisHash.class)) {
        RedisHash hash = cl.getAnnotation(RedisHash.class);
        if (ObjectUtils.isNotEmpty(hash.value())) {
          entityPrefix = hash.value();
        }
      }

      index.setPrefixes(entityPrefix);
      IndexOptions ops = IndexOptions.defaultOptions().setDefinition(index);
      addKeySpaceMapping(entityPrefix, cl);

      // TTL
      if (cl.isAnnotationPresent(Document.class)) {
        KeyspaceSettings setting = new KeyspaceSettings(cl, entityPrefix);

        // Default TTL
        Document document = cl.getAnnotation(Document.class);
        if (document.timeToLive() > 0) {
          setting.setTimeToLive(document.timeToLive());
        }

        for (java.lang.reflect.Field field : cl.getDeclaredFields()) {
          // @TimeToLive
          if (field.isAnnotationPresent(TimeToLive.class)) {
            setting.setTimeToLivePropertyName(field.getName());
          }
        }

        mappingContext.getMappingConfiguration().getKeyspaceConfiguration().addKeyspaceSettings(setting);
      }

      opsForSearch.createIndex(schema, ops);
      entityClassToSchema.put(cl, schema);
    } catch (Exception e) {
      logger.warn(String.format(SKIPPING_INDEX_CREATION, indexName, e.getMessage()));
    }
  }

  public void dropIndexAndDocumentsFor(Class<?> cl) {
    String indexName = "";

    try {
      indexName = cl.getName() + "Idx";
      logger.info(String.format("Dropping index @%s for class: %s", indexName, cl.getName()));
      SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
      opsForSearch.dropIndexAndDocuments();

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

      removeKeySpaceMapping(entityPrefix, cl);
    } catch (Exception e) {
      logger.warn(String.format(SKIPPING_INDEX_CREATION, indexName, e.getMessage()));
    }
  }

  private List<Field> findIndexFields(java.lang.reflect.Field field, String prefix, boolean isDocument) {
    List<Field> fields = new ArrayList<>();

    if (field.isAnnotationPresent(Indexed.class)) {
      logger.info(String.format("Found @Indexed annotation on field of type: %s", field.getType()));

      Indexed indexed = field.getAnnotation(Indexed.class);

      Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(field.getType());

      //
      // Any Character class or Boolean -> Tag Search Field
      //
      if (CharSequence.class.isAssignableFrom(fieldType) || (fieldType == Boolean.class)) {
        fields.add(indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.separator(),
            indexed.arrayIndex()));
      }
      //
      // Any Numeric class -> Numeric Search Field
      //
      else if (Number.class.isAssignableFrom(fieldType) || (fieldType == LocalDateTime.class)
          || (field.getType() == LocalDate.class) || (field.getType() == Date.class)) {
        fields.add(indexAsNumericFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.noindex()));
      }
      //
      // Set / List
      //
      else if (Set.class.isAssignableFrom(fieldType) || List.class.isAssignableFrom(fieldType)) {
        Optional<Class<?>> maybeCollectionType = com.redis.om.spring.util.ObjectUtils.getCollectionElementType(field);

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
              fields.add(indexAsGeoFieldFor(field, true, prefix, indexed.sortable(), indexed.noindex()));
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
        fields.add(indexAsGeoFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.noindex()));
      }
      //
      // Recursively explore the fields for Index annotated fields
      //
      else {
        for (java.lang.reflect.Field subfield : field.getType().getDeclaredFields()) {
          String subfieldPrefix = (prefix == null || prefix.isBlank()) ? field.getName()
              : String.join(".", prefix, field.getName());
          fields.addAll(findIndexFields(subfield, subfieldPrefix, isDocument));
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

  private Field indexAsGeoFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, boolean sortable,
      boolean noIndex) {
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
      java.lang.reflect.Field[] subDeclaredFields = actualTypeArgument.getDeclaredFields();
      String tempPrefix = "";
      if (prefix == null) {
        prefix = field.getName();
      } else {
        prefix += "." + field.getName();
      }
      for (java.lang.reflect.Field subField : subDeclaredFields) {

        Optional<Class<?>> maybeCollectionType = com.redis.om.spring.util.ObjectUtils
            .getCollectionElementType(subField);

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
        }
        getNestedField(fieldPrefix + tempPrefix, subField, prefix, fieldList);
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

  public Optional<String> getIndexName(String keyspace) {
    Class<?> entityClass = keyspaceToEntityClass.get(getKey(keyspace));
    if (entityClass != null) {
      return Optional.of(entityClass.getName() + "Idx");
    } else {
      return Optional.empty();
    }
  }

  public Optional<String> getIndexName(Class<?> entityClass) {
    if (entityClassToKeySpace.containsKey(entityClass)) {
      return Optional.of(entityClass.getName() + "Idx");
    } else {
      return Optional.empty();
    }
  }

  public void addKeySpaceMapping(String keyspace, Class<?> entityClass) {
    String key = getKey(keyspace);
    keyspaceToEntityClass.put(key, entityClass);
    entityClassToKeySpace.put(entityClass, key);
    indexedEntityClasses.add(entityClass);
  }

  public void removeKeySpaceMapping(String keyspace, Class<?> entityClass) {
    String key = getKey(keyspace);
    keyspaceToEntityClass.remove(key);
    entityClassToKeySpace.remove(entityClass);
    indexedEntityClasses.remove(entityClass);
  }

  public Class<?> getEntityClassForKeyspace(String keyspace) {
    return keyspaceToEntityClass.get(getKey(keyspace));
  }

  public String getKeyspaceForEntityClass(Class<?> entityClass) {
    return entityClassToKeySpace.get(entityClass);
  }

  public boolean indexExistsFor(Class<?> entityClass) {
    return indexedEntityClasses.contains(entityClass);
  }

  private String getKey(String keyspace) {
    return keyspace.endsWith(":") ? keyspace : keyspace + ":";
  }

  private String getFieldPrefix(String prefix, boolean isDocument) {
    String chain = (prefix == null || prefix.isBlank()) ? "" : prefix + ".";
    return isDocument ? "$." + chain : chain;
  }
}
