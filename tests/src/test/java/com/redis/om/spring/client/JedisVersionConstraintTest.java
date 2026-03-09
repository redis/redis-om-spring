package com.redis.om.spring.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

/**
 * Verifies that the Jedis 7.3.0+ version constraint in build.gradle resolves correctly
 * and the classes that caused {@code ClassNotFoundException} for consumers with older Jedis
 * versions are present on the classpath.
 * <p>
 * These tests don't require a running Redis instance — they only check the classpath.
 */
class JedisVersionConstraintTest {

  @Test
  void testHybridResultClassAvailable() {
    assertDoesNotThrow(
        () -> Class.forName("redis.clients.jedis.search.hybrid.HybridResult"),
        "HybridResult should be available on the classpath (requires Jedis 7.3.0+)"
    );
  }

  @Test
  void testHybridSearchParamsAvailable() {
    assertDoesNotThrow(
        () -> Class.forName("redis.clients.jedis.search.hybrid.FTHybridParams"),
        "FTHybridParams should be available on the classpath (requires Jedis 7.3.0+)"
    );
  }

  @Test
  void testJedisVersionIsAtLeast7_3() {
    Package jedisPackage = redis.clients.jedis.Jedis.class.getPackage();
    String version = jedisPackage.getImplementationVersion();

    // If the manifest doesn't include version info, verify by class presence instead
    if (version != null) {
      String[] parts = version.split("\\.");
      int major = Integer.parseInt(parts[0]);
      int minor = Integer.parseInt(parts[1]);

      assertThat(major).as("Jedis major version").isGreaterThanOrEqualTo(7);
      if (major == 7) {
        assertThat(minor).as("Jedis minor version").isGreaterThanOrEqualTo(3);
      }
    } else {
      // Fallback: if no version in manifest, at least confirm the 7.3.0 classes exist
      assertDoesNotThrow(
          () -> Class.forName("redis.clients.jedis.search.hybrid.HybridResult"),
          "Jedis 7.3.0+ classes must be on classpath"
      );
    }
  }
}
