package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(force = true)
@Document(filter = "@vehicleType==\"COUPE\"")
public class Vehicle {
  @Id
  private String id;

  @NonNull
  @Indexed
  private String model;

  @NonNull
  @Indexed
  private String brand;

  @NonNull
  @Indexed
  private VehicleType vehicleType;
}
