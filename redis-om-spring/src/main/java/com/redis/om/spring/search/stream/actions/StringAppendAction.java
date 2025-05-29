package com.redis.om.spring.search.stream.actions;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

import redis.clients.jedis.json.Path2;

/**
 * Action that appends a string value to a string field in a Redis JSON document.
 * This action is used within the Entity Streams API to modify string values
 * stored in JSON documents without requiring a full entity reload.
 * 
 * <p>The StringAppendAction performs an atomic string append operation on the specified
 * string field using Redis's JSON.STRAPPEND command. This is more efficient
 * than retrieving the document, concatenating the strings, and saving it back.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * entityStream.filter(Person$.NAME.startsWith("John"))
 *            .forEach(StringAppendAction.of(Person$.NAME, " Jr."));
 * }</pre>
 * 
 * @param <E> the entity type that this action operates on
 * @since 1.0
 * @see BaseAbstractAction
 * @see redis.clients.jedis.json.JsonProtocol.JsonCommand#STRAPPEND
 */
public class StringAppendAction<E> extends BaseAbstractAction implements Consumer<E> {

  /** The string value to append to the target field */
  private final String value;

  /**
   * Constructs a new StringAppendAction that will append the specified value to the target field.
   * 
   * @param field the search field accessor identifying the string field to append to
   * @param value the string value to append to the field
   */
  public StringAppendAction(SearchFieldAccessor field, String value) {
    super(field);
    this.value = value;
  }

  /**
   * Applies the string append operation to the specified entity.
   * This method executes a Redis JSON.STRAPPEND command to atomically
   * append the configured value to the target field.
   * 
   * @param entity the entity whose string field should have the value appended
   */
  @Override
  public void accept(E entity) {
    json.strAppend(getKey(entity), Path2.of("." + field.getSearchAlias()), value);
  }

}
