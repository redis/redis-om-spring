package com.redis.om.spring.fixtures.hash.model;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.Indexed;

import lombok.*;

@Data
@NoArgsConstructor(
    force = true
)
@RequiredArgsConstructor(
    staticName = "of"
)
@RedisHash
public class HashWithAllTheNumerics {
  @Id
  @NonNull
  private String id;

  @NonNull
  @Indexed
  private Float afloat;

  @NonNull
  @Indexed
  private Double adouble;

  @NonNull
  @Indexed
  private BigInteger abigInteger;

  @NonNull
  @Indexed
  private BigDecimal abigDecimal;
}
