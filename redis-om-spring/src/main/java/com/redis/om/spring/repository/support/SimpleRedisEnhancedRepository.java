package com.redis.om.spring.repository.support;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.google.common.collect.Lists;
import com.redis.om.spring.RediSearchIndexer;
import com.redis.om.spring.RedisEnhancedKeyValueAdapter;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.id.ULIDIdentifierGenerator;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.RedisEnhancedRepository;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class SimpleRedisEnhancedRepository<T, ID> extends SimpleKeyValueRepository<T, ID>
    implements RedisEnhancedRepository<T, ID> {
  
  private static final Integer MAX_LIMIT = 10000;

  protected final RedisModulesOperations<String> modulesOperations;
  protected final EntityInformation<T, ID> metadata;
  protected final KeyValueOperations operations;
  protected final RediSearchIndexer indexer;
  protected final MappingRedisOMConverter mappingConverter;
  protected final RedisEnhancedKeyValueAdapter enhancedKeyValueAdapter;

  private final ULIDIdentifierGenerator generator;

  @SuppressWarnings("unchecked")
  public SimpleRedisEnhancedRepository(EntityInformation<T, ID> metadata, //
      KeyValueOperations operations, //
      @Qualifier("redisModulesOperations") RedisModulesOperations<?> rmo, //
      RediSearchIndexer keyspaceToIndexMap) {
    super(metadata, operations);
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.metadata = metadata;
    this.operations = operations;
    this.indexer = keyspaceToIndexMap;
    this.mappingConverter = new MappingRedisOMConverter(null,
        new ReferenceResolverImpl(modulesOperations.getTemplate()));
    this.enhancedKeyValueAdapter = new RedisEnhancedKeyValueAdapter(rmo.getTemplate(), rmo, keyspaceToIndexMap);
    this.generator = ULIDIdentifierGenerator.INSTANCE;
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
      query.returnFields(idField);
      SearchResult searchResult = searchOps.search(query);
  
      result = (List<ID>) searchResult.getDocuments().stream()
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
    
    Pageable pageRequest = PageRequest.of(0, MAX_LIMIT, sort);

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

        processAuditAnnotations(objectKey, entity, isNew);

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

  private void processAuditAnnotations(byte[] redisKey, Object item, boolean isNew) {
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
}
