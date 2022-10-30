package com.redis.om.spring;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration.KeyspaceSettings;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.util.ClassUtils;

import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.JsonAdapter;
import com.redis.om.spring.annotations.Bloom;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.DocumentScore;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;
import com.redis.om.spring.annotations.GeoIndexed;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.annotations.TextIndexed;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.pds.BloomOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.EntityStreamImpl;
import com.redis.om.spring.serialization.gson.DateTypeAdapter;
import com.redis.om.spring.serialization.gson.InstantTypeAdapter;
import com.redis.om.spring.serialization.gson.LocalDateTimeTypeAdapter;
import com.redis.om.spring.serialization.gson.LocalDateTypeAdapter;
import com.redis.om.spring.serialization.gson.PointTypeAdapter;
import com.redis.om.spring.serialization.gson.UlidTypeAdapter;
import static com.redis.om.spring.util.ObjectUtils.getIdFieldForEntityClass;

import io.redisearch.FieldName;
import io.redisearch.Schema;
import io.redisearch.Schema.Field;
import io.redisearch.Schema.FieldType;
import io.redisearch.Schema.TagField;
import io.redisearch.Schema.TextField;
import io.redisearch.client.Client;
import io.redisearch.client.Client.IndexOptions;
import io.redisearch.client.IndexDefinition;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RedisProperties.class)
@EnableAspectJAutoProxy
@ComponentScan("com.redis.om.spring.bloom")
@ComponentScan("com.redis.om.spring.autocomplete")
@ComponentScan("com.redis.om.spring.metamodel")
public class RedisModulesConfiguration extends CachingConfigurerSupport {

  private static final Log logger = LogFactory.getLog(RedisModulesConfiguration.class);

  @Bean
  public GsonBuilder gsonBuilder(List<GsonBuilderCustomizer> customizers) {

    GsonBuilder builder = new GsonBuilder();
    // Enable the spring.gson.* configuration in the configuration file
    customizers.forEach((c) -> c.customize(builder));

    builder.registerTypeAdapter(Point.class, PointTypeAdapter.getInstance());
    builder.registerTypeAdapter(Date.class, DateTypeAdapter.getInstance());
    builder.registerTypeAdapter(LocalDate.class, LocalDateTypeAdapter.getInstance());
    builder.registerTypeAdapter(LocalDateTime.class, LocalDateTimeTypeAdapter.getInstance());
    builder.registerTypeAdapter(Ulid.class, UlidTypeAdapter.getInstance());
    builder.registerTypeAdapter(Instant.class, InstantTypeAdapter.getInstance());

    return builder;
  }

  @Bean(name = "redisModulesClient")
  RedisModulesClient redisModulesClient(JedisConnectionFactory jedisConnectionFactory, GsonBuilder builder) {
    return new RedisModulesClient(jedisConnectionFactory, builder);
  }

  @Bean(name = "redisModulesOperations")
  @Primary
  @ConditionalOnMissingBean
  RedisModulesOperations<?> redisModulesOperations(RedisModulesClient rmc, RedisTemplate<?, ?> template) {
    return new RedisModulesOperations<>(rmc, template);
  }

  @Bean(name = "redisJSONOperations")
  JSONOperations<?> redisJSONOperations(RedisModulesOperations<?> redisModulesOperations) {
    return redisModulesOperations.opsForJSON();
  }

  @Bean(name = "redisBloomOperations")
  BloomOperations<?> redisBloomOperations(RedisModulesOperations<?> redisModulesOperations) {
    return redisModulesOperations.opsForBloom();
  }

  @Bean(name = "redisTemplate")
  @Primary
  public RedisTemplate<?, ?> redisTemplate(JedisConnectionFactory connectionFactory) {
    RedisTemplate<?, ?> template = new RedisTemplate<>();
    template.setKeySerializer(new StringRedisSerializer());
    template.setDefaultSerializer(new StringRedisSerializer());
    template.setConnectionFactory(connectionFactory);

    return template;
  }

  @Bean(name = "keyspaceToIndexMap")
  public KeyspaceToIndexMap keyspaceToIndexMap() {
    return new KeyspaceToIndexMap();
  }
  
  @Bean(name = "redisJSONKeyValueAdapter")
  RedisJSONKeyValueAdapter getRedisJSONKeyValueAdapter(RedisOperations<?, ?> redisOps,
      RedisModulesOperations<?> redisModulesOperations, RedisMappingContext mappingContext, KeyspaceToIndexMap keyspaceToIndexMap) {
    return new RedisJSONKeyValueAdapter(redisOps, redisModulesOperations, mappingContext, keyspaceToIndexMap);
  }

