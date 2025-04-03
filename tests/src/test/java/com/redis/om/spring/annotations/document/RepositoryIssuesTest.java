package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.*;
import com.redis.om.spring.fixtures.document.repository.Doc5Repository;
import com.redis.om.spring.fixtures.document.repository.SKUCacheRepository;
import com.redis.om.spring.fixtures.document.repository.StudentRepository;
import com.redis.om.spring.fixtures.document.repository.User2Repository;
import com.redis.om.spring.search.stream.EntityStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("SpellCheckingInspection")
class RepositoryIssuesTest extends AbstractBaseDocumentTest {
  @Autowired
  User2Repository repository;

  @Autowired
  SKUCacheRepository skuCacheRepository;

  @Autowired
  StudentRepository studentRepository;

  @Autowired
  private Doc5Repository doc5Repository;

  @Autowired
  private EntityStream entityStream;

  @BeforeEach
  void cleanUp() {
    repository.deleteAll();
    repository.save(User2.of("Doe", "Paris", "12 rue Rivoli"));

    skuCacheRepository.deleteAll();
    List<SKU> skuCaches = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      skuCaches.add(new SKU((long) i, "A" + i + i + i + i + i, "SKU " + i));
    }
    skuCacheRepository.saveAll(skuCaches);

    studentRepository.deleteAll();
    List<Student> students = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      var student = Student.of("Student" + i, i != 2 ? LocalDateTime.now() : LocalDateTime.of(2023, 6, 1, 1, 1, 1));
      student.setId((long) i);
      students.add(student);
    }
    studentRepository.saveAll(students);

    doc5Repository.deleteAll();

    // Create test data
    Doc5 model1 = new Doc5();
    model1.setAge(30);
    model1.setRegistrationDate(LocalDateTime.now().minusDays(5));
    model1.setIsActive(true);

    Doc5 model2 = new Doc5();
    model2.setAge(40);
    model2.setRegistrationDate(LocalDateTime.now().minusDays(10));
    model2.setIsActive(false);

    doc5Repository.saveAll(List.of(model1, model2));
  }

  // RediSearchQuery wrong preparedQuery #187
  @Test
  void testIncorrectParameterInjection() {
    Iterable<User2> results = repository.findUser("Doe", "Paris", "12 rue Rivoli");

    assertThat(results).extracting("name").containsExactly("Doe");
  }

  @Test
  void testFindAllByText() {
    List<SKU> result = skuCacheRepository.findAllBySkuNameIn(Set.of("SKU 1", "SKU 2"));

    assertAll( //
        () -> assertThat(result).hasSize(2),
        () -> assertThat(result).extracting("skuName").containsExactlyInAnyOrder("SKU 1", "SKU 2") //
    );
  }

  @Test
  void testFindAllByTag() {
    List<SKU> result = skuCacheRepository.findAllBySkuNumberIn(Set.of("A11111", "A00000"));

    assertAll( //
        () -> assertThat(result).hasSize(2),
        () -> assertThat(result).extracting("skuNumber").containsExactlyInAnyOrder("A11111", "A00000") //
    );
  }

  @Test
  void testFindOneBy() {
    SKU result = skuCacheRepository.findOneBySkuNumber("A11111").orElseThrow();

    assertAll( //
        () -> assertThat(result).isNotNull(), () -> assertThat(result.getSkuNumber()).isEqualTo("A11111") //
    );
  }

  @Test
  void testFindByPropertyWithAliasWithHyphens() {
    List<Student> result = studentRepository.findByUserName("Student2");
    // "FT.SEARCH" "com.redis.om.spring.annotations.document.fixtures.StudentIdx" "@User\\-Name:{Student2}" "LIMIT" "0" "10000"

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

  @Test
  void testFindByAge() {
    // This works fine
    List<Doc5> resultsByAge = doc5Repository.findByAge(30);

    assertThat(resultsByAge).isNotEmpty();
    assertThat(resultsByAge).hasSize(1);
    assertThat(resultsByAge.get(0).getAge()).isEqualTo(30);
  }

  @Test
  void testFindByDateBetween() {
    // LocalDateTime query test
    LocalDateTime from = LocalDateTime.now().minusDays(20);
    LocalDateTime to = LocalDateTime.now();

    // First debug using EntityStream
    List<Doc5> resultsWithEntityStream = entityStream.of(Doc5.class)
        .filter(Doc5$.REGISTRATION_DATE.between(from, to))
        .collect(Collectors.toList());

    // Now try repository method
    List<Doc5> resultsByDate = doc5Repository.findByRegistrationDateBetween(from, to);

    assertThat(resultsByDate).isNotEmpty();
    assertThat(resultsByDate).hasSize(2);
  }

  @Test
  void testFindByIsActive() {
    // First debug using EntityStream
    List<Doc5> resultsWithEntityStream = entityStream.of(Doc5.class)
        .filter(Doc5$.IS_ACTIVE.eq(true))
        .collect(Collectors.toList());

    // Now try repository method
    List<Doc5> resultsByActive = doc5Repository.findByIsActive(true);

    assertThat(resultsByActive).isNotEmpty();
    assertThat(resultsByActive).hasSize(1);
    assertThat(resultsByActive.get(0).getIsActive()).isTrue();
  }
}
