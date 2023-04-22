package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class DeepList {
  @Id
  private String id;

  @Indexed
  @NonNull
  private String name;

  @Indexed
  @NonNull
  private List<NestLevel2> nestLevels;
}
