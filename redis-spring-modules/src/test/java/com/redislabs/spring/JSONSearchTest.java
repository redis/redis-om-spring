package com.redislabs.spring;

import static org.junit.Assert.assertEquals;

import javax.annotation.PreDestroy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.redislabs.spring.ops.RedisModulesOperations;
import com.redislabs.spring.ops.json.JSONOperations;
import com.redislabs.spring.ops.search.SearchOperations;

import io.redisearch.AggregationResult;
import io.redisearch.Query;
import io.redisearch.Schema;
import io.redisearch.SearchResult;
import io.redisearch.aggregation.AggregationBuilder;
import io.redisearch.client.Client;
import io.redisearch.client.IndexDefinition;
import redis.clients.jedis.exceptions.JedisDataException;

@SpringBootTest(classes = JSONSearchTest.Config.class)
public class JSONSearchTest {
  public static String searchIndex = "idx";
  
  @Autowired
  RedisModulesOperations<String, String> modulesOperations;
  
  /**
   *  FT.CREATE idx ON JSON SCHEMA $.title AS title TEXT $.tag[*] AS tag TAG
   *  JSON.SET doc1 . '{"title":"hello world", "tag": ["news", "article"]}'
   *  FT.SEARCH idx '@title:hello @tag:{news}'
   *  1) (integer) 1
   *  2) "doc1"
   *  3) 1) "$"
   *     2) "{\"title\":\"hello world\",\"tag\":[\"news\",\"article\"]}"
   */
  @Test
  public void testBasicSearchOverJSON() {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);
    SearchResult result = ops.search(new Query("@title:hello @tag:{news}"));
    assertEquals(1, result.totalResults);
  }
  
  /*
   * FT.SEARCH idx * RETURN 3 $.tag[0] AS first_tag
   * 1) (integer) 1
   * 2) "doc1"
   * 3) 1) "first_tag"
   *    2) "\"news\""
   */
  @Test
  public void testSearchOverJSONWithPathProjection() {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);
    SearchResult result = ops.search(new Query("*").returnFields("$.tag[0] AS first_tag"));
    assertEquals(1, result.totalResults);
  }
  
  /*
   * FT.AGGREGATE
   * LOAD using JSON Path
   * FT.AGGREGATE idx * LOAD 3 $.tag[1] AS tag2 
   * 1) (integer) 1
   * 2) 1) "tag2"
   *    2) "\"article\""
   */
  @Test
  public void testAggregateLoadUsingJSONPath() {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);

    AggregationBuilder r = new AggregationBuilder()
        .load("$.tag[1]", "AS", "tag2");

    // actual search
    AggregationResult res = ops.aggregate(r);
    assertEquals(1, res.totalResults);
  }

  @SpringBootApplication
  @Configuration
  static class Config {
    @Autowired
    RedisModulesOperations<String, String> modulesOperations;
    
    @Autowired
    RedisTemplate<String, String> template;

    @Bean
    CommandLineRunner loadTestData(RedisTemplate<String, String> template) {
      return args -> {
        System.out.println(">>> loadTestData...");
        JSONOperations<String> ops = modulesOperations.opsForJSON();
        // JSON.SET doc1 . '{"title":"hello world", "tag": ["news", "article"]}'
        ops.set("doc1", "{\"title\":\"hello world\", \"tag\": [\"news\", \"article\"]}");
      };
    }

    @Bean
    CommandLineRunner createSearchIndices(RedisModulesOperations<String, String> modulesOperations) {
      return args -> {
        // FT.CREATE idx ON JSON SCHEMA $.title AS title TEXT $.tag[*] AS tag TAG
        System.out.println(">>> Creating " + searchIndex);

        SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);
        try {
          ops.dropIndex();
        } catch (JedisDataException jdee) {
          // IGNORE: Unknown Index name
        }

        Schema sc = new Schema() //
            .addTextField("first", 1.0) //
            .addTextField("last", 1.0) //
            .addNumericField("age");

        IndexDefinition def = new IndexDefinition().setPrefixes(new String[] { "student:", "pupil:" });

        ops.createIndex(sc, Client.IndexOptions.defaultOptions().setDefinition(def));
      };
    }
    
    @Autowired
    RedisConnectionFactory connectionFactory;
    
    @PreDestroy
    void cleanUp() {
      connectionFactory.getConnection().flushAll();
    }
  }

}
