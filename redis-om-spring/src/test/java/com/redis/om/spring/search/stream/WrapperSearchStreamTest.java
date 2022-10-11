package com.redis.om.spring.search.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Company;
import com.redis.om.spring.annotations.document.fixtures.Company$;
import com.redis.om.spring.annotations.document.fixtures.CompanyRepository;
import com.redis.om.spring.annotations.document.fixtures.Employee;
import com.redis.om.spring.annotations.document.fixtures.User;
import com.redis.om.spring.annotations.document.fixtures.User$;
import com.redis.om.spring.annotations.document.fixtures.UserRepository;

class WrapperSearchStreamTest extends AbstractBaseDocumentTest {
  @Autowired
  CompanyRepository repository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  EntityStream entityStream;

  @BeforeEach
  void cleanUp() {
    // companies
    repository.deleteAll();

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

    // users
    userRepository.deleteAll();
    List<User> users = List.of(User.of("Steve Lorello", .9999), User.of("Nava Levy", 1234.5678),
        User.of("Savannah Norem", 999.99), User.of("Suze Shardlow", 899.0));
    for (User user : users) {
      user.setRoles(List.of("devrel", "educator", "guru"));
    }
    userRepository.saveAll(users);
  }

  @Test
  void testPassThroughsToWrapperSearchStream() {
    SearchStream<String> stream = entityStream.of(Company.class).map(Company$.NAME).sequential();
    assertThat(stream.isParallel()).isFalse();
    assertThat(stream.parallel().isParallel()).isTrue();

    SearchStream<String> stream2 = entityStream.of(Company.class).map(Company$.NAME).sequential().sequential();
    assertThat(stream2.isParallel()).isFalse();
    assertThat(stream2.parallel().isParallel()).isTrue();

    Iterator<String> iter = entityStream.of(Company.class).map(Company$.NAME).iterator();
    SearchStream<String> unordered = entityStream.of(Company.class).map(Company$.NAME).parallel().unordered();
    assertThat(Iterators.elementsEqual(iter, unordered.iterator())).isTrue();
  }

  @Test
  void testCloseHandlerOnWrapperSearchStream() {
    SearchStream<String> stream = entityStream.of(Company.class).map(Company$.NAME).sequential();
    AtomicBoolean wasClosed = new AtomicBoolean(false);
    stream.onClose(() -> wasClosed.set(true));
    stream.close();
    assertThat(wasClosed.get()).isTrue();
  }

  @Test
  void testCloseHandlerIsNullOnWrapperSearchStream() {
    SearchStream<String> stream = entityStream.of(Company.class).map(Company$.NAME).sequential();
    stream.close();
    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> {
      stream.findAny();
    });

    String expectedErrorMessage = "stream has already been operated upon or closed";
    Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void testFilterOnWrapperSearchStream() {
    Predicate<Integer> predicate = i -> (i > 2000);
    List<Integer> foundedAfter2000 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .filter(predicate) //
        .collect(Collectors.toList());

    assertThat(foundedAfter2000).contains(2011, 2003);
  }

  @Test
  void testFlatMapToIntOnMappedField() {
    // expected
    List<Integer> expected = new ArrayList<Integer>();
    for (Company company : entityStream.of(Company.class).collect(Collectors.toList())) {
      for (String tag : company.getTags()) {
        expected.add(tag.length());
      }
    }

    // actual
    IntStream tagLengthIntStream = entityStream //
        .of(Company.class) //
        .map(Company$.TAGS) //
        .sequential() //
        .flatMapToInt(tags -> tags.stream().mapToInt(String::length));

    List<Integer> actual = tagLengthIntStream.boxed().collect(Collectors.toList());

    assertThat(actual).containsExactlyElementsOf(expected);
  }

  @Test
  void testFlatMapToLongOnMappedField() {
    // expected
    List<Long> expected = new ArrayList<Long>();
    for (Company company : entityStream.of(Company.class).collect(Collectors.toList())) {
      for (String tag : company.getTags()) {
        expected.add(Long.valueOf(tag.length()));
      }
    }

    // actual
    LongStream tagLengthIntStream = entityStream //
        .of(Company.class) //
        .map(Company$.TAGS) //
        .sequential() //
        .flatMapToLong(tags -> tags.stream().mapToLong(String::length));

    List<Long> actual = tagLengthIntStream.boxed().collect(Collectors.toList());

    assertThat(actual).containsExactlyElementsOf(expected);
  }

