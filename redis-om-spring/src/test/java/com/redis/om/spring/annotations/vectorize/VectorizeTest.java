package com.redis.om.spring.annotations.vectorize;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.Product;
import com.redis.om.spring.annotations.hash.fixtures.Product$;
import com.redis.om.spring.annotations.hash.fixtures.ProductRepository;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.vectorize.FeatureExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class VectorizeTest extends AbstractBaseEnhancedRedisTest {
  @Autowired ProductRepository repository;
  @Autowired EntityStream entityStream;

  @Autowired FeatureExtractor featureExtractor;

  @Autowired
  private ApplicationContext applicationContext;

  @BeforeEach void loadTestData() throws IOException {
    if (repository.count() == 0) {
      repository.save(Product.of("cat", "classpath:/images/cat.jpg",
          "The cat (Felis catus) is a domestic species of small carnivorous mammal."));
      repository.save(Product.of("cat2", "classpath:/images/cat2.jpg",
          "It is the only domesticated species in the family Felidae and is commonly referred to as the domestic cat or house cat"));
      repository.save(Product.of("catdog", "classpath:/images/catdog.jpg", "This is a picture of a cat and a dog together"));
      repository.save(Product.of("face", "classpath:/images/face.jpg", "Three years later, the coffin was still full of Jello."));
      repository.save(Product.of("face2", "classpath:/images/face2.jpg", "The person box was packed with jelly many dozens of months later."));
    }
  }

  @Test
  void testImageIsVectorized() {
    Optional<Product> cat = repository.findFirstByName("cat");
    assertAll( //
        () -> assertThat(cat).isPresent(), //
        () -> assertThat(cat.get()).extracting("imageEmbedding").isNotNull(), //
        () -> assertThat(cat.get().getImageEmbedding()).hasSize(512*Float.BYTES)
    );
  }

  @Test
  void testSentenceIsVectorized() {
    Optional<Product> cat = repository.findFirstByName("cat");
    assertAll( //
        () -> assertThat(cat).isPresent(), //
        () -> assertThat(cat.get()).extracting("sentenceEmbedding").isNotNull(), //
        () -> assertThat(cat.get().getSentenceEmbedding()).hasSize(768*Float.BYTES)
    );
  }

  @Test
  void testKnnImageSimilaritySearch() {
    Product cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<Product> stream = entityStream.of(Product.class);

    List<Product> results = stream //
        .filter(Product$.IMAGE_EMBEDDING.knn(K, cat.getImageEmbedding())) //
        .sorted(Product$._IMAGE_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(5).map(Product::getName).containsExactly("cat", "cat2",
        "face", "face2", "catdog");
  }

  @Test
  void testKnnSentenceSimilaritySearch() {
    Product cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<Product> stream = entityStream.of(Product.class);

    List<Product> results = stream //
        .filter(Product$.SENTENCE_EMBEDDING.knn(K, cat.getSentenceEmbedding())) //
        .sorted(Product$._SENTENCE_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(5).map(Product::getName).containsExactly( //
        "cat", "catdog", "cat2", "face", "face2" //
    );
  }
}
