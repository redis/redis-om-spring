package com.redis.om.spring.search.stream.predicates;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

/**
 * A predicate that combines multiple predicates using logical AND operations.
 * This class allows for composing complex search conditions by combining
 * multiple individual predicates into a single compound predicate.
 * 
 * <p>AndPredicate maintains a collection of predicates that must all evaluate
 * to true for the overall predicate to be satisfied. It's commonly used in
 * entity stream filtering where multiple conditions need to be met simultaneously.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * AndPredicate<Product, String> complexFilter = new AndPredicate<>(Product$.name.eq("widget"));
 * complexFilter.addPredicate(Product$.price.between(10, 100));
 * complexFilter.addPredicate(Product$.category.eq("electronics"));
 * 
 * List<Product> results = entityStream.filter(complexFilter).toList();
 * }</pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type of the root predicate
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see SearchFieldPredicate
 */
public class AndPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final List<Predicate<T>> predicates = new ArrayList<>();

  /**
   * Constructs a new AndPredicate with an initial root predicate.
   * Additional predicates can be added using {@link #addPredicate(Predicate)}.
   * 
   * @param root the initial predicate that serves as the foundation for this AND operation
   */
  public AndPredicate(SearchFieldPredicate<E, T> root) {
    predicates.add(root);
  }

  /**
   * Adds an additional predicate to this AND operation.
   * The new predicate will be combined with existing predicates using logical AND.
   * 
   * @param predicate the predicate to add to this AND operation
   */
  public void addPredicate(Predicate<T> predicate) {
    this.predicates.add(predicate);
  }

  /**
   * Returns a stream of all predicates contained in this AND operation.
   * This includes both the root predicate and any additional predicates
   * that have been added.
   * 
   * @return a stream of all predicates in this AND operation
   */
  public Stream<Predicate<T>> stream() {
    return predicates.stream();
  }

  /**
   * Applies this AND predicate to create a RediSearch query node.
   * <p>
   * This method transforms all contained predicates into RediSearch query nodes
   * and combines them using an intersect operation, which represents logical AND
   * in RediSearch query syntax. The resulting AND node is then combined with
   * the existing root node using another AND operation (intersect).
   * </p>
   * <p>
   * This ensures that when multiple filters are chained, the AND operation
   * is properly scoped. For example:
   * <pre>
   * .filter(Field1.eq("A"))
   * .filter(Field2.eq("B").and(Field3.eq("C")))
   * </pre>
   * produces: {@code (@field1:{A}) (@field2:{B} @field3:{C})}
   * </p>
   *
   * @param root the root query node to build upon
   * @return a Node representing the intersection (AND) of all contained predicates,
   *         combined with the root using AND
   */
  @SuppressWarnings(
    "rawtypes"
  )
  @Override
  public Node apply(Node root) {
    // Apply each predicate to an empty root to get standalone conditions
    Node[] nodes = stream().map(p -> ((SearchFieldPredicate) p).apply(QueryBuilders.union())).toArray(Node[]::new);

    // Create the AND of all predicates
    Node andNode = QueryBuilders.intersect(nodes);

    // Combine the AND result with the existing root using AND
    // If root is empty, just return the AND node
    return root.toString().isBlank() ? andNode : QueryBuilders.intersect(root, andNode);
  }

}
