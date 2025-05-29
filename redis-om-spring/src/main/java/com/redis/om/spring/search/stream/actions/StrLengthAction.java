package com.redis.om.spring.search.stream.actions;

import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

import redis.clients.jedis.json.Path2;

/**
 * Action that retrieves the length of a string field in a Redis JSON document.
 * This action is used within the Entity Streams API to obtain string lengths
 * without requiring a full entity reload.
 * 
 * <p>The StrLengthAction performs a string length query operation on the specified
 * string field using Redis's JSON.STRLEN command. This is more efficient than
 * retrieving the entire document and measuring the string length programmatically.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * long totalDescriptionLength = entityStream
 *     .filter(Product$.CATEGORY.eq("electronics"))
 *     .mapToLong(new StrLengthAction<>(Product$.DESCRIPTION))
 *     .sum();
 * }</pre>
 * 
 * @param <E> the entity type that this action operates on
 * @since 1.0
 * @see BaseAbstractAction
 * @see ToLongFunction
 * @see redis.clients.jedis.json.JsonProtocol.JsonCommand#STRLEN
 */
public class StrLengthAction<E> extends BaseAbstractAction implements ToLongFunction<E> {

  /**
   * Constructs a new StrLengthAction that will measure the length of the specified string field.
   * 
   * @param field the search field accessor identifying the string field to measure
   */
  public StrLengthAction(SearchFieldAccessor field) {
    super(field);
  }

  /**
   * Applies the string length operation to the specified entity and returns the result.
   * This method executes a Redis JSON.STRLEN command to get the length of the
   * target string field.
   * 
   * @param value the entity whose string field length should be measured
   * @return the length of the string field, or 0 if the field is null or empty
   */
  @Override
  public long applyAsLong(E value) {
    var result = json.strLen(getKey(value), Path2.of("." + field.getSearchAlias()));
    return result != null && !result.isEmpty() ? result.get(0) : 0;
  }

}
