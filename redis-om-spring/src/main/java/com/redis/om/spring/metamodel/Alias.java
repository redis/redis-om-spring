package com.redis.om.spring.metamodel;

/**
 * Represents an alias for a metamodel field in Redis OM aggregation operations.
 * An Alias allows you to reference computed values, expressions, or transformed
 * fields within aggregation pipelines using a custom name.
 * 
 * <p>Aliases are commonly used in aggregation queries where you need to reference
 * the result of an expression or apply operation in subsequent stages of the pipeline.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * Alias<Product, Double> totalValue = Alias.of("total_value");
 * stream.apply("@price * @quantity", "total_value")
 *       .reduce(ReducerFunction.SUM, totalValue)
 *       .toList(ProductStats.class);
 * }</pre>
 * 
 * @param <E> the entity type this alias is associated with
 * @param <T> the type of the aliased value
 * 
 * @since 1.0
 * @see MetamodelField
 */
public class Alias<E, T> extends MetamodelField<E, T> {
  /**
   * Constructs a new Alias with the specified alias name.
   * 
   * @param alias the name of the alias, must not be null or empty
   */
  public Alias(String alias) {
    super(alias, String.class, true);
  }

  /**
   * Creates a new Alias instance with the specified alias name.
   * This is a convenient factory method for creating aliases.
   * 
   * @param <E>   the entity type this alias is associated with
   * @param <T>   the type of the aliased value
   * @param alias the name of the alias, must not be null or empty
   * @return a new Alias instance
   */
  public static <E, T> Alias<E, T> of(String alias) {
    return new Alias<>(alias);
  }
}
