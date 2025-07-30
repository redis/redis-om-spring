package com.redis.om.spring.search.stream;

import static com.redis.om.spring.metamodel.MetamodelUtils.getMetamodelForIdField;
import static com.redis.om.spring.util.ObjectUtils.floatArrayToByteArray;
import static java.util.stream.Collectors.toCollection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redis.om.spring.annotations.Dialect;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.metamodel.indexed.NumericField;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.search.stream.actions.TakesJSONOperations;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.SearchFieldPredicate;
import com.redis.om.spring.search.stream.predicates.lexicographic.*;
import com.redis.om.spring.search.stream.predicates.vector.KNNPredicate;
import com.redis.om.spring.tuple.AbstractTupleMapper;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.tuple.TupleMapper;
import com.redis.om.spring.util.ObjectUtils;
import com.redis.om.spring.util.SearchResultRawResponseToObjectConverter;

import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.Query.HighlightTags;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.aggr.SortedField;
import redis.clients.jedis.search.aggr.SortedField.SortOrder;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.util.SafeEncoder;

/**
 * Implementation of {@link SearchStream} that provides search capabilities for Redis OM entities.
 * <p>
 * This class implements a fluent API for building and executing Redis Search queries against
 * indexed documents. It supports various search operations including filtering, sorting,
 * pagination, aggregation, and vector similarity search.
 * </p>
 * <p>
 * The stream integrates with Redis Search (RediSearch) module to provide:
 * <ul>
 * <li>Full-text and field-based search</li>
 * <li>Vector similarity search (KNN)</li>
 * <li>Query-by-example (QBE) functionality</li>
 * <li>Result projection and summarization</li>
 * <li>Highlighting and sorting</li>
 * </ul>
 *
 * @param <E> the entity type being searched
 */
public class SearchStreamImpl<E> implements SearchStream<E> {

  @SuppressWarnings(
    "unused"
  )
  private static final Log logger = LogFactory.getLog(SearchStreamImpl.class);

  private static final Integer MAX_LIMIT = 10000;

  @SuppressWarnings(
    "unused"
  )
  private final RedisModulesOperations<String> modulesOperations;
  private final SearchOperations<String> search;
  private final JSONOperations<String> json;
  private final String searchIndex;
  private final Class<E> entityClass;
  private final GsonBuilder gsonBuilder;
  private final Field idField;
  private final boolean isDocument;
  private final MappingRedisOMConverter mappingConverter;
  private final List<MetamodelField<E, ?>> projections = new ArrayList<>();
  private final List<MetamodelField<E, ?>> summaryFields = new ArrayList<>();
  private final List<MetamodelField<E, ?>> highlightFields = new ArrayList<>();
  private final ExampleToNodeConverter<E> exampleToNodeConverter;
  private final RediSearchIndexer indexer;
  private Node rootNode = QueryBuilders.union();
  private Gson gson;
  private Long limit;
  private Long skip;
  private SortedField sortBy;
  private boolean onlyIds = false;
  private Runnable closeHandler;
  private Stream<E> resolvedStream;
  private KNNPredicate<E, ?> knnPredicate;
  private int dialect = Dialect.TWO.getValue();
  private SummarizeParams summarizeParams;
  private Pair<String, String> highlightTags;
  private boolean isQBE = false;

  /**
   * Creates a new SearchStreamImpl for the given entity class.
   * <p>
   * This constructor automatically determines the search index name and ID field
   * from the entity class annotations and introspection.
   * </p>
   *
   * @param entityClass       the entity class to create a search stream for
   * @param modulesOperations the Redis modules operations instance
   * @param gsonBuilder       the Gson builder for JSON serialization
   * @param indexer           the Redis search indexer
   * @throws IllegalArgumentException if the entity class does not have an ID field
   */
  public SearchStreamImpl(Class<E> entityClass, RedisModulesOperations<String> modulesOperations,
      GsonBuilder gsonBuilder, RediSearchIndexer indexer) {
    this.indexer = indexer;
    this.modulesOperations = modulesOperations;
    this.entityClass = entityClass;
    this.searchIndex = this.indexer.getIndexName(entityClass);
    this.search = modulesOperations.opsForSearch(searchIndex);
    this.json = modulesOperations.opsForJSON();
    this.gsonBuilder = gsonBuilder;
    Optional<Field> maybeIdField = ObjectUtils.getIdFieldForEntityClass(entityClass);
    if (maybeIdField.isPresent()) {
      this.idField = maybeIdField.get();
    } else {
      throw new IllegalArgumentException(entityClass.getName() + " does not appear to have an ID field");
    }
    this.isDocument = entityClass.isAnnotationPresent(Document.class);
    this.mappingConverter = new MappingRedisOMConverter(null, new ReferenceResolverImpl(modulesOperations.template()));
    this.exampleToNodeConverter = new ExampleToNodeConverter<>(indexer);
  }

