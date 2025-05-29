package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation to specify the RediSearch dialect version for query execution.
 * <p>
 * RediSearch supports different query dialect versions that provide varying
 * syntax capabilities and features. This annotation allows methods (typically
 * repository query methods) to explicitly declare which dialect version should
 * be used when executing the query.
 * </p>
 * <p>
 * The dialect version affects:
 * <ul>
 * <li>Query syntax parsing rules</li>
 * <li>Available query operators and functions</li>
 * <li>Backward compatibility with older query formats</li>
 * <li>Performance optimizations for specific query patterns</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * public interface UserRepository extends RedisDocumentRepository<User, String> {
 * 
 * @UseDialect(dialect = Dialect.THREE)
 * @Query("@name:{$name} @age:[18 +inf]")
 * List<User> findAdultsByName(@Param("name") String name);
 * 
 * @UseDialect(dialect = Dialect.TWO)
 *                     List<User> findByEmailContaining(String email);
 *                     }
 *                     }</pre>
 * 
 * @see com.redis.om.spring.annotations.Query
 * @see com.redis.om.spring.annotations.Dialect
 * @since 0.1.0
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.METHOD, ElementType.ANNOTATION_TYPE }
)
public @interface UseDialect {
  /**
   * The RediSearch dialect version to use for query execution.
   * <p>
   * Different dialect versions provide different query capabilities:
   * <ul>
   * <li>{@link Dialect#ONE} - Original RediSearch query syntax</li>
   * <li>{@link Dialect#TWO} - Enhanced syntax with better field type handling (default)</li>
   * <li>{@link Dialect#THREE} - Latest syntax with advanced features</li>
   * </ul>
   * 
   * @return the dialect version to use
   */
  Dialect dialect() default Dialect.TWO;
}
