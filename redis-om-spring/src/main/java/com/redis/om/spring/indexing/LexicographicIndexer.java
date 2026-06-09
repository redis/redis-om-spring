package com.redis.om.spring.indexing;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.ReflectionUtils;

import com.redis.om.spring.util.ObjectUtils;

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

  /**
   * Creates a new LexicographicIndexer with the specified dependencies.
   * 
   * @param redisTemplate the Redis template for executing Redis operations
   * @param indexer       the RediSearch indexer for accessing field metadata
   */
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
    List<Field> idFields = ObjectUtils.getIdFieldsForEntityClass(entityClass);

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

        // If updating, remove the previous member before adding the current one.
        if (!isNew) {
          removeOldEntry(sortedSetKey, entityId, field, fieldValue, idFields);
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
    List<Field> idFields = ObjectUtils.getIdFieldsForEntityClass(entityClass);

    if (lexicographicFields == null || lexicographicFields.isEmpty()) {
      return;
    }

    for (String fieldName : lexicographicFields) {
      String sortedSetKey = entityPrefix + fieldName + ":lex";
      Field field = ReflectionUtils.findField(entityClass, fieldName);
      if (field == null) {
        logger.warn(String.format("Lexicographic field %s not found on class %s", fieldName, entityClass.getName()));
        removeOldEntryByScan(sortedSetKey, entityId);
        continue;
      }

      field.setAccessible(true);
      Object fieldValue = ReflectionUtils.getField(field, entity);
      if (isIdField(field, idFields) && fieldValue != null) {
        removeExactEntry(sortedSetKey, member(fieldValue, entityId));
      } else {
        removeOldEntryByScan(sortedSetKey, entityId);
      }
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
    Optional<Field> singleIdField = getSingleIdField(entityClass);

    if (lexicographicFields == null || lexicographicFields.isEmpty()) {
      return;
    }

    for (String fieldName : lexicographicFields) {
      String sortedSetKey = entityPrefix + fieldName + ":lex";
      if (singleIdField.map(idField -> idField.getName().equals(fieldName)).orElse(false)) {
        removeExactEntry(sortedSetKey, member(entityId, entityId));
      } else {
        removeOldEntryByScan(sortedSetKey, entityId);
      }
    }
  }

  /**
   * Removes the old lexicographic member for an update.
   * <p>
   * For ID fields the old member is deterministic, because the ID value is stable for
   * the entity being updated. For other mutable lexicographic fields, the previous
   * value may be unknown, so the fallback uses a cursor scan to avoid materializing the
   * entire sorted set in JVM heap.
   *
   * @param sortedSetKey the sorted set key
   * @param entityId     the entity ID to remove
   * @param field        the lexicographic field
   * @param fieldValue   the current field value
   * @param idFields     ID fields declared by the entity
   */
  private void removeOldEntry(String sortedSetKey, String entityId, Field field, Object fieldValue,
      List<Field> idFields) {
    if (isIdField(field, idFields) && fieldValue != null) {
      removeExactEntry(sortedSetKey, member(fieldValue, entityId));
    } else {
      removeOldEntryByScan(sortedSetKey, entityId);
    }
  }

  /**
   * Removes entries ending with the given entity ID from the sorted set using a cursor scan.
   * This is needed because we may not know the old field value during updates.
   *
   * @param sortedSetKey the sorted set key
   * @param entityId     the entity ID to remove
   */
  private void removeOldEntryByScan(String sortedSetKey, String entityId) {
    logger.debug(String.format("Removing old entries for entityId %s from %s", entityId, sortedSetKey));
    String suffix = "#" + entityId;
    logger.debug(String.format("Looking for entries ending with: %s", suffix));

    ScanOptions options = ScanOptions.scanOptions().match("*" + escapeRedisGlob(suffix)).count(1000).build();
    try (Cursor<ZSetOperations.TypedTuple<String>> cursor = redisTemplate.opsForZSet().scan(sortedSetKey, options)) {
      while (cursor.hasNext()) {
        String member = cursor.next().getValue();
        if (member != null && member.endsWith(suffix)) {
          removeExactEntry(sortedSetKey, member);
        }
      }
    }
  }

  private void removeExactEntry(String sortedSetKey, String member) {
    Long removed = redisTemplate.opsForZSet().remove(sortedSetKey, member);
    logger.debug(String.format("Removed entry %s from sorted set %s (result: %s)", member, sortedSetKey, removed));
  }

  private String member(Object fieldValue, String entityId) {
    return fieldValue + "#" + entityId;
  }

  private boolean isIdField(Field field, List<Field> idFields) {
    return idFields.stream().anyMatch(idField -> idField.getName().equals(field.getName()));
  }

  private Optional<Field> getSingleIdField(Class<?> entityClass) {
    List<Field> idFields = ObjectUtils.getIdFieldsForEntityClass(entityClass);
    return idFields.size() == 1 ? Optional.of(idFields.get(0)) : Optional.empty();
  }

  private String escapeRedisGlob(String value) {
    StringBuilder escaped = new StringBuilder(value.length());
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '*' || c == '?' || c == '[' || c == ']' || c == '\\') {
        escaped.append('\\');
      }
      escaped.append(c);
    }
    return escaped.toString();
  }
}
