package com.redis.om.spring.annotations.hash.fixtures;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.GeoIndexed;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.annotations.TextIndexed;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
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
