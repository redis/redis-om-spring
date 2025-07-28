package valid;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.NumericIndexed;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Document
public class ValidDocumentNumericIndexed {
  @Id
  private String id;

  @NonNull
  @NumericIndexed(sortable = true)
  private Double price;

  @NonNull
  @NumericIndexed(alias = "qty")
  private Integer quantity;

  @NonNull
  @NumericIndexed(sortable = true, alias = "rating")
  private Float rating;
}