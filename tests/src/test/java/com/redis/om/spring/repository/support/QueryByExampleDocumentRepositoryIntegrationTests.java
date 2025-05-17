package com.redis.om.spring.repository.support;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.redis.repository.core.MappingRedisEntityInformation;
import org.springframework.data.redis.repository.support.QueryByExampleRedisExecutor;
import org.springframework.data.repository.query.FluentQuery;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.CityDoc;
import com.redis.om.spring.fixtures.document.model.PersonDoc;
import com.redis.om.spring.fixtures.document.repository.PersonDocRepository;

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
class QueryByExampleDocumentRepositoryIntegrationTests extends AbstractBaseDocumentTest {

  private final RedisMappingContext mappingContext = new RedisMappingContext();

  private PersonDoc walt, hank, gus;

  @Autowired
  private PersonDocRepository repository;

  @BeforeEach
  void before() {
    repository.deleteAll();

    walt = new PersonDoc("Walter", "White");
    walt.setHometown(new CityDoc("Albuquerqe"));

    hank = new PersonDoc("Hank", "Schrader");
    hank.setHometown(new CityDoc("Albuquerqe"));

    gus = new PersonDoc("Gus", "Fring");
    gus.setHometown(new CityDoc("Albuquerqe"));

    repository.saveAll(Arrays.asList(walt, hank, gus));
  }

  @Test
  // DATAREDIS-605
  void shouldFindOneByExample() {
    Optional<PersonDoc> result = repository.findOne(Example.of(walt));

    assertThat(result).contains(walt);
  }

