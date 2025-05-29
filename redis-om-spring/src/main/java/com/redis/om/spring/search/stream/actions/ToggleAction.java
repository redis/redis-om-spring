package com.redis.om.spring.search.stream.actions;

import java.util.function.Consumer;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

import redis.clients.jedis.json.Path2;

/**
 * Action that toggles a boolean field in a Redis JSON document.
 * This action is used within the Entity Streams API to flip boolean values
 * stored in JSON documents without requiring a full entity reload.
 * 
 * <p>The ToggleAction performs an atomic toggle operation on the specified
 * boolean field using Redis's JSON.TOGGLE command. This operation changes
 * {@code true} to {@code false} and {@code false} to {@code true}.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * entityStream.filter(Person$.ACTIVE.eq(true))
 *            .forEach(ToggleAction.of(Person$.ACTIVE));
 * }</pre>
 * 
 * @param <E> the entity type that this action operates on
 * @since 1.0
 * @see BaseAbstractAction
 * @see redis.clients.jedis.json.JsonProtocol.JsonCommand#TOGGLE
 */
public class ToggleAction<E> extends BaseAbstractAction implements Consumer<E> {

  /**
   * Constructs a new ToggleAction that will toggle the specified boolean field.
   * 
   * @param field the search field accessor identifying the boolean field to toggle
   */
  public ToggleAction(SearchFieldAccessor field) {
    super(field);
  }

  /**
   * Applies the boolean toggle operation to the specified entity.
   * This method executes a Redis JSON.TOGGLE command to atomically
   * flip the boolean value of the target field.
   * 
   * @param entity the entity whose boolean field should be toggled
   */
  @Override
  public void accept(E entity) {
    json.toggle(getKey(entity), Path2.of("." + field.getSearchAlias()));
  }
}
