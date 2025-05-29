package com.redis.om.spring.search.stream.actions;

import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

import redis.clients.jedis.json.Path2;

/**
 * Action class that retrieves the length (number of elements) of a JSON array field within a Redis JSON document.
 * This class implements the {@link ToLongFunction} interface to return the count of elements in the specified
 * array field as a long value.
 * 
 * <p>The length operation uses the Redis JSON ARRLEN command to determine the size of an array stored at a
 * specific path in a JSON document. This action is useful for validation, filtering, or conditional processing
 * based on array sizes in entity streams.</p>
 * 
 * <p>Example usage in an entity stream:</p>
 * <pre>{@code
 * entityStream
 *   .mapToLong(new ArrayLengthAction<>(MyEntity$.TAGS))
 *   .filter(length -> length > 5)
 *   .forEach(System.out::println);
 * }</pre>
 * 
 * @param <E> the type of entity that contains the JSON array field to measure
 * 
 * @see BaseAbstractAction
 * @see ToLongFunction
 * @see SearchFieldAccessor
 */
public class ArrayLengthAction<E> extends BaseAbstractAction implements ToLongFunction<E> {

  /**
   * Constructs a new ArrayLengthAction for determining the length of the specified JSON array field.
   * 
   * <p>This action will return the number of elements present in the array located at the field's
   * JSON path within Redis JSON documents. If the array does not exist or the path is invalid,
   * the action returns 0.</p>
   * 
   * @param field the search field accessor that identifies the JSON array field to measure;
   *              must not be null and should correspond to an array field in the entity
   * 
   * @throws NullPointerException if the entity class does not have an ID field (thrown by parent constructor)
   */
  public ArrayLengthAction(SearchFieldAccessor field) {
    super(field);
  }

  @Override
  public long applyAsLong(E value) {
    var result = json.arrLen(getKey(value), Path2.of("." + field.getSearchAlias()));
    return result != null && !result.isEmpty() ? result.get(0) : 0;
  }

}
