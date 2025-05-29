package com.redis.om.spring.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining mathematical expressions and transformations
 * to apply during Redis aggregation operations.
 * 
 * <p>The Apply annotation allows you to create computed fields, perform
 * mathematical operations, and apply functions to data during aggregation.
 * It's commonly used within {@link Aggregation} annotations to create
 * derived values or perform calculations on grouped data.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @Aggregation(
 * value = "@category:{electronics}",
 * groupBy = @GroupBy(properties = "brand"),
 * apply = {
 * 
 * @Apply(expression = "count()", alias = "total"),
 * @Apply(expression = "avg(@price)", alias = "avgPrice"),
 * @Apply(expression = "@total * @avgPrice", alias = "revenue")
 *                   }
 *                   )
 *                   List<AggregationResult> getBrandStatistics();
 *                   }
 *                   </pre>
 * 
 *                   <p>Common expressions include:</p>
 *                   <ul>
 *                   <li>Arithmetic: {@code @field1 + @field2}, {@code @price * @quantity}</li>
 *                   <li>Functions: {@code count()}, {@code sum(@field)}, {@code avg(@field)}</li>
 *                   <li>String operations: {@code upper(@name)}, {@code format("%s-%s", @field1, @field2)}</li>
 *                   </ul>
 * 
 * @see Aggregation
 * @since 1.0
 */
@Target(
  { ElementType.METHOD, ElementType.ANNOTATION_TYPE }
)
@Retention(
  RetentionPolicy.RUNTIME
)
public @interface Apply {
  /**
   * The mathematical expression or function to apply.
   * Uses RediSearch aggregation expression syntax, which supports
   * arithmetic operations, built-in functions, and field references.
   * 
   * <p>Field references use the {@code @fieldName} syntax.
   * Common functions include count(), sum(), avg(), min(), max(), etc.</p>
   * 
   * @return the expression to evaluate
   */
  String expression() default "";

  /**
   * The alias name for the computed field.
   * This name will be used to reference the result of the expression
   * in subsequent operations and in the final results.
   * 
   * <p>If not specified, a default name may be generated based on the expression.</p>
   * 
   * @return the alias for the computed field
   */
  String alias() default "";
}
