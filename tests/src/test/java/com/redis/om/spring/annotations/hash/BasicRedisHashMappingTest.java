package com.redis.om.spring.annotations.hash;

import com.google.common.collect.Ordering;
import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.document.model.MyJavaEnum;
import com.redis.om.spring.fixtures.hash.model.*;
import com.redis.om.spring.fixtures.hash.repository.*;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.EntityStream;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.aggr.AggregationResult;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.redis.om.spring.util.ObjectUtils.getKey;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.with;

@SuppressWarnings("SpellCheckingInspection")
class BasicRedisHashMappingTest extends AbstractBaseEnhancedRedisTest {
  static Person john;
  static Person gray;
  static Person terryg;
  static Person eric;
  static Person terryj;
  static Person michael;

  @Autowired
  PersonRepository personRepo;

  @Autowired
  CompanyRepository companyRepo;

  @Autowired
  NonIndexedHashRepository nihRepo;

  @Autowired
  HashWithEnumRepository hashWithEnumRepository;

  @Autowired
  StudentRepository studentRepository;

  @Autowired
  CustomIndexHashRepository customIndexHashRepository;

  @Autowired
  EntityStream es;

  @Autowired
  JedisConnectionFactory jedisConnectionFactory;

  private UnifiedJedis jedis;

  @BeforeEach
  void createTestDataIfNeeded() {
    jedis = new JedisPooled(Objects.requireNonNull(jedisConnectionFactory.getPoolConfig()),
        jedisConnectionFactory.getHostName(), jedisConnectionFactory.getPort());

    flushSearchIndexFor(Company.class);
    flushSearchIndexFor(Person.class);
    john = Person.of("John Cleese", "john.cleese@mp.com", "john");
    john.setRoles(Set.of("Llama lecturer", "Armless Officer", "Albatross Seller"));
    john.setFavoriteFoods(Set.of("Fish and Chips", "Bangers and Mash", "Full English Breakfast"));

    gray = Person.of("Graham Chapman", "graham.chapman@mp.com", "graham");
    gray.setRoles(Set.of("Brian", "King Arthur", "The Colonel"));
    gray.setFavoriteFoods(Set.of("Sunday Roast", "Toad in the Hole"));

    terryg = Person.of("Terry Gilliam", "terry.gilliam@mp.com", "terry");
    terryg.setRoles(Set.of("Patsy", "Green Knight", "Bridgekeeper"));
    terryg.setFavoriteFoods(Set.of("Shepherd’s Pie", "Cottage Pie", "Steak and Kidney Pie"));

    eric = Person.of("Eric Idle", "eric.idle@mp.com", "eric");
    eric.setRoles(
        Set.of("Sir Robin the-not-quite-so-brave-as-Sir-Lancelot", "Lancelot's squire Concorde", "Roger the Shrubber"));
    eric.setFavoriteFoods(Set.of("Cottage Pie", "Steak and Kidney Pie"));

    terryj = Person.of("Terry Jones", "terry.jones@mp.com", "terry");
    terryj.setRoles(Set.of("Mandy Cohen", "Dennis's Mother", "Bert"));
    terryj.setFavoriteFoods(Set.of("Fish and Chips", "Toad in the Hole", "Cottage Pie"));

    michael = Person.of("Michael Palin", "michael.palin@mp.com", "michael");
    michael.setRoles(Set.of("Mr. Big Nose", "Second Tramp", "First Swallow-Savvy Guard"));
    michael.setFavoriteFoods(Set.of("Steak and Kidney Pie", "Sunday Roast", "Bangers and Mash"));

    personRepo.saveAll(List.of(john, gray, terryg, eric, terryj, michael));

    studentRepository.deleteAll();
    List<Student> students = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      var student = Student.of("Student" + i, i != 2 ? LocalDateTime.now() : LocalDateTime.of(2023, 6, 1, 1, 1, 1));
      student.setId((long) i);
      students.add(student);
    }
    studentRepository.saveAll(students);
  }

  @Test
  void testDeleteAll() {
    assertThat(personRepo.count()).isEqualTo(6);
    personRepo.deleteAll();
    with() //
        .pollInterval(Duration.ofMillis(100)).and() //
        .with().pollDelay(20, MILLISECONDS) //
        .await("repository cleared").until(() -> personRepo.count() == 0);
    assertThat(personRepo.count()).isZero();
  }

  @Test
  void testDeleteOne() {
    assertThat(personRepo.count()).isEqualTo(6);
    personRepo.delete(eric);
    assertThat(personRepo.count()).isEqualTo(5);
    personRepo.deleteAll();
  }

  @Test
  void testThatIndexedSimpleCollectionsAreSerializedAsCSV() {
    String hashKey = "people:" + eric.getId();
    String ericRolesRaw = Objects.requireNonNull(template.opsForHash().get(hashKey, "roles")).toString();
    Set<String> ericRoles = Arrays.asList(ericRolesRaw.split("\\|")).stream().map(Object::toString)
        .map(QueryUtils::unescape).collect(Collectors.toSet());
    assertThat(ericRoles).containsExactlyInAnyOrderElementsOf(eric.getRoles());
  }

  @Test
  void testThatIndexedSimpleCollectionsAreDeserializedFromCSV() {
    Optional<Person> maybeEric = personRepo.findById(eric.getId());
    assertThat(maybeEric).isPresent();
    assertThat(maybeEric.get().getRoles()).containsExactlyInAnyOrderElementsOf(eric.getRoles());
  }

  @Test
  void testThatNonIndexedCollectionsAreSerializedAsIndividualHashEntriesPerElement() {
    String hashKey = "people:" + eric.getId();

    String ericFavFood0 = Objects.requireNonNull(template.opsForHash().get(hashKey, "favoriteFoods.[0]")).toString();
    String ericFavFood1 = Objects.requireNonNull(template.opsForHash().get(hashKey, "favoriteFoods.[1]")).toString();
    String ericFavFood2 = (String) template.opsForHash().get(hashKey, "favoriteFoods.[2]");

    assertThat(ericFavFood0).isIn(eric.getFavoriteFoods());
    assertThat(ericFavFood1).isIn(eric.getFavoriteFoods());
    assertThat(ericFavFood2).isNull();
  }

  @Test
  void testByTags() {
    assertThat(personRepo.count()).isEqualTo(6);
    Set<String> roles = Set.of("Llama lecturer", "Green Knight");
    Iterable<Person> persons = personRepo.findByRoles(roles);
    assertThat(persons).containsExactlyInAnyOrder(john, terryg);
  }

  @Test
  void testAggregationAnnotation01() {
    AggregationResult results = personRepo.allNamesInUppercase();
    List<String> upcasedNames = results.getResults().stream().map(m -> m.get("upcasedName")).map(Object::toString)
        .toList();
    assertThat(upcasedNames).containsExactlyInAnyOrder("JOHN CLEESE", //
        "GRAHAM CHAPMAN", //
        "TERRY GILLIAM", //
        "ERIC IDLE", //
        "TERRY JONES", //
        "MICHAEL PALIN" //
    );
  }

  //
  // Test using Company entity
  //

  @Test
  void testBasicCrudOperations() {
    Company redis = companyRepo.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = companyRepo.save(
        Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
            "research@microsoft.com"));

    assertEquals(2, companyRepo.count());

    Optional<Company> maybeRedisLabs = companyRepo.findById(redis.getId());
    Optional<Company> maybeMicrosoft = companyRepo.findById(microsoft.getId());

    assertTrue(maybeRedisLabs.isPresent());
    assertTrue(maybeMicrosoft.isPresent());

    assertEquals(redis, maybeRedisLabs.get());
    assertEquals(microsoft, maybeMicrosoft.get());

    // delete given an entity
    companyRepo.delete(microsoft);

    assertEquals(1, companyRepo.count());

    // delete given an id
    companyRepo.deleteById(redis.getId());

    assertEquals(0, companyRepo.count());
  }

  @Test
  void testFindAllById() {
    Company redis = companyRepo.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = companyRepo.save(
        Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
            "research@microsoft.com"));

    assertEquals(2, companyRepo.count());

    Iterable<Company> companies = companyRepo.findAllById(List.of(redis.getId(), microsoft.getId()));

    assertAll( //
        () -> assertThat(companies).hasSize(2), //
        () -> assertThat(companies).containsExactly(redis, microsoft) //
    );
  }

  @Test
  void testAuditAnnotations() {
    Company redis = companyRepo.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = companyRepo.save(
        Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
            "research@microsoft.com"));

    // created dates should not be null
    assertNotNull(redis.getCreatedDate());
    assertNotNull(microsoft.getCreatedDate());

    // created dates should be null upon creation
    assertNull(redis.getLastModifiedDate());
    assertNull(microsoft.getLastModifiedDate());

    companyRepo.saveAll(List.of(redis, microsoft));

    // last modified dates should not be null after a second save
    assertNotNull(redis.getLastModifiedDate());
    assertNotNull(microsoft.getLastModifiedDate());
  }

  @Test
  void testTagEscapeChars() {
    Company redis = companyRepo.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = companyRepo.save(
        Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
            "research@microsoft.com"));

    assertEquals(2, companyRepo.count());

    Optional<Company> maybeRedisLabs = companyRepo.findFirstByEmail(redis.getEmail());
    Optional<Company> maybeMicrosoft = companyRepo.findFirstByEmail(microsoft.getEmail());

    assertTrue(maybeRedisLabs.isPresent());
    assertTrue(maybeMicrosoft.isPresent());

    assertEquals(redis, maybeRedisLabs.get());
    assertEquals(microsoft, maybeMicrosoft.get());
  }

  @Test
  void testQueriesAgainstNoData() {
    Iterable<Company> all = companyRepo.findAll();
    assertFalse(all.iterator().hasNext());

    Optional<Company> maybeRedisLabs = companyRepo.findById("not-here");
    assertTrue(maybeRedisLabs.isEmpty());

    Optional<Company> maybeMicrosoft = companyRepo.findFirstByEmail("research@microsoft.com");
    assertTrue(maybeMicrosoft.isEmpty());

    Iterable<Company> companies = companyRepo.findAllById(List.of("8675309", "42"));
    assertFalse(companies.iterator().hasNext());
  }

  @Test
  void testMaxQueryReturnMaxDefaultsTo10000() {
    final List<Company> bunchOfCompanies = new ArrayList<>();
    IntStream.range(1, 100).forEach(i -> {
      Company c = Company.of("Company" + i, 2022, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
          "company" + i + "@inc.com");
      if (i % 2 == 0)
        c.setPubliclyListed(true);
      bunchOfCompanies.add(c);
    });
    companyRepo.saveAll(bunchOfCompanies);

    List<Company> publiclyListed = companyRepo.findByPubliclyListed(true);

    //noinspection ResultOfMethodCallIgnored
    assertAll( //
        () -> assertThat(publiclyListed).hasSize(49), //
        () -> assertThat(publiclyListed).allSatisfy(Company::isPubliclyListed) //
    );
  }

  @Test
  void testFindByTagsIn() {
    Company redis = companyRepo.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    redis.setTags(Set.of("fast", "scalable", "reliable", "database", "nosql"));

    Company microsoft = companyRepo.save(
        Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
            "research@microsoft.com"));
    microsoft.setTags(Set.of("innovative", "reliable", "os", "ai"));

    Company tesla = companyRepo.save(
        Company.of("Tesla", 2003, LocalDate.of(2022, 1, 1), new Point(-97.6208903, 30.2210767), "elon@tesla.com"));
    tesla.setTags(Set.of("innovative", "futuristic", "ai"));

    companyRepo.saveAll(List.of(redis, microsoft, tesla));

    List<Company> companies = companyRepo.findByTags(Set.of("reliable"));
    List<String> names = companies.stream().map(Company::getName).toList();

    assertEquals(2, names.size());

    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
  }

  @Test
  void testBasicPagination() {
    final List<Company> bunchOfCompanies = new ArrayList<>();
    IntStream.range(1, 25).forEach(i -> {
      Company c = Company.of("Company" + i, 2022, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
          "company" + i + "@inc.com");
      bunchOfCompanies.add(c);
    });
    companyRepo.saveAll(bunchOfCompanies);

    Pageable pageRequest = PageRequest.of(0, 5);

    Page<Company> result = companyRepo.findAll(pageRequest);

    assertEquals(5, result.getTotalPages());
    assertEquals(24, result.getTotalElements());
    assertEquals(5, result.getContent().size());
  }

  @Test
  void testBasicPaginationWithSorting() {
    final List<Company> bunchOfCompanies = new ArrayList<>();
    IntStream.range(1, 25).forEach(i -> {
      Company c = Company.of("Company" + i, 2022, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
          "company" + i + "@inc.com");
      bunchOfCompanies.add(c);
    });
    companyRepo.saveAll(bunchOfCompanies);

    Pageable pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());

    Page<Company> result = companyRepo.findAll(pageRequest);

    assertEquals(3, result.getTotalPages());
    assertEquals(24, result.getTotalElements());
    assertEquals(10, result.getContent().size());

    List<String> companyNames = result.get().map(Company::getName).collect(Collectors.toList());
    assertThat(Ordering.<String>natural().isOrdered(companyNames)).isTrue();
  }

  @Test
  void testFindAllWithSorting() {
    final List<Company> bunchOfCompanies = new ArrayList<>();
    IntStream.range(1, 25).forEach(i -> {
      Company c = Company.of("Company" + i, 2022, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
          "company" + i + "@inc.com");
      bunchOfCompanies.add(c);
    });
    companyRepo.saveAll(bunchOfCompanies);

    Iterable<Company> result = companyRepo.findAll(Sort.by("name").ascending());

    List<String> companyNames = StreamSupport.stream(result.spliterator(), false).map(Company::getName)
        .collect(Collectors.toList());
    assertThat(Ordering.<String>natural().isOrdered(companyNames)).isTrue();
  }

  @Test
  void testFindAllWithSortingById() {
    final List<Company> bunchOfCompanies = new ArrayList<>();
    IntStream.range(1, 25).forEach(i -> {
      Company c = Company.of("Company" + i, 2022, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
          "company" + i + "@inc.com");
      bunchOfCompanies.add(c);
    });
    companyRepo.saveAll(bunchOfCompanies);

    Iterable<Company> result = companyRepo.findAll(Sort.by("id").ascending());

    List<String> ids = StreamSupport.stream(result.spliterator(), false).map(Company::getId)
        .collect(Collectors.toList());
    assertThat(Ordering.<String>natural().isOrdered(ids)).isTrue();
  }

  @Test
  void testFindAllWithSortingByIdUsingMetamodel() {
    final List<Company> bunchOfCompanies = new ArrayList<>();
    IntStream.range(1, 25).forEach(i -> {
      Company c = Company.of("Company" + i, 2022, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
          "company" + i + "@inc.com");
      bunchOfCompanies.add(c);
    });
    companyRepo.saveAll(bunchOfCompanies);

    Iterable<Company> result = companyRepo.findAll(
        com.redis.om.spring.repository.query.Sort.by(Company$.ID).ascending());

    List<String> ids = StreamSupport.stream(result.spliterator(), false).map(Company::getId)
        .collect(Collectors.toList());
    assertThat(Ordering.<String>natural().isOrdered(ids)).isTrue();
  }

  @Test
  void testBasicPaginationForNonIndexedEntity() {
    final List<NonIndexedHash> bunchOfNihs = new ArrayList<>();
    IntStream.range(1, 25).forEach(i -> {
      NonIndexedHash nih = NonIndexedHash.of("Nih" + i);
      bunchOfNihs.add(nih);
    });
    nihRepo.saveAll(bunchOfNihs);

    Pageable pageRequest = PageRequest.of(0, 5);

    Page<NonIndexedHash> result = nihRepo.findAll(pageRequest);

    assertEquals(5, result.getTotalPages());
    assertEquals(24, result.getTotalElements());
    assertEquals(5, result.getContent().size());
  }

  @Test
  void testTagValsQuery() {
    Company redis = companyRepo.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    redis.setTags(Set.of("fast", "scalable", "reliable", "database", "nosql"));

    Company microsoft = companyRepo.save(
        Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
            "research@microsoft.com"));
    microsoft.setTags(Set.of("innovative", "reliable", "os", "ai"));

    Company tesla = companyRepo.save(
        Company.of("Tesla", 2003, LocalDate.of(2022, 1, 1), new Point(-97.6208903, 30.2210767), "elon@tesla.com"));
    tesla.setTags(Set.of("innovative", "futuristic", "ai"));

    companyRepo.saveAll(List.of(redis, microsoft, tesla));

    Set<String> allTags = Set.of("fast", "scalable", "reliable", "database", "nosql", "innovative", "os", "ai",
        "futuristic");

    Iterable<String> tags = companyRepo.getAllTags();
    assertThat(tags).containsAll(allTags);
  }

  @Test
  void testFullTextSearch() {
    Company redis = companyRepo.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    redis.setTags(Set.of("fast", "scalable", "reliable", "database", "nosql"));

    Company microsoft = companyRepo.save(
        Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
            "research@microsoft.com"));
    microsoft.setTags(Set.of("innovative", "reliable", "os", "ai"));

    Company tesla = companyRepo.save(
        Company.of("Tesla", 2003, LocalDate.of(2022, 1, 1), new Point(-97.6208903, 30.2210767), "elon@tesla.com"));
    tesla.setTags(Set.of("innovative", "futuristic", "ai"));

    companyRepo.saveAll(List.of(redis, microsoft, tesla));

    String q = "red*";
    Iterable<Company> permits = companyRepo.search(q);
    assertThat(permits).containsExactly(redis);
  }

  @Test
  void testFindByFieldWithExplicitGeoIndexedAnnotation() {
    Company redis = companyRepo.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    redis.setTags(Set.of("fast", "scalable", "reliable", "database", "nosql"));

    Company microsoft = companyRepo.save(
        Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
            "research@microsoft.com"));
    microsoft.setTags(Set.of("innovative", "reliable", "os", "ai"));

    Company tesla = companyRepo.save(
        Company.of("Tesla", 2003, LocalDate.of(2022, 1, 1), new Point(-97.6208903, 30.2210767), "elon@tesla.com"));
    tesla.setTags(Set.of("innovative", "futuristic", "ai"));

    companyRepo.saveAll(List.of(redis, microsoft, tesla));

    Point point = new Point(-122.064, 37.384);
    var distance = new Distance(30.0, DistanceUnit.MILES);
    List<Company> results = companyRepo.findByLocationNear(point, distance);

    assertThat(results).containsExactly(redis);
  }

  @Test
  void testFindAllUnpaged() {
    final List<Company> bunchOfCompanies = new ArrayList<>();
    IntStream.range(1, 5).forEach(i -> {
      Company c = Company.of("Company" + i, 2022, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
          "company" + i + "@inc.com");
      bunchOfCompanies.add(c);
    });
    companyRepo.saveAll(bunchOfCompanies);

    Page<Company> result = companyRepo.findAll(Pageable.unpaged());

    assertEquals(1, result.getTotalPages());
    assertEquals(4, result.getTotalElements());
    assertEquals(4, result.getContent().size());
  }

  @Test
  void testUpdateSingleField() {
    Company redisInc = companyRepo.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    companyRepo.updateField(redisInc, Company$.NAME, "Redis");

    Optional<Company> maybeRedis = companyRepo.findById(redisInc.getId());

    assertTrue(maybeRedis.isPresent());

    assertEquals("Redis", maybeRedis.get().getName());
  }

  @Test
  void testGetFieldsByIds() {
    Company redis = companyRepo.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    Company microsoft = companyRepo.save(
        Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
            "research@microsoft.com"));

    Iterable<String> ids = List.of(redis.getId(), microsoft.getId());
    Iterable<String> companyNames = companyRepo.getFieldsByIds(ids, Company$.NAME);
    assertThat(companyNames).containsExactly(redis.getName(), microsoft.getName());
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  void testPersistingEntityMustNotBeNull() {
    IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
        () -> companyRepo.save(null));

    Assertions.assertEquals("Entity must not be null", exception.getMessage());
  }

  @Test
  void testAuditAnnotationsOnSaveAll() {
    Company redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");
    Company microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
        "research@microsoft.com");

    companyRepo.saveAll(List.of(redis, microsoft));

    microsoft.setPubliclyListed(true);

    companyRepo.saveAll(List.of(microsoft));

    assertEquals(2, companyRepo.count());

    Iterable<Company> companies = companyRepo.findAllById(List.of(redis.getId(), microsoft.getId()));

    assertAll( //
        () -> assertThat(companies).hasSize(2), //
        () -> assertThat(companies).containsExactly(redis, microsoft), //
        () -> assertThat(redis.getCreatedDate()).isNotNull(), //
        () -> assertThat(redis.getLastModifiedDate()).isNull(), //
        () -> assertThat(microsoft.getCreatedDate()).isNotNull(), //
        () -> assertThat(microsoft.getLastModifiedDate()).isNotNull());
  }

  @Test
  void testFindByTagValueStartingWith() {
    Company redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");

    Company microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
        "research@microsoft.com");

    companyRepo.saveAll(List.of(redis, microsoft));

    assertEquals(2, companyRepo.count());

    List<Company> shouldBeOnlyRedis = companyRepo.findByEmailStartingWith("stack");
    List<Company> shouldBeOnlyMS = companyRepo.findByEmailStartingWith("res");

    assertAll( //
        () -> assertThat(shouldBeOnlyRedis).map(Company::getName).containsExactly("RedisInc"), //
        () -> assertThat(shouldBeOnlyMS).map(Company::getName).containsExactly("Microsoft") //
    );
  }

  @Test
  void testFindByTagValueEndingWith() {
    Company redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");

    Company microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
        "research@microsoft.com");

    companyRepo.saveAll(List.of(redis, microsoft));

    assertEquals(2, companyRepo.count());

    List<Company> shouldBeOnlyRedis = companyRepo.findByEmailEndingWith("s.com");
    List<Company> shouldBeOnlyMS = companyRepo.findByEmailEndingWith("t.com");

    assertAll( //
        () -> assertThat(shouldBeOnlyRedis).map(Company::getName).containsExactly("RedisInc"), //
        () -> assertThat(shouldBeOnlyMS).map(Company::getName).containsExactly("Microsoft") //
    );
  }

  @Test
  void testOrderByInMethodName() {
    companyRepo.saveAll(
        List.of(Company.of("aaa", 2000, LocalDate.of(2020, 5, 1), new Point(-122.066540, 37.377690), "aaa@aaa.com"),
            Company.of("bbb", 2000, LocalDate.of(2021, 6, 2), new Point(-122.066540, 37.377690), "bbb@bbb.com"),
            Company.of("ccc", 2000, LocalDate.of(2022, 7, 3), new Point(-122.066540, 37.377690), "ccc@ccc.com")));

    List<Company> byNameAsc = companyRepo.findByYearFoundedOrderByNameAsc(2000);
    List<Company> byNameDesc = companyRepo.findByYearFoundedOrderByNameDesc(2000);

    assertAll( //
        () -> assertThat(byNameAsc).extracting("name").containsExactly("aaa", "bbb", "ccc"),
        () -> assertThat(byNameDesc).extracting("name").containsExactly("ccc", "bbb", "aaa"));
  }

  @Test
  void testEnumsAreIndexed() {
    HashWithEnum doc1 = HashWithEnum.of(MyJavaEnum.VALUE_1);
    HashWithEnum doc2 = HashWithEnum.of(MyJavaEnum.VALUE_2);
    HashWithEnum doc3 = HashWithEnum.of(MyJavaEnum.VALUE_3);

    hashWithEnumRepository.saveAll(List.of(doc1, doc2, doc3));

    List<HashWithEnum> onlyVal1 = hashWithEnumRepository.findByEnumProp(MyJavaEnum.VALUE_1);
    List<HashWithEnum> onlyVal2 = hashWithEnumRepository.findByEnumProp(MyJavaEnum.VALUE_2);
    List<HashWithEnum> onlyVal3 = hashWithEnumRepository.findByEnumProp(MyJavaEnum.VALUE_3);

    assertAll( //
        () -> assertThat(onlyVal1).containsExactly(doc1), //
        () -> assertThat(onlyVal2).containsExactly(doc2), //
        () -> assertThat(onlyVal3).containsExactly(doc3)  //
    );
  }

  @Test
  void testFindByPropertyWithAliasWithHyphens() {
    List<Student> result = studentRepository.findByUserName("Student2");

    assertAll( //
        () -> assertThat(result).hasSize(1), //
        () -> assertThat(result).extracting("userName").containsExactly("Student2") //
    );
  }

  @Test
  void testFindByPropertyWithAliasWithHyphensAndOrderBy() {
    LocalDateTime beginLocalDateTime = LocalDateTime.of(2023, 1, 1, 1, 1, 1);
    LocalDateTime endLocalDateTime = LocalDateTime.of(2023, 12, 1, 1, 1, 1);
    List<Student> result = studentRepository.findByUserNameAndEventTimestampBetweenOrderByEventTimestampAsc("Student2",
        beginLocalDateTime, endLocalDateTime);

    assertAll( //
        () -> assertThat(result).hasSize(1), //
        () -> assertThat(result).extracting("userName").containsExactly("Student2") //
    );
  }

  @Test
  void testQBEWithAliasWithHyphensAndOrderBy() {
    Function<FetchableFluentQuery<Student>, Student> sortFunction = query -> query.sortBy(
        Sort.by("Event-Timestamp").descending()).firstValue();

    var matcher = ExampleMatcher.matching().withMatcher("userName", ExampleMatcher.GenericPropertyMatcher::exact);

    var student = new Student();
    student.setUserName("Student2");

    Student result = studentRepository.findFirstByPropertyOrderByEventTimestamp(student, matcher, sortFunction);
    assertThat(result.getUserName()).isEqualTo("Student2");
  }

  //customIndexHashRepository

  @Test
  void testCustomIndexName() {
    // CustomIndexHash is a Hash that has a custom index name defined in the @IndexingOptions annotation
    var indices = jedis.ftList();
    assertThat(indices).contains("MyCustomHashIndex");
    assertThat(indices).contains("myIndexStudent");
  }

  @Test
  void testFreeFormTextSearchOrderIssue() {
    customIndexHashRepository.deleteAll();
    CustomIndexHash redis1 = customIndexHashRepository.save(CustomIndexHash.of("Redis", "wwwabccom"));
    CustomIndexHash redis2 = customIndexHashRepository.save(CustomIndexHash.of("Redis", "wwwxyznet"));
    CustomIndexHash microsoft1 = customIndexHashRepository.save(CustomIndexHash.of("Microsoft", "wwwabcnet"));
    CustomIndexHash microsoft2 = customIndexHashRepository.save(CustomIndexHash.of("Microsoft", "wwwxyzcom"));

    var withFreeTextFirst = es.of(CustomIndexHash.class).filter("*co*").filter(CustomIndexHash$.FIRST.eq("Microsoft"))
        .collect(Collectors.toList());

    var withFreeTextLast = es.of(CustomIndexHash.class).filter(CustomIndexHash$.FIRST.eq("Microsoft")).filter("*co*")
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(withFreeTextLast).containsExactly(microsoft2),
        () -> assertThat(withFreeTextFirst).containsExactly(microsoft2));
  }

  @Test
  void testRepositoryGetKeyFor() {
    Company redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");

    Company microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
        "research@microsoft.com");

    companyRepo.saveAll(List.of(redis, microsoft));

    String redisKey = companyRepo.getKeyFor(redis);
    String microsoftKey = companyRepo.getKeyFor(microsoft);
    assertThat(redisKey).isEqualTo(getKey(Company.class.getName(), redis.getId()));
    assertThat(microsoftKey).isEqualTo(getKey(Company.class.getName(), microsoft.getId()));
  }
}
