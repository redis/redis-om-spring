package com.redis.om.spring.fixtures.hash.model;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.fixtures.document.model.MyJavaEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(force = true)
@RedisHash
public class HashWithEnum {
  @Id
  private String id;

  @Indexed
  @NonNull
  private MyJavaEnum enumProp;
}
