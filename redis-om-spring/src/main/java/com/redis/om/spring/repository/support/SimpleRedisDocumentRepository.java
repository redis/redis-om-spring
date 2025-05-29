package com.redis.om.spring.repository.support;

import static com.redis.om.spring.util.ObjectUtils.*;
import static redis.clients.jedis.json.JsonProtocol.JsonCommand;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.annotation.Reference;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.redis.core.mapping.RedisPersistentProperty;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.annotations.Dialect;
import com.redis.om.spring.audit.EntityAuditor;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.id.IdentifierFilter;
import com.redis.om.spring.id.ULIDIdentifierGenerator;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.mapping.RedisEnhancedPersistentEntity;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.MetamodelUtils;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.EntityStreamImpl;
import com.redis.om.spring.search.stream.RedisFluentQueryByExample;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.serialization.gson.GsonListOfType;
import com.redis.om.spring.util.ObjectUtils;
import com.redis.om.spring.vectorize.Embedder;

import jakarta.persistence.IdClass;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.util.SafeEncoder;

/**
 * Default implementation of {@link RedisDocumentRepository} providing Redis JSON document
 * storage and search capabilities using RedisJSON and RediSearch modules.
 * <p>
 * This repository implementation manages entities as JSON documents in Redis, providing
 * full CRUD operations, bulk operations, field-level updates, and advanced search
 * capabilities through RediSearch integration. It handles entity mapping, ID generation,
 * versioning, auditing, and reference resolution automatically.
 * <p>
 * The repository supports various features including:
 * <ul>
 * <li>Automatic ULID generation for entity IDs</li>
 * <li>Optimistic locking through {@code @Version} support</li>
 * <li>Entity auditing for creation and modification tracking</li>
 * <li>Reference handling with {@code @Reference} annotation</li>
 * <li>TTL (Time To Live) support for automatic expiration</li>
 * <li>Query by Example (QBE) for dynamic queries</li>
 * <li>Bulk operations with pipelined execution</li>
 * <li>Vector embeddings with AI providers (when configured)</li>
 * </ul>
 *
 * @param <T>  the domain entity type managed by this repository
 * @param <ID> the type of the entity identifier
 * @see RedisDocumentRepository
 * @see SimpleKeyValueRepository
 * @since 1.0.0
 */
