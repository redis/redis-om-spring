package com.redis.om.spring.search.stream.actions;

import com.redis.om.spring.ops.json.JSONOperations;

/**
 * Functional interface for components that require JSON operations to function.
 * This interface provides a contract for injecting JSON operations into action classes
 * that need to perform operations on Redis JSON documents.
 * 
 * <p>Classes implementing this interface can receive a {@link JSONOperations} instance
 * that provides access to Redis JSON commands like JSONGET, JSONSET, ARRAPPEND, etc.
 * This allows for dependency injection of JSON operations in a clean, testable way.</p>
 * 
 * @since 1.0
 * @see JSONOperations
 * @see BaseAbstractAction
 */
public interface TakesJSONOperations {
  /**
   * Sets the JSON operations instance that this component will use.
   * This method is typically called during initialization to provide
   * the component with access to Redis JSON operations.
   * 
   * @param json the JSON operations instance to use for Redis JSON commands
   */
  void setJSONOperations(JSONOperations<String> json);
}
