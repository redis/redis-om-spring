package com.redis.om.spring.fixtures.hash.model;

import jakarta.persistence.IdClass;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@RedisHash("accounts")
@IdClass(AccountId.class)
public class Account {
  @Id
  private String accountNumber;

  @Id
  private String accountType;

  private Double balance;
}
