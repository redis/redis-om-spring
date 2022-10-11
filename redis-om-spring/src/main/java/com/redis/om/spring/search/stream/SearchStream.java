package com.redis.om.spring.search.stream;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.redis.om.spring.search.stream.predicates.SearchFieldPredicate;

import io.redisearch.aggregation.SortedField.SortOrder;

public interface SearchStream<E> extends BaseStream<E, SearchStream<E>> {

  SearchStream<E> filter(SearchFieldPredicate<? super E, ?> predicate);

  SearchStream<E> filter(Predicate<?> predicate);

  <R> SearchStream<R> map(Function<? super E, ? extends R> field);
  
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
  
  Stream<Map<String,Object>> mapToLabelledMaps();
}
