package com.redis.om.spring.ops.timeseries;

import java.util.List;
import java.util.Map;

import com.redis.om.spring.client.RedisModulesClient;
import com.redislabs.redistimeseries.Aggregation;
import com.redislabs.redistimeseries.DuplicatePolicy;
import com.redislabs.redistimeseries.Measurement;
import com.redislabs.redistimeseries.Range;
import com.redislabs.redistimeseries.Value;
import com.redislabs.redistimeseries.information.Info;

public class TimeSeriesOperationsImpl<K> implements TimeSeriesOperations<K> {
  
  RedisModulesClient client;

  public TimeSeriesOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }

  @Override
  public boolean create(K key) {
    return client.clientForTimeSeries().create(key.toString());
  }

  @Override
  public boolean create(K key, long retentionTime) {
    return client.clientForTimeSeries().create(key.toString(), retentionTime);
  }

  @Override
  public boolean create(K key, Map<String, String> labels) {
    return client.clientForTimeSeries().create(key.toString(), labels);
  }

  @Override
  public boolean create(K key, long retentionTime, Map<String, String> labels) {
    return client.clientForTimeSeries().create(key.toString(), retentionTime, labels);
  }

  @Override
  public boolean create(K key, long retentionTime, boolean uncompressed, Map<String, String> labels) {
    return client.clientForTimeSeries().create(key.toString(), retentionTime, uncompressed, labels);
  }

  @Override
  public boolean create(K key, long retentionTime, boolean uncompressed, long chunkSize,
      DuplicatePolicy duplicatePolicy, Map<String, String> labels) {
    return client.clientForTimeSeries().create(key.toString(), retentionTime, uncompressed, chunkSize, duplicatePolicy, labels);
  }

  @Override
  public boolean alter(K key, Map<String, String> labels) {
    return client.clientForTimeSeries().alter(key.toString(), labels);
  }

  @Override
  public boolean alter(K key, long retentionTime, Map<String, String> labels) {
    return client.clientForTimeSeries().alter(key.toString(), retentionTime, labels);
  }

  @Override
  public boolean createRule(K sourceKey, Aggregation aggregation, long bucketSize, K destKey) {
    return client.clientForTimeSeries().createRule(sourceKey.toString(), aggregation, bucketSize, destKey.toString());
  }

  @Override
  public boolean deleteRule(K sourceKey, K destKey) {
    return client.clientForTimeSeries().deleteRule(sourceKey.toString(), destKey.toString());
  }

  @Override
  public long add(K sourceKey, double value) {
    return client.clientForTimeSeries().add(sourceKey.toString(), value);
  }

  @Override
  public long add(K sourceKey, long timestamp, double value) {
    return client.clientForTimeSeries().add(sourceKey.toString(), timestamp, value);
  }

  @Override
  public long add(K sourceKey, long timestamp, double value, long retentionTime) {
    return client.clientForTimeSeries().add(sourceKey.toString(), timestamp, value, retentionTime);
  }

  @Override
  public long add(K sourceKey, double value, long retentionTime) {
    return client.clientForTimeSeries().add(sourceKey.toString(), value, retentionTime);
  }

  @Override
  public long add(K sourceKey, long timestamp, double value, Map<String, String> labels) {
    return client.clientForTimeSeries().add(sourceKey.toString(), timestamp, value, labels);
  }

  @Override
  public long add(K sourceKey, long timestamp, double value, long retentionTime, Map<String, String> labels) {
    return client.clientForTimeSeries().add(sourceKey.toString(), timestamp, value, retentionTime, labels);
  }

  @Override
  public long add(K sourceKey, long timestamp, double value, long retentionTime, boolean uncompressed,
      Map<String, String> labels) {
    return client.clientForTimeSeries().add(sourceKey.toString(), timestamp, value, retentionTime, uncompressed, labels);
  }

  @Override
  public long add(K sourceKey, long timestamp, double value, long retentionTime, boolean uncompressed, long chunkSize,
      DuplicatePolicy duplicatePolicy, Map<String, String> labels) {
    return client.clientForTimeSeries().add(sourceKey.toString(), timestamp, value, retentionTime, uncompressed, chunkSize, duplicatePolicy, labels);
  }

  @Override
  public List<Object> madd(Measurement... measurements) {
    return client.clientForTimeSeries().madd(measurements);
  }

  @Override
  public Value[] range(K key, long from, long to) {
    return client.clientForTimeSeries().range(key.toString(), from, to);
  }

  @Override
  public Value[] range(K key, long from, long to, int count) {
    return client.clientForTimeSeries().range(key.toString(), from, to, count);
  }

  @Override
  public Value[] range(K key, long from, long to, Aggregation aggregation, long timeBucket) {
    return client.clientForTimeSeries().range(key.toString(), from, to, aggregation, timeBucket);
  }

  @Override
  public Value[] range(K key, long from, long to, Aggregation aggregation, long timeBucket, int count) {
    return client.clientForTimeSeries().range(key.toString(), from, to, aggregation, timeBucket, count);
  }

  @Override
  public Value[] revrange(K key, long from, long to) {
    return client.clientForTimeSeries().revrange(key.toString(), from, to);
  }

  @Override
  public Value[] revrange(K key, long from, long to, int count) {
    return client.clientForTimeSeries().revrange(key.toString(), from, to, count);
  }

  @Override
  public Value[] revrange(K key, long from, long to, Aggregation aggregation, long timeBucket) {
    return client.clientForTimeSeries().revrange(key.toString(), from, to, aggregation, timeBucket);
  }

  @Override
  public Value[] revrange(K key, long from, long to, Aggregation aggregation, long timeBucket, int count) {
    return client.clientForTimeSeries().revrange(key.toString(), from, to, aggregation, timeBucket, count);
  }

  @Override
  public Range[] mrange(long from, long to, String... filters) {
    return client.clientForTimeSeries().mrange(from, to, filters);
  }

  @Override
  public Range[] mrange(long from, long to, int count, String... filters) {
    return client.clientForTimeSeries().mrange(from, to, count, filters);
  }

  @Override
  public Range[] mrange(long from, long to, Aggregation aggregation, long timeBucket, String... filters) {
    return client.clientForTimeSeries().mrange(from, to, aggregation, timeBucket, filters);
  }

  @Override
  public Range[] mrange(long from, long to, Aggregation aggregation, long timeBucket, boolean withLabels,
      String... filters) {
    return client.clientForTimeSeries().mrange(from, to, aggregation, timeBucket, withLabels, filters);
  }

  @Override
  public Range[] mrange(long from, long to, Aggregation aggregation, long timeBucket, boolean withLabels, int count,
      String... filters) {
    return client.clientForTimeSeries().mrange(from, to, aggregation, timeBucket, withLabels, count, filters);
  }

  @Override
  public Range[] mrevrange(long from, long to, String... filters) {
    return client.clientForTimeSeries().mrevrange(from, to, filters);
  }

  @Override
  public Range[] mrevrange(long from, long to, int count, String... filters) {
    return client.clientForTimeSeries().mrevrange(from, to, count, filters);
  }

  @Override
  public Range[] mrevrange(long from, long to, Aggregation aggregation, long timeBucket, String... filters) {
    return client.clientForTimeSeries().mrevrange(from, to, aggregation, timeBucket, filters);
  }

  @Override
  public Range[] mrevrange(long from, long to, Aggregation aggregation, long timeBucket, boolean withLabels,
      String... filters) {
    return client.clientForTimeSeries().mrevrange(from, to, aggregation, timeBucket, withLabels, filters);
  }

  @Override
  public Range[] mrevrange(long from, long to, Aggregation aggregation, long timeBucket, boolean withLabels, int count,
      String... filters) {
    return client.clientForTimeSeries().mrevrange(from, to, aggregation, timeBucket, withLabels, count, filters);
  }

  @Override
  public Value get(K key) {
    return client.clientForTimeSeries().get(key.toString());
  }

  @Override
  public Range[] mget(boolean withLabels, String... filters) {
    return client.clientForTimeSeries().mget(withLabels, filters);
  }

  @Override
  public long incrBy(K key, int value) {
    return client.clientForTimeSeries().incrBy(key.toString(), value);
  }

  @Override
  public long incrBy(K key, int value, long timestamp) {
    return client.clientForTimeSeries().incrBy(key.toString(), value, timestamp);
  }

  @Override
  public long decrBy(K key, int value) {
    return client.clientForTimeSeries().decrBy(key.toString(), value);
  }

  @Override
  public long decrBy(K key, int value, long timestamp) {
    return client.clientForTimeSeries().decrBy(key.toString(), value, timestamp);
  }

  @Override
  public String[] queryIndex(String... filters) {
    return client.clientForTimeSeries().queryIndex(filters);
  }

  @Override
  public Info info(K key) {
    return client.clientForTimeSeries().info(key.toString());
  }

}
