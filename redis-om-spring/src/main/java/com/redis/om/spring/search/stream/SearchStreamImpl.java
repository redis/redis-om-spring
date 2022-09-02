package com.redis.om.spring.search.stream;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.search.stream.actions.TakesJSONOperations;
import com.redis.om.spring.search.stream.predicates.SearchFieldPredicate;
import com.redis.om.spring.serialization.gson.GsonBuidlerFactory;
import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.TupleMapper;
import com.redis.om.spring.util.ObjectUtils;

import io.redisearch.Query;
import io.redisearch.SearchResult;
import io.redisearch.aggregation.SortedField;
import io.redisearch.aggregation.SortedField.SortOrder;
import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;

public class SearchStreamImpl<E> implements SearchStream<E> {

  @SuppressWarnings("unused")
  private static final Log logger = LogFactory.getLog(SearchStreamImpl.class);

  private static final Integer MAX_LIMIT = 10000;

  @SuppressWarnings("unused")
  private RedisModulesOperations<String> modulesOperations;
  private SearchOperations<String> search;
  private JSONOperations<String> json;
  private String searchIndex;
  private Class<E> entityClass;
  private Node rootNode = QueryBuilder.union();
  private static final Gson gson = GsonBuidlerFactory.getBuilder().create();
  private Optional<Long> limit = Optional.empty();
  private Optional<Long> skip = Optional.empty();
  private Optional<SortedField> sortBy = Optional.empty();
  private boolean onlyIds = false;
  private Field idField;
  private Runnable closeHandler;
  private Stream<E> resolvedStream;

  public SearchStreamImpl(Class<E> entityClass, RedisModulesOperations<String> modulesOperations) {
    this.modulesOperations = modulesOperations;
    this.entityClass = entityClass;
    searchIndex = entityClass.getName() + "Idx";
    search = modulesOperations.opsForSearch(searchIndex);
    json = modulesOperations.opsForJSON();
    Optional<Field> maybeIdField = ObjectUtils.getIdFieldForEntityClass(entityClass);
    if (maybeIdField.isPresent()) {
      idField = maybeIdField.get();
    } else {
      throw new IllegalArgumentException(entityClass.getName() + " does not appear to have an ID field");
    }
  }

  @Override
  public SearchStream<E> filter(SearchFieldPredicate<? super E, ?> predicate) {
    Node node = processPredicate(predicate);
    rootNode = node;
    return this;
  }

  @Override
  public SearchStream<E> filter(Predicate<?> predicate) {
    Node node = processPredicate(predicate);
    rootNode = node;
    return this;
  }

  public Node processPredicate(SearchFieldPredicate<? super E, ?> predicate) {
    return predicate.apply(rootNode);
  }

  private Node processPredicate(Predicate<?> predicate) {
    if (SearchFieldPredicate.class.isAssignableFrom(predicate.getClass())) {
      @SuppressWarnings("unchecked")
      SearchFieldPredicate<? super E, ?> p = (SearchFieldPredicate<? super E, ?>) predicate;
      return processPredicate(p);
    }
    return rootNode;
  }

  @Override
  public <T> SearchStream<T> map(Function<? super E, ? extends T> mapper) {
    List<MetamodelField<E, ?>> returning = new ArrayList<>();

    if (MetamodelField.class.isAssignableFrom(mapper.getClass())) {
      @SuppressWarnings("unchecked")
      MetamodelField<E, T> foi = (MetamodelField<E, T>) mapper;

      returning.add(foi);
    } else if (TupleMapper.class.isAssignableFrom(mapper.getClass())) {
      @SuppressWarnings("rawtypes")
      AbstractTupleMapper tm = (AbstractTupleMapper) mapper;

      IntStream.range(0, tm.degree()).forEach(i -> {
        @SuppressWarnings("unchecked")
        MetamodelField<E, ?> foi = (MetamodelField<E, ?>) tm.get(i);
        returning.add(foi);
      });
    } else {
      if (TakesJSONOperations.class.isAssignableFrom(mapper.getClass())) {
        TakesJSONOperations tjo = (TakesJSONOperations) mapper;
        tjo.setJSONOperations(json);
      }
      return new WrapperSearchStream<>(resolveStream().map(mapper));
    }

    return new ReturnFieldsSearchStreamImpl<>(this, returning);
  }

