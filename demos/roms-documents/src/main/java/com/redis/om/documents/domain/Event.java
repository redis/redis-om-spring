package com.redis.om.documents.domain;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
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
