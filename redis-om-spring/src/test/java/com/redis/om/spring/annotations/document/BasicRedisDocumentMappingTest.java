package com.redis.om.spring.annotations.document;

import com.google.common.collect.Sets;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import redis.clients.jedis.json.Path;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
class BasicRedisDocumentMappingTest extends AbstractBaseDocumentTest {
  @Autowired
  CompanyRepository repository;

  @Autowired
  MetadataRepository metadataRepo;

  @Autowired
  DocWithSetsRepository docWithSetsRepository;

  @BeforeEach
  void cleanUp() {
    flushSearchIndexFor(Company.class);
    flushSearchIndexFor(DocWithSets.class);
  }

  @Test
  void testBasicCrudOperations() {
    Company redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");
    redis.setMetaList(Set.of(CompanyMeta.of("Redis", 100, Set.of("RedisTag"))));

    Company microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com");
    microsoft.setMetaList(Set.of(CompanyMeta.of("MS", 50, Set.of("MsTag"))));

    repository.saveAll(List.of(redis, microsoft));

    assertEquals(2, repository.count());

    Optional<Company> maybeRedisLabs = repository.findById(redis.getId());
    Optional<Company> maybeMicrosoft = repository.findById(microsoft.getId());

    assertTrue(maybeRedisLabs.isPresent());
    assertTrue(maybeMicrosoft.isPresent());

    assertEquals(redis, maybeRedisLabs.get());
    assertEquals(microsoft, maybeMicrosoft.get());

    // delete given an entity
    repository.delete(microsoft);

    assertEquals(1, repository.count());

    // delete given an id
    repository.deleteById(redis.getId());

    assertEquals(0, repository.count());
  }

  @Test
  void testDeleteByIdWithExplicitRootPath() {
    Company redis = repository.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    repository.save(Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
        "research@microsoft.com"));

    assertEquals(2, repository.count());

    repository.deleteById(redis.getId(), Path.ROOT_PATH);

