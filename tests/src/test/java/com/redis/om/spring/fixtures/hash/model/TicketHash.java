package com.redis.om.spring.fixtures.hash.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

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
@RedisHash("ticket_hash")
public class TicketHash {
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
