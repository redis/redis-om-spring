package com.redis.om.spring.fixtures.hash.model;

import jakarta.persistence.EmbeddedId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
//@RedisHash
public class Order {
  @EmbeddedId
  private OrderId orderId;
  private Double total;
}
