package com.redis.om.spring.annotations.document.fixtures;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.geo.Point;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document("company")
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
  private Point location;

  @Indexed
  private Set<String> tags = new HashSet<String>();

  private boolean publiclyListed;

  // audit fields

  @CreatedDate
  private Date createdDate;

  @LastModifiedDate
  private Date lastModifiedDate;
}
