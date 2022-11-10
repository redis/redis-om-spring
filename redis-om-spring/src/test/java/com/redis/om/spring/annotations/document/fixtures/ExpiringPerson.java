package com.redis.om.spring.annotations.document.fixtures;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.TimeToLive;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@Document(timeToLive = 5)
public class ExpiringPerson {
  @Id String id;
  @NonNull @Indexed
  String name;
  
  @NonNull
  @TimeToLive Long ttl;
}
