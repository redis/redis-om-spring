package valid;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.TagIndexed;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Document
public class ValidDocumentNumericIndexedId {
  @Id
  @NumericIndexed
  private Long id;

  @NonNull
  @TagIndexed
  private String name;

  @NonNull
  @NumericIndexed
  private Integer value;
}