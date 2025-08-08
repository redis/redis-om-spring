package com.redis.om.spring.search.stream.predicates;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Predicate;

import redis.clients.jedis.search.Schema.FieldType;
import redis.clients.jedis.search.querybuilder.Node;

/**
 * Interface for predicates that operate on searchable fields in Redis OM entities.
 * This interface extends the standard {@link Predicate} with additional metadata
 * about the field being searched, enabling the construction of Redis search queries.
 * 
 * <p>SearchFieldPredicate provides access to field metadata including the Redis
 * field type, the Java field, and the search alias used in Redis queries.</p>
 * 
 * <p>Implementations of this interface can be combined using logical operators
 * to create complex search conditions.</p>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type of the predicate
 * 
 * @since 1.0
 * @see Predicate
 * @see AndPredicate
 * @see OrPredicate
 */
public interface SearchFieldPredicate<E, T> extends Predicate<T> {
  /**
   * Returns the Redis field type for the field this predicate operates on.
   * This determines how the field is indexed and searched in Redis.
   * 
   * @return the Redis field type (TEXT, TAG, NUMERIC, GEO, etc.)
   */
  FieldType getSearchFieldType();

  /**
   * Returns the Java field that this predicate operates on.
   * 
   * @return the Java field from the entity class
   */
  Field getField();

  /**
   * Returns the search alias used for this field in Redis queries.
   * The alias is typically the field name prefixed with '@'.
   * 
   * @return the search alias for the field
   */
  String getSearchAlias();

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  default Predicate<T> or(Predicate<? super T> other) {
    Objects.requireNonNull(other);
    OrPredicate<E, T> orPredicate = new OrPredicate<>(this);
    orPredicate.addPredicate((Predicate<T>) other);

    return orPredicate;
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  default Predicate<T> and(Predicate<? super T> other) {
    Objects.requireNonNull(other);
    AndPredicate<E, T> andPredicate = new AndPredicate<>(this);
    andPredicate.addPredicate((Predicate<T>) other);

    return andPredicate;
  }

  /**
   * Combines this predicate with another predicate of a potentially different field type
   * using a logical OR operation.
   * 
   * <p>This method allows combining predicates with different field types, which is useful
   * when building complex queries across multiple fields of different types.</p>
   * 
   * @param <U>   the field type of the other predicate
   * @param other the predicate to combine with this one
   * @return a new OR predicate combining both predicates
   */
  @SuppressWarnings(
    { "unchecked", "rawtypes" }
  )
  default <U> SearchFieldPredicate<E, ?> orAny(SearchFieldPredicate<E, U> other) {
    Objects.requireNonNull(other);
    OrPredicate orPredicate = new OrPredicate(this);
    orPredicate.addPredicate(other);
    return orPredicate;
  }

  /**
   * Combines this predicate with another predicate of a potentially different field type
   * using a logical AND operation.
   * 
   * <p>This method allows combining predicates with different field types, which is useful
   * when building complex queries across multiple fields of different types.</p>
   * 
   * @param <U>   the field type of the other predicate
   * @param other the predicate to combine with this one
   * @return a new AND predicate combining both predicates
   */
  @SuppressWarnings(
    { "unchecked", "rawtypes" }
  )
  default <U> SearchFieldPredicate<E, ?> andAny(SearchFieldPredicate<E, U> other) {
    Objects.requireNonNull(other);
    AndPredicate andPredicate = new AndPredicate(this);
    andPredicate.addPredicate(other);
    return andPredicate;
  }

  /**
   * Applies this predicate to a RediSearch query node.
   * This method transforms the predicate into a RediSearch query node
   * that can be used to build the final search query.
   * 
   * @param node the base query node to apply this predicate to
   * @return the modified query node with this predicate applied
   */
  default Node apply(Node node) {
    return node;
  }

}
