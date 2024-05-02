package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Searchable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor(force = true)
@Document("sakila.language")
public class Language {

  @Id
  @NonNull
  private Integer languageId;
  @Searchable
  @NonNull
  private String name;
  private LocalDate lastUpdate;
}