  @Override
  public IntStream mapToInt(ToIntFunction<? super E> mapper) {
    return resolveStream().mapToInt(mapper);
  }

  @Override
  public LongStream mapToLong(ToLongFunction<? super E> mapper) {
    return resolveStream().mapToLong(mapper);
  }

  @Override
  public DoubleStream mapToDouble(ToDoubleFunction<? super E> mapper) {
    return resolveStream().mapToDouble(mapper);
  }

  @Override
  public <R> SearchStream<R> flatMap(Function<? super E, ? extends Stream<? extends R>> mapper) {
    return new WrapperSearchStream<>(resolveStream().flatMap(mapper));
  }

  @Override
  public IntStream flatMapToInt(Function<? super E, ? extends IntStream> mapper) {
    return resolveStream().flatMapToInt(mapper);
  }

  @Override
  public LongStream flatMapToLong(Function<? super E, ? extends LongStream> mapper) {
    return resolveStream().flatMapToLong(mapper);
  }

  @Override
  public DoubleStream flatMapToDouble(Function<? super E, ? extends DoubleStream> mapper) {
    return resolveStream().flatMapToDouble(mapper);
  }

  @Override
  public SearchStream<E> sorted(Comparator<? super E> comparator) {
    if (MetamodelField.class.isAssignableFrom(comparator.getClass())) {
      @SuppressWarnings("unchecked")
      MetamodelField<E, ?> foi = (MetamodelField<E, ?>) comparator;
      sortBy = Optional.of(SortedField.asc(foi.getField().getName()));
    }
    return this;
  }

  @Override
  public SearchStream<E> sorted(Comparator<? super E> comparator, SortOrder order) {
    if (MetamodelField.class.isAssignableFrom(comparator.getClass())) {
      @SuppressWarnings("unchecked")
      MetamodelField<E, ?> foi = (MetamodelField<E, ?>) comparator;
      sortBy = Optional.of(new SortedField(foi.getField().getName(), order));
    }
    return this;
  }

  @Override
  public SearchStream<E> peek(Consumer<? super E> action) {
    return new WrapperSearchStream<>(resolveStream().peek(action));
  }

  @Override
  public SearchStream<E> limit(long maxSize) {
    limit = Optional.of(maxSize);
    return this;
  }

  @Override
  public SearchStream<E> skip(long s) {
    skip = Optional.of(s);
    return this;
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    if (TakesJSONOperations.class.isAssignableFrom(action.getClass())) {
      TakesJSONOperations tjo = (TakesJSONOperations) action;
      tjo.setJSONOperations(json);
    }

    resolveStream().forEach(action);
  }

  @Override
  public void forEachOrdered(Consumer<? super E> action) {
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
  public E reduce(E identity, BinaryOperator<E> accumulator) {
    return resolveStream().reduce(identity, accumulator);
  }

  @Override
  public Optional<E> reduce(BinaryOperator<E> accumulator) {
    return resolveStream().reduce(accumulator);
  }

  @Override
  public <U> U reduce(U identity, BiFunction<U, ? super E, U> accumulator, BinaryOperator<U> combiner) {
    return resolveStream().reduce(identity, accumulator, combiner);
  }

  @Override
  public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super E> accumulator, BiConsumer<R, R> combiner) {
    return resolveStream().collect(supplier, accumulator, combiner);
  }

  @Override
  public <R, A> R collect(Collector<? super E, A, R> collector) {
    return resolveStream().collect(collector);
  }

  @Override
  public Optional<E> min(Comparator<? super E> comparator) {
    // TODO possible aggregation?
    return resolveStream().min(comparator);
  }

