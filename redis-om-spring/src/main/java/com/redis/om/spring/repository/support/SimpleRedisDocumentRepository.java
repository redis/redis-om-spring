package com.redis.om.spring.repository.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.repository.core.EntityInformation;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redislabs.modules.rejson.Path;

public class SimpleRedisDocumentRepository<T, ID> extends SimpleKeyValueRepository<T, ID> implements RedisDocumentRepository<T, ID> {
  
  protected RedisModulesOperations<String> modulesOperations;
  protected EntityInformation<T, ID> metadata;
  protected KeyValueOperations operations;

  @SuppressWarnings("unchecked")
  public SimpleRedisDocumentRepository(EntityInformation<T, ID> metadata, KeyValueOperations operations, @Qualifier("redisModulesOperations") RedisModulesOperations<?> rmo) {
    super(metadata, operations);
    this.modulesOperations = (RedisModulesOperations<String>)rmo;
    this.metadata = metadata;
    this.operations = operations;
  }

  @Override
  public Iterable<ID> getIds() {
    @SuppressWarnings("unchecked")
    RedisTemplate<String,ID> template = (RedisTemplate<String,ID>)modulesOperations.getTemplate();
    SetOperations<String, ID> setOps = template.opsForSet();
    return new ArrayList<ID>(setOps.members(metadata.getJavaType().getName()));
  }

  @Override
  public Page<ID> getIds(Pageable pageable) {
    @SuppressWarnings("unchecked")
    RedisTemplate<String,ID> template = (RedisTemplate<String,ID>)modulesOperations.getTemplate();
    SetOperations<String, ID> setOps = template.opsForSet();
    List<ID> ids = new ArrayList<ID>(setOps.members(metadata.getJavaType().getName()));

    int fromIndex = Long.valueOf(pageable.getOffset()).intValue();
    int toIndex = fromIndex + pageable.getPageSize();
    
    return new PageImpl<ID>((List<ID>) ids.subList(fromIndex, toIndex), pageable, ids.size());
  }

  @Override
  public void deleteById(ID id, Path path) {
    Long deletedCount = modulesOperations.opsForJSON().del(metadata.getJavaType().getName() + ":" + id.toString(), path);
    
    if ((deletedCount > 0) && path.equals(Path.ROOT_PATH)) {
      @SuppressWarnings("unchecked")
      RedisTemplate<String,ID> template = (RedisTemplate<String,ID>)modulesOperations.getTemplate();
      SetOperations<String, ID> setOps = template.opsForSet();
      setOps.remove(metadata.getJavaType().getName(), id);
    }
  }

  @Override
  public void updateField(T entity, MetamodelField<T, ?> field, Object value) {
    modulesOperations.opsForJSON().set(metadata.getJavaType().getName() + ":" + metadata.getId(entity).toString(), value, Path.of("$." + field.getField().getName()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <F> Iterable<F> getFieldsByIds(Iterable<ID> ids, MetamodelField<T, F> field) {
    String[] keys = StreamSupport.stream(ids.spliterator(), false).map(id -> metadata.getJavaType().getName() + ":" + id).toArray(String[]::new);
    return (Iterable<F>) modulesOperations.opsForJSON().mget(Path.of("$." + field.getField().getName()), List.class, keys).stream().flatMap(List::stream).collect(Collectors.toList());
  }

  @Override
  public Long getExpiration(ID id) {
    @SuppressWarnings("unchecked")
    RedisTemplate<String, String> template = (RedisTemplate<String, String>) modulesOperations.getTemplate();
    return template.getExpire(metadata.getJavaType().getName() + ":" + id.toString());
  }

}
