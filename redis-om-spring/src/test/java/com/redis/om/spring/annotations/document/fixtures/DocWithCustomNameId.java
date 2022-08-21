package com.redis.om.spring.annotations.document.fixtures;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;

import lombok.Data;

@Data
@Document
public class DocWithCustomNameId {
  @Id
  private String identidad;
}