  /**
   * Creates a new SearchStreamImpl with explicit search index and ID field configuration.
   * <p>
   * This constructor allows for more explicit control over the search index and ID field,
   * useful when working with custom search indexes or when the automatic detection
   * is not sufficient.
   * </p>
   *
   * @param entityClass       the entity class to create a search stream for
   * @param searchIndex       the explicit search index name to use
   * @param idField           the ID field of the entity
   * @param modulesOperations the Redis modules operations instance
   * @param gsonBuilder       the Gson builder for JSON serialization
   * @param indexer           the Redis search indexer
   */
  public SearchStreamImpl(Class<E> entityClass, String searchIndex, Field idField,
      RedisModulesOperations<String> modulesOperations, GsonBuilder gsonBuilder, RediSearchIndexer indexer) {
    this.indexer = indexer;
    this.modulesOperations = modulesOperations;
    this.entityClass = entityClass;
    this.searchIndex = searchIndex;
    this.search = modulesOperations.opsForSearch(searchIndex);
    this.json = modulesOperations.opsForJSON();
    this.gsonBuilder = gsonBuilder;
    this.idField = idField;
    List indexDefinition = (List) search.getInfo().get("index_definition");
    String keyType = IntStream.range(0, indexDefinition.size() - 1).filter(i -> "key_type".equals(indexDefinition.get(
        i))).mapToObj(i -> indexDefinition.get(i + 1)).findFirst().map(Object::toString).orElse(null);

    this.isDocument = "JSON".equals(keyType);
    this.mappingConverter = new MappingRedisOMConverter(null, new ReferenceResolverImpl(modulesOperations.template()));
    this.exampleToNodeConverter = new ExampleToNodeConverter<>(indexer);
  }

  @Override
  @SuppressWarnings(
    "unchecked"
  )
  public SearchStream<E> filter(SearchFieldPredicate<? super E, ?> predicate) {
    if (predicate instanceof KNNPredicate) {
      knnPredicate = (KNNPredicate<E, ?>) predicate;
    } else {
      rootNode = processPredicate(predicate);
    }
    return this;
  }

  @Override
  public SearchStream<E> filter(Predicate<?> predicate) {
    rootNode = processPredicate(predicate);
    return this;
  }

  @Override
  public SearchStream<E> filter(String freeText) {
    Node freeTextNode = new Node() {
      @Override
      public String toString() {
        return freeText;
      }

      @Override
      public String toString(Parenthesize mode) {
        return switch (mode) {
          case NEVER -> toString();
          case ALWAYS, DEFAULT -> String.format("(%s)", this);
        };
      }
    };
    rootNode = (rootNode.toString().isBlank()) ? freeTextNode : QueryBuilders.intersect(rootNode, freeTextNode);
    return this;
  }

  @Override
  public SearchStream<E> filter(Example<E> example) {
    isQBE = true;
    rootNode = exampleToNodeConverter.processExample(example, rootNode);
    return this;
  }

  @Override
  public <T> SearchStream<E> filterIfNotNull(T value, Supplier<SearchFieldPredicate<? super E, ?>> predicateSupplier) {
    if (value != null) {
      return filter(predicateSupplier.get());
    }
    return this;
  }

  @Override
  public SearchStream<E> filterIfNotBlank(String value,
      Supplier<SearchFieldPredicate<? super E, ?>> predicateSupplier) {
    if (value != null && !value.isBlank()) {
      return filter(predicateSupplier.get());
    }
    return this;
  }

