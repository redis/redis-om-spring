package com.redis.om.spring.annotations.hash;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.ExpiringPerson;
import com.redis.om.spring.annotations.hash.fixtures.ExpiringPersonDifferentTimeUnit;
import com.redis.om.spring.annotations.hash.fixtures.ExpiringPersonDifferentTimeUnitRepository;
import com.redis.om.spring.annotations.hash.fixtures.ExpiringPersonRepository;
import com.redis.om.spring.annotations.hash.fixtures.ExpiringPersonWithDefault;
import com.redis.om.spring.annotations.hash.fixtures.ExpiringPersonWithDefaultRepository;

class RedisHashTTLTests extends AbstractBaseEnhancedRedisTest {
  @Autowired
  ExpiringPersonWithDefaultRepository withDefaultrepository;
  
  @Autowired
  ExpiringPersonRepository withTTLAnnotationRepository;
  
  @Autowired
  ExpiringPersonDifferentTimeUnitRepository withTTLwTimeUnitAnnotationRepository;
  
  @Autowired
  RedisTemplate<String, String> template;
  
  @BeforeEach
  void cleanUp() {
    withDefaultrepository.deleteAll();
  }
  
  @Test
  void testClassLevelDefaultTTL() {
    ExpiringPersonWithDefault gordon = ExpiringPersonWithDefault.of("Gordon Welchman");
    withDefaultrepository.save(gordon);
    
    Long expire = withDefaultrepository.getExpiration(gordon.getId());
    
    assertThat(expire).isEqualTo(5L);
  }
  
  @Test
  void testTimeToLiveAnnotation() {
    ExpiringPerson mWoodger = ExpiringPerson.of("Mike Woodger", 15L);
    withTTLAnnotationRepository.save(mWoodger);
 
    Long expire = withTTLAnnotationRepository.getExpiration(mWoodger.getId());

    assertThat(expire).isEqualTo(15L);
  }
  
  @Test
  void testTimeToLiveAnnotationWithDifferentTimeUnit() {
    ExpiringPersonDifferentTimeUnit jWilkinson = ExpiringPersonDifferentTimeUnit.of("Jim Wilkinson", 7L);
    withTTLwTimeUnitAnnotationRepository.save(jWilkinson);

    Long expire = withTTLwTimeUnitAnnotationRepository.getExpiration(jWilkinson.getId());

    assertThat(expire).isEqualTo(7L*24*60*60);
  }
}
