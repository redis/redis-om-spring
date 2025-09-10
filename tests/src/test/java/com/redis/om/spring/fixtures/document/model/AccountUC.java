package com.redis.om.spring.fixtures.document.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.IndexingOptions;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Account model for testing Map complex object queries with uppercase JSON fields.
 * This model simulates the VOYA data structure where JSON fields are uppercase
 * but Java fields follow standard camelCase conventions.
 */
@Data
@NoArgsConstructor
@Document
@IndexingOptions(indexName = "AccountUCIdx")
public class AccountUC {
  
  @Id
  @JsonProperty("ACCOUNTID")
  private String accountId;
  
  @Indexed(alias = "ACC_NAME")
  @JsonProperty("ACC_NAME")
  private String accountName;
  
  @Indexed(alias = "MANAGER")
  @JsonProperty("MANAGER")
  private String manager;
  
  @Indexed(alias = "ACC_VALUE")
  @JsonProperty("ACC_VALUE")
  private BigDecimal accountValue;
  
  // Additional fields from VOYA data
  @Indexed
  @JsonProperty("COMMISSION_RATE")
  private Integer commissionRate;
  
  @Indexed
  @JsonProperty("CASH_BALANCE")
  private BigDecimal cashBalance;
  
  @JsonProperty("DAY_CHANGE")
  private BigDecimal dayChange;
  
  @JsonProperty("UNREALIZED_GAIN_LOSS")
  private BigDecimal unrealizedGainLoss;
  
  @JsonProperty("MANAGER_FNAME")
  private String managerFirstName;
  
  @JsonProperty("MANAGER_LNAME")
  private String managerLastName;
  
  // Map with complex object values containing indexed fields
  // Note: The field name is "Positions" with capital P to match VOYA JSON
  @Indexed
  @JsonProperty("Positions")
  private Map<String, PositionUC> positions = new HashMap<>();
  
  // Alternative for testing: lowercase field name with uppercase JSON property
  // This would be used if we want to keep Java conventions but map to uppercase JSON
  // @Indexed
  // @JsonProperty("Positions")
  // private Map<String, PositionUC> positions = new HashMap<>();
}