package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.*;
import com.redis.om.spring.tuple.Fields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("SpellCheckingInspection") class EntityStreamsIssuesTest extends AbstractBaseDocumentTest {
  @Autowired SomeDocumentRepository someDocumentRepository;
  @Autowired DeepNestRepository deepNestRepository;
  @Autowired DocRepository docRepository;
  @Autowired Doc2Repository doc2Repository;
  @Autowired DeepListRepository deepListRepository;
  @Autowired DocWithLongRepository docWithLongRepository;
  @Autowired PersonRepository personRepository;

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

    if (doc2Repository.count() == 0) {
      List<Doc2> doc2s = new ArrayList<>();
      IntStream.range(0, 31).forEach(i -> {
          doc2s.add(Doc2.of(String.format("Marca%s Modelo %s", i, i), String.format("COLOR %s", i)));
      });
      doc2Repository.saveAll(doc2s);
    }

    if (deepListRepository.count() == 0) {
      List<NestLevel2> list1 = List.of(
          NestLevel2.of("nl-1-1", "It's just a flesh wound!"),
          NestLevel2.of("nl-1-2", "Nobody expects the Spanish Inquisition!"),
          NestLevel2.of("nl-1-3", "I fart in your general direction!")
      );
      List<NestLevel2> list2 = List.of(
          NestLevel2.of("nl-2-1", "We are the knights who say 'Ni!"),
          NestLevel2.of("nl-2-2", "And now for something completely different."),
          NestLevel2.of("nl-2-3", "What have the Romans ever done for us?")
      );
      List<NestLevel2> list3 = List.of(
          NestLevel2.of("nl-3-1", "I have a very silly job and I take it very seriously."),
          NestLevel2.of("nl-3-2", "What's brown and sticky? A stick!"),
          NestLevel2.of("nl-3-3", "I have a theory that the truth is never told during the nine-to-five hours.")
      );
      DeepList dl1 = DeepList.of("dn-1", list1);
      DeepList dl2 = DeepList.of("dn-2", list2);
      DeepList dl3 = DeepList.of("dn-3", list3);

      deepListRepository.saveAll(List.of(dl1, dl2, dl3));
    }

    if (docWithLongRepository.count() == 0) {
      docWithLongRepository.save(DocWithLong.of("doc-1", 1L));
      docWithLongRepository.save(DocWithLong.of("doc-2", 2L));
      docWithLongRepository.save(DocWithLong.of("doc-3", 3L));
      docWithLongRepository.save(DocWithLong.of("doc-4", 4L));
      docWithLongRepository.save(DocWithLong.of("doc-5", 5L));
      docWithLongRepository.save(DocWithLong.of("doc-6", 6L));
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

  // issue gh-176
  @Test
  void testFreeFormTextSearchOrderIssue() {
    docRepository.deleteAll();
    Doc redis1 = docRepository.save(Doc.of("Redis", "wwwabccom"));
    Doc redis2 = docRepository.save(Doc.of("Redis", "wwwxyznet"));
    Doc microsoft1 = docRepository.save(Doc.of("Microsoft", "wwwabcnet"));
    Doc microsoft2 = docRepository.save(Doc.of("Microsoft", "wwwxyzcom"));

    var withFreeTextFirst = entityStream.of(Doc.class)
        .filter("*co*")
        .filter(Doc$.FIRST.eq("Microsoft"))
        .collect(Collectors.toList());

    var withFreeTextLast = entityStream.of(Doc.class)
        .filter(Doc$.FIRST.eq("Microsoft"))
        .filter("*co*")
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(withFreeTextLast).containsExactly(microsoft2),
        () -> assertThat(withFreeTextFirst).containsExactly(microsoft2)
    );
  }

  // issue gh-184
  @Test void testPrefixAgainstTextFieldWithSpaces() {
    var startingWithMarca2 = entityStream.of(Doc2.class) //
        .filter(Doc2$.TEXT.startsWith("Marca2")) //
        .collect(Collectors.toList());

    assertThat(startingWithMarca2).map(Doc2::getText).allMatch(t -> t.startsWith("Marca2"));
  }

  @Test void testPrefixAgainstTagFieldWithSpaces() {
    var startingWithColor1 = entityStream.of(Doc2.class) //
        .filter(Doc2$.TAG.startsWith("Color 1")) //
        .collect(Collectors.toList());

    assertThat(startingWithColor1).map(Doc2::getTag).allMatch(t -> t.startsWith("COLOR 1"));
  }

  @Test void testSuffixAgainstTextFieldWithSpaces() {
    var startingWithMarca2 = entityStream.of(Doc2.class) //
        .filter(Doc2$.TEXT.endsWith("o 11")) //
        .collect(Collectors.toList());

    String regex = ".*o\s11$";
    assertThat(startingWithMarca2).map(Doc2::getText).allMatch(t -> t.matches(regex));
  }

  @Test void testSuffixAgainstTagFieldWithSpaces() {
    var startingWithMarca2 = entityStream.of(Doc2.class) //
        .filter(Doc2$.TAG.endsWith("LOR 12")) //
        .collect(Collectors.toList());

    String regex = ".*LOR\s12$";
    assertThat(startingWithMarca2).map(Doc2::getTag).allMatch(t -> t.matches(regex));
  }

  @Test void testSearchInsideListOfObjects() {
    var results = entityStream.of(DeepList.class) //
        .filter(DeepList$.NEST_LEVELS.NAME.eq("nl-2-2")) //
        .map(DeepList$.NAME) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(1),
        () -> assertThat(results).containsExactly("dn-2")
    );
  }

  @Test void testSearchInsideListOfObjects2() {
    var results = entityStream.of(DeepList.class) //
        .filter(DeepList$.NEST_LEVELS.NAME.startsWith("nl-2")) //
        .map(DeepList$.NAME) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(1),
        () -> assertThat(results).containsExactly("dn-2")
    );
  }

  @Test void testSearchInsideListOfObjects3() {
    var results = entityStream.of(DeepList.class) //
        .filter(DeepList$.NEST_LEVELS.BLOCK.eq("have")) //
        .map(DeepList$.NAME) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(2),
        () -> assertThat(results).containsExactlyInAnyOrder("dn-2", "dn-3")
    );
  }

  @Test void testLongEqPredicate() {
    var results = entityStream.of(DocWithLong.class) //
        .filter(DocWithLong$.THE_LONG.eq(3L)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(1),
        () -> assertThat(results).extracting("id").containsExactly("doc-3")
    );
  }

  @Test void testLongGePredicate() {
    var results = entityStream.of(DocWithLong.class) //
        .filter(DocWithLong$.THE_LONG.ge(3L)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(4),
        () -> assertThat(results).extracting("id").containsExactly("doc-3", "doc-4", "doc-5", "doc-6")
    );
  }

  @Test void testLongGtPredicate() {
    var results = entityStream.of(DocWithLong.class) //
        .filter(DocWithLong$.THE_LONG.gt(3L)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(3),
        () -> assertThat(results).extracting("id").containsExactly("doc-4", "doc-5", "doc-6")
    );
  }

  @Test void testLongInPredicate() {
    var results = entityStream.of(DocWithLong.class) //
        .filter(DocWithLong$.THE_LONG.in(2L, 4L, 6L)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(3),
        () -> assertThat(results).extracting("id").containsExactly("doc-2", "doc-4", "doc-6")
    );
  }

  @Test void testLongLePredicate() {
    var results = entityStream.of(DocWithLong.class) //
        .filter(DocWithLong$.THE_LONG.le(3L)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(3),
        () -> assertThat(results).extracting("id").containsExactly("doc-1", "doc-2", "doc-3")
    );
  }

  @Test void testLongLtPredicate() {
    var results = entityStream.of(DocWithLong.class) //
        .filter(DocWithLong$.THE_LONG.lt(3L)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(2),
        () -> assertThat(results).extracting("id").containsExactly("doc-1", "doc-2")
    );
  }

  @Test void testLongBetweenPredicate() {
    var results = entityStream.of(DocWithLong.class) //
        .filter(DocWithLong$.THE_LONG.between(2L, 5L)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(4),
        () -> assertThat(results).extracting("id").containsExactly("doc-2", "doc-3", "doc-4", "doc-5")
    );
  }

  @Test void testLongNotEqPredicate() {
    var results = entityStream.of(DocWithLong.class) //
        .filter(DocWithLong$.THE_LONG.notEq(5L)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(5),
        () -> assertThat(results).extracting("id").containsExactly("doc-1", "doc-2", "doc-3", "doc-4", "doc-6")
    );
  }

  // issue gh-264 SCENARIO 1
  @Test
  void testMapEntityStreamsReturnNullValue() {
    Person personWithoutEmail = new Person();
    personWithoutEmail.setName("PersonWithoutEmail");
    personWithoutEmail.setEmail(null);
    personRepository.save(personWithoutEmail);

    var result = entityStream.of(Person.class)
            .filter(Person$.NAME.eq("PersonWithoutEmail"))
            .map(Fields.of(Person$.NAME, Person$.EMAIL)) // should handle empty email as mapped return value
            .collect(Collectors.toList()).stream().findFirst().get();
    assertThat(result.getFirst()).isEqualTo("PersonWithoutEmail");
    assertThat(result.getSecond()).isEqualTo(null);
  }

  // issue gh-264 SCENARIO 2
  @Test
  void testMapEntityStreamsReturnNestedField() {
    var results = entityStream.of(DeepNest.class)
            .filter(DeepNest$.NEST_LEVEL1_NEST_LEVEL2_NAME.eq("nl-2-2"))
            .map(DeepNest$.NEST_LEVEL1_NEST_LEVEL2_NAME) // should handle nested property as mapped return value
            .collect(Collectors.toList());
    assertThat(results).containsOnly("nl-2-2");
  }
}
