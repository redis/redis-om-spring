package com.redis.om.spring.fixtures.document.model;

import java.util.List;

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
