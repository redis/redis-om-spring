package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Document(
  "froots"
)
@AllArgsConstructor(
    staticName = "of"
)
public class Fruit {

  @Id
  private long id;
  @Indexed
  private String name;
  @Indexed
  private String color;

}
