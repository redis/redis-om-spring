package com.redis.om.spring.fixtures.document.model;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor(
    force = true
)
@RequiredArgsConstructor(
    staticName = "of"
)
@Document
public class DocWithSets {

  @Id
  private String id;

  @NonNull
  @Indexed
  private Set<Integer> theNumbers;

  @NonNull
  @Indexed
  private Set<Point> theLocations;
}
