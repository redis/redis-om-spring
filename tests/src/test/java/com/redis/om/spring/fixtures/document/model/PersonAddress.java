package com.redis.om.spring.fixtures.document.model;

import com.github.f4b6a3.ulid.Ulid;
import com.redis.om.spring.annotations.Indexed;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;

import java.util.UUID;

@Data
@RequiredArgsConstructor(staticName = "of")
public class PersonAddress {
  private String streetName;
  private String zipCode;
  @Indexed
  private AddressType addressType;
  @Indexed
  private String city;
  @Indexed
  private String state;
  // @Indexed - nope stackOverflow
  private PersonAddress forwardingAddress;
  @Indexed
  private Point location;
  @Indexed
  private int houseNumber;
  @Indexed
  private boolean bool;
  @Indexed
  private Ulid ulid;
  @Indexed
  private UUID guid;
}
