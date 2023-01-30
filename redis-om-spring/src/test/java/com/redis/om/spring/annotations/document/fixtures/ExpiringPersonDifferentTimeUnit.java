package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@Document(timeToLive = 5)
public class ExpiringPersonDifferentTimeUnit {
  @Id String id;
  @NonNull
  String name;
  
  @NonNull
  @TimeToLive(unit = TimeUnit.DAYS) Long ttl;
}
