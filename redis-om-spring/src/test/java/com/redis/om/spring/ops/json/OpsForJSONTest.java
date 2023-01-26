package com.redis.om.spring.ops.json;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.RedisModulesOperations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.json.JsonSetParams;
import redis.clients.jedis.json.Path;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({ "unused", "SpellCheckingInspection" })
class OpsForJSONTest extends AbstractBaseDocumentTest {

  /* A simple class that represents an object in real life */
  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  private static class IRLObject {
    public final String str;
    public final boolean bTrue;

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
    public final String foo;
    public final boolean fooB;
    public final int fooI;
    public final float fooF;
    public final String[] fooArr;

    public FooBarObject() {
      this.foo = "bar";
      this.fooB = true;
      this.fooI = 6574;
      this.fooF = 435.345f;
      this.fooArr = new String[] { "a", "b", "c" };
    }
  }

  @SuppressWarnings({ "SpellCheckingInspection", "FieldMayBeFinal" })
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

  @SuppressWarnings({ "SpellCheckingInspection", "FieldMayBeFinal" })
  private static class Qux {
    private String quux;
    private String corge;
    private String garply;
    private Baz baz;

    public Qux(String quux, String corge, String garply, Baz baz) {
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
    assertEquals("strong", ops.get("str", String.class));

    // a slightly more complex object
    IRLObject obj = new IRLObject();
    ops.set("obj", obj);
    assertEquals(obj, ops.get("obj", IRLObject.class));

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

    assertEquals("string", Objects.requireNonNull(obj1).str);

    ops.set("obj", "String", Path.of("$.str"));
    IRLObject obj2 = ops.get("obj", IRLObject.class);

    assertEquals("String", Objects.requireNonNull(obj2).str);
  }

  @Test
  void setExistingPathOnlyIfExistsShouldSucceed() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    ops.set("obj", new IRLObject());
    Path p = Path.of(".str");
    ops.set("obj", "strangle", JsonSetParams.jsonSetParams().xx(), p);
    assertEquals("strangle", ops.get("obj", String.class, p));
  }

  @Test
  void setWithoutAPathDefaultsToRootPath() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();
    ops.set("obj1", new IRLObject());
    ops.set("obj1", "strangle");
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
    assertEquals(Boolean.TRUE, ops.get("obj", Boolean.class, pbool));

    // true -> false
    ops.toggle("obj", pbool);
    assertNotEquals(Boolean.TRUE, ops.get("obj", Boolean.class, pbool));

    // false -> true
    ops.toggle("obj", pbool);
    assertEquals(Boolean.TRUE, ops.get("obj", Boolean.class, pbool));

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
    assertEquals(Long.valueOf(4L), ops.arrPop("arr", Long.class, Path.ROOT_PATH, 4));
    assertEquals(Long.valueOf(3L), ops.arrPop("arr", Long.class, Path.ROOT_PATH, -1));
    assertEquals(Long.valueOf(2L), ops.arrPop("arr", Long.class, Path.ROOT_PATH));
    assertEquals(Long.valueOf(0L), ops.arrPop("arr", Long.class, Path.ROOT_PATH, 0));
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
    assertNull(ops.type("foobar", Path.of(".fooErr")));
  }

}
