package com.redis.om.spring.repository.support;

import static com.redis.om.spring.util.ObjectUtils.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.keyvalue.core.IterableConverter;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.redis.core.PartialUpdate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;
import org.springframework.data.redis.core.mapping.RedisPersistentProperty;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.redis.om.spring.RedisEnhancedKeyValueAdapter;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.audit.EntityAuditor;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.id.IdentifierFilter;
import com.redis.om.spring.id.ULIDIdentifierGenerator;
import com.redis.om.spring.indexing.LexicographicIndexer;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.mapping.RedisEnhancedPersistentEntity;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.MetamodelUtils;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.RedisEnhancedRepository;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.EntityStreamImpl;
import com.redis.om.spring.search.stream.RedisFluentQueryByExample;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.util.ObjectUtils;
import com.redis.om.spring.vectorize.Embedder;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.util.SafeEncoder;

/**
 * Default implementation of {@link RedisEnhancedRepository} providing enhanced Redis operations
 * for hash-based entities with search capabilities.
 * <p>
 * This repository implementation extends Spring Data Redis's {@link SimpleKeyValueRepository}
 * and adds support for Redis Stack features including RediSearch indexing, vector similarity search,
 * Query by Example, field-level updates, and batch operations.
 * </p>
 *
 * @param <T>  the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 */
