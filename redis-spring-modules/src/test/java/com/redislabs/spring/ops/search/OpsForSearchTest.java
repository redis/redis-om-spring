package com.redislabs.spring.ops.search;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.redislabs.spring.ops.RedisModulesOperations;

import io.redisearch.Query;
import io.redisearch.Schema;
import io.redisearch.SearchResult;
import io.redisearch.client.Client;
import io.redisearch.client.IndexDefinition;
import redis.clients.jedis.exceptions.JedisDataException;

@SpringBootTest(classes = OpsForSearchTest.Config.class)
public class OpsForSearchTest {
  public static String searchIndex = "student_pupil";

  @Autowired
  RedisModulesOperations<String, String> modulesOperations;

  @Test
  public void testBasicSearch() {
    SearchOperations<String> ops = modulesOperations.opsForSearch(searchIndex);
    SearchResult res1 = ops.search(new Query("@first:Jo*"));
    assertEquals(2, res1.totalResults);

    SearchResult res2 = ops.search(new Query("@first:Pat"));
    assertEquals(1, res2.totalResults);
  }

  @SpringBootApplication
  @Configuration
  static class Config {
    static String HASH_PREFIX = "Article";
    
    @Autowired
    RedisModulesOperations<String, String> modulesOperations;
    
    @Autowired
    RedisTemplate<String, String> template;

    @Bean
    CommandLineRunner loadTestData(RedisTemplate<String, String> template) {
      return args -> {
        System.out.println(">>> loadTestData...");
        HashOperations<String, String, String> hashOps = template.opsForHash();
        hashOps.putAll("profesor:5555", toMap("first", "Albert", "last", "Blue", "age", "55"));
        hashOps.putAll("student:1111", toMap("first", "Joe", "last", "Dod", "age", "18"));
        hashOps.putAll("pupil:2222", toMap("first", "Jen", "last", "Rod", "age", "14"));
        hashOps.putAll("student:3333", toMap("first", "El", "last", "Mark", "age", "17"));
        hashOps.putAll("pupil:4444", toMap("first", "Pat", "last", "Shu", "age", "21"));
        hashOps.putAll("student:5555", toMap("first", "Joen", "last", "Ko", "age", "20"));
        hashOps.putAll("teacher:6666", toMap("first", "Pat", "last", "Rod", "age", "20"));
      };
    }

    @Bean
    CommandLineRunner createSearchIndices(RedisModulesOperations<String, String> modulesOperations) {
      return args -> {
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

    private static Map<String, String> toMap(String... values) {
      Map<String, String> map = new HashMap<>();
      for (int i = 0; i < values.length; i += 2) {
        map.put(values[i], values[i + 1]);
      }
      return map;
    }
  }
}
