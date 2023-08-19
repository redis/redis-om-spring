package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Metamodel;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class DeepNestNonIndexed {
  @Id
  private String id;

  @NonNull
  @Indexed
  private String name;

  @NonNull
  @Metamodel
  private NestLevelNonIndexed1 nestLevel1;
}
