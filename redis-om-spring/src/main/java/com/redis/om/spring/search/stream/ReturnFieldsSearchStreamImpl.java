package com.redis.om.spring.search.stream;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Spliterator;
import java.util.StringTokenizer;
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
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.geo.Point;
import org.springframework.util.ReflectionUtils;

import com.google.gson.Gson;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.search.stream.predicates.SearchFieldPredicate;
import com.redis.om.spring.tuple.Tuples;
import com.redis.om.spring.util.ObjectUtils;

import io.redisearch.Query;
import io.redisearch.SearchResult;
import io.redisearch.aggregation.SortedField.SortOrder;

public class ReturnFieldsSearchStreamImpl<E, T> implements SearchStream<T> {

  @SuppressWarnings("unused")
  private static final Log logger = LogFactory.getLog(ReturnFieldsSearchStreamImpl.class);

  private final Gson gson;

  private SearchStreamImpl<E> entitySearchStream;
  private List<MetamodelField<E, ?>> returning = new ArrayList<>();
  private Stream<T> resolvedStream;
  private Runnable closeHandler;

  public ReturnFieldsSearchStreamImpl(SearchStreamImpl<E> entitySearchStream, List<MetamodelField<E, ?>> returning,
      Gson gson) {
    this.entitySearchStream = entitySearchStream;
    this.returning = returning;
    this.gson = gson;
  }

  @Override
  public Iterator<T> iterator() {
    return resolveStream().iterator();
  }

  @Override
  public Spliterator<T> spliterator() {
    return resolveStream().spliterator();
  }

  @Override
  public boolean isParallel() {
    return resolveStream().isParallel();
  }

  @Override
  public SearchStream<T> sequential() {
    return new WrapperSearchStream<>(resolveStream().sequential());
  }

  @Override
  public SearchStream<T> parallel() {
    return new WrapperSearchStream<>(resolveStream().parallel());
  }

  @Override
  public SearchStream<T> unordered() {
    return new WrapperSearchStream<>(resolveStream().unordered());
  }

  @Override
  public SearchStream<T> onClose(Runnable closeHandler) {
    this.closeHandler = closeHandler;
    return this;
  }

  @Override
  public void close() {
    if (closeHandler == null) {
      resolveStream().close();
    } else {
      resolveStream().onClose(closeHandler);
      resolveStream().close();
    }
  }

  @Override
  public SearchStream<T> filter(SearchFieldPredicate<? super T, ?> predicate) {
    throw new UnsupportedOperationException("Filter on a field predicate is not supported on mapped stream");
  }

  @SuppressWarnings("unchecked")
  @Override
  public SearchStream<T> filter(Predicate<?> predicate) {
    return new WrapperSearchStream<>(resolveStream().filter((Predicate<? super T>) predicate));
  }

  @SuppressWarnings("resource")
  @Override
  public <R> SearchStream<R> map(Function<? super T, ? extends R> mapper) {
    return new WrapperSearchStream<>(resolveStream()).map(mapper);
  }

  @Override
  public IntStream mapToInt(ToIntFunction<? super T> mapper) {
    return resolveStream().mapToInt(mapper);
  }

  @Override
  public LongStream mapToLong(ToLongFunction<? super T> mapper) {
    return resolveStream().mapToLong(mapper);
  }

