package com.redislabs.spring.ops.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.annotation.PreDestroy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.redislabs.spring.ops.RedisModulesOperations;

@SpringBootTest(classes = OpsForJSONTest.Config.class)
public class OpsForJSONTest {

  /* A simple class that represents an object in real life */
  private static class IRLObject {
    public String str;
    public boolean bTrue;

    public IRLObject() {
      this.str = "string";
      this.bTrue = true;
    }
    
    @Override
      public boolean equals(Object other) {
        IRLObject o = (IRLObject)other;
        return this.str.equals(o.str) && this.bTrue == o.bTrue;
      }
  }

  @Autowired
  RedisModulesOperations<String, IRLObject> modulesOperations;

  @Test
  public void testJSONClient() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();

    IRLObject obj = new IRLObject();
    ops.set("obj", obj);

    assertEquals(obj, ops.get("obj", IRLObject.class));
  }

  @SpringBootApplication
  @Configuration
  static class Config {
    @Autowired
    RedisConnectionFactory connectionFactory;
    
    @PreDestroy
    void cleanUp() {
      connectionFactory.getConnection().flushAll();
    }
  }
}
