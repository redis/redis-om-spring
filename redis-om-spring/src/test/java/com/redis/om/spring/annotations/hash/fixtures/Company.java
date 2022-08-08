package com.redis.om.spring.annotations.hash.fixtures;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.Bloom;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RedisHash
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
  private Set<String> tags = new HashSet<String>();

  @NonNull
  @Indexed
  @EqualsAndHashCode.Include
  @Bloom(name = "bf_company_email_2", capacity = 100000, errorRate = 0.001)
  private String email;

  @Indexed
  private boolean publiclyListed;

  // audit fields

  @CreatedDate
  private Date createdDate;

  @LastModifiedDate
  private Date lastModifiedDate;
}
