package com.redis.om.spring.annotations.hash.fixtures;

import com.github.f4b6a3.ulid.Ulid;
import com.redis.om.spring.annotations.Indexed;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RedisHash
public class KitchenSink {
  @Id
  @EqualsAndHashCode.Include
  private String id;

  @NonNull
  @Indexed
  private LocalDate localDate;
  @NonNull
  @Indexed
  private LocalDateTime localDateTime;
  @NonNull
  @Indexed
  private Date date;
  @NonNull
  @Indexed
  private Point point;

  @Indexed
  private Ulid ulid;

  @Singular
  @Indexed
  private Set<String> setThings;

  @Singular
  @Indexed
  private List<String> listThings;
  
  private byte[] byteArray;
  
  private List<String[]> listOfStringArrays;
}
