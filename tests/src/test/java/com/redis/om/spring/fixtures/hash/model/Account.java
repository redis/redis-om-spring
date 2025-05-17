package com.redis.om.spring.fixtures.hash.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.IdClass;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(
    force = true
)
@RedisHash(
  "accounts"
)
@IdClass(
  AccountId.class
)
public class Account {
  @Id
  private String accountNumber;

  @Id
  private String accountType;

  private Double balance;
}
