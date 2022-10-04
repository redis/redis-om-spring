package com.redis.om.spring.annotations.document.serialization;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import com.google.gson.JsonObject;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.KitchenSink;
import com.redis.om.spring.annotations.document.fixtures.KitchenSinkRepository;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;

class SerializationTest extends AbstractBaseDocumentTest {
  @Autowired
  KitchenSinkRepository repository;

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  private KitchenSink ks;
  private KitchenSink ks1;
  private KitchenSink ks2;

  private LocalDate localDate;
  private LocalDateTime localDateTime;
  private Date date;
  private Point point;
  private Ulid ulid;
  private Instant instant;

  private Set<String> setThings;
  private List<String> listThings;


  @BeforeEach
  void cleanUp() {
    repository.deleteAll();

    localDate = LocalDate.now();
    localDateTime = LocalDateTime.now();
    date = new Date();
    point = new Point(33.62826024782707, -111.83592170193586);
    ulid = UlidCreator.getMonotonicUlid();
    setThings = Set.of("thingOne", "thingTwo", "thingThree");
    listThings = List.of("redFish", "blueFish");
    instant = Instant.now();

    ks = KitchenSink.builder() //
        .localDate(localDate) //
        .localDateTime(localDateTime) //
        .date(date) //
        .point(point) //
        .ulid(ulid) //
        .setThings(setThings) //
        .listThings(listThings) //
        .instant(instant) //
        .build();

    ks1 = KitchenSink.builder() //
        .localDate(localDate) //
        .localDateTime(localDateTime) //
        .date(date) //
        .point(point) //
        .ulid(ulid) //
        .setThings(Set.of()) //
        .listThings(List.of()) //
        .instant(instant) //
        .build();

    ks2 = KitchenSink.builder() //
        .localDate(localDate) //
        .localDateTime(localDateTime) //
        .date(date) //
        .point(point) //
        .ulid(ulid) //
        .instant(instant) //
        .build();

    ks2.setSetThings(null);
    ks2.setListThings(null);

    repository.saveAll(List.of(ks, ks1, ks2));
  }

  @Test
  void testJSONSerialization() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();

    // LocalDate
    Instant localDateInstant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    long localDateAsUnixTS = localDateInstant.getEpochSecond();

    // LocalDateTime
    Instant localDateTimeInstant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    long localDateTimeInMillis = localDateTimeInstant.toEpochMilli();

    // Date
    long dateInMillis = date.getTime();

    // Point
    String redisGeo = "33.62826024782707,-111.83592170193586";
    
    // Instant
    long instantInMillis = instant.getEpochSecond();

    JsonObject rawJSON = ops.get(KitchenSink.class.getName() + ":" + ks.getId(), JsonObject.class);

    assertThat(rawJSON.get("localDate").getAsLong()).isEqualTo(localDateAsUnixTS);
    assertThat(rawJSON.get("localDateTime").getAsLong()).isEqualTo(localDateTimeInMillis);
    assertThat(rawJSON.get("date").getAsLong()).isEqualTo(dateInMillis);
    assertThat(rawJSON.get("point").getAsString()).isEqualTo(redisGeo);
    assertThat(rawJSON.get("ulid").getAsString()).isEqualTo(ulid.toString());
    assertThat(rawJSON.get("instant").getAsLong()).isEqualTo(instantInMillis);
    assertThat(Arrays.asList(rawJSON.get("setThings").getAsString().split("\\|"))).containsExactlyInAnyOrder("thingOne", "thingTwo", "thingThree");
    assertThat(rawJSON.get("listThings").getAsString()).isEqualTo("redFish|blueFish");
  }

  @Test
  void testJSONDeserialization() {
    Optional<KitchenSink> fromDb = repository.findById(ks.getId());
    assertThat(fromDb).isPresent();
    assertThat(fromDb.get().getLocalDate()).isEqualTo(localDate);
    assertThat(fromDb.get().getDate()).isEqualTo(date);
    assertThat(fromDb.get().getPoint()).isEqualTo(point);
    assertThat(fromDb.get().getUlid()).isEqualTo(ulid);

    assertThat(fromDb.get().getSetThings()).isEqualTo(setThings);
    assertThat(fromDb.get().getListThings()).isEqualTo(listThings);
    // NOTE: We lose nanosecond precision in order to store LocalDateTime as long in order to allow for RediSearch range queries
    assertThat(fromDb.get().getLocalDateTime()).isEqualToIgnoringNanos(localDateTime);
    assertThat(fromDb.get().getInstant().getEpochSecond()).isEqualTo(instant.getEpochSecond());
  }

  @Test
  void testEmptySetToStringDeserialization() {
    Optional<KitchenSink> fromDb1 = repository.findById(ks1.getId());
    assertThat(fromDb1.get().getSetThings()).isNull();
    assertThat(fromDb1.get().getListThings()).isNull();

    Optional<KitchenSink> fromDb2 = repository.findById(ks2.getId());
    assertThat(fromDb2.get().getSetThings()).isNull();
    assertThat(fromDb2.get().getListThings()).isNull();
  }

  @Test
  void testStringEncodedSerializationForUlid() {
    var serializer = new JdkSerializationRedisSerializer();
    String value = UlidCreator.getMonotonicUlid().toString();
    byte[] serialized = serializer.serialize(value);
    assertThat(serializer.deserialize(serialized)).isEqualTo(value);
  }
}
