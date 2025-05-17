package com.redis.om.spring.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;

class SimpleHashTest extends AbstractBaseEnhancedRedisTest {

  private static final int SIZE = 20;

  private final SimpleHashRepository simpleHashRepository;

  @Autowired
  SimpleHashTest(SimpleHashRepository simpleHashRepository) {
    this.simpleHashRepository = simpleHashRepository;
  }

  @BeforeEach
  void setUp() {
    for (int i = 0; i < SIZE; ++i) {
      simpleHashRepository.save(new SimpleHash());
    }
  }

  @Test
  void testAllSimpleDocumentsReturned() {
    assumeThat(simpleHashRepository.count()).isEqualTo(SIZE);

    List<SimpleHash> documents = simpleHashRepository.findAll();
    assertThat(documents).isNotNull().hasSize(SIZE);
  }

  @AfterEach
  void tearDown() {
    simpleHashRepository.deleteAll();
  }
}
