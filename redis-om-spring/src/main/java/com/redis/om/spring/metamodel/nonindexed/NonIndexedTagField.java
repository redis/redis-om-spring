package com.redis.om.spring.metamodel.nonindexed;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.search.stream.actions.ArrayAppendAction;
import com.redis.om.spring.search.stream.actions.ArrayIndexOfAction;
import com.redis.om.spring.search.stream.actions.ArrayInsertAction;
import com.redis.om.spring.search.stream.actions.ArrayLengthAction;
import com.redis.om.spring.search.stream.actions.ArrayPopAction;
import com.redis.om.spring.search.stream.actions.ArrayTrimAction;

public class NonIndexedTagField<E, T> extends MetamodelField<E, T> {

  public NonIndexedTagField(Field field, boolean indexed) {
    super(field, indexed);
  }

  public Consumer<? super E> add(Object value) {
    return new ArrayAppendAction<>(field, value);
  }

  public Consumer<? super E> insert(Object value, int index) {
    return new ArrayInsertAction<>(field, value, index);
  }

  public Consumer<? super E> prepend(Object value) {
    return new ArrayInsertAction<>(field, value, 0);
  }

  public ToLongFunction<? super E> length() {
    return new ArrayLengthAction<>(field);
  }

  public ToLongFunction<? super E> indexOf(Object element) {
    return new ArrayIndexOfAction<>(field, element);
  }

  public <R> ArrayPopAction<? super E,R> pop(Long index) {
    return new ArrayPopAction<>(field, index);
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

  public Consumer<? super E> trimToRange(int begin, int end) {
    return new ArrayTrimAction<>(field, begin, end);
  }

}
