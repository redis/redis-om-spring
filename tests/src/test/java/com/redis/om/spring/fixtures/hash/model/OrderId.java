package com.redis.om.spring.fixtures.hash.model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Embeddable
public class OrderId implements Serializable {
  private String orderNumber;
  private String locationId;
}
