package valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.redis.om.spring.annotations.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

import com.google.gson.annotations.JsonAdapter;
import com.redis.om.spring.serialization.gson.SetToStringAdapter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import valid.Address;

@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document("tst")
public class ValidDocumentIndexedNested {
  @Id
  private String id;

  @NonNull
  @Indexed
  private Address address;
}
