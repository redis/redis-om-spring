package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.Company;
import com.redis.om.spring.fixtures.document.model.Company$;
import com.redis.om.spring.fixtures.document.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import redis.clients.jedis.search.aggr.SortedField.SortOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for conditional filtering methods in EntityStream for Document entities.
 */
public class EntityStreamConditionalFiltersDocumentTest extends AbstractBaseDocumentTest {

  @Autowired
  CompanyRepository repository;

  @Autowired
  EntityStream entityStream;

  String redisId;
  String microsoftId;
  String teslaId;

  @BeforeEach
  void cleanUp() {
    if (repository.count() == 0) {
      Company redis = repository.save(Company.of(
          "RedisInc", 2011,
          LocalDate.of(2021, 5, 1),
          new Point(-122.066540, 37.377690), "stack@redis.com"
      ));
      redis.setTags(Set.of("fast", "scalable", "reliable", "database", "nosql"));

      Company microsoft = repository.save(Company.of(
          "Microsoft", 1975,
          LocalDate.of(2022, 8, 15),
          new Point(-122.124500, 47.640160), "research@microsoft.com"
      ));
      microsoft.setTags(Set.of("innovative", "reliable", "os", "ai"));

      Company tesla = repository.save(Company.of(
          "Tesla", 2003,
          LocalDate.of(2022, 1, 1),
          new Point(-97.6208903, 30.2210767), "elon@tesla.com"
      ));
      tesla.setTags(Set.of("innovative", "futuristic", "ai"));

      repository.saveAll(List.of(redis, microsoft, tesla));
    }

    List<Company> saved = repository.findAll();
    redisId = saved.get(0).getId();
    microsoftId = saved.get(1).getId();
    teslaId = saved.get(2).getId();
  }

  @Test
  void testFilterIfNotNull() {
    // With non-null value (should apply filter)
    Integer yearToFind = 2011;
    List<Company> companies = entityStream.of(Company.class)
        .filterIfNotNull(yearToFind, () -> Company$.YEAR_FOUNDED.eq(yearToFind))
        .collect(Collectors.toList());

    assertThat(companies).hasSize(1);
    assertThat(companies.get(0).getName()).isEqualTo("RedisInc");

    // With null value (should not apply filter)
    Integer nullYear = null;
    companies = entityStream.of(Company.class)
        .filterIfNotNull(nullYear, () -> Company$.YEAR_FOUNDED.eq(2011))
        .collect(Collectors.toList());

    assertThat(companies).hasSize(3);
  }

  @Test
  void testFilterIfNotBlank() {
    // With non-blank string (should apply filter)
    String nameToFind = "RedisInc";
    List<Company> companies = entityStream.of(Company.class)
        .filterIfNotBlank(nameToFind, () -> Company$.NAME.eq(nameToFind))
        .collect(Collectors.toList());

    assertThat(companies).hasSize(1);
    assertThat(companies.get(0).getName()).isEqualTo("RedisInc");

    // With blank string (should not apply filter)
    String blankName = "   ";
    companies = entityStream.of(Company.class)
        .filterIfNotBlank(blankName, () -> Company$.NAME.eq("RedisInc"))
        .collect(Collectors.toList());

    assertThat(companies).hasSize(3);

    // With empty string (should not apply filter)
    String emptyName = "";
    companies = entityStream.of(Company.class)
        .filterIfNotBlank(emptyName, () -> Company$.NAME.eq("RedisInc"))
        .collect(Collectors.toList());

    assertThat(companies).hasSize(3);

    // With null string (should not apply filter)
    String nullName = null;
    companies = entityStream.of(Company.class)
        .filterIfNotBlank(nullName, () -> Company$.NAME.eq("RedisInc"))
        .collect(Collectors.toList());

    assertThat(companies).hasSize(3);
  }

  @Test
  void testFilterIfPresent() {
    // With present optional (should apply filter)
    Optional<String> presentName = Optional.of("RedisInc");
    List<Company> companies = entityStream.of(Company.class)
        .filterIfPresent(presentName, () -> Company$.NAME.eq("RedisInc"))
        .collect(Collectors.toList());

    assertThat(companies).hasSize(1);
    assertThat(companies.get(0).getName()).isEqualTo("RedisInc");

    // With empty optional (should not apply filter)
    Optional<String> emptyOptional = Optional.empty();
    companies = entityStream.of(Company.class)
        .filterIfPresent(emptyOptional, () -> Company$.NAME.eq("RedisInc"))
        .collect(Collectors.toList());

    assertThat(companies).hasSize(3);
  }

