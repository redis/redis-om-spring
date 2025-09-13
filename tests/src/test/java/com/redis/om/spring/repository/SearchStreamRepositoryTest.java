package com.redis.om.spring.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.Company;
import com.redis.om.spring.fixtures.document.model.Company$;
import com.redis.om.spring.fixtures.document.model.CompanyMeta;
import com.redis.om.spring.fixtures.document.model.Employee;
import com.redis.om.spring.fixtures.document.repository.CompanyRepository;
import com.redis.om.spring.search.stream.SearchStream;
import redis.clients.jedis.search.aggr.SortedField.SortOrder;

/**
 * Test to verify that repositories can return SearchStream for fluent query operations.
 * This validates the documentation claim that "Repositories can return SearchStream for fluent query operations"
 */
class SearchStreamRepositoryTest extends AbstractBaseDocumentTest {
  
  @Autowired
  CompanyRepository repository;
  
  private Company redis;
  private Company microsoft;
  private Company apple;
  private Company ibm;
  private Company oracle;
  
  @BeforeEach
  void setUp() {
    // Create test companies with various founding years
    redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), 
        new Point(-122.066540, 37.377690), "stack@redis.com");
    redis.setTags(Set.of("database", "nosql", "redis"));
    redis.setMetaList(Set.of(CompanyMeta.of("Redis", 100, Set.of("RedisTag"))));
    redis.setPubliclyListed(false);
    redis.setEmployees(Set.of(Employee.of("John Doe"), Employee.of("Jane Smith")));
    
    microsoft = Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15), 
        new Point(-122.124500, 47.640160), "research@microsoft.com");
    microsoft.setTags(Set.of("software", "cloud", "windows"));
    microsoft.setMetaList(Set.of(CompanyMeta.of("MS", 50, Set.of("MsTag"))));
    microsoft.setPubliclyListed(true);
    
    apple = Company.of("Apple", 1976, LocalDate.of(2022, 9, 10), 
        new Point(-122.0322, 37.3220), "info@apple.com");
    apple.setTags(Set.of("hardware", "software", "mobile"));
    apple.setMetaList(Set.of(CompanyMeta.of("AAPL", 75, Set.of("AppleTag"))));
    apple.setPubliclyListed(true);
    
    ibm = Company.of("IBM", 1911, LocalDate.of(2022, 6, 1), 
        new Point(-73.8007, 41.0504), "contact@ibm.com");
    ibm.setTags(Set.of("enterprise", "cloud", "ai"));
    ibm.setPubliclyListed(true);
    
    oracle = Company.of("Oracle", 1977, LocalDate.of(2022, 7, 1), 
        new Point(-122.2659, 37.5314), "info@oracle.com");
    oracle.setTags(Set.of("database", "enterprise", "cloud"));
    oracle.setPubliclyListed(true);
    
    repository.saveAll(List.of(redis, microsoft, apple, ibm, oracle));
  }
  
  @AfterEach
  void tearDown() {
    repository.deleteAll();
  }
  
  @Test
  void testRepositoryReturnsSearchStream() {
    // Test that repository method returns SearchStream
    SearchStream<Company> stream = repository.findByYearFoundedGreaterThan(1970);
    
    assertNotNull(stream, "Repository should return a SearchStream");
    assertThat(stream).isInstanceOf(SearchStream.class);
    
    // Verify the stream contains the expected companies
    List<Company> companies = stream.collect(Collectors.toList());
    assertEquals(4, companies.size(), "Should find 4 companies founded after 1970");
    
    // Verify companies are: Microsoft (1975), Apple (1976), Oracle (1977), RedisInc (2011)
    List<String> companyNames = companies.stream()
        .map(Company::getName)
        .sorted()
        .collect(Collectors.toList());
    
    assertThat(companyNames).containsExactly("Apple", "Microsoft", "Oracle", "RedisInc");
  }
  
  @Test
  void testSearchStreamFluentOperations() {
    // Test fluent operations on SearchStream returned from repository
    SearchStream<Company> stream = repository.findByYearFoundedGreaterThan(1970);
    
    // Filter for publicly listed companies only
    List<Company> publicCompanies = stream
        .filter(Company$.PUBLICLY_LISTED.eq(true))
        .collect(Collectors.toList());
    
    assertEquals(3, publicCompanies.size(), "Should find 3 publicly listed companies");
    
    // Verify the publicly listed companies
    List<String> publicCompanyNames = publicCompanies.stream()
        .map(Company::getName)
        .sorted()
        .collect(Collectors.toList());
    
    assertThat(publicCompanyNames).containsExactly("Apple", "Microsoft", "Oracle");
  }
  
  @Test
  void testSearchStreamMapOperation() {
    // Test map operation on SearchStream to extract company names
    SearchStream<Company> stream = repository.findByYearFoundedGreaterThan(2000);
    
    // Map to company names
    List<String> companyNames = stream
        .map(Company$.NAME)
        .collect(Collectors.toList());
    
    assertEquals(1, companyNames.size(), "Should find 1 company founded after 2000");
    assertEquals("RedisInc", companyNames.get(0));
  }
  
  @Test
  void testSearchStreamChainedFilters() {
    // Test multiple chained filter operations
    SearchStream<Company> stream = repository.findByYearFoundedGreaterThan(1900);
    
    // Filter for publicly listed companies with "database" tag
    List<Company> databaseCompanies = stream
        .filter(Company$.PUBLICLY_LISTED.eq(true))
        .filter(Company$.TAGS.in("database"))
        .collect(Collectors.toList());
    
    assertEquals(1, databaseCompanies.size(), "Should find 1 publicly listed database company");
    assertEquals("Oracle", databaseCompanies.get(0).getName());
  }
  
  @Test
  void testSearchStreamWithSort() {
    // Test sorting capabilities of SearchStream
    SearchStream<Company> stream = repository.findByYearFoundedGreaterThan(1970);
    
    // Sort by year founded ascending
    List<Company> sortedCompanies = stream
        .sorted(Company$.YEAR_FOUNDED, SortOrder.ASC)
        .collect(Collectors.toList());
    
    assertEquals(4, sortedCompanies.size());
    
    // Verify sorting order
    assertEquals("Microsoft", sortedCompanies.get(0).getName()); // 1975
    assertEquals("Apple", sortedCompanies.get(1).getName());     // 1976
    assertEquals("Oracle", sortedCompanies.get(2).getName());    // 1977
    assertEquals("RedisInc", sortedCompanies.get(3).getName());  // 2011
  }
  
  @Test
  void testSearchStreamLimit() {
    // Test limit operation on SearchStream
    SearchStream<Company> stream = repository.findByYearFoundedGreaterThan(1900);
    
    // Limit to first 2 results
    List<Company> limitedCompanies = stream
        .limit(2)
        .collect(Collectors.toList());
    
    assertEquals(2, limitedCompanies.size(), "Should return only 2 companies due to limit");
  }
  
  @Test
  void testSearchStreamCount() {
    // Test count operation on SearchStream
    SearchStream<Company> stream = repository.findByYearFoundedGreaterThan(1970);
    
    long count = stream.count();
    
    assertEquals(4, count, "Should count 4 companies founded after 1970");
  }
  
  @Test
  void testSearchStreamEmptyResult() {
    // Test SearchStream with no matching results
    SearchStream<Company> stream = repository.findByYearFoundedGreaterThan(2020);
    
    List<Company> companies = stream.collect(Collectors.toList());
    
    assertTrue(companies.isEmpty(), "Should return empty list for companies founded after 2020");
  }
  
  @Test
  void testSearchStreamComplexQuery() {
    // Test a complex query combining multiple operations as shown in documentation
    SearchStream<Company> stream = repository.findByYearFoundedGreaterThan(1970);
    
    // Complex query: publicly listed companies, map to names, filter names starting with 'A'
    List<String> names = stream
        .filter(Company$.PUBLICLY_LISTED.eq(true))
        .map(Company$.NAME)
        .filter(name -> ((String) name).startsWith("A"))
        .collect(Collectors.toList());
    
    assertEquals(1, names.size(), "Should find 1 publicly listed company starting with 'A'");
    assertEquals("Apple", names.get(0));
  }
}