package com.redis.om.spring.repository.support;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.RediSearchIndexer;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.id.ULIDIdentifierGenerator;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.EntityStreamImpl;
import com.redis.om.spring.search.stream.FluentQueryByExample;
import com.redis.om.spring.serialization.gson.GsonListOfType;
import com.redis.om.spring.util.ObjectUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Reference;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.*;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.util.SafeEncoder;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.redis.om.spring.util.ObjectUtils.isPrimitiveOfType;
import static com.redis.om.spring.util.ObjectUtils.pageFromSlice;
import static redis.clients.jedis.json.JsonProtocol.JsonCommand;

public class SimpleRedisDocumentRepository<T, ID> extends SimpleKeyValueRepository<T, ID>
    implements RedisDocumentRepository<T, ID> {

  private final GsonBuilder gsonBuilder;
  protected final RedisModulesOperations<String> modulesOperations;
  protected final EntityInformation<T, ID> metadata;
  protected final KeyValueOperations operations;
  protected final RediSearchIndexer indexer;
  protected final MappingRedisOMConverter mappingConverter;
  private final ULIDIdentifierGenerator generator;
  private final RedisOMProperties properties;
  private final RedisMappingContext mappingContext;
  private final EntityStream entityStream;

  @SuppressWarnings("unchecked")
  public SimpleRedisDocumentRepository( //
      EntityInformation<T, ID> metadata, //
      KeyValueOperations operations, //
      @Qualifier("redisModulesOperations") RedisModulesOperations<?> rmo, //
      RediSearchIndexer indexer, //
      RedisMappingContext mappingContext,
      GsonBuilder gsonBuilder,
      RedisOMProperties properties) {
    super(metadata, operations);
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.metadata = metadata;
    this.operations = operations;
    this.indexer = indexer;
    this.mappingConverter = new MappingRedisOMConverter(null,
        new ReferenceResolverImpl(modulesOperations.getTemplate()));
    this.generator = ULIDIdentifierGenerator.INSTANCE;
    this.gsonBuilder = gsonBuilder;
    this.mappingContext = mappingContext;
    this.properties = properties;
    this.entityStream = new EntityStreamImpl(modulesOperations, modulesOperations.getGsonBuilder(), indexer);
  }

  @Override
  public Iterable<ID> getIds() {
    List<ID> result = List.of();
    Gson gson = gsonBuilder.create();
    Optional<Field> maybeIdField = ObjectUtils.getIdFieldForEntityClass(metadata.getJavaType());
    String idField = maybeIdField.map(Field::getName).orElse("id");

    Query query = new Query("*");
    query.limit(0, properties.getRepository().getQuery().getLimit());
    query.returnFields(idField);
    SearchResult searchResult = getSearchOps().search(query);

    return searchResult.getDocuments().stream()
        .map(d -> gson.fromJson(SafeEncoder.encode((byte[])d.get(idField)), metadata.getIdType()))
        .toList();
  }

  @Override
  public Page<ID> getIds(Pageable pageable) {
    List<ID> ids = Lists.newArrayList(getIds());

    int fromIndex = Math.toIntExact(pageable.getOffset());
    int toIndex = fromIndex + pageable.getPageSize();

    return new PageImpl<>(ids.subList(fromIndex, toIndex), pageable, ids.size());
  }

  @Override
  public void deleteById(ID id, Path path) {
    modulesOperations.opsForJSON().del(getKey(id), path);
  }

  @Override
  public void updateField(T entity, MetamodelField<T, ?> field, Object value) {
    modulesOperations.opsForJSON().set(getKey(Objects.requireNonNull(metadata.getId(entity))), value,
        Path.of("$." + field.getSearchAlias()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <F> Iterable<F> getFieldsByIds(Iterable<ID> ids, MetamodelField<T, F> field) {
    String[] keys = StreamSupport.stream(ids.spliterator(), false).map(this::getKey).toArray(String[]::new);
    return (Iterable<F>) modulesOperations.opsForJSON()
        .mget(Path2.of("$." + field.getSearchAlias()), List.class, keys).stream().flatMap(List::stream)
        .toList();
  }

  @Override
  public Long getExpiration(ID id) {
    RedisTemplate<String, String> template = modulesOperations.getTemplate();
    return template.getExpire(getKey(id));
  }

  @Override
  public <S extends T> List<S> saveAll(Iterable<S> entities) {
    Assert.notNull(entities, "The given Iterable of entities must not be null!");
    List<S> saved = new ArrayList<>();

    try (Jedis jedis = modulesOperations.getClient().getJedis().get()) {
      Pipeline pipeline = jedis.pipelined();
      Gson gson = gsonBuilder.create();
      for (S entity : entities) {
        boolean isNew = metadata.isNew(entity);

        KeyValuePersistentEntity<?, ?> keyValueEntity = mappingConverter.getMappingContext()
            .getRequiredPersistentEntity(ClassUtils.getUserClass(entity));
        Object id = isNew ? generator.generateIdentifierOfType(Objects.requireNonNull(keyValueEntity.getIdProperty()).getTypeInformation())
            : keyValueEntity.getPropertyAccessor(entity).getProperty(
            Objects.requireNonNull(keyValueEntity.getIdProperty()));
        keyValueEntity.getPropertyAccessor(entity).setProperty(keyValueEntity.getIdProperty(), id);

        String keyspace = keyValueEntity.getKeySpace();
        byte[] objectKey = createKey(keyspace, Objects.requireNonNull(id).toString());

        processAuditAnnotations(entity, isNew);

        Optional<Long> maybeTtl = getTTLForEntity(entity);

        RedisData rdo = new RedisData();
        mappingConverter.write(entity, rdo);

        List<byte[]> args = new ArrayList<>(4);
        args.add(objectKey);
        args.add(SafeEncoder.encode(Path.ROOT_PATH.toString()));
        args.add(SafeEncoder.encode(gson.toJson(entity)));
        pipeline.sendCommand(JsonCommand.SET, args.toArray(new byte[args.size()][]));

        processReferenceAnnotations(objectKey, entity, pipeline);

        maybeTtl.ifPresent(aLong -> pipeline.expire(objectKey, aLong));

        saved.add(entity);
      }
      pipeline.sync();
    }

    return saved;
  }

  @Override public Iterable<T> bulkLoad(String file) throws IOException {
    try (Reader reader = Files.newBufferedReader(Paths.get(file))) {
      Gson gson = gsonBuilder.create();
      List<T> entities = gson.fromJson(reader, new GsonListOfType<>(metadata.getJavaType()));
      return saveAll(entities);
    }
  }

  @Override
  public <S extends T> S update(S entity) {
    return this.operations.update(this.metadata.getRequiredId(entity), entity);
  }

  public void delete(T entity) {
    Assert.notNull(entity, "The given entity must not be null");
    checkVersion(entity);
    this.operations.delete(entity);
  }

  @Override
  public  List<T> findAllById(Iterable<ID> ids) {
    String[] keys = StreamSupport.stream(ids.spliterator(), false).map(this::getKey).toArray(String[]::new);

    return modulesOperations.opsForJSON()
        .mget(metadata.getJavaType(), keys).stream()
        .toList();
  }

  private String getKeyspace() {
    return indexer.getKeyspaceForEntityClass(metadata.getJavaType());
  }

  private String getKey(Object id) {
    return getKeyspace() + id.toString();
  }

  public byte[] createKey(String keyspace, String id) {
    return this.mappingConverter.toBytes(keyspace + ":" + id);
  }

  private void processAuditAnnotations(Object item, boolean isNew) {
    var auditClass = isNew ? CreatedDate.class : LastModifiedDate.class;

    List<Field> fields = com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(item.getClass(), auditClass);
    if (!fields.isEmpty()) {
      PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(item);
      fields.forEach(f -> {
        if (f.getType() == Date.class) {
          accessor.setPropertyValue(f.getName(), new Date(System.currentTimeMillis()));
        } else if (f.getType() == LocalDateTime.class) {
          accessor.setPropertyValue(f.getName(), LocalDateTime.now());
        } else if (f.getType() == LocalDate.class) {
          accessor.setPropertyValue(f.getName(), LocalDate.now());
        }
      });
    }
  }

  private void processReferenceAnnotations(byte[] objectKey, Object entity, Pipeline pipeline) {
    List<Field> fields = com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(entity.getClass(), Reference.class);
    if (!fields.isEmpty()) {
      PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(entity);
      fields.forEach(f -> {
        var referencedValue = accessor.getPropertyValue(f.getName());
        if (referencedValue != null) {
          Gson gson = gsonBuilder.create();
          if (referencedValue instanceof Collection<?> referenceValues) {
            List<String> referenceKeys = new ArrayList<>();
            referenceValues.forEach(r -> {
              Object id = ObjectUtils.getIdFieldForEntity(r);
              if (id != null) {
                String referenceKey = indexer.getKeyspaceForEntityClass(r.getClass()) + id;
                referenceKeys.add(referenceKey);
              }
            });

            List<byte[]> args = new ArrayList<>(4);
            args.add(objectKey);
            args.add(SafeEncoder.encode(Path.of("$." + f.getName()).toString()));
            args.add(SafeEncoder.encode(gson.toJson(referenceKeys)));
            pipeline.sendCommand(JsonCommand.SET, args.toArray(new byte[args.size()][]));

          } else {
            Object id = ObjectUtils.getIdFieldForEntity(referencedValue);
            if (id != null) {
              String referenceKey = indexer.getKeyspaceForEntityClass(f.getType()) + id;

              List<byte[]> args = new ArrayList<>(4);
              args.add(objectKey);
              args.add(SafeEncoder.encode(Path.of("$." + f.getName()).toString()));
              args.add(SafeEncoder.encode(gson.toJson(referenceKey)));
              pipeline.sendCommand(JsonCommand.SET, args.toArray(new byte[args.size()][]));
            }
          }
        }
      });
    }
  }

  private Optional<Long> getTTLForEntity(Object entity) {
    KeyspaceConfiguration keyspaceConfig = mappingContext.getMappingConfiguration().getKeyspaceConfiguration();
    if (keyspaceConfig.hasSettingsFor(entity.getClass())) {
      var settings = keyspaceConfig.getKeyspaceSettings(entity.getClass());

      if (org.springframework.util.StringUtils.hasText(settings.getTimeToLivePropertyName())) {
        Method ttlGetter;
        try {
          Field fld = ReflectionUtils.findField(entity.getClass(), settings.getTimeToLivePropertyName());
          ttlGetter = ObjectUtils.getGetterForField(entity.getClass(), Objects.requireNonNull(fld));
          long ttlPropertyValue = ((Number) Objects.requireNonNull(ReflectionUtils.invokeMethod(ttlGetter, entity))).longValue();

          ReflectionUtils.invokeMethod(ttlGetter, entity);

          TimeToLive ttl = fld.getAnnotation(TimeToLive.class);
          if (!ttl.unit().equals(TimeUnit.SECONDS)) {
            return Optional.of(TimeUnit.SECONDS.convert(ttlPropertyValue, ttl.unit()));
          } else {
            return Optional.of(ttlPropertyValue);
          }
        } catch (SecurityException | IllegalArgumentException e) {
          return Optional.empty();
        }
      } else if (settings.getTimeToLive() != null && settings.getTimeToLive() > 0) {
        return Optional.of(settings.getTimeToLive());
      }
    }
    return Optional.empty();
  }

  private void checkVersion(T entity) {
    List<Field> fields = ObjectUtils.getFieldsWithAnnotation(entity.getClass(), Version.class);
    if (fields.size() == 1) {
      BeanWrapper wrapper = new BeanWrapperImpl(entity);
      Field versionField = fields.get(0);
      String property = versionField.getName();
      if ((versionField.getType() == Integer.class || isPrimitiveOfType(versionField.getType(), Integer.class)) ||
          (versionField.getType() == Long.class || isPrimitiveOfType(versionField.getType(), Long.class))) {
        Number version = (Number) wrapper.getPropertyValue(property);
        Number dbVersion = getEntityVersion(getKey(this.metadata.getRequiredId(entity)), property);

        if (dbVersion != null && version != null && dbVersion.longValue() != version.longValue()) {
          throw new OptimisticLockingFailureException(
              String.format("Cannot delete entity %s with version %s as it is outdated", entity,
                  version));
        }
      }
    }
  }

  private Number getEntityVersion(String key, String versionProperty) {
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    Class<?> type = new TypeToken<Long[]>() {}.getRawType();
    Long[] dbVersionArray = (Long[]) ops.get(key, type, Path.of("$." + versionProperty));
    return dbVersionArray != null ? dbVersionArray[0] : null;
  }

  // -------------------------------------------------------------------------
  // Query By Example Fluent API - QueryByExampleExecutor
  // -------------------------------------------------------------------------

  @Override
  public <S extends T> Optional<S> findOne(Example<S> example) {
    return entityStream.of(example.getProbeType()).filter(example).findFirst();
  }

  @Override
  public <S extends T> Iterable<S> findAll(Example<S> example) {
    return entityStream.of(example.getProbeType()).filter(example).collect(Collectors.toList());
  }

  @Override
  public <S extends T> Iterable<S> findAll(Example<S> example, Sort sort) {
    return entityStream.of(example.getProbeType()).filter(example).sorted(sort).collect(Collectors.toList());
  }

  @Override
  public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
    return pageFromSlice(entityStream.of(example.getProbeType()).filter(example).getSlice(pageable));
  }

  @Override
  public <S extends T> long count(Example<S> example) {
    return entityStream.of(example.getProbeType()).filter(example).count();
  }

  @Override
  public <S extends T> boolean exists(Example<S> example) {
    return count(example) > 0;
  }

  // -------------------------------------------------------------------------
  // Query By Example Fluent API - QueryByExampleExecutor
  // -------------------------------------------------------------------------

  @Override
  public <S extends T, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
    Assert.notNull(example, "Example must not be null");
    Assert.notNull(queryFunction, "Query function must not be null");

    return queryFunction.apply(new FluentQueryByExample<>(example, example.getProbeType(), entityStream, getSearchOps()));
  }

  private SearchOperations<String> getSearchOps() {
    String keyspace = indexer.getKeyspaceForEntityClass(metadata.getJavaType());
    Optional<String> maybeSearchIndex = indexer.getIndexName(keyspace);
    return modulesOperations.opsForSearch(maybeSearchIndex.get());
  }

//  static class FluentQueryByExample<T> implements FluentQuery.FetchableFluentQuery<T> {
//    private final SearchStream<T> searchStream;
//    private final Class<T> probeType;
//
//    private final SearchOperations<String> searchOps;
//
//    public FluentQueryByExample( //
//        Example<T> example, //
//        Class<T> probeType, //
//        EntityStream entityStream, //
//        SearchOperations<String> searchOps //
//    ) {
//      this.probeType = probeType;
//      this.searchOps = searchOps;
//      this.searchStream = entityStream.of(probeType);
//      searchStream.filter(example);
//    }
//
//    @Override
//    public FetchableFluentQuery<T> sortBy(Sort sort) {
//      searchStream.sorted(sort);
//      return this;
//    }
//
//    @Override
//    public <R> FetchableFluentQuery<R> as(Class<R> resultType) {
//      throw new UnsupportedOperationException("`as` is not supported on a Redis Repositories");
//    }
//
//    @Override
//    public FetchableFluentQuery<T> project(Collection<String> properties) {
//      List<MetamodelField<?, ?>> metamodelFields = MetamodelUtils.getMetamodelFieldsForProperties(probeType,
//          properties);
//      metamodelFields.forEach(mmf ->  searchStream.project((MetamodelField<? super T, ?>) mmf));
//      return this;
//    }
//
//    @Override
//    public T oneValue() {
//      var result = searchStream.collect(Collectors.toList());
//
//      if (org.springframework.util.ObjectUtils.isEmpty(result)) {
//        return null;
//      }
//
//      if (result.size() > 1) {
//        throw new IncorrectResultSizeDataAccessException("Query returned non unique result", 1);
//      }
//
//      return result.iterator().next();
//    }
//
//    @Override
//    public T firstValue() {
//      return searchStream.findFirst().orElse(null);
//    }
//
//    @Override
//    public List<T> all() {
//      return searchStream.collect(Collectors.toList());
//    }
//
//    @Override
//    public Page<T> page(Pageable pageable) {
//      Query query = (searchStream.backingQuery().isBlank()) ? new Query() : new Query(searchStream.backingQuery());
//      query.limit(0, 0);
//      SearchResult searchResult = searchOps.search(query);
//      var count = searchResult.getTotalResults();
//      var pageContents = searchStream.limit(pageable.getPageSize()).skip(pageable.getOffset()).collect(Collectors.toList());
//      return new PageImpl<>(pageContents, pageable, count);
//    }
//
//    @Override
//    public Stream<T> stream() {
//      return all().stream();
//    }
//
//    @Override
//    public long count() {
//      return searchStream.count();
//    }
//
//    @Override
//    public boolean exists() {
//      return searchStream.count() > 0;
//    }
//  }
}
