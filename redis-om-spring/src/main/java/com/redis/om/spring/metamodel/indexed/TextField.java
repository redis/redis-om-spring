package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.StrLengthAction;
import com.redis.om.spring.search.stream.actions.StringAppendAction;
import com.redis.om.spring.search.stream.predicates.fulltext.*;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

public class TextField<E, T> extends MetamodelField<E, T> {

  public TextField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }
  
  public EqualPredicate<E,T> eq(T value) {
    return new EqualPredicate<>(searchFieldAccessor,value);
  }
  
  public NotEqualPredicate<E,T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor,value);
  }
  
  public StartsWithPredicate<E,T> startsWith(T value) {
    return new StartsWithPredicate<>(searchFieldAccessor,value);
  }
  public EndsWithPredicate<E,T> endsWith(T value) {
    return new EndsWithPredicate<>(searchFieldAccessor,value);
  }
  
  public LikePredicate<E,T> like(T value) {
    return new LikePredicate<>(searchFieldAccessor,value);
  }
  
  public NotLikePredicate<E,T> notLike(T value) {
    return new NotLikePredicate<>(searchFieldAccessor,value);
  }
  
  public ContainingPredicate<E,T> containing(T value) {
    return new ContainingPredicate<>(searchFieldAccessor,value);
  }
  
  public NotContainingPredicate<E,T> notContaining(T value) {
    return new NotContainingPredicate<>(searchFieldAccessor,value);
  }

  @SuppressWarnings("unchecked")
  public InPredicate<E, ?> in(T... values) {
    return new InPredicate<>(searchFieldAccessor, Arrays.asList(values));
  }
  
  public Consumer<E> append(String value) {
    return new StringAppendAction<>(searchFieldAccessor, value);
  }
  
  public ToLongFunction<E> length() {
    return new StrLengthAction<>(searchFieldAccessor);
  }

}
