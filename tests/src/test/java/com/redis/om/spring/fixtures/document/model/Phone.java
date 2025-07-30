package com.redis.om.spring.fixtures.document.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Phone {
  @NonNull
  private String number;
  @NonNull 
  private String type; // e.g., "home", "work", "mobile"
}