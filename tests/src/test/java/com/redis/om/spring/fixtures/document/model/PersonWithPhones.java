package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.SchemaFieldType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@Document
public class PersonWithPhones {
  @Id
  private String id;
  
  @NonNull
  @Indexed
  private String name;
  
  @NonNull
  @Indexed(schemaFieldType = SchemaFieldType.NESTED)
  private List<Phone> phonesList;
}