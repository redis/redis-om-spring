package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentWithMixedTypes {

  @Id
  private String id;

  @Indexed
  private String name;

  @Indexed
  private Integer age;

  @Indexed
  private Double salary;

  @Indexed
  private Boolean active;

  @Indexed
  private LocalDate birthDate;

  private String description;
}