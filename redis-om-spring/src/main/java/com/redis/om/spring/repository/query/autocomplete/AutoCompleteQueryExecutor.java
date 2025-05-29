package com.redis.om.spring.repository.query.autocomplete;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.ReflectionUtils;

import com.redis.om.spring.annotations.AutoComplete;
import com.redis.om.spring.autocomplete.Suggestion;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.util.ObjectUtils;

/**
 * Query executor for Redis autocomplete operations in repository methods.
 * This class analyzes repository query methods to determine if they are autocomplete
 * operations and executes them against Redis suggestion dictionaries.
 * 
 * <p>AutoCompleteQueryExecutor identifies methods that start with "autoComplete"
 * and validates that the target field is annotated with {@link AutoComplete}.
 * It then executes the autocomplete query using the appropriate Redis operations.</p>
 * 
 * <p>Example usage in repository:</p>
 * <pre>{@code
 * public interface ProductRepository extends RedisDocumentRepository<Product, String> {
 *     List<Suggestion> autoCompleteName(String prefix);
 *     List<Suggestion> autoCompleteName(String prefix, AutoCompleteOptions options);
 * }
 * }</pre>
 * 
 * @since 1.0
 * @see AutoComplete
 * @see AutoCompleteOptions
 */
public class AutoCompleteQueryExecutor {

  /** The prefix used to identify autocomplete methods */
  public static final String AUTOCOMPLETE_PREFIX = "autoComplete";

  private static final Log logger = LogFactory.getLog(AutoCompleteQueryExecutor.class);

  /** The repository query being executed */
  final RepositoryQuery query;

  /** Redis modules operations for executing autocomplete commands */
  final RedisModulesOperations<String> modulesOperations;

  /**
   * Constructs a new AutoCompleteQueryExecutor for the specified query.
   * 
   * @param query             the repository query to execute
   * @param modulesOperations the Redis modules operations for executing commands
   */
  public AutoCompleteQueryExecutor(RepositoryQuery query, RedisModulesOperations<String> modulesOperations) {
    this.query = query;
    this.modulesOperations = modulesOperations;
  }

  /**
   * Determines the autocomplete dictionary key for the query method.
   * This method analyzes the method name to extract the target property
   * and validates that it has the {@link AutoComplete} annotation.
   * 
   * @return an Optional containing the dictionary key if this is a valid autocomplete method,
   *         or empty if the method is not an autocomplete operation
   */
  public Optional<String> getAutoCompleteDictionaryKey() {
    String methodName = query.getQueryMethod().getName();
    boolean hasExistByPrefix = methodName.startsWith(AUTOCOMPLETE_PREFIX);
    if (hasExistByPrefix && query.getQueryMethod().isCollectionQuery()) {
      String targetProperty = ObjectUtils.firstToLowercase(methodName.substring(AUTOCOMPLETE_PREFIX.length()));
      logger.debug(String.format("Target Property : %s", targetProperty));
      Class<?> entityClass = query.getQueryMethod().getEntityInformation().getJavaType();

      try {
        Field field = ReflectionUtils.findField(entityClass, targetProperty);
        if (field == null) {
          return Optional.empty();
        }
        if (field.isAnnotationPresent(AutoComplete.class)) {
          AutoComplete bloom = field.getAnnotation(AutoComplete.class);
          return Optional.of(!org.apache.commons.lang3.ObjectUtils.isEmpty(bloom.name()) ?
              bloom.name() :
              String.format(Suggestion.KEY_FORMAT_STRING, entityClass.getSimpleName(), field.getName()));
        }
      } catch (SecurityException e) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  /**
   * Executes the autocomplete query with the given parameters.
   * This method handles both simple prefix queries and queries with options.
   * 
   * @param parameters      the query parameters (prefix and optional AutoCompleteOptions)
   * @param autoCompleteKey the Redis key for the autocomplete dictionary
   * @return a list of autocomplete suggestions
   */
  public List<Suggestion> executeAutoCompleteQuery(Object[] parameters, String autoCompleteKey) {
    logger.debug(String.format("Autocomplete Query: key:%s, params:%s", autoCompleteKey, Arrays.toString(parameters)));
    SearchOperations<String> ops = modulesOperations.opsForSearch(autoCompleteKey);

    if ((parameters.length > 1) && (parameters[1].getClass() == AutoCompleteOptions.class)) {
      AutoCompleteOptions options = (AutoCompleteOptions) parameters[1];
      return ops.getSuggestion(autoCompleteKey, parameters[0].toString(), options);
    } else {
      return ops.getSuggestion(autoCompleteKey, parameters[0].toString());
    }
  }
}
