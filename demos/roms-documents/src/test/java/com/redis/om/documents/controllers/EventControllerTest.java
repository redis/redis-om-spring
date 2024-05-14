package com.redis.om.documents.controllers;

import com.redis.om.documents.AbstractDocumentTest;
import com.redis.om.documents.domain.Event;
import com.redis.om.documents.repositories.EventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class EventControllerTest extends AbstractDocumentTest {

  @Autowired
  private MockMvc mvc;
  @Autowired
  private EventRepository eventRepository;

  @BeforeEach
  void init() {
    var event1 = new Event("1", "event 1", LocalDateTime.parse("2023-01-01T00:00:00.000"),
      LocalDateTime.parse("2023-01-02T00:00:00.000"));
    var event2 = new Event("2", "event 2", LocalDateTime.parse("2023-03-08T00:00:00.000"),
      LocalDateTime.parse("2023-03-09T00:00:00.000"));

    eventRepository.saveAll(List.of(event1, event2));
  }

  @AfterEach
  void clear() {
    eventRepository.deleteAll();
  }

  @Test
  void shouldReturnAllEvents() throws Exception {
    var all = eventRepository.findAll();
    assertEquals(2, all.size());

    mvc.perform(MockMvcRequestBuilders.get("/api/events/between")
        .param("start", LocalDateTime.parse("2023-02-01T00:00:00.000").toString())
        .param("end", LocalDateTime.parse("2023-04-01T00:00:00.000").toString())).andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id", equalTo("2")));
  }

  @Test
  void shouldReturnEmpty() throws Exception {
    var all = eventRepository.findAll();
    assertEquals(2, all.size());

    mvc.perform(MockMvcRequestBuilders.get("/api/events/between")
        .param("start", LocalDateTime.parse("2023-03-09T00:00:00.000").toString())
        .param("end", LocalDateTime.parse("2023-04-01T00:00:00.000").toString())).andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(0)));
  }

}