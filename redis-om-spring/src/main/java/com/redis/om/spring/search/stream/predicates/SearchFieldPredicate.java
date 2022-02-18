package com.redis.om.spring.search.stream.predicates;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Predicate;

import io.redisearch.Schema.FieldType;
import io.redisearch.querybuilder.Node;

public interface SearchFieldPredicate<E, T> extends Predicate<T> {
  PredicateType getPredicateType();
  FieldType getSearchFieldType();
  Field getField();

  @SuppressWarnings("unchecked")
  default Predicate<T> or(Predicate<? super T> other) {
    Objects.requireNonNull(other);
    OrPredicate<E, T> orPredicate = new OrPredicate<E,T>(this);
    orPredicate.addPredicate((Predicate<T>) other);

    return orPredicate;
  }

  @SuppressWarnings("unchecked")
  default Predicate<T> and(Predicate<? super T> other) {
    Objects.requireNonNull(other);
    AndPredicate<E, T> andPredicate = new AndPredicate<E,T>(this);
    andPredicate.addPredicate((Predicate<T>) other);

    return andPredicate;
  }
  
  default Node apply(Node node) {
    return node;
  }
  
  @Override
  default boolean test(T t) {
    return false;
  }

}
