package com.redis.om.spring.repository.query.clause;

import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.repository.query.parser.Part;
import redis.clients.jedis.search.Schema.FieldType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

  @Test
  void testPrepareQuery(){
    // Arrange
    List<Object> tagContainingAll = new ArrayList<>(Arrays.asList("db1", "db2"));
    List<Object> numericContaining = new ArrayList<>(Arrays.asList(1,2,3));
    List<Object> numericContainingAll = new ArrayList<>(Arrays.asList(1,2,3));
    List<Object> geoContaining = new ArrayList<>(Arrays.asList(new Point(-122.066540, 37.377690), new Point(122.066540, -37.377690)));
    List<Object> geoContainingAll = new ArrayList<>(Arrays.asList(new Point(-122.066540, 37.377690), new Point(122.066540, -37.377690)));
    List<Object> numericGreaterThan = new ArrayList<>(Arrays.asList(5,10));

    // Act
    String tagContainingAllQuery = QueryClause.TAG_CONTAINING_ALL.prepareQuery("database", tagContainingAll);
    String numericContainingQuery = QueryClause.NUMERIC_CONTAINING.prepareQuery("number", numericContaining);
    String numericContainingAllQuery = QueryClause.NUMERIC_CONTAINING_ALL.prepareQuery("number", numericContainingAll);
    String geoContainingQuery = QueryClause.GEO_CONTAINING.prepareQuery("location", geoContaining);
    String geoContainigAllQuery = QueryClause.GEO_CONTAINING_ALL.prepareQuery("location", geoContainingAll);
    String numericGreaterThanQuery = QueryClause.NUMERIC_GREATER_THAN.prepareQuery("number", numericGreaterThan);

    // Assert
    assertEquals("@database:{db1} @database:{db2}", tagContainingAllQuery);
    assertEquals("@number:[1 1]|@number:[2 2]|@number:[3 3]", numericContainingQuery);
    assertEquals("@number:[1 1] @number:[2 2] @number:[3 3]", numericContainingAllQuery);
    assertEquals("@location:[-122.06654 37.37769 .000001 ft]|@location:[122.06654 -37.37769 .000001 ft]", geoContainingQuery);
    assertEquals("@location:[-122.06654 37.37769 .000001 ft] @location:[122.06654 -37.37769 .000001 ft]", geoContainigAllQuery);
    assertEquals("@number:[(5|10 inf]", numericGreaterThanQuery);
  }

  @Test
  void testGetPostProcessMethodName(){
    String methodName1 = "SomeOtherMethodName";
    assertEquals(methodName1, QueryClause.getPostProcessMethodName(methodName1));
  }
}
