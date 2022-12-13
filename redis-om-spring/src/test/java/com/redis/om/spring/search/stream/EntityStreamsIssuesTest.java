package com.redis.om.spring.search.stream;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.SomeDocument;
import com.redis.om.spring.annotations.document.fixtures.SomeDocument$;
import com.redis.om.spring.annotations.document.fixtures.SomeDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class EntityStreamsIssuesTest extends AbstractBaseDocumentTest {
  @Autowired GsonBuilder gsonBuilder;
  @Autowired SomeDocumentRepository someDocumentRepository;

  @Autowired EntityStream entityStream;

  @BeforeEach void beforeEach() throws IOException {
    // Load Sample Docs
    if (someDocumentRepository.count() == 0) {
      Reader reader = null;
      try {
        reader = Files.newBufferedReader(Paths.get("src/test/resources/data/some_documents.json"));
        List<SomeDocument> characterEntries = gsonBuilder.create()
            .fromJson(reader, new TypeToken<List<SomeDocument>>() {}.getType());
        someDocumentRepository.saveAll(characterEntries);
      } catch (Exception ex) {
        throw ex;
      } finally {
        reader.close();
      }
    }
  }

  // issue gh-124 - return fields of type String with target String cause GSON MalformedJsonException
  @Test void testReturnFieldsOfTypeStringAreProperlyReturned() {
    var docs = someDocumentRepository.findAll();
    List<String> results = entityStream.of(SomeDocument.class) //
        .filter(SomeDocument$.NAME.eq("LRAWMRENZY")) //
        .limit(1000) //
        .map(SomeDocument$.DESCRIPTION) //
        .collect(Collectors.toList());
    results.stream().forEach(System.out::println);
    assertThat(results).contains("nsw fifth pens geo buffalo");
  }
}
