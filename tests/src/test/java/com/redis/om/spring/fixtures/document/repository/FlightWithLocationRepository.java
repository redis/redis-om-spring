package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.FlightWithLocation;
import com.redis.om.spring.repository.RedisDocumentRepository;
import org.springframework.data.geo.Point;

public interface FlightWithLocationRepository extends RedisDocumentRepository<FlightWithLocation, String> {
  
  // Projection interface for testing issue #661
  interface FlightProjection {
    String getNumber();
    String getName();
    Point getLocation(); // This causes NoSuchElementException
  }
  
  // Projection without Point for comparison
  interface FlightProjectionWithoutPoint {
    String getNumber();
    String getName();
  }
  
  // Find methods using projections
  FlightProjection findByNumber(String number);
  
  FlightProjectionWithoutPoint findByName(String name);
}