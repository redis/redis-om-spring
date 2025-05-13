package com.redis.om.documents.service;

import java.time.LocalDateTime;
import java.util.List;

import com.redis.om.documents.domain.Event;

public interface EventService {
  List<Event> searchByBeginDateBetween(LocalDateTime start, LocalDateTime end);
}
