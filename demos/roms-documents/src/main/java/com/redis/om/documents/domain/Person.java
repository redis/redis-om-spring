package com.redis.om.documents.domain;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.annotations.TextIndexed;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class Person {
  @Id
  private String id;

  @NonNull
  @TextIndexed
  private String firstName;

  @NonNull
  @TextIndexed
  private String lastName;

  @NonNull
  @TagIndexed
  private String email;
}