  @Override
  public Optional<E> max(Comparator<? super E> comparator) {
    // TODO possible aggregation?
    return resolveStream().max(comparator);
  }

  @Override
  public long count() {
    Query query = (rootNode.toString().isBlank()) ? new Query() : new Query(rootNode.toString());
    query.limit(0, 0);
    SearchResult searchResult = search.search(query);

    return searchResult.totalResults;
  }

  @Override
  public boolean anyMatch(Predicate<? super E> predicate) {
    return resolveStream().anyMatch(predicate);
  }

  @Override
  public boolean allMatch(Predicate<? super E> predicate) {
    return resolveStream().allMatch(predicate);
  }

  @Override
  public boolean noneMatch(Predicate<? super E> predicate) {
    return resolveStream().noneMatch(predicate);
  }

  @Override
  public Optional<E> findFirst() {
    limit = Optional.of(1L);
    return resolveStream().findFirst();
  }

  @Override
  public Optional<E> findAny() {
    return findFirst();
  }

  @Override
  public Iterator<E> iterator() {
    return resolveStream().iterator();
  }

  @Override
  public Spliterator<E> spliterator() {
    return resolveStream().spliterator();
  }

  @Override
  public boolean isParallel() {
    return false;
  }

  @Override
  public SearchStream<E> sequential() {
    return this;
  }

  @Override
  public SearchStream<E> parallel() {
    return this;
  }

  @Override
  public SearchStream<E> unordered() {
    return this;
  }

  @Override
  public SearchStream<E> onClose(Runnable closeHandler) {
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

  SearchOperations<String> getOps() {
    return search;
  }

  Query prepareQuery() {
    Query query = (rootNode.toString().isBlank()) ? new Query() : new Query(rootNode.toString());

    query.limit(skip.isPresent() ? skip.get().intValue() : 0, limit.isPresent() ? limit.get().intValue() : MAX_LIMIT);

    if (sortBy.isPresent()) {
      SortedField sortField = sortBy.get();
      query.setSortBy(sortField.getField(), sortField.getOrder().equals("ASC"));
    }

    if (onlyIds) {
      query.returnFields(idField.getName());
    }

    return query;
  }

  private SearchResult executeQuery() {
    return search.search(prepareQuery());
  }

  private List<E> toEntityList(SearchResult searchResult) {
    return searchResult.docs.stream().map(d -> gson.fromJson(d.get("$").toString(), entityClass))
        .collect(Collectors.toList());
  }

  private Stream<E> resolveStream() {
    if (resolvedStream == null) {
      resolvedStream = toEntityList(executeQuery()).stream();
    }
    return resolvedStream;
  }

  public Class<E> getEntityClass() {
    return entityClass;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Stream<Long> map(ToLongFunction<? super E> mapper) {
    Stream<Long> result = Stream.empty();

    if (TakesJSONOperations.class.isAssignableFrom(mapper.getClass())) {
      TakesJSONOperations tjo = (TakesJSONOperations) mapper;
      tjo.setJSONOperations(json);

      onlyIds = true;

      Method idSetter = ObjectUtils.getSetterForField(entityClass, idField);
      Stream<E> wrappedIds = (Stream<E>) executeQuery().docs //
          .stream() //
          .map(d -> {
            try {
              String key = idField.getType().getDeclaredConstructor((Class<?>)idField.getType())
                  .newInstance(d.getId()).toString();
              return key.substring(key.indexOf(":") + 1);
            } catch (Exception e) {
              return null;
            }
          }).filter(Objects::nonNull).map(id -> {
            Object entity;
            try {
              entity = entityClass.getDeclaredConstructor().newInstance();
              idSetter.invoke(entity, id);
            } catch (Exception e) {
              entity = null;
            }

            return entity;
          });

      result = wrappedIds.mapToLong(mapper).boxed();
    }
    return result;
  }

}
