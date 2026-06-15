package com.redis.om.spring.repository.query;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.*;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.util.Pair;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.annotations.*;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.query.clause.QueryClause;
import com.redis.om.spring.search.stream.EntityStreamImpl;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.*;

/**
 * Enhanced query implementation for Redis OM Spring that provides advanced search and aggregation
 * capabilities over Redis Hash data structures. Extends {@link AbstractRedisQuery} for shared
 * query infrastructure, and implements Hash-specific result parsing, delete, and null queries.
 *
 * @author Redis OM Spring Team
 * @since 1.0.0
 */
public class RedisEnhancedQuery extends AbstractRedisQuery {

  private static final Log logger = LogFactory.getLog(RedisEnhancedQuery.class);

  // Hash-specific fields
  private final MappingRedisOMConverter mappingConverter;
  private final KeyValueOperations keyValueOperations;
  private final RedisOperations<?, ?> redisOperations;

  /**
   * Constructs a new RedisEnhancedQuery instance for executing repository queries against Redis.
   *
   * @param queryMethod             the Spring Data query method metadata
   * @param metadata                the repository metadata
   * @param indexer                 the RediSearch indexer
   * @param valueExpressionDelegate Spring Data's delegate for value expression evaluation
   * @param keyValueOperations      Spring Data KeyValue operations
   * @param redisOperations         low-level Redis operations
   * @param rmo                     Redis modules operations
   * @param queryCreator            the query creator class (currently unused)
   * @param redisOMProperties       configuration properties for Redis OM
   */
  @SuppressWarnings(
    "unchecked"
  )
  public RedisEnhancedQuery(QueryMethod queryMethod, //
      RepositoryMetadata metadata, //
      RediSearchIndexer indexer, //
      org.springframework.data.repository.query.ValueExpressionDelegate valueExpressionDelegate, //
      KeyValueOperations keyValueOperations, //
      RedisOperations<?, ?> redisOperations, //
      RedisModulesOperations<?> rmo, //
      Class<? extends AbstractQueryCreator<?, ?>> queryCreator, //
      RedisOMProperties redisOMProperties) {
    super(queryMethod, (RedisModulesOperations<String>) rmo, indexer, new EntityStreamImpl(
        (RedisModulesOperations<String>) rmo, new GsonBuilder(), indexer), redisOMProperties, Dialect.ONE);

    logger.info(String.format("Creating query %s", queryMethod.getName()));

    this.keyValueOperations = keyValueOperations;
    this.redisOperations = redisOperations;
    this.mappingConverter = new MappingRedisOMConverter(null, new ReferenceResolverImpl(redisOperations));

    @SuppressWarnings(
      "rawtypes"
    ) Class[] params = queryMethod.getParameters().stream().map(Parameter::getType).toArray(Class[]::new);

    super.initFromMethod(metadata.getRepositoryInterface(), metadata, params, queryMethod.getName());
  }

  // ---------------------------------------------------------------------------
  // Hash-specific query execution
  // ---------------------------------------------------------------------------

