package com.redis.om.spring.search.stream;

import com.google.gson.Gson;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.indexed.NumericField;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.search.stream.predicates.SearchFieldPredicate;
import com.redis.om.spring.tuple.Tuple;
import com.redis.om.spring.tuple.Tuples;
import com.redis.om.spring.util.ObjectUtils;
import com.redis.om.spring.util.SearchResultRawResponseToObjectConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.SortedField.SortOrder;
import redis.clients.jedis.util.SafeEncoder;

import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.*;

public class ReturnFieldsSearchStreamImpl<E, T> implements SearchStream<T> {

  @SuppressWarnings("unused")
  private static final Log logger = LogFactory.getLog(ReturnFieldsSearchStreamImpl.class);

  private final Gson gson;

  private final MappingRedisOMConverter mappingConverter;

  private final SearchStreamImpl<E> entitySearchStream;
  private final List<MetamodelField<E, ?>> returning;
  private Stream<T> resolvedStream;
  private Runnable closeHandler;
  private final boolean useNoContent;
  private final boolean isDocument;

  public ReturnFieldsSearchStreamImpl( //
    SearchStreamImpl<E> entitySearchStream, //
    List<MetamodelField<E, ?>> returning, //
    MappingRedisOMConverter mappingConverter, //
    Gson gson, //
    boolean isDocument //
  ) {
    this.entitySearchStream = entitySearchStream;
    this.returning = returning;
    this.gson = gson;
    this.mappingConverter = mappingConverter;
    this.useNoContent = returning.size() == 1 && returning.get(0).getSearchFieldAccessor().getField().isAnnotationPresent(Id.class);
    this.isDocument = isDocument;
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
  public SearchStream<T> filter(Example<T> example) {
    throw new UnsupportedOperationException("Filter on Example predicate is not supported on mapped stream");
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
  public SearchStream<T> sorted(Sort sort) {
    throw new UnsupportedOperationException("sorted(Sort) is not supported on a ReturnFieldSearchStream");
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
      if (useNoContent) {
        query.setNoContent();
        SearchResult searchResult = entitySearchStream.getOps().search(query);
        String keySample = searchResult.getDocuments().get(0).getId();
        int idBegin = keySample.indexOf(":") + 1;
        resolvedStream = (Stream<T>) searchResult.getDocuments().stream().map(Document::getId).map(key -> key.substring(idBegin));
      } else {
        boolean returningFullEntity = (returning.stream().anyMatch(foi -> foi.getSearchAlias().equalsIgnoreCase("__this")));

        String[] returnFields = !returningFullEntity ? returning.stream() //
            .map(foi -> ObjectUtils.isCollection(foi.getTargetClass()) ? "$." + foi.getSearchAlias() : foi.getSearchAlias())
            .toArray(String[]::new) : new String[]{};

        boolean resultSetHasNonIndexedFields = returning.stream().anyMatch(foi -> !foi.isIndexed());

        if (resultSetHasNonIndexedFields) {
          SearchResult searchResult = entitySearchStream.getOps().search(query);

          List<E> entities = searchResult
              .getDocuments() //
              .stream() //
              .map(d -> { //
                if (isDocument) {
                  return (E) gson.fromJson(SafeEncoder.encode((byte[])d.get("$")), entitySearchStream.getEntityClass());
                } else {
                  return (E) ObjectUtils.documentToObject(d, entitySearchStream.getEntityClass(), mappingConverter);
                }
              }).toList();

          results = toResultTuple(entities, returnFields);

        } else {
          query.returnFields(returnFields);
          results = toResultTuple(entitySearchStream.getOps().search(query), returnFields);
        }
        resolvedStream = results.stream();
      }
    }
    return resolvedStream;
  }

  @SuppressWarnings("unchecked")
  private List<T> toResultTuple(SearchResult searchResult, String[] returnFields) {
    List<T> results = new ArrayList<>();
    searchResult.getDocuments().forEach(doc -> {
      Map<String, Object> props = StreamSupport.stream(doc.getProperties().spliterator(), false)
          .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

      List<Object> mappedResults = new ArrayList<>();
      returning.forEach(foi -> {
        String field = foi.getSearchAlias();
        if (field.equalsIgnoreCase("__this")) {
          if (isDocument) {
            mappedResults.add(gson.fromJson(SafeEncoder.encode((byte[]) doc.get("$")), foi.getTargetClass()));
          } else {
            mappedResults.add(ObjectUtils.documentToObject(doc, foi.getTargetClass(), mappingConverter));
          }
        } else {
          Class<?> targetClass = foi.getTargetClass();
          var rawValue = props.get(ObjectUtils.isCollection(targetClass) ? "$." + field : field);
          mappedResults.add(SearchResultRawResponseToObjectConverter.process(rawValue, targetClass, gson));
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
      returning.forEach(foi -> mappedResults.add(ObjectUtils.getValueByPath(entity, foi.getJSONPath())));

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

  @SafeVarargs @Override
  public final <R> AggregationStream<R> groupBy(MetamodelField<T, ?>... field) {
    throw new UnsupportedOperationException("groupBy is not supported on a ReturnFieldSearchStream");
  }

  @Override
  public <R> AggregationStream<R> apply(String expression, String alias) {
    throw new UnsupportedOperationException("apply is not supported on a ReturnFieldSearchStream");
  }

  @SafeVarargs @Override
  public final <R> AggregationStream<R> load(MetamodelField<T, ?>... fields) {
    throw new UnsupportedOperationException("load is not supported on a ReturnFieldSearchStream");
  }

  @Override
  public <R> AggregationStream<R> loadAll() {
    throw new UnsupportedOperationException("loadAll is not supported on a ReturnFieldSearchStream");
  }

  @Override
  public Optional<T> min(NumericField<T, ?> field) {
    throw new UnsupportedOperationException("min is not supported on a ReturnFieldSearchStream");
  }

  @Override
  public Optional<T> max(NumericField<T, ?> field) {
    throw new UnsupportedOperationException("max is not supported on a ReturnFieldSearchStream");
  }

  @Override public SearchStream<T> dialect(int dialect) {
    throw new UnsupportedOperationException("dialect is not supported on a ReturnFieldSearchStream");
  }

  @Override
  public <R> AggregationStream<R> cursor(int i, Duration duration) {
    throw new UnsupportedOperationException("cursor is not supported on a ReturnFieldSearchStream");
  }

  @Override
  public SearchOperations<String> getSearchOperations() {
    throw new UnsupportedOperationException("getSearchOperations is not supported on a ReturnFieldSearchStream");
  }

  @Override
  public Slice<T> getSlice(Pageable pageable) {
    throw new UnsupportedOperationException("getPage is not supported on a ReturnFieldSearchStream");
  }

  @Override
  public <R> SearchStream<T> project(Function<? super T, ? extends R> field) {
    throw new UnsupportedOperationException("project is not supported on a ReturnFieldSearchStream");
  }

  @SafeVarargs
  @Override
  public final <R> SearchStream<T> project(MetamodelField<? super T, ? extends R>... field) {
    throw new UnsupportedOperationException("project is not supported on a ReturnFieldSearchStream");
  }

  @Override
  public String backingQuery() {
    return entitySearchStream.backingQuery();
  }

}