  @Test
  void testCombiningMultipleFilters() {
    // Setup test parameters
    String nameToFind = "RedisInc";
    String nullTag = null;
    Integer yearToFind = 2011;
    Integer nullYear = null;

    // Combined usage of all filter types
    List<Company> companies = entityStream.of(Company.class)
        .filterIfNotBlank(nameToFind, () -> Company$.NAME.eq(nameToFind))
        .filterIfNotBlank(nullTag, () -> Company$.TAGS.containsAll("database"))
        .filterIfNotNull(yearToFind, () -> Company$.YEAR_FOUNDED.eq(yearToFind))
        .filterIfNotNull(nullYear, () -> Company$.YEAR_FOUNDED.eq(1975))
        .collect(Collectors.toList());

    assertThat(companies).hasSize(1);
    assertThat(companies.get(0).getName()).isEqualTo("RedisInc");
    assertThat(companies.get(0).getYearFounded()).isEqualTo(2011);
  }

  @Test
  void testCustomerUseCaseExample() {
    // Customer's original approach with ternary operator
    String generalClass = "database";
    String isoGroup = null; // simulate not provided
    Integer nominalLength = 2011; // matching year_founded for test
    Integer nominalHeight = null; // simulate not provided

    // Original approach with ternary operator
    SearchStream<Company> stream1 = entityStream.of(Company.class);
    List<Company> resultOriginal = stream1
        .filter(generalClass != null && !generalClass.isBlank() ?
            Company$.TAGS.containsAll(generalClass) : company -> true)
        .filter(isoGroup != null && !isoGroup.isBlank() ?
            Company$.TAGS.containsAll(isoGroup) : company -> true)
        .filter(nominalLength != null ?
            Company$.YEAR_FOUNDED.eq(nominalLength) : company -> true)
        .filter(nominalHeight != null ?
            Company$.YEAR_FOUNDED.eq(nominalHeight) : company -> true)
        .sorted(Company$.NAME, SortOrder.ASC)
        .collect(Collectors.toList());

    // New approach with conditional filter methods
    SearchStream<Company> stream2 = entityStream.of(Company.class);
    List<Company> resultNew = stream2
        .filterIfNotBlank(generalClass, () -> Company$.TAGS.containsAll(generalClass))
        .filterIfNotBlank(isoGroup, () -> Company$.TAGS.containsAll(isoGroup))
        .filterIfNotNull(nominalLength, () -> Company$.YEAR_FOUNDED.eq(nominalLength))
        .filterIfNotNull(nominalHeight, () -> Company$.YEAR_FOUNDED.eq(nominalHeight))
        .sorted(Company$.NAME, SortOrder.ASC)
        .collect(Collectors.toList());

    // Both approaches should yield same results
    assertThat(resultNew.size()).isEqualTo(resultOriginal.size());
    if (!resultOriginal.isEmpty() && !resultNew.isEmpty()) {
      assertThat(resultNew.get(0).getId()).isEqualTo(resultOriginal.get(0).getId());
    }

    // Optional version
    SearchStream<Company> stream3 = entityStream.of(Company.class);
    List<Company> resultWithOptionals = stream3
        .filterIfPresent(Optional.ofNullable(generalClass).filter(gc -> !gc.isBlank()),
            () -> Company$.TAGS.containsAll(generalClass))
        .filterIfPresent(Optional.ofNullable(isoGroup).filter(iso -> !iso.isBlank()),
            () -> Company$.TAGS.containsAll(isoGroup))
        .filterIfPresent(Optional.ofNullable(nominalLength),
            () -> Company$.YEAR_FOUNDED.eq(nominalLength))
        .filterIfPresent(Optional.ofNullable(nominalHeight),
            () -> Company$.YEAR_FOUNDED.eq(nominalHeight))
        .sorted(Company$.NAME, SortOrder.ASC)
        .collect(Collectors.toList());

    // Optional approach should also yield same results
    assertThat(resultWithOptionals.size()).isEqualTo(resultOriginal.size());
    if (!resultOriginal.isEmpty() && !resultWithOptionals.isEmpty()) {
      assertThat(resultWithOptionals.get(0).getId()).isEqualTo(resultOriginal.get(0).getId());
    }
  }
}