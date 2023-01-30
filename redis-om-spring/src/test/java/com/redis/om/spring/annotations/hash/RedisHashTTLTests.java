package com.redis.om.spring.annotations.hash;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpellCheckingInspection") class RedisHashTTLTests extends AbstractBaseEnhancedRedisTest {
  @Autowired
  ExpiringPersonWithDefaultRepository withDefaultrepository;
  
  @Autowired
  ExpiringPersonRepository withTTLAnnotationRepository;
  
  @Autowired
  ExpiringPersonDifferentTimeUnitRepository withTTLwTimeUnitAnnotationRepository;
  
  @Autowired StringRedisTemplate template;
  
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

  @Test
  void testClassLevelDefaultTTLOnSaveAll() {
    ExpiringPersonWithDefault gordon = ExpiringPersonWithDefault.of("Gordon Welchman");
    ExpiringPersonWithDefault irineu = ExpiringPersonWithDefault.of("Irineu Evangelista de Sousa");
    List<ExpiringPersonWithDefault> people = List.of(gordon, irineu);

    withDefaultrepository.saveAll(people);

    Long gordonExpiration = withDefaultrepository.getExpiration(gordon.getId());
    Long irineuExpiration = withDefaultrepository.getExpiration(irineu.getId());


    assertThat(gordonExpiration).isEqualTo(5L);
    assertThat(irineuExpiration).isEqualTo(5L);
  }

  @Test
  void testTimeToLiveAnnotationOnSaveAll() {
    ExpiringPerson mWoodger = ExpiringPerson.of("Mike Woodger", 15L);
    ExpiringPerson mPontes = ExpiringPerson.of("Marcos Pontes", 15L);
    List<ExpiringPerson> people = List.of(mWoodger, mPontes);

    withTTLAnnotationRepository.saveAll(people);

    Long mWoodgerExpiration = withTTLAnnotationRepository.getExpiration(mWoodger.getId());
    Long mPontesExpiration = withTTLAnnotationRepository.getExpiration(mPontes.getId());

    assertThat(mWoodgerExpiration).isEqualTo(15L);
    assertThat(mPontesExpiration).isEqualTo(15L);
  }

  @Test
  void testTimeToLiveAnnotationWithDifferentTimeUnitOnSaveAll() {
    ExpiringPersonDifferentTimeUnit jWilkinson = ExpiringPersonDifferentTimeUnit.of("Jim Wilkinson", 7L);
    ExpiringPersonDifferentTimeUnit sDummont = ExpiringPersonDifferentTimeUnit.of("Santos Dummont", 7L);
    List<ExpiringPersonDifferentTimeUnit> people = List.of(jWilkinson, sDummont);

    withTTLwTimeUnitAnnotationRepository.saveAll(people);

    Long jWilkinsonExpiration = withTTLwTimeUnitAnnotationRepository.getExpiration(jWilkinson.getId());
    Long sDummontExpiration = withTTLwTimeUnitAnnotationRepository.getExpiration(sDummont.getId());

    assertThat(jWilkinsonExpiration).isEqualTo(7L*24*60*60);
    assertThat(sDummontExpiration).isEqualTo(7L*24*60*60);
  }
}
