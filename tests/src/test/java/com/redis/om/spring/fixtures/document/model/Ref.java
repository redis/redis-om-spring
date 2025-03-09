package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor(force = true)
@Document
public class Ref {
  @Id
  private String id;

  @NonNull
  private String name;
}
