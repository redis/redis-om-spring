package com.redis.om.spring.repository.query.lexicographic;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.util.Pair;
import org.springframework.util.ReflectionUtils;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.repository.query.RediSearchQuery;
import com.redis.om.spring.repository.query.clause.QueryClause;

/**
 * Executes lexicographic queries for repository methods.
 * This executor handles GreaterThan, LessThan, Between operations on fields
 * that have been marked with lexicographic=true.
 */
public class LexicographicQueryExecutor {
  private static final Log logger = LogFactory.getLog(LexicographicQueryExecutor.class);

  /**
   * Set of QueryClause types that represent lexicographic operations.
   */
  private static final Set<QueryClause> LEXICOGRAPHIC_QUERY_CLAUSES = Set.of(QueryClause.TEXT_GREATER_THAN,
      QueryClause.TEXT_LESS_THAN, QueryClause.TEXT_GREATER_THAN_EQUAL, QueryClause.TEXT_LESS_THAN_EQUAL,
      QueryClause.TEXT_BETWEEN, QueryClause.TAG_GREATER_THAN, QueryClause.TAG_LESS_THAN,
      QueryClause.TAG_GREATER_THAN_EQUAL, QueryClause.TAG_LESS_THAN_EQUAL, QueryClause.TAG_BETWEEN);

  private final RediSearchQuery rediSearchQuery;
  private final RedisModulesOperations<String> modulesOperations;
  private final RediSearchIndexer indexer;

  public LexicographicQueryExecutor(RediSearchQuery rediSearchQuery, RedisModulesOperations<String> modulesOperations,
      RediSearchIndexer indexer) {
    this.rediSearchQuery = rediSearchQuery;
    this.modulesOperations = modulesOperations;
    this.indexer = indexer;
  }

  /**
   * Processes lexicographic query parts and builds the appropriate RediSearch query.
   *
   * @param queryOrParts the query parts to process
   * @param parameters   the method parameters
   * @param domainType   the entity type
   * @return a prepared query string that uses ID-based filtering
   */
  public String processLexicographicQuery(List<List<Pair<String, QueryClause>>> queryOrParts, Object[] parameters,
      Class<?> domainType) {
    logger.debug(String.format("Processing lexicographic query with %d queryOrParts and %d parameters", queryOrParts
        .size(), parameters.length));

    List<Object> params = new ArrayList<>(Arrays.asList(parameters));
    List<Set<String>> orResults = new ArrayList<>();
    boolean hasLexicographicQuery = false;
    boolean hasNonLexicographicQuery = false;
    int paramIndex = 0;

    for (List<Pair<String, QueryClause>> orPartParts : queryOrParts) {
      Set<String> andResults = null;

      for (Pair<String, QueryClause> pair : orPartParts) {
        String fieldName = pair.getFirst();
        QueryClause queryClause = pair.getSecond();
        logger.debug(String.format("Processing field: %s with queryClause: %s", fieldName, queryClause));

        // Check if this is a lexicographic query
        if (isLexicographicQuery(queryClause)) {
          hasLexicographicQuery = true;
          int numParams = queryClause.getClauseTemplate().getNumberOfArguments();
          Object[] queryParams = Arrays.copyOfRange(parameters, paramIndex, paramIndex + numParams);
          paramIndex += numParams;

          Set<String> entityIds = executeLexicographicQuery(fieldName, queryClause, queryParams, domainType);
          if (andResults == null) {
            andResults = new HashSet<>(entityIds);
          } else {
            // For AND queries within an OR part, we need to intersect
            andResults.retainAll(entityIds);
          }
        } else {
          hasNonLexicographicQuery = true;
          // Skip parameters for non-lexicographic queries
          int numParams = queryClause.getClauseTemplate().getNumberOfArguments();
          paramIndex += numParams;
        }
      }

      if (andResults != null && !andResults.isEmpty()) {
        orResults.add(andResults);
      }
    }

    // Remove the check for non-lexicographic queries since we only get called for all-lexicographic queries

    if (hasLexicographicQuery && !orResults.isEmpty()) {
      // Combine OR results
      Set<String> finalResults = new HashSet<>();
      for (Set<String> orResult : orResults) {
        finalResults.addAll(orResult);
      }

      if (!finalResults.isEmpty()) {
        String idQuery = buildIdQuery(finalResults);
        logger.debug(String.format("Built ID-based query: %s", idQuery));
        return idQuery;
      } else {
        return "@__id:{}"; // No matches
      }
    }

    return null;
  }

  private boolean isLexicographicQuery(QueryClause queryClause) {
    return LEXICOGRAPHIC_QUERY_CLAUSES.contains(queryClause);
  }

  private Set<String> executeLexicographicQuery(String fieldName, QueryClause queryClause, Object[] params,
      Class<?> domainType) {
    logger.debug(String.format("Executing lexicographic query for field: %s, queryClause: %s", fieldName, queryClause));

    // Get the entity prefix
    String entityPrefix = indexer.getKeyspaceForEntityClass(domainType);
    if (entityPrefix == null) {
      logger.debug("Entity prefix is null");
      return Collections.emptySet();
    }
    logger.debug("Entity prefix: " + entityPrefix);

    // Get the actual field name (without alias)
    String actualFieldName = getActualFieldName(domainType, fieldName);
    if (actualFieldName == null) {
      logger.debug(String.format("Could not find actual field name for alias: %s", fieldName));
      return Collections.emptySet();
    }
    logger.debug(String.format("Actual field name: %s", actualFieldName));

    // Check if the field has lexicographic=true
    if (!isFieldLexicographic(domainType, actualFieldName)) {
      logger.debug(String.format("Field %s is not lexicographic", actualFieldName));
      return Collections.emptySet();
    }

    // Construct the sorted set key
    String sortedSetKey = entityPrefix + actualFieldName + ":lex";
    logger.debug(String.format("Sorted set key: %s", sortedSetKey));

    // Execute the appropriate range query
    Set<String> matches = executeRangeQuery(sortedSetKey, queryClause, params);
    logger.debug(String.format("Range query returned %d matches", matches.size()));

    // Extract entity IDs from matches
    Set<String> result = matches.stream().map(match -> {
      int hashIndex = match.lastIndexOf('#');
      return hashIndex >= 0 ? match.substring(hashIndex + 1) : null;
    }).filter(Objects::nonNull).collect(Collectors.toSet());

    logger.debug(String.format("Extracted %d entity IDs: %s", result.size(), result));
    return result;
  }

