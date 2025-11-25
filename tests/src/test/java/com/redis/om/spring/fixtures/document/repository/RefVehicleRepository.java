package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.RefVehicle;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface RefVehicleRepository extends RedisDocumentRepository<RefVehicle, String> {
}
