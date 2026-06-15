package com.redis.om.spring.search.stream;

import static com.redis.om.spring.util.ObjectUtils.floatArrayToByteArray;
import static java.util.stream.Collectors.toCollection;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;

import com.google.gson.Gson;
import com.redis.om.spring.annotations.Dialect;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.search.stream.predicates.SearchFieldPredicate;
import com.redis.om.spring.search.stream.predicates.lexicographic.*;
import com.redis.om.spring.search.stream.predicates.vector.KNNPredicate;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.util.ObjectUtils;
import com.redis.om.spring.util.SearchResultRawResponseToObjectConverter;
import com.redis.vl.query.AggregateHybridQuery;
import com.redis.vl.query.HybridQuery;

import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.Query.HighlightTags;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.aggr.SortedField;
import redis.clients.jedis.search.hybrid.FTHybridParams;
import redis.clients.jedis.search.hybrid.HybridResult;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.util.SafeEncoder;

/**
 * Package-private helper that owns all query-building and query-execution logic for
 * {@link SearchStreamImpl}. All mutable query-state (limit, skip, sortBy, predicates, etc.)
 * lives on {@link SearchStreamImpl} and is passed in as parameters at call time so that
 * deferred execution always sees the final values set by the fluent API.
 *
 * @param <E> the entity type
 */
class SearchStreamQueryExecutor<E> {

  private static final Log logger = LogFactory.getLog(SearchStreamQueryExecutor.class);

  private static final Integer MAX_LIMIT = 10000;

  private final SearchOperations<String> search;
  private final Class<E> entityClass;
  private final boolean isDocument;
  private final Field idField;
  private final MappingRedisOMConverter mappingConverter;
  private final RediSearchIndexer indexer;
  private final RedisModulesOperations<String> modulesOperations;
  private final SearchStreamDocumentMapper<E> documentMapper;

  SearchStreamQueryExecutor(SearchOperations<String> search, Class<E> entityClass, boolean isDocument, Field idField,
      MappingRedisOMConverter mappingConverter, RediSearchIndexer indexer,
      RedisModulesOperations<String> modulesOperations, SearchStreamDocumentMapper<E> documentMapper) {
    this.search = search;
    this.entityClass = entityClass;
    this.isDocument = isDocument;
    this.idField = idField;
    this.mappingConverter = mappingConverter;
    this.indexer = indexer;
    this.modulesOperations = modulesOperations;
    this.documentMapper = documentMapper;
  }

  // ---------------------------------------------------------------------------
  // Predicate processing
  // ---------------------------------------------------------------------------

  /**
   * Processes a search field predicate and applies it to the current query tree.
   */
  Node processPredicate(SearchFieldPredicate<? super E, ?> predicate, Node rootNode) {
    if (predicate instanceof LexicographicPredicate) {
      return processLexicographicPredicate(predicate, rootNode);
    }
    return predicate.apply(rootNode);
  }

  Node processPredicateGeneric(java.util.function.Predicate<?> predicate, Node rootNode) {
    if (SearchFieldPredicate.class.isAssignableFrom(predicate.getClass())) {
      @SuppressWarnings(
        "unchecked"
      ) SearchFieldPredicate<? super E, ?> p = (SearchFieldPredicate<? super E, ?>) predicate;
      return processPredicate(p, rootNode);
    }
    return rootNode;
  }

