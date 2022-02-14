package com.redis.om.spring.search.stream.predicates;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class AndPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private List<Predicate<T>> predicates = new ArrayList<Predicate<T>>();
  
  
  public AndPredicate(SearchFieldPredicate<E, T> root) {
    predicates.add(root);
  }
  
  public void addPredicate(Predicate<T> predicate) {
    this.predicates.add(predicate);
  }

  @Override
  public PredicateType getPredicateType() {
    return PredicateType.AND;
  }

  @Override
  public boolean test(T t) {
    // TODO Auto-generated method stub
    return false;
  }
  
  public Stream<Predicate<T>> stream() {
    return predicates.stream();
  }

}
