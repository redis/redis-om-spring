package com.redis.om.permits.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

import com.google.gson.annotations.JsonAdapter;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.DocumentScore;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.serialization.gson.SetToStringAdapter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "tst")
public class Permit {
  @Id
  private String id;

  @DocumentScore
  private double score;

  @NonNull
  @Indexed(sortable = true)
  private LocalDateTime permitTimestamp = LocalDateTime.now();

  @NonNull
  @Indexed
  private Address address;

  @NonNull
  @Searchable
  private String description;

  @NonNull
  @Searchable(sortable = true, nostem = true, weight = 20.0)
  private String buildingType;

  @NonNull
  @Indexed(separator = ",")
  @JsonAdapter(SetToStringAdapter.class)
  private Set<String> workType;

  @NonNull
  @Indexed(sortable = true)
  private Long constructionValue;

  @NonNull
  @Indexed
  private Point location;

  @NonNull
  @Indexed(alias = "status", arrayIndex = -1)
  private List<String> statusLog;
}
