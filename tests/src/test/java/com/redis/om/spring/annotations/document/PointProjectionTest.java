package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.FlightWithLocation;
import com.redis.om.spring.fixtures.document.repository.FlightWithLocationRepository;
import com.redis.om.spring.fixtures.document.repository.FlightWithLocationRepository.FlightProjection;
import com.redis.om.spring.fixtures.document.repository.FlightWithLocationRepository.FlightProjectionWithoutPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Test for issue #661 - NoSuchElementException when using Point in projection interface
 * https://github.com/redis/redis-om-spring/issues/661
 */
class PointProjectionTest extends AbstractBaseDocumentTest {
  
  @Autowired
  private FlightWithLocationRepository repository;
  
  @BeforeEach
  void setup() {
    repository.deleteAll();
    
    // Create test data
    FlightWithLocation flight1 = new FlightWithLocation("AA123", "Flight to Paris", new Point(2.3522, 48.8566));
    FlightWithLocation flight2 = new FlightWithLocation("BA456", "Flight to London", new Point(-0.1276, 51.5074));
    FlightWithLocation flight3 = new FlightWithLocation("LH789", "Flight to Berlin", new Point(13.4050, 52.5200));
    
    repository.save(flight1);
    repository.save(flight2);
    repository.save(flight3);
  }
  
  @Test
  void testProjectionWithoutPointWorks() {
    // This should work fine - projection without Point field
    FlightProjectionWithoutPoint projection = repository.findByName("Flight to Paris");
    
    assertThat(projection).isNotNull();
    assertThat(projection.getNumber()).isEqualTo("AA123");
    assertThat(projection.getName()).isEqualTo("Flight to Paris");
  }
  
  @Test
  void testProjectionWithPointNowWorks() {
    // This test verifies that issue #661 has been fixed
    // Previously would throw NoSuchElementException, now it works
    FlightProjection projection = repository.findByNumber("AA123");
    
    assertThat(projection).isNotNull();
    assertThat(projection.getNumber()).isEqualTo("AA123");
    assertThat(projection.getName()).isEqualTo("Flight to Paris");
    
    // After fix, accessing Point field in projection should work
    Point location = projection.getLocation();
    assertThat(location).isNotNull();
    assertThat(location.getX()).isEqualTo(2.3522);
    assertThat(location.getY()).isEqualTo(48.8566);
  }
  
  @Test
  void testDirectRepositoryAccessWithPointWorks() {
    // Direct repository access should work fine
    FlightWithLocation flight = repository.findById(
        repository.findAll().iterator().next().getId()
    ).orElseThrow();
    
    assertThat(flight.getLocation()).isNotNull();
    assertThat(flight.getLocation().getX()).isEqualTo(2.3522);
    assertThat(flight.getLocation().getY()).isEqualTo(48.8566);
  }
  
  @Test
  void testProjectionWithPointShouldWork() {
    // After fix, this should work without throwing exception
    FlightProjection projection = repository.findByNumber("BA456");
    
    assertThat(projection).isNotNull();
    assertThat(projection.getNumber()).isEqualTo("BA456");
    assertThat(projection.getName()).isEqualTo("Flight to London");
    
    // After fix, this should NOT throw exception
    assertDoesNotThrow(() -> {
      Point location = projection.getLocation();
      assertThat(location).isNotNull();
      assertThat(location.getX()).isEqualTo(-0.1276);
      assertThat(location.getY()).isEqualTo(51.5074);
    });
  }
}