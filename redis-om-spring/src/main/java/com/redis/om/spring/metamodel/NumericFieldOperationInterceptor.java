package com.redis.om.spring.metamodel;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.redis.om.spring.search.stream.predicates.numeric.BetweenPredicate;
import com.redis.om.spring.search.stream.predicates.numeric.GreaterThanOrEqualPredicate;
import com.redis.om.spring.search.stream.predicates.numeric.GreaterThanPredicate;
import com.redis.om.spring.search.stream.predicates.numeric.LessThanOrEqualPredicate;
import com.redis.om.spring.search.stream.predicates.numeric.LessThanPredicate;
import com.redis.om.spring.search.stream.predicates.numeric.EqualPredicate;
import com.redis.om.spring.search.stream.predicates.numeric.InPredicate;
import com.redis.om.spring.search.stream.predicates.numeric.NotEqualPredicate;

public class NumericFieldOperationInterceptor<E, T> extends FieldOperationInterceptor<E, T> {

  public NumericFieldOperationInterceptor(Field field, boolean indexed) {
    super(field, indexed);
  }
  
  public EqualPredicate<? super E,T> eq(T value) {
    return new EqualPredicate<E,T>(field,value);
  }
  
  public NotEqualPredicate<? super E,T> notEq(T value) {
    return new NotEqualPredicate<E,T>(field,value);
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