  @Test
  void testFlatMapToDoubleOnMappedField() {
    // expected
    List<Double> expected = new ArrayList<Double>();
    for (Company company : entityStream.of(Company.class).collect(Collectors.toList())) {
      for (String tag : company.getTags()) {
        expected.add(Double.valueOf(tag.length()));
      }
    }

    // actual
    DoubleStream tagLengthDoubleStream = entityStream //
        .of(Company.class) //
        .map(Company$.TAGS) //
        .sequential() //
        .flatMapToDouble(tags -> tags.stream().mapToDouble(String::length));

    List<Double> actual = tagLengthDoubleStream.boxed().collect(Collectors.toList());

    assertThat(actual).containsExactlyElementsOf(expected);
  }

  @Test
  void testMapToIntOnReturnFields() {
    IntStream intStream = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .mapToInt(i -> i);

    assertThat(intStream.boxed().collect(Collectors.toList())).contains(2011, 1975, 2003);
  }

  @Test
  void testMapToLongOnReturnFields() {
    LongStream longStream = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .mapToLong(i -> i);

    assertThat(longStream.boxed().collect(Collectors.toList())).contains(2011L, 1975L, 2003L);
  }

  @Test
  void testMapToDoubleOnReturnFields() {
    DoubleStream doubleStream = entityStream //
        .of(User.class) //
        .map(User$.LOTTERY_WINNINGS) //
        .sequential() //
        .mapToDouble(w -> w);

    assertThat(doubleStream.boxed().collect(Collectors.toList())).contains(.9999, 1234.5678, 999.99, 899.0);
  }

  @Test
  void testForEachOrderedOnMappedField() {
    List<String> names = new ArrayList<String>();
    Consumer<? super String> testConsumer = (String companyName) -> names.add(companyName);
    entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .sequential() //
        .forEachOrdered(testConsumer);

    assertEquals(3, names.size());

    assertEquals(names.get(0), "RedisInc");
    assertEquals(names.get(1), "Microsoft");
    assertEquals(names.get(2), "Tesla");
  }

  @Test
  void testForEachOnMappedField() {
    List<String> names = new ArrayList<String>();
    Consumer<? super String> testConsumer = (String companyName) -> names.add(companyName);
    entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .sequential() //
        .forEach(testConsumer);

    assertEquals(3, names.size());

    assertEquals(names.get(0), "RedisInc");
    assertEquals(names.get(1), "Microsoft");
    assertEquals(names.get(2), "Tesla");
  }

