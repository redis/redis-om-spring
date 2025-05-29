package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.SearchFieldAccessor;

/**
 * Metamodel field representing a collection field in a Redis OM Spring entity.
 * <p>
 * This class extends {@link TagField} to provide specialized handling for collection-type
 * fields (lists, sets, arrays) in entities. Collection fields are treated as tag fields
 * where each element in the collection becomes a searchable tag value.
 * </p>
 * <p>
 * Collection fields support:
 * <ul>
 * <li>Multi-value tag search (searching for any element in the collection)</li>
 * <li>Containment queries (checking if a collection contains specific values)</li>
 * <li>Set operations (intersection, union) across multiple collections</li>
 * </ul>
 * <p>
 * Example usage in generated metamodels:
 * <pre>{@code
 * // For an entity with a List<String> tags field
 * public static final CollectionField<MyEntity, List<String>> tags = 
 *     new CollectionField<>(MyEntity.class, "tags");
 * 
 * // Query usage
 * entityStream.filter(MyEntity$.tags.containsAny("important", "urgent"))
 * }</pre>
 *
 * @param <E> the entity type containing this field
 * @param <T> the collection type of this field
 * @see TagField
 * @see SearchFieldAccessor
 * @since 0.1.0
 */
public class CollectionField<E, T> extends TagField<E, T> {

  /**
   * Creates a new CollectionField with the specified search field accessor.
   * <p>
   * This constructor is typically used by the metamodel generation process
   * when creating field instances with pre-configured accessors.
   * </p>
   *
   * @param searchFieldAccessor the accessor for this search field
   * @param indexed             whether this field is indexed for search operations
   */
  public CollectionField(SearchFieldAccessor searchFieldAccessor, boolean indexed) {
    super(searchFieldAccessor, indexed);
  }

  /**
   * Creates a new CollectionField for the specified entity class and field name.
   * <p>
   * This constructor is used when creating field instances with basic class
   * and field name information, typically in static metamodel initialization.
   * </p>
   *
   * @param targetClass the entity class containing this field
   * @param fieldName   the name of the field in the entity class
   */
  public CollectionField(Class<E> targetClass, String fieldName) {
    super(targetClass, fieldName);
  }
}
