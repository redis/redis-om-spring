package com.redis.om.spring.ops.pds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.RedisModulesOperations;

import io.rebloom.client.InsertOptions;
import redis.clients.jedis.exceptions.JedisDataException;

class OpsForBloomTest extends AbstractBaseDocumentTest {
  @Autowired
  RedisModulesOperations<String> modulesOperations;

  BloomOperations<String> bloom;

  @BeforeEach
  void beforeEach() {
    bloom = modulesOperations.opsForBloom();
  }

  @Test
  void testExample() {
    // Simple bloom filter using default module settings
    bloom.add("simpleBloom", "Mark");
    // Does "Mark" now exist?
    bloom.exists("simpleBloom", "Mark"); // true
    bloom.exists("simpleBloom", "Farnsworth"); // False

    // If you have a long list of items to check/add, you can use the
    // "multi" methods

    bloom.addMulti("simpleBloom", "foo", "bar", "baz", "bat", "bag");

    // Check if they exist:
    boolean[] rv = bloom.existsMulti("simpleBloom", "foo", "bar", "baz", "bat", "Mark", "nonexist");
    // All items except the last one will be 'true'
    assertThat(Arrays.toString(new boolean[] { true, true, true, true, true, false })).isEqualTo(Arrays.toString(rv));

    bloom.delete("simpleBloom");
  }

  @Test
  void testExampleBytesApi() {
    // Simple bloom filter using default module settings
    bloom.add("simpleBloom", "Mark".getBytes());
    // Does "Mark" now exist?
    bloom.exists("simpleBloom", "Mark".getBytes()); // true
    bloom.exists("simpleBloom", "Farnsworth".getBytes()); // False

    // If you have a long list of items to check/add, you can use the
    // "multi" methods

    bloom.addMulti("simpleBloom", "foo".getBytes(), "bar".getBytes(), "baz".getBytes(), "bat".getBytes(),
        "bag".getBytes());

    // Check if they exist:
    boolean[] rv = bloom.existsMulti("simpleBloom", "foo".getBytes(), "bar".getBytes(), "baz".getBytes(),
        "bat".getBytes(), "Mark".getBytes(), "nonexist".getBytes());
    // All items except the last one will be 'true'
    assertThat(Arrays.toString(new boolean[] { true, true, true, true, true, false })).isEqualTo(Arrays.toString(rv));

    bloom.delete("simpleBloom");
  }

  @Test
  void reserveExpansionNoCreate() {
    JedisDataException exception = Assertions.assertThrows(JedisDataException.class, () -> {
      bloom.insert("bfexpansion", InsertOptions.insertOptions().nocreate(), "a", "b", "c");
    });

    Assertions.assertEquals("ERR not found", exception.getMessage());
    bloom.delete("bfexpansion");
  }

  @Test
  void reserveExpansion() {
    assertThat(bloom.insert("bfexpansion2", InsertOptions.insertOptions().capacity(1000), "a", "b", "c"))
        .isEqualTo(new boolean[] { true, true, true });
    bloom.delete("bfexpansion2");
  }

  @Test
  void testInfo() {
    bloom.insert("test_info", new InsertOptions().capacity(1L), "1");
    Map<String, Object> info = bloom.info("test_info");
    assertEquals("1", info.get("Number of items inserted").toString());

    // returning an error if the filter does not already exist
    JedisDataException exception = Assertions.assertThrows(JedisDataException.class, () -> {
      bloom.info("not_exist");
    });

    assertEquals("ERR not found", exception.getMessage());
  }

}