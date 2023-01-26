package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import lombok.Data;
import org.springframework.data.annotation.Id;

@SuppressWarnings("SpellCheckingInspection") @Data
@Document
public class DocWithCustomNameId {
  @Id
  private String identidad;
}
