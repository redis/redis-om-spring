package com.redis.om.spring.indexing;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Thread-local context for dynamic index resolution.
 * Provides tenant, environment, and custom attribute information
 * that can be used to determine index names and key prefixes at runtime.
 *
 * <p>This class uses ThreadLocal storage to maintain context isolation
 * between different threads, making it suitable for multi-tenant applications
 * where different requests may need different index configurations.
 *
 * @since 1.0.0
 */
public class RedisIndexContext {
  private final String tenantId;
  private final String environment;
  private final Map<String, Object> attributes;

  private static final ThreadLocal<RedisIndexContext> CONTEXT_HOLDER = new ThreadLocal<>();

  private RedisIndexContext(Builder builder) {
    this.tenantId = builder.tenantId;
    this.environment = builder.environment;
    this.attributes = new HashMap<>(builder.attributes);
  }

  /**
   * Gets the current thread's index context.
   *
   * @return the current context, or null if none is set
   */
  public static RedisIndexContext getContext() {
    return CONTEXT_HOLDER.get();
  }

  /**
   * Sets the index context for the current thread.
   *
   * @param context the context to set
   */
  public static void setContext(RedisIndexContext context) {
    CONTEXT_HOLDER.set(context);
  }

  /**
   * Clears the index context for the current thread.
   */
  public static void clearContext() {
    CONTEXT_HOLDER.remove();
  }

  /**
   * Gets the tenant identifier.
   *
   * @return the tenant ID, or null if not set
   */
  public String getTenantId() {
    return tenantId;
  }

  /**
   * Gets the environment name.
   *
   * @return the environment name, or null if not set
   */
  public String getEnvironment() {
    return environment;
  }

  /**
   * Gets a specific attribute value.
   *
   * @param key the attribute key
   * @return the attribute value, or null if not present
   */
  public Object getAttribute(String key) {
    return attributes.get(key);
  }

  /**
   * Sets an attribute value.
   *
   * @param key   the attribute key
   * @param value the attribute value
   */
  public void setAttribute(String key, Object value) {
    attributes.put(key, value);
  }

  /**
   * Gets all attributes.
   *
   * @return a copy of the attributes map
   */
  public Map<String, Object> getAttributes() {
    return new HashMap<>(attributes);
  }

  /**
   * Runs a Runnable with this context set as the current context.
   * The context is automatically cleared after execution.
   *
   * @param runnable the runnable to execute
   */
  public void runWithContext(Runnable runnable) {
    RedisIndexContext previousContext = CONTEXT_HOLDER.get();
    try {
      CONTEXT_HOLDER.set(this);
      runnable.run();
    } finally {
      if (previousContext != null) {
        CONTEXT_HOLDER.set(previousContext);
      } else {
        CONTEXT_HOLDER.remove();
      }
    }
  }

  /**
   * Calls a Callable with this context set as the current context.
   * The context is automatically cleared after execution.
   *
   * @param callable the callable to execute
   * @param <T>      the return type
   * @return the result of the callable
   * @throws Exception if the callable throws an exception
   */
  public <T> T callWithContext(Callable<T> callable) throws Exception {
    RedisIndexContext previousContext = CONTEXT_HOLDER.get();
    try {
      CONTEXT_HOLDER.set(this);
      return callable.call();
    } finally {
      if (previousContext != null) {
        CONTEXT_HOLDER.set(previousContext);
      } else {
        CONTEXT_HOLDER.remove();
      }
    }
  }

  /**
   * Creates a new builder for RedisIndexContext.
   *
   * @return a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for RedisIndexContext.
   */
  public static class Builder {
    private String tenantId;
    private String environment;
    private Map<String, Object> attributes = new HashMap<>();

    private Builder() {
    }

    /**
     * Sets the tenant identifier.
     *
     * @param tenantId the tenant ID
     * @return this builder
     */
    public Builder tenantId(String tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    /**
     * Sets the environment name.
     *
     * @param environment the environment name
     * @return this builder
     */
    public Builder environment(String environment) {
      this.environment = environment;
      return this;
    }

    /**
     * Sets all attributes.
     *
     * @param attributes the attributes map
     * @return this builder
     */
    public Builder attributes(Map<String, Object> attributes) {
      if (attributes != null) {
        this.attributes = new HashMap<>(attributes);
      }
      return this;
    }

    /**
     * Sets a single attribute.
     *
     * @param key   the attribute key
     * @param value the attribute value
     * @return this builder
     */
    public Builder setAttribute(String key, Object value) {
      this.attributes.put(key, value);
      return this;
    }

    /**
     * Builds the RedisIndexContext.
     *
     * @return the built context
     */
    public RedisIndexContext build() {
      return new RedisIndexContext(this);
    }
  }
}