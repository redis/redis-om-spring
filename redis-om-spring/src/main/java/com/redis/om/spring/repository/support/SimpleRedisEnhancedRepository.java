package com.redis.om.spring.repository.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.keyvalue.core.IterableConverter;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.redis.core.PartialUpdate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.util.Assert;

import com.redis.om.spring.KeyspaceToIndexMap;
import com.redis.om.spring.RedisEnhancedKeyValueAdapter;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.RedisEnhancedRepository;
import com.redis.om.spring.util.ObjectUtils;

import io.redisearch.Query;
import io.redisearch.SearchResult;

public class SimpleRedisEnhancedRepository<T, ID> extends SimpleKeyValueRepository<T, ID>
    implements RedisEnhancedRepository<T, ID> {
  
  private static final Integer MAX_LIMIT = 10000;

  protected RedisModulesOperations<String> modulesOperations;
  protected EntityInformation<T, ID> metadata;
  protected KeyValueOperations operations;
  protected KeyspaceToIndexMap keyspaceToIndexMap;
  protected MappingRedisOMConverter mappingConverter;
  protected RedisEnhancedKeyValueAdapter enhancedKeyValueAdapter;

  @SuppressWarnings("unchecked")
  public SimpleRedisEnhancedRepository(EntityInformation<T, ID> metadata, //
      KeyValueOperations operations, //
      @Qualifier("redisModulesOperations") RedisModulesOperations<?> rmo, //
      KeyspaceToIndexMap keyspaceToIndexMap) {
    super(metadata, operations);
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.metadata = metadata;
    this.operations = operations;
    this.keyspaceToIndexMap = keyspaceToIndexMap;
    this.mappingConverter = new MappingRedisOMConverter(null,
        new ReferenceResolverImpl(modulesOperations.getTemplate()));
    this.enhancedKeyValueAdapter = new RedisEnhancedKeyValueAdapter(rmo.getTemplate(), rmo, keyspaceToIndexMap);
  }

  @Override
  public Iterable<ID> getIds() {
    @SuppressWarnings("unchecked")
    RedisTemplate<String, ID> template = (RedisTemplate<String, ID>) modulesOperations.getTemplate();
    SetOperations<String, ID> setOps = template.opsForSet();
    return new ArrayList<ID>(setOps.members(metadata.getJavaType().getName()));
  }

  @Override
  public Page<ID> getIds(Pageable pageable) {
    @SuppressWarnings("unchecked")
    RedisTemplate<String, ID> template = (RedisTemplate<String, ID>) modulesOperations.getTemplate();
    SetOperations<String, ID> setOps = template.opsForSet();
    List<ID> ids = new ArrayList<ID>(setOps.members(metadata.getJavaType().getName()));

    int fromIndex = Long.valueOf(pageable.getOffset()).intValue();
    int toIndex = fromIndex + pageable.getPageSize();

    return new PageImpl<ID>((List<ID>) ids.subList(fromIndex, toIndex), pageable, ids.size());
  }

  @Override
  public void updateField(T entity, MetamodelField<T, ?> field, Object value) {
    PartialUpdate<?> update = new PartialUpdate<>(metadata.getId(entity).toString(), metadata.getJavaType())
        .set(field.getField().getName(), value);

    enhancedKeyValueAdapter.update(update);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <F> Iterable<F> getFieldsByIds(Iterable<ID> ids, MetamodelField<T, F> field) {
    RedisTemplate<String, String> template = (RedisTemplate<String, String>) modulesOperations.getTemplate();
    List<String> keys = StreamSupport.stream(ids.spliterator(), false) //
        .map(id -> getKey(id)).collect(Collectors.toList());

    return (Iterable<F>) keys.stream() //
        .map(key -> template.opsForHash().get(key, field.getField().getName())) //
        .collect(Collectors.toList());
  }

  @Override
  public Long getExpiration(ID id) {
    @SuppressWarnings("unchecked")
    RedisTemplate<String, String> template = (RedisTemplate<String, String>) modulesOperations.getTemplate();
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
  public Iterable<T> findAll(Sort sort) {

    Assert.notNull(sort, "Sort must not be null!");
    
    Pageable pageRequest = PageRequest.of(0, MAX_LIMIT, sort);

    return findAll(pageRequest);
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

    if (keyspaceToIndexMap.indexExistsFor(metadata.getJavaType())) {
      String searchIndex = keyspaceToIndexMap.getIndexName(metadata.getJavaType()).get();
      SearchOperations<String> searchOps = modulesOperations.opsForSearch(searchIndex);
      Query query = new Query("*");
      query.limit(Long.valueOf(pageable.getOffset()).intValue(), pageable.getPageSize());

      if (pageable.getSort() != null) {
        for (Order order : pageable.getSort()) {
          query.setSortBy(order.getProperty(), order.isAscending());
        }
      }

      SearchResult searchResult = searchOps.search(query);

      @SuppressWarnings("unchecked")
      List<T> content = (List<T>) searchResult.docs.stream() //
          .map(d -> ObjectUtils.documentToObject(d, metadata.getJavaType(), mappingConverter)) //
          .collect(Collectors.toList());

      return new PageImpl<>(content, pageable, searchResult.totalResults);
    } else {
      Iterable<T> content = operations.findInRange(pageable.getOffset(), pageable.getPageSize(), pageable.getSort(),
          metadata.getJavaType());

      return new PageImpl<>(IterableConverter.toList(content), pageable, this.operations.count(metadata.getJavaType()));
    }
  }

  private String getKeyspace() {
    return keyspaceToIndexMap.getKeyspaceForEntityClass(metadata.getJavaType());
  }
  
  private String getKey(Object id) {
    return getKeyspace() + id.toString();
  }

}
