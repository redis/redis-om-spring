package com.redis.om.cache;

import java.time.Duration;

import org.springframework.util.Assert;

/**
 * Interface defining functions to determine the time-to-live (TTL) for cache entries.
 * Implementations of this interface provide strategies for calculating expiration durations
 * for cache entries based on their keys and values.
 */
public interface TtlFunction {

  /**
   * Constant representing no expiration (persistent entries).
   */
  Duration NO_EXPIRATION = Duration.ZERO;

  /**
   * {@link TtlFunction} to create persistent entires that do not expire.
   */
  TtlFunction PERSISTENT = just(NO_EXPIRATION);

  /**
   * {@link TtlFunction} implementation returning the given, predetermined
   * {@link Duration} used for per cache entry
   * {@literal time-to-live (TTL) expiration}.
   *
   */
  public static class FixedDurationTtlFunction implements TtlFunction {

    private final Duration duration;

    /**
     * Creates a new FixedDurationTtlFunction with the specified duration.
     *
     * @param duration the fixed duration to use for all cache entries
     */
    public FixedDurationTtlFunction(Duration duration) {
      this.duration = duration;
    }

    @Override
    public Duration getTtl(Object key, Object value) {
      return this.duration;
    }
  }

  /**
   * Creates a {@literal Singleton} {@link TtlFunction} using the given
   * {@link Duration}.
   *
   * @param duration the time to live. Can be {@link Duration#ZERO} for persistent
   *                 values (i.e. cache entry does not expire).
   * @return a singleton {@link TtlFunction} using {@link Duration}.
   */
  static TtlFunction just(Duration duration) {
    Assert.notNull(duration, "TTL Duration must not be null");
    return new FixedDurationTtlFunction(duration);
  }

  /**
   * Compute a {@link Duration time-to-live (TTL)} using the cache {@code key} and
   * {@code value}.
   * <p>
   * The {@link Duration time-to-live (TTL)} is computed on each write operation.
   * Redis uses millisecond granularity for timeouts. Any more granular values
   * (e.g. micros or nanos) are not considered and will be truncated due to
   * rounding. Returning {@link Duration#ZERO}, or a value less than
   * {@code Duration.ofMillis(1)}, results in a persistent value that does not
   * expire.
   *
   * @param key   the cache key.
   * @param value the cache value. Can be {@code null} if the cache supports
   *              {@code null} value caching.
   * @return the computed {@link Duration time-to-live (TTL)}. Can be
   *         {@link Duration#ZERO} for persistent values (i.e. cache entry does
   *         not expire).
   */
  Duration getTtl(Object key, Object value);

}