  @Test
  void testToArrayOnMappedField() {
    Object[] allCompanies = entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .sequential() //
        .toArray();

    assertEquals(3, allCompanies.length);

    List<String> names = Stream.of(allCompanies).map(Object::toString).collect(Collectors.toList());

    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test
  void testToArrayTypedOnMappedField() {
    String[] namesArray = entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .sequential() //
        .toArray(String[]::new);

    assertEquals(3, namesArray.length);

    List<String> names = Stream.of(namesArray).collect(Collectors.toList());
    assertTrue(names.contains("RedisInc"));
    assertTrue(names.contains("Microsoft"));
    assertTrue(names.contains("Tesla"));
  }

  @Test
  void testCountOnMappedField() {
    long count = entityStream //
        .of(Company.class) //
        .filter( //
            Company$.NAME.notEq("RedisInc") //
                .and(Company$.NAME.notEq("Microsoft")) //
        ) //
        .map(Company$.NAME) //
        .sequential() //
        .count();

    assertEquals(1, count);
  }

  @Test
  void testLimitOnMappedField() {
    List<String> companies = entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .sequential() //
        .limit(2) //
        .collect(Collectors.toList());

    assertEquals(2, companies.size());
  }

  @Test
  void testSkipOnMappedField() {
    List<String> companies = entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .sequential() //
        .skip(1) //
        .collect(Collectors.toList());

    assertEquals(2, companies.size());
  }

  @Test
  void testSortOnMappedField() {
    List<String> names = entityStream //
        .of(Company.class) //
        .map(Company$.NAME) //
        .sequential() //
        .sorted(Comparator.reverseOrder()) //
        .collect(Collectors.toList());

    assertEquals(3, names.size());

    assertEquals(names.get(0), "Tesla");
    assertEquals(names.get(1), "RedisInc");
    assertEquals(names.get(2), "Microsoft");
  }

  @Test
  void testPeekOnMappedField() {
    final List<String> peekedEmails = new ArrayList<>();
    List<String> emails = entityStream //
        .of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .map(Company$.EMAIL) //
        .sequential() //
        .peek(email -> peekedEmails.add(email)) //
        .collect(Collectors.toList());

    assertThat(peekedEmails).containsExactly("stack@redis.com");

    assertEquals(1, emails.size());
  }

  @Test
  void testFindFirstOnMappedField() {
    Optional<String> maybeEmail = entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .map(Company$.EMAIL) //
        .sequential() //
        .findFirst();

    assertTrue(maybeEmail.isPresent());
    assertEquals("stack@redis.com", maybeEmail.get());
  }

  @Test
  void testFindAnyOnMappedField() {
    Optional<String> maybeEmail = entityStream.of(Company.class) //
        .filter(Company$.NAME.eq("RedisInc")) //
        .map(Company$.EMAIL) //
        .sequential() //
        .findAny();

    assertTrue(maybeEmail.isPresent());
    assertEquals("stack@redis.com", maybeEmail.get());
  }

  @Test
  void testReduceWithMethodReferenceOnMappedField() {
    int result = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .reduce(0, (t, u) -> Integer.sum(t, u));

    assertThat(result).isEqualTo(2011 + 1975 + 2003);
  }

  @Test
  void testReduceWithLambdaOnMappedField() {
    int result = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .reduce(0, (subtotal, element) -> subtotal + element);

    assertThat(result).isEqualTo(2011 + 1975 + 2003);
  }

  @Test
  void testReduceWithCombinerOnMappedField() {
    BinaryOperator<Integer> establishedFirst = (c1, c2) -> c1 < c2 ? c1 : c2;

    Optional<Integer> firstEstablish = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .reduce(establishedFirst);

    assertAll( //
        () -> assertThat(firstEstablish).isPresent(), //
        () -> assertThat(firstEstablish).contains(1975) //
    );
  }

  @Test
  void testReduceWithIdentityBifunctionAndBinaryOperatorOnMappedField() {
    Integer firstEstablish = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .reduce(Integer.MAX_VALUE, (minimum, yearFounded) -> Integer.min(minimum, yearFounded), (t, u) -> Integer.min(t, u));
    assertThat(firstEstablish).isEqualTo(1975);
  }

  @Test
  void testCollectWithSupplierAccumulatorAndCombinerOnMappedField() {
    Supplier<AtomicInteger> supplier = AtomicInteger::new;
    BiConsumer<AtomicInteger, Integer> accumulator = (AtomicInteger a, Integer i) -> {
      a.set(a.get() + i);
    };

    BiConsumer<AtomicInteger, AtomicInteger> combiner = (a1, a2) -> {
      a1.set(a1.get() + a2.get());
    };

    AtomicInteger result = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .collect(supplier, accumulator, combiner);

    assertThat(result.intValue()).isEqualTo(2011 + 1975 + 2003);
  }

  @Test
  void testMinOnMappedField() {
    Optional<Integer> firstEstablish = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .min(Integer::compareTo);
    assertAll( //
        () -> assertThat(firstEstablish).isPresent(), //
        () -> assertThat(firstEstablish).contains(1975) //
    );
  }

  @Test
  void testMaxOnMappedField() {
    Optional<Integer> lastEstablish = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .max(Integer::compareTo);
    assertAll( //
        () -> assertThat(lastEstablish).isPresent(), //
        () -> assertThat(lastEstablish).contains(2011) //
    );
  }

  @Test
  void testAnyMatchOnMappedField() {
    boolean c1975 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .anyMatch(c -> c.intValue() == 1975);

    boolean c1976 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .anyMatch(c -> c.intValue() == 1976);

    assertThat(c1975).isTrue();
    assertThat(c1976).isFalse();
  }

  @Test
  void testAllMatchOnMappedField() {
    boolean allEstablishedBefore1970 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .allMatch(c -> c.intValue() < 1970);

    boolean allEstablishedOnOrAfter1970 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .allMatch(c -> c.intValue() >= 1970);

    assertThat(allEstablishedOnOrAfter1970).isTrue();
    assertThat(allEstablishedBefore1970).isFalse();
  }

  @Test
  void testNoneMatchOnMappedField() {
    boolean noneIn1975 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .noneMatch(c -> c.intValue() == 1975);

    boolean noneIn1976 = entityStream //
        .of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .noneMatch(c -> c.intValue() == 1976);

    assertThat(noneIn1976).isTrue();
    assertThat(noneIn1975).isFalse();
  }

  @Test
  void testMapOnMappedField() {
    ToLongFunction<Integer> func = y -> y - 1;

    List<Long> yearsMinusOne = entityStream.of(Company.class) //
        .map(Company$.YEAR_FOUNDED) //
        .sequential() //
        .map(func) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(yearsMinusOne).hasSize(3), //
        () -> assertThat(yearsMinusOne).containsExactly(2010L, 1974L, 2002L) //
    );
  }

}
