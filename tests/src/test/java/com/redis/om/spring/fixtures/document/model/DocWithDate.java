package com.redis.om.spring.fixtures.document.model;

import java.util.Date;

import org.springframework.data.annotation.Id;

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
public class DocWithDate {
  @Id
  @NonNull
  private String id;

  @Indexed
  @NonNull
  private Date date;
}
