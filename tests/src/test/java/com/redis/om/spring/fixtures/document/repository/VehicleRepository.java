package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Vehicle;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface VehicleRepository extends RedisDocumentRepository<Vehicle, String> {
}
