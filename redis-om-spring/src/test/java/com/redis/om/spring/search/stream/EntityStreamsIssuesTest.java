package com.redis.om.spring.search.stream;

import static org.assertj.core.api.Assertions.assertThat;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.SomeDocument;
import com.redis.om.spring.annotations.document.fixtures.SomeDocument.Format;
import com.redis.om.spring.annotations.document.fixtures.SomeDocument.Source;
import com.redis.om.spring.annotations.document.fixtures.SomeDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class EntityStreamsIssues extends AbstractBaseDocumentTest {
  @Autowired SomeDocumentRepository someDocumentRepository;

  @BeforeEach
  void beforeEach() {
    someDocumentRepository.deleteAll();

    SomeDocument sd1 = new SomeDocument();
    sd1.setCategory("CAT1");
    sd1.setDescription("This is the description");
    sd1.setFormat(Format.pdf);
    sd1.setName("SomeDocument1");
    sd1.setSource(Source.sourceA);
    sd1.setDocumentCreationDate(LocalDateTime.now());
    sd1.setSearchableContent("This is the searchable content");
    someDocumentRepository.save(sd1);
  }

  @Test
  void printTheJSON() {
    var docs = someDocumentRepository.findAll();
    assertThat(docs).hasSize(1);
  }
}
