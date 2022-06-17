package com.redis.om.spring.search.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Company;
import com.redis.om.spring.annotations.document.fixtures.Company$;
import com.redis.om.spring.annotations.document.fixtures.CompanyRepository;
import com.redis.om.spring.tuple.Fields;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.tuple.Quad;
import com.redis.om.spring.tuple.Triple;

import io.redisearch.aggregation.SortedField.SortOrder;

public class EntityStreamTest extends AbstractBaseDocumentTest {
  @Autowired
  CompanyRepository repository;

  @Autowired
  EntityStream entityStream;

  @BeforeEach
  public void cleanUp() {
    repository.deleteAll();

    Company redis = repository.save(Company.of("RedisInc", 2011, new Point(-122.066540, 37.377690), "stack@redis.com"));
    redis.setTags(Set.of("fast", "scalable", "reliable", "database", "nosql"));

    Company microsoft = repository
        .save(Company.of("Microsoft", 1975, new Point(-122.124500, 47.640160), "research@microsoft.com"));
    microsoft.setTags(Set.of("innovative", "reliable", "os", "ai"));

    Company tesla = repository.save(Company.of("Tesla", 2003, new Point(-97.6208903, 30.2210767), "elon@tesla.com"));
    tesla.setTags(Set.of("innovative", "futuristic", "ai"));

    repository.saveAll(List.of(redis, microsoft, tesla));
  }

