package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.document.fixtures.MyJavaEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@RedisHash
public class HashWithEnum {
  @Id
  private String id;

  @Indexed
  @NonNull
  private MyJavaEnum enumProp;
}
