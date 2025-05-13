package com.redis.om.documents.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redis.om.documents.domain.Event;
import com.redis.om.documents.service.EventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(
  "/api/events"
)
@RequiredArgsConstructor
public class EventController {

  private final EventService eventService;

  @GetMapping(
    "between"
  )
  List<Event> byNumberOfEmployees(@RequestParam(
    "start"
  ) @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE_TIME
  ) LocalDateTime start, @RequestParam(
    "end"
  ) @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE_TIME
  ) LocalDateTime end) {
    return eventService.searchByBeginDateBetween(start, end);
  }

}
