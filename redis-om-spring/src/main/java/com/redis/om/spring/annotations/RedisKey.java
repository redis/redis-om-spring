package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Marks a field to be populated with the Redis key during search operations.
 * <p>
 * When a field is annotated with @RedisKey, it will automatically be populated
 * with the full Redis key (including prefix) when the entity is retrieved
 * through search operations.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @Document
 * public class MyDocument {
 * 
 * @Id
 *     private String id;
 *
 * @RedisKey
 *           private String redisKey;
 *
 *           // other fields...
 *           }
 *           }
 *           </pre>
 *
 *           <p>Note: Only one field per entity should be annotated with @RedisKey.</p>
 *
 * @author Brian Sam-Bodden
 * @since 1.0.0
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.METHOD }
)
public @interface RedisKey {
}