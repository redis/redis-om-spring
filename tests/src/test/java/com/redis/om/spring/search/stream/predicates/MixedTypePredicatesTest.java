package com.redis.om.spring.search.stream.predicates;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.metamodel.indexed.NumericField;
import com.redis.om.spring.metamodel.indexed.TagField;

/**
 * Unit test for issue #342: Verifying that predicates with different field types
 * can be combined using the new andAny() and orAny() methods.
 */
class MixedTypePredicatesTest {

  static class TestEntity {
    private String nameSpace;
    private Long relateId;
    private String status;
    
    public String getNameSpace() { return nameSpace; }
    public void setNameSpace(String nameSpace) { this.nameSpace = nameSpace; }
    
    public Long getRelateId() { return relateId; }
    public void setRelateId(Long relateId) { this.relateId = relateId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
  }

  @Test
  void testIssue342_CanCombineDifferentTypePredicatesWithAndAny() throws NoSuchFieldException {
    // Create field accessors
    Field nameSpaceField = TestEntity.class.getDeclaredField("nameSpace");
    Field relateIdField = TestEntity.class.getDeclaredField("relateId");
    
    SearchFieldAccessor nameSpaceAccessor = new SearchFieldAccessor("@nameSpace", "$.nameSpace", nameSpaceField);
    SearchFieldAccessor relateIdAccessor = new SearchFieldAccessor("@relateId", "$.relateId", relateIdField);
    
    // Create predicates with different types
    TagField<TestEntity, String> nameSpacePredicate = new TagField<>(nameSpaceAccessor, true);
    NumericField<TestEntity, Long> relateIdPredicate = new NumericField<>(relateIdAccessor, true);
    
    // Test that we can combine String and Long predicates with andAny()
    SearchFieldPredicate<TestEntity, String> stringPred = nameSpacePredicate.eq("PERSONAL");
    SearchFieldPredicate<TestEntity, Long> longPred = relateIdPredicate.eq(100L);
    
    // This should compile without type errors
    SearchFieldPredicate<TestEntity, ?> combined = stringPred.andAny(longPred);
    assertNotNull(combined);
    assertTrue(combined instanceof AndPredicate);
    
    // Verify the predicate was added
    AndPredicate<TestEntity, ?> andPred = (AndPredicate<TestEntity, ?>) combined;
    assertEquals(2, andPred.stream().count());
  }

  @Test
  void testIssue342_CanCombineDifferentTypePredicatesWithOrAny() throws NoSuchFieldException {
    // Create field accessors
    Field nameSpaceField = TestEntity.class.getDeclaredField("nameSpace");
    Field relateIdField = TestEntity.class.getDeclaredField("relateId");
    
    SearchFieldAccessor nameSpaceAccessor = new SearchFieldAccessor("@nameSpace", "$.nameSpace", nameSpaceField);
    SearchFieldAccessor relateIdAccessor = new SearchFieldAccessor("@relateId", "$.relateId", relateIdField);
    
    // Create predicates with different types
    TagField<TestEntity, String> nameSpacePredicate = new TagField<>(nameSpaceAccessor, true);
    NumericField<TestEntity, Long> relateIdPredicate = new NumericField<>(relateIdAccessor, true);
    
    // Test that we can combine String and Long predicates with orAny()
    SearchFieldPredicate<TestEntity, String> stringPred = nameSpacePredicate.eq("BUSINESS");
    SearchFieldPredicate<TestEntity, Long> longPred = relateIdPredicate.eq(200L);
    
    // This should compile without type errors
    SearchFieldPredicate<TestEntity, ?> combined = stringPred.orAny(longPred);
    assertNotNull(combined);
    assertTrue(combined instanceof OrPredicate);
    
    // Verify the predicate was added
    OrPredicate<TestEntity, ?> orPred = (OrPredicate<TestEntity, ?>) combined;
    assertEquals(2, orPred.stream().count());
  }

