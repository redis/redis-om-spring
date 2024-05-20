package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.annotations.Indexed;
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
@RedisHash
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
