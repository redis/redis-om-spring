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
 * A lexicographic comparison predicate that filters entities where a string field value
 * is lexicographically less than a specified value.
 * 
 * <p>This predicate works by querying the lexicographic sorted set maintained for
 * fields marked with {@code lexicographic=true} in their {@code @Indexed} or
 * {@code @Searchable} annotations. It uses Redis ZRANGEBYLEX to efficiently find
 * matching entries.</p>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find products with names before "M" alphabetically
 * entityStream.filter(Product$.NAME.lt("M"))
 * 
 * // Find users with IDs less than "user500"
 * entityStream.filter(User$.USER_ID.lt("user500"))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (must be String or convertible to String)
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see LexicographicGreaterThanPredicate
 * @see LexicographicBetweenPredicate
 */
public class LexicographicLessThanPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  /** The threshold value for comparison */
  private final T value;
  private final RedisModulesOperations<String> rmo;
  private final RediSearchIndexer indexer;

  /**
   * Creates a new LexicographicLessThanPredicate for the specified field and threshold.
   * 
   * @param field   the field accessor for the target string field
   * @param value   the threshold value (field must be lexicographically less than this)
   * @param rmo     the Redis operations helper
   * @param indexer the RediSearch indexer containing field metadata
   */
  public LexicographicLessThanPredicate(SearchFieldAccessor field, T value, RedisModulesOperations<String> rmo,
      RediSearchIndexer indexer) {
    super(field);
    this.value = value;
    this.rmo = rmo;
    this.indexer = indexer;
  }

  /**
   * Returns the threshold value for comparison.
   * 
   * @return the threshold value
   */
  public T getValue() {
    return value;
  }

  /**
   * Applies this lexicographic less-than predicate to the given query node.
   * 
   * <p>This method queries the lexicographic sorted set to find entity IDs where
   * the field value is lexicographically less than the threshold. It then
   * creates an ID-based query to match those specific entities.</p>
   * 
   * <p>If the value is empty or null, the predicate is ignored and the original
   * root node is returned unchanged.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return the modified query node with the lexicographic less-than condition applied,
   *         or the original root if the predicate cannot be applied
   */
  @Override
  public Node apply(Node root) {
    if (isEmpty(getValue())) {
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
    // For less than, we need to ensure we don't include the value itself
    // Since format is "value#id", we need to get everything before "value#" (excluded)
    String ltParam = value.toString() + "#"; // Exclude exact matches with this prefix
    Set<String> matches = rmo.template().opsForZSet().rangeByLex(sortedSetKey, Range.leftUnbounded(Range.Bound
        .exclusive(ltParam)), Limit.unlimited());

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