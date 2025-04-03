package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Doc5;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface Doc5Repository extends RedisDocumentRepository<Doc5, String> {
    List<Doc5> findByRegistrationDateBetween(LocalDateTime from, LocalDateTime to);
    List<Doc5> findByIsActive(boolean active);
    List<Doc5> findByAge(int age);
}