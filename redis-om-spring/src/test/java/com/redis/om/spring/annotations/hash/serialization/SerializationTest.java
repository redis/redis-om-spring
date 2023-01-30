package com.redis.om.spring.annotations.hash.serialization;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.KitchenSink;
import com.redis.om.spring.annotations.hash.fixtures.KitchenSinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpellCheckingInspection") class SerializationTest extends AbstractBaseEnhancedRedisTest {

  @Autowired StringRedisTemplate template;

  @Autowired
  KitchenSinkRepository repository;

  private KitchenSink ks;
  private KitchenSink ks1;
  private KitchenSink ks2;
  private KitchenSink ks3;
  private KitchenSink ks4;

  private LocalDate localDate;
  private LocalDateTime localDateTime;
  private Date date;
  private Point point;
  private Ulid ulid;
  private byte[] byteArray;

  private Set<String> setThings;
  private List<String> listThings;

  @BeforeEach
  public void cleanUp() {
    repository.deleteAll();
    flushSearchIndexFor(KitchenSink.class);

    localDate = LocalDate.now();
    localDateTime = LocalDateTime.now();
    date = new Date();
    point = new Point(-111.83592170193586,33.62826024782707);
    ulid = UlidCreator.getMonotonicUlid();
    byteArray = "Hello World!".getBytes();

    List<String[]> listOfStringArrays = new ArrayList<>();
    listOfStringArrays.add(new String[] {"a", "b"});
    listOfStringArrays.add(new String[] {"c", "d"});
    listOfStringArrays.add(new String[] { null, "e"});
    listOfStringArrays.add(null);
    
    setThings = Set.of("thingOne", "thingTwo", "thingThree");
    listThings = List.of("redFish", "blueFish");

    ks = KitchenSink.builder() //
        .localDate(localDate) //
        .localDateTime(localDateTime) //
        .date(date) //
        .point(point) //
        .ulid(ulid) //
        .setThings(setThings) //
        .listThings(listThings) //
        .build();

    ks1 = KitchenSink.builder() //
        .localDate(localDate) //
        .localDateTime(localDateTime) //
        .date(date) //
        .point(point) //
        .ulid(ulid) //
        .setThings(Set.of()) //
        .listThings(List.of()) //
        .build();

    ks2 = KitchenSink.builder() //
        .localDate(localDate) //
        .localDateTime(localDateTime) //
        .date(date) //
        .point(point) //
        .ulid(ulid) //
        .build();

    ks2.setSetThings(null);
    ks2.setListThings(null);
    
    ks3 = KitchenSink.builder() //
        .localDate(localDate) //
        .localDateTime(localDateTime) //
        .date(date) //
        .point(point) //
        .ulid(ulid) //
        .build();
    
    ks3.setUlid(null);
    ks3.setByteArray(byteArray);
    
    ks4 = KitchenSink.builder() //
        .localDate(localDate) //
        .localDateTime(localDateTime) //
        .date(date) //
        .point(point) //
        .ulid(ulid) //
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
    long rawLocalDateTime = Long.parseLong(Objects.requireNonNull(template.opsForHash().get(key, "localDateTime")).toString());

    // Date
    long dateInMillis = date.getTime();
    long rawDate = Long.parseLong(Objects.requireNonNull(template.opsForHash().get(key, "date")).toString());

    // Point
    String redisGeo = "-111.83592170193586,33.62826024782707";

    String rawPoint = Objects.requireNonNull(template.opsForHash().get(key, "point")).toString();
    String rawUlid = Objects.requireNonNull(template.opsForHash().get(key, "ulid")).toString();
    
    //
    String rawSetThings = Objects.requireNonNull(template.opsForHash().get(key, "setThings")).toString();
    String rawListThings = Objects.requireNonNull(template.opsForHash().get(key, "listThings")).toString();

    assertThat(rawLocalDate).isEqualTo(localDateAsUnixTS);
    assertThat(rawLocalDateTime).isEqualTo(localDateTimeInMillis);
    assertThat(rawDate).isEqualTo(dateInMillis);
    assertThat(rawPoint).isEqualTo(redisGeo);
    assertThat(rawUlid).isEqualTo(ulid.toString());
    assertThat(rawSetThings.split("\\|")).containsExactlyInAnyOrder("thingOne", "thingTwo", "thingThree");
    assertThat(rawListThings).isEqualTo("redFish|blueFish");
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
    assertThat(fromDb.get().getSetThings()).isEqualTo(setThings);
    assertThat(fromDb.get().getListThings()).isEqualTo(listThings);
  }
  
  @Test
  void testLocalDateDeSerializationInQuery() {
    List<KitchenSink> all = repository.findByLocalDateGreaterThan(localDate.minus(2, ChronoUnit.DAYS));
    assertThat(all).containsExactlyInAnyOrder(ks, ks1, ks2, ks3, ks4);
  }
  
  @Test
  void testEmptyUlidReturnsAsNull() {
    Optional<KitchenSink> fromDb = repository.findById(ks3.getId());
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
  void testCantPersistCollectionWithNulls() {
    Optional<KitchenSink> fromDb = repository.findById(ks4.getId());
    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getListOfStringArrays()).isNull();
  }

}
