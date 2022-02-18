package com.redis.om.spring.metamodel;

import java.lang.reflect.Field;

import com.redis.om.spring.search.stream.predicates.numeric.BetweenPredicate;
import com.redis.om.spring.search.stream.predicates.numeric.GreaterThanOrEqualPredicate;
import com.redis.om.spring.search.stream.predicates.numeric.GreaterThanPredicate;
import com.redis.om.spring.search.stream.predicates.numeric.LessThanOrEqualPredicate;
import com.redis.om.spring.search.stream.predicates.numeric.LessThanPredicate;

public class DateFieldOperationInterceptor<E, T> extends FieldOperationInterceptor<E, T> {

  public DateFieldOperationInterceptor(Field field, boolean indexed) {
    super(field, indexed);
  }
  
  public GreaterThanPredicate<? super E,T> after(T value) {
    return new GreaterThanPredicate<E,T>(field,value);
  }
  
  public GreaterThanOrEqualPredicate<? super E,T> onOrAfter(T value) {
    return new GreaterThanOrEqualPredicate<E,T>(field,value);
  }
  
  public LessThanPredicate<? super E,T> before(T value) {
    return new LessThanPredicate<E,T>(field,value);
  }
  
  public LessThanOrEqualPredicate<? super E,T> onOrBefore(T value) {
    return new LessThanOrEqualPredicate<E,T>(field,value);
  }
  
  public BetweenPredicate<? super E,T> between(T min, T max) {
    return new BetweenPredicate<E,T>(field,min,max);
  }

}
