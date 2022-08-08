package com.redis.om.spring.annotations.document.fixtures;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;

import lombok.Data;

@Data
@Document
public class DocWithIntegerId {
  @Id
  private Integer id;
}
