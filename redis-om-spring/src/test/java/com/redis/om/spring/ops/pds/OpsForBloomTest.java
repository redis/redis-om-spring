package com.redis.om.spring.ops.pds;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.RedisModulesOperations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.bloom.BFInsertParams;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection") class OpsForBloomTest extends AbstractBaseDocumentTest {
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
    List<Boolean> rv = bloom.existsMulti("simpleBloom", "foo", "bar", "baz", "bat", "Mark", "nonexist");
    // All items except the last one will be 'true'
    assertThat(List.of(true, true, true, true, true, false)).isEqualTo(rv);

    template.delete("simpleBloom");
  }

  @Test
  void testExampleBytesApi() {
    // Simple bloom filter using default module settings
    bloom.add("simpleBloom", "Mark");
    // Does "Mark" now exist?
    bloom.exists("simpleBloom", "Mark"); // true
    bloom.exists("simpleBloom", "Farnsworth"); // False

    // If you have a long list of items to check/add, you can use the
    // "multi" methods

    bloom.addMulti("simpleBloom", "foo", "bar", "baz", "bat",
        "bag");

    // Check if they exist:
    List<Boolean> rv = bloom.existsMulti("simpleBloom", "foo", "bar", "baz",
        "bat", "Mark", "nonexist");
    // All items except the last one will be 'true'
    assertThat(List.of(true, true, true, true, true, false)).isEqualTo(rv);

    template.delete("simpleBloom");
  }

  @Test
  void reserveExpansionNoCreate() {
    JedisDataException exception = Assertions.assertThrows(JedisDataException.class, () -> bloom.insert("bfexpansion", BFInsertParams.insertParams().noCreate(), "a", "b", "c"));

    Assertions.assertEquals("ERR not found", exception.getMessage());
    template.delete("bfexpansion");
  }

  @Test
  void reserveExpansion() {
    assertThat(bloom.insert("bfexpansion2", BFInsertParams.insertParams().capacity(1000), "a", "b", "c"))
        .isEqualTo(List.of(true, true, true));
    template.delete("bfexpansion2");
  }

  @Test
  void testInfo() {
    bloom.insert("test_info", BFInsertParams.insertParams().capacity(1L), "1");
    Map<String, Object> info = bloom.info("test_info");
    assertEquals("1", info.get("Number of items inserted").toString());

    // returning an error if the filter does not already exist
    JedisDataException exception = Assertions.assertThrows(JedisDataException.class, () -> bloom.info("not_exist"));

    assertEquals("ERR not found", exception.getMessage());
  }

}