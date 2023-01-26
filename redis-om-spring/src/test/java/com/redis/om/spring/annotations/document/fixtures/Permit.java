package com.redis.om.spring.annotations.document.fixtures;

import com.google.gson.annotations.JsonAdapter;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.DocumentScore;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.serialization.gson.SetToStringAdapter;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document("tst")
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
  @Indexed
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

  @NonNull
  @Indexed
  private List<Attribute> attrList;
}
