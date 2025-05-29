package com.redis.om.spring.search.stream.actions;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

import redis.clients.jedis.json.Path2;

/**
 * Action class that inserts a value at a specific index within a JSON array field in a Redis JSON document.
 * This class implements the {@link Consumer} interface to perform insertion operations on Redis JSON arrays
 * through Redis OM entity streams.
 * 
 * <p>The insertion operation uses the Redis JSON ARRINSERT command to add elements at a specified position
 * within an array stored at a specific path in a JSON document. Existing elements at and after the insertion
 * index are shifted to higher indices to accommodate the new element.</p>
 * 
 * <p>Example usage in an entity stream:</p>
 * <pre>{@code
 * entityStream
 *   .filter(entity -> entity.getTags().size() > 2)
 *   .forEach(new ArrayInsertAction<>(MyEntity$.TAGS, "insertedTag", 1));
 * }</pre>
 * 
 * @param <E> the type of entity that contains the JSON array field to be modified
 * 
 * @see BaseAbstractAction
 * @see Consumer
 * @see SearchFieldAccessor
 */
public class ArrayInsertAction<E> extends BaseAbstractAction implements Consumer<E> {

  private final Object value;
  private final Integer index;

  /**
   * Constructs a new ArrayInsertAction for inserting a value at a specific position in the specified JSON array field.
   * 
   * <p>This action will insert the provided value at the specified index within the array located at the
   * field's JSON path. Elements at and after the insertion index will be shifted to higher positions.</p>
   * 
   * @param field the search field accessor that identifies the JSON array field to insert into;
   *              must not be null and should correspond to an array field in the entity
   * @param value the value to insert into the array; can be any JSON-serializable object that
   *              is compatible with the array's element type
   * @param index the zero-based index position where the value should be inserted; must be a valid
   *              index within the array bounds (0 to array length inclusive)
   * 
   * @throws NullPointerException if the entity class does not have an ID field (thrown by parent constructor)
   */
  public ArrayInsertAction(SearchFieldAccessor field, Object value, Integer index) {
    super(field);
    this.value = value;
    this.index = index;
  }

  @Override
  public void accept(E entity) {
    json.arrInsert(getKey(entity), Path2.of("." + field.getSearchAlias()), index, value);
  }

}
