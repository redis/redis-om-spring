package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.github.f4b6a3.ulid.Ulid;
import com.redis.om.spring.annotations.Document;

import lombok.Data;

@SuppressWarnings(
  "SpellCheckingInspection"
)
@Data
@Document
public class DocWithExplicitUlidId {
  @Id
  private Ulid id;
}
