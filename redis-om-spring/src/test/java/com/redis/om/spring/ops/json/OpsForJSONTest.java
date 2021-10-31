package com.redis.om.spring.ops.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.RedisModulesOperations;

public class OpsForJSONTest extends AbstractBaseDocumentTest {

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
      IRLObject o = (IRLObject) other;
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
}
