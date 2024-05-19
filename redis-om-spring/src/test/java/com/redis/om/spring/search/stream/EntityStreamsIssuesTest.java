package com.redis.om.spring.search.stream;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.*;
import com.redis.om.spring.tuple.Fields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("SpellCheckingInspection")
class EntityStreamsIssuesTest extends AbstractBaseDocumentTest {
  @Autowired
  SomeDocumentRepository someDocumentRepository;
  @Autowired
  DeepNestRepository deepNestRepository;
  @Autowired
  DeepNestNonIndexedRepository deepNestNonIndexedRepository;
  @Autowired
  DocRepository docRepository;
  @Autowired
  Doc2Repository doc2Repository;
  @Autowired
  DeepListRepository deepListRepository;
  @Autowired
  DocWithLongRepository docWithLongRepository;
  @Autowired
  PersonRepository personRepository;
  @Autowired
  DocWithBooleanRepository docWithBooleanRepository;
  @Autowired
  DocWithDateRepository docWithDateRepository;
  @Autowired
  KitchenSinkRepository kitchenSinkRepository;

  @Autowired
  EntityStream entityStream;

  private LocalDate localDate;
  private LocalDateTime localDateTime;
  private OffsetDateTime localOffsetDateTime;
  private Date date;
  private Point point;
  private Ulid ulid;
  private Set<String> setThings;
  private List<String> listThings;
  private Instant instant;
  private YearMonth yearMonth;

