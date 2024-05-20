package com.redis.om.spring.annotations.hash;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.Company;
import com.redis.om.spring.fixtures.hash.model.MyHash;
import com.redis.om.spring.fixtures.hash.repository.CompanyRepository;
import com.redis.om.spring.fixtures.hash.repository.MyHashRepository;
import com.redis.om.spring.search.stream.EntityStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.geo.Point;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

public class RedisHashQueryByExampleTest extends AbstractBaseEnhancedRedisTest {
  @Autowired
  MyHashRepository repository;

  @Autowired
  CompanyRepository companyRepository;

  @Autowired
  EntityStream entityStream;

  String id1;
  String id2;

  @BeforeEach
  void loadTestData() {
    repository.deleteAll();
    Point point1 = new Point(-122.124500, 47.640160);
    MyHash hash1 = MyHash.of("hello world", point1, point1, 1);
    Set<String> tags = new HashSet<>();
    tags.add("news");
    tags.add("article");
    hash1.setTag(tags);

    Point point2 = new Point(-122.066540, 37.377690);
    MyHash hash2 = MyHash.of("hello mundo", point2, point2, 2);
    Set<String> tags2 = new HashSet<>();
    tags2.add("noticias");
    tags2.add("articulo");
    hash2.setTag(tags2);

    Point point3 = new Point(-122.066540, 37.377690);
    MyHash hash3 = MyHash.of("ola mundo", point3, point3, 3);
    Set<String> tags3 = new HashSet<>();
    tags3.add("noticias");
    tags3.add("artigo");
    hash3.setTag(tags3);

    Point point4 = new Point(-122.066540, 37.377690);
    MyHash hash4 = MyHash.of("bonjour le monde", point4, point4, 3);
    Set<String> tags4 = new HashSet<>();
    tags4.add("actualite");
    tags4.add("article");
    hash4.setTag(tags4);

    repository.saveAll(List.of(hash1, hash2, hash3, hash4));

    id1 = hash1.getId();
    id2 = hash2.getId();

    companyRepository.deleteAll();
    Company redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
      "stack@redis.com");
    redis.setTags(Set.of("RedisTag", "CommonTag"));

