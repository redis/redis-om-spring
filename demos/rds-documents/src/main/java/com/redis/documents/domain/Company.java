package com.redis.documents.domain;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

import com.redis.spring.annotations.Document;
import com.redis.spring.annotations.GeoIndexed;
import com.redis.spring.annotations.NumericIndexed;
import com.redis.spring.annotations.TagIndexed;
import com.redis.spring.annotations.TextIndexed;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class Company {
  @Id
  private String id;

  @NonNull
  @TextIndexed
  private String name;

  @TagIndexed
  private Set<String> tags = new HashSet<String>();

  @NonNull
  private String url;

  @NonNull
  @GeoIndexed
  private Point location;

  @NonNull
  @NumericIndexed
  private Integer numberOfEmployees;

  @NonNull
  @NumericIndexed
  private Integer yearFounded;

  private boolean publiclyListed;
}
