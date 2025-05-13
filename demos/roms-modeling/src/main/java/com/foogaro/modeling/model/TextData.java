package com.foogaro.modeling.model;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@Document
public class TextData {
  @Id
  private String id;
  @TagIndexed
  private String name;
  @TextIndexed(
      indexMissing = true, indexEmpty = true
  )
  private String description;
  @Indexed
  private int year;
  @NumericIndexed
  private double score;
  //    @Indexed
  @NumericIndexed
  private List<Double> measurements;
}
