package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

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
@Document("ticket")
public class Ticket {
  @Id
  private String id;

  @NonNull
  @Indexed
  private String team;

  @NonNull
  @Indexed
  private String priority;

  @NonNull
  @Searchable
  private String description;
}
