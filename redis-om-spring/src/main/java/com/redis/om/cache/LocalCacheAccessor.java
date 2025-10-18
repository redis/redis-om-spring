package com.redis.om.cache;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hrakaroo.glob.GlobPattern;
import com.hrakaroo.glob.MatchingEngine;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * A {@link CacheAccessor} implementation that provides a local in-memory cache in front of another
 * {@link CacheAccessor} delegate. This implementation helps reduce network calls by caching values locally.
 * It also provides metrics for cache hits, misses, and evictions.
 */
public class LocalCacheAccessor implements CacheAccessor {

  private static final String DESCRIPTION_GETS = "The number of times local cache lookup methods have returned a cached (hit) or uncached (newly loaded or null) value (miss).";
  private static final String DESCRIPTION_EVICTIONS = "The number of times the local cache was evicted";

  private final Map<String, Object> map;
  private final CacheAccessor delegate;
  private final Counter hits;
  private final Counter misses;
  private final Counter evictions;

  /**
   * Creates a new LocalCacheAccessor with the specified cache map, delegate, name, and registry.
   *
   * @param cache    the map to use for local caching
   * @param delegate the underlying CacheAccessor to delegate calls to
   * @param name     the name of the cache, used for metrics
   * @param registry the meter registry for recording metrics
   */
  public LocalCacheAccessor(Map<String, Object> cache, CacheAccessor delegate, String name, MeterRegistry registry) {
    this.map = cache;
    this.delegate = delegate;
    this.hits = Counter.builder("cache.local.gets").tags("name", name).tag("result", "hit").description(
        DESCRIPTION_GETS).register(registry);
    this.misses = Counter.builder("cache.local.gets").tag("name", name).tag("result", "miss").description(
        DESCRIPTION_GETS).register(registry);
    this.evictions = Counter.builder("cache.local.evictions").tag("name", name).description(DESCRIPTION_EVICTIONS)
        .register(registry);
  }

  /**
   * Returns the map used for local caching.
   *
   * @return the map containing locally cached values
   */
  public Map<String, Object> getMap() {
    return map;
  }

  /**
   * Returns the delegate CacheAccessor that this LocalCacheAccessor wraps.
   *
   * @return the underlying CacheAccessor delegate
   */
  public CacheAccessor getDelegate() {
    return delegate;
  }

  @Override
  public Object get(String key, Duration ttl) {
    Object value = map.get(key);
    if (value == null) {
      misses.increment();
      value = delegate.get(key, ttl);
      if (value != null) {
        map.put(key, value);
      }
    } else {
      hits.increment();
    }
    return value;
  }

  @Override
  public void put(String key, Object value, Duration ttl) {
    map.put(key, value);
    delegate.put(key, value, ttl);
    // Register interest in key
    delegate.get(key, ttl);
  }

  @Override
  public Object putIfAbsent(String key, Object value, Duration ttl) {
    if (!map.containsKey(key)) {
      map.put(key, value);
    }
    Object result = delegate.putIfAbsent(key, value, ttl);
    // Register interest in key
    delegate.get(key, ttl);
    return result;
  }

  @Override
  public void remove(String key) {
    delegate.remove(key);
    map.remove(key);
    evictions.increment();
  }

  /**
   * Removes an entry from the local cache without affecting the delegate cache.
   * This is useful for selectively invalidating local cache entries.
   *
   * @param key the key to remove from the local cache
   */
  public void evictLocal(String key) {
    map.remove(key);
    evictions.increment();
  }

  @Override
  public long clean(String pattern) {
    MatchingEngine engine = GlobPattern.compile(pattern);
    List<String> keys = map.keySet().stream().filter(engine::matches).collect(Collectors.toList());
    keys.forEach(map::remove);
    evictions.increment(keys.size());
    return delegate.clean(pattern);
  }

}
