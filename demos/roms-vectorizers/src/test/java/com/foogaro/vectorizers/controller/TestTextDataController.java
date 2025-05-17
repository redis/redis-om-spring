package com.foogaro.vectorizers.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.foogaro.vectorizers.config.TestRedisConfiguration;
import com.foogaro.vectorizers.model.TextData;
import com.foogaro.vectorizers.service.TextDataService;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.main.allow-bean-definition-overriding=true", "management.health.redis.enabled=false",
        "management.metrics.enable.cache=false", "spring.cache.type=none" }
)
@ActiveProfiles(
  "test"
)
@ContextConfiguration(
    classes = TestRedisConfiguration.class
)
@DisabledIfEnvironmentVariable(
    named = "GITHUB_ACTIONS", matches = "true",
    disabledReason = "Skipping tests in the GitHub workflow because they interact with the embedding providers' API, which requires an API token."
)
@Disabled(
  "TODO: fix this when running ./mvnw clean verify"
)
public class TestTextDataController {

  private static final Logger logger = LoggerFactory.getLogger(TestTextDataController.class);

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private TextDataService service;

  private final String uri = "/api/text-data/";

  private final static TextData textData = TextData.of();

  @BeforeAll
  static void init() {
    String id = UUID.randomUUID().toString();
    String name = "Test TextData name";
    String description = "Test TextData description";
    int year = 2025;
    double score = 63.79;
    textData.setId(id);
    textData.setName(name);
    textData.setDescription(description);
    textData.setYear(year);
    textData.setScore(score);
  }

  @BeforeEach
  public void setUp() {
    service.deleteAll();
  }

