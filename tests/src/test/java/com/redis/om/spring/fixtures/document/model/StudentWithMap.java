package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class StudentWithMap {
  @Id
  private String id;
  
  @Indexed
  private String name;
  
  @Indexed
  private Map<String, Integer> courseGrades = new HashMap<>();
  
  @Indexed
  private Map<String, String> courseInstructors = new HashMap<>();
  
  public static StudentWithMap of(String name) {
    StudentWithMap student = new StudentWithMap();
    student.setName(name);
    student.setCourseGrades(new HashMap<>());
    student.setCourseInstructors(new HashMap<>());
    return student;
  }
}