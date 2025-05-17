package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;

import lombok.Data;

@SuppressWarnings(
  "SpellCheckingInspection"
)
@Data
@Document
public class DocWithCustomNameId {
  @Id
  private String identidad;
}
