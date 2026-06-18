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

import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.Query.HighlightTags;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.SortedField;
import redis.clients.jedis.search.querybuilder.Node;

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
   * Hybrid search requires Jedis 7.x and the redisvl library, which are not available
   * on the 1.1.x line (Jedis 6.x). This method is stubbed and will throw at runtime.
   */
  List<E> executeHybridQueryToEntityList(Node rootNode, Long skip, Long limit, String hybridText,
      MetamodelField<? super E, ?> hybridTextField, float[] hybridVector,
      MetamodelField<? super E, ?> hybridVectorField, float hybridAlpha, CombinationMethod hybridCombinationMethod) {
    // Hybrid search requires Jedis 7.x and the redisvl library (com.redis:redisvl),
    // neither of which is available in the 1.1.x dependency set (Jedis 6.x).
    throw new UnsupportedOperationException(
        "hybridSearch() is not supported on redis-om-spring 1.1.x — it requires Jedis 7.x and the redisvl library." + " Please upgrade to 2.x for hybrid search support.");
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
