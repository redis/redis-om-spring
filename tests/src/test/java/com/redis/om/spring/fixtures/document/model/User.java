package com.redis.om.spring.fixtures.document.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.*;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@NoArgsConstructor(
    force = true
)
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
  private List<String> roles = new ArrayList<>();
}
