package com.redis.om.spring.annotations.hash;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.Hash4;
import com.redis.om.spring.annotations.hash.fixtures.Hash4Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisHashRepositoryAdvancedTest extends AbstractBaseEnhancedRedisTest {
  @Autowired
  Hash4Repository hash4Repository;

  @BeforeEach
  void cleanUp() {
    // entity with nullable properties for projection testing
    if (hash4Repository.count() == 0) {
      var doc41 = Hash4.of("doc4", "doc4.1 third");
      doc41.setSecond("doc4.1 second");
      var doc42 = Hash4.of("doc4", "doc4.2 third");

      var doc43 = Hash4.of("doc4", "doc4.3 third");
      doc43.setSecond("doc4.3 second");
      var doc44 = Hash4.of("doc4", "doc4.4 third");
      doc44.setSecond("doc4.4 second");

      hash4Repository.saveAll(List.of(doc41, doc42, doc43, doc44));
    }
  }

  @Test
  public void testFindOneByNullProperty() {
    var result = hash4Repository.findOneByFirstAndSecondNull("doc4");
    assertThat(result).isPresent();
    assertThat(result.get().getFirst()).isEqualTo("doc4");
  }

  @Test
  public void testFindOneByNullProperty2() {
    var result = hash4Repository.findOneBySecondNull();
    assertThat(result).isPresent();
    assertThat(result.get().getFirst()).isEqualTo("doc4");
  }

  @Test
  public void testFindByNullProperty() {
    var result = hash4Repository.findByFirstAndSecondNull("doc4");
    assertThat(result).isNotEmpty();
    assertThat(result.get(0)).extracting(Hash4::getFirst).isEqualTo("doc4");
  }

  @Test
  public void testFindByNotNullProperty() {
    var result = hash4Repository.findByFirstAndSecondNotNull("doc4");
    assertThat(result).isNotEmpty();
    assertThat(result.get(0)).extracting(Hash4::getFirst).isEqualTo("doc4");
  }

}
