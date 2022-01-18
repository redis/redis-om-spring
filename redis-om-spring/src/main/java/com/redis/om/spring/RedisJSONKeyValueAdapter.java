package com.redis.om.spring;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.lang.Nullable;

import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;

public class RedisJSONKeyValueAdapter extends RedisKeyValueAdapter {
  private static final Log logger = LogFactory.getLog(RedisJSONKeyValueAdapter.class);
  private JSONOperations<?> redisJSONOperations;
  private RedisOperations<?, ?> redisOperations;
//  private StringRedisSerializer stringSerializer = new StringRedisSerializer();

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
    logger.debug(String.format("%s, %s, %s", id, item, keyspace));
    @SuppressWarnings("unchecked")
    JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
    
    String key = getKey(keyspace, id);
    
    processAuditAnnotations(key, item);

    ops.set(key, item);

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
    logger.debug(String.format("%s, %s, %s", id, keyspace, type));
    @SuppressWarnings("unchecked")
    JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
    return ops.get(getKey(keyspace, id), type);
  }
  
  @Nullable
  public <T> T get(String key, Class<T> type) {
    @SuppressWarnings("unchecked")
    JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
    return ops.get(key, type);
  }

  /**
   * Get all elements for given keyspace.
   *
   * @param keyspace the keyspace to fetch entities from.
   * @param type the desired target type.
   * @param offset index value to start reading.
   * @param rows maximum number or entities to return.
   * @param <T>
   * @return never {@literal null}.
   * @since 2.5
   */
  @Override
  public <T> List<T> getAllOf(String keyspace, Class<T> type, long offset, int rows) {
    @SuppressWarnings("unchecked")
    JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;

    byte[] binKeyspace = toBytes(keyspace);
    Set<byte[]> ids = redisOperations.execute((RedisCallback<Set<byte[]>>) connection -> connection.sMembers(binKeyspace));

    String[] keys = ids.stream().map(b -> getKey(keyspace, new String(b, StandardCharsets.UTF_8))).toArray(String[]::new);

    if ((keys.length == 0) || (keys.length < offset)) {
      return Collections.emptyList();
    }

    offset = Math.max(0, offset);
    if (rows > 0) {
      keys = Arrays.copyOfRange(keys, (int)offset, Math.min((int) offset + rows, keys.length));
    }

    return ops.mget(type, keys);
  }

  public List<String> getAllKeysOf(String keyspace, long offset, int rows) {
    byte[] binKeyspace = toBytes(keyspace);
    Set<byte[]> ids = redisOperations.execute((RedisCallback<Set<byte[]>>) connection -> connection.sMembers(binKeyspace));

    List<String> keys = ids.stream().map(b -> getKey(keyspace, new String(b, StandardCharsets.UTF_8))).collect(Collectors.toList());

    if (keys.isEmpty() || keys.size() < offset) {
      return Collections.emptyList();
    }

    offset = Math.max(0, offset);
    if (rows > 0) {
      keys = keys.subList((int) offset, Math.min((int) offset + rows, keys.size()));
    }

    return keys;
  }
  
  private void processAuditAnnotations(String key, Object item) {
    boolean isNew = (boolean) redisOperations.execute((RedisCallback<Object>) connection -> {
      return !connection.exists(toBytes(key));
    });
    
    var auditClass = isNew ? CreatedDate.class : LastModifiedDate.class;
    
    List<Field> fields = ObjectUtils.getFieldsWithAnnotation(item.getClass(), auditClass);
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

  protected String getKey(String keyspace, Object id) {
    return String.format("%s:%s", keyspace, id);
  }
}
