package com.redis.om.spring.annotations.document;

import com.github.f4b6a3.ulid.Ulid;
import com.google.common.collect.Lists;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.*;
import com.redis.om.spring.repository.query.Sort;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("SpellCheckingInspection")
class ComplexDocumentSearchTest extends AbstractBaseDocumentTest {
  Permit permit1;
  Permit permit2;
  Permit permit3;

  @Autowired
  PermitRepository repository;

  @Autowired
  ComplexRepository complexRepository;

  @Autowired
  WithNestedListOfUUIDsRepository withNestedListOfUUIDsRepository;

  @Autowired
  WithNestedListOfUlidsRepository withNestedListOfUlidsRepository;

  @Autowired
  EntityStream es;

  UUID uuid1 = UUID.fromString("297c358e-08df-4e9e-8e0e-c5fd6e2b5b2d");
  UUID uuid2 = UUID.fromString("72a93375-30ae-4075-86cf-b07c62800713");
  UUID uuid3 = UUID.fromString("d6b65b6c-3b93-44b7-8f30-8695028ba36f");
  UUID uuid4 = UUID.fromString("2b3b6517-5a4c-48da-a2a7-7a9ef2162c6d");
  UUID uuid5 = UUID.fromString("3b4c23cc-f9b6-4df1-b368-5ec82e32b8f9");
  UUID uuid6 = UUID.fromString("01be53c2-29e6-468f-9fe8-ad082d738c65");

  Ulid ulid1 = Ulid.from(uuid1);
  Ulid ulid2 = Ulid.from(uuid2);
  Ulid ulid3 = Ulid.from(uuid3);
  Ulid ulid4 = Ulid.from(uuid4);
  Ulid ulid5 = Ulid.from(uuid5);
  Ulid ulid6 = Ulid.from(uuid6);

  @BeforeEach
  void setup() {
    repository.deleteAll();
    complexRepository.deleteAll();

    // # Document 1
    Address address1 = Address.of("Lisbon", "25 de Abril");
    Order order1 = Order.of("O11", 1.5);
    Order order2 = Order.of("O12", 5.6);
    Attribute attribute11 = Attribute.of("size", "S", Lists.newArrayList(order1));
    Attribute attribute12 = Attribute.of("size", "M", Lists.newArrayList(order2));
    List<Attribute> attrList1 = Lists.newArrayList(attribute11, attribute12);
    permit1 = Permit.of( //
      address1, //
      "To construct a single detached house with a front covered veranda.", //
      "single detached house", //
      Set.of("demolition", "reconstruction"), //
      42000L, //
      new Point(38.7635877, -9.2018309), //
      List.of("started", "in_progress", "approved"), //
      attrList1);
    permit1.setPermitTimestamp(LocalDateTime.of(2022, 8, 1, 10, 0));

    // # Document 2
    Address address2 = Address.of("Porto", "Av. da Liberdade");
    Order order21 = Order.of("O21", 1.2);
    Order order22 = Order.of("O22", 5.6);
    Attribute attribute21 = Attribute.of("color", "red", Lists.newArrayList(order21));
    Attribute attribute22 = Attribute.of("color", "blue", Lists.newArrayList(order22));
    List<Attribute> attrList2 = Lists.newArrayList(attribute21, attribute22);
    permit2 = Permit.of( //
      address2, //
      "To construct a loft", //
      "apartment", //
      Set.of("construction"), //
      53000L, //
      new Point(38.7205373, -9.148091), //
      List.of("started", "in_progress", "rejected"), //
      attrList2);
    permit2.setPermitTimestamp(LocalDateTime.of(2022, 8, 2, 0, 0));

    // # Document 3
    Address address3 = Address.of("Lagos", "D. João");
    Order order31 = Order.of("ABC", 1.6);
    Order order32 = Order.of("DEF", 1.3);
    Order order33 = Order.of("GHJ", 1.6);
    Order order34 = Order.of("VBN", 1.0);
    Attribute attribute31 = Attribute.of("brand", "A", Lists.newArrayList(order31, order33));
    Attribute attribute32 = Attribute.of("brand", "B", Lists.newArrayList(order32, order34));
    List<Attribute> attrList3 = Lists.newArrayList(attribute31, attribute32);
    permit3 = Permit.of( //
      address3, //
      "New house build", //
      "house", //
      Set.of("construction", "design"), //
      260000L, //
      new Point(37.0990749, -8.6868258), //
      List.of("started", "in_progress", "postponed"), //
      attrList3);
    permit3.setPermitTimestamp(LocalDateTime.of(2022, 8, 25, 0, 0));

    repository.saveAll(List.of(permit1, permit2, permit3));

    // complex deep nested
    Complex complex1 = Complex.of("complex1",
      List.of(HasAList.of(List.of("Nudiustertian", "Comeuppance", "Yarborough")),
        HasAList.of(List.of("Sialoquent", "Pauciloquent", "Bloviate"))));
    Complex complex2 = Complex.of("complex2", List.of(HasAList.of(List.of("Quire", "Zoanthropy", "Flibbertigibbet")),
      HasAList.of(List.of("Taradiddle", "Malarkey", "Comeuppance"))));
    Complex complex3 = Complex.of("complex3", List.of(HasAList.of(List.of("Pandiculation", "Taradiddle", "Ratoon")),
      HasAList.of(List.of("Yarborough", "Wabbit", "Erinaceous"))));

    complexRepository.saveAll(List.of(complex1, complex2, complex3));

    // complex deep nested with uuids
    WithNestedListOfUUIDs withNestedListOfUUIDs1 = WithNestedListOfUUIDs.of("withNestedListOfUUIDs1",
      List.of(uuid1, uuid2, uuid3));
    WithNestedListOfUUIDs withNestedListOfUUIDs2 = WithNestedListOfUUIDs.of("withNestedListOfUUIDs2",
      List.of(uuid4, uuid5, uuid6));

    withNestedListOfUUIDsRepository.saveAll(List.of(withNestedListOfUUIDs1, withNestedListOfUUIDs2));

    // complex deep nested with ulids
    WithNestedListOfUlids withNestedListOfUlids1 = WithNestedListOfUlids.of("withNestedListOfUlids1",
      List.of(ulid1, ulid2, ulid3));
    WithNestedListOfUlids withNestedListOfUlids2 = WithNestedListOfUlids.of("withNestedListOfUlids2",
      List.of(ulid4, ulid5, ulid6));

    withNestedListOfUlidsRepository.saveAll(List.of(withNestedListOfUlids1, withNestedListOfUlids2));

  }