  @Override
  public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
    return resolveStream().mapToDouble(mapper);
  }

  @SuppressWarnings("resource")
  @Override
  public <R> SearchStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
    return new WrapperSearchStream<>(resolveStream()).flatMap(mapper);
  }

  @SuppressWarnings("resource")
  @Override
  public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
    return new WrapperSearchStream<>(resolveStream()).flatMapToInt(mapper);
  }

  @SuppressWarnings("resource")
  @Override
  public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
    return new WrapperSearchStream<>(resolveStream()).flatMapToLong(mapper);
  }

  @SuppressWarnings("resource")
  @Override
  public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
    return new WrapperSearchStream<>(resolveStream()).flatMapToDouble(mapper);
  }

  @Override
  public SearchStream<T> sorted(Comparator<? super T> comparator) {
    return new WrapperSearchStream<>(resolveStream().sorted(comparator));
  }

  @Override
  public SearchStream<T> sorted(Comparator<? super T> comparator, SortOrder order) {
    return sorted(comparator);
  }

  @Override
  public SearchStream<T> peek(Consumer<? super T> action) {
    return new WrapperSearchStream<>(resolveStream().peek(action));
  }

  @Override
  public SearchStream<T> limit(long maxSize) {
    return new WrapperSearchStream<>(resolveStream().limit(maxSize));
  }

  @Override
  public SearchStream<T> skip(long n) {
    return new WrapperSearchStream<>(resolveStream().skip(n));
  }

  @Override
  public void forEach(Consumer<? super T> action) {
    resolveStream().forEach(action);
  }

  @Override
  public void forEachOrdered(Consumer<? super T> action) {
    resolveStream().forEachOrdered(action);
  }

  @Override
  public Object[] toArray() {
    return resolveStream().toArray();
  }

  @Override
  public <A> A[] toArray(IntFunction<A[]> generator) {
    return resolveStream().toArray(generator);
  }

  @Override
  public T reduce(T identity, BinaryOperator<T> accumulator) {
    return resolveStream().reduce(identity, accumulator);
  }

  @Override
  public Optional<T> reduce(BinaryOperator<T> accumulator) {
    return resolveStream().reduce(accumulator);
  }

  @Override
  public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
    return resolveStream().reduce(identity, accumulator, combiner);
  }

  @Override
  public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
    return resolveStream().collect(supplier, accumulator, combiner);
  }

  @Override
  public <R, A> R collect(Collector<? super T, A, R> collector) {
    return resolveStream().collect(collector);
  }

  @Override
  public Optional<T> min(Comparator<? super T> comparator) {
    return resolveStream().min(comparator);
  }

  @Override
  public Optional<T> max(Comparator<? super T> comparator) {
    return resolveStream().max(comparator);
  }

  @Override
  public long count() {
    return resolveStream().count();
  }

  @Override
  public boolean anyMatch(Predicate<? super T> predicate) {
    return resolveStream().anyMatch(predicate);
  }

  @Override
  public boolean allMatch(Predicate<? super T> predicate) {
    return resolveStream().allMatch(predicate);
  }

  @Override
  public boolean noneMatch(Predicate<? super T> predicate) {
    return resolveStream().noneMatch(predicate);
  }

  @Override
  public Optional<T> findFirst() {
    return resolveStream().findFirst();
  }

  @Override
  public Optional<T> findAny() {
    return resolveStream().findAny();
  }

  private Stream<T> resolveStream() {
    if (resolvedStream == null) {
      List<T> results = Collections.emptyList();
      Query query = entitySearchStream.prepareQuery();

      boolean resultSetHasNonIndexedFields = returning.stream().anyMatch(foi -> !foi.isIndexed());

      if (resultSetHasNonIndexedFields) {
        SearchResult searchResult = entitySearchStream.getOps().search(query);

        List<E> entities = searchResult.docs.stream()
            .map(d -> gson.fromJson(d.get("$").toString(), entitySearchStream.getEntityClass()))
            .collect(Collectors.toList());

        results = toResultTuple(entities);

      } else {
        String[] returnFields = returning.stream().map(foi -> "$." + foi.getField().getName()).toArray(String[]::new);
        query.returnFields(returnFields);
        results = toResultTuple(entitySearchStream.getOps().search(query));
      }
      resolvedStream = results.stream();
    }
    return resolvedStream;
  }

  @SuppressWarnings("unchecked")
  private List<T> toResultTuple(SearchResult searchResult) {
    List<T> results = new ArrayList<>();
    searchResult.docs.stream().forEach(doc -> {
      Map<String, Object> props = StreamSupport.stream(doc.getProperties().spliterator(), false)
          .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

      List<Object> mappedResults = new ArrayList<>();
      returning.stream().forEach(foi -> {
        String field = foi.getField().getName();
        Object value = props.get("$." + field);
        Class<?> targetClass = foi.getField().getType();
        if (targetClass == Date.class) {
          mappedResults.add(new Date(Long.valueOf(value.toString())));
        } else if (targetClass == Point.class) {
          StringTokenizer st = new StringTokenizer(value.toString(), ",");
          String lon = st.nextToken();
          String lat = st.nextToken();

          mappedResults.add(new Point(Double.parseDouble(lon), Double.parseDouble(lat)));
        } else {
          mappedResults.add(gson.fromJson(value.toString(), targetClass));
        }
      });

      if (returning.size() > 1) {
        results.add((T) Tuples.ofArray(mappedResults.toArray()));
      } else {
        results.add((T) mappedResults.get(0));
      }
    });

    return results;
  }

  @SuppressWarnings("unchecked")
  private List<T> toResultTuple(List<E> entities) {
    List<T> results = new ArrayList<>();

    entities.stream().forEach(entity -> {
      List<Object> mappedResults = new ArrayList<>();
      returning.stream().forEach(foi -> {
        String getterName = "get" + ObjectUtils.ucfirst(foi.getField().getName());
        Method getter = ReflectionUtils.findMethod(entitySearchStream.getEntityClass(), getterName);
        mappedResults.add(ReflectionUtils.invokeMethod(getter, entity));
      });

      if (returning.size() > 1) {
        results.add((T) Tuples.ofArray(mappedResults.toArray()));
      } else {
        results.add((T) mappedResults.get(0));
      }
    });

    return results;
  }

  @Override
  public Stream<Long> map(ToLongFunction<? super T> mapper) {
    return resolveStream().mapToLong(mapper).boxed();
  }

}
