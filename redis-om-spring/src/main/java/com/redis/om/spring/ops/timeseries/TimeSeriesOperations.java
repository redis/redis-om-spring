package com.redis.om.spring.ops.timeseries;

import java.util.List;
import java.util.Map;

import com.redislabs.redistimeseries.Aggregation;
import com.redislabs.redistimeseries.DuplicatePolicy;
import com.redislabs.redistimeseries.Measurement;
import com.redislabs.redistimeseries.Range;
import com.redislabs.redistimeseries.Value;
import com.redislabs.redistimeseries.information.Info;

public interface TimeSeriesOperations<K> {
  boolean create(K key);

  boolean create(K key, long retentionTime);

  boolean create(K key, Map<String, String> labels);

  boolean create(K key, long retentionTime, Map<String, String> labels);

  boolean create(K key, long retentionTime, boolean uncompressed, Map<String, String> labels);

  boolean create(K key, long retentionTime, boolean uncompressed, long chunkSize, DuplicatePolicy duplicatePolicy,
      Map<String, String> labels);

  boolean alter(K key, Map<String, String> labels);

  boolean alter(K key, long retentionTime, Map<String, String> labels);

  boolean createRule(K sourceKey, Aggregation aggregation, long bucketSize, K destKey);

  boolean deleteRule(K sourceKey, K destKey);

  long add(K sourceKey, double value);

  long add(K sourceKey, long timestamp, double value);

  long add(K sourceKey, long timestamp, double value, long retentionTime);

  long add(K sourceKey, double value, long retentionTime);

  long add(K sourceKey, long timestamp, double value, Map<String, String> labels);

  long add(K sourceKey, long timestamp, double value, long retentionTime, Map<String, String> labels);

  long add(K sourceKey, long timestamp, double value, long retentionTime, boolean uncompressed,
      Map<String, String> labels);

  long add(K sourceKey, long timestamp, double value, long retentionTime, boolean uncompressed, long chunkSize,
      DuplicatePolicy duplicatePolicy, Map<String, String> labels);

  List<Object> madd(Measurement... measurements);

  Value[] range(K key, long from, long to);

  Value[] range(K key, long from, long to, int count);

  Value[] range(K key, long from, long to, Aggregation aggregation, long timeBucket);

  Value[] range(K key, long from, long to, Aggregation aggregation, long timeBucket, int count);

  Value[] revrange(K key, long from, long to);

  Value[] revrange(K key, long from, long to, int count);

  Value[] revrange(K key, long from, long to, Aggregation aggregation, long timeBucket);

  Value[] revrange(K key, long from, long to, Aggregation aggregation, long timeBucket, int count);

  Range[] mrange(long from, long to, String... filters);

  Range[] mrange(long from, long to, int count, String... filters);

  Range[] mrange(long from, long to, Aggregation aggregation, long timeBucket, String... filters);

  Range[] mrange(long from, long to, Aggregation aggregation, long timeBucket, boolean withLabels, String... filters);

  Range[] mrange(long from, long to, Aggregation aggregation, long timeBucket, boolean withLabels, int count,
      String... filters);

  Range[] mrevrange(long from, long to, String... filters);

  Range[] mrevrange(long from, long to, int count, String... filters);

  Range[] mrevrange(long from, long to, Aggregation aggregation, long timeBucket, String... filters);

  Range[] mrevrange(long from, long to, Aggregation aggregation, long timeBucket, boolean withLabels,
      String... filters);

  Range[] mrevrange(long from, long to, Aggregation aggregation, long timeBucket, boolean withLabels, int count,
      String... filters);

  Value get(K key);

  Range[] mget(boolean withLabels, String... filters);

  long incrBy(K key, int value);

  long incrBy(K key, int value, long timestamp);

  long decrBy(K key, int value);

  long decrBy(K key, int value, long timestamp);

  String[] queryIndex(String... filters);

  Info info(K key);

}
