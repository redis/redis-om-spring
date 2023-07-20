package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.annotations.AutoComplete;
import com.redis.om.spring.annotations.Bloom;
import com.redis.om.spring.annotations.Cuckoo;
import com.redis.om.spring.annotations.Indexed;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("people2")
public class Person2 {

  @Id 
  String id;
  
  @NonNull 
  String name;
  
  @NonNull 
  @AutoComplete 
  @Cuckoo(name = "cf_person_email", capacity = 100000)
  String email;
  
  @NonNull
  @Cuckoo(capacity = 100000)
  String nickname;
}