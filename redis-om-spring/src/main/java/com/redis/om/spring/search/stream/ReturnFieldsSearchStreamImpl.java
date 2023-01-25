package com.redis.om.spring.search.stream;

import com.google.gson.Gson;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.indexed.NumericField;
import com.redis.om.spring.search.stream.predicates.SearchFieldPredicate;
import com.redis.om.spring.tuple.Tuple;
import com.redis.om.spring.tuple.Tuples;
import com.redis.om.spring.util.ObjectUtils;
import io.redisearch.Query;
import io.redisearch.SearchResult;
import io.redisearch.aggregation.SortedField.SortOrder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.geo.Point;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.*;

public class ReturnFieldsSearchStreamImpl<E, T> implements SearchStream<T> {

  @SuppressWarnings("unused")
  private static final Log logger = LogFactory.getLog(ReturnFieldsSearchStreamImpl.class);

  private final Gson gson;

  private final SearchStreamImpl<E> entitySearchStream;
  private List<MetamodelField<E, ?>> returning;
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
      resolveStream().onClose(closeHandler).close();
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

  @Override
  public SearchStream<T> filter(String freeText) {
    throw new UnsupportedOperationException("Filter on free text predicate is not supported on mapped stream");
  }

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

  @Override
  public <R> SearchStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
    return new WrapperSearchStream<>(resolveStream()).flatMap(mapper);
  }

  @Override
  public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
    return new WrapperSearchStream<>(resolveStream()).flatMapToInt(mapper);
  }

  @Override
  public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
    return new WrapperSearchStream<>(resolveStream()).flatMapToLong(mapper);
  }

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
      List<T> results;
      Query query = entitySearchStream.prepareQuery();
      String[] returnFields = returning.stream().map(foi -> "$." + foi.getSearchAlias()).toArray(String[]::new);

      boolean resultSetHasNonIndexedFields = returning.stream().anyMatch(foi -> !foi.isIndexed());

      if (resultSetHasNonIndexedFields) {
        SearchResult searchResult = entitySearchStream.getOps().search(query);

        List<E> entities = searchResult.docs.stream()
            .map(d -> gson.fromJson(d.get("$").toString(), entitySearchStream.getEntityClass()))
            .collect(Collectors.toList());

        results = toResultTuple(entities, returnFields);

      } else {

        query.returnFields(returnFields);
        results = toResultTuple(entitySearchStream.getOps().search(query), returnFields);
      }
      resolvedStream = results.stream();
    }
    return resolvedStream;
  }

  @SuppressWarnings("unchecked")
  private List<T> toResultTuple(SearchResult searchResult, String[] returnFields) {
    List<T> results = new ArrayList<>();
    searchResult.docs.forEach(doc -> {
      Map<String, Object> props = StreamSupport.stream(doc.getProperties().spliterator(), false)
          .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

      List<Object> mappedResults = new ArrayList<>();
      returning.forEach(foi -> {
        String field = foi.getSearchAlias();
        Object value = props.get("$." + field);
        Class<?> targetClass = foi.getTargetClass();
        if (targetClass == Date.class) {
          mappedResults.add(new Date(Long.parseLong(value.toString())));
        } else if (targetClass == Point.class) {
          StringTokenizer st = new StringTokenizer(value.toString(), ",");
          String lon = st.nextToken();
          String lat = st.nextToken();

          mappedResults.add(new Point(Double.parseDouble(lon), Double.parseDouble(lat)));
        } else if (targetClass == String.class) {
          mappedResults.add(value.toString());
        } else {
          mappedResults.add(gson.fromJson(value.toString(), targetClass));
        }
      });

      if (returning.size() > 1) {
        results.add((T) Tuples.ofArray(returnFields, mappedResults.toArray()));
      } else {
        results.add((T) mappedResults.get(0));
      }
    });

    return results;
  }

  @SuppressWarnings("unchecked")
  private List<T> toResultTuple(List<E> entities, String[] returnFields) {
    List<T> results = new ArrayList<>();

    entities.forEach(entity -> {
      List<Object> mappedResults = new ArrayList<>();
      returning.forEach(foi -> {
        String getterName = "get" + ObjectUtils.ucfirst(foi.getSearchAlias());
        Method getter = ReflectionUtils.findMethod(entitySearchStream.getEntityClass(), getterName);
        mappedResults.add(ReflectionUtils.invokeMethod(getter, entity));
      });

      if (returning.size() > 1) {
        results.add((T) Tuples.ofArray(returnFields, mappedResults.toArray()));
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

  @Override
  public Stream<Map<String, Object>> mapToLabelledMaps() {
    return resolveStream().map(Tuple.class::cast).map(Tuple::labelledMap);
  }

  @Override
  public <R> AggregationStream<R> groupBy(MetamodelField<T, ?>... field) {
    throw new UnsupportedOperationException("groupBy is not supported on a ReturnFieldSearchStream");
  }

  @Override public <R> AggregationStream<R> apply(String expression, String alias) {
    throw new UnsupportedOperationException("apply is not supported on a ReturnFieldSearchStream");
  }

  @Override public <R> AggregationStream<R> load(MetamodelField<T, ?>... fields) {
    throw new UnsupportedOperationException("load is not supported on a ReturnFieldSearchStream");
  }

  @Override public Optional<T> min(NumericField<T, ?> field) {
    throw new UnsupportedOperationException("min is not supported on a ReturnFieldSearchStream");
  }

  @Override public Optional<T> max(NumericField<T, ?> field) {
    throw new UnsupportedOperationException("max is not supported on a ReturnFieldSearchStream");
  }

}