    assertEquals(1, repository.count());
  }

  @Test
  void testFindAllById() {
    Company redis = repository.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = repository.save(Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com"));

    assertEquals(2, repository.count());

    Iterable<Company> companies = repository.findAllById(List.of(redis.getId(), microsoft.getId()));

    assertAll( //
        () -> assertThat(companies).hasSize(2), //
        () -> assertThat(companies).containsExactly(redis, microsoft) //
    );
  }

  @Test
  void testUpdateSingleField() {
    Company redisInc = repository.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    repository.updateField(redisInc, Company$.NAME, "Redis");

    Optional<Company> maybeRedis = repository.findById(redisInc.getId());

    assertThat(maybeRedis).isPresent().map(Company::getName).contains("Redis");
  }

  @Test
  void testAuditAnnotations() {
    Company redis = repository.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = repository.save(Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com"));

    assertAll( //
        // created dates should not be null
        () -> assertNotNull(redis.getCreatedDate()), //
        () -> assertNotNull(microsoft.getCreatedDate()), //

        // created dates should be null upon creation
        () -> assertNull(redis.getLastModifiedDate()), //
        () -> assertNull(microsoft.getLastModifiedDate()) //
    );

    repository.save(redis);
    repository.save(microsoft);

    assertAll( //
        // last modified dates should not be null after a second save
        () -> assertNotNull(redis.getLastModifiedDate()), //
        () -> assertNotNull(microsoft.getLastModifiedDate()) //
    );
  }

  @Test
  void testGetFieldsByIds() {
    Company redis = repository.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = repository.save(Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com"));

    Iterable<String> ids = List.of(redis.getId(), microsoft.getId());
    Iterable<String> companyNames = repository.getFieldsByIds(ids, Company$.NAME);
    assertThat(companyNames).containsExactly(redis.getName(), microsoft.getName());
  }

  @Test
  void testDynamicBloomRepositoryMethod() {
    Company redis = repository.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = repository.save(Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com"));

    assertTrue(repository.existsByEmail(redis.getEmail()));
    assertTrue(repository.existsByEmail(microsoft.getEmail()));
    assertFalse(repository.existsByEmail("bsb@redis.com"));
  }

  @Test
  void testGetNestedFields() {
    Set<Employee> redisEmployees = Sets.newHashSet(Employee.of("Guy Royse"), Employee.of("Simon Prickett"));
    Company redisInc = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");
    redisInc.setEmployees(redisEmployees);

    Set<Employee> msEmployees = Sets.newHashSet(Employee.of("Kevin Scott"));
    Company microsoft = repository.save(Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com"));
    microsoft.setEmployees(msEmployees);
    repository.saveAll(List.of(redisInc, microsoft));

    List<Company> companies = repository.findByEmployees_name("Guy Royse");
    assertThat(companies).containsExactly(redisInc);
  }

  @Test
  void testFindByMany() {
    /*
     * A 100 B 200 C 300
     */
    Metadata md1 = new Metadata();
    md1.setDeptId("A");
    md1.setEmployeeId("100");

    Metadata md2 = new Metadata();
    md2.setDeptId("B");
    md2.setEmployeeId("200");

    Metadata md3 = new Metadata();
    md3.setDeptId("C");
    md3.setEmployeeId("300");

    metadataRepo.saveAll(Set.of(md1, md2, md3));

    assertEquals(3, metadataRepo.count());

    Iterable<Metadata> metadata = metadataRepo.findByDeptId(Set.of("C", "B"));
    // NOTE: order of the results will NOT match the order of the ids
    // provided
    assertThat(metadata).contains(md2, md3);

    metadataRepo.deleteAll();
  }

  @Test
  void testTagEscapeChars() {
    Company redis = repository.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = repository.save(Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com"));

    assertEquals(2, repository.count());

    Optional<Company> maybeRedisLabs = repository.findFirstByEmail(redis.getEmail());
    Optional<Company> maybeMicrosoft = repository.findFirstByEmail(microsoft.getEmail());

    assertTrue(maybeRedisLabs.isPresent());
    assertTrue(maybeMicrosoft.isPresent());

    assertEquals(redis, maybeRedisLabs.get());
    assertEquals(microsoft, maybeMicrosoft.get());
  }

  @Test
  void testQueriesAgainstNoData() {
    repository.deleteAll();

    Iterable<Company> all = repository.findAll();
    assertFalse(all.iterator().hasNext());

    Optional<Company> maybeRedisLabs = repository.findById("not-here");
    assertTrue(maybeRedisLabs.isEmpty());

    Optional<Company> maybeMicrosoft = repository.findFirstByEmail("research@microsoft.com");
    assertTrue(maybeMicrosoft.isEmpty());

    Iterable<Company> companies = repository.findAllById(List.of("8675309", "42"));
    assertFalse(companies.iterator().hasNext());

    List<Company> employees = repository.findByEmployees_name("Seymour");
    assertTrue(employees.isEmpty());
  }

  @Test
  void testMaxQueryReturnDefaultsTo10() {
    final List<Company> bunchOfCompanies = new ArrayList<>();
    IntStream.range(1, 100).forEach(i -> {
      Company c = Company.of("Company" + i, 2022, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
          "company" + i + "@inc.com");
      if (i % 2 == 0)
        c.setPubliclyListed(true);
      bunchOfCompanies.add(c);
    });
    repository.saveAll(bunchOfCompanies);

    List<Company> publiclyListed = repository.findByPubliclyListed(true);

    // noinspection ResultOfMethodCallIgnored
    assertAll( //
        () -> assertThat(publiclyListed).hasSize(10), //
        () -> assertThat(publiclyListed).allSatisfy(Company::isPubliclyListed) //
    );
  }

  @Test
  void testFindByTagsIn() {
    Company redis = repository.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    redis.setTags(Set.of("fast", "scalable", "reliable", "database", "nosql"));

    Set<Employee> employees = Sets.newHashSet(Employee.of("Brian Sam-Bodden"), Employee.of("Guy Royse"),
        Employee.of("Justin Castilla"));
    redis.setEmployees(employees);

    Company microsoft = repository.save(Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com"));
    microsoft.setTags(Set.of("innovative", "reliable", "os", "ai"));

    Company tesla = repository.save(
        Company.of("Tesla", 2003, LocalDate.of(2022, 1, 1), new Point(-97.6208903, 30.2210767), "elon@tesla.com"));
    tesla.setTags(Set.of("innovative", "futuristic", "ai"));

    repository.saveAll(List.of(redis, microsoft, tesla));

    List<Company> companies = repository.findByTags(Set.of("reliable"));
    List<String> names = companies.stream().map(Company::getName).toList();

    assertEquals(2, names.size());

    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
  }

  @Test
  void testAuditAnnotationsOnSaveAll() {
    Company redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");
    Company microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
        "research@microsoft.com");

    repository.saveAll(List.of(redis, microsoft));

    microsoft.setPubliclyListed(true);

    repository.saveAll(List.of(microsoft));

    assertEquals(2, repository.count());

    Iterable<Company> companies = repository.findAllById(List.of(redis.getId(), microsoft.getId()));

    assertAll( //
        () -> assertThat(companies).hasSize(2), //
        () -> assertThat(companies).containsExactly(redis, microsoft), //
        () -> assertThat(redis.getCreatedDate()).isNotNull(), //
        () -> assertThat(redis.getLastModifiedDate()).isNull(), //
        () -> assertThat(microsoft.getCreatedDate()).isNotNull(), //
        () -> assertThat(microsoft.getLastModifiedDate()).isNotNull());
  }

  @Test
  void testSetOfIntegersIndexed() {
    DocWithSets dwsoi = DocWithSets.of(Set.of(8, 6, 7, 5, 3, 0, 9), Set.of());
    docWithSetsRepository.save(dwsoi);
    // if count() works we know the index was created
    assertThat(docWithSetsRepository.count()).isEqualTo(1);
  }

  @Test
  void testSearchContainingAnyInSetOfInteger() {
    DocWithSets doc1 = DocWithSets.of(Set.of(8, 6, 7, 5, 3, 0, 9), Set.of());
    DocWithSets doc2 = DocWithSets.of(Set.of(7, 8, 0, 6, 3, 5, 9), Set.of());
    DocWithSets doc3 = DocWithSets.of(Set.of(1, 2, 3, 4), Set.of());
    DocWithSets doc4 = DocWithSets.of(Set.of(1, 3, 5), Set.of());
    docWithSetsRepository.saveAll(List.of(doc1, doc2, doc3, doc4));
    assertThat(docWithSetsRepository.count()).isEqualTo(4);

    var docs = docWithSetsRepository.findByTheNumbersContaining(Set.of(3));
    assertThat(docs).containsOnly(doc1, doc2, doc3, doc4);

    docs = docWithSetsRepository.findByTheNumbersContaining(Set.of(5, 1));
    assertThat(docs).containsOnly(doc1, doc2, doc3, doc4);

    docs = docWithSetsRepository.findByTheNumbersContaining(Set.of(4));
    assertThat(docs).containsOnly(doc3);

    docs = docWithSetsRepository.findByTheNumbersContaining(Set.of(11));
    assertThat(docs).isEmpty();

    docs = docWithSetsRepository.findByTheNumbersContaining(Set.of(8, 6, 7));
    assertThat(docs).containsOnly(doc1, doc2);
  }

  @Test
  void testSearchContainingAllInSetOfInteger() {
    DocWithSets doc1 = DocWithSets.of(Set.of(1, 2, 3, 4), Set.of());
    DocWithSets doc2 = DocWithSets.of(Set.of(1, 2, 3), Set.of());
    DocWithSets doc3 = DocWithSets.of(Set.of(1, 2), Set.of());
    DocWithSets doc4 = DocWithSets.of(Set.of(1), Set.of());
    docWithSetsRepository.saveAll(List.of(doc1, doc2, doc3, doc4));
    assertThat(docWithSetsRepository.count()).isEqualTo(4);

    var docs = docWithSetsRepository.findByTheNumbersContainingAll(Set.of(5));
    assertThat(docs).isEmpty();

    docs = docWithSetsRepository.findByTheNumbersContainingAll(Set.of(1));
    assertThat(docs).containsOnly(doc1, doc2, doc3, doc4);

    docs = docWithSetsRepository.findByTheNumbersContainingAll(Set.of(1, 2));
    assertThat(docs).containsOnly(doc1, doc2, doc3);

    docs = docWithSetsRepository.findByTheNumbersContainingAll(Set.of(1, 2, 3));
    assertThat(docs).containsOnly(doc1, doc2);

    docs = docWithSetsRepository.findByTheNumbersContainingAll(Set.of(1, 2, 3, 4));
    assertThat(docs).containsOnly(doc1);
  }

  @Test
  void testSearchByLocationsNearAgainstLocationsArray() {
    Point point1 = new Point(31.785, 35.213);
    Point point2 = new Point(31.768, 35.178);
    Point point3 = new Point(31.984, 35.827);
    Point point4 = new Point(31.79, 34.638);
    Point point5 = new Point(31.793, 34.639);
    Point point6 = new Point(31.817, 34.648);
    Point point7 = new Point(31.806, 34.638);
    Point point8 = new Point(31.785, 34.65);

    DocWithSets doc1 = DocWithSets.of(Set.of(), Set.of(point1, point2, point3));
    DocWithSets doc2 = DocWithSets.of(Set.of(), Set.of(point4, point5));
    DocWithSets doc3 = DocWithSets.of(Set.of(), Set.of(point6, point7, point8));
    docWithSetsRepository.saveAll(List.of(doc1, doc2, doc3));
    assertThat(docWithSetsRepository.count()).isEqualTo(3);

    Point point = new Point(31.5, 34.5);
    var distance = new Distance(40.0, RedisGeoCommands.DistanceUnit.KILOMETERS);

    var docs = docWithSetsRepository.findByTheLocationsNear(point, distance);
    assertThat(docs).containsOnly(doc2, doc3);
  }

  @Test
  void testSearchContainingAnyInSetOfLocations() {
    Point point1 = new Point(31.785, 35.213);
    Point point2 = new Point(31.768, 35.178);
    Point point3 = new Point(31.984, 35.827);
    Point point4 = new Point(31.79, 34.638);
    Point point5 = new Point(31.793, 34.639);
    Point point6 = new Point(31.817, 34.648);
    Point point7 = new Point(31.806, 34.638);
    Point point8 = new Point(31.785, 34.65);

    DocWithSets doc1 = DocWithSets.of(Set.of(), Set.of(point1, point2, point3));
    DocWithSets doc2 = DocWithSets.of(Set.of(), Set.of(point4, point5));
    DocWithSets doc3 = DocWithSets.of(Set.of(), Set.of(point6, point7, point8));
    docWithSetsRepository.saveAll(List.of(doc1, doc2, doc3));
    assertThat(docWithSetsRepository.count()).isEqualTo(3);

    var docs = docWithSetsRepository.findByTheLocationsContaining(Set.of(point1, point3, point7));
    assertThat(docs).containsOnly(doc1, doc3);
  }

  @Test
  void testSearchContainingAllInSetOfLocations() {
    Point point1 = new Point(-122.064, 37.384);
    Point point2 = new Point(38.7635877, -9.2018309);
    Point point3 = new Point(31.984, 35.827);
    Point point4 = new Point(31.79, 34.638);
    Point point5 = new Point(31.793, 34.639);
    Point point6 = new Point(31.817, 34.648);
    Point point7 = new Point(31.806, 34.638);
    Point point8 = new Point(31.785, 34.65);

    DocWithSets doc1 = DocWithSets.of(Set.of(), Set.of(point1, point2, point3));
    DocWithSets doc2 = DocWithSets.of(Set.of(), Set.of(point1, point2, point4));
    DocWithSets doc3 = DocWithSets.of(Set.of(), Set.of(point1, point5, point6, point7, point8));
    DocWithSets doc4 = DocWithSets.of(Set.of(), Set.of(point2, point5, point6, point7, point8));
    docWithSetsRepository.saveAll(List.of(doc1, doc2, doc3, doc4));
    assertThat(docWithSetsRepository.count()).isEqualTo(4);

    var docs = docWithSetsRepository.findByTheLocationsContainingAll(Set.of(point1, point2));
    assertThat(docs).containsOnly(doc1, doc2);
  }

  @Test
  void testFindByTagsInNestedField() {
    Company redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");
    redis.setMetaList(Set.of(CompanyMeta.of("Redis", 100, Set.of("RedisTag", "CommonTag"))));

    Company microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com");
    microsoft.setMetaList(Set.of(CompanyMeta.of("MS", 50, Set.of("MsTag", "CommonTag"))));

    repository.saveAll(List.of(redis, microsoft));

    assertEquals(2, repository.count());

    List<Company> shouldBeOnlyRedis = repository.findByMetaList_tagValues(Set.of("RedisTag"));
    List<Company> shouldBeOnlyMS = repository.findByMetaList_tagValues(Set.of("MsTag"));
    List<Company> shouldBeBoth = repository.findByMetaList_tagValues(Set.of("CommonTag"));

    assertAll( //
        () -> assertThat(shouldBeOnlyRedis).map(Company::getName).containsExactly("RedisInc"), //
        () -> assertThat(shouldBeOnlyMS).map(Company::getName).containsExactly("Microsoft"), //
        () -> assertThat(shouldBeBoth).map(Company::getName).containsExactlyInAnyOrder("RedisInc",
            "Microsoft") //
    );
  }

  @Test
  void testFindByStringValueInNestedField() {
    Company redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");
    redis.setMetaList(Set.of(CompanyMeta.of("RD", 100, Set.of("RedisTag", "CommonTag"))));

    Company microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com");
    microsoft.setMetaList(Set.of(CompanyMeta.of("MS", 50, Set.of("MsTag", "CommonTag"))));

    repository.saveAll(List.of(redis, microsoft));

    assertEquals(2, repository.count());

    List<Company> shouldBeOnlyRedis = repository.findByMetaList_stringValue("RD");
    List<Company> shouldBeOnlyMS = repository.findByMetaList_stringValue("MS");

    assertAll( //
        () -> assertThat(shouldBeOnlyRedis).map(Company::getName).containsExactly("RedisInc"), //
        () -> assertThat(shouldBeOnlyMS).map(Company::getName).containsExactly("Microsoft") //
    );
  }

  @Test
  void testFindByNumericValueInNestedField() {
    Company redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");
    redis.setMetaList(Set.of(CompanyMeta.of("RD", 100, Set.of("RedisTag", "CommonTag"))));

    Company microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com");
    microsoft.setMetaList(Set.of(CompanyMeta.of("MS", 50, Set.of("MsTag", "CommonTag"))));

    repository.saveAll(List.of(redis, microsoft));

    assertEquals(2, repository.count());

    List<Company> shouldBeOnlyRedis = repository.findByMetaList_numberValue(100);
    List<Company> shouldBeOnlyMS = repository.findByMetaList_numberValue(50);

    assertAll( //
        () -> assertThat(shouldBeOnlyRedis).map(Company::getName).containsExactly("RedisInc"), //
        () -> assertThat(shouldBeOnlyMS).map(Company::getName).containsExactly("Microsoft") //
    );
  }
}
