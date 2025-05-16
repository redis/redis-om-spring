package com.redis.om.spring.search.stream;

import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
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

public interface SearchStream<E> extends BaseStream<E, SearchStream<E>> {

  SearchStream<E> filter(SearchFieldPredicate<? super E, ?> predicate);

  SearchStream<E> filter(Predicate<?> predicate);

  SearchStream<E> filter(String freeText);

  SearchStream<E> filter(Example<E> example);

  /**
   * Applies a filter predicate only if the value is not null.
   *
   * @param value             the value to check
   * @param predicateSupplier a supplier that creates a predicate
   * @param <T>               the type of the value
   * @return this SearchStream instance
   */
  <T> SearchStream<E> filterIfNotNull(T value, Supplier<SearchFieldPredicate<? super E, ?>> predicateSupplier);

  /**
   * Applies a filter predicate only if the string value is not null and not blank.
   *
   * @param value             the string value to check
   * @param predicateSupplier a supplier that creates a predicate
   * @return this SearchStream instance
   */
  SearchStream<E> filterIfNotBlank(String value, Supplier<SearchFieldPredicate<? super E, ?>> predicateSupplier);

  /**
   * Applies a filter predicate only if the optional value is present.
   *
   * @param value             the optional value to check
   * @param predicateSupplier a supplier that creates a predicate
   * @param <T>               the type of the optional value
   * @return this SearchStream instance
   */
  <T> SearchStream<E> filterIfPresent(Optional<T> value,
      Supplier<SearchFieldPredicate<? super E, ?>> predicateSupplier);

  <R> SearchStream<R> map(Function<? super E, ? extends R> field);

  Class<E> getEntityClass();

  Stream<Long> map(ToLongFunction<? super E> mapper);

  IntStream mapToInt(ToIntFunction<? super E> mapper);

  LongStream mapToLong(ToLongFunction<? super E> mapper);

  DoubleStream mapToDouble(ToDoubleFunction<? super E> mapper);

  <R> SearchStream<R> flatMap(Function<? super E, ? extends Stream<? extends R>> mapper);

  IntStream flatMapToInt(Function<? super E, ? extends IntStream> mapper);

  LongStream flatMapToLong(Function<? super E, ? extends LongStream> mapper);

  DoubleStream flatMapToDouble(Function<? super E, ? extends DoubleStream> mapper);

  SearchStream<E> sorted(Comparator<? super E> comparator);

  SearchStream<E> sorted(Comparator<? super E> comparator, SortOrder order);

  SearchStream<E> sorted(Sort sort);

  SearchStream<E> peek(Consumer<? super E> action);

  SearchStream<E> limit(long maxSize);

  SearchStream<E> skip(long n);

  void forEach(Consumer<? super E> action);

  void forEachOrdered(Consumer<? super E> action);

  Object[] toArray();

  <A> A[] toArray(IntFunction<A[]> generator);

  E reduce(E identity, BinaryOperator<E> accumulator);

  Optional<E> reduce(BinaryOperator<E> accumulator);

  <U> U reduce(U identity, BiFunction<U, ? super E, U> accumulator, BinaryOperator<U> combiner);

  <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super E> accumulator, BiConsumer<R, R> combiner);

  <R, A> R collect(Collector<? super E, A, R> collector);

  Optional<E> min(Comparator<? super E> comparator);

  Optional<E> max(Comparator<? super E> comparator);

  long count();

  boolean anyMatch(Predicate<? super E> predicate);

  boolean allMatch(Predicate<? super E> predicate);

  boolean noneMatch(Predicate<? super E> predicate);

  Optional<E> findFirst();

  Optional<E> findAny();

  SearchStream<E> findFirstOrElse(Supplier<? extends E> supplier);

  Stream<Map<String, Object>> mapToLabelledMaps();

  @SuppressWarnings(
    "unchecked"
  )
  <R> AggregationStream<R> groupBy(MetamodelField<E, ?>... fields);

  <R> AggregationStream<R> apply(String expression, String alias);

  @SuppressWarnings(
    "unchecked"
  )
  <R> AggregationStream<R> load(MetamodelField<E, ?>... fields);

  <R> AggregationStream<R> loadAll();

  Optional<E> min(NumericField<E, ?> field);

  Optional<E> max(NumericField<E, ?> field);

  SearchStream<E> dialect(int dialect);

  <R> AggregationStream<R> cursor(int i, Duration duration);

  SearchOperations<String> getSearchOperations();

  Page<E> getPage(Pageable pageable);

  <R> SearchStream<E> project(Function<? super E, ? extends R> field);

  @SuppressWarnings(
    "unchecked"
  )
  <R> SearchStream<E> project(MetamodelField<? super E, ? extends R>... field);

  String backingQuery();

  <R> SearchStream<E> summarize(Function<? super E, ? extends R> field);

  <R> SearchStream<E> summarize(Function<? super E, ? extends R> field, SummarizeParams params);

  <R> SearchStream<E> highlight(Function<? super E, ? extends R> field);

  <R> SearchStream<E> highlight(Function<? super E, ? extends R> field, Pair<String, String> tags);

  boolean isDocument();
}
