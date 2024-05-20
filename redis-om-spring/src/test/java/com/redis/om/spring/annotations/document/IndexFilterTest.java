package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.Vehicle;
import com.redis.om.spring.fixtures.document.model.VehicleType;
import com.redis.om.spring.fixtures.document.repository.VehicleRepository;
import com.redis.om.spring.search.stream.EntityStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class IndexFilterTest extends AbstractBaseDocumentTest {
  @Autowired
  VehicleRepository repository;

  @Autowired
  EntityStream entityStream;

  @BeforeEach
  void loadData() {
    Vehicle v1 = Vehicle.of("Beetle", "Volkswagen", VehicleType.COUPE);
    Vehicle v2 = Vehicle.of("911 S 2.4", "Porsche", VehicleType.COUPE);
    Vehicle v3 = Vehicle.of("Bronco", "Ford", VehicleType.SUV);
    Vehicle v4 = Vehicle.of("Range Rover Classic", "Land Rover", VehicleType.SUV);
    Vehicle v5 = Vehicle.of("Country Squire", "Ford", VehicleType.STATION_WAGON);
    Vehicle v6 = Vehicle.of("Corvette C3", "Chevrolet", VehicleType.SPORTS_CAR);
    Vehicle v7 = Vehicle.of("C/K", "Chevrolet", VehicleType.PICKUP_TRUCK);
    Vehicle v8 = Vehicle.of("Thunderbird", "Ford", VehicleType.COUPE);
    Vehicle v9 = Vehicle.of("Civic", "Honda", VehicleType.SEDAN);
    Vehicle v10 = Vehicle.of("S-Class", "Mercedes-Benz", VehicleType.SEDAN);

    repository.saveAll(List.of(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10));
  }

  @Test
  void testFindAllShouldReturnOnlyCoupes() {
    List<Vehicle> vehicles = entityStream.of(Vehicle.class).collect(Collectors.toList());

    assertAll( //
      () -> assertThat(vehicles.size()).isEqualTo(3), //
      () -> assertThat(vehicles).extracting("model").containsExactlyInAnyOrder("Beetle", "911 S 2.4", "Thunderbird"), //
      () -> assertThat(vehicles).allMatch(v -> v.getVehicleType().equals(VehicleType.COUPE)));
  }
}
