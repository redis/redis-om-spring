package com.redis.om.spring.fixtures.document.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
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
  "sakila.film"
)
public class Film {

  @Id
  @NonNull
  private Integer filmId;
  @Searchable
  @NonNull
  private String title;
  @Searchable
  private String description;
  @Indexed
  private LocalDate releaseYear;
  @Indexed
  @Reference
  private Language language;
  @Reference
  private Language originalLanguage;
  private Integer rentalDuration;
  private BigDecimal rentalRate;
  @Indexed
  private Integer length;
  private BigDecimal replacementCost;
  private String rating;
  private String specialFeatures;
  private Date lastUpdate;
}