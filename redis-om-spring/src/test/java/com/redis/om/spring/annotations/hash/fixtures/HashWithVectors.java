package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.DistanceMetric;
import com.redis.om.spring.VectorType;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.SchemaFieldType;
import com.redis.om.spring.annotations.VectorIndexed;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RedisHash
public class HashWithVectors {
  @Id
  private String id;

  @NonNull
  @VectorIndexed(algorithm = VectorAlgorithm.FLAT, type = VectorType.FLOAT32, dimension = 2, distanceMetric = DistanceMetric.L2)
  private String flat;

  @NonNull
  @VectorIndexed(algorithm = VectorAlgorithm.HNSW, type = VectorType.FLOAT32, dimension = 2, distanceMetric = DistanceMetric.L2)
  private String hnsw;

  @NonNull
  @Indexed(schemaFieldType = SchemaFieldType.VECTOR, algorithm = VectorAlgorithm.FLAT, type = VectorType.FLOAT32, dimension = 2, distanceMetric = DistanceMetric.L2)
  private String flat2;

  @NonNull
  @Indexed(schemaFieldType = SchemaFieldType.VECTOR, algorithm = VectorAlgorithm.HNSW, type = VectorType.FLOAT32, dimension = 2, distanceMetric = DistanceMetric.L2)
  private String hnsw2;
}
