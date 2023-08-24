package com.redis.om.spring.repository.support;

import com.google.common.collect.Lists;
import com.redis.om.spring.RediSearchIndexer;
import com.redis.om.spring.RedisEnhancedKeyValueAdapter;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.audit.EntityAuditor;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.id.ULIDIdentifierGenerator;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.RedisEnhancedRepository;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.EntityStreamImpl;
import com.redis.om.spring.search.stream.FluentQueryByExample;
import com.redis.om.spring.util.ObjectUtils;
import com.redis.om.spring.vectorize.FeatureExtractor;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.redis.om.spring.RedisOMProperties.MAX_SEARCH_RESULTS;
import static com.redis.om.spring.util.ObjectUtils.pageFromSlice;

public class SimpleRedisEnhancedRepository<T, ID> extends SimpleKeyValueRepository<T, ID>
    implements RedisEnhancedRepository<T, ID> {

  protected final RedisModulesOperations<String> modulesOperations;
  protected final EntityInformation<T, ID> metadata;
  protected final KeyValueOperations operations;
  protected final RediSearchIndexer indexer;
  protected final MappingRedisOMConverter mappingConverter;
  protected final RedisEnhancedKeyValueAdapter enhancedKeyValueAdapter;
  protected final EntityAuditor auditor;
  protected final FeatureExtractor featureExtractor;

  private final ULIDIdentifierGenerator generator;
  private final RedisOMProperties properties;

  private final EntityStream entityStream;

  @SuppressWarnings("unchecked")
  public SimpleRedisEnhancedRepository( //
    EntityInformation<T, ID> metadata, //
    KeyValueOperations operations, //
    @Qualifier("redisModulesOperations") RedisModulesOperations<?> rmo, //
    RediSearchIndexer indexer, //
    FeatureExtractor featureExtractor, //
    RedisOMProperties properties //
  ) {
    super(metadata, operations);
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.metadata = metadata;
    this.operations = operations;
    this.indexer = indexer;
    this.mappingConverter = new MappingRedisOMConverter(null,
        new ReferenceResolverImpl(modulesOperations.getTemplate()));
    this.enhancedKeyValueAdapter = new RedisEnhancedKeyValueAdapter(rmo.getTemplate(), rmo, indexer, featureExtractor, properties);
    this.generator = ULIDIdentifierGenerator.INSTANCE;
    this.auditor = new EntityAuditor(modulesOperations.getTemplate());
    this.featureExtractor = featureExtractor;
    this.properties = properties;
    this.entityStream = new EntityStreamImpl(modulesOperations, modulesOperations.getGsonBuilder(), indexer);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterable<ID> getIds() {
    String keyspace = indexer.getKeyspaceForEntityClass(metadata.getJavaType());
    Optional<String> maybeSearchIndex = indexer.getIndexName(keyspace);
    List<ID> result = List.of();
    if (maybeSearchIndex.isPresent()) {
      SearchOperations<String> searchOps = modulesOperations.opsForSearch(maybeSearchIndex.get());
      Optional<Field> maybeIdField = ObjectUtils.getIdFieldForEntityClass(metadata.getJavaType());
      String idField = maybeIdField.map(Field::getName).orElse("id");
      
      Query query = new Query("*");
      query.limit(0, MAX_SEARCH_RESULTS);
      query.returnFields(idField);
      SearchResult searchResult = searchOps.search(query);
  
      result = (List<ID>) searchResult.getDocuments().stream() //
          .map(d -> ObjectUtils.documentToObject(d, metadata.getJavaType(), mappingConverter)) //
          .map(e -> ObjectUtils.getIdFieldForEntity(maybeIdField.get(), e)) //
          .toList();
    }

    return result;
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
    PartialUpdate<?> update = new PartialUpdate<>(metadata.getId(entity).toString(), metadata.getJavaType())
        .set(field.getSearchAlias(), value);

    enhancedKeyValueAdapter.update(update);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <F> Iterable<F> getFieldsByIds(Iterable<ID> ids, MetamodelField<T, F> field) {
    RedisTemplate<String, String> template = modulesOperations.getTemplate();
    List<String> keys = StreamSupport.stream(ids.spliterator(), false) //
        .map(this::getKey).toList();

    return (Iterable<F>) keys.stream() //
        .map(key -> template.opsForHash().get(key, field.getSearchAlias())) //
        .collect(Collectors.toList());
  }

  @Override
  public Long getExpiration(ID id) {
    RedisTemplate<String, String> template = modulesOperations.getTemplate();
    return template.getExpire(getKey(id));
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

    if (indexer.indexExistsFor(metadata.getJavaType())) {
      Optional<String> maybeSearchIndex = indexer.getIndexName(metadata.getJavaType());
      if (maybeSearchIndex.isPresent()) {
        String searchIndex = maybeSearchIndex.get();
        SearchOperations<String> searchOps = modulesOperations.opsForSearch(searchIndex);
        Query query = new Query("*");
        query.limit(Math.toIntExact(pageable.getOffset()), pageable.getPageSize());

        if (pageable.getSort() != null) {
          for (Order order : pageable.getSort()) {
            query.setSortBy(order.getProperty(), order.isAscending());
          }
        }

        SearchResult searchResult = searchOps.search(query);

        @SuppressWarnings("unchecked")
        List<T> content = (List<T>) searchResult.getDocuments().stream() //
            .map(d -> ObjectUtils.documentToObject(d, metadata.getJavaType(), mappingConverter)) //
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

  private String getKeyspace() {
    return indexer.getKeyspaceForEntityClass(metadata.getJavaType());
  }
  
  private String getKey(Object id) {
    return getKeyspace() + id.toString();
  }

  @Override
  public <S extends T> List<S> saveAll(Iterable<S> entities) {
    Assert.notNull(entities, "The given Iterable of entities must not be null!");
    List<S> saved = new ArrayList<>();

    try (Jedis jedis = modulesOperations.getClient().getJedis().get()) {
      Pipeline pipeline = jedis.pipelined();

      for (S entity : entities) {
        boolean isNew = metadata.isNew(entity);

        KeyValuePersistentEntity<?, ?> keyValueEntity = mappingConverter.getMappingContext().getRequiredPersistentEntity(ClassUtils.getUserClass(entity));
        Object id = isNew ? generator.generateIdentifierOfType(keyValueEntity.getIdProperty().getTypeInformation()) : (String) keyValueEntity.getPropertyAccessor(entity).getProperty(keyValueEntity.getIdProperty());
        keyValueEntity.getPropertyAccessor(entity).setProperty(keyValueEntity.getIdProperty(), id);

        String keyspace = keyValueEntity.getKeySpace();
        byte[] objectKey = createKey(keyspace, id.toString());

        // process entity pre-save mutation
        auditor.processEntity(entity, isNew);
        featureExtractor.processEntity(entity);

        RedisData rdo = new RedisData();
        mappingConverter.write(entity, rdo);

        pipeline.hmset(objectKey, rdo.getBucket().rawMap());

        if (expires(rdo)) {
          pipeline.expire(objectKey, rdo.getTimeToLive());
        }

        saved.add(entity);
      }
      pipeline.sync();
    }

    return saved;
  }

  public byte[] createKey(String keyspace, String id) {
    return this.mappingConverter.toBytes(keyspace + ":" + id);
  }

  private boolean expires(RedisData data) {
    return data.getTimeToLive() != null && data.getTimeToLive() > 0L;
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
}
