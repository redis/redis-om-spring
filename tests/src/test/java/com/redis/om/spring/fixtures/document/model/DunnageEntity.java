package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import jakarta.persistence.IdClass;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
@Builder
@Document("dunnage")
@IdClass(DunnageId.class)
public class DunnageEntity {
  @Id
  private String id;
  @Id
  private String plant;
  @Id
  @Searchable
  private String dunnageCode;

  @Indexed
  private String dunnageSuppcode;
  @Indexed
  private String dunnageSupplier;

  @Searchable
  private String material;
  @Searchable
  private String description;
  @Indexed
  private Integer weight;
  @Indexed
  private Float unitCost;
  @Indexed
  private String currency;
  @Searchable
  private String comment1;
  @Searchable
  private String comment2;
  @Searchable
  private String dunnagePiece;

  public static UUID generateId(String plant,
      String dunnageCode,
      String dunnageSuppcode) {
    return UUID.nameUUIDFromBytes(String.format("%s%s%s",
        plant,
        dunnageCode,
        dunnageSuppcode).getBytes());
  }
}
