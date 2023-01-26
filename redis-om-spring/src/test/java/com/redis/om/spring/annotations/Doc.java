package com.redis.om.spring.annotations;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document 
public class Doc {
  @Id
  private String id;

  @NonNull
  private String name;
}