public class SimpleRedisDocumentRepository<T, ID> extends SimpleKeyValueRepository<T, ID> implements
    RedisDocumentRepository<T, ID> {

  private final static Logger logger = LoggerFactory.getLogger(SimpleRedisDocumentRepository.class);

  /** Redis operations handler for JSON and search modules */
  protected final RedisModulesOperations<String> modulesOperations;
  /** Entity metadata containing type information and ID handling */
  protected final EntityInformation<T, ID> metadata;
  /** Spring Data Key-Value operations for basic CRUD */
  protected final KeyValueOperations operations;
  /** RediSearch index manager for search operations */
  protected final RediSearchIndexer indexer;
  /** Converter for mapping between entities and Redis data structures */
  protected final MappingRedisOMConverter mappingConverter;
  /** Auditor for tracking entity creation and modification */
  protected final EntityAuditor auditor;
  /** Embedder for generating vector embeddings (AI integration) */
  protected final Embedder embedder;
  private final GsonBuilder gsonBuilder;
  private final ULIDIdentifierGenerator generator;
  private final RedisOMProperties properties;
  private final RedisMappingContext mappingContext;
  private final EntityStream entityStream;

  /**
   * Constructs a new {@code SimpleRedisDocumentRepository} with the required dependencies.
   * <p>
   * This constructor initializes all necessary components for Redis document operations
   * including JSON operations, search capabilities, entity mapping, and auditing.
   *
   * @param metadata       entity information containing type and ID metadata
   * @param operations     Spring Data Key-Value operations for basic CRUD
   * @param rmo            Redis modules operations for JSON and search functionality
   * @param indexer        RediSearch indexer for managing search indexes
   * @param mappingContext Redis mapping context for entity metadata
   * @param gsonBuilder    Gson builder for JSON serialization customization
   * @param embedder       embedder for AI-powered vector generation (may be no-op)
   * @param properties     Redis OM configuration properties
   */
  @SuppressWarnings(
    "unchecked"
  )
  public SimpleRedisDocumentRepository( //
      EntityInformation<T, ID> metadata, //
      KeyValueOperations operations, //
      @Qualifier(
        "redisModulesOperations"
      ) RedisModulesOperations<?> rmo, //
      RediSearchIndexer indexer, //
      RedisMappingContext mappingContext, //
      GsonBuilder gsonBuilder, //
      Embedder embedder, //
      RedisOMProperties properties) {
    super(metadata, operations);
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.metadata = metadata;
    this.operations = operations;
    this.indexer = indexer;
    this.mappingConverter = new MappingRedisOMConverter(null, new ReferenceResolverImpl(modulesOperations.template()));
    this.generator = ULIDIdentifierGenerator.INSTANCE;
    this.gsonBuilder = gsonBuilder;
    this.mappingContext = mappingContext;
    this.auditor = new EntityAuditor(modulesOperations.template());
    this.embedder = embedder;
    this.properties = properties;
    this.entityStream = new EntityStreamImpl(modulesOperations, modulesOperations.gsonBuilder(), indexer);
  }

  @Override
  public Iterable<ID> getIds() {
    Gson gson = gsonBuilder.create();
    Optional<Field> maybeIdField = ObjectUtils.getIdFieldForEntityClass(metadata.getJavaType());
    String idField = maybeIdField.map(Field::getName).orElse("id");

    Query query = new Query("*");
    query.limit(0, properties.getRepository().getQuery().getLimit());
    query.returnFields(idField);
    SearchResult searchResult = getSearchOps().search(query);

    return searchResult.getDocuments().stream().map(d -> gson.fromJson(SafeEncoder.encode((byte[]) d.get(idField)),
        metadata.getIdType())).toList();
  }

  @Override
  public Page<ID> getIds(Pageable pageable) {
    List<ID> ids = Lists.newArrayList(getIds());

    int fromIndex = Math.toIntExact(pageable.getOffset());
    int toIndex = fromIndex + pageable.getPageSize();

    return new PageImpl<>(ids.subList(fromIndex, toIndex), pageable, ids.size());
  }

  @Override
  public void deleteById(ID id, Path2 path) {
    modulesOperations.opsForJSON().del(getKey(id), path);
  }

  @Override
  public void updateField(T entity, MetamodelField<T, ?> field, Object value) {
    modulesOperations.opsForJSON().set(getKey(Objects.requireNonNull(metadata.getId(entity))), value, Path2.of(field
        .getJSONPath()));
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public <F> Iterable<F> getFieldsByIds(Iterable<ID> ids, MetamodelField<T, F> field) {
    String[] keys = StreamSupport.stream(ids.spliterator(), false).map(this::getKey).toArray(String[]::new);
    return (Iterable<F>) modulesOperations.opsForJSON().mget(Path2.of(field.getJSONPath()), List.class, keys).stream()
        .flatMap(List::stream).toList();
  }

  @Override
  public Long getExpiration(ID id) {
    RedisTemplate<String, String> template = modulesOperations.template();
    return template.getExpire(getKey(id));
  }

  @Override
  public boolean setExpiration(ID id, Long expiration, TimeUnit timeUnit) {
    RedisTemplate<String, String> template = modulesOperations.template();
    return Boolean.TRUE.equals(template.expire(getKey(id), expiration, timeUnit));
  }

  @Override
  public <S extends T> List<S> saveAll(Iterable<S> entities) {
    Assert.notNull(entities, "The given Iterable of entities must not be null!");
    List<S> saved = new ArrayList<>();
    List<Object> entityIds = new ArrayList<>();

    embedder.processEntities(entities);

    try (Jedis jedis = modulesOperations.client().getJedis().get()) {
      Pipeline pipeline = jedis.pipelined();
      Gson gson = gsonBuilder.create();
      for (S entity : entities) {
        boolean isNew = metadata.isNew(entity);

        KeyValuePersistentEntity<?, ?> keyValueEntity = mappingConverter.getMappingContext()
            .getRequiredPersistentEntity(ClassUtils.getUserClass(entity));
        Object id = isNew ?
            generator.generateIdentifierOfType(Objects.requireNonNull(keyValueEntity.getIdProperty())
                .getTypeInformation()) :
            keyValueEntity.getPropertyAccessor(entity).getProperty(Objects.requireNonNull(keyValueEntity
                .getIdProperty()));
        keyValueEntity.getPropertyAccessor(entity).setProperty(keyValueEntity.getIdProperty(), id);

        String idAsString = validateKeyForWriting(id, entity);

        entityIds.add(idAsString);

        String keyspace = keyValueEntity.getKeySpace();
        byte[] objectKey = createKey(keyspace, idAsString);

        // process entity pre-save mutation
        auditor.processEntity(entity, isNew);

        Optional<Long> maybeTtl = getTTLForEntity(entity);

        RedisData rdo = new RedisData();
        mappingConverter.write(entity, rdo);
        rdo.setId(idAsString);

        List<byte[]> args = new ArrayList<>(4);
        args.add(objectKey);
        args.add(SafeEncoder.encode(Path2.ROOT_PATH.toString()));
        args.add(SafeEncoder.encode(gson.toJson(entity)));
        pipeline.sendCommand(JsonCommand.SET, args.toArray(new byte[args.size()][]));

        processReferenceAnnotations(objectKey, entity, pipeline);

        maybeTtl.ifPresent(ttl -> {
          if (ttl > 0)
            pipeline.expire(objectKey, ttl);
        });

        saved.add(entity);
      }

      List<Object> responses = pipeline.syncAndReturnAll();

      // Process responses using streams to avoid iterator issues
      if (responses != null && !responses.isEmpty()) {
        long failedCount = IntStream.range(0, Math.min(responses.size(), entityIds.size())).filter(i -> responses.get(
            i) instanceof JedisDataException).peek(i -> logger.warn(
                "Failed JSON.SET command for entity with id: {} Error: {}", entityIds.get(i),
                ((JedisDataException) responses.get(i)).getMessage())).count();

        if (failedCount > 0) {
          logger.warn("Total failed JSON.SET commands: {}", failedCount);
        }
      }
    }

    return saved;
  }

  @Override
  public Iterable<T> bulkLoad(String file) throws IOException {
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
  public List<T> findAllById(Iterable<ID> ids) {
    String[] keys = StreamSupport.stream(ids.spliterator(), false).map(this::getKey).toArray(String[]::new);

    return modulesOperations.opsForJSON().mget(metadata.getJavaType(), keys).stream().toList();
  }

  @Override
  public String getKeyspace() {
    return indexer.getKeyspaceForEntityClass(metadata.getJavaType());
  }

  private String getKey(Object id) {
    String stringId = asStringValue(id);
    var maybeIdentifierFilter = indexer.getIdentifierFilterFor(metadata.getJavaType());
    if (maybeIdentifierFilter.isPresent()) {
      IdentifierFilter<String> filter = (IdentifierFilter<String>) maybeIdentifierFilter.get();
      stringId = filter.filter(stringId);
    }
    return getKeyspace() + stringId;
  }

  /**
   * Creates a Redis key byte array for the given keyspace and ID.
   * <p>
   * This method constructs the full Redis key by combining the keyspace prefix
   * with the entity ID, applying any configured ID filters in the process.
   * The resulting key follows the pattern: {@code keyspace:id}
   * <p>
   * ID filters can transform the ID before it becomes part of the key,
   * which is useful for implementing patterns like hash tags for Redis
   * cluster slot allocation.
   *
   * @param keyspace the keyspace prefix for the entity type
   * @param id       the entity identifier
   * @return the complete Redis key as a byte array
   */
  public byte[] createKey(String keyspace, String id) {
    // handle IdFilters
    var maybeIdentifierFilter = indexer.getIdentifierFilterFor(keyspace);
    if (maybeIdentifierFilter.isPresent()) {
      IdentifierFilter<String> filter = (IdentifierFilter<String>) maybeIdentifierFilter.get();
      id = filter.filter(id);
    }

    return this.mappingConverter.toBytes(keyspace.endsWith(":") ? keyspace + id : keyspace + ":" + id);
  }

  /**
   * Processes {@code @Reference} annotated fields in an entity and updates their values in Redis.
   * <p>
   * This method handles the persistence of entity references by storing only the
   * Redis keys of referenced entities instead of embedding the full objects.
   * It supports both single references and collections of references.
   * <p>
   * For single references, the referenced entity's key is stored as a string.
   * For collection references, an array of keys is stored. This allows for
   * efficient loading of related entities without data duplication.
   *
   * @param objectKey the Redis key of the entity being processed
   * @param entity    the entity containing reference fields
   * @param pipeline  the Jedis pipeline for batched operations
   */
  private void processReferenceAnnotations(byte[] objectKey, Object entity, Pipeline pipeline) {
    List<Field> fields = getFieldsWithAnnotation(entity.getClass(), Reference.class);
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
            args.add(SafeEncoder.encode(Path2.of("$." + f.getName()).toString()));
            args.add(SafeEncoder.encode(gson.toJson(referenceKeys)));
            pipeline.sendCommand(JsonCommand.SET, args.toArray(new byte[args.size()][]));

          } else {
            Object id = ObjectUtils.getIdFieldForEntity(referencedValue);
            if (id != null) {
              String referenceKey = indexer.getKeyspaceForEntityClass(f.getType()) + id;

              List<byte[]> args = new ArrayList<>(4);
              args.add(objectKey);
              args.add(SafeEncoder.encode(Path2.of("$." + f.getName()).toString()));
              args.add(SafeEncoder.encode(gson.toJson(referenceKey)));
              pipeline.sendCommand(JsonCommand.SET, args.toArray(new byte[args.size()][]));
            }
          }
        }
      });
    }
  }

  /**
   * Determines the Time To Live (TTL) value for an entity based on configuration and annotations.
   * <p>
   * This method checks for TTL configuration in the following order:
   * <ol>
   * <li>Entity field annotated with {@code @TimeToLive} - supports dynamic TTL per entity</li>
   * <li>Keyspace configuration settings - static TTL for all entities of a type</li>
   * </ol>
   * <p>
   * When using field-based TTL, the method respects the time unit specified in the
   * {@code @TimeToLive} annotation and converts it to seconds for Redis.
   *
   * @param entity the entity to determine TTL for
   * @return an {@link Optional} containing the TTL in seconds, or empty if no TTL is configured
   */
  private Optional<Long> getTTLForEntity(Object entity) {
    KeyspaceConfiguration keyspaceConfig = mappingContext.getMappingConfiguration().getKeyspaceConfiguration();
    if (keyspaceConfig.hasSettingsFor(entity.getClass())) {
      var settings = keyspaceConfig.getKeyspaceSettings(entity.getClass());

      if (org.springframework.util.StringUtils.hasText(settings.getTimeToLivePropertyName())) {
        Method ttlGetter;
        try {
          Field fld = ReflectionUtils.findField(entity.getClass(), settings.getTimeToLivePropertyName());
          ttlGetter = ObjectUtils.getGetterForField(entity.getClass(), Objects.requireNonNull(fld));
          long ttlPropertyValue = ((Number) Objects.requireNonNull(ReflectionUtils.invokeMethod(ttlGetter, entity)))
              .longValue();

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

  /**
   * Validates entity version for optimistic locking before deletion.
   * <p>
   * This method implements optimistic locking by comparing the version field
   * value in the entity with the current version stored in Redis. If the
   * versions don't match, it indicates the entity has been modified by
   * another process, and an {@link OptimisticLockingFailureException} is thrown.
   * <p>
   * Version fields must be annotated with {@code @Version} and be of type
   * {@code Integer}, {@code int}, {@code Long}, or {@code long}.
   *
   * @param entity the entity to check version for
   * @throws OptimisticLockingFailureException if version mismatch is detected
   */
  private void checkVersion(T entity) {
    List<Field> fields = ObjectUtils.getFieldsWithAnnotation(entity.getClass(), Version.class);
    if (fields.size() == 1) {
      BeanWrapper wrapper = new BeanWrapperImpl(entity);
      Field versionField = fields.get(0);
      String property = versionField.getName();
      if ((versionField.getType() == Integer.class || isPrimitiveOfType(versionField.getType(),
          Integer.class)) || (versionField.getType() == Long.class || isPrimitiveOfType(versionField.getType(),
              Long.class))) {
        Number version = (Number) wrapper.getPropertyValue(property);
        Number dbVersion = getEntityVersion(getKey(this.metadata.getRequiredId(entity)), property);

        if (dbVersion != null && version != null && dbVersion.longValue() != version.longValue()) {
          throw new OptimisticLockingFailureException(String.format(
              "Cannot delete entity %s with version %s as it is outdated", entity, version));
        }
      }
    }
  }

  /**
   * Retrieves the current version value of an entity from Redis.
   * <p>
   * This method fetches the version field value directly from the JSON document
   * stored in Redis using JSONPath. It's used as part of the optimistic locking
   * mechanism to detect concurrent modifications.
   *
   * @param key             the Redis key of the entity
   * @param versionProperty the name of the version property
   * @return the current version number, or null if not found
   */
  private Number getEntityVersion(String key, String versionProperty) {
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    Class<?> type = new TypeToken<Long[]>() {
    }.getRawType();
    Long[] dbVersionArray = (Long[]) ops.get(key, type, Path2.of("$." + versionProperty));
    return dbVersionArray != null ? dbVersionArray[0] : null;
  }

  // -------------------------------------------------------------------------
  // Query By Example Fluent API - QueryByExampleExecutor
  // -------------------------------------------------------------------------

  @Override
  public <S extends T> Optional<S> findOne(Example<S> example) {
    Iterable<S> result = findAll(example);
    var size = Iterables.size(result);
    if (size > 1) {
      throw new IncorrectResultSizeDataAccessException("Query returned non unique result", 1);
    }

    return StreamSupport.stream(result.spliterator(), false).findFirst();
  }

  @Override
  public <S extends T> Iterable<S> findAll(Example<S> example) {
    return entityStream.of(example.getProbeType()).dialect(Dialect.TWO.getValue()).filter(example).collect(Collectors
        .toList());
  }

  @Override
  public <S extends T> Iterable<S> findAll(Example<S> example, Sort sort) {
    return entityStream.of(example.getProbeType()).dialect(Dialect.TWO.getValue()).filter(example).sorted(sort).collect(
        Collectors.toList());
  }

  @Override
  public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
    SearchStream<S> stream = entityStream.of(example.getProbeType());
    var offset = pageable.getPageNumber() * pageable.getPageSize();
    var limit = pageable.getPageSize();
    Page<S> page = stream.filter(example).dialect(Dialect.TWO.getValue()).loadAll().limit(limit, Math.toIntExact(
        offset)).toList(pageable, stream.getEntityClass());

    return page;
  }

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.repository.PagingAndSortingRepository#findAll(org.
   * springframework.data.domain.Pageable) */
  @Override
  public Page<T> findAll(Pageable pageable) {
    Assert.notNull(pageable, "Pageable must not be null!");

    if (pageable.isUnpaged()) {
      List<T> result = findAll(pageable.getSort());
      return new PageImpl<>(result, Pageable.unpaged(), result.size());
    }

    if (indexer.indexDefinitionExistsFor(metadata.getJavaType())) {
      String searchIndex = indexer.getIndexName(metadata.getJavaType());

      SearchOperations<String> searchOps = modulesOperations.opsForSearch(searchIndex);
      Query query = new Query("*");
      query.limit(Math.toIntExact(pageable.getOffset()), pageable.getPageSize());

      for (Order order : pageable.getSort()) {
        query.setSortBy(order.getProperty(), order.isAscending());
      }

      SearchResult searchResult = searchOps.search(query);
      Gson gson = gsonBuilder.create();

      if (searchResult.getTotalResults() > 0) {
        List<T> content = searchResult.getDocuments().stream().map(d -> gson.fromJson(SafeEncoder.encode((byte[]) d.get(
            "$")), metadata.getJavaType())).toList();

        return new PageImpl<>(content, pageable, searchResult.getTotalResults());
      } else {
        return Page.empty();
      }
    } else {
      return super.findAll(pageable);
    }
  }

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.repository.PagingAndSortingRepository#findAll(org.
   * springframework.data.domain.Sort) */
  @Override
  public List<T> findAll(Sort sort) {

    Assert.notNull(sort, "Sort must not be null!");

    Pageable pageRequest = PageRequest.of(0, properties.getRepository().getQuery().getLimit(), sort);

    return findAll(pageRequest).toList();
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

    return queryFunction.apply(new RedisFluentQueryByExample<>(example, example.getProbeType(), entityStream,
        getSearchOps(), mappingConverter.getMappingContext()));
  }

  /**
   * Updates an entity based on the provided example, applying only non-null fields.
   * <p>
   * This method performs a partial update of an existing entity by applying only
   * the non-null fields from the example probe object. The {@link ExampleMatcher}
   * controls which properties are included in the update. After the update,
   * the method retrieves and returns the updated entity from Redis.
   * <p>
   * The entity must have a non-null ID, and only fields that pass the matcher's
   * criteria and have non-null values will be updated.
   *
   * @param <S>     the entity subtype
   * @param example the example containing the probe entity and matcher
   * @return the updated entity after applying the changes
   * @throws IllegalArgumentException if the example probe has no ID
   */
  @Override
  public <S extends T> S update(Example<S> example) {
    S probe = example.getProbe();
    ExampleMatcher matcher = example.getMatcher();
    ID id = metadata.getId(probe);

    if (id == null) {
      throw new IllegalArgumentException("Example object must have an ID");
    }

    String key = getKey(id);

    Class<?> entityType = metadata.getJavaType();
    List<UpdateOperation> updateOperations = new ArrayList<>();

    List<MetamodelField<?, ?>> metamodelFields = MetamodelUtils.getMetamodelFieldsForProperties(entityType,
        getAllProperties(entityType));

    for (MetamodelField<?, ?> metamodelField : metamodelFields) {
      String propertyName = metamodelField.getSearchAlias();

      if (shouldIncludeProperty(matcher, propertyName)) {
        Object value = getPropertyValue(probe, propertyName);

        if (value != null) {
          updateOperations.add(new UpdateOperation(key, metamodelField, value));
        }
      }
    }

    executePipelinedUpdates(updateOperations);

    // Use JSON GET operation to fetch the updated entity
    return (S) getJSONOperations().get(key, entityType);
  }

  /**
   * Updates multiple entities based on the provided examples in a single batch operation.
   * <p>
   * This method efficiently updates multiple entities by collecting all update
   * operations and executing them in a single pipelined batch. Each example
   * in the iterable is processed according to its own {@link ExampleMatcher},
   * allowing different update criteria for each entity.
   * <p>
   * All entities must have non-null IDs. The method returns immediately if
   * the examples iterable is empty.
   *
   * @param <S>      the entity subtype
   * @param examples an iterable of examples to process for updates
   * @throws IllegalArgumentException if any example probe has no ID
   */
  @Override
  public <S extends T> void updateAll(Iterable<Example<S>> examples) {
    if (!examples.iterator().hasNext()) {
      return; // No examples to process
    }

    List<UpdateOperation> updateOperations = new ArrayList<>();
    Class<?> entityType = metadata.getJavaType();
    List<MetamodelField<?, ?>> metamodelFields = MetamodelUtils.getMetamodelFieldsForProperties(entityType,
        getAllProperties(entityType));

    for (Example<S> example : examples) {
      S probe = example.getProbe();
      ExampleMatcher matcher = example.getMatcher();
      ID id = metadata.getId(probe);

      if (id == null) {
        throw new IllegalArgumentException("Example object must have an ID");
      }

      String key = getKey(id);

      for (MetamodelField<?, ?> metamodelField : metamodelFields) {
        String propertyName = metamodelField.getSearchAlias();

        if (shouldIncludeProperty(matcher, propertyName)) {
          Object value = getPropertyValue(probe, propertyName);

          if (value != null) {
            updateOperations.add(new UpdateOperation(key, metamodelField, value));
          }
        }
      }
    }

    executePipelinedUpdates(updateOperations);
  }

  /**
   * Generates the complete Redis key for the given entity.
   * <p>
   * This method constructs the full Redis key by combining the entity's keyspace
   * with its ID. It handles both simple and composite IDs appropriately:
   * <ul>
   * <li>Simple IDs: Converted directly to string and appended to keyspace</li>
   * <li>Composite IDs: Individual ID components are joined with colons</li>
   * </ul>
   * <p>
   * Any configured identifier filters are applied to transform the ID before
   * constructing the final key. This is useful for Redis cluster environments
   * where hash tags may be needed for proper slot distribution.
   *
   * @param entity the entity to generate a key for
   * @return the complete Redis key for the entity
   * @throws IllegalArgumentException if the entity has no ID
   */
  @Override
  public String getKeyFor(T entity) {
    // Get the mapping context's entity info
    RedisEnhancedPersistentEntity<?> persistentEntity = (RedisEnhancedPersistentEntity<?>) mappingContext
        .getRequiredPersistentEntity(entity.getClass());

    String stringId;

    // Handle composite IDs
    if (persistentEntity.isIdClassComposite()) {
      BeanWrapper wrapper = new DirectFieldAccessFallbackBeanWrapper(entity);
      List<String> idParts = new ArrayList<>();

      for (RedisPersistentProperty idProperty : persistentEntity.getIdProperties()) {
        Object propertyValue = wrapper.getPropertyValue(idProperty.getName());
        if (propertyValue != null) {
          idParts.add(propertyValue.toString());
        }
      }

      stringId = String.join(":", idParts);
    } else {
      Object id = getIdFieldForEntity(entity);
      stringId = mappingConverter.getConversionService().convert(id, String.class);
    }

    var maybeIdentifierFilter = indexer.getIdentifierFilterFor(metadata.getJavaType());
    if (maybeIdentifierFilter.isPresent()) {
      IdentifierFilter<String> filter = (IdentifierFilter<String>) maybeIdentifierFilter.get();
      stringId = filter.filter(stringId);
    }
    return getKeyspace() + stringId;
  }

  /**
   * Returns the JSON operations interface for RedisJSON module operations.
   * <p>
   * This convenience method provides access to the JSONOperations interface
   * used for executing RedisJSON commands like JSON.GET, JSON.SET, etc.
   *
   * @return the {@link JSONOperations} instance for JSON operations
   */
  private JSONOperations<String> getJSONOperations() {
    return modulesOperations.opsForJSON();
  }

  /**
   * Executes a batch of field update operations using Redis pipelining.
   * <p>
   * This method efficiently processes multiple field updates by batching them
   * into a single pipeline execution. Each update operation uses RedisJSON's
   * JSON.SET command with the "XX" flag to ensure updates only occur on
   * existing documents.
   * <p>
   * Pipelining reduces network round trips and improves performance when
   * updating multiple fields or multiple entities.
   *
   * @param updateOperations the list of update operations to execute
   */
  private void executePipelinedUpdates(List<UpdateOperation> updateOperations) {
    try (Jedis jedis = modulesOperations.client().getJedis().get()) {
      Pipeline pipeline = jedis.pipelined();

      for (UpdateOperation op : updateOperations) {
        List<byte[]> args = new ArrayList<>(4);
        args.add(SafeEncoder.encode(op.key));
        args.add(SafeEncoder.encode(Path2.of(op.field.getJSONPath()).toString()));
        args.add(SafeEncoder.encode(new Gson().toJson(op.value)));
        args.add(SafeEncoder.encode("XX"));

        pipeline.sendCommand(JsonCommand.SET, args.toArray(new byte[0][]));
      }

      pipeline.sync();
    }
  }

  /**
   * Creates and returns search operations for the entity's search index.
   * <p>
   * This helper method constructs the appropriate {@link SearchOperations}
   * instance for executing RediSearch queries against the entity's index.
   * The search index name is derived from the entity's keyspace.
   *
   * @return configured {@link SearchOperations} for the entity type
   */
  private SearchOperations<String> getSearchOps() {
    String keyspace = indexer.getKeyspaceForEntityClass(metadata.getJavaType());
    String searchIndex = indexer.getIndexName(keyspace);
    return modulesOperations.opsForSearch(searchIndex);
  }

  /**
   * Validates and formats an entity ID for use as a Redis key during write operations.
   * <p>
   * This method handles both simple and composite IDs. For entities using
   * {@code @IdClass} for composite keys, it extracts values from all ID
   * properties and joins them with colons. For simple IDs, it converts
   * the ID to a string representation.
   * <p>
   * Composite key example: An entity with ID properties 'category' and 'productId'
   * would produce a key like "electronics:12345".
   *
   * @param id   the ID value (may be simple or composite)
   * @param item the entity being written
   * @return the formatted ID string suitable for use in Redis keys
   */
  private String validateKeyForWriting(Object id, Object item) {
    // Get the mapping context's entity info
    RedisEnhancedPersistentEntity<?> entity = (RedisEnhancedPersistentEntity<?>) mappingContext
        .getRequiredPersistentEntity(item.getClass());

    // Handle composite IDs
    if (entity.isIdClassComposite()) {
      BeanWrapper wrapper = new DirectFieldAccessFallbackBeanWrapper(item);
      List<String> idParts = new ArrayList<>();

      for (RedisPersistentProperty idProperty : entity.getIdProperties()) {
        Object propertyValue = wrapper.getPropertyValue(idProperty.getName());
        if (propertyValue != null) {
          idParts.add(propertyValue.toString());
        }
      }

      return String.join(":", idParts);
    } else {
      // Regular single ID handling
      return mappingConverter.getConversionService().convert(id, String.class);
    }
  }

  /**
   * Converts an ID value to its string representation, handling composite IDs.
   * <p>
   * This method provides special handling for composite IDs used with {@code @IdClass}.
   * It introspects the mapping context to find entities that use the given value's
   * class as their ID class, then extracts and combines the individual ID property
   * values into a colon-separated string.
   * <p>
   * For simple IDs, it delegates to the conversion service for standard conversion.
   *
   * @param value the ID value to convert (simple or composite)
   * @return the string representation of the ID
   */
  private String asStringValue(Object value) {
    // For composite IDs used in @IdClass
    if (value != null) {
      // Get all persistent entities
      for (RedisPersistentEntity<?> entity : mappingContext.getPersistentEntities()) {
        // Find the entity that uses this ID class
        IdClass idClassAnn = entity.getType().getAnnotation(IdClass.class);
        if (idClassAnn != null && idClassAnn.value().equals(value.getClass())) {
          // Found the entity that uses this ID class
          BeanWrapper wrapper = new DirectFieldAccessFallbackBeanWrapper(value);
          RedisEnhancedPersistentEntity<?> enhancedEntity = (RedisEnhancedPersistentEntity<?>) entity;

          // Build composite key from ID properties in order
          List<String> idParts = new ArrayList<>();
          for (RedisPersistentProperty idProperty : enhancedEntity.getIdProperties()) {
            Object propertyValue = wrapper.getPropertyValue(idProperty.getName());
            if (propertyValue != null) {
              idParts.add(propertyValue.toString());
            }
          }
          return String.join(":", idParts);
        }
      }
    }

    return mappingConverter.getConversionService().convert(value, String.class);
  }

}
