package com.redis.om.spring;

import com.redis.om.spring.fixtures.document.model.Doc;
import com.redis.om.spring.fixtures.document.repository.DocRepository;
import com.redis.om.spring.search.stream.EntityStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
class LargeNumberOfKeysDocsTest extends AbstractBaseDocumentTest {
  @Autowired
  EntityStream entityStream;

  @Autowired
  DocRepository docRepository;

  @BeforeEach
  void cleanUp() {
    List docs = new ArrayList();
    if (docRepository.count() == 0) {
      IntStream.range(0, 10000000).forEach(i -> {
        docs.add(Doc.of("first doc " + i, "second doc " + i));
      });

      docRepository.saveAll(docs);
    }
  }

  @Test
  @Disabled // skip this test on regular runs since it deals with 10,000,000 records
  void testCount() {
    long count = entityStream //
        .of(Doc.class) //
        .count();

    assertEquals(10000000, count);
  }
}
