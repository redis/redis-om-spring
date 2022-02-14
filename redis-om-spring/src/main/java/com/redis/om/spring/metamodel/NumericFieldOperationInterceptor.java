package com.redis.om.spring.metamodel;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.redis.om.spring.search.stream.predicates.BetweenPredicate;
import com.redis.om.spring.search.stream.predicates.GreaterThanOrEqualPredicate;
import com.redis.om.spring.search.stream.predicates.GreaterThanPredicate;
import com.redis.om.spring.search.stream.predicates.InPredicate;
import com.redis.om.spring.search.stream.predicates.LessThanOrEqualPredicate;
import com.redis.om.spring.search.stream.predicates.LessThanPredicate;

public class NumericFieldOperationInterceptor<E, T> extends FieldOperationInterceptor<E, T> {

  public NumericFieldOperationInterceptor(Field field, boolean indexed) {
    super(field, indexed);
  }
  
  public GreaterThanPredicate<? super E,T> gt(T value) {
    return new GreaterThanPredicate<E,T>(field,value);
  }
  
  public GreaterThanOrEqualPredicate<? super E,T> ge(T value) {
    return new GreaterThanOrEqualPredicate<E,T>(field,value);
  }
  
  public LessThanPredicate<? super E,T> lt(T value) {
    return new LessThanPredicate<E,T>(field,value);
  }
  
  public LessThanOrEqualPredicate<? super E,T> le(T value) {
    return new LessThanOrEqualPredicate<E,T>(field,value);
  }
  
  public BetweenPredicate<? super E,T> between(T min, T max) {
    return new BetweenPredicate<E,T>(field,min,max);
  }
  
  @SuppressWarnings("unchecked")
  public InPredicate<? super E, ?> in(T... values) {
    return new InPredicate<E,T>(field, Arrays.asList(values));
  }

}
