package com.redis.om.spring.fixtures.document.autodiscovery;

import com.redis.om.spring.annotations.Document;
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
