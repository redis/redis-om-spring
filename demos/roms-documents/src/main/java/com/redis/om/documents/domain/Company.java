package com.redis.om.documents.domain;

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
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class Company {
  @Id
  private String id;

  @NonNull
  @Searchable
  private String name;

  @Indexed
  private Set<String> tags = new HashSet<String>();

  @NonNull
  private String url;

  @NonNull
  @Indexed
  private Point location;

  @NonNull
  @Indexed
  private Integer numberOfEmployees;

  @NonNull
  @Indexed
  private Integer yearFounded;

  @NonNull
  @Indexed
  private Set<CompanyMeta> metaList;

  private boolean publiclyListed;
  
  // audit fields
  
  @CreatedDate
  private Date createdDate;
  
  @LastModifiedDate
  private Date lastModifiedDate;
}
