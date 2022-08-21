package com.redis.om.spring.annotations.document.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Document
public class User {
  @Id
  private String id;
  
  @NonNull
  @Indexed
  private String name;
  
  @NonNull
  @Indexed
  private Double lotteryWinnings;
  
  @Indexed
  private List<String> roles = new ArrayList<String>();
}