  @Test
  void testFindbyConstructionValue() {
    long value = 42000L;
    Iterable<Permit> permits = repository.findByConstructionValue(value);
    assertThat(permits).containsExactly(permit1);
  }

  @Test
  void testFullTextSearch() {
    String q = "veranda";
    Iterable<Permit> permits = repository.search(q);
    assertThat(permits).containsExactly(permit1);
  }

  @Test
  void testByBuildingType() {
    String type = "detached";
    Iterable<Permit> permits = repository.findByBuildingType(type);
    assertThat(permits).containsExactly(permit1);
  }

  @Test
  void testByCity() {
    Iterable<Permit> permits = repository.findByAddress_City("Lisbon");
    assertThat(permits).containsExactly(permit1);

    permits = repository.findByAddress_City("Porto");
    assertThat(permits).containsExactly(permit2);
  }

  @Test
  void testByTags() {
    Set<String> wts = Set.of("design", "construction");
    Iterable<Permit> permits = repository.findByWorkType(wts);
    assertThat(permits).containsExactlyInAnyOrder(permit2, permit3);
  }

  @Test
  void testByTagsWithNullParams() {
    // # Document 4
    List<Attribute> attrs = List.of();
    Address address4 = Address.of("Coimbra", "R. Serra 14");
    Permit permit4 = Permit.of( //
      address4, //
      "Historical Renovation", //
      "church", //
      Set.of(), //
      260000L, //
      new Point(37.0990749, -8.6868258), //
      List.of(), //
      attrs);

    repository.save(permit4);

    Iterable<Permit> permits = repository.findByWorkType(Set.of());
    assertThat(permits).isEmpty();
  }

  @Test
  void testByAllTags() {
    Set<String> wts = Set.of("design", "construction");
    Iterable<Permit> permits = repository.findByWorkTypeContainingAll(wts);
    assertThat(permits).containsExactly(permit3);
  }

  @Test
  void testByBuildingTypeAndDescription() {
    String buildingType = "house";
    String description = "new";
    Iterable<Permit> permits = repository.findByBuildingTypeAndDescription(buildingType, description);
    assertThat(permits).containsExactly(permit3);
  }

  @Test
  void testByCityOrDescription() {
    String city = "Lagos";
    String description = "detached";
    Iterable<Permit> permits = repository.findByAddress_CityOrDescription(city, description);
    assertThat(permits).containsExactlyInAnyOrder(permit1, permit3);
  }