    Company microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
      "research@microsoft.com");
    microsoft.setTags(Set.of("MsTag", "CommonTag"));

    companyRepository.saveAll(List.of(redis, microsoft));
  }

  @Test
  void testFindById() {
    MyHash template = new MyHash();
    template.setId(id1);

    Example<MyHash> example = Example.of(template);

    Optional<MyHash> maybeDoc1 = repository.findOne(example);
    assertThat(maybeDoc1).isPresent();
    assertThat(maybeDoc1.get().getTitle()).isEqualTo("hello world");
  }

  @Test
  void testFindAllByExampleById() {
    MyHash template = new MyHash();
    template.setId(id1);

    Example<MyHash> example = Example.of(template);

    Iterable<MyHash> hashes = repository.findAll(example);
    assertThat(hashes).hasSize(1);
    assertThat(hashes.iterator().next()).extracting(MyHash::getTitle).isEqualTo("hello world");
  }

  @Test
  void testFindAllByExampleByIdWithExampleMatcher() {
    MyHash template = new MyHash();
    template.setId(id1);

    Example<MyHash> example = Example.of(template, ExampleMatcher.matching().withIgnoreCase(false));

    Iterable<MyHash> hashes = repository.findAll(example);
    assertThat(hashes).hasSize(1);
    assertThat(hashes.iterator().next()).extracting(MyHash::getTitle).isEqualTo("hello world");
  }

  @Test
  void testFindByTextIndexedProperty() {
    MyHash template = new MyHash();
    template.setTitle("hello world");

    Example<MyHash> example = Example.of(template);

    Optional<MyHash> maybeDoc1 = repository.findOne(example);
    assertThat(maybeDoc1).isPresent();
    assertThat(maybeDoc1.get().getTitle()).isEqualTo("hello world");
  }

  @Test
  void testFindByFieldWithExplicitTagIndexedAnnotation() {
    MyHash template = new MyHash();
    template.setTag(Set.of("news"));

    Example<MyHash> example = Example.of(template);

    Optional<MyHash> maybeDoc1 = repository.findOne(example);
    assertThat(maybeDoc1).isPresent();
    MyHash doc1 = maybeDoc1.get();
    assertThat(doc1.getTitle()).isEqualTo("hello world");
    assertThat(doc1.getTag()).contains("news");
  }

  @Test
  void testFindByFieldWithExplicitNumericIndexedAnnotation() {
    MyHash template = new MyHash();
    template.setANumber(1);

    Example<MyHash> example = Example.of(template);

    Optional<MyHash> maybeDoc1 = repository.findOne(example);
    assertThat(maybeDoc1).isPresent();
    MyHash doc1 = maybeDoc1.get();
    assertThat(doc1.getTitle()).isEqualTo("hello world");
    assertThat(doc1.getANumber()).isEqualTo(1);
  }

  @Test
  void testFindByFieldWithExplicitGeoIndexedAnnotation() {
    MyHash template = new MyHash();
    template.setLocation(new Point(-122.066540, 37.377690));

    Example<MyHash> example = Example.of(template);

    Optional<MyHash> maybeDoc1 = repository.findOne(example);
    assertThat(maybeDoc1).isPresent();
    MyHash doc1 = maybeDoc1.get();
    assertThat(doc1.getTitle()).isEqualTo("hello mundo");
    assertThat(doc1.getANumber()).isEqualTo(2);
  }

  @Test
  void testFindByMultipleFields() {
    MyHash template = new MyHash();
    template.setANumber(3);
    template.setTag(Set.of("noticias"));

    Example<MyHash> example = Example.of(template);

    Optional<MyHash> maybeDoc1 = repository.findOne(example);
    assertThat(maybeDoc1).isPresent();
    MyHash doc1 = maybeDoc1.get();
    assertThat(doc1.getTitle()).isEqualTo("ola mundo");
    assertThat(doc1.getTag()).contains("noticias");
  }

  @Test
  public void findByExampleShouldReturnEmptyListIfNotMatching() {
    MyHash template = new MyHash();
    template.setANumber(42);

    Example<MyHash> example = Example.of(template);

    Iterable<MyHash> noMatches = repository.findAll(example);
    assertThat(noMatches).isEmpty();
  }

  @Test
  public void findAllByExampleShouldReturnAllMatches() {
    MyHash template = new MyHash();
    template.setTag(Set.of("noticias"));

    Example<MyHash> example = Example.of(template);

    Iterable<MyHash> allMatches = repository.findAll(example);
    assertThat(allMatches).hasSize(2);
    assertThat(allMatches).extracting("title").contains("hello mundo", "ola mundo");
  }

  @Test
  public void findByExampleShouldReturnEverythingWhenSampleIsEmpty() {
    MyHash template = new MyHash();

    Example<MyHash> example = Example.of(template);

    Iterable<MyHash> allMatches = repository.findAll(example);
    assertThat(allMatches).hasSize(4);
  }

  @Test
  public void findsExampleUsingAnyMatch() {
    MyHash template = new MyHash();
    template.setTitle("hello world");
    template.setTag(Set.of("artigo"));

    Example<MyHash> example = Example.of(template, ExampleMatcher.matchingAny());

    Iterable<MyHash> allMatches = repository.findAll(example);
    assertThat(allMatches).hasSize(2);
    assertThat(allMatches).extracting("title").contains("hello world", "ola mundo");
  }

  @Test
  public void findsExampleUsingAnyMatch2() {
    MyHash template = new MyHash();
    template.setTitle("hello world");
    template.setANumber(3);

    Example<MyHash> example = Example.of(template, ExampleMatcher.matchingAny());

    Iterable<MyHash> allMatches = repository.findAll(example);
    assertThat(allMatches).hasSize(3);
    assertThat(allMatches).extracting("title").contains("hello world", "ola mundo", "bonjour le monde");
  }

  @Test
  void testFindByTextPropertyStartingWith() {
    MyHash template = new MyHash();
    template.setTitle("hello");

    ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(StringMatcher.STARTING);

    Example<MyHash> example = Example.of(template, matcher);

    Iterable<MyHash> allMatches = repository.findAll(example);
    assertThat(allMatches).hasSize(2);
    assertThat(allMatches).extracting("title").contains("hello world", "hello mundo");
  }

  @Test
  void testFindByTextPropertyEndingWith() {
    MyHash template = new MyHash();
    template.setTitle("ndo");

    ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(StringMatcher.ENDING);

    Example<MyHash> example = Example.of(template, matcher);

    Iterable<MyHash> allMatches = repository.findAll(example);
    assertThat(allMatches).hasSize(2);
    assertThat(allMatches).extracting("title").contains("ola mundo", "hello mundo");
  }

  @Test
  void testFindByTextPropertyContaining() {
    MyHash template = new MyHash();
    template.setTitle("llo");

    ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(StringMatcher.CONTAINING);

    Example<MyHash> example = Example.of(template, matcher);

    Iterable<MyHash> allMatches = repository.findAll(example);
    assertThat(allMatches).hasSize(2);
    assertThat(allMatches).extracting("title").contains("hello world", "hello mundo");
  }

  @Test
  public void testFindWithIgnorePaths() {
    MyHash template = new MyHash();
    template.setTitle("hello world");
    template.setANumber(3);

    Example<MyHash> example = Example.of(template, ExampleMatcher.matchingAny().withIgnorePaths("aNumber"));

    Iterable<MyHash> allMatches = repository.findAll(example);
    assertThat(allMatches).hasSize(1);
    assertThat(allMatches).extracting("title").contains("hello world");
  }

  @Test
  void testFindAllByExampleWithTags() {
    Company redisTemplate = new Company();
    redisTemplate.setTags(Set.of("RedisTag"));
    Example<Company> redisExample = Example.of(redisTemplate);

    Company msTemplate = new Company();
    msTemplate.setTags(Set.of("MsTag"));
    Example<Company> msExample = Example.of(msTemplate);

    Company bothTemplate = new Company();
    bothTemplate.setTags(Set.of("CommonTag"));
    Example<Company> bothExample = Example.of(bothTemplate);

    Iterable<Company> shouldBeOnlyRedis = companyRepository.findAll(redisExample);
    Iterable<Company> shouldBeOnlyMS = companyRepository.findAll(msExample);
    Iterable<Company> shouldBeBoth = companyRepository.findAll(bothExample);

    assertAll( //
      () -> assertThat(shouldBeOnlyRedis).map(Company::getName).containsExactly("RedisInc"), //
      () -> assertThat(shouldBeOnlyMS).map(Company::getName).containsExactly("Microsoft"), //
      () -> assertThat(shouldBeBoth).map(Company::getName).containsExactlyInAnyOrder("RedisInc", "Microsoft") //
    );
  }

  @Test
  void testFindByShouldReturnFirstResult() {
    MyHash template = new MyHash();
    template.setTitle("llo");

    ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(StringMatcher.CONTAINING);

    Example<MyHash> example = Example.of(template, matcher);

    MyHash result = repository.findBy(example, FetchableFluentQuery::firstValue);
    assertThat(result).isNotNull().hasFieldOrPropertyWithValue("title", "hello world");
  }

  @Test
  void testFindByShouldReturnOneResult() {
    MyHash template = new MyHash();
    template.setTitle("hello world");

    Example<MyHash> example = Example.of(template);

    MyHash result = repository.findBy(example, FetchableFluentQuery::oneValue);
    assertThat(result).isNotNull().hasFieldOrPropertyWithValue("title", "hello world");

    MyHash moreThanOneMatchTemplate = new MyHash();
    moreThanOneMatchTemplate.setTitle("llo");
    ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(StringMatcher.CONTAINING);

    assertThatExceptionOfType(IncorrectResultSizeDataAccessException.class).isThrownBy(
      () -> repository.findBy(Example.of(moreThanOneMatchTemplate, matcher), FluentQuery.FetchableFluentQuery::one));
  }

  @Test
  void testFindByShouldReturnAll() {
    MyHash template = new MyHash();
    template.setTitle("llo");
    ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(StringMatcher.CONTAINING);

    List<MyHash> result = repository.findBy(Example.of(template, matcher), FluentQuery.FetchableFluentQuery::all);

    assertThat(result).hasSize(2);
  }

  @Test
  void testFindByShouldApplySortAll() {
    Company probe = new Company();

    List<Company> result = companyRepository.findBy( //
      Example.of(probe), //
      it -> it.sortBy(Sort.by("name")).all() //
    );

    assertThat(result).map(Company::getName).containsExactly("Microsoft", "RedisInc");

    result = companyRepository.findBy( //
      Example.of(probe), //
      it -> it.sortBy(Sort.by(Sort.Direction.DESC, "name")).all() //
    );
    assertThat(result).map(Company::getName).containsExactly("RedisInc", "Microsoft");
  }

  @Test
  void findByShouldApplyPagination() {
    MyHash template = new MyHash();
    template.setLocation(new Point(-122.066540, 37.377690));

    Page<MyHash> firstPage = repository.findBy(Example.of(template),
      it -> it.page(PageRequest.of(0, 2, Sort.by("name"))));
    assertThat(firstPage.getTotalElements()).isEqualTo(3);
    assertThat(firstPage.getContent().size()).isEqualTo(2);
    assertThat(firstPage.getContent().stream().toList()).map(MyHash::getTitle)
      .containsExactly("hello mundo", "ola mundo");

    Page<MyHash> secondPage = repository.findBy(Example.of(template),
      it -> it.page(PageRequest.of(1, 2, Sort.by("name"))));

    assertThat(secondPage.getTotalElements()).isEqualTo(3);
    assertThat(secondPage.getContent().size()).isEqualTo(1);
    assertThat(secondPage.getContent().stream().toList()).map(MyHash::getTitle).containsExactly("bonjour le monde");
  }

  @Test
  void testFindByShouldCount() {
    MyHash template = new MyHash();
    template.setLocation(new Point(-122.066540, 37.377690));

    long count = repository.findBy(Example.of(template), FluentQuery.FetchableFluentQuery::count);
    assertThat(count).isEqualTo(3L);

    template = new MyHash();
    template.setId(id1);

    count = repository.findBy(Example.of(template), FluentQuery.FetchableFluentQuery::count);
    assertThat(count).isEqualTo(1L);
  }

  @Test
  void testFindByShouldReportExists() {

    MyHash template = new MyHash();
    template.setLocation(new Point(-122.066540, 37.377690));

    boolean exists = repository.findBy(Example.of(template), FluentQuery.FetchableFluentQuery::exists);
    assertThat(exists).isTrue();

    template = new MyHash();
    template.setId("8675309");

    exists = repository.findBy(Example.of(template), FluentQuery.FetchableFluentQuery::exists);
    assertThat(exists).isFalse();
  }

  @Test
  void testFindByShouldApplyProjection() {
    MyHash template = new MyHash();
    template.setTitle("hello world");

    Example<MyHash> example = Example.of(template);

    MyHash doc1 = repository.findBy(example, it -> it.project("aNumber").firstValue());
    assertThat(doc1.getANumber()).isNotNull();
    assertThat(doc1.getTitle()).isNull();
  }

}
