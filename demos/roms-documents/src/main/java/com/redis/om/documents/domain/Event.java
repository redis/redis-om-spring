package com.redis.om.documents.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.*;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(
    staticName = "of"
)
@AllArgsConstructor
@Document
public class Event {
  @Id
  private String id;

  @NonNull
  @Searchable
  private String name;

  @Indexed
  private LocalDateTime beginDate;

  @Indexed
  private LocalDateTime endDate;

}
