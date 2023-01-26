package com.redis.om.spring.repository.query.clause;

import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.repository.query.parser.Part;
import redis.clients.jedis.search.Schema.FieldType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpellCheckingInspection") class QueryClauseTest {

  @Test
  void testItShouldFindTheClauseByFieldTypeandPartType() {
    assertEquals(QueryClause.TEXT_SIMPLE_PROPERTY, QueryClause.get(FieldType.TEXT, Part.Type.SIMPLE_PROPERTY));
    assertEquals(QueryClause.NUMERIC_SIMPLE_PROPERTY, QueryClause.get(FieldType.NUMERIC, Part.Type.SIMPLE_PROPERTY));
    assertEquals(QueryClause.NUMERIC_BETWEEN, QueryClause.get(FieldType.NUMERIC, Part.Type.BETWEEN));
    assertEquals(QueryClause.GEO_NEAR, QueryClause.get(FieldType.GEO, Part.Type.NEAR));
    assertEquals(QueryClause.TAG_SIMPLE_PROPERTY, QueryClause.get(FieldType.TAG, Part.Type.SIMPLE_PROPERTY));
  }

  @Test
  void testRenderFullTextEqualsQuery() {
    String querySegment = QueryClause.TEXT_SIMPLE_PROPERTY.prepareQuery("name", "Bumfuzzle");
    assertEquals("@name:Bumfuzzle", querySegment);
  }

  @Test
  void testRenderNumericEqualsQuery() {
    String querySegment = QueryClause.NUMERIC_SIMPLE_PROPERTY.prepareQuery("yearEstablished", 1975);
    assertEquals("@yearEstablished:[1975 1975]", querySegment);
  }

  @Test
  void testRenderNumericBetweenQuery() {
    String querySegment = QueryClause.NUMERIC_BETWEEN.prepareQuery("yearEstablished", 1968, 1975);
    assertEquals("@yearEstablished:[1968 1975]", querySegment);
  }

  @Test
  void testRenderGeoNearQuery() {
    Point point = new Point(-122.066540, 37.377690);
    Distance distance = new Distance(45, Metrics.MILES);
    String querySegment = QueryClause.GEO_NEAR.prepareQuery("location", point, distance);
    assertEquals("@location:[-122.06654 37.37769 45.0 mi]", querySegment);
  }

  @Test
  void testHasContainingAllClause() {
    assertTrue(QueryClause.hasContainingAllClause("findByWorkTypeContainingAll"));
    assertTrue(QueryClause.hasContainingAllClause("findByWorkTypeContainsAll"));
    assertTrue(QueryClause.hasContainingAllClause("findByWorkTypeIsContainingAll"));
  }

  @Test
  void testGetNonCustomMethodName() {
    assertEquals("findByWorkTypeContaining", QueryClause.getPostProcessMethodName("findByWorkTypeContainingAll"));
    assertEquals("findByWorkTypeContaining", QueryClause.getPostProcessMethodName("findByWorkTypeContainingAll"));
    assertEquals("findByWorkTypeIsContaining", QueryClause.getPostProcessMethodName("findByWorkTypeIsContainingAll"));
  }

}
