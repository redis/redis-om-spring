package com.redis.om.spring.client;

import com.google.gson.Gson;
import com.redis.om.spring.AbstractBaseDocumentTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.bloom.commands.BloomFilterCommands;
import redis.clients.jedis.json.RedisJsonCommands;
import redis.clients.jedis.search.RediSearchCommands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RedisModulesClientTest extends AbstractBaseDocumentTest {

  @Autowired
  Gson gson;

  /* A simple class that represents an object in real life */
  @SuppressWarnings("unused")
  private static class IRLObject {
    public final String str;
    public final boolean bTrue;

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
    jsonClient.jsonSetLegacy("obj", obj);
    Object expected = gson.fromJson(gson.toJson(obj), Object.class);
    assertEquals(expected, jsonClient.jsonGet("obj"));
  }

  @Test
  void testSearchClient() {
    RediSearchCommands searchClient = client.clientForSearch();
    assertNotNull(searchClient);
  }

  @Test
  void testBloomClient() {
    BloomFilterCommands bloomClient = client.clientForBloom();
    assertNotNull(bloomClient);
  }

}
