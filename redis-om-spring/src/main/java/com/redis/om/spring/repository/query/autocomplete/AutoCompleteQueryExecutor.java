package com.redis.om.spring.repository.query.autocomplete;

import com.redis.om.spring.annotations.AutoComplete;
import com.redis.om.spring.autocomplete.Suggestion;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.util.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AutoCompleteQueryExecutor {

  private static final Log logger = LogFactory.getLog(AutoCompleteQueryExecutor.class);
  public static final String AUTOCOMPLETE_PREFIX = "autoComplete";
  
  final RepositoryQuery query;
  final RedisModulesOperations<String> modulesOperations;

  public AutoCompleteQueryExecutor(RepositoryQuery query, RedisModulesOperations<String> modulesOperations) {
    this.query = query;
    this.modulesOperations = modulesOperations;
  }

  public Optional<String> getAutoCompleteDictionaryKey() {
    String methodName = query.getQueryMethod().getName();
    boolean hasExistByPrefix = methodName.startsWith(AUTOCOMPLETE_PREFIX);
    if (hasExistByPrefix && query.getQueryMethod().isCollectionQuery()) {
      String targetProperty = ObjectUtils
          .firstToLowercase(methodName.substring(AUTOCOMPLETE_PREFIX.length()));
      logger.debug(String.format("Target Property : %s", targetProperty));
      Class<?> entityClass = query.getQueryMethod().getEntityInformation().getJavaType();

      try {
        Field field = entityClass.getDeclaredField(targetProperty);
        if (field.isAnnotationPresent(AutoComplete.class)) {
          AutoComplete bloom = field.getAnnotation(AutoComplete.class);
          return Optional.of(!org.apache.commons.lang3.ObjectUtils.isEmpty(bloom.name()) ? bloom.name()
              : String.format(Suggestion.KEY_FORMAT_STRING, entityClass.getSimpleName(), field.getName()));
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

    if ((parameters.length > 1) && (parameters[1].getClass() == AutoCompleteOptions.class)) {
      AutoCompleteOptions options = (AutoCompleteOptions)parameters[1];
      return ops.getSuggestion(autoCompleteKey, parameters[0].toString(), options);
    } else {
      return ops.getSuggestion(autoCompleteKey, parameters[0].toString());
    }
  }
}
