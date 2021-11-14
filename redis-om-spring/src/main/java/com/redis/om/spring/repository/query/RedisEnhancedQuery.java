package com.redis.om.spring.repository.query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.geo.Point;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.util.Pair;
import org.springframework.data.util.Streamable;

import com.redis.om.spring.annotations.Aggregation;
import com.redis.om.spring.annotations.Bloom;
import com.redis.om.spring.annotations.GeoIndexed;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.annotations.TextIndexed;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.pds.BloomOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.query.clause.QueryClause;

import io.redisearch.AggregationResult;
import io.redisearch.Query;
import io.redisearch.Schema.FieldType;
import io.redisearch.SearchResult;
import io.redisearch.aggregation.AggregationBuilder;

public class RedisEnhancedQuery implements RepositoryQuery {
  
  private static final Log logger = LogFactory.getLog(RedisEnhancedQuery.class);

  private final QueryMethod queryMethod;
  private final String searchIndex;

  private static enum RediSearchQueryType {
    QUERY,
    AGGREGATION
  };

  private RediSearchQueryType type;
  private String value;

  // query fields
  private String[] returnFields;

  // aggregation field
  private String[] load;
  
  //
  private List<Pair<String,QueryClause>> queryFields = new ArrayList<Pair<String,QueryClause>>();

  // for non @Param annotated dynamic names
  private List<String> paramNames = new ArrayList<String>();
  private Class<?> domainType;

  RedisModulesOperations<String, String> modulesOperations;

  @SuppressWarnings("unchecked")
  public RedisEnhancedQuery(QueryMethod queryMethod, RepositoryMetadata metadata,
      QueryMethodEvaluationContextProvider evaluationContextProvider, KeyValueOperations keyValueOperations,
      RedisModulesOperations<?, ?> rmo, Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {
    logger.debug(String.format("Creating query %s", queryMethod.getName()));

    this.modulesOperations = (RedisModulesOperations<String, String>) rmo;
    this.queryMethod = queryMethod;
    this.searchIndex = this.queryMethod.getEntityInformation().getJavaType().getSimpleName() + "Idx";
    this.domainType = this.queryMethod.getEntityInformation().getJavaType();

    Class<?> repoClass = metadata.getRepositoryInterface();
    @SuppressWarnings("rawtypes")
    Class[] params = queryMethod.getParameters().stream().map(p -> p.getType()).toArray(Class[]::new);
    try {
      java.lang.reflect.Method method = repoClass.getDeclaredMethod(queryMethod.getName(), params);
      if (method.isAnnotationPresent(com.redis.om.spring.annotations.Query.class)) {
        com.redis.om.spring.annotations.Query queryAnnotation = method
            .getAnnotation(com.redis.om.spring.annotations.Query.class);
        this.type = RediSearchQueryType.QUERY;
        this.value = queryAnnotation.value();
        this.returnFields = queryAnnotation.returnFields();
      } else if (method.isAnnotationPresent(com.redis.om.spring.annotations.Aggregation.class)) {
        Aggregation aggregation = method.getAnnotation(Aggregation.class);
        this.type = RediSearchQueryType.AGGREGATION;
        this.value = aggregation.value();
        this.load = aggregation.load();
      } else {
        PartTree pt = new PartTree(queryMethod.getName(), metadata.getDomainType());

        Streamable<Part> queryParts = pt.getParts();
        for (Part part : queryParts) {
          String fieldName = part.getProperty().getSegment();
          
          //TODO: refactor this code is symmetrical to code executed during annotation processing
          Field field;
          try {
            field = domainType.getDeclaredField(fieldName);
            if (field.isAnnotationPresent(TextIndexed.class) || field.isAnnotationPresent(Searchable.class)) {
              queryFields.add(Pair.of(fieldName, QueryClause.get(FieldType.FullText, part.getType())));
            } else if (field.isAnnotationPresent(TagIndexed.class)) {
              queryFields.add(Pair.of(fieldName, QueryClause.get(FieldType.Tag, part.getType())));
            } else if (field.isAnnotationPresent(GeoIndexed.class)) {
              queryFields.add(Pair.of(fieldName, QueryClause.get(FieldType.Geo, part.getType())));
            } else if (field.isAnnotationPresent(NumericIndexed.class)) {
              queryFields.add(Pair.of(fieldName, QueryClause.get(FieldType.Numeric, part.getType())));
            } else if (field.isAnnotationPresent(Indexed.class)) {
              //
              // Any Character class -> Tag Search Field
              //
              if (CharSequence.class.isAssignableFrom(field.getType())) {
                queryFields.add(Pair.of(fieldName, QueryClause.get(FieldType.Tag, part.getType())));
              }  
              //
              // Any Numeric class -> Numeric Search Field
              //
              if (Number.class.isAssignableFrom(field.getType())) {
                queryFields.add(Pair.of(fieldName, QueryClause.get(FieldType.Numeric, part.getType())));
              }
              //
              // Set 
              //
              if (Set.class.isAssignableFrom(field.getType())) {
                queryFields.add(Pair.of(fieldName, QueryClause.get(FieldType.Tag, part.getType())));
              }
              // 
              // Point
              //
              if (field.getType() == Point.class) {
                queryFields.add(Pair.of(fieldName, QueryClause.get(FieldType.Geo, part.getType())));
              }
            }
          } catch (NoSuchFieldException e) {
            logger.debug(String.format("Did not find a field named ", fieldName));
          }
        }

        this.type = RediSearchQueryType.QUERY;
        this.returnFields = new String[] {};
      }
    } catch (NoSuchMethodException | SecurityException e) {
      logger.warn(String.format("Did not find query method %s(%s)", queryMethod.getName(), Arrays.toString(params)), e);
    }
  }

  @Override
  public Object execute(Object[] parameters) {
    Optional<String> maybeBloomFilter = getBloomFilter();
    if (maybeBloomFilter.isPresent()) {
      logger.debug("Bloom filter found...");
      return executeBloomQuery(parameters, maybeBloomFilter.get());
    } else if (type == RediSearchQueryType.QUERY) {
      return executeQuery(parameters);
    } else /* if (type == RediSearchQueryType.AGGREGATION) */ {
      return executeAggregation(parameters);
    }
  }

  @Override
  public QueryMethod getQueryMethod() {
    return queryMethod;
  }

  private Object executeQuery(Object[] parameters) {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);
    Query query = new Query(prepareQuery(parameters));
    query.returnFields(returnFields);
    SearchResult searchResult = ops.search(query);
    // what to return
    Object result = null;
    if (queryMethod.getReturnedObjectType() == SearchResult.class) {
      result = searchResult;
    } else if (queryMethod.isQueryForEntity() && !queryMethod.isCollectionQuery()) {
      String jsonResult = searchResult.docs.isEmpty() ? "{}" : searchResult.docs.get(0).get("$").toString();
      //result = gson.fromJson(jsonResult, queryMethod.getReturnedObjectType());
    } else if (queryMethod.isQueryForEntity() && queryMethod.isCollectionQuery()) {
//      result = searchResult.docs.stream()
//          .map(d -> gson.fromJson(d.get("$").toString(), queryMethod.getReturnedObjectType()))
//          .collect(Collectors.toList());
    }

    return result;
  }

