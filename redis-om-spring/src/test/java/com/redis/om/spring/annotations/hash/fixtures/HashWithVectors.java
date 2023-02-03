package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.DistanceMetric;
import com.redis.om.spring.VectorType;
import com.redis.om.spring.annotations.VectorIndexed;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import redis.clients.jedis.search.Schema.VectorField.VectorAlgo;

@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RedisHash
public class HashWithVectors {
  @Id
  private String id;

  @NonNull
  @VectorIndexed(algorithm = VectorAlgo.FLAT, type = VectorType.FLOAT32, dimension = 2, distanceMetric = DistanceMetric.L2)
  private String flat;

  @NonNull
  @VectorIndexed(algorithm = VectorAlgo.HNSW, type = VectorType.FLOAT32, dimension = 2, distanceMetric = DistanceMetric.L2)
  private String hnsw;
}
