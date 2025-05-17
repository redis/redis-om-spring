package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.fixtures.hash.model.AccountId;

import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(
    force = true
)
@Document(
  "daccounts"
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