  private Set<String> executeRangeQuery(String sortedSetKey, QueryClause queryClause, Object[] params) {
    logger.debug(String.format("Executing range query on %s with clause %s and param: %s", sortedSetKey, queryClause,
        params[0]));

    switch (queryClause) {
      case TEXT_GREATER_THAN:
      case TAG_GREATER_THAN:
        // When doing greater than, we need to exclude exact matches with the same prefix
        // Since our format is "value#id", we append a high character to ensure we skip all entries with this prefix
        String gtParam = params[0].toString() + "\uffff"; // Unicode max character
        Set<String> results = modulesOperations.template().opsForZSet().rangeByLex(sortedSetKey,
            org.springframework.data.redis.connection.RedisZSetCommands.Range.range().gt(gtParam),
            org.springframework.data.redis.connection.RedisZSetCommands.Limit.unlimited());
        logger.debug(String.format("ZRANGEBYLEX %s (%s +inf returned: %s", sortedSetKey, gtParam, results));
        return results;

      case TEXT_LESS_THAN:
      case TAG_LESS_THAN:
        // For less than, we need to ensure we don't include the value itself
        // Since format is "value#id", we need to get everything before "value#" (excluded)
        String ltParam = params[0].toString() + "#"; // Exclude exact matches with this prefix
        return modulesOperations.template().opsForZSet().rangeByLex(sortedSetKey,
            org.springframework.data.redis.connection.RedisZSetCommands.Range.range().lt(ltParam),
            org.springframework.data.redis.connection.RedisZSetCommands.Limit.unlimited());

      case TEXT_GREATER_THAN_EQUAL:
      case TAG_GREATER_THAN_EQUAL:
        // For greater than or equal, we include the value itself
        // Since format is "value#id", we start from exactly "value#"
        String gteParam = params[0].toString() + "#"; // Include exact matches with this prefix
        return modulesOperations.template().opsForZSet().rangeByLex(sortedSetKey,
            org.springframework.data.redis.connection.RedisZSetCommands.Range.range().gte(gteParam),
            org.springframework.data.redis.connection.RedisZSetCommands.Limit.unlimited());

      case TEXT_LESS_THAN_EQUAL:
      case TAG_LESS_THAN_EQUAL:
        // For less than or equal, we include all values with this prefix
        // Since format is "value#id", we use high unicode char to include all IDs with this value
        String lteParam = params[0].toString() + "\uffff"; // Include all exact matches with this prefix
        return modulesOperations.template().opsForZSet().rangeByLex(sortedSetKey,
            org.springframework.data.redis.connection.RedisZSetCommands.Range.range().lte(lteParam),
            org.springframework.data.redis.connection.RedisZSetCommands.Limit.unlimited());

      case TEXT_BETWEEN:
      case TAG_BETWEEN:
        // For between queries, we include both bounds
        // Start from exactly "minValue#" (inclusive) to "maxValue\uffff" (inclusive of all with maxValue)
        String minParam = params[0].toString() + "#";
        String maxParam = params[1].toString() + "\uffff";
        return modulesOperations.template().opsForZSet().rangeByLex(sortedSetKey,
            org.springframework.data.redis.connection.RedisZSetCommands.Range.range().gte(minParam).lte(maxParam),
            org.springframework.data.redis.connection.RedisZSetCommands.Limit.unlimited());

      default:
        return Collections.emptySet();
    }
  }

  private String getActualFieldName(Class<?> domainType, String fieldAlias) {
    // Try to find the field by alias or name
    for (Field field : domainType.getDeclaredFields()) {
      if (field.isAnnotationPresent(Indexed.class)) {
        Indexed indexed = field.getAnnotation(Indexed.class);
        String alias = indexed.alias().isBlank() ? field.getName() : indexed.alias();
        if (alias.equals(fieldAlias) || field.getName().equals(fieldAlias)) {
          return field.getName();
        }
      } else if (field.isAnnotationPresent(Searchable.class)) {
        Searchable searchable = field.getAnnotation(Searchable.class);
        String alias = searchable.alias().isBlank() ? field.getName() : searchable.alias();
        if (alias.equals(fieldAlias) || field.getName().equals(fieldAlias)) {
          return field.getName();
        }
      }
    }
    return null;
  }

  private boolean isFieldLexicographic(Class<?> domainType, String fieldName) {
    Field field = ReflectionUtils.findField(domainType, fieldName);
    if (field == null) {
      return false;
    }

    if (field.isAnnotationPresent(Indexed.class)) {
      return field.getAnnotation(Indexed.class).lexicographic();
    } else if (field.isAnnotationPresent(Searchable.class)) {
      return field.getAnnotation(Searchable.class).lexicographic();
    }

    return false;
  }

  private String buildIdQuery(Set<String> entityIds) {
    if (entityIds.isEmpty()) {
      return "@id:{}"; // No matches
    }

    // Build query like: @id:{id1|id2|id3}
    String idList = entityIds.stream().collect(Collectors.joining("|"));
    return "@id:{" + idList + "}";
  }
}