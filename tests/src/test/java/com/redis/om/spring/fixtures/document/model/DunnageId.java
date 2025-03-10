package com.redis.om.spring.fixtures.document.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class DunnageId implements Serializable {
  private String id;
  private String plant;
  private String dunnageCode;
}