  @BeforeEach
  void beforeEach() throws IOException {
    // Load Sample Docs
    if (someDocumentRepository.count() == 0) {
      someDocumentRepository.bulkLoad("src/test/resources/data/some_documents.json");
    }

    if (deepNestRepository.count() == 0) {
      DeepNest dn1 = DeepNest.of("dn-1",
        NestLevel1.of("nl-1-1", "Louis, I think this is the beginning of a beautiful friendship.",
          NestLevel2.of("nl-2-1", "Here's looking at you, kid.")));
      DeepNest dn2 = DeepNest.of("dn-2",
        NestLevel1.of("nl-1-2", "Whoever you are, I have always depended on the kindness of strangers.",
          NestLevel2.of("nl-2-2", "Hey, you hens! Cut out the cackling in there!")));
      DeepNest dn3 = DeepNest.of("dn-3",
        NestLevel1.of("nl-1-3", "A good body with a dull brain is as cheap as life itself.",
          NestLevel2.of("nl-2-3", "I'm Spartacus!")));
      deepNestRepository.saveAll(List.of(dn1, dn2, dn3));
    }

    if (deepNestNonIndexedRepository.count() == 0) {
      DeepNestNonIndexed dnni1 = DeepNestNonIndexed.of("dn-1",
        NestLevelNonIndexed1.of("nl-1-1", "Louis, I think this is the beginning of a beautiful friendship.",
          NestLevelNonIndexed2.of("nl-2-1", "Here's looking at you, kid.")));
      DeepNestNonIndexed dnni2 = DeepNestNonIndexed.of("dn-2",
        NestLevelNonIndexed1.of("nl-1-2", "Whoever you are, I have always depended on the kindness of strangers.",
          NestLevelNonIndexed2.of("nl-2-2", "Hey, you hens! Cut out the cackling in there!")));
      DeepNestNonIndexed dnni3 = DeepNestNonIndexed.of("dn-3",
        NestLevelNonIndexed1.of("nl-1-3", "A good body with a dull brain is as cheap as life itself.",
          NestLevelNonIndexed2.of("nl-2-3", "I'm Spartacus!")));
      deepNestNonIndexedRepository.saveAll(List.of(dnni1, dnni2, dnni3));
    }

    if (doc2Repository.count() == 0) {
      List<Doc2> doc2s = new ArrayList<>();
      IntStream.range(0, 31)
        .forEach(i -> doc2s.add(Doc2.of(String.format("Marca%s Modelo %s", i, i), String.format("COLOR %s", i))));
      doc2Repository.saveAll(doc2s);
    }

    if (deepListRepository.count() == 0) {
      List<NestLevel2> list1 = List.of(NestLevel2.of("nl-1-1", "It's just a flesh wound!"),
        NestLevel2.of("nl-1-2", "Nobody expects the Spanish Inquisition!"),
        NestLevel2.of("nl-1-3", "I fart in your general direction!"));
      List<NestLevel2> list2 = List.of(NestLevel2.of("nl-2-1", "We are the knights who say 'Ni!"),
        NestLevel2.of("nl-2-2", "And now for something completely different."),
        NestLevel2.of("nl-2-3", "What have the Romans ever done for us?"));
      List<NestLevel2> list3 = List.of(NestLevel2.of("nl-3-1", "I have a very silly job and I take it very seriously."),
        NestLevel2.of("nl-3-2", "What's brown and sticky? A stick!"),
        NestLevel2.of("nl-3-3", "I have a theory that the truth is never told during the nine-to-five hours."));
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

    kitchenSinkRepository.deleteAll();
    localDate = LocalDate.now();
    localDateTime = LocalDateTime.now();
    localOffsetDateTime = OffsetDateTime.now();
    date = new Date();
    point = new Point(33.62826024782707, -111.83592170193586);
    ulid = UlidCreator.getMonotonicUlid();
    setThings = Set.of("thingOne", "thingTwo", "thingThree");
    listThings = List.of("redFish", "blueFish");
    instant = Instant.now();
    yearMonth = YearMonth.of(1972, 6);

    KitchenSink ks = KitchenSink.builder() //
      .localDate(localDate) //
      .localDateTime(localDateTime) //
      .localOffsetDateTime(localOffsetDateTime) //
      .date(date) //
      .point(point) //
      .ulid(ulid) //
      .setThings(setThings) //
      .listThings(listThings) //
      .instant(instant) //
      .yearMonth(yearMonth) //
      .build();

    ks.setId("ks0");

    KitchenSink ks1 = KitchenSink.builder() //
      .localDate(localDate) //
      .localDateTime(localDateTime) //
      .localOffsetDateTime(localOffsetDateTime) //
      .date(date) //
      .point(point) //
      .ulid(ulid) //
      .setThings(Set.of()) //
      .listThings(List.of()) //
      .instant(instant) //
      .yearMonth(yearMonth) //
      .build();

    ks1.setId("ks1");

    KitchenSink ks2 = KitchenSink.builder() //
      .localDate(localDate) //
      .localDateTime(localDateTime) //
      .localOffsetDateTime(localOffsetDateTime) //
      .date(date) //
      .point(point) //
      .ulid(ulid) //
      .instant(instant) //
      .yearMonth(yearMonth) //
      .build();

    ks2.setId("ks2");
    ks2.setSetThings(null);
    ks2.setListThings(null);

    kitchenSinkRepository.saveAll(List.of(ks, ks1, ks2));
  }

  // issue gh-124 - return fields of type String with target String cause GSON MalformedJsonException
  @Test
  void testReturnFieldsOfTypeStringAreProperlyReturned() {
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
      .filter(DeepNest$.NEST_LEVEL1_NEST_LEVEL2_NAME.eq("nl-2-2")).map(DeepNest$.NAME) //
      .collect(Collectors.toList());
    assertThat(results).containsOnly("dn-2");
  }

  @Test
  void testFilterEntityStreamsByNestedField2() {
    var results = entityStream.of(DeepNest.class) //
      .filter(DeepNest$.NEST_LEVEL1_NEST_LEVEL2_BLOCK.containing("Spartacus")).map(DeepNest$.NAME) //
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

    var withFreeTextFirst = entityStream.of(Doc.class).filter("*co*").filter(Doc$.FIRST.eq("Microsoft"))
      .collect(Collectors.toList());

    var withFreeTextLast = entityStream.of(Doc.class).filter(Doc$.FIRST.eq("Microsoft")).filter("*co*")
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(withFreeTextLast).containsExactly(microsoft2),
      () -> assertThat(withFreeTextFirst).containsExactly(microsoft2));
  }

  // issue gh-184
  @Test
  void testPrefixAgainstTextFieldWithSpaces() {
    var startingWithMarca2 = entityStream.of(Doc2.class) //
      .filter(Doc2$.TEXT.startsWith("Marca2")) //
      .collect(Collectors.toList());

    assertThat(startingWithMarca2).map(Doc2::getText).allMatch(t -> t.startsWith("Marca2"));
  }

