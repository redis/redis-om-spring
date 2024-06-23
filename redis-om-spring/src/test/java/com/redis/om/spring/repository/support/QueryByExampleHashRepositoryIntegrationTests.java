package com.redis.om.spring.repository.support;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.fixtures.hash.model.CityHash;
import com.redis.om.spring.fixtures.hash.model.PersonHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.redis.repository.core.MappingRedisEntityInformation;
import org.springframework.data.redis.repository.support.QueryByExampleRedisExecutor;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * Port of org.springframework.data.redis.repository.support.QueryByExampleRedisExecutorIntegrationTests
 * from Spring Data Redis.
 * Integration tests for {@link QueryByExampleRedisExecutor}.
 *
 * @author Mark Paluch
 * @author Christoph Strobl
 * @author John Blum
 * @author Brian Sam-Bodden
 */
class QueryByExampleHashRepositoryIntegrationTests extends AbstractBaseEnhancedRedisTest {

  private final RedisMappingContext mappingContext = new RedisMappingContext();

  private PersonHash walt, hank, gus;

  private SimpleRedisEnhancedRepository<PersonHash, String> repository;

  @BeforeEach
  void before() {

    repository = new SimpleRedisEnhancedRepository<>(getEntityInformation(PersonHash.class),
        new KeyValueTemplate(new RedisKeyValueAdapter(template)), modulesOperations, indexer, embedder,
      new RedisOMProperties());
    repository.deleteAll();

    walt = new PersonHash("Walter", "White");
    walt.setHometown(new CityHash("Albuquerqe"));

    hank = new PersonHash("Hank", "Schrader");
    hank.setHometown(new CityHash("Albuquerqe"));

    gus = new PersonHash("Gus", "Fring");
    gus.setHometown(new CityHash("Albuquerqe"));

    repository.saveAll(Arrays.asList(walt, hank, gus));
  }

  @Test
    // DATAREDIS-605
  void shouldFindOneByExample() {
    Optional<PersonHash> result = repository.findOne(Example.of(walt));

    assertThat(result).contains(walt);
  }

