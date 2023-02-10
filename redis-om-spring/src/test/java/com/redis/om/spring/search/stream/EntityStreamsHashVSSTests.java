package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.*;
import com.redis.om.spring.tuple.Fields;
import com.redis.om.spring.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("SpellCheckingInspection") class EntityStreamsHashVSSTests extends AbstractBaseEnhancedRedisTest {
  @Autowired HashWithByteArrayHNSWVectorRepository hnswRepository;
  @Autowired HashWithByteArrayFlatVectorRepository flatRepository;

  @Autowired EntityStream entityStream;
  
  @BeforeEach
  void cleanUp() {
    if (hnswRepository.count() == 0 && flatRepository.count() == 0) {
      int amount = 300;
      int dimension = 100;

      List<HashWithByteArrayHNSWVector> hashWithByteArrayHNSWVectors = new ArrayList<>();
      List<HashWithByteArrayFlatVector> hashWithByteArrayFlatVectors = new ArrayList<>();

      for (int i = 0; i < amount; i++) {
        // hash key
        float[] vec = new float[dimension];
        float val = (float) i / (dimension + i);
        Arrays.fill(vec, val);
        hashWithByteArrayHNSWVectors.add(HashWithByteArrayHNSWVector.of("doc:" + i, floatToByte(vec), i));
        hashWithByteArrayFlatVectors.add(HashWithByteArrayFlatVector.of("doc:" + i, floatToByte(vec), i));
      }
      hnswRepository.saveAll(hashWithByteArrayHNSWVectors);
      flatRepository.saveAll(hashWithByteArrayFlatVectors);
    }
  }

  /**
   * A simple FT.SEARCH (only vector similarity)
   * Get top 4 documents where the vector field is closest to [1.4e-30f, 1.4e-30f,...]
   *
   * FT.SEARCH QUERY = `"(*)=>[KNN $K @vector $vector_blob]"`
   */
  @Test
  void testPureKNNSearchWithHNSWVectorIndex() {
    float[] e = new float[100];
    Arrays.fill(e, 1.4e-30f);

    int K = 4;

    SearchStream<HashWithByteArrayHNSWVector> stream = entityStream.of(HashWithByteArrayHNSWVector.class);

    List<HashWithByteArrayHNSWVector> results = stream //
        .filter(HashWithByteArrayHNSWVector$.VECTOR.knn(K, floatToByte(e))) //
        .sorted(HashWithByteArrayHNSWVector$._VECTOR_SCORE)
        .limit(K)
        .collect(Collectors.toList());

    assertThat(results).hasSize(4).map(HashWithByteArrayHNSWVector::getId).containsExactly("doc:132", "doc:12", "doc:75", "doc:240");
  }

  /**
   * A simple FT.SEARCH (only vector similarity)
   * Get top 4 documents where the vector field is closest to [1.4e-30f, 1.4e-30f,...]
   *
   * FT.SEARCH QUERY = `"(*)=>[KNN $K @vector $vector_blob]"`
   */
  @Test
  void testPureKNNSearchWithHNSWVectorIndexOnlyNumberAndDistances() {
    float[] e = new float[100];
    Arrays.fill(e, 1.4e-30f);

    int K = 4;

    SearchStream<HashWithByteArrayHNSWVector> stream = entityStream.of(HashWithByteArrayHNSWVector.class);

    List<Pair<Integer,Double>> results = stream //
        .filter(HashWithByteArrayHNSWVector$.VECTOR.knn(K, floatToByte(e))) //
        .sorted(HashWithByteArrayHNSWVector$._VECTOR_SCORE) //
        .limit(K) //
        .map(Fields.of(HashWithByteArrayHNSWVector$.NUMBER, HashWithByteArrayHNSWVector$._VECTOR_SCORE)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(4), //
        () -> assertThat(results).map(Pair::getFirst).containsExactly(132, 12, 75, 240), //
        () -> assertThat(results).map(Pair::getSecond).containsExactly(1.08511912913e-05, 4.01816287194e-05, 4.01816287194e-05, 4.18252566305e-05) //
    );
  }

  /**
   * Another Hybrid Query FT.SEARCH (vector and non-vector search criteria)
   * Get top 5 articles with
   * number value is between 0 and 20
   * OR
   * number value is between indexsize-20 and indexsize
   */
  @Test
  void testHybridKNNSearch() {
    float[] e = new float[100];
    Arrays.fill(e, 1.4e-30f);

    int K = 5;
    int NUMBER_ARTICLES = 300;

    SearchStream<HashWithByteArrayHNSWVector> stream = entityStream.of(HashWithByteArrayHNSWVector.class);

    List<Pair<Integer,Double>> results = stream //
        .filter(HashWithByteArrayFlatVector$.NUMBER.between(0, 20) //
            .or(HashWithByteArrayFlatVector$.NUMBER.between(NUMBER_ARTICLES - 20, NUMBER_ARTICLES))) //
        .filter(HashWithByteArrayHNSWVector$.VECTOR.knn(K, floatToByte(e))) //
        .sorted(HashWithByteArrayHNSWVector$._VECTOR_SCORE) //
        .limit(K) //
        .map(Fields.of(HashWithByteArrayHNSWVector$.NUMBER, HashWithByteArrayHNSWVector$._VECTOR_SCORE)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(5), //
        () -> assertThat(results).map(Pair::getFirst).containsExactly(12, 289, 15, 280, 2), //
        () -> assertThat(results).map(Pair::getSecond).containsExactly(4.01816287194e-05, 4.19937859988e-05, 4.19969328505e-05, 4.19990610681e-05, 4.19991047238e-05) //
    );
  }

  /**
   * Get top 5 documents with number value is between 0 and 100
   * FT.SEARCH QUERY = @number:[0 100]=>[KNN $K @my_vector $BLOB AS scores]
   */
  @Test
  void testHybridKNNSearchWithFlatVectorIndex() {
    float[] e = new float[100];
    Arrays.fill(e, 1.4e-30f);

    int K = 5;

    SearchStream<HashWithByteArrayFlatVector> stream = entityStream.of(HashWithByteArrayFlatVector.class);

    List<HashWithByteArrayFlatVector> results = stream //
        .filter(HashWithByteArrayFlatVector$.NUMBER.between(0, 100)) //
        .filter(HashWithByteArrayFlatVector$.VECTOR.knn(K, floatToByte(e))) //
        .sorted(HashWithByteArrayFlatVector$._VECTOR_SCORE)
        .limit(K)
        .collect(Collectors.toList());

    assertThat(results).hasSize(5).map(HashWithByteArrayFlatVector::getId).containsExactly("doc:12", "doc:75", "doc:71", "doc:15", "doc:2");
  }

  private byte[] floatToByte(float[] input) {
    byte[] ret = new byte[input.length*4];
    for (int x = 0; x < input.length; x++) {
      ByteBuffer.wrap(ret, x*4, 4).putFloat(input[x]);
    }
    return ret;
  }
}
