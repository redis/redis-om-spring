package com.redis.om.documents.repositories;

import com.redis.om.documents.domain.Event;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface EventRepository extends RedisDocumentRepository<Event, String> {

}
