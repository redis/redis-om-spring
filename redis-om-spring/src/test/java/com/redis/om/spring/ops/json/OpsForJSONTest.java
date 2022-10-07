package com.redis.om.spring.ops.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redislabs.modules.rejson.JReJSON.ExistenceModifier;
import com.redislabs.modules.rejson.Path;

import redis.clients.jedis.exceptions.JedisDataException;

class OpsForJSONTest extends AbstractBaseDocumentTest {
  
  @Autowired
  Gson gson;

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

  @SuppressWarnings("unused")
  private static class FooBarObject {
    public String foo;
    public boolean fooB;
    public int fooI;
    public float fooF;
    public String[] fooArr;

    public FooBarObject() {
      this.foo = "bar";
      this.fooB = true;
      this.fooI = 6574;
      this.fooF = 435.345f;
      this.fooArr = new String[] { "a", "b", "c" };
    }
  }

  private static class Baz {
    private String quuz;
    private String grault;
    private String waldo;

    public Baz(final String quuz, final String grault, final String waldo) {
      this.quuz = quuz;
      this.grault = grault;
      this.waldo = waldo;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null)
        return false;
      if (getClass() != o.getClass())
        return false;
      Baz other = (Baz) o;

      return Objects.equals(quuz, other.quuz) && //
          Objects.equals(grault, other.grault) && //
          Objects.equals(waldo, other.waldo);
    }
  }

  private static class Qux {
    private String quux;
    private String corge;
    private String garply;
    private Baz baz;

    public Qux(final String quux, final String corge, final String garply, final Baz baz) {
      this.quux = quux;
      this.corge = corge;
      this.garply = garply;
      this.baz = baz;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null)
        return false;
      if (getClass() != o.getClass())
        return false;
      Qux other = (Qux) o;

      return Objects.equals(quux, other.quux) && //
          Objects.equals(corge, other.corge) && //
          Objects.equals(garply, other.garply) && //
          Objects.equals(baz, other.baz);
    }
  }

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @Test
  void testJSONClient() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();

    IRLObject obj = new IRLObject();
    ops.set("obj", obj);

    assertEquals(obj, ops.get("obj", IRLObject.class));
  }

  @Test
  void basicSetGetShouldSucceed() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    // naive set with a path
    ops.set("null", null, Path.ROOT_PATH);
    assertNull(ops.get("null", String.class, Path.ROOT_PATH));

    // real scalar value and no path
    ops.set("str", "strong");
    assertEquals("strong", ops.get("str"));

    // a slightly more complex object
    IRLObject obj = new IRLObject();
    ops.set("obj", obj);
    Object expected = gson.fromJson(gson.toJson(obj), Object.class);
    assertTrue(expected.equals(ops.get("obj")));

    // check an update
    Path p = Path.of(".str");
    ops.set("obj", "strung", p);
    assertEquals("strung", ops.get("obj", String.class, p));
  }

  @Test
  void testSetWithPath() {
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
  void setExistingPathOnlyIfExistsShouldSucceed() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    ops.set("obj", new IRLObject());
    Path p = Path.of(".str");
    ops.set("obj", "strangle", ExistenceModifier.MUST_EXIST, p);
    assertEquals("strangle", ops.get("obj", String.class, p));
  }

  @Test
  void setWithoutAPathDefaultsToRootPath() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    ops.set("obj1", new IRLObject());
    ops.set("obj1", "strangle", ExistenceModifier.MUST_EXIST);
    assertEquals("strangle", ops.get("obj1", String.class, Path.ROOT_PATH));
  }

  @Test
  void testMultipleGetAtRootPathAllKeysExist() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    Baz baz1 = new Baz("quuz1", "grault1", "waldo1");
    Baz baz2 = new Baz("quuz2", "grault2", "waldo2");
    Qux qux1 = new Qux("quux1", "corge1", "garply1", baz1);
    Qux qux2 = new Qux("quux2", "corge2", "garply2", baz2);

    ops.set("qux1", qux1);
    ops.set("qux2", qux2);

    List<Qux> oneQux = ops.mget(Qux.class, "qux1");
    List<Qux> allQux = ops.mget(Qux.class, "qux1", "qux2");

    assertEquals(1, oneQux.size());
    assertEquals(2, allQux.size());

    assertEquals(qux1, oneQux.get(0));

    Qux testQux1 = allQux.stream() //
        .filter(q -> q.quux.equals("quux1")) //
        .findFirst() //
        .orElseThrow(() -> new NullPointerException(""));
    Qux testQux2 = allQux.stream() //
        .filter(q -> q.quux.equals("quux2")) //
        .findFirst() //
        .orElseThrow(() -> new NullPointerException(""));

    assertEquals(qux1, testQux1);
    assertEquals(qux2, testQux2);
  }

  @Test
  void testToggle() {

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
  void testStringLen() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    ops.set("str", "foo", Path.ROOT_PATH);
    assertEquals(Long.valueOf(3L), ops.strLen("str", Path.ROOT_PATH));
  }

  @Test
  void testArrayPop() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    ops.set("arr", new int[] { 0, 1, 2, 3, 4 }, Path.ROOT_PATH);
    assertEquals(Long.valueOf(4L), ops.arrPop("arr", Long.class, Path.ROOT_PATH, 4L));
    assertEquals(Long.valueOf(3L), ops.arrPop("arr", Long.class, Path.ROOT_PATH, -1L));
    assertEquals(Long.valueOf(2L), ops.arrPop("arr", Long.class, Path.ROOT_PATH));
    assertEquals(Long.valueOf(0L), ops.arrPop("arr", Long.class, Path.ROOT_PATH, 0L));
    assertEquals(Long.valueOf(1L), ops.arrPop("arr", Long.class));
  }

  @Test
  void typeChecksShouldSucceed() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    ops.set("foobar", new FooBarObject(), Path.ROOT_PATH);
    assertSame(Object.class, ops.type("foobar"));
    assertSame(Object.class, ops.type("foobar", Path.ROOT_PATH));
    assertSame(String.class, ops.type("foobar", Path.of(".foo")));
    assertSame(int.class, ops.type("foobar", Path.of(".fooI")));
    assertSame(float.class, ops.type("foobar", Path.of(".fooF")));
    assertSame(List.class, ops.type("foobar", Path.of(".fooArr")));
    assertSame(boolean.class, ops.type("foobar", Path.of(".fooB")));

    try {
      ops.type("foobar", Path.of(".fooErr"));
      fail();
    } catch (Exception e) {}
  }

}
