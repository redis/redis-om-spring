package com.redis.spring.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.redis.spring.AbstractBaseTest;
import com.redislabs.modules.rejson.JReJSON;
import com.redislabs.redisai.RedisAI;
import com.redislabs.redisgraph.impl.api.RedisGraph;
import com.redislabs.redistimeseries.RedisTimeSeries;

import io.redisearch.Client;

public class RedisModulesClientTest extends AbstractBaseTest {

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
  public void testJSONClient() {
    JReJSON jsonClient = client.clientForJSON();

    IRLObject obj = new IRLObject();
    jsonClient.set("obj", obj);
    Object expected = g.fromJson(g.toJson(obj), Object.class);
    assertEquals(expected, jsonClient.get("obj"));
  }

  @Test
  public void testAIClient() {
    RedisAI aiClient = client.clientForAI();
    assertNotNull(aiClient);
  }

  @Test
  public void testGraphClient() {
    RedisGraph graphClient = client.clientForGraph();
    assertNotNull(graphClient);
  }

  @Test
  public void testSearchClient() {
    Client searchClient = client.clientForSearch("index");
    assertNotNull(searchClient);
  }

  @Test
  public void testBloomClient() {
    io.rebloom.client.Client bloomClient = client.clientForBloom();
    assertNotNull(bloomClient);
  }

  @Test
  public void testTimeSeriesClient() {
    RedisTimeSeries timeSeriesClient = client.clientForTimeSeries();
    assertNotNull(timeSeriesClient);
  }
}
