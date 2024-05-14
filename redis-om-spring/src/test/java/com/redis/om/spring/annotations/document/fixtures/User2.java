package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(force = true)
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
