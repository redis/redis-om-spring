import com.redis.om.spring.annotations.Document;
import lombok.*;
import org.springframework.data.annotation.Id;

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