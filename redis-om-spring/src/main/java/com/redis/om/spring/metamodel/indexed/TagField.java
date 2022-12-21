package com.redis.om.spring.metamodel.indexed;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.ArrayAppendAction;
import com.redis.om.spring.search.stream.actions.ArrayIndexOfAction;
import com.redis.om.spring.search.stream.actions.ArrayInsertAction;
import com.redis.om.spring.search.stream.actions.ArrayLengthAction;
import com.redis.om.spring.search.stream.actions.ArrayPopAction;
import com.redis.om.spring.search.stream.actions.ArrayTrimAction;
import com.redis.om.spring.search.stream.predicates.tag.ContainsAllPredicate;
import com.redis.om.spring.search.stream.predicates.tag.EqualPredicate;
import com.redis.om.spring.search.stream.predicates.tag.InPredicate;
import com.redis.om.spring.search.stream.predicates.tag.NotEqualPredicate;

public class TagField<E, T> extends MetamodelField<E, T> {

  public TagField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }
  
  public EqualPredicate<? super E,T> eq(T value) {
    return new EqualPredicate<>(searchFieldAccessor,value);
  }
  
  public NotEqualPredicate<? super E,T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor,value);
  }
  
  public NotEqualPredicate<? super E,T> notEq(String... values) {
    return new NotEqualPredicate<>(searchFieldAccessor, Arrays.asList(values));
  }
  
  public InPredicate<? super E, ?> in(String... values) {
    return new InPredicate<>(searchFieldAccessor, Arrays.asList(values));
  }
  
  public ContainsAllPredicate<? super E, ?> containsAll(String... values) {
    return new ContainsAllPredicate<>(searchFieldAccessor, Arrays.asList(values));
  }
  
  public NotEqualPredicate<? super E,T> containsNone(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor,value);
  }
  
  public Consumer<? super E> add(Object value) {
    return new ArrayAppendAction<>(searchFieldAccessor, value);
  }

  public Consumer<? super E> insert(Object value, Long index) {
    return new ArrayInsertAction<>(searchFieldAccessor, value, index);
  }
  
  public Consumer<? super E> prepend(Object value) {
    return new ArrayInsertAction<>(searchFieldAccessor, value, 0L);
  }
  
  public ToLongFunction<? super E> length() {
    return new ArrayLengthAction<>(searchFieldAccessor);
  }

  public ToLongFunction<? super E> indexOf(Object element) {
    return new ArrayIndexOfAction<>(searchFieldAccessor, element);
  }
  
  public <R> ArrayPopAction<? super E,R> pop(Long index) {
    return new ArrayPopAction<>(searchFieldAccessor, index);
  }
  
  public <R> ArrayPopAction<? super E,R> pop() {
    return pop(-1L);
  }
  
  public <R> ArrayPopAction<? super E,R> removeFirst() {
    return pop(0L);
  }
  
  public <R> ArrayPopAction<? super E,R> removeLast() {
    return pop(-1L);
  }
  
  public <R> ArrayPopAction<? super E,R> remove(Long index) {
    return pop(index);
  }

  public Consumer<? super E> trimToRange(Long begin, Long end) {
    return new ArrayTrimAction<>(searchFieldAccessor, begin, end);
  }

}
