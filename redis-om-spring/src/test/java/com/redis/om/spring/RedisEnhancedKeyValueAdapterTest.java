package com.redis.om.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.Bucket;
import org.springframework.data.redis.core.convert.RedisData;

public class RedisEnhancedKeyValueAdapterTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  @Qualifier("redisCustomKeyValueTemplate")
  CustomRedisKeyValueTemplate kvTemplate;
  
  @Autowired
  RedisTemplate<String, String> template;

  @Test
  public void testPutRedisData() {
    RedisData rdo = new RedisData(Bucket.newBucketFromStringMap(Collections.singletonMap("firstname", "rand")));
    rdo.setId("abc");
    rdo.setKeyspace("redisdata");
    kvTemplate.getAdapter().put("abc", rdo, "redisdata");
    
    Object firstName = template.opsForHash().get("redisdata:abc", "firstname");
    assertThat(firstName.toString()).isEqualTo("rand");
  }
}
