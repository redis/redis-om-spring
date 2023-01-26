package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

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
  private List<String> roles = new ArrayList<>();
}
