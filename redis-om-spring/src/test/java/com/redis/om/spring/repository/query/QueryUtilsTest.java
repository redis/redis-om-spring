package com.redis.om.spring.repository.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class QueryUtilsTest {
  @Test
  void testEscapeTagField() {
    assertEquals("roger\\.green\\@example\\.com", QueryUtils.escapeTagField("roger.green@example.com"));
  }
}
