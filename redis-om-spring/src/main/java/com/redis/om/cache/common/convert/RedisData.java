package com.redis.om.cache.common.convert;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Data object holding {@link Bucket} representing the domain object to be stored in a Redis hash. Index information
 * points to additional structures holding the objects is for searching.
 *
 */
public class RedisData {

  private final Bucket bucket;
  private final Set<IndexedData> indexedData;

  private @Nullable String keyspace;
  private @Nullable String id;
  private @Nullable Long timeToLive;

  /**
   * Creates new {@link RedisData} with empty {@link Bucket}.
   */
  public RedisData() {
    this(Collections.emptyMap());
  }

  /**
   * Creates new {@link RedisData} with {@link Bucket} holding provided values.
   *
   * @param raw should not be {@literal null}.
   */
  public RedisData(Map<byte[], byte[]> raw) {
    this(Bucket.newBucketFromRawMap(raw));
  }

  /**
   * Creates new {@link RedisData} with {@link Bucket}
   *
   * @param bucket must not be {@literal null}.
   */
  public RedisData(Bucket bucket) {

    Assert.notNull(bucket, "Bucket must not be null");

    this.bucket = bucket;
    this.indexedData = new HashSet<>();
  }

  /**
   * Set the id to be used as part of the key.
   *
   * @param id the ID to set, can be {@literal null}
   */
  public void setId(@Nullable String id) {
    this.id = id;
  }

  /**
   * Get the ID used as part of the key.
   *
   * @return the ID or {@literal null} if not set
   */
  @Nullable
  public String getId() {
    return this.id;
  }

  /**
   * Get the time before expiration in seconds.
   *
   * @return {@literal null} if not set.
   */
  @Nullable
  public Long getTimeToLive() {
    return timeToLive;
  }

  /**
   * Add indexed data for additional search structures.
   *
   * @param index must not be {@literal null}.
   */
  public void addIndexedData(IndexedData index) {

    Assert.notNull(index, "IndexedData to add must not be null");
    this.indexedData.add(index);
  }

  /**
   * Add multiple indexed data entries for additional search structures.
   *
   * @param indexes must not be {@literal null}.
   */
  public void addIndexedData(Collection<IndexedData> indexes) {

    Assert.notNull(indexes, "IndexedData to add must not be null");
    this.indexedData.addAll(indexes);
  }

  /**
   * Get all indexed data entries for additional search structures.
   *
   * @return an unmodifiable set of indexed data, never {@literal null}.
   */
  public Set<IndexedData> getIndexedData() {
    return Collections.unmodifiableSet(this.indexedData);
  }

  /**
   * Get the keyspace used for storing this data in Redis.
   *
   * @return the keyspace or {@literal null} if not set
   */
  @Nullable
  public String getKeyspace() {
    return keyspace;
  }

  /**
   * Set the keyspace to be used for storing this data in Redis.
   *
   * @param keyspace the keyspace to set, can be {@literal null}
   */
  public void setKeyspace(@Nullable String keyspace) {
    this.keyspace = keyspace;
  }

  /**
   * Get the bucket containing the data to be stored in Redis.
   *
   * @return the bucket, never {@literal null}
   */
  public Bucket getBucket() {
    return bucket;
  }

  /**
   * Set the time before expiration in {@link TimeUnit#SECONDS}.
   *
   * @param timeToLive can be {@literal null}.
   */
  public void setTimeToLive(Long timeToLive) {
    this.timeToLive = timeToLive;
  }

  /**
   * Set the time before expiration converting the given arguments to {@link TimeUnit#SECONDS}.
   *
   * @param timeToLive must not be {@literal null}
   * @param timeUnit   must not be {@literal null}
   */
  public void setTimeToLive(Long timeToLive, TimeUnit timeUnit) {

    Assert.notNull(timeToLive, "TimeToLive must not be null when used with TimeUnit");
    Assert.notNull(timeUnit, "TimeUnit must not be null");

    setTimeToLive(TimeUnit.SECONDS.convert(timeToLive, timeUnit));
  }

  @Override
  public String toString() {
    return "RedisDataObject [key=" + keyspace + ":" + id + ", hash=" + bucket + "]";
  }

}
