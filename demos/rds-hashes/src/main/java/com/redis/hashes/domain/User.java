package com.redis.hashes.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@RedisHash
public class User {
  @Id 
  private String id;
  
  @Indexed @NonNull
  private String firstName;
  
  @Indexed 
  private String middleName;
  
  @Indexed @NonNull 
  private String lastName;
  
  @NonNull 
  @Reference
  private Role role;
}
