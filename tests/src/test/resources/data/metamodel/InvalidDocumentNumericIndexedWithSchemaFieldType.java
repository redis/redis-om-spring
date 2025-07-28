package valid;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.SchemaFieldType;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Document
public class InvalidDocumentNumericIndexedWithSchemaFieldType {
  @Id
  private String id;

  // This is INVALID - @NumericIndexed does not have a schemaFieldType parameter
  // This will cause a compilation error
  /*
  @NumericIndexed(
    schemaFieldType = SchemaFieldType.NUMERIC, 
    sortable = true
  )
  private Double invalidField;
  */
  
  // This is the correct way to use @NumericIndexed
  @NumericIndexed(sortable = true)
  private Double validField;
}