  @Override
  protected Object executeQuery(Object[] parameters) {
    ParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameters);
    ResultProcessor processor = queryMethod.getResultProcessor().withDynamicProjection(accessor);

    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);
    boolean excludeNullParams = !isNullParamQuery;
    String preparedQuery = prepareQuery(parameters, excludeNullParams);
    Query query = new Query(preparedQuery);

    ReturnedType returnedType = processor.getReturnedType();

    boolean isProjecting = returnedType.isProjecting() && returnedType.getReturnedType() != SearchResult.class;
    boolean isOpenProjecting = Arrays.stream(returnedType.getReturnedType().getMethods()).anyMatch(m -> m
        .isAnnotationPresent(Value.class));
    boolean canPerformQueryOptimization = isProjecting && !isOpenProjecting;

    if (canPerformQueryOptimization) {
      query.returnFields(returnedType.getInputProperties().stream().map(inputProperty -> new FieldName(
          "$." + inputProperty, inputProperty)).toArray(FieldName[]::new));
    } else {
      query.returnFields(returnFields);
    }

    Optional<Pageable> maybePageable = Optional.empty();

    boolean needsLimit = true;
    if (queryMethod.isPageQuery()) {
      maybePageable = Arrays.stream(parameters).filter(Pageable.class::isInstance).map(Pageable.class::cast)
          .findFirst();

      if (maybePageable.isPresent()) {
        Pageable pageable = maybePageable.get();
        if (!pageable.isUnpaged()) {
          query.limit(Math.toIntExact(pageable.getOffset()), pageable.getPageSize());
          needsLimit = false;

          pageable.getSort();
          for (Order order : pageable.getSort()) {
            query.setSortBy(order.getProperty(), order.isAscending());
          }
        }
      }
    }

    if (needsLimit) {
      if ((limit != null && limit != Integer.MIN_VALUE) || (offset != null && offset != Integer.MIN_VALUE)) {
        query.limit(offset != null ? offset : 0, limit != null ?
            limit :
            redisOMProperties.getRepository().getQuery().getLimit());
      } else {
        query.limit(0, redisOMProperties.getRepository().getQuery().getLimit());
      }
    }

    if ((sortBy != null && !sortBy.isBlank())) {
      var alias = indexer.getAlias(domainType, sortBy);
      query.setSortBy(alias, sortAscending);
    }

    if (hasLanguageParameter) {
      Optional<SearchLanguage> maybeSearchLanguage = Arrays.stream(parameters).filter(SearchLanguage.class::isInstance)
          .map(SearchLanguage.class::cast).findFirst();
      maybeSearchLanguage.ifPresent(searchLanguage -> query.setLanguage(searchLanguage.getValue()));
    }

    // Intercept TAG collection queries with empty parameters and use an aggregation
    if (queryMethod.isCollectionQuery() && !queryMethod.getParameters().isEmpty()) {
      List<Collection<?>> emptyCollectionParams = Arrays.stream(parameters) //
          .filter(Collection.class::isInstance) //
          .map(p -> (Collection<?>) p) //
          .filter(Collection::isEmpty) //
          .collect(Collectors.toList());
      if (!emptyCollectionParams.isEmpty()) {
        return Collections.emptyList();
      }
    }

    // Set query dialect
    query.dialect(dialect.getValue());

    SearchResult searchResult = ops.search(query);

    // what to return
    Object result;

    // Check if this is a SearchStream query
    if (SearchStream.class.isAssignableFrom(queryMethod.getReturnedObjectType())) {
      @SuppressWarnings(
        "unchecked"
      ) SearchStream<?> stream = entityStream.of((Class<Object>) domainType);

      String queryString = prepareQuery(parameters, true);

      if (!queryString.equals("*") && !queryString.isEmpty()) {
        stream = stream.filter(queryString);
      }

      if (limit != null && limit > 0) {
        stream = stream.limit(limit);
      }

      return stream;
    } else if (processor.getReturnedType().getReturnedType() == boolean.class || processor.getReturnedType()
        .getReturnedType() == Boolean.class) {
      result = searchResult.getTotalResults() > 0;
    } else if (queryMethod.getReturnedObjectType() == SearchResult.class) {
      result = searchResult;
    } else if (queryMethod.isPageQuery()) {
      List<Object> content = searchResult.getDocuments().stream().map(d -> {
        Object entity = ObjectUtils.documentToObject(d, queryMethod.getReturnedObjectType(), mappingConverter);
        return ObjectUtils.populateRedisKey(entity, d.getId());
      }).collect(Collectors.toList());

      if (maybePageable.isPresent()) {
        Pageable pageable = maybePageable.get();
        result = new PageImpl<>(content, pageable, searchResult.getTotalResults());
      } else {
        result = content;
      }
    } else if (!queryMethod.isCollectionQuery()) {
      if (searchResult.getTotalResults() > 0 && !searchResult.getDocuments().isEmpty()) {
        redis.clients.jedis.search.Document doc = searchResult.getDocuments().get(0);
        Object entity = ObjectUtils.documentToObject(doc, queryMethod.getReturnedObjectType(), mappingConverter);
        result = ObjectUtils.populateRedisKey(entity, doc.getId());
      } else {
        result = null;
      }
    } else if (queryMethod.isCollectionQuery()) {
      result = searchResult.getDocuments().stream().map(d -> {
        Object entity = ObjectUtils.documentToObject(d, queryMethod.getReturnedObjectType(), mappingConverter);
        return ObjectUtils.populateRedisKey(entity, d.getId());
      }).collect(Collectors.toList());
    } else {
      result = null;
    }

    return processor.processResult(result);
  }

  @Override
  protected Object executeDeleteQuery(Object[] parameters) {
    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);
    String baseQuery = prepareQuery(parameters, true);
    AggregationBuilder aggregation = new AggregationBuilder(baseQuery);

    // Load fields with IS_NULL or IS_NOT_NULL query clauses
    String[] fields = Stream.concat(Stream.of("@__key"), queryOrParts.stream().flatMap(List::stream).filter(pair -> pair
        .getSecond() == QueryClause.IS_NULL || pair.getSecond() == QueryClause.IS_NOT_NULL).map(pair -> String.format(
            "@%s", pair.getFirst()))).toArray(String[]::new);
    aggregation.load(fields);

    // Apply exists or !exists filter for null parameters
    for (List<Pair<String, QueryClause>> orPartParts : queryOrParts) {
      for (Pair<String, QueryClause> pair : orPartParts) {
        if (pair.getSecond() == QueryClause.IS_NULL) {
          if (hasIndexMissing(pair.getFirst())) {
            aggregation.filter("ismissing(@" + pair.getFirst() + ")");
          } else {
            aggregation.filter("!exists(@" + pair.getFirst() + ")");
          }
        } else if (pair.getSecond() == QueryClause.IS_NOT_NULL) {
          if (hasIndexMissing(pair.getFirst())) {
            aggregation.filter("!ismissing(@" + pair.getFirst() + ")");
          } else {
            aggregation.filter("exists(@" + pair.getFirst() + ")");
          }
        }
      }
    }

    aggregation.sortBy(aggregationSortedFields.toArray(new SortedField[] {}));
    aggregation.limit(0, redisOMProperties.getRepository().getQuery().getLimit());

    // Set query dialect
    aggregation.dialect(dialect.getValue());

    // Execute the aggregation query
    AggregationResult aggregationResult = ops.aggregate(aggregation);

    // extract the keys from the aggregation result
    List<String> keys = aggregationResult.getResults().stream().map(d -> d.get("__key").toString()).toList();

    // determine if we need to return the deleted entities or just obtain the keys
    Class<?> returnType = queryMethod.getReturnedObjectType();
    if (Number.class.isAssignableFrom(returnType) || returnType.equals(int.class) || returnType.equals(
        long.class) || returnType.equals(short.class)) {
      if (keys.isEmpty()) {
        return 0;
      } else {
        return modulesOperations.template().delete(keys);
      }
    } else {
      if (keys.isEmpty()) {
        return Collections.emptyList();
      } else {
        var entities = new ArrayList<>();

        redisOperations.executePipelined((RedisCallback<Map<byte[], Map<byte[], byte[]>>>) connection -> {
          for (String key : keys) {
            connection.hashCommands().hGetAll(key.getBytes());
          }

          List<Object> results = connection.closePipeline();

          for (Object result : results) {
            @SuppressWarnings(
              "unchecked"
            ) Map<byte[], byte[]> hashMap = (Map<byte[], byte[]>) result;
            Object entity = mappingConverter.read(returnType, new RedisData(hashMap));
            entities.add(entity);
          }
          return null;
        });
        modulesOperations.template().delete(keys);

        return entities;
      }
    }
  }

  @Override
  protected Object executeNullQuery(Object[] parameters) {
    String indexName = indexer.getIndexName(this.domainType);
    SearchOperations<String> ops = modulesOperations.opsForSearch(indexName);
    String baseQuery = prepareQuery(parameters, true);

    AggregationBuilder aggregation = new AggregationBuilder(baseQuery);

    // Load fields with IS_NULL or IS_NOT_NULL query clauses
    String[] fields = Stream.concat(Stream.of("@__key"), queryOrParts.stream().flatMap(List::stream).filter(pair -> pair
        .getSecond() == QueryClause.IS_NULL || pair.getSecond() == QueryClause.IS_NOT_NULL).map(pair -> String.format(
            "@%s", pair.getFirst()))).toArray(String[]::new);

    aggregation.load(fields);

    // Apply exists or !exists filter for null parameters
    for (List<Pair<String, QueryClause>> orPartParts : queryOrParts) {
      for (Pair<String, QueryClause> pair : orPartParts) {
        if (pair.getSecond() == QueryClause.IS_NULL) {
          if (hasIndexMissing(pair.getFirst())) {
            aggregation.filter("ismissing(@" + pair.getFirst() + ")");
          } else {
            aggregation.filter("!exists(@" + pair.getFirst() + ")");
          }
        } else if (pair.getSecond() == QueryClause.IS_NOT_NULL) {
          if (hasIndexMissing(pair.getFirst())) {
            aggregation.filter("!ismissing(@" + pair.getFirst() + ")");
          } else {
            aggregation.filter("exists(@" + pair.getFirst() + ")");
          }
        }
      }
    }

    Optional<Pageable> maybePageable = Optional.empty();

    boolean needsLimit = true;
    if (queryMethod.isPageQuery()) {
      maybePageable = Arrays.stream(parameters).filter(Pageable.class::isInstance).map(Pageable.class::cast)
          .findFirst();

      if (maybePageable.isPresent()) {
        Pageable pageable = maybePageable.get();
        if (!pageable.isUnpaged()) {
          aggregation.limit(Math.toIntExact(pageable.getOffset()), pageable.getPageSize());
          needsLimit = false;

          pageable.getSort();
          for (Order order : pageable.getSort()) {
            var alias = indexer.getAlias(domainType, order.getProperty());
            if (order.isAscending()) {
              aggregation.sortByAsc(alias);
            } else {
              aggregation.sortByDesc(alias);
            }
          }
        }
      }
    }

    if ((sortBy != null && !sortBy.isBlank())) {
      var alias = indexer.getAlias(domainType, sortBy);
      aggregation.sortByAsc(alias);
    } else if (!aggregationSortedFields.isEmpty()) {
      if (aggregationSortByMax != null) {
        aggregation.sortBy(aggregationSortByMax, aggregationSortedFields.toArray(new SortedField[] {}));
      } else {
        aggregation.sortBy(aggregationSortedFields.toArray(new SortedField[] {}));
      }
    }

    if (needsLimit) {
      if ((limit != null) || (offset != null)) {
        aggregation.limit(offset != null ? offset : 0, limit != null ? limit : 0);
      } else {
        aggregation.limit(0, redisOMProperties.getRepository().getQuery().getLimit());
      }
    }

    // Set query dialect
    aggregation.dialect(dialect.getValue());

    // Execute the aggregation query
    AggregationResult aggregationResult = ops.aggregate(aggregation);

    // extract the keys from the aggregation result
    var ids = aggregationResult.getResults().stream().map(d -> d.get("__key").toString().split(":")).map(
        parts -> parts[parts.length - 1]).toList();
    var entities = new ArrayList<>();
    ids.forEach(id -> keyValueOperations.findById(id, domainType).ifPresent(entities::add));

    if (!queryMethod.isCollectionQuery()) {
      return entities.isEmpty() ? null : entities.get(0);
    } else {
      return entities;
    }
  }

  @Override
  protected String prepareQuery(final Object[] parameters, boolean excludeNullParams) {
    logger.debug(String.format("parameters: %s", Arrays.toString(parameters)));
    List<Object> params = new ArrayList<>(Arrays.asList(parameters));
    StringBuilder preparedQuery = new StringBuilder();

    boolean multipleOrParts = queryOrParts.size() > 1;
    logger.debug(String.format("queryOrParts: %s", queryOrParts.size()));
    if (!queryOrParts.isEmpty()) {
      preparedQuery.append(queryOrParts.stream().map(qop -> {
        String orPart = multipleOrParts ? "(" : "";
        orPart = orPart + qop.stream().map(fieldClauses -> {
          if (excludeNullParams && (fieldClauses.getSecond() == QueryClause.IS_NULL || fieldClauses
              .getSecond() == QueryClause.IS_NOT_NULL)) {
            return "";
          }
          String fieldName = QueryUtils.escape(fieldClauses.getFirst());
          QueryClause queryClause = fieldClauses.getSecond();
          int paramsCnt = queryClause.getClauseTemplate().getNumberOfArguments();

          Object[] ps = params.subList(0, paramsCnt).toArray();
          params.subList(0, paramsCnt).clear();

          return queryClause.prepareQuery(fieldName, ps);
        }).collect(Collectors.joining(" "));
        orPart = orPart + (multipleOrParts ? ")" : "");

        return orPart;
      }).collect(Collectors.joining(" | ")));
    } else {
      @SuppressWarnings(
        "unchecked"
      ) Iterator<Parameter> iterator = (Iterator<Parameter>) queryMethod.getParameters().iterator();
      int index = 0;

      if (value != null && !value.isBlank()) {
        preparedQuery.append(value);
      }

      while (iterator.hasNext()) {
        Parameter p = iterator.next();
        Optional<String> maybeKey = p.getName();
        String key;
        if (maybeKey.isPresent()) {
          key = maybeKey.get();
        } else {
          key = paramNames.size() > index ? paramNames.get(index) : "";
        }

        if (!key.isBlank()) {
          String v;

          if (parameters[index] instanceof Collection<?> c) {
            v = c.stream().map(n -> ObjectUtils.asString(n, mappingConverter)).collect(Collectors.joining(" | "));
          } else {
            v = ObjectUtils.asString(parameters[index], mappingConverter);
          }

          var regex = "(\\$" + Pattern.quote(key) + "(?![a-zA-Z0-9_]))(\\W+|\\*|\\+|$)?";
          preparedQuery = new StringBuilder(preparedQuery.toString().replaceAll(regex, v + "$2"));
        }
        index++;
      }
    }

    if (preparedQuery.toString().isBlank()) {
      preparedQuery.append("*");
    }

    logger.debug(String.format("query: %s", preparedQuery));

    return preparedQuery.toString();
  }
}
