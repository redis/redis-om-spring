package com.redis.om.spring.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redislabs.modules.rejson.JReJSON;

import io.redisearch.Client;

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
    JReJSON jsonClient = client.clientForJSON();

    IRLObject obj = new IRLObject();
    jsonClient.set("obj", obj);
    Object expected = g.fromJson(g.toJson(obj), Object.class);
    assertEquals(expected, jsonClient.get("obj"));
  }

  @Test
  void testSearchClient() {
    Client searchClient = client.clientForSearch("index");
    assertNotNull(searchClient);
  }

  @Test
  void testBloomClient() {
    io.rebloom.client.Client bloomClient = client.clientForBloom();
    assertNotNull(bloomClient);
  }

}