  @Test
  void testFullTextSearchShouldNotEscapeSearchString() {
    String q = "Single detached House";
    Iterable<Permit> permits = repository.search(q);
    assertThat(permits).containsExactly(permit1);
  }

  @Test
  void testFullTextSearchWithExplicitEscaping() {
    String q = "house\\ with\\ a\\ front";
    Iterable<Permit> permits = repository.search(q);
    assertThat(permits).isEmpty();
  }

  @Test
  void testFullTextSearchExplicitEscapeSearchTerm() {
    String q = "\"single detached house\"";
    Iterable<Permit> permits = repository.search(q);
    assertThat(permits).containsExactly(permit1);
  }

  @Test
  void testFullTextSearchExplicitEscapeSearchTermPrefixSearch() {
    String q = "To construct*";
    Iterable<Permit> permits = repository.search(q);
    assertThat(permits).containsExactly(permit1, permit2);
  }

  /**
   * "FT.SEARCH" "com.redis.om.spring.annotations.document.fixtures.PermitIdx" "To construct*" "SORTBY" "constructionValue" "DESC"
   */
  @Test
  void testFullTextSearchWithSortByNumericFieldDesc() {
    String q = "To construct*";
    Pageable pageRequest = PageRequest.of(0, 10).withSort(Sort.by(Direction.DESC, Permit$.CONSTRUCTION_VALUE));
    Page<Permit> result = repository.search(q, pageRequest);

    assertThat(result.getTotalPages()).isEqualTo(1);
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent()).containsExactly(permit2, permit1);
  }

  /**
   * "FT.SEARCH" "com.redis.om.spring.annotations.document.fixtures.PermitIdx" "To construct*" "SORTBY" "constructionValue" "ASC"
   */
  @Test
  void testFullTextSearchWithSortByNumericFieldAsc() {
    String q = "To construct*";
    Pageable pageRequest = PageRequest.of(0, 10).withSort(Sort.by(Direction.ASC, Permit$.CONSTRUCTION_VALUE));
    Page<Permit> result = repository.search(q, pageRequest);

    assertThat(result.getTotalPages()).isEqualTo(1);
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent()).containsExactly(permit1, permit2);
  }

  /**
   * "FT.SEARCH" "com.redis.om.spring.annotations.document.fixtures.PermitIdx" "To construct*" "SORTBY" "buildingType" "DESC"
   */
  @Test
  void testFullTextSearchWithSortByTextFieldDesc() {
    String q = "To construct*";
    Pageable pageRequest = PageRequest.of(0, 10).withSort(Sort.by(Direction.DESC, Permit$.BUILDING_TYPE));
    Page<Permit> result = repository.search(q, pageRequest);

    assertThat(result.getTotalPages()).isEqualTo(1);
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent()).containsExactly(permit1, permit2);
  }

  /**
   * "FT.SEARCH" "com.redis.om.spring.annotations.document.fixtures.PermitIdx" "To construct*" "SORTBY" "buildingType" "ASC"
   */
  @Test
  void testFullTextSearchWithSortByTextFieldAsc() {
    String q = "To construct*";
    Pageable pageRequest = PageRequest.of(0, 10).withSort(Sort.by(Direction.ASC, Permit$.BUILDING_TYPE));
    Page<Permit> result = repository.search(q, pageRequest);

    assertThat(result.getTotalPages()).isEqualTo(1);
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent()).containsExactly(permit2, permit1);
  }

  /**
   * "FT.SEARCH" "com.redis.om.spring.annotations.document.fixtures.PermitIdx" "house|loft" "SORTBY" "permitTimestamp" "DESC"
   */
  @Test
  void testFullTextSearchWithSortByDateFieldDesc() {
    String q = "house|loft";
    Pageable pageRequest = PageRequest.of(0, 10).withSort(Sort.by(Direction.DESC, Permit$.PERMIT_TIMESTAMP));
    Page<Permit> result = repository.search(q, pageRequest);

    assertThat(result.getTotalPages()).isEqualTo(1);
    assertThat(result.getTotalElements()).isEqualTo(3);
    assertThat(result.getContent()).containsExactly(permit3, permit2, permit1);
  }

  /**
   * "FT.SEARCH" "com.redis.om.spring.annotations.document.fixtures.PermitIdx" "house|loft" "SORTBY" "permitTimestamp" "ASC"
   */
  @Test
  void testFullTextSearchWithSortByDateFieldAsc() {
    String q = "house|loft";
    Pageable pageRequest = PageRequest.of(0, 10).withSort(Sort.by(Direction.ASC, Permit$.PERMIT_TIMESTAMP));
    Page<Permit> result = repository.search(q, pageRequest);

    assertThat(result.getTotalPages()).isEqualTo(1);
    assertThat(result.getTotalElements()).isEqualTo(3);
    assertThat(result.getContent()).containsExactly(permit1, permit2, permit3);
  }

  /**
   * "FT.SEARCH" "com.redis.om.spring.annotations.document.fixtures.PermitIdx" "house|loft" "SORTBY" "address_city" "DESC"
   */
  @Test
  void testFullTextSearchWithSortByNestedTextTagFieldDesc() {
    String q = "house|loft";
    Pageable pageRequest = PageRequest.of(0, 10).withSort(Sort.by(Direction.DESC, Permit$.ADDRESS_CITY));
    Page<Permit> result = repository.search(q, pageRequest);

    assertThat(result.getTotalPages()).isEqualTo(1);
    assertThat(result.getTotalElements()).isEqualTo(3);
    assertThat(result.getContent()).containsExactly(permit2, permit1, permit3);
  }

  /**
   * "FT.SEARCH" "com.redis.om.spring.annotations.document.fixtures.PermitIdx" "house|loft" "SORTBY" "address_city" "ASC"
   */
  @Test
  void testFullTextSearchWithSortByNestedTextTagAsc() {
    String q = "house|loft";
    Pageable pageRequest = PageRequest.of(0, 10).withSort(Sort.by(Direction.ASC, Permit$.ADDRESS_CITY));
    Page<Permit> result = repository.search(q, pageRequest);

    assertThat(result.getTotalPages()).isEqualTo(1);
    assertThat(result.getTotalElements()).isEqualTo(3);
    assertThat(result.getContent()).containsExactly(permit3, permit1, permit2);
  }

  @Test
  void testListInsideAListTagSearch() {
    List<Complex> withYarborough = es.of(Complex.class).filter(Complex$.MY_LIST.INNER_LIST.in("Yarborough"))
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(withYarborough.size()).isEqualTo(2), //
      () -> assertThat(withYarborough).extracting("id").containsExactlyInAnyOrder("complex1", "complex3") //
    );

    List<Complex> withComeuppance = es.of(Complex.class).filter(Complex$.MY_LIST.INNER_LIST.in("Comeuppance"))
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(withComeuppance.size()).isEqualTo(2), //
      () -> assertThat(withComeuppance).extracting("id").containsExactlyInAnyOrder("complex1", "complex2") //
    );

    List<Complex> withTaradiddle = es.of(Complex.class).filter(Complex$.MY_LIST.INNER_LIST.in("Taradiddle"))
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(withTaradiddle.size()).isEqualTo(2), //
      () -> assertThat(withTaradiddle).extracting("id").containsExactlyInAnyOrder("complex2", "complex3") //
    );
  }

  // UUID Tests
  @Test
  void testFindByNestedListOfUUIDsValuesIn() {
    SearchStream<WithNestedListOfUUIDs> stream1 = es.of(WithNestedListOfUUIDs.class);

    List<WithNestedListOfUUIDs> results1 = stream1 //
      .filter(WithNestedListOfUUIDs$.UUIDS.in(uuid1)).collect(Collectors.toList());

    List<String> ids1 = results1.stream().map(WithNestedListOfUUIDs::getId).collect(Collectors.toList());
    assertThat(ids1).contains("withNestedListOfUUIDs1");

    SearchStream<WithNestedListOfUUIDs> stream2 = es.of(WithNestedListOfUUIDs.class);

    List<WithNestedListOfUUIDs> results2 = stream2 //
      .filter(WithNestedListOfUUIDs$.UUIDS.in(uuid1, uuid4)).collect(Collectors.toList());

    List<String> ids2 = results2.stream().map(WithNestedListOfUUIDs::getId).collect(Collectors.toList());
    assertThat(ids2).contains("withNestedListOfUUIDs1", "withNestedListOfUUIDs2");

    SearchStream<WithNestedListOfUUIDs> stream3 = es.of(WithNestedListOfUUIDs.class);

    List<WithNestedListOfUUIDs> results3 = stream3 //
      .filter(WithNestedListOfUUIDs$.UUIDS.in(uuid1, uuid4)).collect(Collectors.toList());

    List<String> ids3 = results3.stream().map(WithNestedListOfUUIDs::getId).collect(Collectors.toList());
    assertThat(ids3).contains("withNestedListOfUUIDs1", "withNestedListOfUUIDs2");
  }

  @Test
  void testFindByNestedListOfUUIDsValuesNotEq() {
    SearchStream<WithNestedListOfUUIDs> stream1 = es.of(WithNestedListOfUUIDs.class);

    List<WithNestedListOfUUIDs> results1 = stream1 //
      .filter(WithNestedListOfUUIDs$.UUIDS.notEq(uuid1, uuid2)).collect(Collectors.toList());

    List<String> ids1 = results1.stream().map(WithNestedListOfUUIDs::getId).collect(Collectors.toList());
    assertThat(ids1).containsExactly("withNestedListOfUUIDs2");

    SearchStream<WithNestedListOfUUIDs> stream2 = es.of(WithNestedListOfUUIDs.class);

    List<WithNestedListOfUUIDs> results2 = stream2 //
      .filter(WithNestedListOfUUIDs$.UUIDS.notEq(uuid4, uuid5)).collect(Collectors.toList());

    List<String> ids2 = results2.stream().map(WithNestedListOfUUIDs::getId).collect(Collectors.toList());
    assertThat(ids2).containsExactly("withNestedListOfUUIDs1");

    SearchStream<WithNestedListOfUUIDs> stream3 = es.of(WithNestedListOfUUIDs.class);

    List<WithNestedListOfUUIDs> results3 = stream3 //
      .filter(WithNestedListOfUUIDs$.UUIDS.notEq(uuid1, uuid4)).collect(Collectors.toList());

    List<String> ids3 = results3.stream().map(WithNestedListOfUUIDs::getId).collect(Collectors.toList());
    assertThat(ids3).isEmpty();
  }

  // ULIDs Tests

  @Test
  void testFindByNestedListOfUlidsValuesIn() {
    SearchStream<WithNestedListOfUlids> stream1 = es.of(WithNestedListOfUlids.class);

    List<WithNestedListOfUlids> results1 = stream1 //
      .filter(WithNestedListOfUlids$.ULIDS.in(ulid1)).collect(Collectors.toList());

    List<String> ids1 = results1.stream().map(WithNestedListOfUlids::getId).collect(Collectors.toList());
    assertThat(ids1).contains("withNestedListOfUlids1");

    SearchStream<WithNestedListOfUlids> stream2 = es.of(WithNestedListOfUlids.class);

    List<WithNestedListOfUlids> results2 = stream2 //
      .filter(WithNestedListOfUlids$.ULIDS.in(ulid1, ulid4)).collect(Collectors.toList());

    List<String> ids2 = results2.stream().map(WithNestedListOfUlids::getId).collect(Collectors.toList());
    assertThat(ids2).contains("withNestedListOfUlids1", "withNestedListOfUlids2");

    SearchStream<WithNestedListOfUlids> stream3 = es.of(WithNestedListOfUlids.class);

    List<WithNestedListOfUlids> results3 = stream3 //
      .filter(WithNestedListOfUlids$.ULIDS.in(ulid1, ulid4)).collect(Collectors.toList());

    List<String> ids3 = results3.stream().map(WithNestedListOfUlids::getId).collect(Collectors.toList());
    assertThat(ids3).contains("withNestedListOfUlids1", "withNestedListOfUlids2");
  }

  @Test
  void testFindByNestedListOfUlidsValuesNotEq() {
    SearchStream<WithNestedListOfUlids> stream1 = es.of(WithNestedListOfUlids.class);

    List<WithNestedListOfUlids> results1 = stream1 //
      .filter(WithNestedListOfUlids$.ULIDS.notEq(ulid1, ulid2)).collect(Collectors.toList());

    List<String> ids1 = results1.stream().map(WithNestedListOfUlids::getId).collect(Collectors.toList());
    assertThat(ids1).containsExactly("withNestedListOfUlids2");

    SearchStream<WithNestedListOfUlids> stream2 = es.of(WithNestedListOfUlids.class);

    List<WithNestedListOfUlids> results2 = stream2 //
      .filter(WithNestedListOfUlids$.ULIDS.notEq(ulid4, ulid5)).collect(Collectors.toList());

    List<String> ids2 = results2.stream().map(WithNestedListOfUlids::getId).collect(Collectors.toList());
    assertThat(ids2).containsExactly("withNestedListOfUlids1");

    SearchStream<WithNestedListOfUlids> stream3 = es.of(WithNestedListOfUlids.class);

    List<WithNestedListOfUlids> results3 = stream3 //
      .filter(WithNestedListOfUlids$.ULIDS.notEq(ulid1, ulid4)).collect(Collectors.toList());

    List<String> ids3 = results3.stream().map(WithNestedListOfUlids::getId).collect(Collectors.toList());
    assertThat(ids3).isEmpty();
  }
}
