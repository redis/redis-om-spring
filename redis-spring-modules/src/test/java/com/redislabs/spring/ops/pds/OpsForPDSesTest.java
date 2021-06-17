package com.redislabs.spring.ops.pds;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.annotation.PreDestroy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.redislabs.spring.ops.RedisModulesOperations;

public class OpsForPDSesTest {
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
  
  @SpringBootApplication
  @Configuration
  static class Config {
    @Autowired
    RedisConnectionFactory connectionFactory;
    
    @PreDestroy
    void cleanUp() {
      connectionFactory.getConnection().flushAll();
    }
  }
}
