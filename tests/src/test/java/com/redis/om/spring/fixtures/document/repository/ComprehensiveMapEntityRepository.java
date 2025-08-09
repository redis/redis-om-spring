package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.ComprehensiveMapEntity;
import com.redis.om.spring.fixtures.document.model.ComprehensiveMapEntity.TestEnum;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.github.f4b6a3.ulid.Ulid;
import org.springframework.data.geo.Point;

public interface ComprehensiveMapEntityRepository extends RedisDocumentRepository<ComprehensiveMapEntity, String> {
  
  // Standard field queries
  List<ComprehensiveMapEntity> findByName(String name);
  
  // TAG field queries (exact matching) - using MapContains suffix for Map value queries
  List<ComprehensiveMapEntity> findByStringValuesMapContains(String value);
  List<ComprehensiveMapEntity> findByBooleanValuesMapContains(Boolean value);  
  List<ComprehensiveMapEntity> findByUuidValuesMapContains(UUID value);
  List<ComprehensiveMapEntity> findByUlidValuesMapContains(Ulid value);
  List<ComprehensiveMapEntity> findByEnumValuesMapContains(TestEnum value);

  // NUMERIC field queries (comparison operations) - using MapContains suffix for Map value queries
  List<ComprehensiveMapEntity> findByIntegerValuesMapContains(Integer value);
  List<ComprehensiveMapEntity> findByIntegerValuesMapContainsGreaterThan(Integer value);
  List<ComprehensiveMapEntity> findByIntegerValuesMapContainsLessThan(Integer value);
  
  List<ComprehensiveMapEntity> findByLongValuesMapContains(Long value);
  List<ComprehensiveMapEntity> findByLongValuesMapContainsGreaterThan(Long value);
  
  List<ComprehensiveMapEntity> findByDoubleValuesMapContains(Double value);
  List<ComprehensiveMapEntity> findByDoubleValuesMapContainsGreaterThan(Double value);
  
  List<ComprehensiveMapEntity> findByFloatValuesMapContains(Float value);
  List<ComprehensiveMapEntity> findByFloatValuesMapContainsGreaterThan(Float value);
  
  List<ComprehensiveMapEntity> findByBigDecimalValuesMapContains(BigDecimal value);
  List<ComprehensiveMapEntity> findByBigDecimalValuesMapContainsGreaterThan(BigDecimal value);
  
  List<ComprehensiveMapEntity> findByLocalDateTimeValuesMapContains(LocalDateTime value);
  List<ComprehensiveMapEntity> findByLocalDateTimeValuesMapContainsAfter(LocalDateTime value);
  
  List<ComprehensiveMapEntity> findByLocalDateValuesMapContains(LocalDate value);
  List<ComprehensiveMapEntity> findByLocalDateValuesMapContainsAfter(LocalDate value);
  
  List<ComprehensiveMapEntity> findByDateValuesMapContains(Date value);
  List<ComprehensiveMapEntity> findByDateValuesMapContainsAfter(Date value);
  
  List<ComprehensiveMapEntity> findByInstantValuesMapContains(Instant value);
  List<ComprehensiveMapEntity> findByInstantValuesMapContainsAfter(Instant value);
  
  List<ComprehensiveMapEntity> findByOffsetDateTimeValuesMapContains(OffsetDateTime value);
  List<ComprehensiveMapEntity> findByOffsetDateTimeValuesMapContainsAfter(OffsetDateTime value);

  // GEO field queries would typically use spatial queries, but for basic testing we'll use equality
  List<ComprehensiveMapEntity> findByPointValuesMapContains(Point value);
}