package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.Doc4;
import com.redis.om.spring.fixtures.document.repository.Doc4Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DocRepositoryAdvancedTest extends AbstractBaseDocumentTest {
  @Autowired
  Doc4Repository doc4Repository;

  @BeforeEach
  void cleanUp() {
    // entity with nullable properties for projection testing
    if (doc4Repository.count() == 0) {
      var doc41 = Doc4.of("doc4", "doc4.1 third");
      doc41.setSecond("doc4.1 second");
      var doc42 = Doc4.of("doc4", "doc4.2 third");

      var doc43 = Doc4.of("doc4", "doc4.3 third");
      doc43.setSecond("doc4.3 second");
      var doc44 = Doc4.of("doc4", "doc4.4 third");
      doc44.setSecond("doc4.4 second");

      doc4Repository.saveAll(List.of(doc41, doc42, doc43, doc44));
    }
  }

  @Test
  public void testFindOneByNullProperty() {
    var result = doc4Repository.findOneByFirstAndSecondNull("doc4");
    assertThat(result).isPresent();
    assertThat(result.get().getFirst()).isEqualTo("doc4");
  }

  @Test
  public void testFindOneByNullProperty2() {
    var result = doc4Repository.findOneBySecondNull();
    assertThat(result).isPresent();
    assertThat(result.get().getFirst()).isEqualTo("doc4");
  }

  @Test
  public void testFindByNullProperty() {
    var result = doc4Repository.findByFirstAndSecondNull("doc4");
    assertThat(result).isNotEmpty();
    assertThat(result.get(0)).extracting(Doc4::getFirst).isEqualTo("doc4");
  }

  @Test
  public void testFindByNotNullProperty() {
    var result = doc4Repository.findByFirstAndSecondNotNull("doc4");
    assertThat(result).isNotEmpty();
    assertThat(result.get(0)).extracting(Doc4::getFirst).isEqualTo("doc4");
  }

}
