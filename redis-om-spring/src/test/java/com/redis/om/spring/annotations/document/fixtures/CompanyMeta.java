package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Indexed;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
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