  @Bean(name = "redisJSONKeyValueTemplate")
  public CustomRedisKeyValueTemplate getRedisJSONKeyValueTemplate(RedisOperations<?, ?> redisOps,
      RedisModulesOperations<?> redisModulesOperations, RedisMappingContext mappingContext, KeyspaceToIndexMap keyspaceToIndexMap) {
    return new CustomRedisKeyValueTemplate(getRedisJSONKeyValueAdapter(redisOps, redisModulesOperations, mappingContext, keyspaceToIndexMap), mappingContext);
  }

  @Bean(name = "redisCustomKeyValueTemplate")
  public CustomRedisKeyValueTemplate getKeyValueTemplate(RedisOperations<?, ?> redisOps,
      RedisModulesOperations<?> redisModulesOperations, RedisMappingContext mappingContext,
      KeyspaceToIndexMap keyspaceToIndexMap) {
    return new CustomRedisKeyValueTemplate(
        new RedisEnhancedKeyValueAdapter(redisOps, redisModulesOperations, mappingContext, keyspaceToIndexMap),
        mappingContext);
  }

  @Bean(name = "streamingQueryBuilder")
  EntityStream streamingQueryBuilder(RedisModulesOperations<?> redisModulesOperations, Gson gson) {
    return new EntityStreamImpl(redisModulesOperations, gson);
  }

  @EventListener(ContextRefreshedEvent.class)
  public void ensureIndexesAreCreated(ContextRefreshedEvent cre) {
    logger.info("Creating Indexes......");

    ApplicationContext ac = cre.getApplicationContext();
    createIndicesFor(Document.class, ac);
    createIndicesFor(RedisHash.class, ac);
  }