  @Test
  public void testStreamSelectAll() {
    SearchStream<Company> stream = entityStream.of(Company.class);
    List<Company> allCompanies = stream.collect(Collectors.toList());

    assertEquals(3, allCompanies.size());

    List<String> names = allCompanies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test
  public void testFindByOnePropertyEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.NAME.eq("RedisInc")) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("RedisInc"));
  }

  @Test
  public void testFindByOnePropertyNotEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.NAME.notEq("RedisInc")) //
        .collect(Collectors.toList());

    assertEquals(2, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test
  public void testFindByTwoPropertiesOredEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter( //
            Company$.NAME.eq("RedisInc") //
                .or(Company$.NAME.eq("Microsoft")) //
        ) //
        .collect(Collectors.toList());

    assertEquals(2, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
  }

  @Test
  public void testFindByThreePropertiesOredEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter( //
            Company$.NAME.eq("RedisInc") //
                .or(Company$.NAME.eq("Microsoft")) //
                .or(Company$.NAME.eq("Tesla")) //
        ) //
        .collect(Collectors.toList());

    assertEquals(3, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test
  public void testFindByThreePropertiesOrList() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.NAME.in("RedisInc", "Microsoft", "Tesla")).collect(Collectors.toList());

    assertEquals(3, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test
  public void testFindByThreePropertiesOrList2() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.NAME.in("RedisInc", "Tesla")).collect(Collectors.toList());

    assertEquals(2, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Tesla"));
  }

  @Test
  public void testFindByThreeNumericPropertiesOrList() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.in(2011, 1975, 2003)).collect(Collectors.toList());

    assertEquals(3, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test
  public void testFindByTwoPropertiesAndedNotEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter( //
            Company$.NAME.notEq("RedisInc") //
                .and(Company$.NAME.notEq("Microsoft")) //
        ) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("Tesla"));
  }

  @Test
  public void testFindByNumericPropertyEquals() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.eq(2011)) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("RedisInc"));
  }

  @Test
  public void testFindByNumericPropertyGreaterThan() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.gt(2000)) //
        .collect(Collectors.toList());

    assertEquals(2, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Tesla"));
  }

  @Test
  public void testFindByNumericPropertyGreaterThanOrEqual() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.ge(2011)) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("RedisInc"));
  }

  @Test
  public void testFindByNumericPropertyLessThan() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.lt(2000)) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("Microsoft"));
  }

  @Test
  public void testFindByNumericPropertyLessThanOrEqual() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.le(1975)) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("Microsoft"));
  }

  @Test
  public void testFindByNumericPropertyBetween() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.YEAR_FOUNDED.between(1976, 2010)) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("Tesla"));
  }

  @Test
  public void testCount() {
    long count = entityStream //
        .of(Company.class) //
        .filter( //
            Company$.NAME.notEq("RedisInc") //
                .and(Company$.NAME.notEq("Microsoft")) //
        ).count();

    assertEquals(1, count);
  }

  @Test
  public void testLimit() {
    List<Company> companies = entityStream //
        .of(Company.class) //
        .limit(2).collect(Collectors.toList());

    assertEquals(2, companies.size());
  }

  @Test
  public void testSkip() {
    List<Company> companies = entityStream //
        .of(Company.class) //
        .skip(1).collect(Collectors.toList());

    assertEquals(2, companies.size());
  }

  @Test
  public void testSortDefaultAscending() {
    List<Company> companies = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME).collect(Collectors.toList());

    assertEquals(3, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertEquals(names.get(0), "Microsoft");
    assertEquals(names.get(1), "RedisInc");
    assertEquals(names.get(2), "Tesla");
  }

  @Test
  public void testSortAscending() {
    List<Company> companies = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.ASC).collect(Collectors.toList());

    assertEquals(3, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertEquals(names.get(0), "Microsoft");
    assertEquals(names.get(1), "RedisInc");
    assertEquals(names.get(2), "Tesla");
  }

  @Test
  public void testSortDescending() {
    List<Company> companies = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.DESC).collect(Collectors.toList());

    assertEquals(3, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());

    assertEquals(names.get(0), "Tesla");
    assertEquals(names.get(1), "RedisInc");
    assertEquals(names.get(2), "Microsoft");
  }

  @Test
  public void testForEachOrdered() {
    List<Company> companies = new ArrayList<Company>();
    Consumer<? super Company> testConsumer = (Company c) -> companies.add(c);
    entityStream //
        .of(Company.class) //
        .forEachOrdered(testConsumer);

    assertEquals(3, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());

    assertEquals(names.get(0), "RedisInc");
    assertEquals(names.get(1), "Microsoft");
    assertEquals(names.get(2), "Tesla");
  }

  @Test
  public void testMapToOneProperty() {
    List<String> names = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(3, names.size());

    assertEquals("Tesla", names.get(0));
    assertEquals("RedisInc", names.get(1));
    assertEquals("Microsoft", names.get(2));
  }

  @Test
  public void testMapToTwoProperties() {
    List<Pair<String, Integer>> results = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Fields.of(Company$.NAME, Company$.YEAR_FOUNDED)) //
        .collect(Collectors.toList());

    assertEquals(3, results.size());

    assertEquals(results.get(0).getFirst(), "Tesla");
    assertEquals(results.get(1).getFirst(), "RedisInc");
    assertEquals(results.get(2).getFirst(), "Microsoft");

    assertEquals(results.get(0).getSecond(), 2003);
    assertEquals(results.get(1).getSecond(), 2011);
    assertEquals(results.get(2).getSecond(), 1975);
  }

  @Test
  public void testMapToThreeProperties() {
    List<Triple<String, Integer, Point>> results = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Fields.of(Company$.NAME, Company$.YEAR_FOUNDED, Company$.LOCATION)) //
        .collect(Collectors.toList());

    assertEquals(3, results.size());

    assertEquals(results.get(0).getFirst(), "Tesla");
    assertEquals(results.get(1).getFirst(), "RedisInc");
    assertEquals(results.get(2).getFirst(), "Microsoft");

    assertEquals(results.get(0).getSecond(), 2003);
    assertEquals(results.get(1).getSecond(), 2011);
    assertEquals(results.get(2).getSecond(), 1975);

    assertEquals(results.get(0).getThird(), new Point(-97.6208903, 30.2210767));
    assertEquals(results.get(1).getThird(), new Point(-122.066540, 37.377690));
    assertEquals(results.get(2).getThird(), new Point(-122.124500, 47.640160));
  }

  @Test
  public void testMapToFourPropertiesOneNotIndexed() {
    List<Quad<String, Integer, Point, Date>> results = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Fields.of(Company$.NAME, Company$.YEAR_FOUNDED, Company$.LOCATION, Company$.CREATED_DATE)) //
        .collect(Collectors.toList());

    assertEquals(3, results.size());

    assertEquals(results.get(0).getFirst(), "Tesla");
    assertEquals(results.get(1).getFirst(), "RedisInc");
    assertEquals(results.get(2).getFirst(), "Microsoft");

    assertEquals(results.get(0).getSecond(), 2003);
    assertEquals(results.get(1).getSecond(), 2011);
    assertEquals(results.get(2).getSecond(), 1975);

    assertEquals(results.get(0).getThird(), new Point(-97.6208903, 30.2210767));
    assertEquals(results.get(1).getThird(), new Point(-122.066540, 37.377690));
    assertEquals(results.get(2).getThird(), new Point(-122.124500, 47.640160));

    assertNotNull(results.get(0).getFourth());
    assertNotNull(results.get(1).getFourth());
    assertNotNull(results.get(2).getFourth());
  }

  @Test
  public void testGeoNearPredicate() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.LOCATION.near(new Point(-122.064, 37.384), new Distance(30, Metrics.MILES))) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(1, names.size());

    assertEquals("RedisInc", names.get(0));
  }

  @Test
  public void testFindByTextLike() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.NAME.like("Micros")) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("Microsoft"));
  }

  @Test
  public void testFindByTextNotLike() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.NAME.notLike("Micros")) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(2, names.size());

    assertTrue(names.contains("Tesla"));
    assertTrue(names.contains("RedisInc"));
  }

  @Test
  public void testFindByTextThatStartsWith() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.NAME.startsWith("Mic")) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("Microsoft"));
  }

  @Test
  public void testFindByTagsIn() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.TAGS.in("reliable")) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(2, names.size());

    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
  }

  @Test
  public void testFindByTagsIn2() {
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

  @Test
  public void testFindByTagsContainingAll() {
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.TAGS.containsAll("fast", "scalable", "reliable", "database", "nosql")) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(1, names.size());

    assertTrue(names.contains("RedisInc"));
  }

  @Test
  public void testFindByTagsEquals() {
    Set<String> tags = Set.of("fast", "scalable", "reliable", "database", "nosql");
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.TAGS.eq(tags)) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(1, names.size());

    assertTrue(names.contains("RedisInc"));
  }

  @Test
  public void testFindByTagsNotEquals() {
    Set<String> tags = Set.of("fast", "scalable", "reliable", "database", "nosql");
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.TAGS.notEq(tags)) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(1, names.size());
    assertTrue(names.contains("Tesla"));
  }

  @Test
  public void testFindByTagsContainsNone() {
    Set<String> tags = Set.of("innovative");
    List<String> names = entityStream //
        .of(Company.class) //
        .filter(Company$.TAGS.containsNone(tags)) //
        .map(Company$.NAME) //
        .collect(Collectors.toList());

    assertEquals(1, names.size());

    assertTrue(names.contains("RedisInc"));
  }

  @Test
  public void testFindFirst() {
    Optional<Company> maybeCompany = entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .findFirst();

    assertTrue(maybeCompany.isPresent());
    assertEquals("RedisInc", maybeCompany.get().getName());
  }

  @Test
  public void testToggleBooleanFieldInDocuments() {
    Optional<Company> maybeRedisBefore = repository.findFirstByName("RedisInc");
    assertTrue(maybeRedisBefore.isPresent());
    assertEquals(false, maybeRedisBefore.get().isPubliclyListed());

    entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .forEach(Company$.PUBLICLY_LISTED.toggle());

    Optional<Company> maybeRedisAfter = repository.findFirstByName("RedisInc");
    assertTrue(maybeRedisAfter.isPresent());
    assertEquals(true, maybeRedisAfter.get().isPubliclyListed());
  }

  @Test
  public void testNumIncrByNumericFieldInDocuments() {
    Optional<Company> maybeRedisBefore = repository.findFirstByName("RedisInc");
    assertTrue(maybeRedisBefore.isPresent());
    assertEquals(2011, maybeRedisBefore.get().getYearFounded());

    entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .forEach(Company$.YEAR_FOUNDED.incrBy(5L));

    Optional<Company> maybeRedisAfter = repository.findFirstByName("RedisInc");
    assertTrue(maybeRedisAfter.isPresent());
    assertEquals(2016, maybeRedisAfter.get().getYearFounded());
  }

  @Test
  public void testNumDecrByNumericFieldInDocuments() {
    Optional<Company> maybeRedisBefore = repository.findFirstByName("RedisInc");
    assertTrue(maybeRedisBefore.isPresent());
    assertEquals(2011, maybeRedisBefore.get().getYearFounded());

    entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .forEach(Company$.YEAR_FOUNDED.decrBy(2L));

    Optional<Company> maybeRedisAfter = repository.findFirstByName("RedisInc");
    assertTrue(maybeRedisAfter.isPresent());
    assertEquals(2009, maybeRedisAfter.get().getYearFounded());
  }

  @Test
  public void testStrAppendToIndexedTextFieldInDocuments() {
    entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("Microsoft")) //
        .forEach(Company$.NAME.append(" Corp"));

    Optional<Company> maybeMicrosoft = repository.findFirstByEmail("research@microsoft.com");
    assertTrue(maybeMicrosoft.isPresent());
  }

  @Test
  public void testStrAppendToNonIndexedTextFieldInDocuments() {
    entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("Microsoft")) //
        .forEach(Company$.EMAIL.append("zzz"));

    Optional<Company> maybeMicrosoft = repository.findFirstByName("Microsoft");
    assertTrue(maybeMicrosoft.isPresent());
    assertThat(maybeMicrosoft.get().getEmail()).endsWith("zzz");
  }

  @Test
  public void testStrLenToIndexedTextFieldInDocuments() {
    List<Long> emailLengths = entityStream.of(Company.class) //
        .map(Company$.NAME.length()) //
        .collect(Collectors.toList());
    assertThat(emailLengths).hasSize(3);
    assertThat(emailLengths).containsExactly(8L, 9L, 5L);
  }

  @Test
  public void testStrLenToNonIndexedTagFieldInDocuments() {
    List<Long> emailLengths = entityStream.of(Company.class) //
        .map(Company$.EMAIL.length()) //
        .collect(Collectors.toList());
    assertThat(emailLengths).hasSize(3);
    assertThat(emailLengths).containsExactly(15L, 22L, 14L);
  }
  
  @Test
  public void testFindByTagEscapesChars() {
    SearchStream<Company> stream = entityStream.of(Company.class);

    List<Company> companies = stream //
        .filter(Company$.EMAIL.eq("stack@redis.com")) //
        .collect(Collectors.toList());

    assertEquals(1, companies.size());

    List<String> names = companies.stream().map(Company::getName).collect(Collectors.toList());
    assertTrue(names.contains("RedisInc"));
  }

}
