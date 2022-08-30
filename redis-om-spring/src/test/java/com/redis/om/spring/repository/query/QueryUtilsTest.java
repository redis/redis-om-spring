package com.redis.om.spring.repository.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class QueryUtilsTest {
  @Test
  void testEscapeTagField() {
    assertEquals("roger\\.green\\@example\\.com", QueryUtils.escape("roger.green@example.com"));
  }
}
