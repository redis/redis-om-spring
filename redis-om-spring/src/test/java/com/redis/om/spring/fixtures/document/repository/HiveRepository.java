package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Hive;
import com.redis.om.spring.repository.RedisDocumentRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HiveRepository extends RedisDocumentRepository<Hive, String> {

  Iterable<Hive> findByDrones_Content(UUID drones_content);

}
