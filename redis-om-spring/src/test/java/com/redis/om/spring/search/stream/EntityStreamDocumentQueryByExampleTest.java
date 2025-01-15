package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.Company;
import com.redis.om.spring.fixtures.document.model.CompanyMeta;
import com.redis.om.spring.fixtures.document.model.MyDoc;
import com.redis.om.spring.fixtures.document.repository.CompanyRepository;
import com.redis.om.spring.fixtures.document.repository.MyDocRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.geo.Point;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class EntityStreamDocumentQueryByExampleTest extends AbstractBaseDocumentTest {
  @Autowired
  MyDocRepository repository;

  @Autowired
  EntityStream entityStream;

  @Autowired
  CompanyRepository companyRepository;

  String id1;
  String id2;

  @BeforeEach
  void loadTestData() {
    repository.deleteAll();

    Point point1 = new Point(-122.124500, 47.640160);
    MyDoc doc1 = MyDoc.of("hello world", point1, point1, 1);
    doc1.setTag(Set.of("news", "article"));

    Point point2 = new Point(-122.066540, 37.377690);
    MyDoc doc2 = MyDoc.of("hello mundo", point2, point2, 2);
    doc2.setTag(Set.of("noticias", "articulo"));

    Point point3 = new Point(-122.066540, 37.377690);
    MyDoc doc3 = MyDoc.of("ola mundo", point3, point3, 3);
    doc3.setTag(Set.of("noticias", "artigo"));

    Point point4 = new Point(-122.066540, 37.377690);
    MyDoc doc4 = MyDoc.of("bonjour le monde", point4, point4, 3);
    doc4.setTag(Set.of("actualite", "article"));

    repository.saveAll(List.of(doc1, doc2, doc3, doc4));

    id1 = doc1.getId();
    id2 = doc2.getId();

    companyRepository.deleteAll();
    Company redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");
    redis.setTags(Set.of("RedisTag", "CommonTag"));
    redis.setMetaList(Set.of(CompanyMeta.of("RD", 100, Set.of("RedisTag", "CommonTag"))));

    Company microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
        "research@microsoft.com");
    microsoft.setTags(Set.of("MsTag", "CommonTag"));
    microsoft.setMetaList(Set.of(CompanyMeta.of("MS", 50, Set.of("MsTag", "CommonTag"))));

    companyRepository.saveAll(List.of(redis, microsoft));
  }

  @Test
  void testFindOneByExampleById() {
    MyDoc template = new MyDoc();
    template.setId(id1);

    Example<MyDoc> example = Example.of(template);

    Optional<MyDoc> maybeDoc1 = entityStream.of(MyDoc.class).filter(example).findFirst();

    assertThat(maybeDoc1).isPresent();
    assertThat(maybeDoc1.get().getTitle()).isEqualTo("hello world");
  }

  @Test
  void testFindOneByExampleWithTextIndexedProperty() {
    MyDoc template = new MyDoc();
    template.setTitle("hello world");

    Example<MyDoc> example = Example.of(template);

    Optional<MyDoc> maybeDoc1 = entityStream.of(MyDoc.class).filter(example).findFirst();
    assertThat(maybeDoc1).isPresent();
    assertThat(maybeDoc1.get().getTitle()).isEqualTo("hello world");
  }

  @Test
  void testFindOneByExampleWithExplicitTagIndexedAnnotation() {
    MyDoc template = new MyDoc();
    template.setTag(Set.of("news"));

    Example<MyDoc> example = Example.of(template);

    Optional<MyDoc> maybeDoc1 = entityStream.of(MyDoc.class).filter(example).findFirst();
    assertThat(maybeDoc1).isPresent();
    MyDoc doc1 = maybeDoc1.get();
    assertThat(doc1.getTitle()).isEqualTo("hello world");
    assertThat(doc1.getTag()).contains("news");
  }

  @Test
  void testFindOneByExampleWithExplicitNumericIndexedAnnotation() {
    MyDoc template = new MyDoc();
    template.setANumber(1);

    Example<MyDoc> example = Example.of(template);

    Optional<MyDoc> maybeDoc1 = entityStream.of(MyDoc.class).filter(example).findFirst();
    assertThat(maybeDoc1).isPresent();
    MyDoc doc1 = maybeDoc1.get();
    assertThat(doc1.getTitle()).isEqualTo("hello world");
    assertThat(doc1.getANumber()).isEqualTo(1);
  }

  @Test
  void testFindOneByExampleWithFieldWithExplicitGeoIndexedAnnotation() {
    MyDoc template = new MyDoc();
    template.setLocation(new Point(-122.124500, 47.640160));

    Example<MyDoc> example = Example.of(template);

    Optional<MyDoc> maybeDoc1 = entityStream.of(MyDoc.class).filter(example).findFirst();
    assertThat(maybeDoc1).isPresent();
    MyDoc doc1 = maybeDoc1.get();
    assertThat(doc1.getTitle()).isEqualTo("hello world");
    assertThat(doc1.getANumber()).isEqualTo(1);
  }

  @Test
  void testFindOneByExampleWithMultipleFields() {
    MyDoc template = new MyDoc();
    template.setANumber(3);
    template.setTag(Set.of("noticias"));

    Example<MyDoc> example = Example.of(template);

    Optional<MyDoc> maybeDoc1 = entityStream.of(MyDoc.class).filter(example).findFirst();
    assertThat(maybeDoc1).isPresent();
    MyDoc doc1 = maybeDoc1.get();
    assertThat(doc1.getTitle()).isEqualTo("ola mundo");
    assertThat(doc1.getTag()).contains("noticias");
  }

  @Test
  void testFindAllByExampleById() {
    MyDoc template = new MyDoc();
    template.setId(id1);

    Example<MyDoc> example = Example.of(template);

    Iterable<MyDoc> docs = entityStream.of(MyDoc.class).filter(example).collect(Collectors.toList());
    assertThat(docs).hasSize(1);
    assertThat(docs.iterator().next()).extracting(MyDoc::getTitle).isEqualTo("hello world");
  }

  @Test
  void testFindAllByExampleByIdWithExampleMatcher() {
    MyDoc template = new MyDoc();
    template.setId(id1);

    Example<MyDoc> example = Example.of(template, ExampleMatcher.matching().withIgnoreCase(false));

    Iterable<MyDoc> docs = entityStream.of(MyDoc.class).filter(example).collect(Collectors.toList());
    assertThat(docs).hasSize(1);
    assertThat(docs.iterator().next()).extracting(MyDoc::getTitle).isEqualTo("hello world");
  }

  @Test
  void testFindAllByWithPageableAndSortAsc() {
    Pageable pageRequest = PageRequest.of(0, 2, Sort.by("title").ascending());
    Page<MyDoc> content = entityStream.of(MyDoc.class).getPage(pageRequest);

    assertThat(content.getSize()).isEqualTo(2);
    assertThat(content.getContent().get(0).getTitle()).isEqualTo("bonjour le monde");
  }

  @Test
  void testFindAllByWithPageableAndSortDec() {
    Pageable pageRequest = PageRequest.of(0, 2, Sort.by("title").descending());
    Page<MyDoc> content = entityStream.of(MyDoc.class).getPage(pageRequest);

    assertThat(content.getSize()).isEqualTo(2);
    assertThat(content.getContent().get(0).getTitle()).isEqualTo("ola mundo");
  }

  @Test
  public void testFindAllByExampleShouldReturnEmptyListIfNotMatching() {
    MyDoc template = new MyDoc();
    template.setANumber(42);

    Example<MyDoc> example = Example.of(template);

    Iterable<MyDoc> noMatches = entityStream.of(MyDoc.class).filter(example).collect(Collectors.toList());
    assertThat(noMatches).isEmpty();
  }

  @Test
  public void testFindAllByExampleShouldReturnAllMatches() {
    MyDoc template = new MyDoc();
    template.setTag(Set.of("noticias"));

    Example<MyDoc> example = Example.of(template);

    Iterable<MyDoc> allMatches = entityStream.of(MyDoc.class).filter(example).collect(Collectors.toList());
    assertThat(allMatches).hasSize(2);
    assertThat(allMatches).extracting("title").contains("hello mundo", "ola mundo");
  }

  @Test
  public void testFindAllByExampleShouldReturnEverythingWhenSampleIsEmpty() {
    MyDoc template = new MyDoc();

    Example<MyDoc> example = Example.of(template);

    Iterable<MyDoc> allMatches = entityStream.of(MyDoc.class).filter(example).collect(Collectors.toList());
    assertThat(allMatches).hasSize(4);
  }

  @Test
  public void testFindAllByExampleUsingAnyMatch() {
    MyDoc template = new MyDoc();
    template.setTitle("hello world");
    template.setTag(Set.of("artigo"));

    Example<MyDoc> example = Example.of(template, ExampleMatcher.matchingAny());

    Iterable<MyDoc> allMatches = entityStream.of(MyDoc.class).filter(example).collect(Collectors.toList());
    assertThat(allMatches).hasSize(2);
    assertThat(allMatches).extracting("title").contains("hello world", "ola mundo");
  }

  @Test
  public void testFindAllByExampleUsingAnyMatch2() {
    MyDoc template = new MyDoc();
    template.setTitle("hello world");
    template.setANumber(3);

    Example<MyDoc> example = Example.of(template, ExampleMatcher.matchingAny());

    Iterable<MyDoc> allMatches = entityStream.of(MyDoc.class).filter(example).collect(Collectors.toList());
    assertThat(allMatches).hasSize(3);
    assertThat(allMatches).extracting("title").contains("hello world", "ola mundo", "bonjour le monde");
  }

  @Test
  void testFindAllByExampleWithTextPropertyStartingWith() {
    MyDoc template = new MyDoc();
    template.setTitle("hello");

    ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(StringMatcher.STARTING);

    Example<MyDoc> example = Example.of(template, matcher);

    Iterable<MyDoc> allMatches = entityStream.of(MyDoc.class).filter(example).collect(Collectors.toList());
    assertThat(allMatches).hasSize(2);
    assertThat(allMatches).extracting("title").contains("hello world", "hello mundo");
  }

  @Test
  void testFindAllByExampleWithTextPropertyEndingWith() {
    MyDoc template = new MyDoc();
    template.setTitle("ndo");

    ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(StringMatcher.ENDING);

    Example<MyDoc> example = Example.of(template, matcher);

    Iterable<MyDoc> allMatches = entityStream.of(MyDoc.class).filter(example).collect(Collectors.toList());
    assertThat(allMatches).hasSize(2);
    assertThat(allMatches).extracting("title").contains("ola mundo", "hello mundo");
  }

  @Test
  void testFindAllByExampleWithTextPropertyContaining() {
    MyDoc template = new MyDoc();
    template.setTitle("llo");

    ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(StringMatcher.CONTAINING);

    Example<MyDoc> example = Example.of(template, matcher);

    Iterable<MyDoc> allMatches = entityStream.of(MyDoc.class).filter(example).collect(Collectors.toList());
    assertThat(allMatches).hasSize(2);
    assertThat(allMatches).extracting("title").contains("hello world", "hello mundo");
  }

  @Test
  public void testFindAllByExampleWithIgnorePaths() {
    MyDoc template = new MyDoc();
    template.setTitle("hello world");
    template.setANumber(3);

    Example<MyDoc> example = Example.of(template, ExampleMatcher.matchingAny().withIgnorePaths("aNumber"));

    Iterable<MyDoc> allMatches = entityStream.of(MyDoc.class).filter(example).collect(Collectors.toList());
    assertThat(allMatches).hasSize(1);
    assertThat(allMatches).extracting("title").contains("hello world");
  }

  @Test
  void testFindAllByExampleWithStringValueExampleInNestedField() {
    Company redisTemplate = new Company();
    CompanyMeta redisCm = new CompanyMeta();
    redisCm.setStringValue("RD");
    redisTemplate.setMetaList(Set.of(redisCm));

    Example<Company> redisExample = Example.of(redisTemplate);

    Company msTemplate = new Company();
    CompanyMeta msCm = new CompanyMeta();
    msCm.setStringValue("MS");
    msTemplate.setMetaList(Set.of(msCm));

    Example<Company> msExample = Example.of(msTemplate);

    Iterable<Company> shouldBeOnlyRedis = entityStream.of(Company.class).filter(redisExample).collect(Collectors.toList());
    Iterable<Company> shouldBeOnlyMS = entityStream.of(Company.class).filter(msExample).collect(Collectors.toList());

    assertAll( //
        () -> assertThat(shouldBeOnlyRedis).map(Company::getName).containsExactly("RedisInc"), //
        () -> assertThat(shouldBeOnlyMS).map(Company::getName).containsExactly("Microsoft") //
    );
  }

  @Test
  void testFindAllByExampleWithNumericValueExampleInNestedField() {
    Company redisTemplate = new Company();
    CompanyMeta redisCm = new CompanyMeta();
    redisCm.setNumberValue(100);
    redisTemplate.setMetaList(Set.of(redisCm));

    Example<Company> redisExample = Example.of(redisTemplate);

    Company msTemplate = new Company();
    CompanyMeta msCm = new CompanyMeta();
    msCm.setNumberValue(50);
    msTemplate.setMetaList(Set.of(msCm));

    Example<Company> msExample = Example.of(msTemplate);

    Iterable<Company> shouldBeOnlyRedis = entityStream.of(Company.class).filter(redisExample).collect(Collectors.toList());
    Iterable<Company> shouldBeOnlyMS = entityStream.of(Company.class).filter(msExample).collect(Collectors.toList());

    assertAll( //
        () -> assertThat(shouldBeOnlyRedis).map(Company::getName).containsExactly("RedisInc"), //
        () -> assertThat(shouldBeOnlyMS).map(Company::getName).containsExactly("Microsoft") //
    );
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

    Iterable<Company> shouldBeOnlyRedis = entityStream.of(Company.class).filter(redisExample).collect(Collectors.toList());
    Iterable<Company> shouldBeOnlyMS = entityStream.of(Company.class).filter(msExample).collect(Collectors.toList());
    Iterable<Company> shouldBeBoth = entityStream.of(Company.class).filter(bothExample).collect(Collectors.toList());

    assertAll( //
        () -> assertThat(shouldBeOnlyRedis).map(Company::getName).containsExactly("RedisInc"), //
        () -> assertThat(shouldBeOnlyMS).map(Company::getName).containsExactly("Microsoft"), //
        () -> assertThat(shouldBeBoth).map(Company::getName).containsExactlyInAnyOrder("RedisInc", "Microsoft") //
    );
  }

  @Test
  void testFindAllByExampleWithTagsInNestedField() {
    Company redisTemplate = new Company();
    CompanyMeta redisCm = new CompanyMeta();
    redisCm.setTagValues(Set.of("RedisTag"));
    redisTemplate.setMetaList(Set.of(redisCm));
    Example<Company> redisExample = Example.of(redisTemplate);

    Company msTemplate = new Company();
    CompanyMeta msCm = new CompanyMeta();
    msCm.setTagValues(Set.of("MsTag"));
    msTemplate.setMetaList(Set.of(msCm));
    Example<Company> msExample = Example.of(msTemplate);

    Company bothTemplate = new Company();
    CompanyMeta bothCm = new CompanyMeta();
    bothCm.setTagValues(Set.of("CommonTag"));
    bothTemplate.setMetaList(Set.of(bothCm));
    Example<Company> bothExample = Example.of(bothTemplate);

    Iterable<Company> shouldBeOnlyRedis = entityStream.of(Company.class).filter(redisExample).collect(Collectors.toList());
    Iterable<Company> shouldBeOnlyMS = entityStream.of(Company.class).filter(msExample).collect(Collectors.toList());
    Iterable<Company> shouldBeBoth = entityStream.of(Company.class).filter(bothExample).collect(Collectors.toList());

    assertAll( //
        () -> assertThat(shouldBeOnlyRedis).map(Company::getName).containsExactly("RedisInc"), //
        () -> assertThat(shouldBeOnlyMS).map(Company::getName).containsExactly("Microsoft"), //
        () -> assertThat(shouldBeBoth).map(Company::getName).containsExactlyInAnyOrder("RedisInc", "Microsoft") //
    );
  }

  @Test
  void testFindByShouldReturnFirstResult() {
    MyDoc template = new MyDoc();
    template.setTitle("llo");

    ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(StringMatcher.CONTAINING);

    Example<MyDoc> example = Example.of(template, matcher);

    Optional<MyDoc> result = entityStream.of(MyDoc.class).filter(example).findFirst();
    assertThat(result).isPresent().get().hasFieldOrPropertyWithValue("title", "hello world");
  }

  @Test
  void testFindByShouldReturnAll() {
    MyDoc template = new MyDoc();
    template.setTitle("llo");
    ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(StringMatcher.CONTAINING);

    List<MyDoc> result =  entityStream.of(MyDoc.class).filter(Example.of(template, matcher)).collect(Collectors.toList());

    assertThat(result).hasSize(2);
  }

  @Test
  void findByShouldApplyPagination() {
    MyDoc template = new MyDoc();
    template.setLocation(new Point(-122.066540, 37.377690));

    Page<MyDoc> firstPage = entityStream.of(MyDoc.class).filter(Example.of(template)).getPage(PageRequest.of(0, 2, Sort.by("title")));
    assertThat(firstPage.getTotalElements()).isEqualTo(3);
    assertThat(firstPage.getContent().size()).isEqualTo(2);
    assertThat(firstPage.getContent().stream().toList()).map(MyDoc::getTitle)
        .containsExactly("bonjour le monde", "hello mundo");

    Page<MyDoc> secondPage = entityStream.of(MyDoc.class).filter(Example.of(template)).getPage(PageRequest.of(1, 2, Sort.by("title")));

    assertThat(secondPage.getTotalElements()).isEqualTo(3);
    assertThat(secondPage.getContent().size()).isEqualTo(1);
    assertThat(secondPage.getContent().stream().toList()).map(MyDoc::getTitle).containsExactly("ola mundo");
  }

  @Test
  void testTagEscapeCharsFindByShouldReturnOneResult() {
    Company template = new Company();
    template.setEmail("stack@redis.com");

    Example<Company> example = Example.of(template);

    Optional<Company> result = entityStream.of(Company.class).filter(example).findFirst();
    assertThat(result).isPresent().get().hasFieldOrPropertyWithValue("email", "stack@redis.com");
    assertThat(result.get().getName()).isEqualTo("RedisInc");
  }
}
