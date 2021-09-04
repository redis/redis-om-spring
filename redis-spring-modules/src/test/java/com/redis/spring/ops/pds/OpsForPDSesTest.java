package com.redis.spring.ops.pds;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.spring.AbstractBaseTest;
import com.redis.spring.ops.RedisModulesOperations;

public class OpsForPDSesTest extends AbstractBaseTest {
  @Autowired
  RedisModulesOperations<String,String> modulesOperations;
  
  @Test
  public void testBasicBloomOperations() {
    BloomOperations<String> bloom = modulesOperations.opsForBloom();

    assertNotNull(bloom);
  }
  
  @Test
  public void testBasicCMSOperations() {
    CountMinSketchOperations<String> bloom = modulesOperations.opsForCountMinSketch();

    assertNotNull(bloom);
  }
  
  @Test
  public void testTopKOperations() {
    TopKOperations<String> topk = modulesOperations.opsForTopK();
    
    assertNotNull(topk);
  }
  
  @Test
  public void testCuckooKOperations() {
    CuckooFilterOperations<String> cuckoo = modulesOperations.opsForCuckoFilter();
    
    assertNotNull(cuckoo);
  }
}
