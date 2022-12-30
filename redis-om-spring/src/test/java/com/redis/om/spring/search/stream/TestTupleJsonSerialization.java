package com.redis.om.spring.search.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.data.geo.Point;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Company;
import com.redis.om.spring.annotations.document.fixtures.Company$;
import com.redis.om.spring.annotations.document.fixtures.CompanyRepository;
import com.redis.om.spring.annotations.document.fixtures.Employee;
import com.redis.om.spring.tuple.Fields;

import io.redisearch.aggregation.SortedField.SortOrder;

class TestTupleJsonSerialization extends AbstractBaseDocumentTest {
  @Autowired
  CompanyRepository repository;

  @Autowired
  EntityStream entityStream;
  
  private JacksonTester<List<Map<String, Object>>> json;

  @BeforeEach
  void setupAndCleanup() {
    ObjectMapper objectMapper = new ObjectMapper();
    JacksonTester.initFields(this, objectMapper);
    
    // companies
    repository.deleteAll();


    Company redis = repository.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    redis.setTags(Set.of("fast", "scalable", "reliable", "database", "nosql"));

    Set<Employee> employees = Sets.newHashSet(Employee.of("Brian Sam-Bodden"), Employee.of("Guy Royse"),
        Employee.of("Justin Castilla"));
    redis.setEmployees(employees);

    Company microsoft = repository.save(Company.of("Microsoft", 1975, LocalDate.of(2022, 8, 15),
        new Point(-122.124500, 47.640160), "research@microsoft.com"));
    microsoft.setTags(Set.of("innovative", "reliable", "os", "ai"));

    Company tesla = repository.save(
        Company.of("Tesla", 2003, LocalDate.of(2022, 1, 1), new Point(-97.6208903, 30.2210767), "elon@tesla.com"));
    tesla.setTags(Set.of("innovative", "futuristic", "ai"));

    repository.saveAll(List.of(redis, microsoft, tesla));
  }
  
  @Test
  void testTripleResultWithLabels() throws IOException {
    List<Map<String, Object>> results = entityStream //
        .of(Company.class) //
        .sorted(Company$.NAME, SortOrder.DESC) //
        .map(Fields.of(Company$.NAME, Company$.YEAR_FOUNDED, Company$.LOCATION)) //
        .mapToLabelledMaps() //
        .collect(Collectors.toList());

    assertEquals(3, results.size());
    
    JsonContent<List<Map<String, Object>>> asJson = json.write(results);
    
    // See JSON file under srr/test/resource/com/redis/om/spring/search/stream/companies.json
    assertThat(asJson).isEqualToJson("companies.json");
  }
}
