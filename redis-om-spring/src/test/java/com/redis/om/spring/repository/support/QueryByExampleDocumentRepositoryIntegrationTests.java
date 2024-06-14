package com.redis.om.spring.repository.support;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.redis.repository.core.MappingRedisEntityInformation;
import org.springframework.data.redis.repository.support.QueryByExampleRedisExecutor;
import org.springframework.data.repository.query.FluentQuery;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
class QueryByExampleDocumentRepositoryIntegrationTests extends AbstractBaseDocumentTest {

  private final RedisMappingContext mappingContext = new RedisMappingContext();

  private PersonDoc walt, hank, gus;

  private SimpleRedisDocumentRepository<PersonDoc, String> repository;

  @BeforeEach
  void before() {
    repository = new SimpleRedisDocumentRepository<>(
        getEntityInformation(PersonDoc.class), //
      new KeyValueTemplate(new RedisKeyValueAdapter(template)), //
        modulesOperations, //
        indexer, //
        mappingContext, //
        gsonBuilder, //
        embedder,
      new RedisOMProperties());
    repository.deleteAll();

    walt = new PersonDoc("Walter", "White");
    walt.setHometown(new City("Albuquerqe"));

    hank = new PersonDoc("Hank", "Schrader");
    hank.setHometown(new City("Albuquerqe"));

    gus = new PersonDoc("Gus", "Fring");
    gus.setHometown(new City("Albuquerqe"));

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

    Page<PersonDoc> result = repository.findAll(Example.of(person), PageRequest.of(0, 2));
    assertThat(result).hasSize(2);
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
    assertThat(
      repository.findBy(Example.of(walt), it -> it.as(PersonProjection.class).firstValue()).getFirstname()) //
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

    assertThatExceptionOfType(IncorrectResultSizeDataAccessException.class).isThrownBy(
      () -> repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::one));
    assertThat(
      repository.findBy(Example.of(walt), it -> it.as(PersonProjection.class).oneValue()).getFirstname()).isEqualTo(
      walt.getFirstname());
  }

  @Test
    // GH-2150
  void findByShouldFindAll() {
    PersonDoc person = new PersonDoc();
    person.setHometown(walt.getHometown());

    assertThat((List<PersonDoc>) repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::all)).hasSize(3);
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

  @SuppressWarnings("unchecked")
  private <T> MappingRedisEntityInformation<T, String> getEntityInformation(Class<T> entityClass) {
    return new MappingRedisEntityInformation<>(
      (RedisPersistentEntity) mappingContext.getRequiredPersistentEntity(entityClass));
  }

  @Document("dpersons")
  static class PersonDoc {

    private @Id String id;
    private @Indexed String firstname;
    private String lastname;
    private @Indexed City hometown;

    PersonDoc() {
    }

    PersonDoc(String firstname, String lastname) {
      this.firstname = firstname;
      this.lastname = lastname;
    }

    public String getId() {
      return this.id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getFirstname() {
      return this.firstname;
    }

    public void setFirstname(String firstname) {
      this.firstname = firstname;
    }

    public String getLastname() {
      return this.lastname;
    }

    public void setLastname(String lastname) {
      this.lastname = lastname;
    }

    public City getHometown() {
      return this.hometown;
    }

    public void setHometown(City hometown) {
      this.hometown = hometown;
    }

    @Override
    public boolean equals(Object obj) {

      if (this == obj) {
        return true;
      }

      if (!(obj instanceof PersonDoc that)) {
        return false;
      }

      return Objects.equals(this.getId(), that.getId()) && Objects.equals(this.getFirstname(),
        that.getFirstname()) && Objects.equals(this.getLastname(), that.getLastname()) && Objects.equals(
        this.getHometown(), that.getHometown());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getId(), getFirstname(), getLastname(), getHometown());
    }
  }

  static class City {

    private @Indexed String name;

    public City() {
    }

    public City(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @Override
    public boolean equals(Object obj) {

      if (this == obj) {
        return true;
      }

      if (!(obj instanceof City that)) {
        return false;
      }

      return Objects.equals(this.getName(), that.getName());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getName());
    }
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
