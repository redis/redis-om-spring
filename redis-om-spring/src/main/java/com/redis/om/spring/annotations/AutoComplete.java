package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation for enabling autocomplete functionality on text fields
 * using Redis's autocomplete capabilities.
 * 
 * <p>When applied to a field, this annotation creates an autocomplete
 * suggestion dictionary that can be used to provide fast, prefix-based
 * text suggestions. This is particularly useful for implementing
 * search-as-you-type functionality in user interfaces.</p>
 * 
 * <p>The autocomplete feature uses Redis's built-in suggestion engine
 * to provide efficient prefix matching and scoring of suggestions
 * based on usage patterns.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @Document
 * public class Product {
 * 
 * @Id
 *     private String id;
 * 
 * @AutoComplete(name = "product_names")
 * @Indexed
 *          private String name;
 * 
 * @AutoComplete
 *               private String description;
 *               }
 *               }
 *               </pre>
 * 
 *               <p>The autocomplete suggestions can then be accessed through the
 *               Redis OM Spring autocomplete operations or directly through
 *               the suggestion API.</p>
 * 
 * @see com.redis.om.spring.autocomplete.AutoCompleteAspect
 * @see com.redis.om.spring.autocomplete.Suggestion
 * @since 1.0
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface AutoComplete {
  /**
   * The name of the autocomplete suggestion dictionary.
   * 
   * <p>If not specified, a default name will be generated based on
   * the entity class name and field name. Multiple fields can share
   * the same dictionary name to combine their suggestions.</p>
   * 
   * <p>The dictionary name is used as the key in Redis to store
   * the suggestion data structure.</p>
   * 
   * @return the name of the autocomplete dictionary
   */
  String name() default "";
}
