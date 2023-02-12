package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

import java.util.List;

@Data
@Document
public class Person {
  @Id
  private String id;

  //@Indexed - Nope! This will cause a StackOverflow
  public Person mother;

  @Searchable
  public String name;

  @Indexed(sortable = true)
  public Point home;

  @Indexed(sortable = true)
  public Point work;

  @Indexed
  public PersonAddress personAddress;

  public boolean engineer;

  @Indexed(sortable = true)
  public int age;

  @Indexed(sortable = true)
  public double height;

  @Indexed
  public String[] nickNames;

  @Indexed
  public List<String> nickNamesList;

  @Indexed
  public String tagField;

  @Indexed(sortable = true)
  public int departmentNumber;

  @Indexed(sortable = true)
  public double sales;

  @Indexed(sortable = true)
  public double salesAdjustment;

  @Indexed(sortable = true)
  public long lastTimeOnline;

  @Searchable
  public String timeString;

  @Indexed
  public String email;

  @Indexed(sortable = true)
  public String unaggreatableField;

  @Indexed
  public String nullableStringField;
}
