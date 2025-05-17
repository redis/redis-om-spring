package com.foogaro.modeling.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.foogaro.modeling.config.TestRedisConfiguration;
import com.foogaro.modeling.model.TextData;
import com.foogaro.modeling.service.TextDataService;

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
    textData.setMeasurements(Arrays.asList(1.0, 2.0, 8.0, 9.0));
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
  public void testFindByRightDescription() {
    TextData saved = service.save(textData);

    ResponseEntity<TextData[]> response = restTemplate.getForEntity(uri + "findByDescription/TextData",
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
  public void testFindByWrongDescription() {
    service.save(textData);

    ResponseEntity<TextData[]> response = restTemplate.getForEntity(uri + "findByDescription/Luigi", TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
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
  public void findByMeasurementsIn() {
    TextData t1 = TextData.of();
    t1.setId(UUID.randomUUID().toString());
    t1.setName("Test TextData1 name");
    t1.setDescription("Test TextData1 description");
    t1.setYear(2025);
    t1.setScore(63.79);
    t1.setMeasurements(Arrays.asList(1.0, 2.0, 8.0, 9.0));
    t1 = service.save(t1);
    TextData t2 = TextData.of();
    t2.setId(UUID.randomUUID().toString());
    t2.setName("Test TextData2 name");
    t2.setDescription("Test TextData2 description");
    t2.setYear(2025);
    t2.setScore(63.79);
    t2.setMeasurements(Arrays.asList(1.0, 2.0, 5.0, 8.0, 9.0));
    t2 = service.save(t2);

    ResponseEntity<TextData[]> response = restTemplate.getForEntity(uri + "findByMeasurementsIn/2.0", TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().length).isEqualTo(2);
    assertThat(response.getBody()[0].getId()).isEqualTo(t1.getId());
    assertThat(response.getBody()[1].getId()).isEqualTo(t2.getId());

    response = restTemplate.getForEntity(uri + "findByMeasurementsIn/5.0", TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()[0].getId()).isEqualTo(t2.getId());

    response = restTemplate.getForEntity(uri + "findByMeasurementsIn/3.0", TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void testFindByMissingDescription() {
    TextData t1 = TextData.of();
    t1.setId(UUID.randomUUID().toString());
    t1.setName("Test TextData1 name");
    //        t1.setDescription("");
    t1.setYear(2025);
    t1.setScore(63.79);
    t1.setMeasurements(Arrays.asList(1.0, 2.0, 8.0, 9.0));
    t1 = service.save(t1);

    TextData t2 = TextData.of();
    t2.setId(UUID.randomUUID().toString());
    t2.setName("Test TextData2 name");
    t2.setDescription("");
    t2.setYear(2025);
    t2.setScore(63.79);
    t2.setMeasurements(Arrays.asList(1.0, 2.0, 8.0, 9.0));
    t2 = service.save(t2);

    TextData t3 = TextData.of();
    t3.setId(UUID.randomUUID().toString());
    t3.setName("Test TextData3 name");
    t3.setDescription("Test TextData3 description");
    t3.setYear(2025);
    t3.setScore(63.79);
    t3.setMeasurements(Arrays.asList(1.0, 2.0, 8.0, 9.0));
    t3 = service.save(t3);

    //        JSON.SET com.foogaro.modeling.model.TextData:ea77ac16-eaef-4f34-96e3-7db1d9813444 . '{"id":"ea77ac16-eaef-4f34-96e3-7db1d9813444","name":"Test TextData1 name","year":2025,"score":63.79,"measurements":[1.0,2.0,8.0,9.0]}'
    //        JSON.SET com.foogaro.modeling.model.TextData:acbc6993-671b-4310-bf30-555709495f16 . '{"id":"acbc6993-671b-4310-bf30-555709495f16","name":"Test TextData2 name","description":"","year":2025,"score":63.79,"measurements":[1.0,2.0,8.0,9.0]}'
    //        JSON.SET com.foogaro.modeling.model.TextData:a302b38b-533e-4a9b-ae75-81a75556934c . '{"id":"a302b38b-533e-4a9b-ae75-81a75556934c","name":"Test TextData3 name","description":"Test TextData3 description","year":2025,"score":63.79,"measurements":[1.0,2.0,8.0,9.0]}'
    //        FT.SEARCH com.foogaro.modeling.model.TextDataIdx 'ismissing(@description)' DIALECT

    ResponseEntity<TextData[]> response = restTemplate.getForEntity(uri + "findByMissingDescription", TextData[].class);
    logger.info("TextData: {}", (Object[]) response.getBody());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().length).isEqualTo(1);
    assertThat(response.getBody()[0].getId()).isEqualTo(t1.getId());
    assertThat(response.getBody()[0].getName()).isEqualTo(t1.getName());
    assertThat(response.getBody()[0].getDescription()).isNullOrEmpty();
    assertThat(response.getBody()[0].getDescription()).isEqualTo(t1.getDescription());
    assertThat(response.getBody()[0].getYear()).isEqualTo(t1.getYear());
    assertThat(response.getBody()[0].getScore()).isEqualTo(t1.getScore());
  }

}