  @Test
  public void testDeleteById() {
    ResponseEntity<TextData> response = restTemplate.postForEntity(uri, textData, TextData.class);
    logger.info("TextData: {}", response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isEqualTo(textData.getId());

    restTemplate.delete(uri + "deleteById/" + textData.getId());

    response = restTemplate.getForEntity(uri + "findById/" + textData.getId(), TextData.class);
    logger.info("TextData: {}", response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void testCreateTextData() {

    TextData saved = service.save(textData);

    ResponseEntity<TextData> response = restTemplate.getForEntity(uri + "findById/" + textData.getId(), TextData.class);
    logger.info("TextData: {}", response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isEqualTo(saved.getId());
    assertThat(response.getBody().getName()).isEqualTo(saved.getName());
    assertThat(response.getBody().getDescription()).isEqualTo(saved.getDescription());
    assertThat(response.getBody().getYear()).isEqualTo(saved.getYear());
    assertThat(response.getBody().getScore()).isEqualTo(saved.getScore());
  }

  @Test
  public void testFindById() {
    TextData saved = service.save(textData);

    ResponseEntity<TextData> response = restTemplate.getForEntity(uri + "findById/" + textData.getId(), TextData.class);
    logger.info("TextData: {}", response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isEqualTo(saved.getId());
    assertThat(response.getBody().getName()).isEqualTo(saved.getName());
    assertThat(response.getBody().getDescription()).isEqualTo(saved.getDescription());
    assertThat(response.getBody().getYear()).isEqualTo(saved.getYear());
    assertThat(response.getBody().getScore()).isEqualTo(saved.getScore());
  }

  @Test
  public void testFindByRightName() {
    TextData saved = service.save(textData);

    ResponseEntity<TextData> response = restTemplate.getForEntity(uri + "findByName/" + textData.getName(),
        TextData.class);
    logger.info("TextData: {}", response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isEqualTo(saved.getId());
    assertThat(response.getBody().getName()).isEqualTo(saved.getName());
    assertThat(response.getBody().getDescription()).isEqualTo(saved.getDescription());
    assertThat(response.getBody().getYear()).isEqualTo(saved.getYear());
    assertThat(response.getBody().getScore()).isEqualTo(saved.getScore());
  }

  @Test
  public void testFindByWrongName() {
    service.save(textData);
    ResponseEntity<TextData> response = restTemplate.getForEntity(uri + "findByName/Luigi", TextData.class);
    logger.info("TextData: {}", response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void testFindByRightYear() {
    TextData saved = service.save(textData);

    ResponseEntity<TextData[]> response = restTemplate.getForEntity(uri + "findByYear/" + textData.getYear(),
        TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()[0].getId()).isEqualTo(saved.getId());
    assertThat(response.getBody()[0].getName()).isEqualTo(saved.getName());
    assertThat(response.getBody()[0].getDescription()).isEqualTo(saved.getDescription());
    assertThat(response.getBody()[0].getYear()).isEqualTo(saved.getYear());
    assertThat(response.getBody()[0].getScore()).isEqualTo(saved.getScore());
  }

  @Test
  public void testFindByWrongYear() {
    service.save(textData);

    ResponseEntity<TextData[]> response = restTemplate.getForEntity(uri + "findByYear/1978", TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void testFindByRightYearBetween() {
    TextData saved = service.save(textData);

    ResponseEntity<TextData[]> response = restTemplate.getForEntity(uri + "findByYearBetween/2024/2026",
        TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()[0].getId()).isEqualTo(saved.getId());
    assertThat(response.getBody()[0].getName()).isEqualTo(saved.getName());
    assertThat(response.getBody()[0].getDescription()).isEqualTo(saved.getDescription());
    assertThat(response.getBody()[0].getYear()).isEqualTo(saved.getYear());
    assertThat(response.getBody()[0].getScore()).isEqualTo(saved.getScore());
  }

  @Test
  public void testFindByWrongYearBetween() {
    service.save(textData);

    ResponseEntity<TextData[]> response = restTemplate.getForEntity(uri + "findByYearBetween/2014/2016",
        TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void testFindByRightScore() {
    TextData saved = service.save(textData);

    ResponseEntity<TextData[]> response = restTemplate.getForEntity(uri + "findByScore/" + textData.getScore(),
        TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()[0].getId()).isEqualTo(saved.getId());
    assertThat(response.getBody()[0].getName()).isEqualTo(saved.getName());
    assertThat(response.getBody()[0].getDescription()).isEqualTo(saved.getDescription());
    assertThat(response.getBody()[0].getYear()).isEqualTo(saved.getYear());
    assertThat(response.getBody()[0].getScore()).isEqualTo(saved.getScore());
  }

  @Test
  public void testFindByWrongScore() {
    service.save(textData);

    ResponseEntity<TextData[]> response = restTemplate.getForEntity(uri + "findByScore/19.78", TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void testFindByRightScoreBetween() {
    TextData saved = service.save(textData);

    ResponseEntity<TextData[]> response = restTemplate.getForEntity(uri + "findByScoreBetween/63.00/64.00",
        TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()[0].getId()).isEqualTo(saved.getId());
    assertThat(response.getBody()[0].getName()).isEqualTo(saved.getName());
    assertThat(response.getBody()[0].getDescription()).isEqualTo(saved.getDescription());
    assertThat(response.getBody()[0].getYear()).isEqualTo(saved.getYear());
    assertThat(response.getBody()[0].getScore()).isEqualTo(saved.getScore());
  }

  @Test
  public void testFindByWrongScoreBetween() {
    service.save(textData);

    ResponseEntity<TextData[]> response = restTemplate.getForEntity(uri + "findByScoreBetween/50.50/60.60",
        TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void testFindBySemanticSearch() {
    TextData saved = service.save(textData);

    ResponseEntity<TextData[]> response = restTemplate.postForEntity(uri + "semanticSearch",
        "Are there any TextData description?", TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()[0].getId()).isEqualTo(saved.getId());
    assertThat(response.getBody()[0].getName()).isEqualTo(saved.getName());
    assertThat(response.getBody()[0].getDescription()).isEqualTo(saved.getDescription());
    assertThat(response.getBody()[0].getYear()).isEqualTo(saved.getYear());
    int length = response.getBody().length;
    for (int i = 0; i < length; i++) {
      logger.info("TextData: {}", response.getBody()[i]);
    }
  }

}