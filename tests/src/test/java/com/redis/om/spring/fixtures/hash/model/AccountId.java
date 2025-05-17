package com.redis.om.spring.fixtures.hash.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(
    force = true
)
public class AccountId implements Serializable {
  private String accountNumber;
  private String accountType;
}
