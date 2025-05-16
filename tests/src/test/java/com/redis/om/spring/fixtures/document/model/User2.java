package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.*;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@NoArgsConstructor(
    force = true
)
@Document
public class User2 {
  @Id
  @Indexed
  private String id;

  @NonNull
  @Indexed
  private String name;

  @NonNull
  @Indexed
  private String address;

  @NonNull
  @Indexed
  private String addressComplement;
}
