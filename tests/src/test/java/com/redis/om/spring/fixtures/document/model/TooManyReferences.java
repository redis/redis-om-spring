package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@NoArgsConstructor(
    force = true
)
@Document
public class TooManyReferences {
  @Id
  private String id;

  @Indexed
  @NonNull
  private String name;

  @Reference
  private Ref ref1;
  @Reference
  private Ref ref2;
  @Reference
  private Ref ref3;
  @Reference
  private Ref ref4;
  @Reference
  private Ref ref5;
  @Reference
  private Ref ref6;
  @Reference
  private Ref ref7;
  @Reference
  private Ref ref8;
  @Reference
  private Ref ref9;
  @Reference
  private Ref ref10;
}
