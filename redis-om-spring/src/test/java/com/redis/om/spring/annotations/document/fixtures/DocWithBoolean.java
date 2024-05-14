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
public class DocWithBoolean {
  @NonNull
  @Indexed
  public Boolean indexedBoolean;
  @NonNull
  public Boolean nonIndexedBoolean;
  @Indexed
  public boolean indexedPrimitiveBoolean;
  public boolean nonIndexedPrimitiveBoolean;
  @Id
  private String id;
}
