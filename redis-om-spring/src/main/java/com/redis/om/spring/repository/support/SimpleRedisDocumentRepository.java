package com.redis.om.spring.repository.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.repository.core.EntityInformation;

import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redislabs.modules.rejson.Path;

public class SimpleRedisDocumentRepository<T, ID> extends SimpleKeyValueRepository<T, ID> implements RedisDocumentRepository<T, ID> {
  
  protected RedisModulesOperations<String, String> modulesOperations;
  protected EntityInformation<T, ID> metadata;

  @SuppressWarnings("unchecked")
  public SimpleRedisDocumentRepository(EntityInformation<T, ID> metadata, KeyValueOperations operations, RedisModulesOperations<?, ?> rmo) {
    super(metadata, operations);
    this.modulesOperations = (RedisModulesOperations<String, String>)rmo;
    this.metadata = metadata;
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
    modulesOperations.opsForJSON().del(metadata.getJavaType().getName() + ":" + id.toString(), path);
  }

}
