package com.redis.om.spring.search.stream.predicates.lexicographic;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;

import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * A lexicographic range predicate that filters entities where a string field value
 * is lexicographically between two specified values (inclusive).
 * 
 * <p>This predicate works by querying the lexicographic sorted set maintained for
 * fields marked with {@code lexicographic=true} in their {@code @Indexed} or
 * {@code @Searchable} annotations. It uses Redis ZRANGEBYLEX to efficiently find
 * matching entries within the specified range.</p>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find products with names between "A" and "M" alphabetically
 * entityStream.filter(Product$.NAME.between("A", "M"))
 * 
 * // Find users with IDs between "user100" and "user500"
 * entityStream.filter(User$.USER_ID.between("user100", "user500"))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (must be String or convertible to String)
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see LexicographicGreaterThanPredicate
 * @see LexicographicLessThanPredicate
 */
public class LexicographicBetweenPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  /** The minimum value for the range (inclusive) */
  private final T min;
  /** The maximum value for the range (inclusive) */
  private final T max;
  private final RedisModulesOperations<String> rmo;
  private final RediSearchIndexer indexer;

  /**
   * Creates a new LexicographicBetweenPredicate for the specified field and range.
   * 
   * @param field   the field accessor for the target string field
   * @param min     the minimum value (inclusive)
   * @param max     the maximum value (inclusive)
   * @param rmo     the Redis operations helper
   * @param indexer the RediSearch indexer containing field metadata
   */
  public LexicographicBetweenPredicate(SearchFieldAccessor field, T min, T max, RedisModulesOperations<String> rmo,
      RediSearchIndexer indexer) {
    super(field);
    this.min = min;
    this.max = max;
    this.rmo = rmo;
    this.indexer = indexer;
  }

  /**
   * Returns the minimum value for the range.
   * 
   * @return the minimum value
   */
  public T getMin() {
    return min;
  }

  /**
   * Returns the maximum value for the range.
   * 
   * @return the maximum value
   */
  public T getMax() {
    return max;
  }

  /**
   * Applies this lexicographic between predicate to the given query node.
   * 
   * <p>This method queries the lexicographic sorted set to find entity IDs where
   * the field value is lexicographically between the min and max values (inclusive).
   * It then creates an ID-based query to match those specific entities.</p>
   * 
   * <p>If either value is empty or null, the predicate is ignored and the original
   * root node is returned unchanged.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return the modified query node with the lexicographic between condition applied,
   *         or the original root if the predicate cannot be applied
   */
  @Override
  public Node apply(Node root) {
    if (isEmpty(getMin()) || isEmpty(getMax())) {
      return root;
    }

    // Get the entity class and field name
    Class<?> entityClass = getSearchFieldAccessor().getDeclaringClass();
    String fieldName = getSearchFieldAccessor().getField().getName();

    // Get the entity prefix
    String entityPrefix = indexer.getKeyspaceForEntityClass(entityClass);
    if (entityPrefix == null) {
      return root;
    }

    // Construct the sorted set key
    String sortedSetKey = entityPrefix + fieldName + ":lex";

    // Use ZRANGEBYLEX to find matching entries
    // For between queries, we include both bounds
    // Start from exactly "minValue#" (inclusive) to "maxValue\uffff" (inclusive of all with maxValue)
    String minParam = min.toString() + "#";
    String maxParam = max.toString() + "\uffff";
    Set<String> matches = rmo.template().opsForZSet().rangeByLex(sortedSetKey, Range.closed(minParam, maxParam), Limit
        .unlimited());

    if (matches == null || matches.isEmpty()) {
      // No matches, return a query that matches nothing by using an impossible ID
      if (root == null || root.toString().isEmpty()) {
        return QueryBuilders.intersect().add("id", "{__NOMATCH__}");
      } else {
        return QueryBuilders.intersect(root).add("id", "{__NOMATCH__}");
      }
    }

    // Extract entity IDs from the matches (format is "value#entityId")
    Set<String> entityIds = matches.stream().map(match -> {
      int hashIndex = match.lastIndexOf('#');
      return hashIndex >= 0 ? match.substring(hashIndex + 1) : null;
    }).filter(id -> id != null).collect(Collectors.toSet());

    if (entityIds.isEmpty()) {
      // No valid IDs found, return a query that matches nothing
      if (root == null || root.toString().isEmpty()) {
        return QueryBuilders.intersect().add("id", "{__NOMATCH__}");
      } else {
        return QueryBuilders.intersect(root).add("id", "{__NOMATCH__}");
      }
    }

    // Build an ID-based query using the tag syntax: @id:{id1 | id2 | id3}
    // Join the IDs with " | " separator as per RediSearch tag syntax
    String idQuery = entityIds.stream().collect(Collectors.joining(" | "));

    // Handle empty root node case - check if root is null, empty, or union (which produces empty string)
    if (root == null || root.toString().isEmpty()) {
      return QueryBuilders.intersect().add("id", "{" + idQuery + "}");
    } else {
      return QueryBuilders.intersect(root).add("id", "{" + idQuery + "}");
    }
  }
}