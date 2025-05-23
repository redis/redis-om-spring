package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.*;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@NoArgsConstructor
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@Document
public class DeepNest {
  @Id
  private String id;

  @Indexed
  @NonNull
  private String name;

  @Indexed
  @NonNull
  private NestLevel1 nestLevel1;
}
