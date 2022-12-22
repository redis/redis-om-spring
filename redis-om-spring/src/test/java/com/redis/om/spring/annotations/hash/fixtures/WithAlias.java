package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.annotations.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisHash;

import java.util.Set;

@SuppressWarnings("SpellCheckingInspection") @Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash
public class WithAlias {
  @Id
  private String id;

  @NonNull
  @TextIndexed(alias = "texto")
  private String text;

  @NonNull
  @TagIndexed(alias = "etiquetas")
  private Set<String> tags;

  @NonNull
  @GeoIndexed(alias = "coordinadas")
  private Point location;

  @NonNull
  @NumericIndexed(alias = "numero")
  private Integer number;

  @NonNull
  @Indexed(alias = "direccion")
  private Direccion address;
}
