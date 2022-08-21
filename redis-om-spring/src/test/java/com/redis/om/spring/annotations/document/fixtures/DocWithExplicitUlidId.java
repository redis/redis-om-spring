package com.redis.om.spring.annotations.document.fixtures;

import org.springframework.data.annotation.Id;

import com.github.f4b6a3.ulid.Ulid;
import com.redis.om.spring.annotations.Document;

import lombok.Data;

@Data
@Document
public class DocWithExplicitUlidId {
  @Id
  private Ulid id;
}
