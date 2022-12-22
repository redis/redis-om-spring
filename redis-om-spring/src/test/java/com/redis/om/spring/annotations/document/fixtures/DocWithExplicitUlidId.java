package com.redis.om.spring.annotations.document.fixtures;

import com.github.f4b6a3.ulid.Ulid;
import com.redis.om.spring.annotations.Document;
import lombok.Data;
import org.springframework.data.annotation.Id;

@SuppressWarnings("SpellCheckingInspection") @Data
@Document
public class DocWithExplicitUlidId {
  @Id
  private Ulid id;
}
