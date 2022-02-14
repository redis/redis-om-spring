package com.redis.om.spring.metamodel;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.redis.om.spring.search.stream.predicates.ContainsAllTagPredicate;
import com.redis.om.spring.search.stream.predicates.InTagPredicate;

public class TagFieldOperationInterceptor<E, T> extends FieldOperationInterceptor<E, T> {

  public TagFieldOperationInterceptor(Field field, boolean indexed) {
    super(field, indexed);
  }

  public InTagPredicate<? super E, ?> in(String... values) {
    return new InTagPredicate<E, T>(field, Arrays.asList(values));
  }
  
  public ContainsAllTagPredicate<? super E, ?> containsAll(String... values) {
    return new ContainsAllTagPredicate<E, T>(field, Arrays.asList(values));
  }

}
