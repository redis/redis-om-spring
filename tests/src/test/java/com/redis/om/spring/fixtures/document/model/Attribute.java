package com.redis.om.spring.fixtures.document.model;

import java.util.List;

import com.redis.om.spring.annotations.Indexed;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
public class Attribute {

  @NonNull
  @Indexed
  private String name;

  @NonNull
  @Indexed
  private String value;

  @NonNull
  @Indexed
  private List<Order> orderList;
}
