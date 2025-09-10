package com.redis.om.spring.fixtures.document.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redis.om.spring.annotations.Indexed;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Position model for testing Map complex object queries with uppercase JSON fields.
 * This model uses @JsonProperty to map Java fields to uppercase JSON field names
 * and @Indexed(alias) to ensure the search index uses the correct field names.
 */
@Data
@NoArgsConstructor
public class PositionUC {
  
  @Indexed(alias = "POSITIONID")
  @JsonProperty("POSITIONID")
  private String positionId;
  
  @Indexed(alias = "CUSIP")
  @JsonProperty("CUSIP")
  private String cusip;
  
  @Indexed(alias = "QUANTITY")
  @JsonProperty("QUANTITY")
  private Integer quantity;
  
  // Additional fields that might be in the Position object
  @JsonProperty("ACCOUNTID")
  private String accountId;
  
  // Optional fields for more complete testing
  @Indexed
  @JsonProperty("DESCRIPTION")
  private String description;
  
  @Indexed
  @JsonProperty("MANAGER")
  private String manager;
  
  @Indexed
  @JsonProperty("PRICE")
  private BigDecimal price;
  
  @Indexed
  @JsonProperty("AS_OF_DATE")
  private LocalDate asOfDate;
}