package com.redis.om.spring.fixtures.document.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.TagIndexed;

import lombok.*;

/**
 * Model for testing Issue #677: Indexed subfields for @Reference fields.
 * The Owner entity has searchable/indexed fields that should be accessible
 * from entities that reference it.
 *
 * This model also tests various index attribute options:
 * - @Searchable with indexMissing/indexEmpty
 * - @Indexed with sortable
 * - @TagIndexed with indexMissing/indexEmpty
 * - @NumericIndexed with sortable
 * - @Indexed on Boolean
 * - @Indexed on Date/time types (LocalDate, LocalDateTime)
 * - @Indexed on UUID
 */
@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(force = true)
@Document("owner")
public class Owner {
  @Id
  private String id;

  @NonNull
  @Searchable(indexMissing = true, indexEmpty = true)
  private String name;

  @NonNull
  @Indexed(sortable = true)
  private String email;

  @TagIndexed(indexMissing = true, indexEmpty = true)
  private String category;

  @NumericIndexed(sortable = true)
  private Integer age;

  @Indexed(sortable = true, indexMissing = true, indexEmpty = true)
  private Boolean active;

  @Indexed(sortable = true)
  private LocalDate birthDate;

  @Indexed(sortable = true)
  private LocalDateTime createdAt;

  @Indexed
  private UUID externalId;
}
