package com.redis.om.spring.ops;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.json.JSONOperationsImpl;
import com.redis.om.spring.ops.pds.*;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.ops.search.SearchOperationsImpl;

/**
 * A record that provides centralized access to Redis module operations.
 * <p>
 * This record acts as a factory for creating operation interfaces that interact with various Redis modules
 * including RedisJSON, RediSearch, and probabilistic data structures like Bloom filters, Count-Min sketches,
 * Cuckoo filters, TopK, and T-Digest.
 * </p>
 * <p>
 * The record encapsulates the necessary dependencies (client, template, and JSON serialization configuration)
 * and provides typed access to module-specific operations through the {@code opsFor*} methods.
 * </p>
 *
 * @param <K>         the type of keys used in Redis operations
 * @param client      the Redis modules client for executing commands
 * @param template    the Spring Data Redis template for additional Redis operations
 * @param gsonBuilder the Gson builder for JSON serialization/deserialization configuration
 * @param commandListener A command listener for monitoring Redis commands
 *
 * @author Redis OM Spring Team
 * @see JSONOperations
 * @see SearchOperations
 * @see BloomOperations
 * @see CountMinSketchOperations
 * @see CuckooFilterOperations
 * @see TopKOperations
 * @see TDigestOperations
 */
public record RedisModulesOperations<K>(RedisModulesClient client, StringRedisTemplate template,
                                        GsonBuilder gsonBuilder, CommandListener commandListener) {

  /**
   * Creates and returns operations for interacting with RedisJSON module.
   * <p>
   * RedisJSON operations allow storing, retrieving, and manipulating JSON documents
   * directly in Redis with path-based access to JSON elements. The operations
   * automatically participate in Redis transactions when executed within a transaction context.
   * </p>
   *
   * @return a {@link JSONOperations} instance for JSON document operations
   */
  public JSONOperations<K> opsForJSON() {
    // Pass the template to enable transaction support
    return new JSONOperationsImpl<>(client, gsonBuilder, template);
  }

  /**
   * Creates and returns operations for interacting with RediSearch module.
   * <p>
   * RediSearch operations provide full-text search, secondary indexing, and complex
   * query capabilities over Redis data structures.
   * </p>
   *
   * @param index the name of the search index to operate on
   * @return a {@link SearchOperations} instance for search and indexing operations
   */
  public SearchOperations<K> opsForSearch(K index) {
    return new SearchOperationsImpl<>(index, client, template, commandListener);
  }

  /**
   * Creates and returns operations for interacting with Redis Bloom filters.
   * <p>
   * Bloom filter operations provide probabilistic data structure capabilities
   * for membership testing with configurable false positive rates.
   * </p>
   *
   * @return a {@link BloomOperations} instance for Bloom filter operations
   */
  public BloomOperations<K> opsForBloom() {
    return new BloomOperationsImpl<>(client);
  }

  /**
   * Creates and returns operations for interacting with Redis Count-Min sketches.
   * <p>
   * Count-Min sketch operations provide probabilistic data structure capabilities
   * for frequency estimation of elements in data streams.
   * </p>
   *
   * @return a {@link CountMinSketchOperations} instance for Count-Min sketch operations
   */
  public CountMinSketchOperations<K> opsForCountMinSketch() {
    return new CountMinSketchOperationsImpl<>(client);
  }

  /**
   * Creates and returns operations for interacting with Redis Cuckoo filters.
   * <p>
   * Cuckoo filter operations provide probabilistic data structure capabilities
   * for membership testing with support for deletion operations.
   * </p>
   *
   * @return a {@link CuckooFilterOperations} instance for Cuckoo filter operations
   */
  public CuckooFilterOperations<K> opsForCuckoFilter() {
    return new CuckooFilterOperationsImpl<>(client);
  }

  /**
   * Creates and returns operations for interacting with Redis TopK data structures.
   * <p>
   * TopK operations provide probabilistic data structure capabilities
   * for maintaining a list of the top K most frequent items.
   * </p>
   *
   * @return a {@link TopKOperations} instance for TopK operations
   */
  public TopKOperations<K> opsForTopK() {
    return new TopKOperationsImpl<>(client);
  }

  /**
   * Creates and returns operations for interacting with Redis T-Digest data structures.
   * <p>
   * T-Digest operations provide probabilistic data structure capabilities
   * for estimating quantiles and percentiles from streaming data.
   * </p>
   *
   * @return a {@link TDigestOperations} instance for T-Digest operations
   */
  public TDigestOperations<K> opsForTDigest() {
    return new TDigestOperationsImpl<>(client);
  }
}
