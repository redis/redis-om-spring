package com.redis.om.spring.indexing;

import java.lang.reflect.Field;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ReflectionUtils;

/**
 * Handles maintenance of lexicographic sorted sets for fields marked with lexicographic=true.
 * This component is responsible for keeping sorted sets in sync with entity changes
 * during save, update, and delete operations.
 *
 * @author Redis OM Spring Team
 * @since 1.0.0
 * @author Brian Sam-Bodden
 */
public class LexicographicIndexer {
  private static final Log logger = LogFactory.getLog(LexicographicIndexer.class);

  private final RedisTemplate<String, String> redisTemplate;
  private final RediSearchIndexer indexer;

  public LexicographicIndexer(RedisTemplate<String, String> redisTemplate, RediSearchIndexer indexer) {
    this.redisTemplate = redisTemplate;
    this.indexer = indexer;
  }

  /**
   * Process entity before save/update to maintain lexicographic sorted sets.
   * Removes old entries if updating and adds new entries.
   *
   * @param entity       the entity being saved
   * @param entityId     the entity ID
   * @param isNew        whether this is a new entity
   * @param entityPrefix the Redis key prefix for the entity type
   */
  public void processEntity(Object entity, String entityId, boolean isNew, String entityPrefix) {
    Class<?> entityClass = entity.getClass();
    Set<String> lexicographicFields = indexer.getLexicographicFields(entityClass);

    logger.debug(String.format("Processing entity %s with ID %s, isNew=%s, entityPrefix=%s", entityClass
        .getSimpleName(), entityId, isNew, entityPrefix));
    logger.debug(String.format("Lexicographic fields: %s", lexicographicFields));

    if (lexicographicFields == null || lexicographicFields.isEmpty()) {
      logger.debug("No lexicographic fields found, skipping processing");
      return;
    }

    for (String fieldName : lexicographicFields) {
      Field field = ReflectionUtils.findField(entityClass, fieldName);
      if (field == null) {
        logger.warn(String.format("Lexicographic field %s not found on class %s", fieldName, entityClass.getName()));
        continue;
      }

      field.setAccessible(true);
      Object fieldValue = ReflectionUtils.getField(field, entity);

      if (fieldValue != null) {
        String sortedSetKey = entityPrefix + fieldName + ":lex";
        String member = fieldValue.toString() + "#" + entityId;

        logger.debug(String.format("Processing field %s, value=%s, member=%s, isNew=%s", fieldName, fieldValue, member,
            isNew));

        // If updating, remove the old entry (we don't know the old value, so we need to find and remove it)
        if (!isNew) {
          removeOldEntry(sortedSetKey, entityId);
        }

        // Add the new entry with score 0 (all entries have same score for lexicographic ordering)
        Boolean added = redisTemplate.opsForZSet().add(sortedSetKey, member, 0.0);
        logger.debug(String.format("Added entry %s to sorted set %s (result: %s)", member, sortedSetKey, added));
      }
    }
  }

  /**
   * Process entity deletion to remove entries from lexicographic sorted sets.
   *
   * @param entity       the entity being deleted
   * @param entityId     the entity ID
   * @param entityPrefix the Redis key prefix for the entity type
   */
  public void processEntityDeletion(Object entity, String entityId, String entityPrefix) {
    Class<?> entityClass = entity.getClass();
    Set<String> lexicographicFields = indexer.getLexicographicFields(entityClass);

    if (lexicographicFields == null || lexicographicFields.isEmpty()) {
      return;
    }

    for (String fieldName : lexicographicFields) {
      String sortedSetKey = entityPrefix + fieldName + ":lex";
      removeOldEntry(sortedSetKey, entityId);
    }
  }

  /**
   * Process entity deletion by ID when entity is not available.
   *
   * @param entityClass  the entity class
   * @param entityId     the entity ID
   * @param entityPrefix the Redis key prefix for the entity type
   */
  public void processEntityDeletionById(Class<?> entityClass, String entityId, String entityPrefix) {
    Set<String> lexicographicFields = indexer.getLexicographicFields(entityClass);

    if (lexicographicFields == null || lexicographicFields.isEmpty()) {
      return;
    }

    for (String fieldName : lexicographicFields) {
      String sortedSetKey = entityPrefix + fieldName + ":lex";
      removeOldEntry(sortedSetKey, entityId);
    }
  }

  /**
   * Removes entries ending with the given entity ID from the sorted set.
   * This is needed because we may not know the old field value during updates.
   *
   * @param sortedSetKey the sorted set key
   * @param entityId     the entity ID to remove
   */
  private void removeOldEntry(String sortedSetKey, String entityId) {
    // Get all members and remove those ending with #entityId
    Set<String> members = redisTemplate.opsForZSet().range(sortedSetKey, 0, -1);
    logger.debug(String.format("Removing old entries for entityId %s from %s", entityId, sortedSetKey));
    logger.debug(String.format("Current members: %s", members));
    if (members != null) {
      String suffix = "#" + entityId;
      logger.debug(String.format("Looking for entries ending with: %s", suffix));
      for (String member : members) {
        if (member.endsWith(suffix)) {
          Long removed = redisTemplate.opsForZSet().remove(sortedSetKey, member);
          logger.debug(String.format("Removed entry %s from sorted set %s (result: %s)", member, sortedSetKey,
              removed));
        }
      }
    }
  }
}