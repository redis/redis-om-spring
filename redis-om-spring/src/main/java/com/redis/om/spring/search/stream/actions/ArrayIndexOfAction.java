package com.redis.om.spring.search.stream.actions;

import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

import redis.clients.jedis.json.Path2;

/**
 * Action class that finds the index of a specific element within a JSON array field in a Redis JSON document.
 * This class implements the {@link ToLongFunction} interface to return the zero-based index position
 * of the first occurrence of the specified element in the array.
 * 
 * <p>The index search operation uses the Redis JSON ARRINDEX command to locate elements within an array
 * stored at a specific path in a JSON document. This action is useful for determining the position of
 * elements before performing other array operations like insertion or removal.</p>
 * 
 * <p>Example usage in an entity stream:</p>
 * <pre>{@code
 * entityStream
 *   .mapToLong(new ArrayIndexOfAction<>(MyEntity$.TAGS, "searchTag"))
 *   .filter(index -> index >= 0)
 *   .forEach(System.out::println);
 * }</pre>
 * 
 * @param <E> the type of entity that contains the JSON array field to be searched
 * 
 * @see BaseAbstractAction
 * @see ToLongFunction
 * @see SearchFieldAccessor
 */
public class ArrayIndexOfAction<E> extends BaseAbstractAction implements ToLongFunction<E> {

  private final Object element;

  /**
   * Constructs a new ArrayIndexOfAction for finding the index of an element in the specified JSON array field.
   * 
   * <p>This action will search for the first occurrence of the specified element within the array
   * located at the field's JSON path. The search is performed using value equality comparison.</p>
   * 
   * @param field   the search field accessor that identifies the JSON array field to search in;
   *                must not be null and should correspond to an array field in the entity
   * @param element the element to search for within the array; can be any JSON-serializable object
   *                that is compatible with the array's element type
   * 
   * @throws NullPointerException if the entity class does not have an ID field (thrown by parent constructor)
   */
  public ArrayIndexOfAction(SearchFieldAccessor field, Object element) {
    super(field);
    this.element = element;
  }

  @Override
  public long applyAsLong(E value) {
    var result = json.arrIndex(getKey(value), Path2.of("." + field.getSearchAlias()), element);
    return result != null && !result.isEmpty() ? result.get(0) : 0;
  }
}
