package com.redis.om.spring.fixtures.hash.model;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.SchemaFieldType;
import com.redis.om.spring.indexing.DistanceMetric;
import com.redis.om.spring.indexing.VectorType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RedisHash
public class HashWithByteArrayFlatVector {
  @Id
  @NonNull
  private String id;

  @Indexed(//
           schemaFieldType = SchemaFieldType.VECTOR, //
           algorithm = VectorAlgorithm.FLAT, //
           type = VectorType.FLOAT32, //
           dimension = 100, //
           distanceMetric = DistanceMetric.L2, //
           initialCapacity = 300, m = 40
  )
  @NonNull
  private byte[] vector;

  @Indexed
  @NonNull
  private int number;
}
