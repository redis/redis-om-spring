package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Indexed;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Position {
  @Indexed
  private String cusip;
  
  @Indexed
  private String description;
  
  @Indexed
  private String manager;
  
  @Indexed
  private Integer quantity;
  
  @Indexed
  private BigDecimal price;
  
  @Indexed
  private LocalDate asOfDate;
}