  @Override
  public <T> SearchStream<E> filterIfPresent(Optional<T> value,
      Supplier<SearchFieldPredicate<? super E, ?>> predicateSupplier) {
    if (value.isPresent()) {
      return filter(predicateSupplier.get());
    }
    return this;
  }

  /**
   * Processes a search field predicate and applies it to the current query tree.
   * <p>
   * This method takes a search field predicate and integrates it with the existing
   * root query node, allowing predicates to be combined into complex search expressions.
   * </p>
   *
   * @param predicate the search field predicate to process
   * @return the query node representing the processed predicate
   */
  public Node processPredicate(SearchFieldPredicate<? super E, ?> predicate) {
    // Handle lexicographic predicates specially
    if (predicate instanceof LexicographicPredicate) {
      return processLexicographicPredicate(predicate);
    }
    return predicate.apply(rootNode);
  }

  private Node processPredicate(Predicate<?> predicate) {
    if (SearchFieldPredicate.class.isAssignableFrom(predicate.getClass())) {
      @SuppressWarnings(
        "unchecked"
      ) SearchFieldPredicate<? super E, ?> p = (SearchFieldPredicate<? super E, ?>) predicate;
      return processPredicate(p);
    }
    return rootNode;
  }

  private Node processLexicographicPredicate(SearchFieldPredicate<? super E, ?> predicate) {
    // Cast to BaseAbstractPredicate to access the search field accessor
    if (!(predicate instanceof BaseAbstractPredicate)) {
      throw new IllegalArgumentException("Lexicographic predicates must extend BaseAbstractPredicate");
    }
    SearchFieldAccessor searchFieldAccessor = ((BaseAbstractPredicate<?, ?>) predicate).getSearchFieldAccessor();

    if (predicate instanceof LexicographicGreaterThanMarker) {
      LexicographicGreaterThanPredicate<E, ?> actualPredicate = new LexicographicGreaterThanPredicate<>(
          searchFieldAccessor, ((LexicographicGreaterThanMarker<E, ?>) predicate).getValue(), modulesOperations,
          indexer);
      return actualPredicate.apply(rootNode);
    } else if (predicate instanceof LexicographicLessThanMarker) {
      LexicographicLessThanPredicate<E, ?> actualPredicate = new LexicographicLessThanPredicate<>(searchFieldAccessor,
          ((LexicographicLessThanMarker<E, ?>) predicate).getValue(), modulesOperations, indexer);
      return actualPredicate.apply(rootNode);
    } else if (predicate instanceof LexicographicBetweenMarker) {
      LexicographicBetweenMarker<E, ?> marker = (LexicographicBetweenMarker<E, ?>) predicate;
      LexicographicBetweenPredicate<E, ?> actualPredicate = new LexicographicBetweenPredicate<>(searchFieldAccessor,
          marker.getMin(), marker.getMax(), modulesOperations, indexer);
      return actualPredicate.apply(rootNode);
    }
    throw new IllegalArgumentException("Unknown lexicographic predicate type: " + predicate.getClass());
  }

