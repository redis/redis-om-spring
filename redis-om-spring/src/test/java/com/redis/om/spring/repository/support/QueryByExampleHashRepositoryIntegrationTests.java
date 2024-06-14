package com.redis.om.spring.repository.support;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.RedisOMProperties;
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
import org.springframework.data.redis.core.RedisHash;
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
class QueryByExampleHashRepositoryIntegrationTests extends AbstractBaseEnhancedRedisTest {

  private final RedisMappingContext mappingContext = new RedisMappingContext();

  private Person walt, hank, gus;

  private SimpleRedisEnhancedRepository<Person, String> repository;

  @BeforeEach
  void before() {

    repository = new SimpleRedisEnhancedRepository<>(getEntityInformation(Person.class),
        new KeyValueTemplate(new RedisKeyValueAdapter(template)), modulesOperations, indexer, embedder,
      new RedisOMProperties());
    repository.deleteAll();

    walt = new Person("Walter", "White");
    walt.setHometown(new City("Albuquerqe"));

    hank = new Person("Hank", "Schrader");
    hank.setHometown(new City("Albuquerqe"));

    gus = new Person("Gus", "Fring");
    gus.setHometown(new City("Albuquerqe"));

    repository.saveAll(Arrays.asList(walt, hank, gus));
  }

  @Test
    // DATAREDIS-605
  void shouldFindOneByExample() {
    Optional<Person> result = repository.findOne(Example.of(walt));

    assertThat(result).contains(walt);
  }

  @Test
    // DATAREDIS-605
  void shouldThrowExceptionWhenFindOneByExampleReturnsNonUniqueResult() {
    Person person = new Person();
    person.setHometown(walt.getHometown());

    assertThatThrownBy(() -> repository.findOne(Example.of(person))).isInstanceOf(
      IncorrectResultSizeDataAccessException.class);
  }

  @Test
    // DATAREDIS-605
  void shouldNotFindOneByExample() {
    Optional<Person> result = repository.findOne(Example.of(new Person("Skyler", "White")));
    assertThat(result).isEmpty();
  }

  @Test
    // DATAREDIS-605, GH-2880
  void shouldFindAllByExample() {
    Person person = new Person();
    person.setHometown(walt.getHometown());

    Iterable<Person> result = repository.findAll(Example.of(person));
    assertThat(result).contains(walt, gus, hank);
  }

  @Test
    // DATAREDIS-605
  void shouldNotSupportFindAllOrdered() {
    Person person = new Person();
    person.setHometown(walt.getHometown());

    assertThatThrownBy(() -> repository.findAll(Example.of(person), Sort.by("foo"))).isInstanceOf(
      UnsupportedOperationException.class);
  }

  @Test
    // DATAREDIS-605
  void shouldFindAllPagedByExample() {
    Person person = new Person();
    person.setHometown(walt.getHometown());

    Page<Person> result = repository.findAll(Example.of(person), PageRequest.of(0, 2));
    assertThat(result).hasSize(2);
  }

  @Test
    // DATAREDIS-605
  void shouldCountCorrectly() {
    Person person = new Person();
    person.setHometown(walt.getHometown());

    assertThat(repository.count(Example.of(person))).isEqualTo(3);
    assertThat(repository.count(Example.of(walt))).isEqualTo(1);
    assertThat(repository.count(Example.of(new Person()))).isEqualTo(3);
    assertThat(repository.count(Example.of(new Person("Foo", "Bar")))).isZero();
  }

  @Test
    // DATAREDIS-605
  void shouldReportExistenceCorrectly() {
    Person person = new Person();
    person.setHometown(walt.getHometown());

    assertThat(repository.exists(Example.of(person))).isTrue();
    assertThat(repository.exists(Example.of(walt))).isTrue();
    assertThat(repository.exists(Example.of(new Person()))).isTrue();
    assertThat(repository.exists(Example.of(new Person("Foo", "Bar")))).isFalse();
  }

  @Test
    // GH-2150
  void findByShouldFindFirst() {
    Person person = new Person();
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
    Person person = new Person();
    person.setHometown(walt.getHometown());

    assertThat(repository.findBy(Example.of(walt), it -> it.as(PersonDto.class).firstValue()).getFirstname()).isEqualTo(
      walt.getFirstname());
  }

  @Test
    // GH-2150
  void findByShouldFindOne() {
    Person person = new Person();
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
    Person person = new Person();
    person.setHometown(walt.getHometown());

    assertThat((List<Person>) repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::all)).hasSize(3);
    List<PersonProjection> people = repository.findBy(Example.of(walt), it -> it.as(PersonProjection.class).all());
    assertThat(people).hasSize(1);
    assertThat(people).hasOnlyElementsOfType(PersonProjection.class);
  }

  @Test
    // GH-2150
  void findByShouldFindPage() {
    Person person = new Person();
    person.setHometown(walt.getHometown());

    Page<Person> result = repository.findBy(Example.of(person), it -> it.page(PageRequest.of(0, 2)));
    assertThat(result).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(3);
  }

  @Test
    // GH-2150
  void findByShouldFindStream() {
    Person person = new Person();
    person.setHometown(walt.getHometown());

    Stream<Person> result = repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::stream);
    assertThat(result).hasSize(3);
  }

  @Test
    // GH-2150
  void findByShouldCount() {
    Person person = new Person();
    person.setHometown(walt.getHometown());

    assertThat((Long) repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::count)).isEqualTo(3);
  }

  @Test
    // GH-2150
  void findByShouldExists() {
    Person person = new Person();
    person.setHometown(walt.getHometown());

    assertThat((Boolean) repository.findBy(Example.of(person), FluentQuery.FetchableFluentQuery::exists)).isTrue();
  }

  @SuppressWarnings("unchecked")
  private <T> MappingRedisEntityInformation<T, String> getEntityInformation(Class<T> entityClass) {
    return new MappingRedisEntityInformation<>(
      (RedisPersistentEntity) mappingContext.getRequiredPersistentEntity(entityClass));
  }

  @RedisHash("persons")
  static class Person {

    private @Id String id;
    private @Indexed String firstname;
    private String lastname;
    private @Indexed City hometown;

    Person() {
    }

    Person(String firstname, String lastname) {
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

      if (!(obj instanceof Person that)) {
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

      if (!(obj instanceof Person that)) {
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