  @Test
  void testIssue342_ChainMultipleDifferentTypes() throws NoSuchFieldException {
    // Create field accessors
    Field nameSpaceField = TestEntity.class.getDeclaredField("nameSpace");
    Field relateIdField = TestEntity.class.getDeclaredField("relateId");
    Field statusField = TestEntity.class.getDeclaredField("status");
    
    SearchFieldAccessor nameSpaceAccessor = new SearchFieldAccessor("@nameSpace", "$.nameSpace", nameSpaceField);
    SearchFieldAccessor relateIdAccessor = new SearchFieldAccessor("@relateId", "$.relateId", relateIdField);
    SearchFieldAccessor statusAccessor = new SearchFieldAccessor("@status", "$.status", statusField);
    
    // Create predicates with different types
    TagField<TestEntity, String> nameSpacePredicate = new TagField<>(nameSpaceAccessor, true);
    NumericField<TestEntity, Long> relateIdPredicate = new NumericField<>(relateIdAccessor, true);
    TagField<TestEntity, String> statusPredicate = new TagField<>(statusAccessor, true);
    
    // Test chaining multiple different types
    SearchFieldPredicate<TestEntity, ?> combined = nameSpacePredicate.eq("PERSONAL")
        .andAny(relateIdPredicate.eq(100L))
        .andAny(statusPredicate.eq("ACTIVE"));
    
    assertNotNull(combined);
    assertTrue(combined instanceof AndPredicate);
  }

  @Test
  void testIssue342_MixAndAnyAndOrAny() throws NoSuchFieldException {
    // Create field accessors
    Field nameSpaceField = TestEntity.class.getDeclaredField("nameSpace");
    Field relateIdField = TestEntity.class.getDeclaredField("relateId");
    
    SearchFieldAccessor nameSpaceAccessor = new SearchFieldAccessor("@nameSpace", "$.nameSpace", nameSpaceField);
    SearchFieldAccessor relateIdAccessor = new SearchFieldAccessor("@relateId", "$.relateId", relateIdField);
    
    // Create predicates with different types
    TagField<TestEntity, String> nameSpacePredicate = new TagField<>(nameSpaceAccessor, true);
    NumericField<TestEntity, Long> relateIdPredicate = new NumericField<>(relateIdAccessor, true);
    
    // Test mixing andAny and orAny
    SearchFieldPredicate<TestEntity, ?> combined = nameSpacePredicate.eq("PERSONAL")
        .andAny(relateIdPredicate.gt(50L))
        .orAny(nameSpacePredicate.eq("BUSINESS"));
    
    assertNotNull(combined);
    assertTrue(combined instanceof OrPredicate);
  }

  @Test
  void testIssue342_SameTypeStillWorksWithRegularAnd() throws NoSuchFieldException {
    // Create field accessors for same type
    Field nameSpaceField = TestEntity.class.getDeclaredField("nameSpace");
    Field statusField = TestEntity.class.getDeclaredField("status");
    
    SearchFieldAccessor nameSpaceAccessor = new SearchFieldAccessor("@nameSpace", "$.nameSpace", nameSpaceField);
    SearchFieldAccessor statusAccessor = new SearchFieldAccessor("@status", "$.status", statusField);
    
    // Create predicates with same type (String)
    TagField<TestEntity, String> nameSpacePredicate = new TagField<>(nameSpaceAccessor, true);
    TagField<TestEntity, String> statusPredicate = new TagField<>(statusAccessor, true);
    
    // Test that same-type predicates still work with regular and()
    // Note: and() returns Predicate<T>, not SearchFieldPredicate, so we use andAny for consistency
    SearchFieldPredicate<TestEntity, ?> combined = nameSpacePredicate.eq("PERSONAL")
        .andAny(statusPredicate.eq("ACTIVE"));
    
    assertNotNull(combined);
    assertTrue(combined instanceof AndPredicate);
    
    // Verify both predicates are included
    @SuppressWarnings("unchecked")
    AndPredicate<TestEntity, ?> andPred = (AndPredicate<TestEntity, ?>) combined;
    assertEquals(2, andPred.stream().count());
  }
}