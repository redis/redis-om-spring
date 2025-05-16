package com.redis.om.spring.fixtures.hash.model;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(
    force = true
)
@Embeddable
public class OrderId implements Serializable {
  private String orderNumber;
  private String locationId;
}
