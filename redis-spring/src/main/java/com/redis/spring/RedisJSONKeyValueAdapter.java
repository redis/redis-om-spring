package com.redis.spring;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.lang.Nullable;

import com.redis.spring.ops.json.JSONOperations;

public class RedisJSONKeyValueAdapter extends RedisKeyValueAdapter {
  private JSONOperations<?> redisJSONOperations;
  private RedisOperations<?, ?> redisOperations;
  
  /**
   * Creates new {@link RedisKeyValueAdapter} with default {@link RedisMappingContext} and default
   * {@link RedisCustomConversions}.
   *
   * @param redisOps must not be {@literal null}.
   */
  public RedisJSONKeyValueAdapter(RedisOperations<?, ?> redisOps, JSONOperations<?> redisJSONOperations) {
    super(redisOps, new RedisMappingContext());
    this.redisJSONOperations = redisJSONOperations;
    this.redisOperations = redisOps;
  }

  /**
   * Creates new {@link RedisKeyValueAdapter} with default {@link RedisCustomConversions}.
   *
   * @param redisOps must not be {@literal null}.
   * @param mappingContext must not be {@literal null}.
   */
  public RedisJSONKeyValueAdapter(RedisOperations<?, ?> redisOps, JSONOperations<?> redisJSONOperations, RedisMappingContext mappingContext) {
    super(redisOps, mappingContext, new RedisCustomConversions());
    this.redisJSONOperations = redisJSONOperations;
    this.redisOperations = redisOps;
  }
  
  /*
   * (non-Javadoc)
   * @see org.springframework.data.keyvalue.core.KeyValueAdapter#put(java.lang.Object, java.lang.Object, java.lang.String)
   */
  @Override
  public Object put(Object id, Object item, String keyspace) {
    System.out.println(String.format(">>>> RedisJSONKeyValueAdapter::put(%s, %s, %s", id, item, keyspace));
    @SuppressWarnings("unchecked")
    JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
    
    ops.set(getKey(keyspace, id), item);
    
    redisOperations.execute((RedisCallback<Object>) connection -> {
      connection.sAdd(toBytes(keyspace), toBytes(id));
      return null;
    });

    return item;
  }
  
  /*
   * (non-Javadoc)
   * @see org.springframework.data.keyvalue.core.KeyValueAdapter#get(java.lang.Object, java.lang.String, java.lang.Class)
   */
  @Nullable
  @Override
  public <T> T get(Object id, String keyspace, Class<T> type) {
    System.out.println(String.format(">>>> RedisJSONKeyValueAdapter::put(%s, %s, %s", id, keyspace, type));
    @SuppressWarnings("unchecked")
    JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
    return ops.get(getKey(keyspace, id), type);
  }
  
  protected String getKey(String keyspace, Object id) {
//    String idAsString = redisConverter.getConversionService().convert(id, String.class);
    return String.format("%s:%s", keyspace, id);
  }
}
