package com.redis.om.spring.fixtures.document.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.redis.om.spring.fixtures.document.model.Hive;
import com.redis.om.spring.repository.RedisDocumentRepository;

@Repository
public interface HiveRepository extends RedisDocumentRepository<Hive, String> {

  Iterable<Hive> findByDrones_Content(UUID drones_content);

}
