package com.redis.om.spring.indexing;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
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
 * <p>Preferred usage with try-with-resources for automatic cleanup:
 * <pre>{@code
 * try (RedisIndexContext ctx = RedisIndexContext.builder()
 *         .tenantId("tenant-A")
 *         .build()
 *         .activate()) {
 *     // context is active here
 * }
 * // context is automatically restored/cleared
 * }</pre>
 *
 * @since 1.0.0
 */
public class RedisIndexContext implements AutoCloseable {
  private final String tenantId;
  private final String environment;
  private final Map<String, Object> attributes;

  private static final ThreadLocal<RedisIndexContext> CONTEXT_HOLDER = new ThreadLocal<>();
  private static final ThreadLocal<Deque<RedisIndexContext>> PREVIOUS_CONTEXT_STACK = ThreadLocal.withInitial(
      LinkedList::new);

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
   * @deprecated Use {@link #activate()} with try-with-resources, {@link #runWithContext(Runnable)},
   *             or {@link #callWithContext(Callable)} instead for automatic cleanup.
   */
  @Deprecated
  public static void setContext(RedisIndexContext context) {
    CONTEXT_HOLDER.set(context);
  }

  /**
   * Clears the index context and any saved activation history for the current thread.
   */
  public static void clearContext() {
    CONTEXT_HOLDER.remove();
    PREVIOUS_CONTEXT_STACK.remove();
  }

  /**
   * Activates this context on the current thread and returns it for use
   * with try-with-resources. When closed, the previous context is restored
   * (or the ThreadLocal is removed if there was no previous context).
   *
   * <pre>{@code
   * try (RedisIndexContext ctx = context.activate()) {
   *     // context is active here
   * }
   * // previous context is restored
   * }</pre>
   *
   * @return this context, for use in a try-with-resources statement
   */
  public RedisIndexContext activate() {
    PREVIOUS_CONTEXT_STACK.get().push(CONTEXT_HOLDER.get());
    CONTEXT_HOLDER.set(this);
    return this;
  }

  /**
   * Restores the previous context (or clears the ThreadLocal if none existed).
   * Called automatically when used with try-with-resources via {@link #activate()}.
   * This method is idempotent: a second call after the context has already been
   * deactivated is a no-op.
   */
  @Override
  public void close() {
    // Guard: only pop if this context is still the active one, making close() idempotent.
    if (CONTEXT_HOLDER.get() != this) {
      return;
    }
    Deque<RedisIndexContext> stack = PREVIOUS_CONTEXT_STACK.get();
    if (!stack.isEmpty()) {
      RedisIndexContext previous = stack.pop();
      if (previous != null) {
        CONTEXT_HOLDER.set(previous);
      } else {
        CONTEXT_HOLDER.remove();
      }
    } else {
      CONTEXT_HOLDER.remove();
    }
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