package com.redis.om.spring.repository.query;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.Company;
import com.redis.om.spring.fixtures.document.repository.CompanyRepositoryWithLimiting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests to verify that findTop/findFirst repository methods properly limit results.
 * This tests the issue where findTopByTeamOrderByDueDateAsc returns LIMIT 0 10000 instead of LIMIT 0 1.
 */
class FindTopLimitingTest extends AbstractBaseDocumentTest {

  @Autowired
  CompanyRepositoryWithLimiting repository;

  @BeforeEach
  void setup() {
    repository.deleteAll();

    // Create test data
    Company company1 = Company.of("RedisInc", 2011, LocalDate.of(2011, 5, 1),
        new Point(-122.066540, 37.377690), "stack@redis.com");
    company1.setTags(Set.of("fast", "scalable", "reliable"));

    Company company2 = Company.of("Microsoft", 1975, LocalDate.of(1975, 4, 4),
        new Point(-122.124500, 47.640160), "info@microsoft.com");
    company2.setTags(Set.of("software", "cloud", "enterprise"));

    Company company3 = Company.of("Tesla", 2003, LocalDate.of(2003, 7, 1),
        new Point(-122.145800, 37.396400), "contact@tesla.com");
    company3.setTags(Set.of("electric", "automotive", "innovative"));

    repository.saveAll(List.of(company1, company2, company3));
  }

  @Test
  void testFindFirstByTags() {
    // Test findFirst with a tag filter
    Optional<Company> result = repository.findFirstByTags("software");
    
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Microsoft");
  }

  @Test
  void testFindFirstByOrderBy() {
    // Test findFirst with ordering
    Optional<Company> result = repository.findFirstByOrderByYearFoundedAsc();
    
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Microsoft");
    assertThat(result.get().getYearFounded()).isEqualTo(1975);
  }

  @Test
  void testFindTopByOrderBy() {
    // Test findTop with ordering
    Optional<Company> result = repository.findTopByOrderByYearFoundedDesc();
    
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("RedisInc");
    assertThat(result.get().getYearFounded()).isEqualTo(2011);
  }

  @Test
  void testFindTop2ByOrderBy() {
    // Test findTop with explicit number
    List<Company> results = repository.findTop2ByOrderByYearFoundedAsc();
    
    // This test will likely fail because Redis OM Spring doesn't properly parse the limit from method names
    assertThat(results).hasSize(2);
    assertThat(results.get(0).getName()).isEqualTo("Microsoft");
    assertThat(results.get(1).getName()).isEqualTo("Tesla");
  }

  @Test 
  void testFindFirstByTagsOrderBy() {
    // Test the reported issue pattern: findTopByTeamOrderByDueDateAsc
    Optional<Company> result = repository.findFirstByTagsOrderByYearFoundedAsc("automotive");
    
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Tesla");
  }

  @Test
  void testFindTop5ByOrderBy() {
    // Add more test data to verify limit 5
    Company company4 = Company.of("Apple", 1976, LocalDate.of(1976, 4, 1),
        new Point(-122.030000, 37.330000), "info@apple.com");
    Company company5 = Company.of("Amazon", 1994, LocalDate.of(1994, 7, 5),
        new Point(-122.329200, 47.614900), "info@amazon.com");
    Company company6 = Company.of("Google", 1998, LocalDate.of(1998, 9, 4),
        new Point(-122.084000, 37.422000), "info@google.com");
    repository.saveAll(List.of(company4, company5, company6));
    
    List<Company> results = repository.findTop5ByOrderByNameAsc();
    
    assertThat(results).hasSize(5);
    assertThat(results.get(0).getName()).isEqualTo("Amazon");
    assertThat(results.get(1).getName()).isEqualTo("Apple");
    assertThat(results.get(2).getName()).isEqualTo("Google");
    assertThat(results.get(3).getName()).isEqualTo("Microsoft");
    assertThat(results.get(4).getName()).isEqualTo("RedisInc");
  }

  @Test
  void testFindFirst3ByOrderBy() {
    List<Company> results = repository.findFirst3ByOrderByEmailDesc();
    
    assertThat(results).hasSize(3);
    assertThat(results.get(0).getEmail()).isEqualTo("stack@redis.com");
    assertThat(results.get(1).getEmail()).isEqualTo("info@microsoft.com");
    assertThat(results.get(2).getEmail()).isEqualTo("contact@tesla.com");
  }

  @Test
  void testFindTopByTagsOrderBy() {
    Optional<Company> result = repository.findTopByTagsOrderByNameAsc("software");
    
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Microsoft");
  }

  @Test
  void testFindTop10WithCriteria() {
    // This should return only the companies founded after 2000, limited to 10
    List<Company> results = repository.findTop10ByYearFoundedGreaterThanOrderByNameAsc(2000);
    
    assertThat(results).hasSize(2); // Only RedisInc (2011) and Tesla (2003) match
    assertThat(results.get(0).getName()).isEqualTo("RedisInc");
    assertThat(results.get(1).getName()).isEqualTo("Tesla");
  }

}