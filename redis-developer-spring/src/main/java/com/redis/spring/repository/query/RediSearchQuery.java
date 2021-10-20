package com.redis.spring.repository.query;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.keyvalue.core.KeyValueOperations;
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

import com.google.gson.Gson;
import com.redis.spring.annotations.Aggregation;
import com.redis.spring.annotations.GeoIndexed;
import com.redis.spring.annotations.NumericIndexed;
import com.redis.spring.annotations.TagIndexed;
import com.redis.spring.annotations.TextIndexed;
import com.redis.spring.ops.RedisModulesOperations;
import com.redis.spring.ops.search.SearchOperations;
import com.redis.spring.repository.query.clause.QueryClause;
import com.redis.spring.serialization.gson.GsonBuidlerFactory;

import io.redisearch.AggregationResult;
import io.redisearch.Query;
import io.redisearch.Schema.FieldType;
import io.redisearch.SearchResult;
import io.redisearch.aggregation.AggregationBuilder;
import lombok.ToString;

@ToString
public class RediSearchQuery implements RepositoryQuery {

  private final QueryMethod queryMethod;
  private final String searchIndex;

  private static enum RediSearchQueryType {
    QUERY,
    AGGREGATION
  };

  private RediSearchQueryType type;
  private static final Gson gson = GsonBuidlerFactory.getBuilder().create();
  private String value;

  private RepositoryMetadata metadata;

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
  public RediSearchQuery(QueryMethod queryMethod, RepositoryMetadata metadata,
      QueryMethodEvaluationContextProvider evaluationContextProvider, KeyValueOperations keyValueOperations,
      RedisModulesOperations<?, ?> rmo, Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {
    System.out.println(">>>> RediSearchQuery#new.... " + queryMethod.getName());

    this.modulesOperations = (RedisModulesOperations<String, String>) rmo;
    this.queryMethod = queryMethod;
    this.searchIndex = this.queryMethod.getEntityInformation().getJavaType().getSimpleName() + "Idx";
    this.metadata = metadata;
    this.domainType = this.queryMethod.getEntityInformation().getJavaType();

    Class<?> repoClass = metadata.getRepositoryInterface();
    @SuppressWarnings("rawtypes")
    Class[] params = queryMethod.getParameters().stream().map(p -> p.getType()).toArray(Class[]::new);
    try {
      java.lang.reflect.Method method = repoClass.getDeclaredMethod(queryMethod.getName(), params);
      if (method.isAnnotationPresent(com.redis.spring.annotations.Query.class)) {
        com.redis.spring.annotations.Query queryAnnotation = method
            .getAnnotation(com.redis.spring.annotations.Query.class);
        this.type = RediSearchQueryType.QUERY;
        this.value = queryAnnotation.value();
        this.returnFields = queryAnnotation.returnFields();
      } else if (method.isAnnotationPresent(com.redis.spring.annotations.Aggregation.class)) {
        Aggregation aggregation = method.getAnnotation(Aggregation.class);
        this.type = RediSearchQueryType.AGGREGATION;
        this.value = aggregation.value();
        this.load = aggregation.load();
      } else {
        PartTree pt = new PartTree(queryMethod.getName(), metadata.getDomainType());

        Streamable<Part> queryParts = pt.getParts();
        for (Part part : queryParts) {
          String fieldName = part.getProperty().getSegment();
          
          Field field;
          try {
            field = domainType.getDeclaredField(fieldName);
            if (field.isAnnotationPresent(TextIndexed.class)) {
              queryFields.add(Pair.of(fieldName, QueryClause.get(FieldType.FullText, part.getType())));
            } else if (field.isAnnotationPresent(TagIndexed.class)) {
              queryFields.add(Pair.of(fieldName, QueryClause.get(FieldType.Tag, part.getType())));
            } else if (field.isAnnotationPresent(GeoIndexed.class)) {
              queryFields.add(Pair.of(fieldName, QueryClause.get(FieldType.Geo, part.getType())));
            } else if (field.isAnnotationPresent(NumericIndexed.class)) {
              queryFields.add(Pair.of(fieldName, QueryClause.get(FieldType.Numeric, part.getType())));
            }
          } catch (NoSuchFieldException e) {
            System.out.println(">>> NoSuchFieldException for ==> " + fieldName);
          }
        }

        this.type = RediSearchQueryType.QUERY;
        this.returnFields = new String[] {};
      }
    } catch (NoSuchMethodException | SecurityException e) {
      System.out
          .println(String.format(">>>> Did not find method %s(%s)", queryMethod.getName(), Arrays.toString(params)));
    }
  }

  @Override
  public Object execute(Object[] parameters) {
    if (type == RediSearchQueryType.QUERY) {
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
      result = gson.fromJson(jsonResult, queryMethod.getReturnedObjectType());
    } else if (queryMethod.isQueryForEntity() && queryMethod.isCollectionQuery()) {
      result = searchResult.docs.stream()
          .map(d -> gson.fromJson(d.get("$").toString(), queryMethod.getReturnedObjectType()))
          .collect(Collectors.toList());
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

  private String prepareQuery(Object[] parameters) {
    System.out.println(">>> parameters: " + Arrays.toString(parameters));
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

}
