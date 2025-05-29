package com.redis.om.spring.search.stream;

import java.time.Duration;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.indexed.NumericField;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.search.stream.predicates.SearchFieldPredicate;
import com.redis.om.spring.tuple.Pair;

import redis.clients.jedis.search.aggr.SortedField.SortOrder;

/**
 * A wrapper implementation of {@link SearchStream} that delegates stream operations
 * to an underlying Java Stream while providing SearchStream-specific functionality.
 * <p>
 * This class acts as an adapter between standard Java Stream operations and the
 * Redis OM search stream API. It delegates most standard stream operations to the
 * backing stream while providing no-op implementations for Redis-specific search
 * operations that cannot be performed on a wrapped stream.
 * </p>
 *
 * @param <E> the type of elements in the stream
 * @see SearchStream
 * @see Stream
 */
public class WrapperSearchStream<E> implements SearchStream<E> {

  private final Stream<E> backingStream;
  private Runnable closeHandler;

  /**
   * Constructs a new WrapperSearchStream with the given backing stream.
   *
   * @param backingStream the underlying Java Stream to wrap
   */
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

  @SuppressWarnings(
    "unchecked"
  )
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
  public SearchStream<E> filter(Example<E> example) {
    // NO-OP
    return this;
  }

  @Override
  public <T> SearchStream<E> filterIfNotNull(T value, Supplier<SearchFieldPredicate<? super E, ?>> predicateSupplier) {
    // NO-OP - filtering performed on backed stream
    return this;
  }

  @Override
  public SearchStream<E> filterIfNotBlank(String value,
      Supplier<SearchFieldPredicate<? super E, ?>> predicateSupplier) {
    // NO-OP - filtering performed on backed stream
    return this;
  }

  @Override
  public <T> SearchStream<E> filterIfPresent(Optional<T> value,
      Supplier<SearchFieldPredicate<? super E, ?>> predicateSupplier) {
    // NO-OP - filtering performed on backed stream
    return this;
  }

  @Override
  public <R> SearchStream<R> map(Function<? super E, ? extends R> mapper) {
    return new WrapperSearchStream<>(backingStream.map(mapper));
  }

  @Override
  public Class<E> getEntityClass() {
    return null;
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
  public SearchStream<E> sorted(Sort sort) {
    throw new UnsupportedOperationException("sorted(Sort) is not supported on a WrappedSearchStream");
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

  @SafeVarargs
  @Override
  public final <R> AggregationStream<R> groupBy(MetamodelField<E, ?>... fields) {
    throw new UnsupportedOperationException("groupBy is not supported on a WrappedSearchStream");
  }

  @Override
  public <R> AggregationStream<R> apply(String expression, String alias) {
    throw new UnsupportedOperationException("apply is not supported on a WrappedSearchStream");
  }

  @SafeVarargs
  @Override
  public final <R> AggregationStream<R> load(MetamodelField<E, ?>... fields) {
    throw new UnsupportedOperationException("load is not supported on a WrappedSearchStream");
  }

  @Override
  public <R> AggregationStream<R> loadAll() {
    throw new UnsupportedOperationException("loadAll is not supported on a WrappedSearchStream");
  }

  @Override
  public Optional<E> min(NumericField<E, ?> field) {
    throw new UnsupportedOperationException("min is not supported on a WrappedSearchStream");
  }

  @Override
  public Optional<E> max(NumericField<E, ?> field) {
    throw new UnsupportedOperationException("max is not supported on a WrappedSearchStream");
  }

  @Override
  public SearchStream<E> dialect(int dialect) {
    throw new UnsupportedOperationException("dialect is not supported on a WrappedSearchStream");
  }

  @Override
  public <R> AggregationStream<R> cursor(int i, Duration duration) {
    throw new UnsupportedOperationException("cursor is not supported on a WrappedSearchStream");
  }

  @Override
  public SearchOperations<String> getSearchOperations() {
    throw new UnsupportedOperationException("getSearchOperations is not supported on a WrappedSearchStream");
  }

  @Override
  public Page<E> getPage(Pageable pageable) {
    throw new UnsupportedOperationException("getPage is not supported on a WrappedSearchStream");
  }

  @Override
  public <R> SearchStream<E> project(Function<? super E, ? extends R> field) {
    throw new UnsupportedOperationException("project is not supported on a WrappedSearchStream");
  }

  @SafeVarargs
  @Override
  public final <R> SearchStream<E> project(MetamodelField<? super E, ? extends R>... field) {
    throw new UnsupportedOperationException("project is not supported on a WrappedSearchStream");
  }

  @Override
  public String backingQuery() {
    throw new UnsupportedOperationException("backingQuery is not supported on a WrappedSearchStream");
  }

  @Override
  public <R> SearchStream<E> summarize(Function<? super E, ? extends R> field) {
    throw new UnsupportedOperationException("summarize is not supported on a WrappedSearchStream");
  }

  @Override
  public <R> SearchStream<E> summarize(Function<? super E, ? extends R> field, SummarizeParams params) {
    throw new UnsupportedOperationException("summarize is not supported on a WrappedSearchStream");
  }

  @Override
  public <R> SearchStream<E> highlight(Function<? super E, ? extends R> field) {
    throw new UnsupportedOperationException("highlight is not supported on a WrappedSearchStream");
  }

  @Override
  public <R> SearchStream<E> highlight(Function<? super E, ? extends R> field, Pair<String, String> tags) {
    throw new UnsupportedOperationException("highlight is not supported on a WrappedSearchStream");
  }

  @Override
  public SearchStream<E> findFirstOrElse(Supplier<? extends E> supplier) {
    throw new UnsupportedOperationException("findFirstOrElse is not supported on a WrappedSearchStream");
  }

  @Override
  public boolean isDocument() {
    return false;
  }

}
