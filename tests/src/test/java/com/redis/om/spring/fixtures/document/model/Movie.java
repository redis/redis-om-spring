package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Document
public class Movie {

  @Id
  private long id;

  @Indexed
  private String node;
}
