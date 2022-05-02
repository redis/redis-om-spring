package com.redis.om.spring.annotations.document.fixtures;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Document
public class Metadata {
  @Id
  private String id;
  
  @Indexed
  private String employeeId; 
  
  @Indexed
  private String deptId;
}
