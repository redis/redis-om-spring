package valid;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.SchemaFieldType;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Document
public class ValidDocumentNumericIndexedComplex {
  @Id
  private String id;

  // Test case from the GitHub issue - but corrected (schemaFieldType is not valid for @NumericIndexed)
  @NumericIndexed(
    sortable = true
  )
  private Double issueReportedField;

  // Compare with @Indexed annotation
  @Indexed(
    schemaFieldType = SchemaFieldType.NUMERIC,
    sortable = true
  )
  private Double indexedField;

  // Test various numeric types with @NumericIndexed
  @NumericIndexed
  private Integer integerField;

  @NumericIndexed
  private Long longField;

  @NumericIndexed
  private Float floatField;

  @NumericIndexed
  private BigDecimal bigDecimalField;

  @NumericIndexed
  private BigInteger bigIntegerField;

  // Test primitive types
  @NumericIndexed
  private int primitiveInt;

  @NumericIndexed
  private long primitiveLong;

  @NumericIndexed
  private double primitiveDouble;

  @NumericIndexed
  private float primitiveFloat;
}