package valid;

import com.redis.om.spring.annotations.Document;
import lombok.*;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Document
public class IdOnly {
}

