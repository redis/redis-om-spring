package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.numeric.*;

public class DateField<E, T> extends MetamodelField<E, T> {

  public DateField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }
  
  public EqualPredicate<? super E,T> eq(T value) {
    return new EqualPredicate<>(searchFieldAccessor,value);
  }
  
  public NotEqualPredicate<? super E,T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor,value);
  }
  
  public GreaterThanPredicate<? super E,T> after(T value) {
    return new GreaterThanPredicate<>(searchFieldAccessor,value);
  }
  
  public GreaterThanOrEqualPredicate<? super E,T> onOrAfter(T value) {
    return new GreaterThanOrEqualPredicate<>(searchFieldAccessor,value);
  }
  
  public LessThanPredicate<? super E,T> before(T value) {
    return new LessThanPredicate<>(searchFieldAccessor,value);
  }
  
  public LessThanOrEqualPredicate<? super E,T> onOrBefore(T value) {
    return new LessThanOrEqualPredicate<>(searchFieldAccessor,value);
  }
  
  public BetweenPredicate<? super E,T> between(T min, T max) {
    return new BetweenPredicate<>(searchFieldAccessor,min,max);
  }

}
