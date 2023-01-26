package com.redis.om.spring.search.stream;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.indexed.NumericField;
import com.redis.om.spring.search.stream.predicates.SearchFieldPredicate;
import redis.clients.jedis.search.aggr.SortedField.SortOrder;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class WrapperSearchStream<E> implements SearchStream<E> {

  private final Stream<E> backingStream;
  private Runnable closeHandler;

  public WrapperSearchStream(Stream<E> backingStream) {
    this.backingStream = backingStream;
  }

  @Override
  public Iterator<E> iterator() {
    return backingStream.iterator();
  }

  @Override
  public Spliterator<E> spliterator() {
    return backingStream.spliterator();
  }

  @Override
  public boolean isParallel() {
    return backingStream.isParallel();
  }

  @Override
  public SearchStream<E> sequential() {
    return new WrapperSearchStream<>(backingStream.sequential());
  }

  @Override
  public SearchStream<E> parallel() {
    return new WrapperSearchStream<>(backingStream.parallel());
  }

  @Override
  public SearchStream<E> unordered() {
    return new WrapperSearchStream<>(backingStream.unordered());
  }

  @Override
  public SearchStream<E> onClose(Runnable closeHandler) {
    this.closeHandler = closeHandler;
    return this;
  }

  @Override
  public void close() {
    if (closeHandler == null) {
      backingStream.close();
    } else {
      backingStream.onClose(closeHandler).close();
    }
  }

  @Override
  public SearchStream<E> filter(SearchFieldPredicate<? super E, ?> predicate) {
    // NO-OP
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public SearchStream<E> filter(Predicate<?> predicate) {
    // TODO: need to test this cast!
    return new WrapperSearchStream<>(backingStream.filter((Predicate<? super E>) predicate));
  }

  @Override
  public SearchStream<E> filter(String freeText) {
    // NO-OP
    return this;
  }

  @Override
  public <R> SearchStream<R> map(Function<? super E, ? extends R> mapper) {
    return new WrapperSearchStream<>(backingStream.map(mapper));
  }

  @Override
  public IntStream mapToInt(ToIntFunction<? super E> mapper) {
    return backingStream.mapToInt(mapper);
  }

  @Override
  public LongStream mapToLong(ToLongFunction<? super E> mapper) {
    return backingStream.mapToLong(mapper);
  }

  @Override
  public DoubleStream mapToDouble(ToDoubleFunction<? super E> mapper) {
    return backingStream.mapToDouble(mapper);
  }

  @Override
  public <R> SearchStream<R> flatMap(Function<? super E, ? extends Stream<? extends R>> mapper) {
    return new WrapperSearchStream<>(backingStream.flatMap(mapper));
  }

  @Override
  public IntStream flatMapToInt(Function<? super E, ? extends IntStream> mapper) {
    return backingStream.flatMapToInt(mapper);
  }

  @Override
  public LongStream flatMapToLong(Function<? super E, ? extends LongStream> mapper) {
    return backingStream.flatMapToLong(mapper);
  }

  @Override
  public DoubleStream flatMapToDouble(Function<? super E, ? extends DoubleStream> mapper) {
    return backingStream.flatMapToDouble(mapper);
  }

  @Override
  public SearchStream<E> sorted(Comparator<? super E> comparator) {
    return new WrapperSearchStream<>(backingStream.sorted(comparator));
  }

  @Override
  public SearchStream<E> sorted(Comparator<? super E> comparator, SortOrder order) {
    return new WrapperSearchStream<>(backingStream.sorted(comparator));
  }

  @Override
  public SearchStream<E> peek(Consumer<? super E> action) {
    return new WrapperSearchStream<>(backingStream.peek(action));
  }

  @Override
  public SearchStream<E> limit(long maxSize) {
    return new WrapperSearchStream<>(backingStream.limit(maxSize));
  }

  @Override
  public SearchStream<E> skip(long n) {
    return new WrapperSearchStream<>(backingStream.skip(n));
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    backingStream.forEach(action);
  }

  @Override
  public void forEachOrdered(Consumer<? super E> action) {
    backingStream.forEachOrdered(action);
  }

  @Override
  public Object[] toArray() {
    return backingStream.toArray();
  }

  @Override
  public <A> A[] toArray(IntFunction<A[]> generator) {
    return backingStream.toArray(generator);
  }

  @Override
  public E reduce(E identity, BinaryOperator<E> accumulator) {
    return backingStream.reduce(identity, accumulator);
  }

  @Override
  public Optional<E> reduce(BinaryOperator<E> accumulator) {
    return backingStream.reduce(accumulator);
  }

  @Override
  public <U> U reduce(U identity, BiFunction<U, ? super E, U> accumulator, BinaryOperator<U> combiner) {
    return backingStream.reduce(identity, accumulator, combiner);
  }

  @Override
  public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super E> accumulator, BiConsumer<R, R> combiner) {
    return backingStream.collect(supplier, accumulator, combiner);
  }

  @Override
  public <R, A> R collect(Collector<? super E, A, R> collector) {
    return backingStream.collect(collector);
  }

  @Override
  public Optional<E> min(Comparator<? super E> comparator) {
    return backingStream.min(comparator);
  }

  @Override
  public Optional<E> max(Comparator<? super E> comparator) {
    return backingStream.max(comparator);
  }

  @Override
  public long count() {
    return backingStream.count();
  }

  @Override
  public boolean anyMatch(Predicate<? super E> predicate) {
    return backingStream.anyMatch(predicate);
  }

  @Override
  public boolean allMatch(Predicate<? super E> predicate) {
    return backingStream.allMatch(predicate);
  }

  @Override
  public boolean noneMatch(Predicate<? super E> predicate) {
    return backingStream.noneMatch(predicate);
  }

  @Override
  public Optional<E> findFirst() {
    return backingStream.findFirst();
  }

  @Override
  public Optional<E> findAny() {
    return backingStream.findAny();
  }

  @Override
  public Stream<Long> map(ToLongFunction<? super E> mapper) {
    return backingStream.mapToLong(mapper).boxed();
  }

  @Override
  public Stream<Map<String, Object>> mapToLabelledMaps() {
    throw new UnsupportedOperationException("mapToLabelledMaps is not supported on a WrappedSearchStream");
  }

  @SafeVarargs @Override
  public final <R> AggregationStream<R> groupBy(MetamodelField<E, ?>... fields) {
    throw new UnsupportedOperationException("groupBy is not supported on a WrappedSearchStream");
  }

  @Override
  public <R> AggregationStream<R> apply(String expression, String alias) {
    throw new UnsupportedOperationException("apply is not supported on a WrappedSearchStream");
  }

  @SafeVarargs @Override
  public final <R> AggregationStream<R> load(MetamodelField<E, ?>... fields) {
    throw new UnsupportedOperationException("load is not supported on a WrappedSearchStream");
  }

  @Override
  public Optional<E> min(NumericField<E, ?> field) {
    throw new UnsupportedOperationException("min is not supported on a WrappedSearchStream");
  }

  @Override
  public Optional<E> max(NumericField<E, ?> field) {
    throw new UnsupportedOperationException("max is not supported on a WrappedSearchStream");
  }

}
