package com.redis.om.spring.indexing;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.ModelDropAndRecreate;
import com.redis.om.spring.fixtures.document.model.ModelSkipAlways;
import com.redis.om.spring.fixtures.document.model.ModelSkipIfExist;
import com.redis.om.spring.fixtures.document.repository.ModelDropAndRecreateRepository;
import com.redis.om.spring.fixtures.document.repository.ModelSkipAlwaysRepository;
import com.redis.om.spring.fixtures.document.repository.ModelSkipIfExistsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

public class IndexingOptionsTest extends AbstractBaseDocumentTest {
  @Autowired
  ModelSkipIfExistsRepository modelSkipIfExistRepository;
  @Autowired
  ModelSkipAlwaysRepository modelSkipAlwaysRepository;
  @Autowired
  ModelDropAndRecreateRepository modelDropAndRecreateRepository;

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testSkipIfExists() {
    assertThat(indexer.indexExistsFor(ModelSkipIfExist.class)).isTrue();
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testSkipAlways() {
    assertThat(indexer.indexExistsFor(ModelSkipAlways.class)).isFalse();
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testDropAndRecreate() {
    assertThat(indexer.indexExistsFor(ModelDropAndRecreate.class)).isTrue();
  }

}
