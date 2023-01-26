package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.Data;

import java.util.List;

@Data
@Document
public class DocWithoutId {
  private String id;
  
  @Indexed
  private List<String> tags;
}
