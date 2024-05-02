package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Document("froots")
@AllArgsConstructor(staticName = "of")
public class Fruit {

  @Id
  private long id;
  @Indexed
  private String name;
  @Indexed
  private String color;

}
