package com.redis.om.documents.service.impl;

import com.redis.om.documents.domain.Event;
import com.redis.om.documents.domain.Event$;
import com.redis.om.documents.service.EventService;
import com.redis.om.spring.search.stream.EntityStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EntityStream entityStream;

    @Override
    public List<Event> searchByBeginDateBetween(LocalDateTime start, LocalDateTime end) {

        return entityStream.of(Event.class)
                .filter(Event$.BEGIN_DATE.between(start, end))
                .collect(Collectors.toList());
    }
}
