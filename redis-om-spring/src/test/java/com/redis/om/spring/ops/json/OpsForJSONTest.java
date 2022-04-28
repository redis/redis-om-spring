package com.redis.om.spring.ops.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redislabs.modules.rejson.Path;

import redis.clients.jedis.exceptions.JedisDataException;

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
  RedisModulesOperations<String> modulesOperations;

  @Test
  public void testJSONClient() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();

    IRLObject obj = new IRLObject();
    ops.set("obj", obj);

    assertEquals(obj, ops.get("obj", IRLObject.class));
  }

  @Test
  public void testSetWithPath() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();

    IRLObject obj = new IRLObject();
    ops.set("obj", obj);

    IRLObject obj1 = ops.get("obj", IRLObject.class);

    assertEquals("string", obj1.str);

    ops.set("obj", "String", Path.of("$.str"));
    IRLObject obj2 = ops.get("obj", IRLObject.class);

    assertEquals("String", obj2.str);
  }

  @Test
  public void testToggle() {

    JSONOperations<String> ops = modulesOperations.opsForJSON();

    IRLObject obj = new IRLObject();
    ops.set("obj", obj);

    Path pbool = Path.of(".bTrue");
    // check initial value
    assertTrue(ops.get("obj", Boolean.class, pbool));

    // true -> false
    ops.toggle("obj", pbool);
    assertFalse(ops.get("obj", Boolean.class, pbool));

    // false -> true
    ops.toggle("obj", pbool);
    assertTrue(ops.get("obj", Boolean.class, pbool));

    // ignore non-boolean field
    Path pstr = Path.of(".str");
    try {
      ops.toggle("obj", pstr);
      fail("Path not a bool");
    } catch (JedisDataException jde) {
      assertTrue(jde.getMessage().contains("not a bool"));
    }
    assertEquals("string", ops.get("obj", String.class, pstr));
  }
  
  @Test
  public void testStringLen() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    ops.set( "str", "foo", Path.ROOT_PATH);
    assertEquals(Long.valueOf(3L), ops.strLen( "str", Path.ROOT_PATH));
  }

}
