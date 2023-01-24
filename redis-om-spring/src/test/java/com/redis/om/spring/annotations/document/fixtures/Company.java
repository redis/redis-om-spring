package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Bloom;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.geo.Point;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Document
public class Company {
  @Id
  private String id;

  @NonNull
  @Searchable(sortable = true)
  private String name;

  @NonNull
  @Indexed
  private Integer yearFounded;

  @NonNull
  @Indexed
  private LocalDate lastValuation;

  @NonNull
  @Indexed
  private Point location;

  @Indexed
  private Set<String> tags = new HashSet<>();

  @NonNull
  @Indexed
  @Bloom(name = "bf_company_email", capacity = 100000, errorRate = 0.001)
  private String email;

  @Indexed
  private Set<CompanyMeta> metaList;

  @Indexed
  private boolean publiclyListed;

  // audit fields

  @CreatedDate
  private Date createdDate;

  @LastModifiedDate
  private Date lastModifiedDate;

  @Indexed
  private Set<Employee> employees;
}
