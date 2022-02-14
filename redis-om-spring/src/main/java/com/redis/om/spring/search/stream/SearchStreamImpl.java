package com.redis.om.spring.search.stream;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.StringJoiner;
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
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;

import com.google.gson.Gson;
import com.redis.om.spring.metamodel.FieldOperationInterceptor;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.search.stream.predicates.AndPredicate;
import com.redis.om.spring.search.stream.predicates.BetweenPredicate;
import com.redis.om.spring.search.stream.predicates.ContainsAllTagPredicate;
import com.redis.om.spring.search.stream.predicates.EqualPredicate;
import com.redis.om.spring.search.stream.predicates.GreaterThanOrEqualPredicate;
import com.redis.om.spring.search.stream.predicates.GreaterThanPredicate;
import com.redis.om.spring.search.stream.predicates.InPredicate;
import com.redis.om.spring.search.stream.predicates.InTagPredicate;
import com.redis.om.spring.search.stream.predicates.LessThanOrEqualPredicate;
import com.redis.om.spring.search.stream.predicates.LessThanPredicate;
import com.redis.om.spring.search.stream.predicates.LikePredicate;
import com.redis.om.spring.search.stream.predicates.NearPredicate;
import com.redis.om.spring.search.stream.predicates.NotEqualPredicate;
import com.redis.om.spring.search.stream.predicates.NotLikePredicate;
import com.redis.om.spring.search.stream.predicates.OrPredicate;
import com.redis.om.spring.search.stream.predicates.SearchFieldPredicate;
import com.redis.om.spring.search.stream.predicates.StartsWithPredicate;
import com.redis.om.spring.serialization.gson.GsonBuidlerFactory;
import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.TupleMapper;

import io.redisearch.Query;
import io.redisearch.Schema.FieldType;
import io.redisearch.SearchResult;
import io.redisearch.aggregation.SortedField;
import io.redisearch.aggregation.SortedField.SortOrder;
import io.redisearch.querybuilder.GeoValue;
import io.redisearch.querybuilder.Node;
import io.redisearch.querybuilder.QueryBuilder;
import io.redisearch.querybuilder.QueryNode;
import io.redisearch.querybuilder.Values;

import static io.redisearch.querybuilder.GeoValue.Unit;

public class SearchStreamImpl<E> implements SearchStream<E> {

  @SuppressWarnings("unused")
  private static final Log logger = LogFactory.getLog(SearchStreamImpl.class);

  @SuppressWarnings("unused")
  private RedisModulesOperations<String, String> modulesOperations;
  private SearchOperations<String> ops;
  private String searchIndex;
  private Class<E> entityClass;
  private Node rootNode = QueryBuilder.union();
  private static final Gson gson = GsonBuidlerFactory.getBuilder().create();
  private Optional<Integer> limit = Optional.empty();
  private Optional<Integer> skip = Optional.empty();
  private Optional<SortedField> sortBy = Optional.empty();

  public SearchStreamImpl(Class<E> entityClass, RedisModulesOperations<String, String> modulesOperations) {
    this.modulesOperations = modulesOperations;
    this.entityClass = entityClass;
    searchIndex = entityClass.getName() + "Idx";
    ops = modulesOperations.opsForSearch(searchIndex);
  }

