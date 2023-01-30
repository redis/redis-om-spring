package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Document
public class DocWithIntegerId {
  @Id
  private Integer id;
}
