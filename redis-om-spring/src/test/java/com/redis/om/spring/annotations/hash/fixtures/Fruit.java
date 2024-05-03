package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.annotations.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@RedisHash("hfroots")
@AllArgsConstructor(staticName = "of")
public class Fruit {

  @Id
  private long id;
  @Indexed
  private String name;
  @Indexed
  private String color;

}