  @Test
  void testPrefixAgainstTagFieldWithSpaces() {
    var startingWithColor1 = entityStream.of(Doc2.class) //
      .filter(Doc2$.TAG.startsWith("Color 1")) //
      .collect(Collectors.toList());

    assertThat(startingWithColor1).map(Doc2::getTag).allMatch(t -> t.startsWith("COLOR 1"));
  }

  @Test
  void testSuffixAgainstTextFieldWithSpaces() {
    var startingWithMarca2 = entityStream.of(Doc2.class) //
      .filter(Doc2$.TEXT.endsWith("o 11")) //
      .collect(Collectors.toList());

    String regex = ".*o 11$";
    assertThat(startingWithMarca2).map(Doc2::getText).allMatch(t -> t.matches(regex));
  }

  @Test
  void testSuffixAgainstTagFieldWithSpaces() {
    var startingWithMarca2 = entityStream.of(Doc2.class) //
      .filter(Doc2$.TAG.endsWith("LOR 12")) //
      .collect(Collectors.toList());

    String regex = ".*LOR 12$";
    assertThat(startingWithMarca2).map(Doc2::getTag).allMatch(t -> t.matches(regex));
  }

  @Test
  void testSearchInsideListOfObjects() {
    var results = entityStream.of(DeepList.class) //
      .filter(DeepList_nestLevels$.NAME.eq("nl-2-2")) //
      .map(DeepList$.NAME) //
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(results).hasSize(1), () -> assertThat(results).containsExactly("dn-2"));
  }

  @Test
  void testSearchInsideListOfObjects2() {
    var results = entityStream.of(DeepList.class) //
      .filter(DeepList_nestLevels$.NAME.startsWith("nl-2")) //
      .map(DeepList$.NAME) //
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(results).hasSize(1), () -> assertThat(results).containsExactly("dn-2"));
  }

  @Test
  void testSearchInsideListOfObjects3() {
    var results = entityStream.of(DeepList.class) //
      .filter(DeepList_nestLevels$.BLOCK.eq("have")) //
      .map(DeepList$.NAME) //
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(results).hasSize(2), () -> assertThat(results).containsExactlyInAnyOrder("dn-2", "dn-3"));
  }

  @Test
  void testLongEqPredicate() {
    var results = entityStream.of(DocWithLong.class) //
      .filter(DocWithLong$.THE_LONG.eq(3L)) //
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(results).hasSize(1), () -> assertThat(results).extracting("id").containsExactly("doc-3"));
  }

  @Test
  void testLongGePredicate() {
    var results = entityStream.of(DocWithLong.class) //
      .filter(DocWithLong$.THE_LONG.ge(3L)) //
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(results).hasSize(4),
      () -> assertThat(results).extracting("id").containsExactly("doc-3", "doc-4", "doc-5", "doc-6"));
  }

  @Test
  void testLongGtPredicate() {
    var results = entityStream.of(DocWithLong.class) //
      .filter(DocWithLong$.THE_LONG.gt(3L)) //
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(results).hasSize(3),
      () -> assertThat(results).extracting("id").containsExactly("doc-4", "doc-5", "doc-6"));
  }

  @Test
  void testLongInPredicate() {
    var results = entityStream.of(DocWithLong.class) //
      .filter(DocWithLong$.THE_LONG.in(2L, 4L, 6L)) //
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(results).hasSize(3),
      () -> assertThat(results).extracting("id").containsExactly("doc-2", "doc-4", "doc-6"));
  }

  @Test
  void testLongLePredicate() {
    var results = entityStream.of(DocWithLong.class) //
      .filter(DocWithLong$.THE_LONG.le(3L)) //
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(results).hasSize(3),
      () -> assertThat(results).extracting("id").containsExactly("doc-1", "doc-2", "doc-3"));
  }

  @Test
  void testLongLtPredicate() {
    var results = entityStream.of(DocWithLong.class) //
      .filter(DocWithLong$.THE_LONG.lt(3L)) //
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(results).hasSize(2),
      () -> assertThat(results).extracting("id").containsExactly("doc-1", "doc-2"));
  }

  @Test
  void testLongBetweenPredicate() {
    var results = entityStream.of(DocWithLong.class) //
      .filter(DocWithLong$.THE_LONG.between(2L, 5L)) //
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(results).hasSize(4),
      () -> assertThat(results).extracting("id").containsExactly("doc-2", "doc-3", "doc-4", "doc-5"));
  }

  @Test
  void testLongNotEqPredicate() {
    var results = entityStream.of(DocWithLong.class) //
      .filter(DocWithLong$.THE_LONG.notEq(5L)) //
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(results).hasSize(5),
      () -> assertThat(results).extracting("id").containsExactly("doc-1", "doc-2", "doc-3", "doc-4", "doc-6"));
  }

  // issue gh-264 SCENARIO 1
  @Test
  void testMapEntityStreamsReturnNullValue() {
    Person personWithoutEmail = new Person();
    personWithoutEmail.setName("PersonWithoutEmail");
    personWithoutEmail.setEmail(null);
    personRepository.save(personWithoutEmail);

    var result = entityStream.of(Person.class).filter(Person$.NAME.eq("PersonWithoutEmail"))
      .map(Fields.of(Person$.NAME, Person$.EMAIL)) // should handle empty email as mapped return value
      .collect(Collectors.toList()).stream().findFirst().get();
    assertThat(result.getFirst()).isEqualTo("PersonWithoutEmail");
    assertThat(result.getSecond()).isEqualTo(null);
  }

  // issue gh-264 SCENARIO 2
  @Test
  void testMapEntityStreamsReturnNestedField() {
    var results = entityStream.of(DeepNest.class).filter(DeepNest$.NEST_LEVEL1_NEST_LEVEL2_NAME.eq("nl-2-2"))
      .map(DeepNest$.NEST_LEVEL1_NEST_LEVEL2_NAME) // should handle nested property as mapped return value
      .collect(Collectors.toList());
    assertThat(results).containsOnly("nl-2-2");
  }

  // issue gh-265 - indexed boolean
  @Test
  void testMapEntityStreamsReturnIndexedBooleanValue() {
    DocWithBoolean docWithBoolean = new DocWithBoolean();
    docWithBoolean.setIndexedBoolean(true);
    docWithBooleanRepository.save(docWithBoolean);

    var result = entityStream.of(DocWithBoolean.class).filter(DocWithBoolean$.ID.eq(docWithBoolean.getId()))
      .map(DocWithBoolean$.INDEXED_BOOLEAN) // should handle returned indexed boolean value, but fails
      .collect(Collectors.toList());
    assertThat(result).containsOnly(true);
  }

  // issue gh-265 - indexed boolean primitive
  @Test
  void testMapEntityStreamsReturnIndexedBooleanPrimitiveValue() {
    DocWithBoolean docWithBoolean = new DocWithBoolean();
    docWithBoolean.setIndexedPrimitiveBoolean(true);
    docWithBooleanRepository.save(docWithBoolean);

    var result = entityStream.of(DocWithBoolean.class).filter(DocWithBoolean$.ID.eq(docWithBoolean.getId())).map(
        DocWithBoolean$.INDEXED_PRIMITIVE_BOOLEAN) // should handle returned indexed boolean primitive value, but fails
      .collect(Collectors.toList());
    assertThat(result).containsOnly(true);
  }

  // issue gh-265 - non indexed boolean
  @Test
  void testMapEntityStreamsReturnNonIndexedBooleanValue() {
    DocWithBoolean docWithBoolean = new DocWithBoolean();
    docWithBoolean.setNonIndexedBoolean(true);
    docWithBooleanRepository.save(docWithBoolean);

    var result = entityStream.of(DocWithBoolean.class).filter(DocWithBoolean$.ID.eq(docWithBoolean.getId()))
      .map(DocWithBoolean$.NON_INDEXED_BOOLEAN) // should handle returned non indexed boolean value, succeeds
      .collect(Collectors.toList());
    assertThat(result).containsOnly(true);
  }

  // issue gh-265 - non indexed boolean primitive
  @Test
  void testMapEntityStreamsReturnNonIndexedBooleanPrimitiveValue() {
    DocWithBoolean docWithBoolean = new DocWithBoolean();
    docWithBoolean.setNonIndexedPrimitiveBoolean(true);
    docWithBooleanRepository.save(docWithBoolean);

    var result = entityStream.of(DocWithBoolean.class).filter(DocWithBoolean$.ID.eq(docWithBoolean.getId())).map(
        DocWithBoolean$.NON_INDEXED_PRIMITIVE_BOOLEAN) // should handle returned non indexed primitive boolean primitive value, but fails
      .collect(Collectors.toList());
    assertThat(result).containsOnly(true);
  }

  // issue gh-270
  @Test
  void testOnOrAfterDateFilter() throws ParseException {
    SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);

    Date date1 = formatter.parse("01-01-1972");
    Date date2 = formatter.parse("01-02-1972");
    Date date3 = formatter.parse("01-03-1972");
    Date date4 = formatter.parse("01-04-1972");
    Date date5 = formatter.parse("01-05-1972");
    Date date6 = formatter.parse("01-06-1972");
    Date date7 = formatter.parse("01-07-1972");
    Date date8 = formatter.parse("01-08-1972");
    Date date9 = formatter.parse("01-09-1972");

    DocWithDate dwd1 = DocWithDate.of("one", date1);
    DocWithDate dwd2 = DocWithDate.of("two", date3);
    DocWithDate dwd3 = DocWithDate.of("three", date5);
    DocWithDate dwd4 = DocWithDate.of("four", date7);
    DocWithDate dwd5 = DocWithDate.of("five", date9);

    docWithDateRepository.saveAll(List.of(dwd1, dwd2, dwd3, dwd4, dwd5));

    var results = entityStream.of(DocWithDate.class).filter(DocWithDate$.DATE.onOrAfter(date4)).map(DocWithDate$.ID)
      .collect(Collectors.toList());
    assertThat(results).containsOnly("three", "four", "five");
  }

  // issue gh-327
  @Test
  void testNonIndexedReturnedFields() {
    var allFields = Fields.of( //
      KitchenSink$.LOCAL_DATE, KitchenSink$.LOCAL_DATE_TIME, KitchenSink$.DATE, KitchenSink$.POINT, KitchenSink$.ULID,
      KitchenSink$.SET_THINGS, KitchenSink$.LIST_THINGS);
    var result = entityStream.of(KitchenSink.class).filter(KitchenSink$.ID.eq("ks0")).map(allFields)
      .collect(Collectors.toList());

    assertThat(result).hasSize(1);

    var ksValues = result.get(0);

    assertAll( //
      () -> assertThat(ksValues.getFirst()).isEqualTo(localDate),
      () -> assertThat(ksValues.getSecond()).isEqualToIgnoringNanos(localDateTime),
      () -> assertThat(ksValues.getThird()).isEqualTo(date), () -> assertThat(ksValues.getFourth()).isEqualTo(point),
      () -> assertThat(ksValues.getFifth()).isEqualTo(ulid),
      () -> assertThat(ksValues.getSixth()).containsExactlyInAnyOrderElementsOf(setThings),
      () -> assertThat(ksValues.getSeventh()).containsExactlyInAnyOrderElementsOf(listThings));
  }

  @Test
  void testNonIndexedReturnedFieldsWithEmptyCollections() {
    var allFields = Fields.of( //
      KitchenSink$.LOCAL_DATE, KitchenSink$.LOCAL_DATE_TIME, KitchenSink$.DATE, KitchenSink$.POINT, KitchenSink$.ULID,
      KitchenSink$.SET_THINGS, KitchenSink$.LIST_THINGS);
    var result = entityStream.of(KitchenSink.class).filter(KitchenSink$.ID.eq("ks1")).map(allFields)
      .collect(Collectors.toList());

    assertThat(result).hasSize(1);

    var ksValues = result.get(0);

    assertAll( //
      () -> assertThat(ksValues.getFirst()).isEqualTo(localDate),
      () -> assertThat(ksValues.getSecond()).isEqualToIgnoringNanos(localDateTime),
      () -> assertThat(ksValues.getThird()).isEqualTo(date), () -> assertThat(ksValues.getFourth()).isEqualTo(point),
      () -> assertThat(ksValues.getFifth()).isEqualTo(ulid), () -> assertThat(ksValues.getSixth()).isNull(),
      () -> assertThat(ksValues.getSeventh()).isNull());
  }

  @Test
  void testNonIndexedReturnedFieldsWithNullCollections() {
    var allFields = Fields.of( //
      KitchenSink$.LOCAL_DATE, KitchenSink$.LOCAL_DATE_TIME, KitchenSink$.DATE, KitchenSink$.POINT, KitchenSink$.ULID,
      KitchenSink$.SET_THINGS, KitchenSink$.LIST_THINGS);

    var result = entityStream.of(KitchenSink.class).filter(KitchenSink$.ID.eq("ks2")).map(allFields)
      .collect(Collectors.toList());

    assertThat(result).hasSize(1);

    var ksValues = result.get(0);

    assertAll( //
      () -> assertThat(ksValues.getFirst()).isEqualTo(localDate),
      () -> assertThat(ksValues.getSecond()).isEqualToIgnoringNanos(localDateTime),
      () -> assertThat(ksValues.getThird()).isEqualTo(date), () -> assertThat(ksValues.getFourth()).isEqualTo(point),
      () -> assertThat(ksValues.getFifth()).isEqualTo(ulid), () -> assertThat(ksValues.getSixth()).isNull(),
      () -> assertThat(ksValues.getSeventh()).isNull());
  }

  @Test
  void testReturnedFieldsDeepNested() {
    var allFields = Fields.of( //
      DeepNest$.NAME, DeepNest$.NEST_LEVEL1_NAME, DeepNest$.NEST_LEVEL1_BLOCK, DeepNest$.NEST_LEVEL1_NEST_LEVEL2_NAME,
      DeepNest$.NEST_LEVEL1_NEST_LEVEL2_BLOCK);
    var result = entityStream.of(DeepNest.class).filter(DeepNest$.NAME.eq("dn-1")).map(allFields)
      .collect(Collectors.toList());

    assertThat(result).hasSize(1);

    var values = result.get(0);

    assertAll( //
      () -> assertThat(values.getFirst()).isEqualTo("dn-1"), () -> assertThat(values.getSecond()).isEqualTo("nl-1-1"),
      () -> assertThat(values.getThird()).isEqualTo("Louis, I think this is the beginning of a beautiful friendship."),
      () -> assertThat(values.getFourth()).isEqualTo("nl-2-1"),
      () -> assertThat(values.getFifth()).isEqualTo("Here's looking at you, kid."));
  }

  @Test
  void testReturnedFieldsDeepNestedNonIndexed() {
    var allFields = Fields.of( //
      DeepNestNonIndexed$.NAME, DeepNestNonIndexed$.NEST_LEVEL1_NAME, DeepNestNonIndexed$.NEST_LEVEL1_BLOCK,
      DeepNestNonIndexed$.NEST_LEVEL1_NEST_LEVEL2_NAME, DeepNestNonIndexed$.NEST_LEVEL1_NEST_LEVEL2_BLOCK);
    var result = entityStream.of(DeepNestNonIndexed.class).filter(DeepNestNonIndexed$.NAME.eq("dn-1")).map(allFields)
      .collect(Collectors.toList());

    assertThat(result).hasSize(1);

    var values = result.get(0);

    assertAll( //
      () -> assertThat(values.getFirst()).isEqualTo("dn-1"), () -> assertThat(values.getSecond()).isEqualTo("nl-1-1"),
      () -> assertThat(values.getThird()).isEqualTo("Louis, I think this is the beginning of a beautiful friendship."),
      () -> assertThat(values.getFourth()).isEqualTo("nl-2-1"),
      () -> assertThat(values.getFifth()).isEqualTo("Here's looking at you, kid."));
  }

  @Test
  void testEqAgainstContentWithForwardSlash() {
    Doc2 doc1 = doc2Repository.save(Doc2.of("This is Picture", "excellent/birds"));
    Doc2 doc2 = doc2Repository.save(Doc2.of("Here it comes", "excellent/snow"));

    var result = entityStream.of(Doc2.class).filter(Doc2$.TAG.eq("excellent/birds")).collect(Collectors.toList());

    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(doc1);

    doc2Repository.deleteAll(List.of(doc1, doc2));
  }

}
