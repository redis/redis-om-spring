package com.redis.om.spring.fixtures.document.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Searchable;

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
@Document(
  "sakila.language"
)
public class Language {

  @Id
  @NonNull
  private Integer languageId;
  @Searchable
  @NonNull
  private String name;
  private LocalDate lastUpdate;
}