package com.redis.om.spring.search.stream.predicates.lexicographic;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.util.Set;
import java.util.stream.Collectors;

import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * A lexicographic comparison predicate that filters entities where a string field value
 * is lexicographically greater than a specified value.
 * 
 * <p>This predicate works by querying the lexicographic sorted set maintained for
 * fields marked with {@code lexicographic=true} in their {@code @Indexed} or
 * {@code @Searchable} annotations. It uses Redis ZRANGEBYLEX to efficiently find
 * matching entries.</p>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find products with names after "P" alphabetically
 * entityStream.filter(Product$.NAME.gt("P"))
 * 
 * // Find users with IDs greater than "user100"
 * entityStream.filter(User$.USER_ID.gt("user100"))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (must be String or convertible to String)
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see LexicographicLessThanPredicate
 * @see LexicographicBetweenPredicate
 */
public class LexicographicGreaterThanPredicate<E, T> extends BaseAbstractPredicate<E, T> {
  /** The threshold value for comparison */
  private final T value;
  private final RedisModulesOperations<String> rmo;
  private final RediSearchIndexer indexer;

  /**
   * Creates a new LexicographicGreaterThanPredicate for the specified field and threshold.
   * 
   * @param field   the field accessor for the target string field
   * @param value   the threshold value (field must be lexicographically greater than this)
   * @param rmo     the Redis operations helper
   * @param indexer the RediSearch indexer containing field metadata
   */
  public LexicographicGreaterThanPredicate(SearchFieldAccessor field, T value, RedisModulesOperations<String> rmo,
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
   * Applies this lexicographic greater-than predicate to the given query node.
   * 
   * <p>This method queries the lexicographic sorted set to find entity IDs where
   * the field value is lexicographically greater than the threshold. It then
   * creates an ID-based query to match those specific entities.</p>
   * 
   * <p>If the value is empty or null, the predicate is ignored and the original
   * root node is returned unchanged.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return the modified query node with the lexicographic greater-than condition applied,
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
    // For greater than, we need to exclude exact matches with the same prefix
    // Since our format is "value#id", we append a high character to ensure we skip all entries with this prefix
    String gtParam = value.toString() + "\uffff"; // Unicode max character
    Set<String> matches = rmo.template().opsForZSet().rangeByLex(sortedSetKey,
        org.springframework.data.redis.connection.RedisZSetCommands.Range.range().gt(gtParam),
        org.springframework.data.redis.connection.RedisZSetCommands.Limit.unlimited());

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

    // When root is empty (union node), we just return the ID query directly
    // without intersecting with the empty root to avoid extra parentheses
    if (root == null || root.toString().isEmpty()) {
      return QueryBuilders.intersect().add("id", "{" + idQuery + "}");
    } else {
      return QueryBuilders.intersect(root).add("id", "{" + idQuery + "}");
    }
  }
}