package com.redis.om.spring.repository.query.autocomplete;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.repository.query.RepositoryQuery;

import com.redis.om.spring.annotations.AutoComplete;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.util.ObjectUtils;

import io.redisearch.Suggestion;
import io.redisearch.client.SuggestionOptions;

public class AutoCompleteQueryExecutor {

  private static final Log logger = LogFactory.getLog(AutoCompleteQueryExecutor.class);
  public static final String AUTOCOMPLETE_PREFIX = "autoComplete";
  
  RepositoryQuery query;
  RedisModulesOperations<String> modulesOperations;

  public AutoCompleteQueryExecutor(RepositoryQuery query, RedisModulesOperations<String> modulesOperations) {
    this.query = query;
    this.modulesOperations = modulesOperations;
  }

  public Optional<String> getAutoCompleteDictionaryKey() {
    String methodName = query.getQueryMethod().getName();
    boolean hasExistByPrefix = methodName.startsWith(AUTOCOMPLETE_PREFIX);
    if (hasExistByPrefix && query.getQueryMethod().isCollectionQuery()) {
      String targetProperty = ObjectUtils
          .firstToLowercase(methodName.substring(AUTOCOMPLETE_PREFIX.length(), methodName.length()));
      logger.debug(String.format("Target Property : %s", targetProperty));
      Class<?> entityClass = query.getQueryMethod().getEntityInformation().getJavaType();

      try {
        Field field = entityClass.getDeclaredField(targetProperty);
        if (field.isAnnotationPresent(AutoComplete.class)) {
          AutoComplete bloom = field.getAnnotation(AutoComplete.class);
          return Optional.of(!org.apache.commons.lang3.ObjectUtils.isEmpty(bloom.name()) ? bloom.name()
              : String.format("sugg:%s:%s", entityClass.getSimpleName(), field.getName()));
        }
      } catch (NoSuchFieldException | SecurityException e) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  public List<Suggestion> executeAutoCompleteQuery(Object[] parameters, String autoCompleteKey) {
    logger.debug(String.format("Autocomplete Query: key:%s, params:%s", autoCompleteKey, Arrays.toString(parameters)));
    SearchOperations<String> ops = modulesOperations.opsForSearch(autoCompleteKey);
    
    SuggestionOptions options = SuggestionOptions.builder().build();
    if ((parameters.length > 1) && (parameters[1].getClass() == AutoCompleteOptions.class)) {
      options = ((AutoCompleteOptions)parameters[1]).toSuggestionOptions();
    }

    return ops.getSuggestion(parameters[0].toString(), options);
  }
}