  @Override
  public SearchStream<E> filter(SearchFieldPredicate<? super E,?> predicate) {
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

  private Node processPredicate(SearchFieldPredicate<? super E,?> predicate) {
    if (predicate.getClass() == EqualPredicate.class) {
      EqualPredicate<? super E,?> ep = (EqualPredicate<? super E, ?>)predicate;
      Object value = ep.getValue();
      String fieldName = ep.getField().getName();

      if (ep.getSearchFieldType() == FieldType.FullText) {
        return QueryBuilder.intersect(rootNode).add(fieldName, value.toString());
      } else if (ep.getSearchFieldType() == FieldType.Numeric) {
        return QueryBuilder.intersect(rootNode).add(fieldName, Values.eq(Integer.valueOf(value.toString())));
      } else if (ep.getSearchFieldType() == FieldType.Tag) {
        if (Iterable.class.isAssignableFrom(ep.getField().getType())) {
          Iterable<?> values = (Iterable<?>)value;
          QueryNode and = QueryBuilder.intersect();
          for (Object v : values) {
            and.add(fieldName, "{" + v.toString() + "}");
          }
          return QueryBuilder.intersect(rootNode, and);
        } else {
          return QueryBuilder.intersect(rootNode).add(fieldName, "{" +value.toString() + "}");
        }
      } else {
        return QueryBuilder.intersect(rootNode).add(fieldName, value.toString());
      }

    } else if (predicate.getClass() == NotEqualPredicate.class) {
      NotEqualPredicate<? super E,?> ep = (NotEqualPredicate<? super E, ?>)predicate;
      Object value = ep.getValue();
      String fieldName = ep.getField().getName();
      return QueryBuilder.intersect(rootNode).add(QueryBuilder.disjunct(fieldName, Values.value(value.toString())));
    } else if (predicate.getClass() == OrPredicate.class) {
      OrPredicate<? super E,?> op = (OrPredicate<? super E, ?>)predicate;
      Node[] nodes = op.stream().map(p -> processPredicate(p)).toArray(Node[]::new);
      return QueryBuilder.union(nodes);
    } else if (predicate.getClass() == AndPredicate.class) {
      AndPredicate<? super E,?> op = (AndPredicate<? super E, ?>)predicate;
      Node[] nodes = op.stream().map(p -> processPredicate(p)).toArray(Node[]::new);
      return QueryBuilder.intersect(nodes);
    } else if (predicate.getClass() == GreaterThanPredicate.class) {
      GreaterThanPredicate<? super E,?> ep = (GreaterThanPredicate<? super E, ?>)predicate;
      Object value = ep.getValue();
      String fieldName = ep.getField().getName();

      return QueryBuilder.intersect(rootNode).add(fieldName, Values.gt(Integer.valueOf(value.toString())));
    } else if (predicate.getClass() == GreaterThanOrEqualPredicate.class) {
      GreaterThanOrEqualPredicate<? super E,?> ep = (GreaterThanOrEqualPredicate<? super E, ?>)predicate;
      Object value = ep.getValue();
      String fieldName = ep.getField().getName();

      return QueryBuilder.intersect(rootNode).add(fieldName, Values.ge(Integer.valueOf(value.toString())));
    } else if (predicate.getClass() == LessThanPredicate.class) {
      LessThanPredicate<? super E,?> ep = (LessThanPredicate<? super E, ?>)predicate;
      Object value = ep.getValue();
      String fieldName = ep.getField().getName();

      return QueryBuilder.intersect(rootNode).add(fieldName, Values.lt(Integer.valueOf(value.toString())));
    } else if (predicate.getClass() == LessThanOrEqualPredicate.class) {
      LessThanOrEqualPredicate<? super E,?> ep = (LessThanOrEqualPredicate<? super E, ?>)predicate;
      Object value = ep.getValue();
      String fieldName = ep.getField().getName();

      return QueryBuilder.intersect(rootNode).add(fieldName, Values.le(Integer.valueOf(value.toString())));
    } else if (predicate.getClass() == BetweenPredicate.class) {
      BetweenPredicate<? super E,?> ep = (BetweenPredicate<? super E, ?>)predicate;
      Object min = ep.getMin();
      Object max = ep.getMax();
      String fieldName = ep.getField().getName();

      return QueryBuilder.intersect(rootNode).add(fieldName, Values.between(Double.valueOf(min.toString()), Double.valueOf(max.toString())));
    } else if (predicate.getClass() == InPredicate.class) {
      InPredicate<? super E,?> ip = (InPredicate<? super E, ?>)predicate;
      List<?> values = ip.getValues();
      String fieldName = ip.getField().getName();

      StringJoiner sj = new StringJoiner(" | ");
      for (Object value : values) {
          sj.add(value.toString());
      }

      if (ip.getSearchFieldType() == FieldType.Tag) {
        return QueryBuilder.intersect(rootNode).add(fieldName, "{" + sj.toString() + "}");
      } else if (ip.getSearchFieldType() == FieldType.Numeric) {
        QueryNode or = QueryBuilder.union();

        for (Object value : values) {
          or.add(fieldName, Values.eq(Integer.valueOf(value.toString())));
        }

        return QueryBuilder.intersect(rootNode, or);

      } else {
        return QueryBuilder.intersect(rootNode).add(fieldName, sj.toString());
      }
    } else if (predicate.getClass() == InTagPredicate.class) {
      InTagPredicate<? super E,?> ip = (InTagPredicate<? super E, ?>)predicate;
      List<String> values = ip.getValues();
      String fieldName = ip.getField().getName();
      StringJoiner sj = new StringJoiner(" | ");
      for (Object value : values) {
          sj.add(value.toString());
      }

      return QueryBuilder.intersect(rootNode).add(fieldName, "{" + sj.toString() + "}");

    // ContainsAllTagPredicate
    } else if (predicate.getClass() == ContainsAllTagPredicate.class) {
      ContainsAllTagPredicate<? super E,?> ip = (ContainsAllTagPredicate<? super E, ?>)predicate;
      List<String> values = ip.getValues();
      String fieldName = ip.getField().getName();

      QueryNode and = QueryBuilder.intersect();
      for (String value : values) {
        and.add(fieldName, "{" + value + "}");
      }

      return QueryBuilder.intersect(rootNode, and);
    } else if (predicate.getClass() == NearPredicate.class) {
      NearPredicate<? super E,?> np = (NearPredicate<? super E, ?>)predicate;
      String fieldName = np.getField().getName();
      Point point = np.getPoint();
      Distance distance = np.getDistance();
      GeoValue geoValue = new GeoValue(point.getX(), point.getY(), distance.getValue(), getDistanceUnit(distance));

      return QueryBuilder.intersect(rootNode).add(fieldName, geoValue);
    } else if (predicate.getClass() == LikePredicate.class) {
      LikePredicate<? super E,?> lp = (LikePredicate<? super E, ?>)predicate;
      Object value = lp.getValue();
      String fieldName = lp.getField().getName();

      return QueryBuilder.intersect(rootNode).add(fieldName, "%%%"+value.toString()+"%%%");
    } else if (predicate.getClass() == NotLikePredicate.class) {
      NotLikePredicate<? super E,?> lp = (NotLikePredicate<? super E, ?>)predicate;
      Object value = lp.getValue();
      String fieldName = lp.getField().getName();

      return QueryBuilder.intersect(rootNode).add(QueryBuilder.disjunct(fieldName, Values.value("%%%"+value.toString()+"%%%")));
    } else if (predicate.getClass() == StartsWithPredicate.class) {
      StartsWithPredicate<? super E,?> sw = (StartsWithPredicate<? super E, ?>)predicate;
      Object value = sw.getValue();
      String fieldName = sw.getField().getName();

      return QueryBuilder.intersect(rootNode).add(fieldName, value.toString()+"*");
    } else {
      return null;
    }
  }

  private static Unit getDistanceUnit(Distance distance) {
    if (distance.getUnit().equals(DistanceUnit.MILES.getAbbreviation())) {
      return GeoValue.Unit.MILES;
    } else if (distance.getUnit().equals(DistanceUnit.FEET.getAbbreviation())) {
      return GeoValue.Unit.FEET;
    } else if (distance.getUnit().equals(DistanceUnit.KILOMETERS.getAbbreviation())) {
      return GeoValue.Unit.KILOMETERS;
    } else {
      return GeoValue.Unit.METERS;
    }
  }

  private Node processPredicate(Predicate<?> predicate) {
    if (SearchFieldPredicate.class.isAssignableFrom(predicate.getClass())) {
      @SuppressWarnings("unchecked")
      SearchFieldPredicate<? super E,?> p = (SearchFieldPredicate<? super E,?>)predicate;
      return processPredicate(p);
    }
    return null;
  }

  @Override
  public <T> SearchStream<T> map(Function<? super E, ? extends T> mapper) {
    List<FieldOperationInterceptor<E, ?>> returning = new ArrayList<FieldOperationInterceptor<E, ?>>();

    if (FieldOperationInterceptor.class.isAssignableFrom(mapper.getClass())) {
      @SuppressWarnings("unchecked")
      FieldOperationInterceptor<E, T> foi = (FieldOperationInterceptor<E, T>)mapper;

      returning.add(foi);
    } else if (TupleMapper.class.isAssignableFrom(mapper.getClass())) {
      @SuppressWarnings("rawtypes")
      AbstractTupleMapper tm = (AbstractTupleMapper)mapper;

      IntStream.range(0, tm.degree()).forEach(i -> {
        @SuppressWarnings("unchecked")
        FieldOperationInterceptor<E, ?> foi = (FieldOperationInterceptor<E, ?>)tm.get(i);
        returning.add(foi);
      });
    }

    return new ReturnFieldsSearchStreamImpl<E, T>(this, returning);
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
  public <R> SearchStream<R> flatMap(Function<? super E, ? extends SearchStream<? extends R>> mapper) {
    //return stream.flatMap(mapper);
    //return resolveStream().flatMap(mapper);
    return null;
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
  public SearchStream<E> distinct() {
    //return stream.distinct();
    return null;
  }

  @Override
  public SearchStream<E> sorted() {
    // TODO possible impl: find the first "sortable" field in the schema and sort by it in ASC order
    return null;
  }

  @Override
  public SearchStream<E> sorted(Comparator<? super E> comparator) {
    if (FieldOperationInterceptor.class.isAssignableFrom(comparator.getClass())) {
      @SuppressWarnings("unchecked")
      FieldOperationInterceptor<E,?> foi = (FieldOperationInterceptor<E,?>)comparator;
      sortBy = Optional.of(SortedField.asc(foi.getField().getName()));
    }
    return this;
  }

  @Override
  public SearchStream<E> sorted(Comparator<? super E> comparator, SortOrder order) {
    if (FieldOperationInterceptor.class.isAssignableFrom(comparator.getClass())) {
      @SuppressWarnings("unchecked")
      FieldOperationInterceptor<E,?> foi = (FieldOperationInterceptor<E,?>)comparator;
      sortBy = Optional.of(new SortedField(foi.getField().getName(), order));
    }
    return this;
  }

  @Override
  public SearchStream<E> peek(Consumer<? super E> action) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SearchStream<E> limit(long maxSize) {
    limit = Optional.of(Long.valueOf(maxSize).intValue());
    return this;
  }

  @Override
  public SearchStream<E> skip(long s) {
    skip = Optional.of(Long.valueOf(s).intValue());
    return this;
  }

  @Override
  public void forEach(Consumer<? super E> action) {
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
    SearchResult searchResult = ops.search(query);

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
  public Optional<E> findFirst() { // first match unsorted
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<E> findAny() { // first match sorted
    // TODO Auto-generated method stub
    return null;
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SearchStream<E> unordered() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SearchStream<E> onClose(Runnable closeHandler) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub
  }

  SearchOperations<String> getOps() {
    return ops;
  }

  Query prepareQuery() {
    Query query = (rootNode.toString().isBlank()) ? new Query() : new Query(rootNode.toString());

    query.limit(skip.isPresent() ? skip.get() : 0, limit.isPresent() ? limit.get() : 1000000);

    if (sortBy.isPresent()) {
      SortedField sortField = sortBy.get();
      query.setSortBy(sortField.getField(), sortField.getOrder().equals("ASC"));
    }

    return query;
  }

  private SearchResult executeQuery() {
    return ops.search(prepareQuery());
  }

  private List<E> toEntityList(SearchResult searchResult) {
    return searchResult.docs.stream()
        .map(d -> gson.fromJson(d.get("$").toString(), entityClass))
        .collect(Collectors.toList());
  }

  private Stream<E> resolveStream() {
    return toEntityList(executeQuery()).stream();
  }

  public Class<E> getEntityClass() {
    return entityClass;
  }

}
