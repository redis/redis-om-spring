package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.ExpiringPerson;
import com.redis.om.spring.annotations.document.fixtures.ExpiringPersonDifferentTimeUnit;
import com.redis.om.spring.annotations.document.fixtures.ExpiringPersonDifferentTimeUnitRepository;
import com.redis.om.spring.annotations.document.fixtures.ExpiringPersonRepository;
import com.redis.om.spring.annotations.document.fixtures.ExpiringPersonWithDefault;
import com.redis.om.spring.annotations.document.fixtures.ExpiringPersonWithDefaultRepository;

class DocumentTTLTests extends AbstractBaseDocumentTest {
  @Autowired
  ExpiringPersonWithDefaultRepository withDefaultrepository;
  
  @Autowired
  ExpiringPersonRepository withTTLAnnotationRepository;
  
  @Autowired
  ExpiringPersonDifferentTimeUnitRepository withTTLwTimeUnitAnnotationRepository;
  
  @BeforeEach
  void cleanUp() {
    withDefaultrepository.deleteAll();
    withTTLAnnotationRepository.deleteAll();
    withTTLwTimeUnitAnnotationRepository.deleteAll();
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
