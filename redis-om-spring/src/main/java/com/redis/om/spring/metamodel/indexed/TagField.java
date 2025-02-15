package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.*;
import com.redis.om.spring.search.stream.predicates.tag.ContainsAllPredicate;
import com.redis.om.spring.search.stream.predicates.tag.EqualPredicate;
import com.redis.om.spring.search.stream.predicates.tag.InPredicate;
import com.redis.om.spring.search.stream.predicates.tag.NotEqualPredicate;
import com.redis.om.spring.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

public class TagField<E, T> extends MetamodelField<E, T> {

  public TagField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  public TagField(Class<E> targetClass, String fieldName) {
    super(targetClass, fieldName);
  }

  public EqualPredicate<E, T> eq(T value) {
    return new EqualPredicate<>(searchFieldAccessor, value);
  }

  public NotEqualPredicate<E, T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor, value);
  }

  public NotEqualPredicate<E, T> notEq(String... values) {
    return new NotEqualPredicate<>(searchFieldAccessor, Arrays.asList(values));
  }

  public NotEqualPredicate<E, T> notEq(Object... values) {
    return new NotEqualPredicate<>(searchFieldAccessor, Arrays.stream(values).map(Object::toString).toList());
  }

  public InPredicate<E, ?> in(String... values) {
    return new InPredicate<>(searchFieldAccessor, Arrays.asList(values));
  }

  public InPredicate<E, ?> in(Object... values) {
    return new InPredicate<>(searchFieldAccessor, Arrays.stream(values).map(Object::toString).toList());
  }

  public ContainsAllPredicate<E, ?> containsAll(String... values) {
    return new ContainsAllPredicate<>(searchFieldAccessor, Arrays.asList(values));
  }

  public ContainsAllPredicate<E, ?> containsAll(Object... values) {
    return new ContainsAllPredicate<>(searchFieldAccessor, Arrays.stream(values).map(Object::toString).toList());
  }

  public NotEqualPredicate<E, T> containsNone(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor, value);
  }

  public NotEqualPredicate<E, ?> containsNone(String value) {
    if (!ObjectUtils.isCollection(value.getClass())) {
      return new NotEqualPredicate<>(searchFieldAccessor, Set.of(value));
    } else {
      return new NotEqualPredicate<>(searchFieldAccessor, value);
    }
  }

  public Consumer<E> add(Object value) {
    return new ArrayAppendAction<>(searchFieldAccessor, value);
  }

  public Consumer<E> insert(Object value, Integer index) {
    return new ArrayInsertAction<>(searchFieldAccessor, value, index);
  }

  public Consumer<E> prepend(Object value) {
    return new ArrayInsertAction<>(searchFieldAccessor, value, 0);
  }

  public ToLongFunction<E> length() {
    return new ArrayLengthAction<>(searchFieldAccessor);
  }

  public ToLongFunction<E> indexOf(Object element) {
    return new ArrayIndexOfAction<>(searchFieldAccessor, element);
  }

  public <R> ArrayPopAction<E, R> pop(Integer index) {
    return new ArrayPopAction<>(searchFieldAccessor, index);
  }

  public <R> ArrayPopAction<E, R> pop() {
    return pop(-1);
  }

  public <R> ArrayPopAction<E, R> removeFirst() {
    return pop(0);
  }

  public <R> ArrayPopAction<E, R> removeLast() {
    return pop(-1);
  }

  public <R> ArrayPopAction<E, R> remove(Integer index) {
    return pop(index);
  }

  public Consumer<E> trimToRange(Integer begin, Integer end) {
    return new ArrayTrimAction<>(searchFieldAccessor, begin, end);
  }
}