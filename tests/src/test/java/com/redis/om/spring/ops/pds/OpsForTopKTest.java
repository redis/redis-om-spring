package com.redis.om.spring.ops.pds;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.RedisModulesOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OpsForTopKTest extends AbstractBaseDocumentTest {
  @Autowired
  RedisModulesOperations<String> modulesOperations;

  TopKOperations<String> topK;

  @BeforeEach
  void beforeEach() {
    topK = modulesOperations.opsForTopK();
  }

  @Test
  void testBasicOperations() {
    // Create a TopK filter with a capacity of 3
    String result = topK.createFilter("topkTest", 3);
    assertEquals("OK", result);

    // Add three items
    List<String> dropped = topK.add("topkTest", "item1", "item2", "item3");
    assertEquals(3, dropped.size(), "Should return a list with the same size of the added items");
    dropped.forEach(it -> assertNull(it, "No item should be dropped initially"));

    // Add one more item to exceed the capacity
    dropped = topK.add("topkTest", "item4");
    assertEquals(1, dropped.size(), "Should return a list with the same size of the added items");
    assertEquals("item1", dropped.get(0), "The first item should be dropped");

    // Query items
    List<Boolean> exists = topK.query("topkTest", "item1", "item2", "item3", "item4");
    // item1 should've been dropped, so it should not exist. All the others should exist.
    assertFalse(exists.get(0), "item1 should not exist");
    exists.stream().skip(1).forEach(it -> assertTrue(it, "All other items should exist"));

    // List items
    List<String> topItems = topK.list("topkTest");
    assertFalse(topItems.isEmpty(), "Should get the three items");
    assertThat(topItems.size()).isEqualTo(3);

    // Get counts
    Map<String, Long> counts = topK.listWithCount("topkTest");
    assertFalse(counts.isEmpty(), "Should get counts for the three items");
    assertEquals(1L, counts.get("item2"), "item2 should have a count of 1");
    assertEquals(1L, counts.get("item3"), "item3 should have a count of 1");
    assertEquals(1L, counts.get("item4"), "item4 should have a count of 1");
    
    // Clean up after the test
    template.delete("topkTest");
  }

  @Test
  void testIncrementBy() {
    // Create a TopK filter
    topK.createFilter("topkIncrTest", 3);

    // Increment a single item
    String dropped = topK.incrementBy("topkIncrTest", "item1", 5);
    assertNull(dropped, "No item should be dropped initially");

    // Increment multiple items
    Map<String, Long> itemIncrMap = new HashMap<>();
    itemIncrMap.put("item2", 3L);
    itemIncrMap.put("item3", 7L);
    
    List<String> droppedList = topK.incrementBy("topkIncrTest", itemIncrMap);
    assertEquals(2, droppedList.size(), "Should return a list with the same size of the added items");
    assertNull(droppedList.get(0), "No item should be dropped");

    // Check that the items exist with correct counts
    Map<String, Long> counts = topK.listWithCount("topkIncrTest");
    assertFalse(counts.isEmpty());
    
    // Verify counts are as expected (may vary due to probabilistic nature)
    assertEquals(5L, counts.get("item1"), "item1 should have a count of 5");
    assertEquals(3L, counts.get("item2"), "item2 should have a count of 3");
    assertEquals(7L, counts.get("item3"), "item3 should have a count of 7");
    
    // Clean up after the test
    template.delete("topkIncrTest");
  }

  @Test
  void testInfo() {
    // Create a TopK filter with custom parameters
    String status = topK.createFilter("topkInfoTest", 5, 10, 7, 0.9);
    assertEquals("OK", status, "Filter should be created successfully");
    
    // Get filter info
    Map<String, Object> info = topK.info("topkInfoTest");
    
    // Verify expected keys in info map
    assertNotNull(info);
    assertFalse(info.isEmpty());
    
    // Verify specific filter parameters
    assertEquals(5L, info.get("k"));
    assertEquals(10L, info.get("width"));
    assertEquals(7L, info.get("depth"));
    assertEquals(0.9, Double.parseDouble((String) info.get("decay")));
    
    // Clean up after the test
    template.delete("topkInfoTest");
  }
  
  @Test
  void testNonExistingKey() {
    // Attempt to get info for a non-existing key
    JedisDataException exception = assertThrows(JedisDataException.class, 
        () -> topK.info("nonExistingKey"));
    
    // Verify error message
    assertEquals("TopK: key does not exist", exception.getMessage());
  }
} 