package com.redis.om.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;

import com.redis.om.spring.annotations.document.fixtures.Company;
import com.redis.om.spring.annotations.document.fixtures.CompanyMeta;
import com.redis.om.spring.annotations.document.fixtures.CompanyRepository;

class RedisJSONKeyValueAdapterTest extends AbstractBaseDocumentTest {
  @Autowired
  RedisJSONKeyValueAdapter adapter;

  @Autowired
  CompanyRepository repository;

  Company redis;
  Company microsoft;

  @BeforeEach
  void createData() {
    repository.deleteAll();

    redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");
    redis.setMetaList(Set.of(CompanyMeta.of("Redis", 100, Set.of("RedisTag"))));

    microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), new Point(-122.124500, 47.640160),
        "research@microsoft.com");
    microsoft.setMetaList(Set.of(CompanyMeta.of("MS", 50, Set.of("MsTag"))));

    repository.saveAll(List.of(redis, microsoft));
  }

  @Test
  void testGetAllOf() {
    assertEquals(2, repository.count());
    Iterable<Company> companies = adapter.getAllOf("com.redis.om.spring.annotations.document.fixtures.Company",
        Company.class);
    assertAll( //
        () -> assertThat(companies).hasSize(2) //
    );
  }

  @Test
  void testGetAllOfWithRowsSet() {
    assertEquals(2, repository.count());
    Iterable<Company> companies = adapter.getAllOf("com.redis.om.spring.annotations.document.fixtures.Company",
        Company.class, 0, 1);
    assertAll( //
        () -> assertThat(companies).hasSize(1) //
    );
  }

  @Test
  void testGetAllKeys() {
    String keyspace = "com.redis.om.spring.annotations.document.fixtures.Company";
    assertEquals(2, repository.count());
    List<String> keys = adapter.getAllKeys(keyspace, Company.class);
    assertAll( //
        () -> assertThat(keys).hasSize(2), //
        () -> assertThat(keys).contains(String.format("%s:%s", keyspace, redis.getId())), //
        () -> assertThat(keys).contains(String.format("%s:%s", keyspace, microsoft.getId())) //
    );
  }
}
