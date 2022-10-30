package com.redis.om.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.Bucket;
import org.springframework.data.redis.core.convert.RedisData;

import com.redis.om.spring.annotations.hash.fixtures.Company;
import com.redis.om.spring.annotations.hash.fixtures.CompanyRepository;

class RedisEnhancedKeyValueAdapterTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  @Qualifier("redisCustomKeyValueTemplate")
  CustomRedisKeyValueTemplate kvTemplate;
  
  @Autowired
  RedisTemplate<String, String> template;
  
  RedisEnhancedKeyValueAdapter adapter;

  @Autowired
  CompanyRepository repository;

  @Test
  void testPutRedisData() {
    RedisData rdo = new RedisData(Bucket.newBucketFromStringMap(Collections.singletonMap("firstname", "rand")));
    rdo.setId("abc");
    rdo.setKeyspace("redisdata");
    kvTemplate.getAdapter().put("abc", rdo, "redisdata");
    
    Object firstName = template.opsForHash().get("redisdata:abc", "firstname");
    assertThat(firstName).hasToString("rand");
  }
  
  Company redis;
  Company microsoft;

  @BeforeEach
  void createData() {
    adapter = (RedisEnhancedKeyValueAdapter) kvTemplate.getAdapter();
    repository.deleteAll();
    redis = repository.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    microsoft = repository.save(Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com"));
  }

  @Test
  void testGetAllOf() {
    assertEquals(2, repository.count());
    Iterable<Company> companies = adapter.getAllOf("com.redis.om.spring.annotations.hash.fixtures.Company",
        Company.class);
    assertAll( //
        () -> assertThat(companies).hasSize(2) //
    );
  }
  
  @Test
  void testGetAllOfWithRowsSet() {
    assertEquals(2, repository.count());
    Iterable<Company> companies = adapter.getAllOf("com.redis.om.spring.annotations.hash.fixtures.Company",
        Company.class, 0, 1);
    assertAll( //
        () -> assertThat(companies).hasSize(1) //
    );
  }

  @Test
  void testGetAllIds() {
    String keyspace = "com.redis.om.spring.annotations.hash.fixtures.Company";
    assertEquals(2, repository.count());
    List<String> keys = adapter.getAllIds(keyspace, Company.class);
    assertAll( //
        () -> assertThat(keys).hasSize(2), //
        () -> assertThat(keys).contains(redis.getId()), //
        () -> assertThat(keys).contains(microsoft.getId()) //
    );
  }
}
