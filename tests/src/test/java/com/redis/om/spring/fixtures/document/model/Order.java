package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Indexed;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class Order {

  @NonNull
  @Indexed
  private String skuNo;

  @NonNull
  private Double price;

}
