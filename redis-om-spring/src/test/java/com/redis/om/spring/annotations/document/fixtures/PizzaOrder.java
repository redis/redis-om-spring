package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(staticName = "of")
@Document
public class PizzaOrder {
  @NonNull @Id
  private Integer id;

  @NonNull @Indexed(sortable = true)
  private String name;

  @NonNull @Indexed(sortable = true)
  private String size;

  @NonNull @Indexed(sortable = true)
  private double price;

  @NonNull @Indexed(sortable = true)
  private int quantity;

  @NonNull @Indexed(sortable = true)
  private Instant date;

  @NonNull @Indexed(sortable = true)
  private LocalDateTime created;
}
