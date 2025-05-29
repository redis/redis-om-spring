package com.redis.om.spring.audit;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;

/**
 * Auditor for automatically setting entity audit fields like created and last modified dates.
 */
public class EntityAuditor {
  private final RedisOperations<?, ?> redisOperations;

  /**
   * Creates a new entity auditor.
   *
   * @param redisOperations the Redis operations
   */
  public EntityAuditor(RedisOperations<?, ?> redisOperations) {
    this.redisOperations = redisOperations;
  }

  /**
   * Processes entity audit fields based on whether the key exists.
   *
   * @param redisKey the Redis key
   * @param item     the entity to audit
   */
  public void processEntity(byte[] redisKey, Object item) {
    boolean isNew = (boolean) redisOperations.execute((RedisCallback<Object>) connection -> !connection.keyCommands()
        .exists(redisKey));
    processEntity(item, isNew);
  }

  /**
   * Processes entity audit fields based on whether the key exists.
   *
   * @param redisKey the Redis key
   * @param item     the entity to audit
   */
  public void processEntity(String redisKey, Object item) {
    processEntity(redisKey.getBytes(), item);
  }

  /**
   * Processes entity audit fields.
   *
   * @param item  the entity to audit
   * @param isNew whether the entity is new
   */
  public void processEntity(Object item, boolean isNew) {
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