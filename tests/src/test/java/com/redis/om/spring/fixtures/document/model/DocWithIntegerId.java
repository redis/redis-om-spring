package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Document
public class DocWithIntegerId {
  @Id
  private Integer id;
}
