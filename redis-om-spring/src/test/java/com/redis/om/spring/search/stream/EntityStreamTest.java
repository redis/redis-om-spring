package com.redis.om.spring.search.stream;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.*;
import com.redis.om.spring.tuple.Fields;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.tuple.Quad;
import com.redis.om.spring.tuple.Triple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import redis.clients.jedis.search.aggr.SortedField.SortOrder;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection") class EntityStreamTest extends AbstractBaseDocumentTest {
  @Autowired CompanyRepository repository;

  @Autowired UserRepository userRepository;

  // repository with an entity with only one indexed field
  @Autowired NiCompanyRepository nicRepository;

  @Autowired EntityStream entityStream;

  @BeforeEach void cleanUp() {
    if (repository.count() == 0) {
      Company redis = repository.save(Company.of( //
          "RedisInc", 2011, //
          LocalDate.of(2021, 5, 1), //
          new Point(-122.066540, 37.377690), "stack@redis.com" //
      ));
      redis.setTags(Set.of("fast", "scalable", "reliable", "database", "nosql"));

      Set<Employee> employees = Sets.newHashSet(Employee.of("Brian Sam-Bodden"), Employee.of("Guy Royse"),
          Employee.of("Justin Castilla"));
      redis.setEmployees(employees);

      Company microsoft = repository.save(Company.of(//
          "Microsoft", 1975, //
          LocalDate.of(2022, 8, 15), //
          new Point(-122.124500, 47.640160), "research@microsoft.com" //
      ));
      microsoft.setTags(Set.of("innovative", "reliable", "os", "ai"));

      Company tesla = repository.save(Company.of( //
          "Tesla", 2003, //
          LocalDate.of(2022, 1, 1), //
          new Point(-97.6208903, 30.2210767), "elon@tesla.com" //
      ));
      tesla.setTags(Set.of("innovative", "futuristic", "ai"));

      repository.saveAll(List.of(redis, microsoft, tesla));
    }

    // users
    if (userRepository.count() == 0) {
      List<User> users = List.of(User.of("Steve Lorello", .9999), User.of("Nava Levy", 1234.5678),
          User.of("Savannah Norem", 999.99), User.of("Suze Shardlow", 899.0));
      for (User user : users) {
        user.setRoles(List.of("devrel", "educator", "guru"));
      }
      userRepository.saveAll(users);
    }

    // partially non-indexed companies
    if (nicRepository.count() == 0) {
      NiCompany niRedis = nicRepository.save(NiCompany.of( //
           "RedisInc", 2011, //
          LocalDate.of(2021, 5, 1), //
          new Point(-122.066540, 37.377690), "stack@redis.com" //
      ));
      niRedis.setTags(List.of("fast", "scalable", "reliable", "database", "nosql"));

      NiCompany niMicrosoft = nicRepository.save(NiCompany.of( //
          "Microsoft", 1975, //
          LocalDate.of(2022, 8, 15), //
          new Point(-122.124500, 47.640160), "research@microsoft.com" //
      ));
      niMicrosoft.setTags(List.of("innovative", "reliable", "os", "ai"));

      NiCompany niTesla = nicRepository.save(NiCompany.of( //
          "Tesla", 2003, //
          LocalDate.of(2022, 1, 1), //
          new Point(-97.6208903, 30.2210767), "elon@tesla.com" //
      ));
      niTesla.setTags(List.of("innovative", "futuristic", "ai"));

      nicRepository.saveAll(List.of(niRedis, niMicrosoft, niTesla));
    }
  }

  @Test void testStreamSelectAll() {
    SearchStream<Company> stream = entityStream.of(Company.class);
    List<Company> allCompanies = stream.collect(Collectors.toList());

    List<String> names = allCompanies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("RedisInc", "Microsoft", "Tesla");
  }

  @Test void testFindByOnePropertyEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.NAME.eq("RedisInc")) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("RedisInc");
  }

  @Test void testFindByOnePropertyNotEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.NAME.notEq("RedisInc")) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("Microsoft", "Tesla");
  }

  @Test void testFindByTwoPropertiesOredEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter( //
            Company$.NAME.eq("RedisInc") //
                .or(Company$.NAME.eq("Microsoft")) //
        ) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("RedisInc", "Microsoft");
  }

  @Test void testFindByThreePropertiesOredEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter( //
            Company$.NAME.eq("RedisInc") //
                .or(Company$.NAME.eq("Microsoft")) //
                .or(Company$.NAME.eq("Tesla")) //
        ) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("RedisInc", "Microsoft", "Tesla");
  }

  @Test void testFindByThreePropertiesOrList() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.NAME.in("RedisInc", "Microsoft", "Tesla")).collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("RedisInc", "Microsoft", "Tesla");
  }

  @Test void testFindByThreePropertiesOrList2() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.NAME.in("RedisInc", "Tesla")).collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("RedisInc", "Tesla");
  }

  @Test void testFindByThreeNumericPropertiesOrList() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.in(2011, 1975, 2003)).collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("RedisInc", "Microsoft", "Tesla");
  }

  @Test void testFindByTwoPropertiesAndedNotEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter( //
            Company$.NAME.notEq("RedisInc") //
                .and(Company$.NAME.notEq("Microsoft")) //
        ) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("Tesla");
  }

  @Test void testFindByNumericPropertyEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.eq(2011)) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("RedisInc");
  }

  @Test void testFindByNumericPropertyNotEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.notEq(2011)) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("Tesla", "Microsoft");
  }

  @Test void testFindByNumericPropertyGreaterThan() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.gt(2000)) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("RedisInc", "Tesla");
  }

  @Test void testFindByNumericPropertyGreaterThanOrEqual() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.ge(2011)) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("RedisInc");
  }

  @Test void testFindByNumericPropertyLessThan() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.lt(2000)) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("Microsoft");
  }

  @Test void testFindByNumericPropertyLessThanOrEqual() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.le(1975)) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("Microsoft");
  }

  @Test void testFindByNumericPropertyBetween() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.between(1976, 2010)) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).contains("Tesla");
  }

  @Test void testCount() {
    long count = entityStream //
        .of(Company.class) //
        .filter( //
            Company$.NAME.notEq("RedisInc") //
                .and(Company$.NAME.notEq("Microsoft")) //
        ).count();

    assertEquals(1, count);
  }

  @Test void testLimit() {
    List<Company> companies = entityStream //
        .of(Company.class) //
        .limit(2).collect(Collectors.toList());

    assertEquals(2, companies.size());
  }

  @Test void testSkip() {
    List<Company> companies = entityStream //
        .of(Company.class) //
        .skip(1).collect(Collectors.toList());

    assertEquals(2, companies.size());
  }

  @Test void testSortDefaultAscending() {
    List<Company> companies = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());

    assertThat(names).containsExactly("Microsoft", "RedisInc", "Tesla");
  }

  @Test void testSortAscending() {
    List<Company> companies = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.ASC) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).containsExactly("Microsoft", "RedisInc", "Tesla");
  }

  @Test void testSortDescending() {
    List<Company> companies = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).containsExactly("Tesla", "RedisInc", "Microsoft");
  }

  @Test void testForEachOrdered() {
    List<Company> companies = new ArrayList<>();
    Consumer<? super Company> testConsumer = companies::add;
    entityStream //
        .of(Company.class) //
        .forEachOrdered(testConsumer);

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());

    assertThat(names).containsExactly("RedisInc", "Microsoft", "Tesla");
  }

  @Test void testMapToOneProperty() {
    List<String> names = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertThat(names).containsExactly("Tesla", "RedisInc", "Microsoft");
  }

  @Test void testMapToTwoProperties() {
    List<Pair<String, Integer>> results = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Fields.of(Company$.NAME, Company$.YEAR_FOUNDED)) //
        .collect(Collectors.toList());

    assertEquals(3, results.size());

    assertEquals("Tesla", results.get(0).getFirst());
    assertEquals("RedisInc", results.get(1).getFirst());
    assertEquals("Microsoft", results.get(2).getFirst());

    assertEquals(2003, results.get(0).getSecond());
    assertEquals(2011, results.get(1).getSecond());
    assertEquals(1975, results.get(2).getSecond());
  }

  @Test void testMapToThreeProperties() {
    List<Triple<String, Integer, Point>> results = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Fields.of(Company$.NAME, Company$.YEAR_FOUNDED, Company$.LOCATION)) //
        .collect(Collectors.toList());

    assertEquals(3, results.size());

    assertEquals("Tesla", results.get(0).getFirst());
    assertEquals("RedisInc", results.get(1).getFirst());
    assertEquals("Microsoft", results.get(2).getFirst());

    assertEquals(2003, results.get(0).getSecond());
    assertEquals(2011, results.get(1).getSecond());
    assertEquals(1975, results.get(2).getSecond());

    assertEquals(results.get(0).getThird(), new Point(-97.6208903, 30.2210767));
    assertEquals(results.get(1).getThird(), new Point(-122.066540, 37.377690));
    assertEquals(results.get(2).getThird(), new Point(-122.124500, 47.640160));
  }

  @Test void testMapToFourPropertiesOneNotIndexed() {
    List<Quad<String, Integer, Point, Date>> results = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Fields.of(Company$.NAME, Company$.YEAR_FOUNDED, Company$.LOCATION, Company$.CREATED_DATE)) //
        .collect(Collectors.toList());

    assertEquals(3, results.size());

    assertEquals("Tesla", results.get(0).getFirst());
    assertEquals("RedisInc", results.get(1).getFirst());
    assertEquals("Microsoft", results.get(2).getFirst());

    assertEquals(2003, results.get(0).getSecond());
    assertEquals(2011, results.get(1).getSecond());
    assertEquals(1975, results.get(2).getSecond());

    assertEquals(results.get(0).getThird(), new Point(-97.6208903, 30.2210767));
    assertEquals(results.get(1).getThird(), new Point(-122.066540, 37.377690));
    assertEquals(results.get(2).getThird(), new Point(-122.124500, 47.640160));

    assertNotNull(results.get(0).getFourth());
    assertNotNull(results.get(1).getFourth());
    assertNotNull(results.get(2).getFourth());
  }

  @Test void testGeoNearPredicate() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.LOCATION.near(new Point(-122.064, 37.384), new Distance(30, Metrics.MILES))) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertThat(names).containsExactly("RedisInc");
  }

  @Test void testGeoOutsideOfPredicate() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.LOCATION.outsideOf(new Point(-122.064, 37.384), new Distance(30, Metrics.MILES))) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertThat(names).containsExactly("Tesla", "Microsoft");
  }

  @Test void testGeoEqPredicateUsingPoint() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.LOCATION.eq(new Point(-122.066540, 37.377690))) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertThat(names).containsExactly("RedisInc");
  }

  @Test void testGeoEqPredicateUsingCSV() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.LOCATION.eq("-122.066540, 37.377690")) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(1, names.size());

    assertEquals("RedisInc", names.get(0));
  }

  @Test void testGeoEqPredicateUsingDoubles() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.LOCATION.eq(-122.066540, 37.377690)) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(1, names.size());

    assertEquals("RedisInc", names.get(0));
  }

  @Test void testGeoNotEqPredicateUsingPoint() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.LOCATION.notEq(new Point(-122.066540, 37.377690))) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(2, names.size());

    assertEquals("Tesla", names.get(0));
    assertEquals("Microsoft", names.get(1));
  }

  @Test void testGeoNotEqPredicateUsingCSV() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.LOCATION.notEq("-122.066540, 37.377690")) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(2, names.size());

    assertEquals("Tesla", names.get(0));
    assertEquals("Microsoft", names.get(1));
  }

  @Test void testGeoNotEqPredicateUsingDoubles() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.LOCATION.notEq(-122.066540, 37.377690)) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(2, names.size());

    assertEquals("Tesla", names.get(0));
    assertEquals("Microsoft", names.get(1));
  }

  @Test void testFindByTextLike() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.NAME.like("Micros")) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).toList();

    assertTrue(names.contains("Microsoft"));
  }

  @Test void testFindByTextNotLike() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.NAME.notLike("Micros")) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(2, names.size());

    assertTrue(names.contains("Tesla"));
    assertTrue(names.contains("RedisInc"));
  }

  @Test void testFindByTextContaining() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.NAME.containing("Micros")) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).toList();

    assertTrue(names.contains("Microsoft"));
  }

  @Test void testFindByTextNotContaining() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.NAME.notContaining("Micros")) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(2, names.size());

    assertTrue(names.contains("Tesla"));
    assertTrue(names.contains("RedisInc"));
  }

  @Test void testFindByTextThatStartsWith() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.NAME.startsWith("Mic")) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).toList();

    assertTrue(names.contains("Microsoft"));
  }

  @Test void testFindByTagsIn() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.TAGS.in("reliable")) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(2, names.size());

    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
  }

  @Test void testFindByTagsIn2() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.TAGS.in("reliable", "ai")) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(3, names.size());

    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test void testFindByTagsContainingAll() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.TAGS.containsAll("fast", "scalable", "reliable", "database", "nosql")) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(1, names.size());

    assertTrue(names.contains("RedisInc"));
  }

  @Test void testFindByTagsEquals() {
    Set<String> tags = Set.of("fast", "scalable", "reliable", "database", "nosql");
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.TAGS.eq(tags)) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(1, names.size());

    assertTrue(names.contains("RedisInc"));
  }

  @Test void testFindByTagsNotEqualsSingleValue() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.TAGS.notEq("ai")) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(1, names.size());
    assertTrue(names.contains("RedisInc"));
  }

  @Test void testFindByTagsNotEquals() {
    Set<String> tags = Set.of("fast", "scalable", "reliable", "database", "nosql");
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.TAGS.notEq(tags)) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(1, names.size());
    assertTrue(names.contains("Tesla"));
  }

  @Test void testFindByTagsContainsNone() {
    Set<String> tags = Set.of("innovative");
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.TAGS.containsNone(tags)) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(1, names.size());

    assertTrue(names.contains("RedisInc"));
  }

  @Test void testFindFirst() {
    Optional<Company> maybeCompany = entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .findFirst();

    assertTrue(maybeCompany.isPresent());
    assertEquals("RedisInc", maybeCompany.get().getName());
  }

  @Test void testFindAny() {
    Optional<Company> maybeCompany = entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .findAny();

    assertTrue(maybeCompany.isPresent());
    assertEquals("RedisInc", maybeCompany.get().getName());
  }

  @Test void testToggleBooleanFieldInDocuments() {
    Optional<Company> maybeRedisBefore = repository.findFirstByName("RedisInc");
    assertTrue(maybeRedisBefore.isPresent());
    assertFalse(maybeRedisBefore.get().isPubliclyListed());

    entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .forEach(Company$.PUBLICLY_LISTED.toggle());

    Optional<Company> maybeRedisAfter = repository.findFirstByName("RedisInc");
    assertTrue(maybeRedisAfter.isPresent());
    assertTrue(maybeRedisAfter.get().isPubliclyListed());
    flushSearchIndexFor(Company.class);
  }

  @Test void testNumIncrByNumericFieldInDocuments() {
    Optional<Company> maybeRedisBefore = repository.findFirstByName("RedisInc");
    assertTrue(maybeRedisBefore.isPresent());
    assertEquals(2011, maybeRedisBefore.get().getYearFounded());

    entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .forEach(Company$.YEAR_FOUNDED.incrBy(5L));

    Optional<Company> maybeRedisAfter = repository.findFirstByName("RedisInc");
    assertTrue(maybeRedisAfter.isPresent());
    assertEquals(2016, maybeRedisAfter.get().getYearFounded());
    flushSearchIndexFor(Company.class);
  }

  @Test void testNumDecrByNumericFieldInDocuments() {
    Optional<Company> maybeRedisBefore = repository.findFirstByName("RedisInc");
    assertTrue(maybeRedisBefore.isPresent());
    assertEquals(2011, maybeRedisBefore.get().getYearFounded());

    entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .forEach(Company$.YEAR_FOUNDED.decrBy(2L));

    Optional<Company> maybeRedisAfter = repository.findFirstByName("RedisInc");
    assertTrue(maybeRedisAfter.isPresent());
    assertEquals(2009, maybeRedisAfter.get().getYearFounded());
    flushSearchIndexFor(Company.class);
  }

  @Test void testStrAppendToIndexedTextFieldInDocuments() {
    entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("Microsoft")) //
        .forEach(Company$.NAME.append(" Corp"));

    Optional<Company> maybeMicrosoft = repository.findFirstByEmail("research@microsoft.com");
    assertTrue(maybeMicrosoft.isPresent());
    assertThat(maybeMicrosoft.get().getName()).isEqualTo("Microsoft Corp");
    flushSearchIndexFor(Company.class);
  }

  @Test void testStrLenToIndexedTextFieldInDocuments() {
    List<Long> emailLengths = entityStream.of(Company.class) //
        .map(Company$.NAME.length()) //
        .collect(Collectors.toList());
    assertAll( //
        () -> assertThat(emailLengths).hasSize(3), //
        () -> assertThat(emailLengths).containsExactly(8L, 9L, 5L) //
    );
  }

  @Test void testFindByTagEscapesChars() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.EMAIL.eq("stack@redis.com")) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).toList();
    assertTrue(names.contains("RedisInc"));
  }

  @Test void testArrayAppendToSimpleIndexedTagFieldInDocuments() {
    entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("Microsoft")) //
        .forEach(Company$.TAGS.add("gaming"));

    Optional<Company> maybeMicrosoft = repository.findFirstByEmail("research@microsoft.com");
    assertTrue(maybeMicrosoft.isPresent());
    Company microsoft = maybeMicrosoft.get();
    assertThat(microsoft.getTags()).contains("innovative", "reliable", "os", "ai", "gaming");
    flushSearchIndexFor(Company.class);
  }

  @Test void testArrayAppendToComplexIndexedTagFieldInDocuments() {
    entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .forEach(Company$.EMPLOYEES.add(Employee.of("Simon Prickett")));

    Optional<Company> maybeRedis = repository.findFirstByEmail("stack@redis.com");
    assertTrue(maybeRedis.isPresent());
    Company microsoft = maybeRedis.get();

    List<String> employeeNames = microsoft.getEmployees().stream().map(Employee::getName).collect(Collectors.toList());
    assertThat(employeeNames).contains("Brian Sam-Bodden", "Guy Royse", "Justin Castilla", "Simon Prickett");
    flushSearchIndexFor(Company.class);
  }

  @Test void testArrayInsertToSimpleIndexedTagFieldInDocuments() {
    entityStream.of(User.class) //
        .filter(User$.NAME.eq("Steve Lorello")) //
        .forEach(User$.ROLES.insert("dotnet", 0));

    Optional<User> maybeSteve = userRepository.findFirstByName("Steve Lorello");
    assertTrue(maybeSteve.isPresent());
    User steve = maybeSteve.get();
    assertThat(steve.getRoles()).containsExactly("dotnet", "devrel", "educator", "guru");
    flushSearchIndexFor(User.class);
  }

  @Test void testArrayPrependToSimpleIndexedTagFieldInDocuments() {
    entityStream.of(User.class) //
        .filter(User$.NAME.eq("Steve Lorello")) //
        .forEach(User$.ROLES.prepend("dotnet"));

    Optional<User> maybeSteve = userRepository.findFirstByName("Steve Lorello");
    assertTrue(maybeSteve.isPresent());
    User steve = maybeSteve.get();
    assertThat(steve.getRoles()).containsExactly("dotnet", "devrel", "educator", "guru");
    flushSearchIndexFor(User.class);
  }

  @Test void testArrayInsertToSimpleIndexedTagFieldInDocuments2() {
    entityStream.of(User.class) //
        .filter(User$.NAME.eq("Steve Lorello")) //
        .forEach(User$.ROLES.insert("dotnet", 1));

    Optional<User> maybeSteve = userRepository.findFirstByName("Steve Lorello");
    assertTrue(maybeSteve.isPresent());
    User steve = maybeSteve.get();
    assertThat(steve.getRoles()).containsExactly("devrel", "dotnet", "educator", "guru");
    flushSearchIndexFor(User.class);
  }

  @Test void testArrayLenToSimpleIndexedTagFieldInDocuments() {
    List<Long> rolesLengths = entityStream.of(User.class) //
        .map(User$.ROLES.length()) //
        .collect(Collectors.toList());
    assertAll( //
        () -> assertThat(rolesLengths).hasSize(4), //
        () -> assertThat(rolesLengths).containsExactly(3L, 3L, 3L, 3L) //
    );
  }

  @Test void testArrayIndexOfOnSimpleIndexedTagFieldInDocuments() {
    List<Long> rolesLengths = entityStream.of(User.class) //
        .filter(User$.NAME.eq("Steve Lorello")) //
        .map(User$.ROLES.indexOf("guru")) //
        .collect(Collectors.toList());
    assertAll( //
        () -> assertThat(rolesLengths).hasSize(1), //
        () -> assertThat(rolesLengths).containsExactly(2L) //
    );
  }

  @Test void testArrayPopOnSimpleIndexedTagFieldInDocuments() {
    List<Object> roles = entityStream.of(User.class) //
        .filter(User$.NAME.eq("Steve Lorello")) //
        .map(User$.ROLES.pop()) //
        .collect(Collectors.toList());

    // contains the last
    assertThat(roles).containsExactly("guru");

    Optional<User> maybeSteve = userRepository.findFirstByName("Steve Lorello");
    assertTrue(maybeSteve.isPresent());
    User steve = maybeSteve.get();

    // the rest remains
    assertThat(steve.getRoles()).containsExactly("devrel", "educator");

    flushSearchIndexFor(User.class);
  }

  @Test void testArrayRemoveLastOnSimpleIndexedTagFieldInDocuments() {
    List<Object> roles = entityStream.of(User.class) //
        .filter(User$.NAME.eq("Steve Lorello")) //
        .map(User$.ROLES.removeLast()) //
        .collect(Collectors.toList());

    // contains the last
    assertThat(roles).containsExactly("guru");

    Optional<User> maybeSteve = userRepository.findFirstByName("Steve Lorello");
    assertTrue(maybeSteve.isPresent());
    User steve = maybeSteve.get();

    // the first remains
    assertThat(steve.getRoles()).containsExactly("devrel", "educator");
    flushSearchIndexFor(User.class);
  }

  @Test void testArrayPopFirstOnSimpleIndexedTagFieldInDocuments() {
    List<Object> roles = entityStream.of(User.class) //
        .filter(User$.NAME.eq("Steve Lorello")) //
        .map(User$.ROLES.pop(0)) //
        .collect(Collectors.toList());

    // contains the last
    assertThat(roles).containsExactly("devrel");

    Optional<User> maybeSteve = userRepository.findFirstByName("Steve Lorello");
    assertTrue(maybeSteve.isPresent());
    User steve = maybeSteve.get();

    // the rest remains
    assertThat(steve.getRoles()).containsExactly("educator", "guru");

    flushSearchIndexFor(User.class);
  }

  @Test void testArrayRemoveFirstOnSimpleIndexedTagFieldInDocuments() {
    List<Object> roles = entityStream.of(User.class) //
        .filter(User$.NAME.eq("Steve Lorello")) //
        .map(User$.ROLES.removeFirst()) //
        .collect(Collectors.toList());

    // contains the first
    assertThat(roles).containsExactly("devrel");

    Optional<User> maybeSteve = userRepository.findFirstByName("Steve Lorello");
    assertTrue(maybeSteve.isPresent());
    User steve = maybeSteve.get();

    // the rest remains
    assertThat(steve.getRoles()).containsExactly("educator", "guru");

    flushSearchIndexFor(User.class);
  }

  @Test void testArrayRemoveByIndexOnSimpleIndexedTagFieldInDocuments() {
    List<Object> roles = entityStream.of(User.class) //
        .filter(User$.NAME.eq("Steve Lorello")) //
        .map(User$.ROLES.remove(0)) //
        .collect(Collectors.toList());

    // contains the first
    assertThat(roles).containsExactly("devrel");

    Optional<User> maybeSteve = userRepository.findFirstByName("Steve Lorello");
    assertTrue(maybeSteve.isPresent());
    User steve = maybeSteve.get();

    // the rest remains
    assertThat(steve.getRoles()).containsExactly("educator", "guru");

    flushSearchIndexFor(User.class);
  }

  @Test void testArrayTrimToRangeOnSimpleIndexedTagFieldInDocuments() {
    entityStream.of(User.class) //
        .filter(User$.NAME.eq("Steve Lorello")) //
        .forEach(User$.ROLES.trimToRange(1, 1));

    Optional<User> maybeSteve = userRepository.findFirstByName("Steve Lorello");
    assertTrue(maybeSteve.isPresent());
    User steve = maybeSteve.get();
    assertThat(steve.getRoles()).containsExactly("educator");
    flushSearchIndexFor(User.class);
  }

  @Test void testArrayTrimToRangeOnSimpleIndexedTagFieldInDocuments2() {
    entityStream.of(User.class) //
        .filter(User$.NAME.eq("Steve Lorello")) //
        .forEach(User$.ROLES.trimToRange(0, 0));

    Optional<User> maybeSteve = userRepository.findFirstByName("Steve Lorello");
    assertTrue(maybeSteve.isPresent());
    User steve = maybeSteve.get();
    assertThat(steve.getRoles()).containsExactly("devrel");
    flushSearchIndexFor(User.class);
  }

  @Test void testArrayTrimToRangeOnSimpleIndexedTagFieldInDocuments3() {
    entityStream.of(User.class) //
        .filter(User$.NAME.eq("Steve Lorello")) //
        .forEach(User$.ROLES.trimToRange(1, 2));

    Optional<User> maybeSteve = userRepository.findFirstByName("Steve Lorello");
    assertTrue(maybeSteve.isPresent());
    User steve = maybeSteve.get();
    assertThat(steve.getRoles()).containsExactly("educator", "guru");
    flushSearchIndexFor(User.class);
  }

  @Test void testArrayTrimToRangeOnSimpleIndexedTagFieldInDocuments4() {
    entityStream.of(User.class) //
        .filter(User$.NAME.eq("Steve Lorello")) //
        .forEach(User$.ROLES.trimToRange(1, -1));

    Optional<User> maybeSteve = userRepository.findFirstByName("Steve Lorello");
    assertTrue(maybeSteve.isPresent());
    User steve = maybeSteve.get();
    assertThat(steve.getRoles()).containsExactly("educator", "guru");
    flushSearchIndexFor(User.class);
  }

  @Test void testFindByDatePropertyEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.LAST_VALUATION.eq(LocalDate.of(2022, 1, 1))) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).containsExactly("Tesla");
  }

  @Test void testFindByDatePropertyNotEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.LAST_VALUATION.notEq(LocalDate.of(2021, 5, 1))) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertThat(names).containsExactly("Microsoft", "Tesla");
  }

  @Test void testFindByDatePropertyIsAfter() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.LAST_VALUATION.after(LocalDate.of(2021, 5, 2))) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).toList();
    assertThat(names).containsExactly("Microsoft", "Tesla");
  }

  @Test void testFindByDatePropertyIsOnOrAfter() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.LAST_VALUATION.onOrAfter(LocalDate.of(2022, 1, 1))) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).toList();
    assertThat(names).containsExactly("Microsoft", "Tesla");
  }

  @Test void testFindByDatePropertyBefore() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.LAST_VALUATION.before(LocalDate.of(2021, 6, 15))) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).toList();
    assertThat(names).containsExactly("RedisInc");
  }

  @Test void testFindByDatePropertyIsOnOrBefore() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.LAST_VALUATION.onOrBefore(LocalDate.of(2022, 1, 1))) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).toList();
    assertThat(names).containsExactly("RedisInc", "Tesla");
  }

  @Test void testFindByDatePropertyIsBetween() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.LAST_VALUATION.between(LocalDate.of(2021, 5, 1), LocalDate.of(2022, 8, 15))) //
        .collect(Collectors.toList());

    List<String> names = companies.stream().map(Company::getName).toList();
    assertThat(names).containsExactly("RedisInc", "Microsoft", "Tesla");
  }

  @Test void testFindByDoubleNumericPropertyEquals() {
    SearchStream<User> stream = entityStream.of(User.class);

    List<User> users = stream //
        .filter(User$.LOTTERY_WINNINGS.eq(.9999)) //
        .collect(Collectors.toList());

    List<String> names = users.stream().map(User::getName).toList();

    assertThat(names).containsExactly("Steve Lorello");
  }

  @Test void testFindByDoubleNumericPropertyNotEquals() {
    SearchStream<User> stream = entityStream.of(User.class);

    List<User> users = stream //
        .filter(User$.LOTTERY_WINNINGS.notEq(.9999)) //
        .collect(Collectors.toList());

    List<String> names = users.stream().map(User::getName).toList();
    assertThat(names).containsExactly("Nava Levy", "Savannah Norem", "Suze Shardlow");
  }

  @Test void testFindByDoubleNumericPropertyIn() {
    SearchStream<User> stream = entityStream.of(User.class);

    List<User> users = stream //
        .filter(User$.LOTTERY_WINNINGS.in(1234.5678, 999.99)) //
        .collect(Collectors.toList());

    List<String> names = users.stream().map(User::getName).toList();
    assertThat(names).containsExactly("Nava Levy", "Savannah Norem");
  }

  @Test void testFindByDoubleNumericPropertyBetween() {
    SearchStream<User> stream = entityStream.of(User.class);

    List<User> users = stream //
        .filter(User$.LOTTERY_WINNINGS.between(1.0, 900.0)) //
        .collect(Collectors.toList());

    List<String> names = users.stream().map(User::getName).toList();
    assertThat(names).containsExactly("Suze Shardlow");
  }

  @Test void testFindByDoubleNumericPropertyGreaterThan() {
    SearchStream<User> stream = entityStream.of(User.class);

    List<User> users = stream //
        .filter(User$.LOTTERY_WINNINGS.gt(900.0)) //
        .collect(Collectors.toList());

    List<String> names = users.stream().map(User::getName).toList();
    assertThat(names).containsExactly("Nava Levy", "Savannah Norem");
  }

  @Test void testFindByDoubleNumericPropertyGreaterThanOrEqual() {
    SearchStream<User> stream = entityStream.of(User.class);

    List<User> users = stream //
        .filter(User$.LOTTERY_WINNINGS.ge(899.0)) //
        .collect(Collectors.toList());

    List<String> names = users.stream().map(User::getName).toList();
    assertThat(names).containsExactly("Nava Levy", "Savannah Norem", "Suze Shardlow");
  }

  @Test void testFindByDoubleNumericPropertyLessThan() {
    SearchStream<User> stream = entityStream.of(User.class);

    List<User> users = stream //
        .filter(User$.LOTTERY_WINNINGS.lt(1.0)) //
        .collect(Collectors.toList());

    List<String> names = users.stream().map(User::getName).toList();
    assertThat(names).containsExactly("Steve Lorello");
  }

  @Test void testFindByDoubleNumericPropertyLessThanOrEqual() {
    SearchStream<User> stream = entityStream.of(User.class);

    List<User> users = stream //
        .filter(User$.LOTTERY_WINNINGS.le(899.0)) //
        .collect(Collectors.toList());

    assertEquals(2, users.size());

    List<String> names = users.stream().map(User::getName).toList();
    assertTrue(names.contains("Steve Lorello"));
    assertTrue(names.contains("Suze Shardlow"));
  }

  @Test void testEntityWithoutIdThrowsException() {
    IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> entityStream.of(DocWithoutId.class));

    String expectedErrorMessage = String.format("%s does not appear to have an ID field", DocWithoutId.class.getName());
    Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test void testFilterWithNonSearchFieldPredicateIsNoop() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(c -> c.toString().equals("foo")) //
        .collect(Collectors.toList());

    assertEquals(repository.count(), companies.size());
  }

  @Test void testMapToIntOnReturnFields() {
    IntStream intStream = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .mapToInt(i -> i);

    assertThat(intStream.boxed().collect(Collectors.toList())).contains(2011, 1975, 2003);
  }

  @Test void testMapToInt() {
    IntStream intStream = entityStream //
        .of(Company.class) //
        .mapToInt(Company::getYearFounded);

    assertThat(intStream.boxed().collect(Collectors.toList())).contains(2011, 1975, 2003);
  }

  @Test void testMapToLongOnReturnFields() {
    LongStream longStream = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .mapToLong(i -> i);

    assertThat(longStream.boxed().collect(Collectors.toList())).contains(2011L, 1975L, 2003L);
  }

  @Test void testMapToLong() {
    LongStream longStream = entityStream //
        .of(Company.class) //
        .mapToLong(Company::getYearFounded);

    assertThat(longStream.boxed().collect(Collectors.toList())).contains(2011L, 1975L, 2003L);
  }

  @Test void testMapToDoubleOnReturnFields() {
    DoubleStream doubleStream = entityStream //
        .of(User.class) //
        .map(User$.LOTTERY_WINNINGS) //
        .mapToDouble(w -> w);

    assertThat(doubleStream.boxed().collect(Collectors.toList())).contains(.9999, 1234.5678, 999.99, 899.0);
  }

  @Test void testMapToDouble() {
    DoubleStream doubleStream = entityStream //
        .of(User.class) //
        .mapToDouble(User::getLotteryWinnings);

    assertThat(doubleStream.boxed().collect(Collectors.toList())).contains(.9999, 1234.5678, 999.99, 899.0);
  }

  @Test void testFlatMapToInt() {
    // expected
    List<Integer> expected = new ArrayList<>();
    for (Company company : entityStream.of(Company.class).collect(Collectors.toList())) {
      for (String tag : company.getTags()) {
        expected.add(tag.length());
      }
    }

    // actual
    IntStream tagLengthIntStream = entityStream //
        .of(Company.class) //
        .flatMapToInt(c -> c.getTags().stream().mapToInt(String::length));

    List<Integer> actual = tagLengthIntStream.boxed().collect(Collectors.toList());

    assertThat(actual).containsExactlyElementsOf(expected);
  }

  @Test void testFlatMapToLong() {
    // expected
    List<Long> expected = new ArrayList<>();
    for (Company company : entityStream.of(Company.class).collect(Collectors.toList())) {
      for (String tag : company.getTags()) {
        expected.add((long) tag.length());
      }
    }

    // actual
    LongStream tagLengthIntStream = entityStream //
        .of(Company.class) //
        .flatMapToLong(c -> c.getTags().stream().mapToLong(String::length));

    List<Long> actual = tagLengthIntStream.boxed().collect(Collectors.toList());

    assertThat(actual).containsExactlyElementsOf(expected);
  }

  @Test void testFlatMapToDouble() {
    // expected
    List<Double> expected = new ArrayList<>();
    for (Company company : entityStream.of(Company.class).collect(Collectors.toList())) {
      for (String tag : company.getTags()) {
        expected.add((double) tag.length());
      }
    }

    // actual
    DoubleStream tagLengthDoubleStream = entityStream //
        .of(Company.class) //
        .flatMapToDouble(c -> c.getTags().stream().mapToDouble(String::length));

    List<Double> actual = tagLengthDoubleStream.boxed().collect(Collectors.toList());

    assertThat(actual).containsExactlyElementsOf(expected);
  }

  @Test void testPeek() {
    final List<String> peekedEmails = new ArrayList<>();
    List<Company> companies = entityStream //
        .of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .peek(c -> peekedEmails.add(c.getEmail())) //
        .collect(Collectors.toList());

    assertThat(peekedEmails).containsExactly("stack@redis.com");

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).toList();
    assertTrue(names.contains("RedisInc"));
  }

  @Test void testToArray() {
    Object[] allCompanies = entityStream //
        .of(Company.class) //
        .toArray();

    assertEquals(3, allCompanies.length);

    List<String> names = Stream.of(allCompanies).map(c -> (Company) c).map(Company::getName).toList();
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test void testToArrayTyped() {
    Company[] allCompanies = entityStream //
        .of(Company.class) //
        .toArray(Company[]::new);

    assertEquals(3, allCompanies.length);

    List<String> names = Stream.of(allCompanies).map(Company::getName).toList();
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test void testReduceWithIdentityBifunctionAndBinaryOperator() {
    Integer firstEstablish = entityStream //
        .of(Company.class) //
        .reduce(Integer.MAX_VALUE, (minimum, company) -> Integer.min(minimum, company.getYearFounded()), (t, u) -> Integer.min(t, u));
    assertThat(firstEstablish).isEqualTo(1975);
  }

  @Test void testReduceWithMethodReferenceAndCombiner() {
    int result = entityStream //
        .of(Company.class) //
        .reduce(0, (acc, company) -> acc + company.getYearFounded(), (t, u) -> Integer.sum(t, u));

    assertThat(result).isEqualTo(2011 + 1975 + 2003);
  }

  @Test void testReduceWithCombiner() {
    BinaryOperator<Company> establishedFirst = (c1, c2) -> c1.getYearFounded() < c2.getYearFounded() ? c1 : c2;

    Optional<Company> firstEstablish = entityStream //
        .of(Company.class) //
        .reduce(establishedFirst);

    assertThat(firstEstablish).isPresent();
    assertThat(firstEstablish.get().getYearFounded()).isEqualTo(1975);
  }

  @Test void testReduceWithIdAndCombiner() {
    BinaryOperator<Company> establishedFirst = (c1, c2) -> c1.getYearFounded() < c2.getYearFounded() ? c1 : c2;

    Company c = new Company();
    c.setYearFounded(Integer.MAX_VALUE);
    Optional<Company> firstEstablish = Optional.of(entityStream //
        .of(Company.class) //
        .reduce(c, establishedFirst));

    assertThat(firstEstablish).isPresent();
    assertThat(firstEstablish.get().getYearFounded()).isEqualTo(1975);
  }

  @Test void testReduceWithMethodReferenceOnMappedField() {
    int result = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .reduce(0, (t, u) -> Integer.sum(t, u));

    assertThat(result).isEqualTo(2011 + 1975 + 2003);
  }

  @Test void testReduceWithLambdaOnMappedField() {
    int result = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .reduce(0, (t, u) -> Integer.sum(t, u));

    assertThat(result).isEqualTo(2011 + 1975 + 2003);
  }

  @Test void testReduceWithCombinerOnMappedField() {
    BinaryOperator<Integer> establishedFirst = (c1, c2) -> c1 < c2 ? c1 : c2;

    Optional<Integer> firstEstablish = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .reduce(establishedFirst);

    assertAll( //
        () -> assertThat(firstEstablish).isPresent(), //
        () -> assertThat(firstEstablish).contains(1975) //
    );
  }

  @Test void testReduceWithIdentityBifunctionAndBinaryOperatorOnMappedField() {
    Integer firstEstablish = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .reduce(Integer.MAX_VALUE, (t, u) -> Integer.min(t, u), (t, u) -> Integer.min(t, u));
    assertThat(firstEstablish).isEqualTo(1975);
  }

  @Test void testCollectWithSupplierAccumulatorAndCombinerOnMappedField() {
    Supplier<AtomicInteger> supplier = AtomicInteger::new;
    BiConsumer<AtomicInteger, Integer> accumulator = (AtomicInteger a, Integer i) -> a.set(a.get() + i);

    BiConsumer<AtomicInteger, AtomicInteger> combiner = (a1, a2) -> a1.set(a1.get() + a2.get());

    AtomicInteger result = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .collect(supplier, accumulator, combiner);

    assertThat(result.intValue()).isEqualTo(2011 + 1975 + 2003);
  }

  @Test void testCollectWithIdAndCombiner() {
    Supplier<AtomicInteger> supplier = AtomicInteger::new;
    BiConsumer<AtomicInteger, Company> accumulator = (AtomicInteger a, Company c) -> a.set(a.get() + c.getYearFounded());

    BiConsumer<AtomicInteger, AtomicInteger> combiner = (a1, a2) -> a1.set(a1.get() + a2.get());

    AtomicInteger result = entityStream //
        .of(Company.class) //
        .collect(supplier, accumulator, combiner);

    assertThat(result.intValue()).isEqualTo(2011 + 1975 + 2003);
  }

  @Test void testMinOnMappedField() {
    Optional<Integer> firstEstablish = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .min(Integer::compareTo);
    assertAll( //
        () -> assertThat(firstEstablish).isPresent(), //
        () -> assertThat(firstEstablish).contains(1975) //
    );
  }

  @Test void testMaxOnMappedField() {
    Optional<Integer> lastEstablish = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .max(Integer::compareTo);
    assertAll( //
        () -> assertThat(lastEstablish).isPresent(), //
        () -> assertThat(lastEstablish).contains(2011) //
    );
  }

  @Test void testAnyMatchOnMappedField() {
    boolean c1975 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .anyMatch(c -> c == 1975);

    boolean c1976 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .anyMatch(c -> c == 1976);

    assertThat(c1975).isTrue();
    assertThat(c1976).isFalse();
  }

  @Test void testAllMatchOnMappedField() {
    boolean allEstablishedBefore1970 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .allMatch(c -> c < 1970);

    boolean allEstablishedOnOrAfter1970 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .allMatch(c -> c >= 1970);

    assertThat(allEstablishedOnOrAfter1970).isTrue();
    assertThat(allEstablishedBefore1970).isFalse();
  }

  @Test void testNoneMatchOnMappedField() {
    boolean noneIn1975 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .noneMatch(c -> c == 1975);

    boolean noneIn1976 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .noneMatch(c -> c == 1976);

    assertThat(noneIn1976).isTrue();
    assertThat(noneIn1975).isFalse();
  }

  @Test void testMin() {
    Optional<Company> firstEstablish = entityStream //
        .of(Company.class) //
        .min(Comparator.comparing(Company::getYearFounded));
    assertThat(firstEstablish).isPresent();
    assertThat(firstEstablish.get().getYearFounded()).isEqualTo(1975);
  }

  @Test void testMax() {
    Optional<Company> lastEstablish = entityStream //
        .of(Company.class) //
        .max(Comparator.comparing(Company::getYearFounded));
    assertThat(lastEstablish).isPresent();
    assertThat(lastEstablish.get().getYearFounded()).isEqualTo(2011);
  }

  @Test void testAnyMatch() {
    boolean c1975 = entityStream //
        .of(Company.class) //
        .anyMatch(c -> c.getYearFounded() == 1975);

    boolean c1976 = entityStream //
        .of(Company.class) //
        .anyMatch(c -> c.getYearFounded() == 1976);

    assertThat(c1975).isTrue();
    assertThat(c1976).isFalse();
  }

  @Test void testAllMatch() {
    boolean allEstablishedBefore1970 = entityStream //
        .of(Company.class) //
        .allMatch(c -> c.getYearFounded() < 1970);

    boolean allEstablishedOnOrAfter1970 = entityStream //
        .of(Company.class) //
        .allMatch(c -> c.getYearFounded() >= 1970);

    assertThat(allEstablishedOnOrAfter1970).isTrue();
    assertThat(allEstablishedBefore1970).isFalse();
  }

  @Test void testNoneMatch() {
    boolean noneIn1975 = entityStream //
        .of(Company.class) //
        .noneMatch(c -> c.getYearFounded() == 1975);

    boolean noneIn1976 = entityStream //
        .of(Company.class) //
        .noneMatch(c -> c.getYearFounded() == 1976);

    assertThat(noneIn1976).isTrue();
    assertThat(noneIn1975).isFalse();
  }

  @Test void testIterator() {
    List<Company> allCompanies = new ArrayList<>();
    for (Iterator<Company> iterator = entityStream.of(Company.class).iterator(); iterator.hasNext(); ) {
      allCompanies.add(iterator.next());
    }

    assertEquals(3, allCompanies.size());

    List<String> names = allCompanies.stream().map(Company::getName).toList();
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test void testSplitIterator() {
    ArrayList<Company> allCompanies = new ArrayList<>();
    Spliterator<Company> iter1 = entityStream.of(Company.class).spliterator();
    Spliterator<Company> iter2 = iter1.trySplit();

    iter1.forEachRemaining(allCompanies::add);
    iter2.forEachRemaining(allCompanies::add);

    assertEquals(3, allCompanies.size());

    List<String> names = allCompanies.stream().map(Company::getName).toList();
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test void testNoops() {
    SearchStream<Company> stream = entityStream.of(Company.class);
    assertThat(stream.isParallel()).isFalse();
    assertThat(stream.parallel()).isEqualTo(stream);
    assertThat(stream.sequential()).isEqualTo(stream);
    assertThat(stream.unordered()).isEqualTo(stream);
  }

  @Test void testCloseHandler() {
    SearchStream<Company> stream = entityStream.of(Company.class);
    AtomicBoolean wasClosed = new AtomicBoolean(false);
    //noinspection ResultOfMethodCallIgnored
    stream.onClose(() -> wasClosed.set(true));
    stream.close();
    assertThat(wasClosed.get()).isTrue();
  }

  @Test void testCloseHandlerIsNull() {
    SearchStream<Company> stream = entityStream.of(Company.class);
    stream.close();
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, stream::findAny);

    String expectedErrorMessage = "stream has already been operated upon or closed";
    Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test void testIteratorOnMappedField() {
    List<String> allCompanies = new ArrayList<>();
    for (Iterator<String> iterator = entityStream.of(Company.class).map(Company$.NAME)
        .iterator(); iterator.hasNext(); ) {
      allCompanies.add(iterator.next());
    }

    assertEquals(3, allCompanies.size());

    List<String> names = allCompanies.stream().toList();
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test void testSplitIteratorOnMappedField() {
    List<String> allCompanies = new ArrayList<>();
    Spliterator<String> iter1 = entityStream.of(Company.class).map(Company$.NAME).spliterator();
    Spliterator<String> iter2 = iter1.trySplit();

    iter1.forEachRemaining(allCompanies::add);
    iter2.forEachRemaining(allCompanies::add);

    assertEquals(3, allCompanies.size());

    List<String> names = allCompanies.stream().toList();
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test void testStreamOnMappedFieldIsNotParallel() {
    SearchStream<String> stream = entityStream.of(Company.class).map(Company$.NAME);
    assertThat(stream.isParallel()).isFalse();
  }

  @Test void testCloseHandlerOnMappedField() {
    SearchStream<String> stream = entityStream.of(Company.class).map(Company$.NAME);
    AtomicBoolean wasClosed = new AtomicBoolean(false);
    //noinspection ResultOfMethodCallIgnored
    stream.onClose(() -> wasClosed.set(true));
    stream.close();
    assertThat(wasClosed.get()).isTrue();
  }

  @Test void testCloseHandlerIsNullOnMappedField() {
    SearchStream<String> stream = entityStream.of(Company.class).map(Company$.NAME);
    stream.close();
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, stream::findAny);

    String expectedErrorMessage = "stream has already been operated upon or closed";
    Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test void testFilterOnMappedField() {
    Predicate<Integer> predicate = i -> (i > 2000);
    List<Integer> foundedAfter2000 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .filter(predicate) //
        .collect(Collectors.toList());

    assertThat(foundedAfter2000).contains(2011, 2003);
  }

  @Test void testFlatMapToIntOnMappedField() {
    // expected
    List<Integer> expected = new ArrayList<>();
    for (Company company : entityStream.of(Company.class).collect(Collectors.toList())) {
      for (String tag : company.getTags()) {
        expected.add(tag.length());
      }
    }

    // actual
    IntStream tagLengthIntStream = entityStream //
        .of(Company.class) //
        .map(Company$.TAGS) //
        .flatMapToInt(tags -> tags.stream().mapToInt(String::length));

    List<Integer> actual = tagLengthIntStream.boxed().collect(Collectors.toList());

    assertThat(actual).containsExactlyElementsOf(expected);
  }

  @Test void testFlatMapToLongOnMappedField() {
    // expected
    List<Long> expected = new ArrayList<>();
    for (Company company : entityStream.of(Company.class).collect(Collectors.toList())) {
      for (String tag : company.getTags()) {
        expected.add((long) tag.length());
      }
    }

    // actual
    LongStream tagLengthIntStream = entityStream //
        .of(Company.class) //
        .map(Company$.TAGS) //
        .flatMapToLong(tags -> tags.stream().mapToLong(String::length));

    List<Long> actual = tagLengthIntStream.boxed().collect(Collectors.toList());

    assertThat(actual).containsExactlyElementsOf(expected);
  }

  @Test void testFlatMapToDoubleOnMappedField() {
    // expected
    List<Double> expected = new ArrayList<>();
    for (Company company : entityStream.of(Company.class).collect(Collectors.toList())) {
      for (String tag : company.getTags()) {
        expected.add((double) tag.length());
      }
    }

    // actual
    DoubleStream tagLengthDoubleStream = entityStream //
        .of(Company.class) //
        .map(Company$.TAGS) //
        .flatMapToDouble(tags -> tags.stream().mapToDouble(String::length));

    List<Double> actual = tagLengthDoubleStream.boxed().collect(Collectors.toList());

    assertThat(actual).containsExactlyElementsOf(expected);
  }

  @Test void testCollectionRetrievalOnMappedField() {
    List<Set<String>> expected = new ArrayList<>();
    for (Company company : entityStream.of(Company.class).collect(Collectors.toList())) {
      expected.add(company.getTags());
    }

    List<Set<String>> actual = entityStream //
        .of(Company.class) //
        .map(Company$.TAGS) //
        .collect(Collectors.toList());

    assertThat(actual).containsExactlyElementsOf(expected);
  }

  @Test void testMapWithFunctionAgainstMappedField() {
    Function<Set<String>, String> mapper = (Set<String> tags) -> String.join("-", new TreeSet<>(tags));
    List<String> joinedTags = entityStream //
        .of(Company.class) //
        .map(Company$.TAGS) //
        .map(mapper) //
        .collect(Collectors.toList());

    assertThat(joinedTags).containsExactly( //
        "database-fast-nosql-reliable-scalable", //
        "ai-innovative-os-reliable", //
        "ai-futuristic-innovative" //
    );
  }

  @Test void testFlatMap() {
    Function<Set<String>, Stream<String>> mapper = (Set<String> tags) -> Stream.of(String.join("-", new TreeSet<>(tags)));

    List<String> joinedTags = entityStream //
        .of(Company.class) //
        .map(Company$.TAGS) //
        .flatMap(mapper) //
        .collect(Collectors.toList());

    assertThat(joinedTags).containsExactly( //
        "database-fast-nosql-reliable-scalable", //
        "ai-innovative-os-reliable", //
        "ai-futuristic-innovative" //
    );
  }

  @Test void testFlatMapOnMappedField() {
    Function<Company, Stream<String>> mapper = (Company company) -> Stream.of(String.join("-", new TreeSet<>(company.getTags())));

    List<String> joinedTags = entityStream //
        .of(Company.class) //
        .flatMap(mapper) //
        .collect(Collectors.toList());

    assertThat(joinedTags).containsExactly( //
        "database-fast-nosql-reliable-scalable", //
        "ai-innovative-os-reliable", //
        "ai-futuristic-innovative" //
    );
  }

  @Test void testForEachOrderedOnMappedField() {
    List<String> names = new ArrayList<>();
    Consumer<? super String> testConsumer = names::add;
    entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .forEachOrdered(testConsumer);

    assertEquals(3, names.size());

    assertEquals("RedisInc", names.get(0));
    assertEquals("Microsoft", names.get(1));
    assertEquals("Tesla", names.get(2));
  }

  @Test void testForEachOnMappedField() {
    List<String> names = new ArrayList<>();
    Consumer<? super String> testConsumer = names::add;
    entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .forEach(testConsumer);

    assertEquals(3, names.size());

    assertEquals("RedisInc", names.get(0));
    assertEquals("Microsoft", names.get(1));
    assertEquals("Tesla", names.get(2));
  }

  @Test void testToArrayOnMappedField() {
    Object[] allCompanies = entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .toArray();

    assertEquals(3, allCompanies.length);

    List<String> names = Stream.of(allCompanies).map(Object::toString).toList();

    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test void testToArrayTypedOnMappedField() {
    String[] namesArray = entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .toArray(String[]::new);

    assertEquals(3, namesArray.length);

    List<String> names = Stream.of(namesArray).toList();
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test void testCountOnMappedField() {
    long count = entityStream //
        .of(Company.class) //
        .filter( //
            Company$.NAME.notEq("RedisInc") //
                .and(Company$.NAME.notEq("Microsoft")) //
        ) //
        .map(Company$.NAME) //
        .count();

    assertEquals(1, count);
  }

  @Test void testLimitOnMappedField() {
    List<String> companies = entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .limit(2).collect(Collectors.toList());

    assertEquals(2, companies.size());
  }

  @Test void testSkipOnMappedField() {
    List<String> companies = entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .skip(1) //
        .collect(Collectors.toList());

    assertEquals(2, companies.size());
  }

  @Test void testSortOnMappedField() {
    List<String> names = entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .sorted(Comparator.reverseOrder()) //
        .collect(Collectors.toList());

    assertEquals(3, names.size());

    assertEquals("Tesla", names.get(0));
    assertEquals("RedisInc", names.get(1));
    assertEquals("Microsoft", names.get(2));
  }

  @Test void testPeekOnMappedField() {
    final List<String> peekedEmails = new ArrayList<>();
    List<String> emails = entityStream //
        .of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .map(Company$.EMAIL) //
        .peek(peekedEmails::add) //
        .collect(Collectors.toList());

    assertThat(peekedEmails).containsExactly("stack@redis.com");

    assertEquals(1, emails.size());
  }

  @Test void testFindFirstOnMappedField() {
    Optional<String> maybeEmail = entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .map(Company$.EMAIL) //
        .findFirst();

    assertTrue(maybeEmail.isPresent());
    assertEquals("stack@redis.com", maybeEmail.get());
  }

  @Test void testFindAnyOnMappedField() {
    Optional<String> maybeEmail = entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .map(Company$.EMAIL) //
        .findAny();

    assertTrue(maybeEmail.isPresent());
    assertEquals("stack@redis.com", maybeEmail.get());
  }

  @Test void testNoopsOnMappedField() {
    Iterator<String> iter1 = entityStream.of(Company.class).map(Company$.NAME).iterator();
    Iterator<String> iter2 = entityStream.of(Company.class).map(Company$.NAME).iterator();
    Iterator<String> iter3 = entityStream.of(Company.class).map(Company$.NAME).iterator();
    SearchStream<String> parallel = entityStream.of(Company.class).map(Company$.NAME).parallel();
    SearchStream<String> sequential = entityStream.of(Company.class).map(Company$.NAME).sequential();
    SearchStream<String> unordered = entityStream.of(Company.class).map(Company$.NAME).unordered();
    assertThat(Iterators.elementsEqual(iter1, parallel.iterator())).isTrue();
    assertThat(Iterators.elementsEqual(iter2, sequential.iterator())).isTrue();
    assertThat(Iterators.elementsEqual(iter3, unordered.iterator())).isTrue();
  }

  @Test void testMapOnMappedField() {
    ToLongFunction<Integer> func = y -> y - 1;

    List<Long> yearsMinusOne = entityStream.of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .map(func) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(yearsMinusOne).hasSize(3), //
        () -> assertThat(yearsMinusOne).containsExactly(2010L, 1974L, 2002L) //
    );
  }

  @Test void testSplitIteratorOnMappedFieldParallelStream() {
    List<String> allCompanies = new ArrayList<>();
    Spliterator<String> iter1 = entityStream.of(Company.class).map(Company$.NAME).parallel().spliterator();
    Spliterator<String> iter2 = iter1.trySplit();

    iter1.forEachRemaining(allCompanies::add);
    iter2.forEachRemaining(allCompanies::add);

    assertEquals(3, allCompanies.size());

    List<String> names = allCompanies.stream().toList();
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test void testSplitIteratorOnMappedFieldSequentialStream() {
    List<String> allCompanies = new ArrayList<>();
    Spliterator<String> iter1 = entityStream.of(Company.class).map(Company$.NAME).sequential().spliterator();
    Spliterator<String> iter2 = iter1.trySplit();

    iter1.forEachRemaining(allCompanies::add);
    iter2.forEachRemaining(allCompanies::add);

    assertEquals(3, allCompanies.size());

    List<String> names = allCompanies.stream().toList();
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  //
  // Non-indexed Fields
  //

  @Test void testStrAppendToNonIndexedTextFieldInDocuments() {
    entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .forEach(NiCompany$.EMAIL.append("zzz"));

    Optional<NiCompany> maybeMicrosoft = nicRepository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    assertThat(maybeMicrosoft.get().getEmail()).endsWith("zzz");
  }

  @Test void testStrLenToNonIndexedTagFieldInDocuments() {
    List<Long> emailLengths = entityStream.of(NiCompany.class) //
        .map(NiCompany$.EMAIL.length()) //
        .collect(Collectors.toList());
    assertAll( //
        () -> assertThat(emailLengths).hasSize(3), //
        () -> assertThat(emailLengths).containsExactly(15L, 22L, 14L) //
    );
  }

  @Test void testToggleToNonIndexedBooleanFieldInDocuments() {
    Optional<NiCompany> maybeRedisBefore = nicRepository.findFirstByName("RedisInc");
    assertTrue(maybeRedisBefore.isPresent());
    assertFalse(maybeRedisBefore.get().isPubliclyListed());

    entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("RedisInc")) //
        .forEach(NiCompany$.PUBLICLY_LISTED.toggle());

    Optional<NiCompany> maybeRedisAfter = nicRepository.findFirstByName("RedisInc");
    assertTrue(maybeRedisAfter.isPresent());
    assertTrue(maybeRedisAfter.get().isPubliclyListed());
    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testNumIncrByToNonIndexedNumericFieldInDocuments() {
    Optional<NiCompany> maybeRedisBefore = nicRepository.findFirstByName("RedisInc");
    assertTrue(maybeRedisBefore.isPresent());
    assertEquals(2011, maybeRedisBefore.get().getYearFounded());

    entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("RedisInc")) //
        .forEach(NiCompany$.YEAR_FOUNDED.incrBy(5L));

    Optional<NiCompany> maybeRedisAfter = nicRepository.findFirstByName("RedisInc");
    assertTrue(maybeRedisAfter.isPresent());
    assertEquals(2016, maybeRedisAfter.get().getYearFounded());
    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testNumDecrByToNonIndexedNumericFieldInDocuments() {
    Optional<NiCompany> maybeRedisBefore = nicRepository.findFirstByName("RedisInc");
    assertTrue(maybeRedisBefore.isPresent());
    assertEquals(2011, maybeRedisBefore.get().getYearFounded());

    entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("RedisInc")) //
        .forEach(NiCompany$.YEAR_FOUNDED.decrBy(2L));

    Optional<NiCompany> maybeRedisAfter = nicRepository.findFirstByName("RedisInc");
    assertTrue(maybeRedisAfter.isPresent());
    assertEquals(2009, maybeRedisAfter.get().getYearFounded());
    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testArrayAppendToNonIndexedTagFieldInDocuments() {
    entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .forEach(NiCompany$.TAGS.add("gaming"));

    Optional<NiCompany> maybeMicrosoft = nicRepository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    NiCompany microsoft = maybeMicrosoft.get();
    assertThat(microsoft.getTags()).contains("innovative", "reliable", "os", "ai", "gaming");
    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testArrayInsertToNonIndexedTagFieldInDocuments() {
    entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .forEach(NiCompany$.TAGS.insert("gaming", 2));

    Optional<NiCompany> maybeMicrosoft = nicRepository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    NiCompany microsoft = maybeMicrosoft.get();
    assertThat(microsoft.getTags()).contains("innovative", "reliable", "gaming", "os", "ai");
    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testArrayPrependToNonIndexedTagFieldInDocuments() {
    entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .forEach(NiCompany$.TAGS.prepend("gaming"));

    Optional<NiCompany> maybeMicrosoft = nicRepository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    NiCompany microsoft = maybeMicrosoft.get();
    assertThat(microsoft.getTags()).contains("gaming", "innovative", "reliable", "os", "ai");
    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testStrLenToNonIndexedTextFieldInDocuments() {
    List<Long> emailLengths = entityStream.of(NiCompany.class) //
        .map(NiCompany$.NAME.length()) //
        .collect(Collectors.toList());
    assertAll( //
        () -> assertThat(emailLengths).hasSize(3), //
        () -> assertThat(emailLengths).containsExactly(8L, 9L, 5L) //
    );
  }

  @Test void testArrayLengthOnNonIndexedTagFieldInDocuments() {
    List<Long> tagsLengths = entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .map(NiCompany$.TAGS.length()) //
        .collect(Collectors.toList());
    assertAll( //
        () -> assertThat(tagsLengths).hasSize(1), //
        () -> assertThat(tagsLengths).containsExactly(4L) //
    );
  }

  @Test void testArrayIndexOfOnNonIndexedTagFieldInDocuments() {
    List<Long> tagsLengths = entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .map(NiCompany$.TAGS.indexOf("os")) //
        .collect(Collectors.toList());
    assertAll( //
        () -> assertThat(tagsLengths).hasSize(1), //
        () -> assertThat(tagsLengths).containsExactly(2L) //
    );
  }

  @Test void testArrayPopOnNonIndexedTagFieldInDocuments() {
    List<Object> tags = entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .map(NiCompany$.TAGS.pop()) //
        .collect(Collectors.toList());

    // contains the last
    assertThat(tags).containsExactly("ai");

    Optional<NiCompany> maybeMicrosoft = nicRepository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    NiCompany microsoft = maybeMicrosoft.get();

    // the rest remains
    assertThat(microsoft.getTags()).contains("innovative", "reliable", "os");

    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testArrayRemoveLastOnNonIndexedTagFieldInDocuments() {
    List<Object> tags = entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .map(NiCompany$.TAGS.removeLast()) //
        .collect(Collectors.toList());

    // contains the last
    assertThat(tags).containsExactly("ai");

    Optional<NiCompany> maybeMicrosoft = nicRepository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    NiCompany microsoft = maybeMicrosoft.get();

    // the rest remains
    assertThat(microsoft.getTags()).contains("innovative", "reliable", "os");

    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testArrayPopFirstOnNonIndexedTagFieldInDocuments() {
    List<Object> tags = entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .map(NiCompany$.TAGS.pop(0)) //
        .collect(Collectors.toList());

    // contains the last
    assertThat(tags).containsExactly("innovative");

    Optional<NiCompany> maybeMicrosoft = nicRepository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    NiCompany microsoft = maybeMicrosoft.get();

    // the rest remains
    assertThat(microsoft.getTags()).contains("reliable", "os", "ai");

    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testArrayRemoveFirstOnNonIndexedTagFieldInDocuments() {
    List<Object> tags = entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .map(NiCompany$.TAGS.removeFirst()) //
        .collect(Collectors.toList());

    // contains the last
    assertThat(tags).containsExactly("innovative");

    Optional<NiCompany> maybeMicrosoft = nicRepository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    NiCompany microsoft = maybeMicrosoft.get();

    // the rest remains
    assertThat(microsoft.getTags()).contains("reliable", "os", "ai");

    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testArrayRemoveByIndexOnNonIndexedTagFieldInDocuments() {
    List<Object> tags = entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .map(NiCompany$.TAGS.remove(0)) //
        .collect(Collectors.toList());

    // contains the last
    assertThat(tags).containsExactly("innovative");

    Optional<NiCompany> maybeMicrosoft = nicRepository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    NiCompany microsoft = maybeMicrosoft.get();

    // the rest remains
    assertThat(microsoft.getTags()).contains("reliable", "os", "ai");

    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testArrayTrimToRangeOnNonIndexedTagFieldInDocuments() {
    entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .forEach(NiCompany$.TAGS.trimToRange(1, 1));

    Optional<NiCompany> maybeMicrosoft = nicRepository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    NiCompany microsoft = maybeMicrosoft.get();

    assertThat(microsoft.getTags()).containsExactly("reliable");
    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testArrayTrimToRangeOnNonIndexedTagFieldInDocuments2() {
    entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .forEach(NiCompany$.TAGS.trimToRange(0, 0));

    Optional<NiCompany> maybeMicrosoft = nicRepository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    NiCompany microsoft = maybeMicrosoft.get();

    assertThat(microsoft.getTags()).containsExactly("innovative");
    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testArrayTrimToRangeOnNonIndexedTagFieldInDocuments3() {
    entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .forEach(NiCompany$.TAGS.trimToRange(1, 2));

    Optional<NiCompany> maybeMicrosoft = nicRepository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    NiCompany microsoft = maybeMicrosoft.get();

    assertThat(microsoft.getTags()).containsExactly("reliable", "os");
    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testArrayTrimToRangeOnNonIndexedTagFieldInDocuments4() {
    entityStream.of(NiCompany.class) //
        .filter(NiCompany$.NAME.eq("Microsoft")) //
        .forEach(NiCompany$.TAGS.trimToRange(1, -1));

    Optional<NiCompany> maybeMicrosoft = nicRepository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    NiCompany microsoft = maybeMicrosoft.get();

    assertThat(microsoft.getTags()).containsExactly("reliable", "os", "ai");
    flushSearchIndexFor(NiCompany.class);
  }

  @Test void testTupleResultWithLabels() {
    List<Map<String, Object>> results = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Fields.of(Company$.NAME, Company$.YEAR_FOUNDED, Company$.LOCATION)) //
        .mapToLabelledMaps().toList();

    assertEquals(3, results.size());

    assertThat(results.get(0)) //
        .containsEntry("name", "Tesla") //
        .containsEntry("yearFounded", 2003) //
        .containsEntry("location", new Point(-97.6208903, 30.2210767));

    assertThat(results.get(1)) //
        .containsEntry("name", "RedisInc") //
        .containsEntry("yearFounded", 2011) //
        .containsEntry("location", new Point(-122.066540, 37.377690));

    assertThat(results.get(2)) //
        .containsEntry("name", "Microsoft") //
        .containsEntry("yearFounded", 1975) //
        .containsEntry("location", new Point(-122.124500, 47.640160));
  }
}
