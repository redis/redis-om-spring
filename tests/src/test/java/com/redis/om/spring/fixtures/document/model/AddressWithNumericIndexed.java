package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.NumericIndexed;
import lombok.Data;

@Data
public class AddressWithNumericIndexed {
  private String street;
  private String city;
  
  @NumericIndexed
  private Integer zipCode;
}