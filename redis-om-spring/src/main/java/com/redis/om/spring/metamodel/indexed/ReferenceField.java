package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.reference.EqualPredicate;
import com.redis.om.spring.search.stream.predicates.reference.NotEqualPredicate;

/**
 * Metamodel field representation for entity reference fields.
 * <p>
 * This class provides predicate operations specifically designed for reference fields
 * that contain relationships to other entities. Reference fields support equality
 * comparisons for finding entities with specific referenced values.
 * <p>
 * Common use cases include:
 * <ul>
 * <li>Finding entities that reference a specific other entity</li>
 * <li>Filtering by referenced entity properties</li>
 * <li>Building queries based on entity relationships</li>
 * </ul>
 *
 * @param <E> the entity type that contains this reference field
 * @param <T> the type of the referenced entity or value
 * @since 1.0.0
 */
public class ReferenceField<E, T> extends MetamodelField<E, T> {
  /**
   * Creates a new reference field with the specified accessor and indexing configuration.
   *
   * @param field   the search field accessor for this reference field
   * @param indexed whether this reference field is indexed for search operations
   */
  public ReferenceField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  /**
   * Creates an equality predicate for this reference field.
   * <p>
   * This predicate matches entities where the reference field equals the specified value.
   * The comparison is typically performed on the referenced entity's identifier or
   * a specific property of the referenced entity.
   *
   * @param value the value to compare against
   * @return an equality predicate for query building
   */
  public EqualPredicate<E, T> eq(T value) {
    return new EqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a not-equal predicate for this reference field.
   * <p>
   * This predicate matches entities where the reference field does not equal
   * the specified value. This is useful for excluding entities with specific
   * reference relationships.
   *
   * @param value the value to compare against (excluded from results)
   * @return a not-equal predicate for query building
   */
  public NotEqualPredicate<E, T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor, value);
  }
}
