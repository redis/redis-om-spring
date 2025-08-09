package com.redis.om.spring.fixtures.document.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

import com.github.f4b6a3.ulid.Ulid;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.annotations.IndexCreationMode;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@Document
@IndexingOptions(indexName = "ComprehensiveMapEntityIdx", creationMode = IndexCreationMode.DROP_AND_RECREATE)
public class ComprehensiveMapEntity {
  
  @Id
  private String id;

  @NonNull
  @Indexed
  private String name;

  // TAG field types (String, Boolean, UUID, Ulid, Enum)
  @Indexed
  private Map<String, String> stringValues = new HashMap<>();

  @Indexed  
  private Map<String, Boolean> booleanValues = new HashMap<>();

  @Indexed
  private Map<String, UUID> uuidValues = new HashMap<>();

  @Indexed
  private Map<String, Ulid> ulidValues = new HashMap<>();

  @Indexed
  private Map<String, TestEnum> enumValues = new HashMap<>();

  // NUMERIC field types (Integer, Long, Double, Float, BigDecimal, Date types)
  @Indexed
  private Map<String, Integer> integerValues = new HashMap<>();

  @Indexed
  private Map<String, Long> longValues = new HashMap<>();

  @Indexed
  private Map<String, Double> doubleValues = new HashMap<>();

  @Indexed
  private Map<String, Float> floatValues = new HashMap<>();

  @Indexed
  private Map<String, BigDecimal> bigDecimalValues = new HashMap<>();

  @Indexed
  private Map<String, LocalDateTime> localDateTimeValues = new HashMap<>();

  @Indexed
  private Map<String, LocalDate> localDateValues = new HashMap<>();

  @Indexed
  private Map<String, Date> dateValues = new HashMap<>();

  @Indexed
  private Map<String, Instant> instantValues = new HashMap<>();

  @Indexed
  private Map<String, OffsetDateTime> offsetDateTimeValues = new HashMap<>();

  // GEO field types (Point)
  @Indexed
  private Map<String, Point> pointValues = new HashMap<>();

  public enum TestEnum {
    OPTION_A, OPTION_B, OPTION_C
  }
}