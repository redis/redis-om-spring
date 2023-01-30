package com.redis.om.spring.repository.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryUtilsTest {
  @Test
  void testEscapeTagField() {
    assertEquals("roger\\.green\\@example\\.com", QueryUtils.escape("roger.green@example.com"));
  }
}
