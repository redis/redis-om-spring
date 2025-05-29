package com.redis.om.spring.search.stream.actions;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.json.Path2;

/**
 * Action class that removes and returns an element at a specific index from a JSON array field within a Redis JSON
 * document.
 * This class implements the {@link Function} interface to perform pop operations on Redis JSON arrays and return the
 * removed element through Redis OM entity streams.
 * 
 * <p>The pop operation uses the Redis JSON ARRPOP command to remove elements from a specified position within an array
 * stored at a specific path in a JSON document. The removed element is returned to the caller, making this operation
 * useful for extracting and processing array elements in a destructive manner.</p>
 * 
 * <p>Example usage in an entity stream:</p>
 * <pre>{@code
 * entityStream
 *   .map(new ArrayPopAction<>(MyEntity$.TAGS, 0))  // Pop first element
 *   .filter(Objects::nonNull)
 *   .forEach(System.out::println);
 * }</pre>
 * 
 * @param <E> the type of entity that contains the JSON array field to be modified
 * @param <R> the type of elements contained in the array being popped
 * 
 * @see BaseAbstractAction
 * @see Function
 * @see SearchFieldAccessor
 */
public class ArrayPopAction<E, R> extends BaseAbstractAction implements Function<E, R> {

  private final Integer index;

  /**
   * Constructs a new ArrayPopAction for removing an element at a specific position from the specified JSON array field.
   * 
   * <p>This action will remove and return the element at the specified index within the array located at the
   * field's JSON path. The array size is reduced by one, and elements after the removed position shift down
   * to fill the gap.</p>
   * 
   * @param field the search field accessor that identifies the JSON array field to pop from;
   *              must not be null and should correspond to an array field in the entity
   * @param index the zero-based index position of the element to remove and return; must be a valid
   *              index within the array bounds (0 to array length - 1)
   * 
   * @throws NullPointerException if the entity class does not have an ID field (thrown by parent constructor)
   */
  public ArrayPopAction(SearchFieldAccessor field, Integer index) {
    super(field);
    this.index = index;
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public R apply(E entity) {
    Field f = field.getField();
    Optional<Class<?>> maybeClass = ObjectUtils.getCollectionElementClass(f);
    if (maybeClass.isPresent()) {
      var popResult = json.arrPop(getKey(entity), maybeClass.get(), Path2.of("." + f.getName()), index);
      return popResult != null && !popResult.isEmpty() ? (R) popResult.get(0) : null;
    } else {
      throw new RuntimeException("Cannot determine contained element type for collection " + f.getName());
    }
  }
}
