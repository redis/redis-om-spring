import org.springframework.data.annotation.Id;
import com.redis.om.spring.annotations.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor
@AllArgsConstructor
@Document
public class ValidDocumentUnindexedWoPackage {
  @Id
  private String id;

  @NonNull
  private String name;
}