  @Override
  public <T> SearchStream<T> map(Function<? super E, ? extends T> mapper) {
    List<MetamodelField<E, ?>> returning = new ArrayList<>();

    if (MetamodelField.class.isAssignableFrom(mapper.getClass())) {
      @SuppressWarnings(
        "unchecked"
      ) MetamodelField<E, T> foi = (MetamodelField<E, T>) mapper;

      returning.add(foi);
    } else if (TupleMapper.class.isAssignableFrom(mapper.getClass())) {
      @SuppressWarnings(
        "rawtypes"
      ) AbstractTupleMapper tm = (AbstractTupleMapper) mapper;

      IntStream.range(0, tm.degree()).forEach(i -> {
        @SuppressWarnings(
          "unchecked"
        ) MetamodelField<E, ?> foi = (MetamodelField<E, ?>) tm.get(i);
        returning.add(foi);
      });
    } else {
      if (TakesJSONOperations.class.isAssignableFrom(mapper.getClass())) {
        TakesJSONOperations tjo = (TakesJSONOperations) mapper;
        tjo.setJSONOperations(json);
      }
      return new WrapperSearchStream<>(resolveStream().map(mapper));
    }

    resolvedStream = Stream.empty();

    return new ReturnFieldsSearchStreamImpl<>(this, returning, mappingConverter, getGson(), isDocument);
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
      @SuppressWarnings(
        "unchecked"
      ) MetamodelField<E, ?> foi = (MetamodelField<E, ?>) comparator;
      sortBy = SortedField.asc(foi.getSearchAlias());
    }
    return this;
  }

  @Override
  public SearchStream<E> sorted(Comparator<? super E> comparator, SortOrder order) {
    if (MetamodelField.class.isAssignableFrom(comparator.getClass())) {
      @SuppressWarnings(
        "unchecked"
      ) MetamodelField<E, ?> foi = (MetamodelField<E, ?>) comparator;
      sortBy = new SortedField(foi.getSearchAlias(), order);
    }
    return this;
  }

  @Override
  public SearchStream<E> sorted(Sort sort) {
    Optional<Order> maybeOrder = sort.stream().sorted().findFirst();
    if (maybeOrder.isPresent()) {
      Order order = maybeOrder.get();
      sortBy = new SortedField(order.getProperty(), order.isAscending() ? SortOrder.ASC : SortOrder.DESC);
    }
    return this;
  }

  @Override
  public SearchStream<E> peek(Consumer<? super E> action) {
    return new WrapperSearchStream<>(resolveStream().peek(action));
  }

  @Override
  public SearchStream<E> limit(long maxSize) {
    limit = maxSize;
    return this;
  }

  @Override
  public SearchStream<E> skip(long s) {
    skip = s;
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
    return resolveStream().min(comparator);
  }

  @Override
  public Optional<E> max(Comparator<? super E> comparator) {
    return resolveStream().max(comparator);
  }

  @Override
  public long count() {
    if (!rootNode.toString().isBlank()) {
      // Trim any leading/trailing spaces from the query to avoid syntax errors with DIALECT
      String queryString = rootNode.toString().trim();
      Query query = new Query(queryString);
      query.limit(0, 0);
      query.dialect(dialect); // Use the configured dialect value
      SearchResult searchResult = search.search(query);
      resolvedStream = Stream.empty();

      return searchResult.getTotalResults();
    } else {
      var info = search.getInfo();
      return (long) info.get("num_docs");
    }
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
    limit = 1L;
    return resolveStream().findFirst();
  }

  @Override
  public Optional<E> findAny() {
    return findFirst();
  }

  @Override
  public SearchStream<E> findFirstOrElse(Supplier<? extends E> supplier) {
    if (resolvedStream == null) {
      resolvedStream = toEntityList(executeQuery()).stream();
    }

    if (resolvedStream.findFirst().isEmpty()) {
      var entity = supplier.get();
      if (entity != null) {
        resolvedStream = Stream.of(entity);
      }
    }

    return this;
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
      resolveStream().onClose(closeHandler).close();
    }
  }

  SearchOperations<String> getOps() {
    return search;
  }

  Query prepareQuery() {
    Query query;

    if (knnPredicate != null) {
      query = new Query(knnPredicate.apply(rootNode).toString());
      query.dialect(Dialect.TWO.getValue());
      query.addParam(knnPredicate.getBlobAttributeName(), knnPredicate.getBlobAttribute() != null ?
          knnPredicate.getBlobAttribute() :
          floatArrayToByteArray(knnPredicate.getDoublesAttribute()));
      query.addParam("K", knnPredicate.getK());
    } else {
      query = (rootNode.toString().isBlank()) ? new Query() : new Query(rootNode.toString());
      query.dialect(Dialect.TWO.getValue());
      query.dialect(dialect);
    }

    query.limit(skip != null ? skip.intValue() : 0, limit != null ? limit.intValue() : MAX_LIMIT);

    if (sortBy != null) {
      SortedField sortField = sortBy;
      query.setSortBy(sortField.getField(), sortField.getOrder().equals("ASC"));
    }

    if (!summaryFields.isEmpty()) {
      var fields = summaryFields.stream() //
          .map(foi -> ObjectUtils.isCollection(foi.getTargetClass()) ?
              "$." + foi.getSearchAlias() :
              foi.getSearchAlias()).collect(toCollection(ArrayList::new));

      if (summarizeParams == null) {
        query.summarizeFields(fields.toArray(String[]::new));
      } else {
        query.summarizeFields( //
            summarizeParams.getFragSize(), //
            summarizeParams.getFragsNum(), //
            summarizeParams.getSeparator(), //
            fields.toArray(String[]::new) //
        );
      }
    }

    if (!highlightFields.isEmpty()) {
      var fields = highlightFields.stream() //
          .map(foi -> ObjectUtils.isCollection(foi.getTargetClass()) ?
              "$." + foi.getSearchAlias() :
              foi.getSearchAlias()).collect(toCollection(ArrayList::new));

      if (highlightTags == null) {
        query.highlightFields(fields.toArray(String[]::new));
      } else {
        HighlightTags tags = new HighlightTags(highlightTags.getFirst(), highlightTags.getSecond());
        query.highlightFields(tags, fields.toArray(String[]::new));
      }
    }

    if (onlyIds) {
      query.returnFields(idField.getName());
    } else if (!projections.isEmpty()) {
      var returnFields = projections.stream() //
          .map(foi -> ObjectUtils.isCollection(foi.getTargetClass()) ?
              "$." + foi.getSearchAlias() :
              foi.getSearchAlias()).collect(toCollection(ArrayList::new));
      returnFields.add(idField.getName());

      query.returnFields(returnFields.toArray(String[]::new));
    }

    return query;
  }

  private SearchResult executeQuery() {
    try {
      Query query = prepareQuery();
      return search.search(query);
    } catch (JedisDataException jde) {
      if (isQBE && jde.getMessage().contains("not loaded nor in schema")) {
        throw new UnsupportedOperationException("The example object properties are not part of the search schema", jde);
      } else
        throw jde;
    }
  }

  @SuppressWarnings(
    "unchecked"
  )
  private List<E> toEntityList(SearchResult searchResult) {
    if (projections.isEmpty()) {
      if (isDocument) {
        Gson g = getGson();
        return searchResult.getDocuments().stream().map(d -> {
          E entity = g.fromJson(SafeEncoder.encode((byte[]) d.get("$")), entityClass);
          return ObjectUtils.populateRedisKey(entity, d.getId());
        }).toList();
      } else {
        return searchResult.getDocuments().stream().map(d -> {
          E entity = (E) ObjectUtils.documentToObject(d, entityClass, mappingConverter);
          return ObjectUtils.populateRedisKey(entity, d.getId());
        }).toList();
      }
    } else {
      List<E> projectedEntities = new ArrayList<>();
      searchResult.getDocuments().forEach(doc -> {
        Map<String, Object> props = StreamSupport.stream(doc.getProperties().spliterator(), false).collect(Collectors
            .toMap(Entry::getKey, Entry::getValue));

        E entity = BeanUtils.instantiateClass(this.entityClass);
        projections.forEach(foi -> {
          String field = foi.getSearchAlias();
          Class<?> targetClass = foi.getTargetClass();

          var rawValue = props.get(ObjectUtils.isCollection(targetClass) ? "$." + field : field);
          Object processValue = SearchResultRawResponseToObjectConverter.process(rawValue, targetClass, getGson());

          if (processValue != null) {
            try {
              foi.getSearchFieldAccessor().getField().set(entity, processValue);
            } catch (IllegalAccessException e) {
              logger.debug("🧨 couldn't set value on " + field, e);
            }
          }
        });
        projectedEntities.add(entity);
      });
      return projectedEntities;
    }
  }

  private Stream<E> resolveStream() {
    if (resolvedStream == null) {
      resolvedStream = toEntityList(executeQuery()).stream();
    }
    return resolvedStream;
  }

  private boolean isStreamResolved() {
    return resolvedStream != null;
  }

  @Override
  public Class<E> getEntityClass() {
    return entityClass;
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public Stream<Long> map(ToLongFunction<? super E> mapper) {
    Stream<Long> result = Stream.empty();

    if (TakesJSONOperations.class.isAssignableFrom(mapper.getClass())) {
      TakesJSONOperations tjo = (TakesJSONOperations) mapper;
      tjo.setJSONOperations(json);

      onlyIds = true;

      Method idSetter = ObjectUtils.getSetterForField(entityClass, idField);
      Stream<E> wrappedIds = (Stream<E>) executeQuery().getDocuments() //
          .stream() //
          .map(d -> {
            try {
              String key = idField.getType().getDeclaredConstructor(idField.getType()).newInstance(d.getId())
                  .toString();
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
    resolvedStream = Stream.empty();

    return result;
  }

  @Override
  public Stream<Map<String, Object>> mapToLabelledMaps() {
    throw new UnsupportedOperationException("mapToLabelledMaps is not supported on a SearchStream");
  }

  @SafeVarargs
  @Override
  public final <R> AggregationStream<R> groupBy(MetamodelField<E, ?>... fields) {
    resolvedStream = Stream.empty();
    String query = (rootNode.toString().isBlank()) ? "*" : rootNode.toString();
    return new AggregationStreamImpl<>(searchIndex, modulesOperations, getGson(), entityClass, query, fields);
  }

  @Override
  public <R> AggregationStream<R> apply(String expression, String alias) {
    resolvedStream = Stream.empty();
    String query = (rootNode.toString().isBlank()) ? "*" : rootNode.toString();
    AggregationStream<R> aggregationStream = new AggregationStreamImpl<>(searchIndex, modulesOperations, getGson(),
        entityClass, query);
    aggregationStream.apply(expression, alias);
    return aggregationStream;
  }

  @SafeVarargs
  @Override
  public final <R> AggregationStream<R> load(MetamodelField<E, ?>... fields) {
    resolvedStream = Stream.empty();
    String query = (rootNode.toString().isBlank()) ? "*" : rootNode.toString();
    AggregationStream<R> aggregationStream = new AggregationStreamImpl<>(searchIndex, modulesOperations, getGson(),
        entityClass, query);
    aggregationStream.load(fields);
    return aggregationStream;
  }

  @Override
  public <R> AggregationStream<R> loadAll() {
    resolvedStream = Stream.empty();
    String query = (rootNode.toString().isBlank()) ? "*" : rootNode.toString();
    AggregationStream<R> aggregationStream = new AggregationStreamImpl<>(searchIndex, modulesOperations, getGson(),
        entityClass, query);
    aggregationStream.loadAll();
    return aggregationStream;
  }

  @Override
  public <R> AggregationStream<R> cursor(int count, Duration timeout) {
    resolvedStream = Stream.empty();
    String query = (rootNode.toString().isBlank()) ? "*" : rootNode.toString();
    AggregationStream<R> aggregationStream = new AggregationStreamImpl<>(searchIndex, modulesOperations, getGson(),
        entityClass, query);
    aggregationStream.cursor(count, timeout);
    return aggregationStream;
  }

  @Override
  public Optional<E> min(NumericField<E, ?> field) {
    resolvedStream = Stream.empty();
    List<Pair<String, ?>> minByField = this //
        .load(new MetamodelField<E, String>("__key", String.class)) //
        .sorted(Order.asc("@" + field.getSearchAlias())).limit(1) //
        .toList(String.class, Double.class);

    return minByField.isEmpty() ?
        Optional.empty() :
        Optional.ofNullable(json.get(minByField.get(0).getFirst(), entityClass));
  }

  @Override
  public Optional<E> max(NumericField<E, ?> field) {
    resolvedStream = Stream.empty();
    List<Pair<String, ?>> maxByField = this //
        .load(new MetamodelField<E, String>("__key", String.class)) //
        .sorted(1, Order.desc("@" + field.getSearchAlias())).limit(1) //
        .toList(String.class, Double.class);

    return maxByField.isEmpty() ?
        Optional.empty() :
        Optional.ofNullable(json.get(maxByField.get(0).getFirst(), entityClass));
  }

  @Override
  public SearchStream<E> dialect(int dialect) {
    this.dialect = dialect;
    return this;
  }

  @Override
  public SearchOperations<String> getSearchOperations() {
    return search;
  }

  @Override
  public Page<E> getPage(Pageable pageable) {
    if (pageable.getClass().isAssignableFrom(AggregationPageable.class)) {
      resolvedStream = Stream.empty();
      AggregationPageable ap = (AggregationPageable) pageable;
      AggregationResult ar = search.cursorRead(ap.getCursorId(), pageable.getPageSize());
      return new AggregationPage<>(ar, pageable, entityClass, getGson(), mappingConverter, isDocument, this.search);
    } else {
      if (!isStreamResolved()) {
        this.sorted(pageable.getSort()).limit(pageable.getPageSize()).skip(Math.toIntExact(pageable.getOffset()));
        // issue a count query to answer the hasNext? question for the slice/page
        Query countQuery = (rootNode.toString().isBlank()) ? new Query() : new Query(rootNode.toString());
        countQuery.limit(Math.toIntExact(pageable.getOffset() + pageable.getPageSize()), pageable.getPageSize());
        SearchResult searchResult = search.search(countQuery);

        return new PageImpl<>(this.resolveStream().toList(), pageable, searchResult.getTotalResults());
      } else {
        return new PageImpl<E>(List.of());
      }

    }
  }

  @Override
  @SuppressWarnings(
    "unchecked"
  )
  public <R> SearchStream<E> project(Function<? super E, ? extends R> field) {
    if (MetamodelField.class.isAssignableFrom(field.getClass())) {
      MetamodelField<E, R> foi = (MetamodelField<E, R>) field;

      projections.add(foi);
    } else if (TupleMapper.class.isAssignableFrom(field.getClass())) {
      @SuppressWarnings(
        "rawtypes"
      ) AbstractTupleMapper tm = (AbstractTupleMapper) field;

      IntStream.range(0, tm.degree()).forEach(i -> {
        MetamodelField<E, ?> foi = (MetamodelField<E, ?>) tm.get(i);
        projections.add(foi);
      });
    }
    projections.add((MetamodelField<E, ?>) getMetamodelForIdField(this.entityClass));
    return this;
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public <R> SearchStream<E> project(MetamodelField<? super E, ? extends R>... fields) {
    for (MetamodelField<? super E, ? extends R> field : fields) {
      projections.add((MetamodelField<E, ?>) field);
    }
    return this;
  }

  @Override
  public String backingQuery() {
    return rootNode.toString();
  }

  @Override
  public <R> SearchStream<E> summarize(Function<? super E, ? extends R> field) {
    if (MetamodelField.class.isAssignableFrom(field.getClass())) {
      @SuppressWarnings(
        "unchecked"
      ) MetamodelField<E, R> foi = (MetamodelField<E, R>) field;
      summaryFields.add(foi);

    } else if (TupleMapper.class.isAssignableFrom(field.getClass())) {
      @SuppressWarnings(
        "rawtypes"
      ) AbstractTupleMapper tm = (AbstractTupleMapper) field;

      IntStream.range(0, tm.degree()).forEach(i -> {
        @SuppressWarnings(
          "unchecked"
        ) MetamodelField<E, ?> foi = (MetamodelField<E, ?>) tm.get(i);
        summaryFields.add(foi);
      });
    }
    return this;
  }

  @Override
  public <R> SearchStream<E> summarize(Function<? super E, ? extends R> field, SummarizeParams summarizeParams) {
    this.summarizeParams = summarizeParams;
    return summarize(field);
  }

  @Override
  public <R> SearchStream<E> highlight(Function<? super E, ? extends R> field) {
    if (MetamodelField.class.isAssignableFrom(field.getClass())) {
      @SuppressWarnings(
        "unchecked"
      ) MetamodelField<E, R> foi = (MetamodelField<E, R>) field;
      highlightFields.add(foi);

    } else if (TupleMapper.class.isAssignableFrom(field.getClass())) {
      @SuppressWarnings(
        "rawtypes"
      ) AbstractTupleMapper tm = (AbstractTupleMapper) field;

      IntStream.range(0, tm.degree()).forEach(i -> {
        @SuppressWarnings(
          "unchecked"
        ) MetamodelField<E, ?> foi = (MetamodelField<E, ?>) tm.get(i);
        highlightFields.add(foi);
      });
    }
    return this;
  }

  @Override
  public <R> SearchStream<E> highlight(Function<? super E, ? extends R> field, Pair<String, String> tags) {
    highlightTags = tags;
    return highlight(field);
  }

  @Override
  public boolean isDocument() {
    return isDocument;
  }

  private Gson getGson() {
    if (gson == null) {
      gson = gsonBuilder.create();
    }
    return gson;
  }
}
