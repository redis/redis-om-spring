package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.*;

/**
 * Model for testing Issue #677: Indexed subfields for @Reference fields.
 * This entity references Owner with @Reference @Indexed, and should have
 * metamodel fields like OWNER_NAME and OWNER_EMAIL generated to allow
 * searching on the referenced entity's indexed/searchable fields.
 */
@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(force = true)
@Document("refvehicle")
public class RefVehicle {
  @Id
  private String id;

  @NonNull
  @Searchable
  private String model;

  @NonNull
  @Indexed
  private String brand;

  @NonNull
  @Reference
  @Indexed
  private Owner owner;
}
