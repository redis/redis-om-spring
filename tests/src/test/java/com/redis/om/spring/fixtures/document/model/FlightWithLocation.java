package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

/**
 * Test model for issue #661 - NoSuchElementException when using Point in projection interface
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class FlightWithLocation {
  @Id
  private String id;
  
  @Indexed
  private String number;
  
  private String name;
  
  @Indexed
  private Point location;
  
  public FlightWithLocation(String number, String name, Point location) {
    this.number = number;
    this.name = name;
    this.location = location;
  }
}