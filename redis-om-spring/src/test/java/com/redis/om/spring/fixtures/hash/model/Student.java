package com.redis.om.spring.fixtures.hash.model;

import com.redis.om.spring.annotations.IndexCreationMode;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.IndexingOptions;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@IndexingOptions(creationMode = IndexCreationMode.SKIP_IF_EXIST, indexName = "myIndexStudent")
@RedisHash("my:key:space")
public class Student {

  @Id
  private Long id;

  @Indexed(alias = "User-Name")
  @NonNull
  private String userName;

  @Indexed(alias = "Event-Timestamp")
  @NonNull
  private LocalDateTime eventTimestamp;

}
