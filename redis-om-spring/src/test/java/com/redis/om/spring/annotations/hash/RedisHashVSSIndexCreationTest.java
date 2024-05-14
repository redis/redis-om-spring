package com.redis.om.spring.annotations.hash;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.HashWithVectors;
import com.redis.om.spring.annotations.hash.fixtures.HashWithVectorsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.args.SortingOrder;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.FTSearchParams;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class RedisHashVSSIndexCreationTest extends AbstractBaseEnhancedRedisTest {
  private static final String INDEX = "com.redis.om.spring.annotations.hash.fixtures.HashWithVectorsIdx";
  @Autowired
  HashWithVectorsRepository repository;
  @Autowired
  JedisConnectionFactory jedisConnectionFactory;
  private UnifiedJedis jedis;
  private HashWithVectors hwv1;

  @BeforeEach
  void cleanUp() {
    repository.deleteAll();
    hwv1 = repository.save(HashWithVectors.of("aaaaaaaa", "aaaaaaaa", "aaaaaaaa", "aaaaaaaa"));
    repository.save(HashWithVectors.of("aaaabaaa", "aaaabaaa", "aaaabaaa", "aaaabaaa"));
    repository.save(HashWithVectors.of("aaaaabaa", "aaaaabaa", "aaaaabaa", "aaaaabaa"));

    jedis = new JedisPooled(Objects.requireNonNull(jedisConnectionFactory.getPoolConfig()),
      jedisConnectionFactory.getHostName(), jedisConnectionFactory.getPort());
  }

  @Test
  void testFlatVectorFieldIndexCreationWithVectorIndexed() {
    FTSearchParams searchParams = FTSearchParams.searchParams().addParam("vec", "aaaaaaaa")
      .sortBy("__flat_score", SortingOrder.ASC).returnFields("__flat_score").dialect(2);

    Document doc1 = jedis.ftSearch(INDEX, "*=>[KNN 2 @flat $vec]", searchParams).getDocuments().get(0);

    assertThat(doc1.getId()).isEqualTo("com.redis.om.spring.annotations.hash.fixtures.HashWithVectors:" + hwv1.getId());
    assertThat(doc1.get("__flat_score")).isEqualTo("0");
  }

  @Test
  void testHNSWVectorFieldIndexCreationWithVectorIndexed() {
    FTSearchParams searchParams = FTSearchParams.searchParams().addParam("vec", "aaaaaaaa")
      .sortBy("__hnsw_score", SortingOrder.ASC).returnFields("__hnsw_score").dialect(2);

    Document doc1 = jedis.ftSearch(INDEX, "*=>[KNN 2 @hnsw $vec]", searchParams).getDocuments().get(0);

    assertThat(doc1.getId()).isEqualTo("com.redis.om.spring.annotations.hash.fixtures.HashWithVectors:" + hwv1.getId());
    assertThat(doc1.get("__hnsw_score")).isEqualTo("0");
  }

  @Test
  void testFlatVectorFieldIndexCreationWithIndexed() {
    FTSearchParams searchParams = FTSearchParams.searchParams().addParam("vec", "aaaaaaaa")
      .sortBy("__flat2_score", SortingOrder.ASC).returnFields("__flat2_score").dialect(2);

    Document doc1 = jedis.ftSearch(INDEX, "*=>[KNN 2 @flat2 $vec]", searchParams).getDocuments().get(0);

    assertThat(doc1.getId()).isEqualTo("com.redis.om.spring.annotations.hash.fixtures.HashWithVectors:" + hwv1.getId());
    assertThat(doc1.get("__flat2_score")).isEqualTo("0");
  }

  @Test
  void testHNSWVectorFieldIndexCreationWithIndexed() {
    FTSearchParams searchParams = FTSearchParams.searchParams().addParam("vec", "aaaaaaaa")
      .sortBy("__hnsw2_score", SortingOrder.ASC).returnFields("__hnsw2_score").dialect(2);

    Document doc1 = jedis.ftSearch(INDEX, "*=>[KNN 2 @hnsw2 $vec]", searchParams).getDocuments().get(0);

    assertThat(doc1.getId()).isEqualTo("com.redis.om.spring.annotations.hash.fixtures.HashWithVectors:" + hwv1.getId());
    assertThat(doc1.get("__hnsw2_score")).isEqualTo("0");
  }
}
