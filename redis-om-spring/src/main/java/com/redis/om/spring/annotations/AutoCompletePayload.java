package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Marks a field to be used as payload data for autocomplete suggestions.
 * When a field annotated with {@link AutoComplete} generates suggestions,
 * fields annotated with {@code @AutoCompletePayload} provide additional
 * contextual data that is stored with each suggestion.
 * 
 * <p>Payload data can be retrieved along with suggestions to provide
 * additional information for display or processing. This is useful for
 * storing metadata like categories, descriptions, or IDs that relate
 * to the autocomplete suggestion.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @Document
 * public class Product {
 * 
 * @AutoComplete(name = "product_names")
 *                    private String name;
 * 
 * @AutoCompletePayload
 *                      private String category;
 * 
 * @AutoCompletePayload
 *                      private String description;
 *                      }
 *                      }</pre>
 * 
 *                      <p>When autocomplete suggestions are generated for the product name,
 *                      the category and description will be stored as payload data and can
 *                      be retrieved along with the suggestions for enhanced user experience.</p>
 * 
 * @since 1.0
 * @see AutoComplete
 * @see com.redis.om.spring.autocomplete.AutoCompleteAspect
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface AutoCompletePayload {
  /**
   * The key name for the payload data. If not specified, the field name is used.
   * 
   * @return the payload key name
   */
  String value() default "";

  /**
   * Additional fields to include in the payload data.
   * This allows referencing other entity fields for composite payload.
   * 
   * @return array of field names to include in payload
   */
  String[] fields() default {};
}
