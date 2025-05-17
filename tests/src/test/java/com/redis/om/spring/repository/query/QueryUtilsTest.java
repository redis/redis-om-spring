package com.redis.om.spring.repository.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class QueryUtilsTest {
  @Test
  void testEscapeTagField() {
    assertEquals("roger\\.green\\@example\\.com", QueryUtils.escape("roger.green@example.com"));
  }

  @Test
  void testSearchIndexFieldAlliasFor() {
    // Arrange
    Field field = Mockito.mock(Field.class);
    Mockito.when(field.getName()).thenReturn("redis");
    String prefix = "roger.green";

    // Act
    String outputWhenPrefixNull = QueryUtils.searchIndexFieldAliasFor(field, null);
    String outputWhenPrefixBlank = QueryUtils.searchIndexFieldAliasFor(field, "");
    String outputWhenPrefixValid = QueryUtils.searchIndexFieldAliasFor(field, prefix);

    // Assert
    assertEquals(field.getName(), outputWhenPrefixNull);
    assertEquals(field.getName(), outputWhenPrefixBlank);
    assertEquals("roger_green_redis", outputWhenPrefixValid);
  }

  @Test
  void testEscapeWithNullValue() {
    assertNull(QueryUtils.escape(null));
  }

  @Test
  void testEscapeWithBlankValue() {
    assertEquals("", QueryUtils.escape(""));
  }
}
