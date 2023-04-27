package valid;

import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.annotations.JsonAdapter;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.serialization.gson.ListToStringAdapter;
import com.redis.om.spring.serialization.gson.SetToStringAdapter;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor
@AllArgsConstructor
@Document
public class ValidDocumentUnindexed {
  @Id
  private String id;
  
  @NonNull
  private String string;
  
  @NonNull
  private Boolean bool;

  @NonNull
  private Integer integerWrapper;
  
  @NonNull
  private int integerPrimitive;

  @NonNull
  private LocalDate localDate;
  @NonNull
  private LocalDateTime localDateTime;
  @NonNull
  private Date date;
  @NonNull
  private Point point;
  @NonNull
  private Ulid ulid;

  @Singular
  @JsonAdapter(SetToStringAdapter.class)
  private Set<String> setThings;

  @Singular
  @JsonAdapter(ListToStringAdapter.class)
  private List<String> listThings;

}

