package com.redis.om.spring.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.redis.om.spring.AbstractBaseDocumentTest;

import redis.clients.jedis.bloom.RedisBloomCommands;
import redis.clients.jedis.json.RedisJsonCommands;
import redis.clients.jedis.search.RediSearchCommands;

class RedisModulesClientTest extends AbstractBaseDocumentTest {

  private final Gson g = new Gson();

  /* A simple class that represents an object in real life */
  @SuppressWarnings("unused")
  private static class IRLObject {
    public String str;
    public boolean bTrue;

    public IRLObject() {
      this.str = "string";
      this.bTrue = true;
    }
  }

  @Autowired
  RedisModulesClient client;

  @Test
  void testJSONClient() {
    RedisJsonCommands jsonClient = client.clientForJSON();

    IRLObject obj = new IRLObject();
    jsonClient.jsonSet("obj", obj);
    Object expected = g.fromJson(g.toJson(obj), Object.class);
    assertEquals(expected, jsonClient.jsonGet("obj"));
  }

  @Test
  void testSearchClient() {
    RediSearchCommands searchClient = client.clientForSearch();
    assertNotNull(searchClient);
  }

  @Test
  void testBloomClient() {
    RedisBloomCommands bloomClient = client.clientForBloom();
    assertNotNull(bloomClient);
  }

}
