package com.redis.om.spring.fixtures.hash.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class AccountId implements Serializable {
  private String accountNumber;
  private String accountType;
}