  @EventListener(ContextRefreshedEvent.class)
  public void processBloom(ContextRefreshedEvent cre) {
    ApplicationContext ac = cre.getApplicationContext();
    @SuppressWarnings("unchecked")
    RedisModulesOperations<String> rmo = (RedisModulesOperations<String>) ac.getBean("redisModulesOperations");

    Set<BeanDefinition> beanDefs = getBeanDefinitionsFor(ac, Document.class, RedisHash.class);

    for (BeanDefinition beanDef : beanDefs) {
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        for (java.lang.reflect.Field field : cl.getDeclaredFields()) {
          // Text
          if (field.isAnnotationPresent(Bloom.class)) {
            Bloom bloom = field.getAnnotation(Bloom.class);
            BloomOperations<String> ops = rmo.opsForBloom();
            String filterName = !ObjectUtils.isEmpty(bloom.name()) ? bloom.name()
                : String.format("bf:%s:%s", cl.getSimpleName(), field.getName());
            ops.createFilter(filterName, bloom.capacity(), bloom.errorRate());
          }
        }
      } catch (Exception e) {
        logger.debug("Error during processing of @Bloom annotation: ", e);
      }
    }
  }

  private void createIndicesFor(Class<?> cls, ApplicationContext ac) {
    @SuppressWarnings("unchecked")
    RedisModulesOperations<String> rmo = (RedisModulesOperations<String>) ac.getBean("redisModulesOperations");

    RedisMappingContext mappingContext = (RedisMappingContext) ac.getBean("keyValueMappingContext");
    KeyspaceToIndexMap keyspaceToIndexMap = (KeyspaceToIndexMap) ac.getBean("keyspaceToIndexMap");

    Set<BeanDefinition> beanDefs = new HashSet<>();
    beanDefs.addAll(getBeanDefinitionsFor(ac, cls));

    logger.info(String.format("Found %s @%s annotated Beans...", beanDefs.size(), cls.getSimpleName()));

    for (BeanDefinition beanDef : beanDefs) {
      String indexName = "";
      String scoreField = null;
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        indexName = cl.getName() + "Idx";
        logger.info(String.format("Found @%s annotated class: %s", cls.getSimpleName(), cl.getName()));

        List<Field> fields = new ArrayList<>();

        for (java.lang.reflect.Field field : cl.getDeclaredFields()) {
          fields.addAll(findIndexFields(field, null, cls == Document.class));

          // @DocumentScore
          if (field.isAnnotationPresent(DocumentScore.class)) {
            String fieldPrefix = cls == Document.class ? "$." : "";
            scoreField = fieldPrefix + field.getName();
          }
        }
        
        Optional<java.lang.reflect.Field> maybeIdField = getIdFieldForEntityClass(cl);
        if (maybeIdField.isPresent()) {
          java.lang.reflect.Field idField = maybeIdField.get();
          if (!fields.stream().anyMatch(f -> f.name.equals(idField.getName()))) {
            if (Number.class.isAssignableFrom(idField.getType())) {
              fields.add(indexAsNumericFieldFor(maybeIdField.get(), cls == Document.class, "", true, false));
            } else {
              fields.add(indexAsTagFieldFor(maybeIdField.get(), cls == Document.class, "", false, ",", Integer.MIN_VALUE));
            }
          }
        }

        String entityPrefix = cl.getName() + ":";

        if (!fields.isEmpty()) {
          Schema schema = new Schema();
          SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
          for (Field field : fields) {
            schema.addField(field);
          }

          IndexDefinition index = new IndexDefinition(
              cls == Document.class ? IndexDefinition.Type.JSON : IndexDefinition.Type.HASH);

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
              index.setLanguage(document.language());
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
          IndexOptions ops = Client.IndexOptions.defaultOptions().setDefinition(index);
          opsForSearch.createIndex(schema, ops);
          keyspaceToIndexMap.addKeySpaceMapping(entityPrefix, cl, true);
        } else {
          keyspaceToIndexMap.addKeySpaceMapping(entityPrefix, cl, false);
        }

        // TTL
        if (cl.isAnnotationPresent(Document.class)) {
          KeyspaceSettings setting = new KeyspaceSettings(cl, cl.getName() + ":");

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
      } catch (Exception e) {
        logger.warn(String.format("Skipping index creation for %s because %s", indexName, e.getMessage()));
      }
    }
  }

  private List<Field> findIndexFields(java.lang.reflect.Field field, String prefix, boolean isDocument) {
    List<Field> fields = new ArrayList<>();

    if (field.isAnnotationPresent(Indexed.class)) {
      logger.info(String.format("FOUND @Indexed annotation on field of type: %s", field.getType()));

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

        // It is only possible to index an array of strings or booleans in a TAG
        // identifier.
        // https://redis.io/docs/stack/json/indexing_json/#json-arrays-can-only-be-indexed-in-tag-identifiers
        if (maybeCollectionType.isPresent()) {
          Class<?> collectionType = maybeCollectionType.get();

          if (CharSequence.class.isAssignableFrom(collectionType) || (collectionType == Boolean.class)) {
            fields.add(indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(), indexed.separator(),
                indexed.arrayIndex()));
            // Index nested fields
          } else if (isDocument) {
            // Index nested JSON Array field. But the current implementation of RediSearch
            // supports array only for TAG fields, not TEXT fields.
            // https://github.com/RediSearch/RediSearch/issues/2293
            logger.debug(String.format("FOUND nested field on field of type: %s", field.getType()));
            fields.addAll(indexAsNestedFieldFor(field, prefix));
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
          fields.addAll(findIndexFields(subfield, field.getName(), isDocument));
        }
      }
    }

    // Searchable - behaves like Text indexed
    else if (field.isAnnotationPresent(Searchable.class)) {
      logger.info(String.format("FOUND @Searchable annotation on field of type: %s", field.getType()));
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

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Set<BeanDefinition> getBeanDefinitionsFor(ApplicationContext ac, Class... classes) {
    Map<String, Object> annotatedBeans = ac.getBeansWithAnnotation(SpringBootApplication.class);
    Class<?> app = annotatedBeans.values().toArray()[0].getClass();
    Set<BeanDefinition> beanDefs = new HashSet<>();

    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
    for (Class cls : classes) {
      provider.addIncludeFilter(new AnnotationTypeFilter(cls));
    }

    if (app.isAnnotationPresent(EnableRedisDocumentRepositories.class)) {
      EnableRedisDocumentRepositories edr = app.getAnnotation(EnableRedisDocumentRepositories.class);
      if (edr.basePackages().length > 0) {
        for (String pkg : edr.basePackages()) {
          beanDefs.addAll(provider.findCandidateComponents(pkg));
        }
      } else if (edr.basePackageClasses().length > 0) {
        for (Class<?> pkg : edr.basePackageClasses()) {
          beanDefs.addAll(provider.findCandidateComponents(pkg.getPackageName()));
        }
      } else {
        beanDefs.addAll(provider.findCandidateComponents(app.getPackageName()));
      }
    }

    if (app.isAnnotationPresent(EnableRedisEnhancedRepositories.class)) {
      EnableRedisEnhancedRepositories er = app.getAnnotation(EnableRedisEnhancedRepositories.class);
      if (er.basePackages().length > 0) {
        for (String pkg : er.basePackages()) {
          beanDefs.addAll(provider.findCandidateComponents(pkg));
        }
      } else if (er.basePackageClasses().length > 0) {
        for (Class<?> pkg : er.basePackageClasses()) {
          beanDefs.addAll(provider.findCandidateComponents(pkg.getPackageName()));
        }
      } else {
        beanDefs.addAll(provider.findCandidateComponents(app.getPackageName()));
      }
    }

    return beanDefs;
  }

  private Field indexAsTagFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, TagIndexed ti) {
    ClassTypeInformation<?> typeInfo = ClassTypeInformation.from(field.getType());
    String chain = (prefix == null || prefix.isBlank()) ? "" : prefix + ".";
    String fieldPrefix = isDocument ? "$." + chain : chain;
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
    ClassTypeInformation<?> typeInfo = ClassTypeInformation.from(field.getType());
    String chain = (prefix == null || prefix.isBlank()) ? "" : prefix + ".";
    String fieldPrefix = isDocument ? "$." + chain : chain;
    String index = (arrayIndex != Integer.MIN_VALUE) ? ".[" + arrayIndex + "]" : "[*]";
    String fieldPostfix = (isDocument && typeInfo.isCollectionLike() && !field.isAnnotationPresent(JsonAdapter.class))
        ? index
        : "";
    FieldName fieldName = FieldName.of(fieldPrefix + field.getName() + fieldPostfix);

    fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));

    return new TagField(fieldName, separator.isBlank() ? null : separator, sortable);
  }

  private Field indexAsTextFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, TextIndexed ti) {
    String chain = (prefix == null || prefix.isBlank()) ? "" : prefix + ".";
    String fieldPrefix = isDocument ? "$." + chain : chain;
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
    String chain = (prefix == null || prefix.isBlank()) ? "" : prefix + ".";
    String fieldPrefix = isDocument ? "$." + chain : chain;
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
    String chain = (prefix == null || prefix.isBlank()) ? "" : prefix + ".";
    String fieldPrefix = isDocument ? "$." + chain : chain;
    FieldName fieldName = FieldName.of(fieldPrefix + field.getName());

    if (!ObjectUtils.isEmpty(gi.alias())) {
      fieldName = fieldName.as(gi.alias());
    } else {
      fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));
    }

    return new Field(fieldName, FieldType.Geo);
  }

  private Field indexAsNumericFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      NumericIndexed ni) {
    String chain = (prefix == null || prefix.isBlank()) ? "" : prefix + ".";
    String fieldPrefix = isDocument ? "$." + chain : chain;
    FieldName fieldName = FieldName.of(fieldPrefix + field.getName());

    if (!ObjectUtils.isEmpty(ni.alias())) {
      fieldName = fieldName.as(ni.alias());
    } else {
      fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));
    }

    return new Field(fieldName, FieldType.Numeric);
  }

  private Field indexAsNumericFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      boolean sortable, boolean noIndex) {
    String chain = (prefix == null || prefix.isBlank()) ? "" : prefix + ".";
    String fieldPrefix = isDocument ? "$." + chain : chain;
    FieldName fieldName = FieldName.of(fieldPrefix + field.getName());

    fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));

    return new Field(fieldName, FieldType.Numeric, sortable, noIndex);
  }

  private Field indexAsGeoFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, boolean sortable,
      boolean noIndex) {
    String chain = (prefix == null || prefix.isBlank()) ? "" : prefix + ".";
    String fieldPrefix = isDocument ? "$." + chain : chain;
    FieldName fieldName = FieldName.of(fieldPrefix + field.getName());

    fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(field, prefix));

    return new Field(fieldName, FieldType.Geo);
  }

  private List<Field> indexAsNestedFieldFor(java.lang.reflect.Field field, String prefix) {
    String chain = (prefix == null || prefix.isBlank()) ? "" : prefix + ".";
    String fieldPrefix = "$." + chain;
    return getNestedField(fieldPrefix, field, prefix, null);
  }

  private List<Field> getNestedField(String fieldPrefix, java.lang.reflect.Field field, String prefix,
      List<Field> fieldList) {
    if (fieldList == null) {
      fieldList = new ArrayList<>();
    }
    Type genericType = field.getGenericType();
    if (genericType instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) genericType;
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
          boolean subFieldIsTagField = ((subField.isAnnotationPresent(Indexed.class)
              && ((CharSequence.class.isAssignableFrom(subField.getType()) || (subField.getType() == Boolean.class)
                  || (maybeCollectionType.isPresent() && (CharSequence.class.isAssignableFrom(maybeCollectionType.get())
                      || (maybeCollectionType.get() == Boolean.class)))))));
          if (subFieldIsTagField) {
            Indexed indexed = subField.getAnnotation(Indexed.class);
            tempPrefix = field.getName() + "[0:].";

            FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName());
            fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(subField, prefix));

            logger.info(String.format("Creating nested relationships: %s -> %s", field.getName(), subField.getName()));
            fieldList.add(new TagField(fieldName, indexed.separator(), false));
            continue;
          }
        }
        getNestedField(fieldPrefix + tempPrefix, subField, prefix, fieldList);
      }
    }
    return fieldList;
  }

}
