package com.redis.om.spring.repository.query.clause;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.repository.query.parser.Part;

import io.redisearch.Schema.FieldType;

class QueryClauseTest {

  @Test
  void testItShouldFindTheClauseByFieldTypeandPartType() {
    assertEquals(QueryClause.get(FieldType.FullText, Part.Type.SIMPLE_PROPERTY), QueryClause.FullText_SIMPLE_PROPERTY);
    assertEquals(QueryClause.get(FieldType.Numeric, Part.Type.SIMPLE_PROPERTY), QueryClause.Numeric_SIMPLE_PROPERTY);
    assertEquals(QueryClause.get(FieldType.Numeric, Part.Type.BETWEEN), QueryClause.Numeric_BETWEEN);
    assertEquals(QueryClause.get(FieldType.Geo, Part.Type.NEAR), QueryClause.Geo_NEAR);
    assertEquals(QueryClause.get(FieldType.Tag, Part.Type.SIMPLE_PROPERTY), QueryClause.Tag_SIMPLE_PROPERTY);
  }

  @Test
  void testRenderFullTextEqualsQuery() {
    String querySegment = QueryClause.FullText_SIMPLE_PROPERTY.prepareQuery("name", "Bumfuzzle");
    assertEquals("@name:Bumfuzzle", querySegment);
  }
  
  @Test
  void testRenderNumericEqualsQuery() {
    String querySegment = QueryClause.Numeric_SIMPLE_PROPERTY.prepareQuery("yearEstablished", 1975);
    assertEquals("@yearEstablished:[1975 1975]", querySegment);
  }
  
  @Test
  void testRenderNumericBetweenQuery() {
    String querySegment = QueryClause.Numeric_BETWEEN.prepareQuery("yearEstablished", 1968, 1975);
    assertEquals("@yearEstablished:[1968 1975]", querySegment);
  }
  
  @Test
  void testRenderGeoNearQuery() {
    Point point = new Point(-122.066540, 37.377690); 
    Distance distance = new Distance(45, Metrics.MILES);
    String querySegment = QueryClause.Geo_NEAR.prepareQuery("location", point, distance);
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