public class SimpleRedisEnhancedRepository<T, ID> extends SimpleKeyValueRepository<T, ID> implements
    RedisEnhancedRepository<T, ID> {

  private static final Logger logger = LoggerFactory.getLogger(SimpleRedisEnhancedRepository.class);

  /** Operations for Redis modules (Search, JSON, etc.) */
  protected final RedisModulesOperations<String> modulesOperations;
  /** Metadata about the entity type managed by this repository */
  protected final EntityInformation<T, ID> metadata;
  /** Core key-value operations */
  protected final KeyValueOperations operations;
  /** Manages RediSearch indexes for entities */
  protected final RediSearchIndexer indexer;
  /** Converts between Java objects and Redis data structures */
  protected final MappingRedisOMConverter mappingConverter;
  /** Enhanced adapter for Redis key-value operations */
  protected final RedisEnhancedKeyValueAdapter enhancedKeyValueAdapter;
  /** Handles entity auditing (created/modified timestamps) */
  protected final EntityAuditor auditor;
  /** Handles vector embedding generation for AI/ML features */
  protected final Embedder embedder;
  /**
   * Handles lexicographic sorted set maintenance
   */
  protected final LexicographicIndexer lexicographicIndexer;

  private final ULIDIdentifierGenerator generator;
  private final RedisOMProperties properties;

  private final EntityStream entityStream;

  /**
   * Constructs a new {@code SimpleRedisEnhancedRepository} with the specified dependencies.
   *
   * @param metadata   metadata about the entity type
   * @param operations key-value operations for basic Redis operations
   * @param rmo        Redis modules operations for advanced features
   * @param indexer    RediSearch indexer for managing search indexes
   * @param embedder   embedder for generating vector embeddings
   * @param properties configuration properties for Redis OM
   */
  @SuppressWarnings(
    "unchecked"
  )
  public SimpleRedisEnhancedRepository( //
      EntityInformation<T, ID> metadata, //
      KeyValueOperations operations, //
      @Qualifier(
        "redisModulesOperations"
      ) RedisModulesOperations<?> rmo, //
      RediSearchIndexer indexer, //
      Embedder embedder, //
      RedisOMProperties properties //
  ) {
    super(metadata, operations);
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.metadata = metadata;
    this.operations = operations;
    this.indexer = indexer;
    this.mappingConverter = new MappingRedisOMConverter(null, new ReferenceResolverImpl(modulesOperations.template()));
    this.enhancedKeyValueAdapter = new RedisEnhancedKeyValueAdapter(rmo.template(), rmo, indexer, embedder, properties);
    this.generator = ULIDIdentifierGenerator.INSTANCE;
    this.auditor = new EntityAuditor(modulesOperations.template());
    this.embedder = embedder;
    this.properties = properties;
    this.lexicographicIndexer = new LexicographicIndexer(modulesOperations.template(), indexer);
    this.entityStream = new EntityStreamImpl(modulesOperations, modulesOperations.gsonBuilder(), indexer);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public Iterable<ID> getIds() {
    String keyspace = indexer.getKeyspaceForEntityClass(metadata.getJavaType());
    String searchIndex = indexer.getIndexName(keyspace);

    SearchOperations<String> searchOps = modulesOperations.opsForSearch(searchIndex);
    Optional<Field> maybeIdField = ObjectUtils.getIdFieldForEntityClass(metadata.getJavaType());
    String idField = maybeIdField.map(Field::getName).orElse("id");
    Query query = new Query("*");
    query.limit(0, properties.getRepository().getQuery().getLimit());
    query.returnFields(idField);
    SearchResult searchResult = searchOps.search(query);

    return (List<ID>) searchResult.getDocuments().stream() //
        .map(d -> ObjectUtils.documentToObject(d, metadata.getJavaType(), mappingConverter)) //
        .map(e -> ObjectUtils.getIdFieldForEntity(maybeIdField.get(), e)) //
        .toList();
  }

  @Override
  public Page<ID> getIds(Pageable pageable) {
    List<ID> ids = Lists.newArrayList(getIds());

    int fromIndex = Long.valueOf(pageable.getOffset()).intValue();
    int toIndex = fromIndex + pageable.getPageSize();

    return new PageImpl<>(ids.subList(fromIndex, toIndex), pageable, ids.size());
  }

  @Override
  public void updateField(T entity, MetamodelField<T, ?> field, Object value) {
    PartialUpdate<?> update = new PartialUpdate<>(metadata.getId(entity).toString(), metadata.getJavaType()).set(field
        .getSearchAlias(), value);

    enhancedKeyValueAdapter.update(update);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public <F> Iterable<F> getFieldsByIds(Iterable<ID> ids, MetamodelField<T, F> field) {
    RedisTemplate<String, String> template = modulesOperations.template();
    List<String> keys = StreamSupport.stream(ids.spliterator(), false) //
        .map(this::getKey).toList();

    return (Iterable<F>) keys.stream() //
        .map(key -> template.opsForHash().get(key, field.getSearchAlias())) //
        .collect(Collectors.toList());
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

  /* (non-Javadoc)
   *
   * @see org.springframework.data.repository.CrudRepository#findAll() */
  @Override
  public List<T> findAll() {
    return IterableConverter.toList(operations.findAll(metadata.getJavaType()));
  }

  // -------------------------------------------------------------------------
  // Methods from PagingAndSortingRepository
  // -------------------------------------------------------------------------

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

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.repository.PagingAndSortingRepository#findAll(org.
   * springframework.data.domain.Pageable) */
  @Override
  public Page<T> findAll(Pageable pageable) {
    Assert.notNull(pageable, "Pageable must not be null!");

    if (pageable.isUnpaged()) {
      List<T> result = findAll();
      return new PageImpl<>(result, Pageable.unpaged(), result.size());
    }

    if (indexer.indexDefinitionExistsFor(metadata.getJavaType())) {
      String searchIndex = indexer.getIndexName(metadata.getJavaType());
      SearchOperations<String> searchOps = modulesOperations.opsForSearch(searchIndex);
      Query query = new Query("*");
      query.limit(Math.toIntExact(pageable.getOffset()), pageable.getPageSize());

      pageable.getSort();
      for (Order order : pageable.getSort()) {
        query.setSortBy(order.getProperty(), order.isAscending());
      }

      SearchResult searchResult = searchOps.search(query);

      if (searchResult.getTotalResults() > 0) {
        @SuppressWarnings(
          "unchecked"
        ) List<T> content = (List<T>) searchResult.getDocuments().stream() //
            .map(d -> {
              Object entity = ObjectUtils.documentToObject(d, metadata.getJavaType(), mappingConverter);
              return ObjectUtils.populateRedisKey(entity, d.getId());
            }) //
            .toList();
        return new PageImpl<>(content, pageable, searchResult.getTotalResults());
      } else {
        return Page.empty();
      }
    } else {
      Iterable<T> content = operations.findInRange(pageable.getOffset(), pageable.getPageSize(), pageable.getSort(),
          metadata.getJavaType());

      return new PageImpl<>(IterableConverter.toList(content), pageable, this.operations.count(metadata.getJavaType()));
    }
  }

  @Override
  public String getKeyspace() {
    return indexer.getKeyspaceForEntityClass(metadata.getJavaType());
  }

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

      if (propertyName.equals("id")) {
        continue;
      }

      if (shouldIncludeProperty(matcher, propertyName)) {
        Object value = getPropertyValue(probe, propertyName);

        if (value != null) {
          updateOperations.add(new UpdateOperation(key, metamodelField, value));
        }
      }
    }

    if (!updateOperations.isEmpty()) {
      executePipelinedUpdates(updateOperations);
    }

    return (S) findById(id).orElseThrow(() -> new RuntimeException("Failed to fetch updated entity"));
  }

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

  @Override
  public String getKeyFor(T entity) {
    // Get the mapping context's entity info
    RedisEnhancedPersistentEntity<?> persistentEntity = (RedisEnhancedPersistentEntity<?>) mappingConverter
        .getMappingContext().getRequiredPersistentEntity(entity.getClass());

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

  private String getKey(Object id) {
    var maybeIdentifierFilter = indexer.getIdentifierFilterFor(metadata.getJavaType());
    if (maybeIdentifierFilter.isPresent()) {
      IdentifierFilter<String> filter = (IdentifierFilter<String>) maybeIdentifierFilter.get();
      id = filter.filter(id.toString());
    }
    return getKeyspace() + id.toString();
  }

  @Override
  public <S extends T> List<S> saveAll(Iterable<S> entities) {
    Assert.notNull(entities, "The given Iterable of entities must not be null!");
    List<S> saved = new ArrayList<>();
    List<String> entityIds = new ArrayList<>();

    embedder.processEntities(entities);

    try (Jedis jedis = modulesOperations.client().getJedis().get()) {
      Pipeline pipeline = jedis.pipelined();

      for (S entity : entities) {
        boolean isNew = metadata.isNew(entity);

        KeyValuePersistentEntity<?, ?> keyValueEntity = mappingConverter.getMappingContext()
            .getRequiredPersistentEntity(ClassUtils.getUserClass(entity));
        Object id = isNew ?
            generator.generateIdentifierOfType(keyValueEntity.getIdProperty().getTypeInformation()) :
            keyValueEntity.getPropertyAccessor(entity).getProperty(keyValueEntity.getIdProperty());
        keyValueEntity.getPropertyAccessor(entity).setProperty(keyValueEntity.getIdProperty(), id);

        String idAsString = validateKeyForWriting(id, entity);
        entityIds.add(idAsString);

        String keyspace = keyValueEntity.getKeySpace();
        byte[] objectKey = createKey(keyspace, idAsString);

        // process entity pre-save mutation
        auditor.processEntity(entity, isNew);

        // Process lexicographic indexing
        String keyspaceWithColon = keyspace.endsWith(":") ? keyspace : keyspace + ":";
        lexicographicIndexer.processEntity(entity, idAsString, isNew, keyspaceWithColon);

        RedisData rdo = new RedisData();
        mappingConverter.write(entity, rdo);

        pipeline.hmset(objectKey, rdo.getBucket().rawMap());

        if (expires(rdo)) {
          pipeline.expire(objectKey, rdo.getTimeToLive());
        }

        saved.add(entity);
      }

      List<Object> responses = pipeline.syncAndReturnAll();

      // Process responses to check for errors
      if (responses != null && !responses.isEmpty()) {
        List<String> failedIds = new ArrayList<>();
        long failedCount = IntStream.range(0, Math.min(responses.size(), entityIds.size())).filter(i -> responses.get(
            i) instanceof JedisDataException).peek(i -> {
              failedIds.add(entityIds.get(i));
              logger.warn("Failed HMSET command for entity with id: {} Error: {}", entityIds.get(i),
                  ((JedisDataException) responses.get(i)).getMessage());
            }).count();

        if (failedCount > 0) {
          String errorMsg = String.format("Failed to save %d entities with IDs: %s", failedCount, failedIds);
          if (properties.getRepository().isThrowOnSaveAllFailure()) {
            throw new RuntimeException(errorMsg);
          } else {
            logger.warn("Total failed HMSET commands: {}", failedCount);
          }
        }
      }
    }

    return saved;
  }

  @Override
  public void delete(T entity) {
    Assert.notNull(entity, "The given entity must not be null");

    // Check if this entity class has lexicographic fields
    Set<String> lexicographicFields = indexer.getLexicographicFields(entity.getClass());
    if (lexicographicFields != null && !lexicographicFields.isEmpty()) {
      // Process lexicographic deletion before deleting the entity
      Object id = metadata.getRequiredId(entity);
      String idAsString = validateKeyForWriting(id, entity);
      KeyValuePersistentEntity<?, ?> keyValueEntity = mappingConverter.getMappingContext().getRequiredPersistentEntity(
          ClassUtils.getUserClass(entity));
      String keyspace = keyValueEntity.getKeySpace();
      String keyspaceWithColon = keyspace.endsWith(":") ? keyspace : keyspace + ":";
      lexicographicIndexer.processEntityDeletion(entity, idAsString, keyspaceWithColon);
    }

    super.delete(entity);
  }

  @Override
  public void deleteById(ID id) {
    Assert.notNull(id, "The given id must not be null");

    // Check if this entity class has lexicographic fields
    Set<String> lexicographicFields = indexer.getLexicographicFields(metadata.getJavaType());
    if (lexicographicFields != null && !lexicographicFields.isEmpty()) {
      // Try to load the entity to process lexicographic deletion
      Optional<T> entity = findById(id);
      if (entity.isPresent()) {
        delete(entity.get());
        return;
      }
    }

    // For entities without lexicographic fields or when entity not found, just call parent
    super.deleteById(id);
  }

  @Override
  public void deleteAll(Iterable<? extends T> entities) {
    Assert.notNull(entities, "The given Iterable of entities not be null!");
    entities.forEach(this::delete);
  }

  /**
   * Creates a Redis key from the given keyspace and ID, applying any configured identifier filters.
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

  private boolean expires(RedisData data) {
    return data.getTimeToLive() != null && data.getTimeToLive() > 0L;
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
    return entityStream.of(example.getProbeType()).filter(example).collect(Collectors.toList());
  }

  @Override
  public <S extends T> Iterable<S> findAll(Example<S> example, Sort sort) {
    return entityStream.of(example.getProbeType()).filter(example).sorted(sort).collect(Collectors.toList());
  }

  @Override
  public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
    SearchStream<S> stream = entityStream.of(example.getProbeType());
    var offset = pageable.getPageNumber() * pageable.getPageSize();
    var limit = pageable.getPageSize();
    Page<S> page = stream.filter(example).loadAll().limit(limit, Math.toIntExact(offset)).toList(pageable, stream
        .getEntityClass());

    return page;
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

  private void executePipelinedUpdates(List<UpdateOperation> updateOperations) {
    try (Jedis jedis = modulesOperations.client().getJedis().get()) {
      Pipeline pipeline = jedis.pipelined();

      Map<String, Map<byte[], byte[]>> updates = new HashMap<>();

      for (UpdateOperation op : updateOperations) {
        byte[] value = convertToBinary(op.field, op.value);
        if (value != null && value.length > 0) {
          updates.computeIfAbsent(op.key, k -> new HashMap<>()).put(SafeEncoder.encode(op.field.getSearchAlias()),
              value);
        }
      }

      for (Map.Entry<String, Map<byte[], byte[]>> entry : updates.entrySet()) {
        if (!entry.getValue().isEmpty()) {
          pipeline.hmset(SafeEncoder.encode(entry.getKey()), entry.getValue());
        }
      }

      pipeline.sync();
    }
  }

  private byte[] convertToBinary(MetamodelField<?, ?> field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof String) {
      return SafeEncoder.encode((String) value);
    }
    RedisData redisData = new RedisData();
    mappingConverter.write(value, redisData);
    byte[] binaryValue = redisData.getBucket().get(field.getSearchAlias());
    return binaryValue != null && binaryValue.length > 0 ? binaryValue : null;
  }

  private SearchOperations<String> getSearchOps() {
    String keyspace = indexer.getKeyspaceForEntityClass(metadata.getJavaType());
    String searchIndex = indexer.getIndexName(keyspace);
    return modulesOperations.opsForSearch(searchIndex);
  }

  private String validateKeyForWriting(Object id, Object item) {
    // Get the mapping context's entity info
    RedisEnhancedPersistentEntity<?> entity = (RedisEnhancedPersistentEntity<?>) mappingConverter.getMappingContext()
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
}