  @Test
    // DATAREDIS-605
  void shouldThrowExceptionWhenFindOneByExampleReturnsNonUniqueResult() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    assertThatThrownBy(() -> repository.findOne(Example.of(person))).isInstanceOf(
      IncorrectResultSizeDataAccessException.class);
  }

  @Test
    // DATAREDIS-605
  void shouldNotFindOneByExample() {
    Optional<PersonHash> result = repository.findOne(Example.of(new PersonHash("Skyler", "White")));
    assertThat(result).isEmpty();
  }

  @Test
    // DATAREDIS-605, GH-2880
  void shouldFindAllByExample() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    Iterable<PersonHash> result = repository.findAll(Example.of(person));
    assertThat(result).contains(walt, gus, hank);
  }

  @Test
    // DATAREDIS-605
  void shouldNotSupportFindAllOrdered() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    assertThatThrownBy(() -> repository.findAll(Example.of(person), Sort.by("foo"))).isInstanceOf(
      UnsupportedOperationException.class);
  }

  @Test
    // DATAREDIS-605
  void shouldFindAllPagedByExample() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    Page<PersonHash> result = repository.findAll(Example.of(person), PageRequest.of(0, 2));
    assertThat(result).hasSize(2);
  }

  @Test
    // DATAREDIS-605
  void shouldCountCorrectly() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    assertThat(repository.count(Example.of(person))).isEqualTo(3);
    assertThat(repository.count(Example.of(walt))).isEqualTo(1);
    assertThat(repository.count(Example.of(new PersonHash()))).isEqualTo(3);
    assertThat(repository.count(Example.of(new PersonHash("Foo", "Bar")))).isZero();
  }

  @Test
    // DATAREDIS-605
  void shouldReportExistenceCorrectly() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    assertThat(repository.exists(Example.of(person))).isTrue();
    assertThat(repository.exists(Example.of(walt))).isTrue();
    assertThat(repository.exists(Example.of(new PersonHash()))).isTrue();
    assertThat(repository.exists(Example.of(new PersonHash("Foo", "Bar")))).isFalse();
  }

  @Test
    // GH-2150
  void findByShouldFindFirst() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    assertThat((Object) repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::first)).isNotNull();
    assertThat(
      repository.findBy(Example.of(walt), it -> it.as(PersonProjection.class).firstValue()).getFirstname()) //
      .isEqualTo(walt.getFirstname() //
    );
  }

  @Test
    // GH-2150
  void findByShouldFindFirstAsDto() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    assertThat(repository.findBy(Example.of(walt), it -> it.as(PersonDto.class).firstValue()).getFirstname()).isEqualTo(
      walt.getFirstname());
  }

  @Test
    // GH-2150
  void findByShouldFindOne() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    assertThatExceptionOfType(IncorrectResultSizeDataAccessException.class).isThrownBy(
      () -> repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::one));
    assertThat(
      repository.findBy(Example.of(walt), it -> it.as(PersonProjection.class).oneValue()).getFirstname()).isEqualTo(
      walt.getFirstname());
  }

  @Test
    // GH-2150
  void findByShouldFindAll() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    assertThat((List<PersonHash>) repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::all)).hasSize(
        3);
    List<PersonProjection> people = repository.findBy(Example.of(walt), it -> it.as(PersonProjection.class).all());
    assertThat(people).hasSize(1);
    assertThat(people).hasOnlyElementsOfType(PersonProjection.class);
  }

  @Test
    // GH-2150
  void findByShouldFindPage() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    Page<PersonHash> result = repository.findBy(Example.of(person), it -> it.page(PageRequest.of(0, 2)));
    assertThat(result).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(3);
  }

  @Test
    // GH-2150
  void findByShouldFindStream() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    Stream<PersonHash> result = repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::stream);
    assertThat(result).hasSize(3);
  }

  @Test
    // GH-2150
  void findByShouldCount() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    assertThat((Long) repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::count)).isEqualTo(3);
  }

  @Test
    // GH-2150
  void findByShouldExists() {
    PersonHash person = new PersonHash();
    person.setHometown(walt.getHometown());

    assertThat((Boolean) repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::exists)).isTrue();
  }

  @Test
  void shouldUpdateSingleFieldByExample() {
    PersonHash updateProbe = new PersonHash();
    updateProbe.setId(walt.getId());
    updateProbe.setFirstname("Walter Jr.");

    repository.update(Example.of(updateProbe));

    PersonHash updated = repository.findById(walt.getId()).orElseThrow();

    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(walt.getId());
    assertThat(updated.getFirstname()).isEqualTo("Walter Jr.");
    assertThat(updated.getLastname()).isEqualTo(walt.getLastname());
    assertThat(updated.getHometown()).isEqualTo(walt.getHometown());
  }

  @Test
  void shouldUpdateMultipleFieldsByExample() {
    PersonHash updateProbe = new PersonHash();
    updateProbe.setId(hank.getId());
    updateProbe.setFirstname("Henry");
    updateProbe.setLastname("Schrader Jr.");

    repository.update(Example.of(updateProbe));

    PersonHash updated = repository.findById(hank.getId()).orElseThrow();

    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(hank.getId());
    assertThat(updated.getFirstname()).isEqualTo("Henry");
    assertThat(updated.getLastname()).isEqualTo("Schrader Jr.");
    assertThat(updated.getHometown()).isEqualTo(hank.getHometown());
  }

  @Test
  void shouldNotUpdateNullFieldsByExample() {
    PersonHash updateProbe = new PersonHash();
    updateProbe.setId(gus.getId());
    updateProbe.setFirstname("Gustavo");
    updateProbe.setLastname(null);

    repository.update(Example.of(updateProbe));

    PersonHash updated = repository.findById(gus.getId()).orElseThrow();

    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(gus.getId());
    assertThat(updated.getFirstname()).isEqualTo("Gustavo");
    assertThat(updated.getLastname()).isEqualTo(gus.getLastname());
    assertThat(updated.getHometown()).isEqualTo(gus.getHometown());
  }

  @Test
  void shouldUpdateNestedObjectByExample() {
    PersonHash updateProbe = new PersonHash();
    updateProbe.setId(walt.getId());
    updateProbe.setHometown(new CityHash("Albuquerque"));

    repository.update(Example.of(updateProbe));

    PersonHash updated = repository.findById(walt.getId()).orElseThrow();

    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(walt.getId());
    assertThat(updated.getFirstname()).isEqualTo(walt.getFirstname());
    assertThat(updated.getLastname()).isEqualTo(walt.getLastname());
    assertThat(updated.getHometown().getName()).isEqualTo("Albuquerqe");
  }

  @Test
  void shouldNotUpdateIgnoredFieldsByExample() {
    PersonHash updateProbe = new PersonHash();
    updateProbe.setId(walt.getId());
    updateProbe.setFirstname("Walter Jr.");
    updateProbe.setLastname("White Jr.");

    ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("lastname");

    repository.update(Example.of(updateProbe, matcher));

    PersonHash updated = repository.findById(walt.getId()).orElseThrow();

    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(walt.getId());
    assertThat(updated.getFirstname()).isEqualTo("Walter Jr.");
    assertThat(updated.getLastname()).isEqualTo(walt.getLastname());
    assertThat(updated.getHometown()).isEqualTo(walt.getHometown());
  }

  // updateAll Tests

  @Test
  void shouldUpdateMultipleEntitiesByExample() {
    PersonHash vincent = new PersonHash("Vincent", "Vega");
    vincent.setId("pf1");
    vincent.setHometown(new CityHash("Los Angeles"));

    PersonHash jules = new PersonHash("Jules", "Winnfield");
    jules.setId("pf2");
    jules.setHometown(new CityHash("Los Angeles"));

    PersonHash mia = new PersonHash("Mia", "Wallace");
    mia.setId("pf3");
    mia.setHometown(new CityHash("Los Angeles"));

    repository.saveAll(Arrays.asList(vincent, jules, mia));

    PersonHash vincentUpdate = new PersonHash();
    vincentUpdate.setId("pf1");
    vincentUpdate.setLastname("Vega-Update");

    PersonHash julesUpdate = new PersonHash();
    julesUpdate.setId("pf2");
    julesUpdate.setFirstname("Jules-Update");

    List<Example<PersonHash>> examples = Arrays.asList(Example.of(vincentUpdate), Example.of(julesUpdate));

    repository.updateAll(examples);

    PersonHash updatedVincent = repository.findById("pf1").orElseThrow();
    PersonHash updatedJules = repository.findById("pf2").orElseThrow();
    PersonHash unchangedMia = repository.findById("pf3").orElseThrow();

    assertThat(updatedVincent.getFirstname()).isEqualTo("Vincent");
    assertThat(updatedVincent.getLastname()).isEqualTo("Vega-Update");
    assertThat(updatedJules.getFirstname()).isEqualTo("Jules-Update");
    assertThat(updatedJules.getLastname()).isEqualTo("Winnfield");
    assertThat(unchangedMia.getFirstname()).isEqualTo("Mia");
    assertThat(unchangedMia.getLastname()).isEqualTo("Wallace");
  }

  @Test
  void shouldRespectExampleMatcherInUpdateAll() {
    PersonHash butch = new PersonHash("Butch", "Coolidge");
    butch.setId("pf4");
    butch.setHometown(new CityHash("Los Angeles"));

    PersonHash savedButch = repository.save(butch);
    assertThat(savedButch).isNotNull();
    assertThat(savedButch.getId()).isEqualTo("pf4");

    // Verify the entity was saved correctly
    Optional<PersonHash> retrievedButch = repository.findById("pf4");
    assertThat(retrievedButch).isPresent();
    assertThat(retrievedButch.get().getFirstname()).isEqualTo("Butch");
    assertThat(retrievedButch.get().getLastname()).isEqualTo("Coolidge");

    PersonHash butchUpdate = new PersonHash();
    butchUpdate.setId("pf4");
    butchUpdate.setFirstname("BUTCH");
    butchUpdate.setLastname("COOLIDGE");

    ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("lastname")
        .withMatcher("firstname", ExampleMatcher.GenericPropertyMatchers.ignoreCase());

    repository.updateAll(Collections.singletonList(Example.of(butchUpdate, matcher)));

    // Use findById and isPresent check instead of orElseThrow
    Optional<PersonHash> maybeUpdatedButch = repository.findById("pf4");
    assertThat(maybeUpdatedButch).isPresent();

    PersonHash updatedButch = maybeUpdatedButch.get();
    assertThat(updatedButch.getFirstname()).isEqualTo("BUTCH");
    assertThat(updatedButch.getLastname()).isEqualTo("Coolidge"); // Not updated due to ignored path
  }

  @Test
  void shouldHandleEmptyExamplesList() {
    List<Example<PersonHash>> emptyList = Collections.emptyList();
    assertThatCode(() -> repository.updateAll(emptyList)).doesNotThrowAnyException();
  }

  @Test
  void shouldThrowExceptionForExampleWithoutId() {
    PersonHash invalidUpdate = new PersonHash();
    invalidUpdate.setFirstname("Marsellus");

    List<Example<PersonHash>> examples = Collections.singletonList(Example.of(invalidUpdate));

    assertThatThrownBy(() -> repository.updateAll(examples)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Example object must have an ID");
  }

  @SuppressWarnings("unchecked")
  private <T> MappingRedisEntityInformation<T, String> getEntityInformation(Class<T> entityClass) {
    return new MappingRedisEntityInformation<>(
      (RedisPersistentEntity) mappingContext.getRequiredPersistentEntity(entityClass));
  }

  static class PersonDto {

    private String firstname;

    public String getFirstname() {
      return this.firstname;
    }

    public void setFirstname(String firstname) {
      this.firstname = firstname;
    }

    @Override
    public boolean equals(Object obj) {

      if (this == obj) {
        return true;
      }

      if (!(obj instanceof PersonHash that)) {
        return false;
      }

      return Objects.equals(this.getFirstname(), that.getLastname());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getFirstname());
    }

    @Override
    public String toString() {
      return getFirstname();
    }
  }

  interface PersonProjection {
    String getFirstname();
  }
}
