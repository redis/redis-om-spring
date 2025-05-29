package com.redis.om.spring.ops.pds;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.redis.om.spring.client.RedisModulesClient;

/**
 * Default implementation of {@link CountMinSketchOperations} that delegates to Redis Count-Min Sketch commands.
 * <p>
 * This implementation provides concrete Redis Count-Min Sketch operations by wrapping the underlying
 * Jedis Count-Min Sketch client. It handles the conversion of operation parameters and manages the
 * communication with Redis Stack's Count-Min Sketch module.
 * </p>
 * <p>
 * The implementation is automatically configured by Redis OM Spring when Count-Min Sketch
 * functionality is enabled and is used by the {@link com.redis.om.spring.countmin.CountMinAspect}
 * for transparent frequency tracking during entity operations.
 * </p>
 *
 * @param <K> the type of keys used to identify count-min sketches
 * @see CountMinSketchOperations
 * @see com.redis.om.spring.client.RedisModulesClient
 * @since 0.1.0
 */
public class CountMinSketchOperationsImpl<K> implements CountMinSketchOperations<K> {
  final RedisModulesClient client;

  /**
   * Creates a new CountMinSketchOperationsImpl with the specified Redis modules client.
   *
   * @param client the Redis modules client for executing Count-Min Sketch commands
   */
  public CountMinSketchOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }

  @Override
  public void cmsInitByDim(K key, long width, long depth) {
    client.clientForCMS().cmsInitByDim(key.toString(), width, depth);
  }

  @Override
  public void cmsInitByProb(K key, double error, double probability) {
    client.clientForCMS().cmsInitByProb(key.toString(), error, probability);
  }

  @Override
  public long cmsIncrBy(K key, String item, long increment) {
    return client.clientForCMS().cmsIncrBy(key.toString(), item, increment);
  }

  @Override
  public List<Long> cmsIncrBy(K key, Map<String, Long> itemIncrements) {
    return client.clientForCMS().cmsIncrBy(key.toString(), itemIncrements);
  }

  @Override
  public List<Long> cmsQuery(K key, String... items) {
    return client.clientForCMS().cmsQuery(key.toString(), items);
  }

  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public void cmsMerge(K destKey, K... keys) {
    client.clientForCMS().cmsMerge( //
        destKey.toString(), //
        Arrays.stream(keys).map(Object::toString).toArray(String[]::new));
  }

  @Override
  public void cmsMerge(K destKey, Map<K, Long> keysAndWeights) {
    client.clientForCMS().cmsMerge( //
        destKey.toString(), //
        keysAndWeights //
            .entrySet() //
            .stream() //
            .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue)));
  }

  @Override
  public Map<String, Object> cmsInfo(K key) {
    return client.clientForCMS().cmsInfo(key.toString());
  }
}
