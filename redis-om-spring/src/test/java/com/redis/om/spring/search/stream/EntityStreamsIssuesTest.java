package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpellCheckingInspection") class EntityStreamsIssuesTest extends AbstractBaseDocumentTest {
  @Autowired SomeDocumentRepository someDocumentRepository;
  @Autowired DeepNestRepository deepNestRepository;

  @Autowired EntityStream entityStream;

  @BeforeEach void beforeEach() throws IOException {
    // Load Sample Docs
    if (someDocumentRepository.count() == 0) {
      someDocumentRepository.bulkLoad("src/test/resources/data/some_documents.json");
    }

    if (deepNestRepository.count() == 0) {
        DeepNest dn1 = DeepNest.of("dn-1", NestLevel1.of("nl-1-1", "Louis, I think this is the beginning of a beautiful friendship.", NestLevel2.of("nl-2-1", "Here's looking at you, kid.")));
        DeepNest dn2 = DeepNest.of("dn-2", NestLevel1.of("nl-1-2", "Whoever you are, I have always depended on the kindness of strangers.", NestLevel2.of("nl-2-2", "Hey, you hens! Cut out the cackling in there!")));
        DeepNest dn3 = DeepNest.of("dn-3", NestLevel1.of("nl-1-3", "A good body with a dull brain is as cheap as life itself.", NestLevel2.of("nl-2-3", "I'm Spartacus!")));
        deepNestRepository.saveAll(List.of(dn1, dn2, dn3));
    }
  }

  // issue gh-124 - return fields of type String with target String cause GSON MalformedJsonException
  @Test void testReturnFieldsOfTypeStringAreProperlyReturned() {
    List<String> results = entityStream.of(SomeDocument.class) //
        .filter(SomeDocument$.NAME.eq("LRAWMRENZY")) //
        .limit(1000) //
        .map(SomeDocument$.DESCRIPTION) //
        .collect(Collectors.toList());
    assertThat(results).contains("nsw fifth pens geo buffalo");
  }

  @Test
  void testFilterEntityStreamsByNestedField() {
    var results = entityStream.of(DeepNest.class) //
        .filter(DeepNest$.NEST_LEVEL1_NEST_LEVEL2_NAME.eq("nl-2-2"))
        .map(DeepNest$.NAME) //
        .collect(Collectors.toList());
    assertThat(results).containsOnly("dn-2");
  }

  @Test
  void testFilterEntityStreamsByNestedField2() {
    var results = entityStream.of(DeepNest.class) //
        .filter(DeepNest$.NEST_LEVEL1_NEST_LEVEL2_BLOCK.containing("Spartacus"))
        .map(DeepNest$.NAME) //
        .collect(Collectors.toList());
    assertThat(results).containsOnly("dn-3");
  }
}
