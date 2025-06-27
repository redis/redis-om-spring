package com.redis.om.streams.controller;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.redis.om.streams.Producer;
import com.redis.om.streams.exception.InvalidMessageException;
import com.redis.om.streams.exception.ProducerTimeoutException;
import com.redis.om.streams.exception.TopicNotFoundException;
import com.redis.om.streams.model.TextData;

@RestController
@RequestMapping(
  "/api/streams"
)
public class StreamsController {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public static boolean stopLoading = Boolean.FALSE;

  private final Producer producer;
  private final ObjectMapper objectMapper;

  public StreamsController(Producer producer, ObjectMapper objectMapper) {
    this.producer = producer;
    this.objectMapper = objectMapper;
  }

  private void create(TextData textData) {
    try {
      producer.produce(objectMapper.convertValue(textData, new TypeReference<>() {
      }));
    } catch (TopicNotFoundException | InvalidMessageException | ProducerTimeoutException e) {
      logger.error(e.getMessage(), e);
    }
  }

  @GetMapping(
      path = "/start-load"
  )
  public ResponseEntity<Integer> startLoading() {
    Faker faker = new Faker();
    StreamsController.stopLoading = Boolean.FALSE;
    AtomicInteger created = new AtomicInteger();
    while (!StreamsController.stopLoading) {
      TextData textData = TextData.of();
      try {
        textData.setId(created.getAndIncrement());
        textData.setName(faker.dune().character());
        textData.setDescription(faker.dune().quote());
        create(textData);
        showSpinner(created.get());
      } catch (Exception e) {
        logger.error("Error while creating new TextData: {}", textData, e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }
    }
    System.out.print("\r                                                   ");
    System.out.print("\r");
    return ResponseEntity.ok(created.get());
  }

  @GetMapping(
      path = "/start-load/{count}"
  )
  public ResponseEntity<Integer> startLoading(@PathVariable int count) {
    Faker faker = new Faker();
    StreamsController.stopLoading = Boolean.FALSE;
    AtomicInteger created = new AtomicInteger();
    while (created.get() < count) {
      TextData textData = TextData.of();
      try {
        textData.setId(created.getAndIncrement());
        textData.setName(faker.dune().character());
        textData.setDescription(faker.dune().quote());
        create(textData);
        showSpinner(created.get());
      } catch (Exception e) {
        logger.error("Error while creating new TextData: {}", textData, e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }
    }
    System.out.print("\r                                                   ");
    System.out.print("\r");
    return ResponseEntity.ok(created.get());
  }

  @GetMapping(
      path = "/stop-load"
  )
  public ResponseEntity<Void> stopLoading() {
    StreamsController.stopLoading = true;
    return ResponseEntity.noContent().build();
  }

  //    private final List<String> wheel = List.of("|", "/", "-", "\\", "|", "/", "-", "\\");
  //    private final String wheel = "⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏";
  //    private final String wheel = "⠹⠸⠼⠧⠇⠏";
  private void showSpinner(int count) {
    String s = String.format("%,d", count);
    String wheel = "⠁ ⠃ ⠇ ⠧⠷⠿⡿⣟⣯⣷";
    System.out.printf("\rProgress: " + s + " -> " + wheel.charAt(count % wheel.length()) + "  ");
  }
}
