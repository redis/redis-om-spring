package com.redis.om.spring.annotations;

/**
 * Enumeration of supported RediSearch dialect versions.
 * This enum defines the different RediSearch query dialects that can be used
 * with Redis OM Spring for search operations.
 * 
 * <p>Different dialects provide varying levels of functionality and syntax support:
 * <ul>
 * <li>Dialect 1: Basic search functionality</li>
 * <li>Dialect 2: Enhanced query features and improved syntax</li>
 * <li>Dialect 3: Additional search capabilities</li>
 * </ul>
 * 
 * <p>The dialect affects how queries are constructed and what features are available
 * for search operations. Higher dialect numbers generally provide more functionality
 * but require compatible Redis Stack versions.</p>
 * 
 * @since 1.0
 * @see UseDialect
 */
public enum Dialect {
  /** RediSearch dialect version 1 */
  ONE(1),

  /** RediSearch dialect version 2 */
  TWO(2),

  /** RediSearch dialect version 3 */
  THREE(3);

  private final int value;

  /**
   * Creates a new Dialect with the specified version number.
   * 
   * @param value the numeric value of the dialect version
   */
  Dialect(int value) {
    this.value = value;
  }

  /**
   * Returns the numeric value of this dialect version.
   * 
   * @return the dialect version number
   */
  public int getValue() {
    return value;
  }
}
