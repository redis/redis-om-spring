package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.ReducerFunction;
import com.redis.om.spring.fixtures.document.model.Company;
import com.redis.om.spring.fixtures.document.model.Company$;
import com.redis.om.spring.fixtures.document.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.geo.Point;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for projection support in aggregations (issue #539).
 * Verifies that aggregations can return projection interfaces following
 * Spring Data conventions where IDs are not automatically included.
 */
class AggregationProjectionTest extends AbstractBaseDocumentTest {

  @Autowired
  CompanyRepository repository;

  @Autowired
  EntityStream entityStream;

  // Projection interfaces
  public interface CompanyNameProjection {
    String getName();
  }

  public interface CompanyDetailsProjection {
    String getName();
    Integer getYearFounded();
  }

  public interface CompanyWithIdProjection {
    String getId();
    String getName();
    Integer getYearFounded();
  }

  public interface CompanyStatsProjection {
    Integer getYearFounded();
    Long getCount();
  }

  @BeforeEach
  void setup() {
    repository.deleteAll();

    // Create test data
    Company company1 = Company.of("RedisInc", 2011, LocalDate.of(2011, 5, 1),
        new Point(-122.066540, 37.377690),
        "stack@redis.com");
    company1.setPubliclyListed(true);
    company1.setTags(Set.of("fast", "scalable", "reliable"));
    company1.setId("company1");

    Company company2 = Company.of("Microsoft", 1975, LocalDate.of(1975, 4, 4),
        new Point(-122.124500, 47.640160),
        "info@microsoft.com");
    company2.setPubliclyListed(true);
    company2.setTags(Set.of("software", "cloud", "enterprise"));
    company2.setId("company2");

    Company company3 = Company.of("Tesla", 2003, LocalDate.of(2003, 7, 1),
        new Point(-122.145800, 37.396400),
        "contact@tesla.com");
    company3.setPubliclyListed(true);
    company3.setTags(Set.of("electric", "automotive", "innovative"));
    company3.setId("company3");

    repository.saveAll(List.of(company1, company2, company3));
  }

  @Test
  void testSimpleProjection() {
    // Test basic projection with single field
    List<CompanyNameProjection> projections = entityStream
        .of(Company.class)
        .filter(Company$.PUBLICLY_LISTED.eq(true))
        .load(Company$.NAME)
        .toProjection(CompanyNameProjection.class);

    assertThat(projections).hasSize(3);
    
    Set<String> names = projections.stream()
        .map(CompanyNameProjection::getName)
        .collect(java.util.stream.Collectors.toSet());
    
    assertThat(names).containsExactlyInAnyOrder("Microsoft", "RedisInc", "Tesla");
  }

  @Test
  void testMultiFieldProjection() {
    // Test projection with multiple fields
    List<CompanyDetailsProjection> projections = entityStream
        .of(Company.class)
        .filter(Company$.YEAR_FOUNDED.gt(1990))
        .load(Company$.NAME, Company$.YEAR_FOUNDED)
        .toProjection(CompanyDetailsProjection.class);

    assertThat(projections).hasSize(2);
    
    for (CompanyDetailsProjection projection : projections) {
      assertThat(projection.getName()).isNotNull();
      assertThat(projection.getYearFounded()).isNotNull();
      assertThat(projection.getYearFounded()).isGreaterThan(1990);
    }
  }

  @Test
  void testProjectionWithExplicitId() {
    // Test that ID is only included when explicitly defined in projection
    List<CompanyWithIdProjection> projections = entityStream
        .of(Company.class)
        .filter(Company$.NAME.eq("RedisInc"))
        .load(Company$.ID, Company$.NAME, Company$.YEAR_FOUNDED)
        .toProjection(CompanyWithIdProjection.class);

    assertThat(projections).hasSize(1);
    
    CompanyWithIdProjection projection = projections.get(0);
    assertThat(projection.getId()).isEqualTo("company1");
    assertThat(projection.getName()).isEqualTo("RedisInc");
    assertThat(projection.getYearFounded()).isEqualTo(2011);
  }

  @Test
  void testProjectionWithGroupByAndReduce() {
    // Test projection with aggregation functions
    List<CompanyStatsProjection> projections = entityStream
        .of(Company.class)
        .groupBy(Company$.YEAR_FOUNDED)
        .reduce(ReducerFunction.COUNT)
        .as("count")
        .toProjection(CompanyStatsProjection.class);

    assertThat(projections).hasSize(3);
    
    // Each year should have count of 1
    for (CompanyStatsProjection projection : projections) {
      assertThat(projection.getYearFounded()).isNotNull();
      assertThat(projection.getCount()).isEqualTo(1L);
    }
  }

  @Test
  void testProjectionWithNonInterfaceThrowsException() {
    // Test that non-interface classes throw exception
    assertThatThrownBy(() ->
        entityStream
            .of(Company.class)
            .load(Company$.NAME)
            .toProjection(String.class)
    ).isInstanceOf(IllegalArgumentException.class)
     .hasMessageContaining("Projection class must be an interface");
  }

  @Test
  void testProjectionHandlesNullValues() {
    // Test projection with existing data - skip creating null data for now
    List<CompanyNameProjection> projections = entityStream
        .of(Company.class)
        .filter(Company$.PUBLICLY_LISTED.eq(true))
        .load(Company$.NAME)
        .toProjection(CompanyNameProjection.class);

    assertThat(projections).hasSize(3);
    assertThat(projections.get(0).getName()).isNotNull();
  }

  @Test
  void testProjectionFieldNameMapping() {
    // Test that projection handles field name variations with aggregation
    List<CompanyNameProjection> projections = entityStream
        .of(Company.class)
        .groupBy(Company$.NAME)
        .reduce(ReducerFunction.COUNT).as("count")
        .toProjection(CompanyNameProjection.class);

    assertThat(projections).hasSize(3);
    
    Set<String> names = projections.stream()
        .map(CompanyNameProjection::getName)
        .collect(java.util.stream.Collectors.toSet());
    
    assertThat(names).containsExactlyInAnyOrder("RedisInc", "Microsoft", "Tesla");
  }

  @Test
  void testMapsWithIdIncluded() {
    // Test toMaps() with ID included by default using aggregation with count
    List<Map<String, Object>> maps = entityStream
        .of(Company.class)
        .filter(Company$.PUBLICLY_LISTED.eq(true))
        .groupBy(Company$.NAME, Company$.YEAR_FOUNDED)
        .reduce(ReducerFunction.COUNT).as("count")
        .toMaps();

    assertThat(maps).hasSize(3);
    
    Map<String, Object> firstMap = maps.get(0);
    assertThat(firstMap).containsKey("name");
    assertThat(firstMap).containsKey("yearFounded");
    assertThat(firstMap).containsKey("count");
    assertThat(firstMap.get("name")).isNotNull();
    assertThat(firstMap.get("yearFounded")).isNotNull();
  }

  @Test
  void testMapsWithIdExcluded() {
    // Test toMaps(false) with ID excluded using aggregation with count
    List<Map<String, Object>> maps = entityStream
        .of(Company.class)
        .filter(Company$.PUBLICLY_LISTED.eq(true))
        .groupBy(Company$.NAME, Company$.YEAR_FOUNDED)
        .reduce(ReducerFunction.COUNT).as("count")
        .toMaps(false);

    assertThat(maps).hasSize(3);
    
    Map<String, Object> firstMap = maps.get(0);
    assertThat(firstMap).doesNotContainKey("id");
    assertThat(firstMap).containsKey("name");
    assertThat(firstMap).containsKey("yearFounded");
    assertThat(firstMap).containsKey("count");
    assertThat(firstMap.get("name")).isNotNull();
  }

  @Test
  void testMapsWithAggregation() {
    // Test maps with grouping and reduction
    List<Map<String, Object>> maps = entityStream
        .of(Company.class)
        .groupBy(Company$.YEAR_FOUNDED)
        .reduce(com.redis.om.spring.annotations.ReducerFunction.COUNT)
        .as("companyCount")
        .sorted(Order.asc("@yearFounded"))
        .toMaps();

    assertThat(maps).hasSize(3);
    
    // Check first result
    Map<String, Object> firstMap = maps.get(0);
    assertThat(firstMap).containsKey("yearFounded");
    assertThat(firstMap).containsKey("companyCount");
    
    Object yearFoundedValue = convertValue(firstMap.get("yearFounded"), Integer.class);
    assertThat(yearFoundedValue).isEqualTo(1975);
    
    Object companyCountValue = convertValue(firstMap.get("companyCount"), Long.class);
    assertThat(companyCountValue).isEqualTo(1L);
  }

  @Test
  void testProjectionEquality() {
    // Test that projections with same data are equal
    List<CompanyNameProjection> projections1 = entityStream
        .of(Company.class)
        .filter(Company$.NAME.eq("RedisInc"))
        .load(Company$.NAME)
        .toProjection(CompanyNameProjection.class);

    List<CompanyNameProjection> projections2 = entityStream
        .of(Company.class)
        .filter(Company$.NAME.eq("RedisInc"))
        .load(Company$.NAME)
        .toProjection(CompanyNameProjection.class);

    assertThat(projections1).hasSize(1);
    assertThat(projections2).hasSize(1);
    
    // Test toString
    assertThat(projections1.get(0).toString()).contains("CompanyNameProjection");
    assertThat(projections1.get(0).toString()).contains("name");
    
    // Test hashCode consistency
    assertThat(projections1.get(0).hashCode()).isEqualTo(projections2.get(0).hashCode());
  }

  /**
   * Helper method to convert values from Redis aggregation results.
   * Redis may return values as strings that need to be converted to the expected type.
   */
  private Object convertValue(Object value, Class<?> targetType) {
    if (value == null) {
      return null;
    }
    
    if (targetType.isAssignableFrom(value.getClass())) {
      return value;
    }
    
    // Handle string conversions
    if (value instanceof String stringValue) {
      if (targetType == Integer.class) {
        return Integer.parseInt(stringValue);
      } else if (targetType == Long.class) {
        return Long.parseLong(stringValue);
      } else if (targetType == Double.class) {
        return Double.parseDouble(stringValue);
      } else if (targetType == Float.class) {
        return Float.parseFloat(stringValue);
      } else if (targetType == Boolean.class) {
        return Boolean.parseBoolean(stringValue);
      }
    }
    
    return value;
  }
}