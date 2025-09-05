package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.IndexingOptions;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@Document
@IndexingOptions(indexName = "AccountWithPositionsIdx")
public class AccountWithPositions {
  @Id
  private String id;
  
  @Indexed
  private String accountNumber;
  
  @Indexed
  private String accountHolder;
  
  @Indexed
  private BigDecimal totalValue;
  
  // Map with complex object values containing indexed fields
  @Indexed
  private Map<String, Position> positions = new HashMap<>();
}