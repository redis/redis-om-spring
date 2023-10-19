package com.redis.om.vss.domain;

import com.redis.om.spring.DistanceMetric;
import com.redis.om.spring.VectorType;
import com.redis.om.spring.annotations.EmbeddingType;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.SchemaFieldType;
import com.redis.om.spring.annotations.Vectorize;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import redis.clients.jedis.search.Schema.VectorField.VectorAlgo;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor
@RedisHash
public class Product {
  @Id
  private String id;

  @Indexed
  @NonNull
  private String gender;

  @Indexed
  @NonNull
  private String masterCategory;

  @Indexed
  @NonNull
  private String subCategory;

  @Indexed
  @NonNull
  private String articleType;

  @Indexed
  @NonNull
  private String baseColour;

  @Indexed
  @NonNull
  private String season;

  @Indexed
  @NonNull
  private String year;

  @Indexed
  @NonNull
  private String usage;

  @Indexed
  @NonNull
  private String productDisplayName;


  @Indexed(//
      schemaFieldType = SchemaFieldType.VECTOR, //
      algorithm = VectorAlgo.HNSW, //
      type = VectorType.FLOAT32, //
      dimension = 512, //
      distanceMetric = DistanceMetric.COSINE, //
      initialCapacity = 10
  )
  private byte[] imageEmbedding;

  @Vectorize(destination = "imageEmbedding", embeddingType = EmbeddingType.IMAGE)
  @NonNull
  private String imagePath;

  @Indexed(//
      schemaFieldType = SchemaFieldType.VECTOR, //
      algorithm = VectorAlgo.HNSW, //
      type = VectorType.FLOAT32, //
      dimension = 768, //
      distanceMetric = DistanceMetric.COSINE, //
      initialCapacity = 10
  )
  private byte[] sentenceEmbedding;

  @Vectorize(destination = "sentenceEmbedding", embeddingType = EmbeddingType.SENTENCE)
  @NonNull
  private String productText;

  public static Product fromCSV(String line, boolean useLocalImages) {
    // CSV columns
    // id, gender, masterCategory, subCategory, articleType, baseColour, season, year, usage, productDisplayName, link
    String[] values = line.split(",");
    String id = values[0];
    String gender = values[1];
    String masterCategory = values[2];
    String subCategory = values[3];
    if (subCategory.equalsIgnoreCase("Innerwear")) return null;
    String articleType = values[4];
    String baseColour = values[5];
    String season = values[6];
    String year = values[7];
    String usage  = values[8];
    String productDisplayName = values[9];
    String imagePath = useLocalImages ? "classpath:/static/product-images/" + id + ".jpg" : values[10];
    String productText = Stream.of(productDisplayName, "category", masterCategory, "subcategory", subCategory, "color", baseColour, "gender", gender).map(String::toLowerCase).collect(
        Collectors.joining(" "));

    Product p = Product.of(gender, masterCategory, subCategory, articleType, baseColour, season, year, usage, productDisplayName, imagePath, productText);
    p.setId(id);
    return p;
  }

  public String getProductImage() {
    return imagePath.startsWith("classpath:") ? "product-images/" + id + ".jpg" : imagePath;
  }
}