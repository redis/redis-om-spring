package com.redis.om.spring.search.stream.actions;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

import redis.clients.jedis.json.Path2;

/**
 * Action that increments a numeric field in a Redis JSON document by a specified value.
 * This action is used within the Entity Streams API to modify numeric values
 * stored in JSON documents without requiring a full entity reload.
 * 
 * <p>The NumIncrByAction performs an atomic increment operation on the specified
 * numeric field using Redis's JSON.NUMINCRBY command. This is more efficient
 * than retrieving the document, modifying the value, and saving it back.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * entityStream.filter(Person$.AGE.eq(25))
 *            .forEach(NumIncrByAction.of(Person$.SCORE, 10L));
 * }</pre>
 * 
 * @param <E> the entity type that this action operates on
 * @since 1.0
 * @see BaseAbstractAction
 * @see redis.clients.jedis.json.JsonProtocol.JsonCommand#NUMINCRBY
 */
public class NumIncrByAction<E> extends BaseAbstractAction implements Consumer<E> {
  /** The value to increment the numeric field by */
  private final Long value;

  /**
   * Constructs a new NumIncrByAction that will increment the specified field by the given value.
   * 
   * @param field the search field accessor identifying the numeric field to increment
   * @param value the amount to increment the field by (can be negative for decrement)
   */
  public NumIncrByAction(SearchFieldAccessor field, Long value) {
    super(field);
    this.value = value;
  }

  /**
   * Applies the numeric increment operation to the specified entity.
   * This method executes a Redis JSON.NUMINCRBY command to atomically
   * increment the target field by the configured value.
   * 
   * @param entity the entity whose numeric field should be incremented
   */
  @Override
  public void accept(E entity) {
    json.numIncrBy(getKey(entity), Path2.of("." + field.getSearchAlias()), value);
  }

}
