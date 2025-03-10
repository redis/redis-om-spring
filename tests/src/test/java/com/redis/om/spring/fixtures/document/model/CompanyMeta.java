package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Indexed;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(staticName = "of")
public class CompanyMeta {

  @Indexed
  @NonNull
  private String stringValue;

  @Indexed
  @NonNull
  private Integer numberValue;

  @Indexed
  @NonNull
  private Set<String> tagValues;
}
