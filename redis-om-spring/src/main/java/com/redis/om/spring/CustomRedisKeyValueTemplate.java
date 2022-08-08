package com.redis.om.spring;

import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.core.convert.RedisConverter;
import org.springframework.data.redis.core.mapping.RedisMappingContext;

import com.redis.om.spring.id.ULIDIdentifierGenerator;

public class CustomRedisKeyValueTemplate extends KeyValueTemplate {

  private final RedisKeyValueAdapter adapter;

  /**
   * Create new {@link RedisKeyValueTemplate}.
   *
   * @param adapter        must not be {@literal null}.
   * @param mappingContext must not be {@literal null}.
   */
  public CustomRedisKeyValueTemplate(RedisKeyValueAdapter adapter, RedisMappingContext mappingContext) {
    super(adapter, mappingContext, ULIDIdentifierGenerator.INSTANCE);
    this.adapter = adapter;
  }

  /**
   * Obtain the underlying redis specific
   * {@link org.springframework.data.convert.EntityConverter}.
   * @return the EntityConverter
   */
  public RedisConverter getConverter() {
    return adapter.getConverter();
  }
  
  /**
   * Obtain the underlying redis specific
   * {@link org.springframework.data.redis.core.RedisKeyValueAdapter}.
   * @return the KeyValueAdapter
   */
  public RedisKeyValueAdapter getAdapter() {
    return adapter;
  }

  /* (non-Javadoc)
   * 
   * @see
   * org.springframework.data.keyvalue.core.KeyValueTemplate#getMappingContext(
   * ) */
  @Override
  public RedisMappingContext getMappingContext() {
    return (RedisMappingContext) super.getMappingContext();
  }

}