  private Node processLexicographicPredicate(SearchFieldPredicate<? super E, ?> predicate, Node rootNode) {
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

  // ---------------------------------------------------------------------------
  // Query building
  // ---------------------------------------------------------------------------

  /**
   * Builds the Jedis {@link Query} object from the current mutable query state.
   * All mutable state is passed explicitly so the executor remains stateless.
   */
  Query prepareQuery(Node rootNode, KNNPredicate<E, ?> knnPredicate, Long skip, Long limit, SortedField sortBy,
      boolean withScores, Scorer scorer, int dialect, List<MetamodelField<E, ?>> summaryFields,
      SummarizeParams summarizeParams, List<MetamodelField<E, ?>> highlightFields, Pair<String, String> highlightTags,
      boolean onlyIds, List<MetamodelField<E, ?>> projections) {
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

    if (withScores) {
      query.setWithScores();
    }
    if (scorer != null) {
      query.setScorer(scorer.getValue());
    }

    if (!summaryFields.isEmpty()) {
      var fields = summaryFields.stream().map(foi -> ObjectUtils.isCollection(foi.getTargetClass()) ?
          "$." + foi.getSearchAlias() :
          foi.getSearchAlias()).collect(toCollection(ArrayList::new));

      if (summarizeParams == null) {
        query.summarizeFields(fields.toArray(String[]::new));
      } else {
        query.summarizeFields(summarizeParams.getFragSize(), summarizeParams.getFragsNum(), summarizeParams
            .getSeparator(), fields.toArray(String[]::new));
      }
    }

    if (!highlightFields.isEmpty()) {
      var fields = highlightFields.stream().map(foi -> ObjectUtils.isCollection(foi.getTargetClass()) ?
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
      var returnFields = projections.stream().map(foi -> ObjectUtils.isCollection(foi.getTargetClass()) ?
          "$." + foi.getSearchAlias() :
          foi.getSearchAlias()).collect(toCollection(ArrayList::new));
      returnFields.add(idField.getName());
      query.returnFields(returnFields.toArray(String[]::new));
    } else if (isDocument) {
      // JSON indexes on detached indices don't always return $ unless requested explicitly
      query.returnFields("$");
    }

    return query;
  }

  // ---------------------------------------------------------------------------
  // Query execution
  // ---------------------------------------------------------------------------

  /**
   * Executes the FT.SEARCH query and returns the raw {@link SearchResult}.
   */
  SearchResult executeQuery(Node rootNode, KNNPredicate<E, ?> knnPredicate, Long skip, Long limit, SortedField sortBy,
      boolean withScores, Scorer scorer, int dialect, List<MetamodelField<E, ?>> summaryFields,
      SummarizeParams summarizeParams, List<MetamodelField<E, ?>> highlightFields, Pair<String, String> highlightTags,
      boolean onlyIds, List<MetamodelField<E, ?>> projections, boolean isQBE) {
    try {
      Query query = prepareQuery(rootNode, knnPredicate, skip, limit, sortBy, withScores, scorer, dialect,
          summaryFields, summarizeParams, highlightFields, highlightTags, onlyIds, projections);
      return search.search(query);
    } catch (JedisDataException jde) {
      if (isQBE && jde.getMessage().contains("not loaded nor in schema")) {
        throw new UnsupportedOperationException("The example object properties are not part of the search schema", jde);
      } else
        throw jde;
    }
  }

  /**
   * Executes a hybrid query using RedisVL's HybridQuery implementation.
   * <p>
   * Tries the native FT.HYBRID command first (Redis 8.4+), and falls back
   * to FT.AGGREGATE if FT.HYBRID is not available on the current Redis version.
   * The HybridQuery is built at execution time so that limit/skip set after
   * hybridSearch() are properly captured.
   * </p>
   */
  @SuppressWarnings(
    "unchecked"
  )
  List<E> executeHybridQueryToEntityList(Node rootNode, Long skip, Long limit, String hybridText,
      MetamodelField<? super E, ?> hybridTextField, float[] hybridVector,
      MetamodelField<? super E, ?> hybridVectorField, float hybridAlpha, CombinationMethod hybridCombinationMethod) {
    // Build the filter expression from existing rootNode filters
    String filterExpression = null;
    if (rootNode != null && !rootNode.toString().isBlank() && !rootNode.toString().equals("*")) {
      filterExpression = rootNode.toString();
    }

    // Determine how many results the hybrid query should request.
    int numResults = MAX_LIMIT;
    if (limit != null) {
      long requested = limit;
      if (skip != null) {
        requested += skip;
      }
      numResults = (int) Math.min(Math.max(requested, 1), MAX_LIMIT);
    }

    // Convert alpha (vector weight) to linearAlpha (text weight) for HybridQuery
    float linearAlpha = 1.0f - hybridAlpha;

    HybridQuery.CombinationMethod redisvlCombinationMethod = switch (hybridCombinationMethod) {
      case RRF -> HybridQuery.CombinationMethod.RRF;
      case LINEAR -> HybridQuery.CombinationMethod.LINEAR;
    };

    HybridQuery.HybridQueryBuilder builder = HybridQuery.builder().text(hybridText).textFieldName(hybridTextField
        .getSearchAlias()).vector(hybridVector).vectorFieldName(hybridVectorField.getSearchAlias()).combinationMethod(
            redisvlCombinationMethod).linearAlpha(linearAlpha).numResults(numResults);

    if (filterExpression != null) {
      builder.filterExpression(filterExpression);
    }

    HybridQuery hybridQueryObj = builder.build();

    // Try FT.HYBRID first (Redis 8.4+)
    try {
      FTHybridParams params = hybridQueryObj.buildFTHybridParams();
      HybridResult result = search.ftHybrid(params);
      List<E> entities = documentMapper.hybridDocumentsToEntities(result.getDocuments());

      // Apply skip/limit in-memory for pagination
      if (skip != null && skip > 0) {
        entities = entities.stream().skip(skip).collect(Collectors.toList());
      }
      if (limit != null) {
        entities = entities.stream().limit(limit).collect(Collectors.toList());
      }

      return entities;
    } catch (Exception e) {
      logger.warn("FT.HYBRID failed, falling back to FT.AGGREGATE: " + e.getClass().getSimpleName() + ": " + e
          .getMessage(), e);
    }

    // Fallback to FT.AGGREGATE path
    AggregateHybridQuery fallback = hybridQueryObj.toAggregateHybridQuery();
    redis.clients.jedis.search.aggr.AggregationBuilder aggregation = fallback.buildRedisAggregation();

    Map<String, Object> params = fallback.getParams();
    if (params != null && !params.isEmpty()) {
      aggregation.params(params);
    }

    List<String> fieldsToLoad = new ArrayList<>();
    List<Field> entityFields = ObjectUtils.getDeclaredFieldsTransitively(entityClass);
    for (Field field : entityFields) {
      if (!field.getName().equals(idField.getName())) {
        fieldsToLoad.add("@" + field.getName());
      }
    }
    if (!fieldsToLoad.isEmpty()) {
      aggregation.load(fieldsToLoad.toArray(String[]::new));
    }

    if (skip != null || limit != null) {
      int skipValue = skip != null ? skip.intValue() : 0;
      int limitValue = limit != null ? limit.intValue() : MAX_LIMIT;
      aggregation.limit(skipValue, limitValue);
    }

    AggregationResult aggResult = search.aggregate(aggregation);

    if (isDocument) {
      Gson g = documentMapper.getGson();
      return aggResult.getResults().stream().map(d -> {
        Object rawJson = d.get("$");
        if (rawJson == null) {
          logger.warn("Aggregation result has no '$' field; skipping entity mapping for " + entityClass
              .getSimpleName());
          return null;
        }
        String jsonStr = (rawJson instanceof byte[]) ? SafeEncoder.encode((byte[]) rawJson) : rawJson.toString();
        return g.fromJson(jsonStr, entityClass);
      }).filter(Objects::nonNull).collect(Collectors.toList());
    } else {
      return aggResult.getResults().stream().map(h -> (E) ObjectUtils.mapToObject(h, entityClass, mappingConverter))
          .collect(Collectors.toList());
    }
  }

  /**
   * Converts a {@link SearchResult} to a list of entities, applying projection if set.
   */
  @SuppressWarnings(
    "unchecked"
  )
  List<E> toEntityList(SearchResult searchResult, List<MetamodelField<E, ?>> projections, Gson gson) {
    if (projections.isEmpty()) {
      return documentMapper.documentsToEntities(searchResult.getDocuments());
    } else {
      List<E> projectedEntities = new ArrayList<>();
      searchResult.getDocuments().forEach(doc -> {
        Map<String, Object> props = StreamSupport.stream(doc.getProperties().spliterator(), false).collect(Collectors
            .toMap(Entry::getKey, Entry::getValue));

        E entity = BeanUtils.instantiateClass(entityClass);
        projections.forEach(foi -> {
          String field = foi.getSearchAlias();
          Class<?> targetClass = foi.getTargetClass();

          var rawValue = props.get(ObjectUtils.isCollection(targetClass) ? "$." + field : field);
          Object processValue = SearchResultRawResponseToObjectConverter.process(rawValue, targetClass, gson);

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

  /**
   * Resolves the entity stream. Hybrid queries use executeHybridQueryToEntityList();
   * regular queries use executeQuery() + toEntityList().
   */
  java.util.stream.Stream<E> resolveStream(Node rootNode, KNNPredicate<E, ?> knnPredicate, Long skip, Long limit,
      SortedField sortBy, boolean withScores, Scorer scorer, int dialect, List<MetamodelField<E, ?>> summaryFields,
      SummarizeParams summarizeParams, List<MetamodelField<E, ?>> highlightFields, Pair<String, String> highlightTags,
      boolean onlyIds, List<MetamodelField<E, ?>> projections, boolean isQBE, String hybridText,
      MetamodelField<? super E, ?> hybridTextField, float[] hybridVector,
      MetamodelField<? super E, ?> hybridVectorField, float hybridAlpha, CombinationMethod hybridCombinationMethod) {
    if (hybridText != null) {
      return executeHybridQueryToEntityList(rootNode, skip, limit, hybridText, hybridTextField, hybridVector,
          hybridVectorField, hybridAlpha, hybridCombinationMethod).stream();
    } else {
      SearchResult result = executeQuery(rootNode, knnPredicate, skip, limit, sortBy, withScores, scorer, dialect,
          summaryFields, summarizeParams, highlightFields, highlightTags, onlyIds, projections, isQBE);
      return toEntityList(result, projections, documentMapper.getGson()).stream();
    }
  }

  List<Pair<E, Double>> toListWithScores(Node rootNode, KNNPredicate<E, ?> knnPredicate, Long skip, Long limit,
      SortedField sortBy, Scorer scorer, int dialect, List<MetamodelField<E, ?>> summaryFields,
      SummarizeParams summarizeParams, List<MetamodelField<E, ?>> highlightFields, Pair<String, String> highlightTags,
      List<MetamodelField<E, ?>> projections, boolean isQBE) {
    SearchResult searchResult = executeQuery(rootNode, knnPredicate, skip, limit, sortBy, true, scorer, dialect,
        summaryFields, summarizeParams, highlightFields, highlightTags, false, projections, isQBE);
    return documentMapper.documentsToEntityScorePairs(searchResult.getDocuments());
  }
}
