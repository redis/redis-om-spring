package com.redis.om.spring.ops.pds;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.RedisModulesOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OpsForCountMinSketchTest extends AbstractBaseDocumentTest {
  @Autowired
  RedisModulesOperations<String> modulesOperations;

  CountMinSketchOperations<String> cms;

  @BeforeEach
  void beforeEach() {
    cms = modulesOperations.opsForCountMinSketch();
  }

  @Test
  void testMerge() {
    cms.cmsInitByDim("A", 1000L, 5L);
    cms.cmsInitByDim("B", 1000L, 5L);
    cms.cmsInitByDim("C", 1000L, 5L);

    Map<String, Long> aValues = new HashMap<>();
    aValues.put("foo", 5L);
    aValues.put("bar", 3L);
    aValues.put("baz", 9L);

    cms.cmsIncrBy("A", aValues);

    Map<String, Long> bValues = new HashMap<>();
    bValues.put("foo", 2L);
    bValues.put("bar", 3L);
    bValues.put("baz", 1L);

    cms.cmsIncrBy("B", bValues);

    List<Long> q1 = cms.cmsQuery("A", "foo", "bar", "baz");
    assertArrayEquals(new Long[] { 5L, 3L, 9L }, q1.toArray(new Long[0]));

    List<Long> q2 = cms.cmsQuery("B", "foo", "bar", "baz");
    assertArrayEquals(new Long[] { 2L, 3L, 1L }, q2.toArray(new Long[0]));

    cms.cmsMerge("C", "A", "B");

    List<Long> q3 = cms.cmsQuery("C", "foo", "bar", "baz");
    assertArrayEquals(new Long[] { 7L, 6L, 10L }, q3.toArray(new Long[0]));

    Map<String, Long> keysAndWeights = new HashMap<>();
    keysAndWeights.put("A", 1L);
    keysAndWeights.put("B", 2L);

    cms.cmsMerge("C", keysAndWeights);

    List<Long> q4 = cms.cmsQuery("C", "foo", "bar", "baz");
    assertArrayEquals(new Long[] { 9L, 9L, 11L }, q4.toArray(new Long[0]));

    keysAndWeights.clear();
    keysAndWeights.put("A", 2L);
    keysAndWeights.put("B", 3L);

    cms.cmsMerge("C", keysAndWeights);

    List<Long> q5 = cms.cmsQuery("C", "foo", "bar", "baz");
    assertArrayEquals(new Long[] { 16L, 15L, 21L }, q5.toArray(new Long[0]));
  }
  
  @Test
  void testInitByProb() {
    cms.cmsInitByProb("cms2", 0.01, 0.01);
    Map<String, Object> info = cms.cmsInfo("cms2");
    assertEquals(200L, info.get("width"));
    assertEquals(7L, info.get("depth"));
    assertEquals(0L, info.get("count"));
  }
  
  @Test
  void testIncrBy() {
    cms.cmsInitByDim("cms3", 1000L, 5L);
    long resp = cms.cmsIncrBy("cms3", "foo", 5L);
    assertEquals(5L, resp);

    Map<String, Object> info = cms.cmsInfo("cms3");
    assertEquals(1000L, info.get("width"));
    assertEquals(5L, info.get("depth"));
    assertEquals(5L, info.get("count"));
  }

}