  private Object executeAggregation(Object[] parameters) {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);
    AggregationBuilder aggregation = new AggregationBuilder().load(load);
    AggregationResult aggregationResult = ops.aggregate(aggregation);

    // what to return
    Object result = null;
    if (queryMethod.getReturnedObjectType() == AggregationResult.class) {
      result = aggregationResult;
    } else if (queryMethod.isCollectionQuery()) {
      result = Collections.EMPTY_LIST;
    }

    return result;
  }
  
  public static final String EXISTS_BY_PREFIX = "existsBy";
  
  private Optional<String> getBloomFilter() {
    String methodName = getQueryMethod().getName();
    boolean hasExistByPrefix = methodName.startsWith(EXISTS_BY_PREFIX);

    if (hasExistByPrefix && boolean.class.isAssignableFrom(getQueryMethod().getReturnedObjectType())) {
      String targetProperty = firstToLowercase(methodName.substring(EXISTS_BY_PREFIX.length(), methodName.length()));
      logger.debug(String.format("Target Property : %s", targetProperty));
      Class<?> entityClass = getQueryMethod().getEntityInformation().getJavaType();

      try {
        Field field = entityClass.getDeclaredField(targetProperty);
        if (field.isAnnotationPresent(Bloom.class)) {
          Bloom bloom = field.getAnnotation(Bloom.class);
          return Optional.of(!ObjectUtils.isEmpty(bloom.name()) ? bloom.name()
              : String.format("bf:%s:%s", entityClass.getSimpleName(), field.getName()));
        }
      } catch (NoSuchFieldException e) {
        // NO-OP
      } catch (SecurityException e) {
        // NO-OP
      }
    }
    return Optional.empty();
  }

  public Object executeBloomQuery(Object[] parameters, String bloomFilter) {
    logger.debug(
        String.format("filter:%s, params:%s", bloomFilter, Arrays.toString(parameters)));
    BloomOperations<String> ops = modulesOperations.opsForBloom();
    return ops.exists(bloomFilter, parameters[0].toString());
  }

  private String prepareQuery(Object[] parameters) {
    logger.debug(
        String.format("parameters: %s", Arrays.toString(parameters)));
    StringBuilder preparedQuery = new StringBuilder();
    
    if (!queryFields.isEmpty()) {
      for (Pair<String,QueryClause> fieldClauses : queryFields) {
        String fieldName = fieldClauses.getFirst();
        QueryClause queryClause = fieldClauses.getSecond();
        int paramsCnt = queryClause.getValue().getNumberOfArguments();
        
        preparedQuery.append(queryClause.prepareQuery(fieldName, Arrays.copyOfRange(parameters, 0, paramsCnt)));
        preparedQuery.append(" ");
        
        parameters = Arrays.copyOfRange(parameters, paramsCnt, parameters.length);
      }
    } else {
      @SuppressWarnings("unchecked")
      Iterator<Parameter> iterator = (Iterator<Parameter>) queryMethod.getParameters().iterator();
      int index = 0;

      while (iterator.hasNext()) {
        Parameter p = iterator.next();
        String key = (p.getName().isPresent() ? p.getName().get() : (paramNames.size() > index ? paramNames.get(index) : ""));
        String v = "";
        
        if (parameters[index] instanceof Collection<?>) {
          @SuppressWarnings("rawtypes")
          Collection<?> c = (Collection) parameters[index];
          v = c.stream().map(n -> n.toString()).collect(Collectors.joining(" | "));
        } else {
          v = parameters[index].toString();
        }
        
        if (value.isBlank()) {
          preparedQuery.append("@"+key+":"+v).append(" ");
        } else {
          preparedQuery.append(value.replace("$"+key, v)).append(" ");;
        }
      }
    }

    return preparedQuery.toString();
  }
  
  private String firstToLowercase(String string) {
    char c[] = string.toCharArray();
    c[0] = Character.toLowerCase(c[0]);
    return new String(c);
  }
}
