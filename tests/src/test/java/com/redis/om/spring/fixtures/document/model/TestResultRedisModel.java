package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.*;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@NoArgsConstructor(
    force = true
)
@Document(
  "TestResultRedis"
)
public class TestResultRedisModel {
  @Id
  @Indexed
  @NonNull
  private Long id;

  @Indexed
  @NonNull
  String uuid;

  @Searchable
  @NonNull
  String filename;

  @Indexed
  @NonNull
  String status;
}
