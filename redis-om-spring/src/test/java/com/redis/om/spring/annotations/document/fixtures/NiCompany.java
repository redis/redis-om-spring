package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Searchable;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.geo.Point;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Document
public class NiCompany {
  @Id
  private String id;

  @NonNull
  @Searchable(sortable = true)
  private String name;

  @NonNull
  private Integer yearFounded;

  @NonNull
  private LocalDate lastValuation;

  @NonNull
  private Point location;

  private List<String> tags = new ArrayList<>();

  @NonNull
  private String email;

  private boolean publiclyListed;

  // audit fields

  @CreatedDate
  private Date createdDate;

  @LastModifiedDate
  private Date lastModifiedDate;
}