  @Test
  // DATAREDIS-605
  void shouldThrowExceptionWhenFindOneByExampleReturnsNonUniqueResult() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    assertThatThrownBy(() -> repository.findOne(Example.of(person))).isInstanceOf(
        IncorrectResultSizeDataAccessException.class);
  }

  @Test
  // DATAREDIS-605
  void shouldNotFindOneByExample() {
    Optional<PersonDoc> result = repository.findOne(Example.of(new PersonDoc("Skyler", "White")));
    assertThat(result).isEmpty();
  }

  @Test
  // DATAREDIS-605, GH-2880
  void shouldFindAllByExample() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    Iterable<PersonDoc> result = repository.findAll(Example.of(person));
    assertThat(result).contains(walt, gus, hank);
  }

  @Test
  // DATAREDIS-605
  void shouldNotSupportFindAllOrdered() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    assertThatThrownBy(() -> repository.findAll(Example.of(person), Sort.by("foo"))).isInstanceOf(
        UnsupportedOperationException.class);
  }

  @Test
  // DATAREDIS-605
  void shouldFindAllPagedByExample() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    Pageable firstPage = PageRequest.of(0, 2);
    Page<PersonDoc> result = repository.findAll(Example.of(person), firstPage);
    assertThat(result).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(3);
    assertThat(result.getTotalPages()).isEqualTo(2);
    assertThat(result.hasNext()).isTrue();

    Pageable nextPage = result.nextPageable();
    Page<PersonDoc> next = repository.findAll(Example.of(person), nextPage);
    assertThat(next).hasSize(1);
    assertThat(next.getTotalElements()).isEqualTo(3);
    assertThat(next.getTotalPages()).isEqualTo(2);
    assertThat(next.hasNext()).isFalse();
  }

  @Test
  // DATAREDIS-605
  void shouldCountCorrectly() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    assertThat(repository.count(Example.of(person))).isEqualTo(3);
    assertThat(repository.count(Example.of(walt))).isEqualTo(1);
    assertThat(repository.count(Example.of(new PersonDoc()))).isEqualTo(3);
    assertThat(repository.count(Example.of(new PersonDoc("Foo", "Bar")))).isZero();
  }

  @Test
  // DATAREDIS-605
  void shouldReportExistenceCorrectly() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    assertThat(repository.exists(Example.of(person))).isTrue();
    assertThat(repository.exists(Example.of(walt))).isTrue();
    assertThat(repository.exists(Example.of(new PersonDoc()))).isTrue();
    assertThat(repository.exists(Example.of(new PersonDoc("Foo", "Bar")))).isFalse();
  }

  @Test
  // GH-2150
  void findByShouldFindFirst() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    assertThat((Object) repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::first)).isNotNull();
    assertThat(repository.findBy(Example.of(walt), it -> it.as(PersonProjection.class).firstValue()).getFirstname()) //
        .isEqualTo(walt.getFirstname() //
        );
  }

  @Test
  // GH-2150
  void findByShouldFindFirstAsDto() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    assertThat(repository.findBy(Example.of(walt), it -> it.as(PersonDto.class).firstValue()).getFirstname()).isEqualTo(
        walt.getFirstname());
  }

  @Test
  // GH-2150
  void findByShouldFindOne() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    assertThatExceptionOfType(IncorrectResultSizeDataAccessException.class).isThrownBy(() -> repository.findBy(Example
        .of(person), FluentQuery.FetchableFluentQuery::one));
    assertThat(repository.findBy(Example.of(walt), it -> it.as(PersonProjection.class).oneValue()).getFirstname())
        .isEqualTo(walt.getFirstname());
  }

  @Test
  // GH-2150
  void findByShouldFindAll() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    assertThat((List<PersonDoc>) repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::all)).hasSize(
        3);
    List<PersonProjection> people = repository.findBy(Example.of(walt), it -> it.as(PersonProjection.class).all());
    assertThat(people).hasSize(1);
    assertThat(people).hasOnlyElementsOfType(PersonProjection.class);
  }

  @Test
  // GH-2150
  void findByShouldFindPage() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    Page<PersonDoc> result = repository.findBy(Example.of(person), it -> it.page(PageRequest.of(0, 2)));
    assertThat(result).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(3);
  }

  @Test
  // GH-2150
  void findByShouldFindStream() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    Stream<PersonDoc> result = repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::stream);
    assertThat(result).hasSize(3);
  }

  @Test
  // GH-2150
  void findByShouldCount() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    assertThat((Long) repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::count)).isEqualTo(3);
  }

  @Test
  // GH-2150
  void findByShouldExists() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    assertThat((Boolean) repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::exists)).isTrue();
  }

  @Test
  void shouldUpdateSingleFieldByExample() {
    PersonDoc updateProbe = new PersonDoc();
    updateProbe.setId(walt.getId());
    updateProbe.setFirstname("Walter Jr.");

    repository.update(Example.of(updateProbe));

    PersonDoc updated = repository.findById(walt.getId()).orElseThrow();

    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(walt.getId());
    assertThat(updated.getFirstname()).isEqualTo("Walter Jr.");
    assertThat(updated.getLastname()).isEqualTo(walt.getLastname());
    assertThat(updated.getHometown()).isEqualTo(walt.getHometown());
  }

  @Test
  void shouldUpdateMultipleFieldsByExample() {
    PersonDoc updateProbe = new PersonDoc();
    updateProbe.setId(hank.getId());
    updateProbe.setFirstname("Henry");
    updateProbe.setLastname("Schrader Jr.");

    repository.update(Example.of(updateProbe));

    PersonDoc updated = repository.findById(hank.getId()).orElseThrow();

    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(hank.getId());
    assertThat(updated.getFirstname()).isEqualTo("Henry");
    assertThat(updated.getLastname()).isEqualTo("Schrader Jr.");
    assertThat(updated.getHometown()).isEqualTo(hank.getHometown());
  }

  @Test
  void shouldNotUpdateNullFieldsByExample() {
    PersonDoc updateProbe = new PersonDoc();
    updateProbe.setId(gus.getId());
    updateProbe.setFirstname("Gustavo");
    updateProbe.setLastname(null);

    repository.update(Example.of(updateProbe));

    PersonDoc updated = repository.findById(gus.getId()).orElseThrow();

    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(gus.getId());
    assertThat(updated.getFirstname()).isEqualTo("Gustavo");
    assertThat(updated.getLastname()).isEqualTo(gus.getLastname());
    assertThat(updated.getHometown()).isEqualTo(gus.getHometown());
  }

  @Test
  void shouldUpdateNestedObjectByExample() {
    PersonDoc updateProbe = new PersonDoc();
    updateProbe.setId(walt.getId());
    updateProbe.setHometown(new CityDoc("Albuquerque"));

    repository.update(Example.of(updateProbe));

    PersonDoc updated = repository.findById(walt.getId()).orElseThrow();

    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(walt.getId());
    assertThat(updated.getFirstname()).isEqualTo(walt.getFirstname());
    assertThat(updated.getLastname()).isEqualTo(walt.getLastname());
    assertThat(updated.getHometown().getName()).isEqualTo("Albuquerqe");
  }

  @Test
  void shouldNotUpdateIgnoredFieldsByExample() {
    PersonDoc updateProbe = new PersonDoc();
    updateProbe.setId(walt.getId());
    updateProbe.setFirstname("Walter Jr.");
    updateProbe.setLastname("White Jr.");

    ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("lastname");

    repository.update(Example.of(updateProbe, matcher));

    PersonDoc updated = repository.findById(walt.getId()).orElseThrow();

    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(walt.getId());
    assertThat(updated.getFirstname()).isEqualTo("Walter Jr.");
    assertThat(updated.getLastname()).isEqualTo(walt.getLastname());
    assertThat(updated.getHometown()).isEqualTo(walt.getHometown());
  }

  // updateAll Tests

  @Test
  void shouldUpdateMultipleEntitiesByExample() {
    PersonDoc vincent = new PersonDoc("Vincent", "Vega");
    vincent.setId("pf1");
    vincent.setHometown(new CityDoc("Los Angeles"));

    PersonDoc jules = new PersonDoc("Jules", "Winnfield");
    jules.setId("pf2");
    jules.setHometown(new CityDoc("Los Angeles"));

    PersonDoc mia = new PersonDoc("Mia", "Wallace");
    mia.setId("pf3");
    mia.setHometown(new CityDoc("Los Angeles"));

    repository.saveAll(Arrays.asList(vincent, jules, mia));

    PersonDoc vincentUpdate = new PersonDoc();
    vincentUpdate.setId("pf1");
    vincentUpdate.setLastname("Vega-Update");

    PersonDoc julesUpdate = new PersonDoc();
    julesUpdate.setId("pf2");
    julesUpdate.setFirstname("Jules-Update");

    List<Example<PersonDoc>> examples = Arrays.asList(Example.of(vincentUpdate), Example.of(julesUpdate));

    repository.updateAll(examples);

    PersonDoc updatedVincent = repository.findById("pf1").orElseThrow();
    PersonDoc updatedJules = repository.findById("pf2").orElseThrow();
    PersonDoc unchangedMia = repository.findById("pf3").orElseThrow();

    assertThat(updatedVincent.getFirstname()).isEqualTo("Vincent");
    assertThat(updatedVincent.getLastname()).isEqualTo("Vega-Update");
    assertThat(updatedJules.getFirstname()).isEqualTo("Jules-Update");
    assertThat(updatedJules.getLastname()).isEqualTo("Winnfield");
    assertThat(unchangedMia.getFirstname()).isEqualTo("Mia");
    assertThat(unchangedMia.getLastname()).isEqualTo("Wallace");
  }

  @Test
  void shouldRespectExampleMatcherInUpdateAll() {
    PersonDoc butch = new PersonDoc("Butch", "Coolidge");
    butch.setId("pf4");
    butch.setHometown(new CityDoc("Los Angeles"));

    PersonDoc savedButch = repository.save(butch);
    assertThat(savedButch).isNotNull();
    assertThat(savedButch.getId()).isEqualTo("pf4");

    // Verify the entity was saved correctly
    Optional<PersonDoc> retrievedButch = repository.findById("pf4");
    assertThat(retrievedButch).isPresent();
    assertThat(retrievedButch.get().getFirstname()).isEqualTo("Butch");
    assertThat(retrievedButch.get().getLastname()).isEqualTo("Coolidge");

    PersonDoc butchUpdate = new PersonDoc();
    butchUpdate.setId("pf4");
    butchUpdate.setFirstname("BUTCH");
    butchUpdate.setLastname("COOLIDGE");

    ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("lastname").withMatcher("firstname",
        ExampleMatcher.GenericPropertyMatchers.ignoreCase());

    repository.updateAll(Collections.singletonList(Example.of(butchUpdate, matcher)));

    // Use findById and isPresent check instead of orElseThrow
    Optional<PersonDoc> maybeUpdatedButch = repository.findById("pf4");
    assertThat(maybeUpdatedButch).isPresent();

    PersonDoc updatedButch = maybeUpdatedButch.get();
    assertThat(updatedButch.getFirstname()).isEqualTo("BUTCH");
    assertThat(updatedButch.getLastname()).isEqualTo("Coolidge"); // Not updated due to ignored path
  }

  @Test
  void shouldHandleEmptyExamplesList() {
    List<Example<PersonDoc>> emptyList = Collections.emptyList();
    assertThatCode(() -> repository.updateAll(emptyList)).doesNotThrowAnyException();
  }

  @Test
  void shouldThrowExceptionForExampleWithoutId() {
    PersonDoc invalidUpdate = new PersonDoc();
    invalidUpdate.setFirstname("Marsellus");

    List<Example<PersonDoc>> examples = Collections.singletonList(Example.of(invalidUpdate));

    assertThatThrownBy(() -> repository.updateAll(examples)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Example object must have an ID");
  }

  @SuppressWarnings(
    "unchecked"
  )
  private <T> MappingRedisEntityInformation<T, String> getEntityInformation(Class<T> entityClass) {
    return new MappingRedisEntityInformation<>((RedisPersistentEntity) mappingContext.getRequiredPersistentEntity(
        entityClass));
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

      if (!(obj instanceof PersonDoc that)) {
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
