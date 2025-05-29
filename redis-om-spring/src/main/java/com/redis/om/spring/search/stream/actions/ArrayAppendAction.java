package com.redis.om.spring.search.stream.actions;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

import redis.clients.jedis.json.Path2;

/**
 * Action class that appends a value to the end of a JSON array field within a Redis JSON document.
 * This class implements the {@link Consumer} interface to perform append operations on Redis JSON arrays
 * through Redis OM entity streams.
 * 
 * <p>The append operation uses the Redis JSON ARRAPPEND command to add elements to the end of an array
 * stored at a specific path in a JSON document. This action is commonly used in stream processing
 * workflows where array fields need to be extended with new values.</p>
 * 
 * <p>Example usage in an entity stream:</p>
 * <pre>{@code
 * entityStream
 *   .collect(Collectors.groupingBy(Entity::getSomeField))
 *   .forEach((key, entities) -> {
 *     entities.forEach(new ArrayAppendAction<>(MyEntity$.TAGS, "newTag"));
 * });
 * }</pre>
 * 
 * @param <E> the type of entity that contains the JSON array field to be modified
 * 
 * @see BaseAbstractAction
 * @see Consumer
 * @see SearchFieldAccessor
 */
public class ArrayAppendAction<E> extends BaseAbstractAction implements Consumer<E> {

  private final Object value;

  /**
   * Constructs a new ArrayAppendAction for appending a value to the specified JSON array field.
   * 
   * <p>This action will append the provided value to the end of the array located at the field's
   * JSON path within Redis JSON documents. The field must represent an array type in the entity's
   * JSON structure.</p>
   * 
   * @param field the search field accessor that identifies the JSON array field to append to;
   *              must not be null and should correspond to an array field in the entity
   * @param value the value to append to the array; can be any JSON-serializable object that
   *              is compatible with the array's element type
   * 
   * @throws NullPointerException if the entity class does not have an ID field (thrown by parent constructor)
   */
  public ArrayAppendAction(SearchFieldAccessor field, Object value) {
    super(field);
    this.value = value;
  }

  @Override
  public void accept(E entity) {
    json.arrAppend(getKey(entity), Path2.of("." + field.getSearchAlias()), value);
  }

}
