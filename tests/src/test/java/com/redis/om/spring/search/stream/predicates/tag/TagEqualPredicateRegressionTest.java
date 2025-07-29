package com.redis.om.spring.search.stream.predicates.tag;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify the fix for issue #609
 * https://github.com/redis/redis-om-spring/issues/609
 * 
 * In version 0.9.10, the EqualPredicate for tag fields started incorrectly
 * escaping quotes, causing syntax errors in Redis queries.
 * This test verifies the fix is working correctly.
 */
class TagEqualPredicateRegressionTest {
  
  @Test
  void testTagEqualPredicateWithSimpleValue() throws NoSuchFieldException {
    // Given a field and a simple alphanumeric value
    Field mockField = String.class.getDeclaredField("value");
    SearchFieldAccessor fieldAccessor = new SearchFieldAccessor("type", "$.type", mockField);
    String enumValue = "REGISTRATION";
    
    // When creating an EqualPredicate
    EqualPredicate<Object, String> predicate = new EqualPredicate<>(fieldAccessor, enumValue);
    
    // And applying it to a query
    Node root = QueryBuilders.intersect();
    Node result = predicate.apply(root);
    
    // Then the generated query should have the correct syntax without quotes
    String queryString = result.toString();
    
    // Simple values should not have quotes
    assertThat(queryString).contains("@type:{REGISTRATION}");
    assertThat(queryString).doesNotContain("@type:{\"REGISTRATION\"}");
  }
  
  @Test
  void testTagEqualPredicateWithSpecialCharacters() throws NoSuchFieldException {
    // Given a field and value with special characters (spaces)
    Field mockField = String.class.getDeclaredField("value");
    SearchFieldAccessor fieldAccessor = new SearchFieldAccessor("status", "$.status", mockField);
    String valueWithSpaces = "IN PROGRESS";
    
    // When creating an EqualPredicate
    EqualPredicate<Object, String> predicate = new EqualPredicate<>(fieldAccessor, valueWithSpaces);
    
    // And applying it to a query
    Node root = QueryBuilders.intersect();
    Node result = predicate.apply(root);
    
    // Then the generated query should use quotes for values with special characters
    String queryString = result.toString();
    
    // Values with special characters should be quoted
    assertThat(queryString).contains("@status:{\"IN PROGRESS\"}");
  }
  
  @Test
  void testTagEqualPredicateWithUUID() throws NoSuchFieldException {
    // Given a field and a UUID value (contains hyphens)
    Field mockField = String.class.getDeclaredField("value");
    SearchFieldAccessor fieldAccessor = new SearchFieldAccessor("uuid", "$.uuid", mockField);
    String uuidValue = "123e4567-e89b-12d3-a456-426614174000";
    
    // When creating an EqualPredicate
    EqualPredicate<Object, String> predicate = new EqualPredicate<>(fieldAccessor, uuidValue);
    
    // And applying it to a query
    Node root = QueryBuilders.intersect();
    Node result = predicate.apply(root);
    
    // Then the generated query should use quotes because UUIDs contain hyphens
    String queryString = result.toString();
    
    // UUIDs with hyphens need quotes
    assertThat(queryString).contains("@uuid:{\"123e4567-e89b-12d3-a456-426614174000\"}");
  }
}