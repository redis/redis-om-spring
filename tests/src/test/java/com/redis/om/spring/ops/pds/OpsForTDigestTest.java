package com.redis.om.spring.ops.pds;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.RedisModulesOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OpsForTDigestTest extends AbstractBaseDocumentTest {
  @Autowired
  RedisModulesOperations<String> modulesOperations;

  TDigestOperations<String> tdigest;

  @BeforeEach
  void beforeEach() {
    tdigest = modulesOperations.opsForTDigest();
  }

  @Test
  void testBasicOperations() {
    // Create a T-Digest sketch
    String result = tdigest.create("tdigestTest");
    assertEquals("OK", result);

    // Add some values
    result = tdigest.add("tdigestTest", 1.0, 2.0, 3.0, 4.0, 5.0);
    assertEquals("OK", result);

    // Get min and max
    double min = tdigest.min("tdigestTest");
    double max = tdigest.max("tdigestTest");
    assertEquals(1.0, min);
    assertEquals(5.0, max);

    // Get quantiles
    List<Double> quantiles = tdigest.quantile("tdigestTest", 0.25, 0.5, 0.75);
    assertEquals(3, quantiles.size());
    assertThat(quantiles.get(0)).isEqualTo(2.0);
    assertThat(quantiles.get(1)).isEqualTo(3.0);
    assertThat(quantiles.get(2)).isEqualTo(4.0);

    // Get CDF
    List<Double> cdf = tdigest.cdf("tdigestTest", 2.0, 4.0);
    assertEquals(2, cdf.size());
    assertThat(cdf.get(0)).isEqualTo(0.3);
    assertThat(cdf.get(1)).isEqualTo(0.7);

    // Get ranks
    List<Long> ranks = tdigest.rank("tdigestTest", 2.0, 4.0);
    assertEquals(2, ranks.size());
    assertThat(ranks.get(0)).isEqualTo(1L);
    assertThat(ranks.get(1)).isEqualTo(3L);

    // Get values by rank
    List<Double> values = tdigest.byRank("tdigestTest", 0, 2, 4);
    assertEquals(3, values.size());
    assertThat(values.get(0)).isEqualTo(1.0);
    assertThat(values.get(1)).isEqualTo(3.0);
    assertThat(values.get(2)).isEqualTo(5.0);

    // Get trimmed mean
    double trimmedMean = tdigest.trimmedMean("tdigestTest", 0.1, 0.9);
    assertThat(trimmedMean).isEqualTo(3.0);

    // Clean up after the test
    template.delete("tdigestTest");
  }

  @Test
  void testMerge() {
    // Create two T-Digest sketches
    tdigest.create("tdigest1");
    tdigest.create("tdigest2");

    // Add values to both sketches
    tdigest.add("tdigest1", 1.0, 2.0, 3.0);
    tdigest.add("tdigest2", 4.0, 5.0, 6.0);

    // Merge them
    String result = tdigest.merge("tdigestMerged", "tdigest1", "tdigest2");
    assertEquals("OK", result);

    // Verify merged sketch
    double min = tdigest.min("tdigestMerged");
    double max = tdigest.max("tdigestMerged");
    assertEquals(1.0, min);
    assertEquals(6.0, max);

    // Clean up after the test
    template.delete("tdigest1");
    template.delete("tdigest2");
    template.delete("tdigestMerged");
  }

  @Test
  void testInfo() {
    // Create a T-Digest sketch with custom compression
    tdigest.create("tdigestInfoTest", 100);
    
    // Add some values
    tdigest.add("tdigestInfoTest", 1.0, 2.0, 3.0);
    
    // Get info
    Map<String, Object> info = tdigest.info("tdigestInfoTest");
    
    // Verify info contains expected keys
    assertNotNull(info);
    assertFalse(info.isEmpty());
    assertEquals(100L, info.get("Compression"));
    
    // Clean up after the test
    template.delete("tdigestInfoTest");
  }

  @Test
  void testNonExistingKey() {
    // Attempt to get info for a non-existing key
    JedisDataException exception = assertThrows(JedisDataException.class, 
        () -> tdigest.info("nonExistingKey"));
    
    // Verify error message
    assertEquals("ERR T-Digest: key does not exist", exception.getMessage());
  }

  @Test
  void testReset() {
    // Create a T-Digest sketch
    tdigest.create("tdigestResetTest");
    
    // Add some values
    tdigest.add("tdigestResetTest", 1.0, 2.0, 3.0);
    
    // Reset the sketch
    String result = tdigest.reset("tdigestResetTest");
    assertEquals("OK", result);
    
    // Verify sketch is empty
    Map<String, Object> info = tdigest.info("tdigestResetTest");
    assertEquals(0L, info.get("Merged nodes"));
    assertEquals(0L, info.get("Unmerged nodes"));
    
    // Clean up after the test
    template.delete("tdigestResetTest");
  }
} 