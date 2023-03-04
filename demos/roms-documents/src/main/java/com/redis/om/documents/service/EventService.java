package com.redis.om.documents.service;

import com.redis.om.documents.domain.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<Event> searchByBeginDateBetween(LocalDateTime start, LocalDateTime end);
}
