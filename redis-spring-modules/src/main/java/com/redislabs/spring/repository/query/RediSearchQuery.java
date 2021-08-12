package com.redislabs.spring.repository.query;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;

import com.google.gson.Gson;
import com.redislabs.spring.annotations.Aggregation;
import com.redislabs.spring.ops.RedisModulesOperations;
import com.redislabs.spring.ops.search.SearchOperations;

import io.redisearch.AggregationResult;
import io.redisearch.Query;
import io.redisearch.SearchResult;
import io.redisearch.aggregation.AggregationBuilder;

public class RediSearchQuery implements RepositoryQuery {
  
  private final QueryMethod queryMethod;
  private final String searchIndex;
  private static enum RediSearchQueryType { QUERY, AGGREGATION };
  private RediSearchQueryType type;
  private static final Gson gson = new Gson();
  private String value;
  
  // query fields
  private String[] returnFields;
  
  // aggregation field
  private String[] load;
  
  RedisModulesOperations<String, String> modulesOperations;

  @SuppressWarnings("unchecked")
  public RediSearchQuery(QueryMethod queryMethod, RepositoryMetadata metadata, QueryMethodEvaluationContextProvider evaluationContextProvider,
      KeyValueOperations keyValueOperations, RedisModulesOperations<?,?> rmo, Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {
    
    this.modulesOperations = (RedisModulesOperations<String, String>) rmo; 
    this.queryMethod = queryMethod;
    this.searchIndex = this.queryMethod.getEntityInformation().getJavaType().getSimpleName() + "Idx";
    
    Class<?> repoClass = metadata.getRepositoryInterface();
    @SuppressWarnings("rawtypes")
    Class[] params = queryMethod.getParameters().stream().map(p -> p.getType()).toArray(Class[]::new);
    try {
      java.lang.reflect.Method method = repoClass.getDeclaredMethod(queryMethod.getName(), params);
      if (method.isAnnotationPresent(com.redislabs.spring.annotations.Query.class)) {
        com.redislabs.spring.annotations.Query queryAnnotation = method.getAnnotation(com.redislabs.spring.annotations.Query.class);
        this.type = RediSearchQueryType.QUERY;
        this.value = queryAnnotation.value();
        this.returnFields = queryAnnotation.returnFields();
      } else if (method.isAnnotationPresent(com.redislabs.spring.annotations.Aggregation.class)) {
        Aggregation aggregation = method.getAnnotation(Aggregation.class);
        this.type = RediSearchQueryType.AGGREGATION;
        this.value = aggregation.value();
        this.load = aggregation.load();
      }
    } catch (NoSuchMethodException | SecurityException e) {
      System.out.println(String.format(">>>> Did not find method %s(%s)", queryMethod.getName(), Arrays.toString(params)));
    }    
  }

  @Override
  public Object execute(Object[] parameters) {
    return (type == RediSearchQueryType.QUERY) ? executeQuery(parameters) : executeAggregation(parameters);
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
    } else if (queryMethod.isCollectionQuery()) {
      result =  searchResult.docs.stream().map(d -> gson.fromJson(d.get("$").toString(), queryMethod.getReturnedObjectType())).collect(Collectors.toList());
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
    String preparedQuery = value;
    
    @SuppressWarnings("unchecked")
    Iterator<Parameter> iterator = (Iterator<Parameter>) queryMethod.getParameters().iterator();
    int index = 0;
    while (iterator.hasNext()) {
      Parameter p = iterator.next();
      String key = "$" + p.getName().get();
      String value = parameters[index].toString();
      if (parameters[index] instanceof Collection<?>){
        @SuppressWarnings("rawtypes")
        Collection<?> c = (Collection)parameters[index];
        value = c.stream().map( n -> n.toString()).collect(Collectors.joining(", "));
      }
      
      preparedQuery = preparedQuery.replace(key, value);
      index = index + 1;
    }
    
    return preparedQuery;
  }

}
