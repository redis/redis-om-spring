package com.redis.om.spring.metamodel;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.redis.om.spring.search.stream.predicates.tag.ContainsAllPredicate;
import com.redis.om.spring.search.stream.predicates.tag.EqualPredicate;
import com.redis.om.spring.search.stream.predicates.tag.InPredicate;
import com.redis.om.spring.search.stream.predicates.tag.NotEqualPredicate;

public class TagFieldOperationInterceptor<E, T> extends FieldOperationInterceptor<E, T> {

  public TagFieldOperationInterceptor(Field field, boolean indexed) {
    super(field, indexed);
  }
  
  public EqualPredicate<? super E,T> eq(T value) {
    return new EqualPredicate<E,T>(field,value);
  }
  
  public NotEqualPredicate<? super E,T> notEq(T value) {
    return new NotEqualPredicate<E,T>(field,value);
  }
  
  public InPredicate<? super E, ?> in(String... values) {
    return new InPredicate<E, T>(field, Arrays.asList(values));
  }
  
  public ContainsAllPredicate<? super E, ?> containsAll(String... values) {
    return new ContainsAllPredicate<E, T>(field, Arrays.asList(values));
  }
  
  public NotEqualPredicate<? super E,T> containsNone(T value) {
    return new NotEqualPredicate<E,T>(field,value);
  }

}
