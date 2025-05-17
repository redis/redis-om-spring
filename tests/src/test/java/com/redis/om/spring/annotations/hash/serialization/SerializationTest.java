package com.redis.om.spring.annotations.hash.serialization;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.time.*;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.KitchenSink;
import com.redis.om.spring.fixtures.hash.repository.KitchenSinkRepository;
import com.redis.om.spring.vectorize.DefaultEmbedder;
import com.redis.om.spring.vectorize.Embedder;

@SuppressWarnings(
  "SpellCheckingInspection"
)
class SerializationTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  StringRedisTemplate template;

  @Autowired
  KitchenSinkRepository repository;

  @Autowired
  Embedder embedder;

  @Autowired
  private ApplicationContext applicationContext;

  private KitchenSink ks;
  private KitchenSink ks1;
  private KitchenSink ks2;
  private KitchenSink ks3;
  private KitchenSink ks4;

  private LocalDate localDate;
  private LocalDateTime localDateTime;
  private OffsetDateTime localOffsetDateTime;
  private Date date;
  private YearMonth yearMonth;
  private Point point;
  private Ulid ulid;
  private byte[] byteArray;
  private byte[] byteArray2;

  private Set<String> setThings;
  private List<String> listThings;

  @BeforeEach
  public void cleanUp() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    repository.deleteAll();
    flushSearchIndexFor(KitchenSink.class);

    localDate = LocalDate.now();
    localDateTime = LocalDateTime.now();
    localOffsetDateTime = OffsetDateTime.now();
    date = new Date();
    point = new Point(-111.83592170193586, 33.62826024782707);
    ulid = UlidCreator.getMonotonicUlid();
    byteArray = "Hello World!".getBytes();

    java.lang.reflect.Method method = DefaultEmbedder.class.getDeclaredMethod("getImageEmbeddingsAsByteArrayFor",
        InputStream.class);
    method.setAccessible(true);

    byteArray2 = (byte[]) method.invoke(embedder, applicationContext.getResource("classpath:/images/cat.jpg")
        .getInputStream());
    yearMonth = YearMonth.of(1972, 6);

    List<String[]> listOfStringArrays = new ArrayList<>();
    listOfStringArrays.add(new String[] { "a", "b" });
    listOfStringArrays.add(new String[] { "c", "d" });
    listOfStringArrays.add(new String[] { null, "e" });
    listOfStringArrays.add(null);

    setThings = Set.of("thingOne", "thingTwo", "thingThree");
    listThings = List.of("redFish", "blueFish");

    ks = KitchenSink.builder() //
        .name("ks") //
        .localDate(localDate) //
        .localDateTime(localDateTime) //
        .localOffsetDateTime(localOffsetDateTime) //
        .date(date) //
        .point(point) //
        .ulid(ulid) //
        .setThings(setThings) //
        .listThings(listThings) //
        .yearMonth(yearMonth) //
        .build();

    ks1 = KitchenSink.builder() //
        .name("ks1") //
        .localDate(localDate) //
        .localDateTime(localDateTime) //
        .localOffsetDateTime(localOffsetDateTime) //
        .date(date) //
        .point(point) //
        .ulid(ulid) //
        .setThings(Set.of()) //
        .listThings(List.of()) //
        .yearMonth(yearMonth) //
        .build();

    ks2 = KitchenSink.builder() //
        .name("ks2") //
        .localDate(localDate) //
        .localDateTime(localDateTime) //
        .localOffsetDateTime(localOffsetDateTime) //
        .date(date) //
        .point(point) //
        .ulid(ulid) //
        .yearMonth(yearMonth) //
        .build();

    ks2.setSetThings(null);
    ks2.setListThings(null);
    ks2.setByteArray(byteArray2);

    ks3 = KitchenSink.builder() //
        .name("ks3") //
        .localDate(localDate) //
        .localDateTime(localDateTime) //
        .localOffsetDateTime(localOffsetDateTime) //
        .date(date) //
        .point(point) //
        .ulid(ulid) //
        .yearMonth(yearMonth) //
        .build();

    ks3.setUlid(null);
    ks3.setByteArray(byteArray);

    ks4 = KitchenSink.builder() //
        .name("ks4") //
        .localDate(localDate) //
        .localDateTime(localDateTime) //
        .localOffsetDateTime(localOffsetDateTime) //
        .date(date) //
        .point(point) //
        .ulid(ulid) //
        .yearMonth(yearMonth) //
        .build();

    ks4.setUlid(null);
    ks4.setByteArray(null);
    ks4.setListOfStringArrays(listOfStringArrays);

    repository.saveAll(List.of(ks, ks1, ks2, ks3, ks4));
  }

  @Test
  void testHashSerialization() {
    String key = KitchenSink.class.getName() + ":" + ks.getId();
    // LocalDate
    Instant localDateInstant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    long localDateAsUnixTS = localDateInstant.getEpochSecond();
    long rawLocalDate = Long.parseLong(Objects.requireNonNull(template.opsForHash().get(key, "localDate")).toString());

    // LocalDateTime
    Instant localDateTimeInstant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    long localDateTimeInMillis = localDateTimeInstant.toEpochMilli();
    long rawLocalDateTime = Long.parseLong(Objects.requireNonNull(template.opsForHash().get(key, "localDateTime"))
        .toString());

    // OffsetDateTime
    Instant localOffsetDateTimeInstant = localOffsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toInstant();
    long localOffsetDateTimeInMillis = localOffsetDateTimeInstant.toEpochMilli();
    long rawlocalOffsetDateTime = Long.parseLong(Objects.requireNonNull(template.opsForHash().get(key,
        "localOffsetDateTime")).toString());

    // Date
    long dateInMillis = date.getTime();
    long rawDate = Long.parseLong(Objects.requireNonNull(template.opsForHash().get(key, "date")).toString());

    // YearMonth
    String rawYearMonth = Objects.requireNonNull(template.opsForHash().get(key, "yearMonth")).toString();

    // Point
    String redisGeo = "-111.83592170193586,33.62826024782707";

    String rawPoint = Objects.requireNonNull(template.opsForHash().get(key, "point")).toString();
    String rawUlid = Objects.requireNonNull(template.opsForHash().get(key, "ulid")).toString();

    //
    String rawSetThings = Objects.requireNonNull(template.opsForHash().get(key, "setThings")).toString();
    String rawListThings = Objects.requireNonNull(template.opsForHash().get(key, "listThings")).toString();

    assertThat(rawLocalDate).isEqualTo(localDateAsUnixTS);
    assertThat(rawLocalDateTime).isEqualTo(localDateTimeInMillis);
    assertThat(rawlocalOffsetDateTime).isEqualTo(localOffsetDateTimeInMillis);
    assertThat(rawDate).isEqualTo(dateInMillis);
    assertThat(rawPoint).isEqualTo(redisGeo);
    assertThat(rawUlid).isEqualTo(ulid.toString());
    assertThat(rawSetThings.split("\\|")).containsExactlyInAnyOrder("thingOne", "thingTwo", "thingThree");
    assertThat(rawListThings).isEqualTo("redFish|blueFish");
    assertThat(rawYearMonth).isEqualTo("1972-06");
  }

  @Test
  void testHashDeserialization() {
    Optional<KitchenSink> fromDb = repository.findById(ks.getId());
    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getLocalDate()).isEqualTo(localDate);
    assertThat(fromDb.get().getDate()).isEqualTo(date);
    assertThat(fromDb.get().getPoint()).isEqualTo(point);
    assertThat(fromDb.get().getUlid()).isEqualTo(ulid);
    // NOTE: We lose nanosecond precision in order to store LocalDateTime as long in
    // order to allow for RediSearch range queries
    assertThat(fromDb.get().getLocalDateTime()).isEqualToIgnoringNanos(localDateTime);
    assertThat(fromDb.get().getLocalOffsetDateTime()).isEqualToIgnoringNanos(localOffsetDateTime);
    assertThat(fromDb.get().getSetThings()).isEqualTo(setThings);
    assertThat(fromDb.get().getListThings()).isEqualTo(listThings);
    assertThat(fromDb.get().getYearMonth()).isEqualTo(yearMonth);
  }

  @Test
  void testHashDeserializationUsingCustomFinder() {
    Optional<KitchenSink> fromDb = repository.findFirstByName("ks");
    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getLocalDate()).isEqualTo(localDate);
    assertThat(fromDb.get().getDate()).isEqualTo(date);
    assertThat(fromDb.get().getPoint()).isEqualTo(point);
    assertThat(fromDb.get().getUlid()).isEqualTo(ulid);
    // NOTE: We lose nanosecond precision in order to store LocalDateTime as long in
    // order to allow for RediSearch range queries
    assertThat(fromDb.get().getLocalDateTime()).isEqualToIgnoringNanos(localDateTime);
    assertThat(fromDb.get().getLocalOffsetDateTime()).isEqualToIgnoringNanos(localOffsetDateTime);
    assertThat(fromDb.get().getSetThings()).isEqualTo(setThings);
    assertThat(fromDb.get().getListThings()).isEqualTo(listThings);
    assertThat(fromDb.get().getYearMonth()).isEqualTo(yearMonth);
  }

  @Test
  void testLocalDateDeSerializationInQuery() {
    List<KitchenSink> all = repository.findByLocalDateGreaterThan(localDate.minusDays(2));
    assertThat(all).containsExactlyInAnyOrder(ks, ks1, ks2, ks3, ks4);
  }

  @Test
  void testEmptyUlidReturnsAsNull() {
    Optional<KitchenSink> fromDb = repository.findById(ks3.getId());
    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getUlid()).isNull();
  }

  @Test
  void testEmptyUlidReturnsAsNullUsingCustomFinder() {
    Optional<KitchenSink> fromDb = repository.findFirstByName("ks3");
    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getUlid()).isNull();
  }

  @Test
  void testArraySerialization() {
    Optional<KitchenSink> fromDb = repository.findById(ks3.getId());
    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getByteArray()).isEqualTo(byteArray);
  }

  @Test
  void testArraySerializationUsingCustomFinder() {
    Optional<KitchenSink> fromDb = repository.findFirstByName("ks3");
    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getByteArray()).isEqualTo(byteArray);
  }

  @Test
  void testArraySerializationLargeBlob() {
    Optional<KitchenSink> fromDb = repository.findById(ks2.getId());
    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getByteArray()).isEqualTo(byteArray2);
  }

  @Test
  void testArraySerializationLargeBlobUsingCustomFinder() {
    Optional<KitchenSink> fromDb = repository.findFirstByName("ks2");
    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getByteArray()).isEqualTo(byteArray2);
  }

  @Test
  void testCantPersistCollectionWithNulls() {
    Optional<KitchenSink> fromDb = repository.findById(ks4.getId());
    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getListOfStringArrays()).isNull();
  }

  @Test
  void testCantPersistCollectionWithNullsUsingCustomFinder() {
    Optional<KitchenSink> fromDb = repository.findFirstByName("ks4");
    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getListOfStringArrays()).isNull();
  }
}
