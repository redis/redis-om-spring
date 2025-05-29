package com.redis.om.spring.search.stream.actions;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

import redis.clients.jedis.json.Path2;

/**
 * Action class that trims a JSON array field to retain only elements within a specified range of indices.
 * This class implements the {@link Consumer} interface to perform trim operations on Redis JSON arrays
 * through Redis OM entity streams.
 * 
 * <p>The trim operation uses the Redis JSON ARRTRIM command to remove elements outside of the specified
 * start and end indices, effectively resizing the array to contain only the elements within the given range.
 * This action is useful for maintaining array size limits or extracting specific subsequences from arrays.</p>
 * 
 * <p>Example usage in an entity stream:</p>
 * <pre>{@code
 * entityStream
 *   .filter(entity -> entity.getTags().size() > 10)
 *   .forEach(new ArrayTrimAction<>(MyEntity$.TAGS, 0, 9));  // Keep first 10 elements
 * }</pre>
 * 
 * @param <E> the type of entity that contains the JSON array field to be trimmed
 * 
 * @see BaseAbstractAction
 * @see Consumer
 * @see SearchFieldAccessor
 */
public class ArrayTrimAction<E> extends BaseAbstractAction implements Consumer<E> {

  private final Integer begin;
  private final Integer end;

  /**
   * Constructs a new ArrayTrimAction for trimming the specified JSON array field to a specific range.
   * 
   * <p>This action will trim the array located at the field's JSON path to retain only elements
   * between the specified start and end indices (inclusive). Elements outside this range are removed.</p>
   * 
   * @param field the search field accessor that identifies the JSON array field to trim;
   *              must not be null and should correspond to an array field in the entity
   * @param begin the starting index (inclusive) for the range of elements to retain;
   *              must be a non-negative integer and less than or equal to the end index
   * @param end   the ending index (inclusive) for the range of elements to retain;
   *              must be greater than or equal to the begin index and within array bounds
   * 
   * @throws NullPointerException if the entity class does not have an ID field (thrown by parent constructor)
   */
  public ArrayTrimAction(SearchFieldAccessor field, Integer begin, Integer end) {
    super(field);
    this.begin = begin;
    this.end = end;
  }

  @Override
  public void accept(E entity) {
    json.arrTrim(getKey(entity), Path2.of("." + field.getSearchAlias()), begin, end);
  }
}
