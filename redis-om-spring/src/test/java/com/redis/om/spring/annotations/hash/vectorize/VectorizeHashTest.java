package com.redis.om.spring.annotations.hash.vectorize;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.Product;
import com.redis.om.spring.fixtures.hash.model.Product$;
import com.redis.om.spring.fixtures.hash.repository.ProductRepository;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.tuple.Fields;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.vectorize.Embedder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class VectorizeHashTest extends AbstractBaseEnhancedRedisTest {
  @Autowired
  ProductRepository repository;
  @Autowired
  EntityStream entityStream;

  @Autowired
  Embedder embedder;

  @BeforeEach
  void loadTestData() {
    if (repository.count() == 0) {
      repository.save(Product.of("cat", "classpath:/images/cat.jpg",
          "The cat (Felis catus) is a domestic species of small carnivorous mammal."));
      repository.save(Product.of("cat2", "classpath:/images/cat2.jpg",
          "It is the only domesticated species in the family Felidae and is commonly referred to as the domestic cat or house cat"));
      repository.save(
          Product.of("catdog", "classpath:/images/catdog.jpg",
                  "This is a picture of a cat and a dog together. And this sentence is just making the whole text longer."));
      repository.save(
          Product.of("face", "classpath:/images/face.jpg",
                  "Three years later, the coffin was still full of Jello. And this sentence is just making the whole text longer."));
      repository.save(Product.of("face2", "classpath:/images/face2.jpg",
          "The person box was packed with jelly many dozens of months later. And this sentence is just making the whole text longer."));
    }
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testImageIsVectorized() {
    Optional<Product> cat = repository.findFirstByName("cat");
    assertAll( //
        () -> assertThat(cat).isPresent(), //
        () -> assertThat(cat.get()).extracting("imageEmbedding").isNotNull(), //
        () -> assertThat(cat.get().getImageEmbedding()).hasSize(512 * Float.BYTES));
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testSentenceIsVectorized() {
    Optional<Product> cat = repository.findFirstByName("cat");
    assertAll( //
        () -> assertThat(cat).isPresent(), //
        () -> assertThat(cat.get()).extracting("sentenceEmbedding").isNotNull(), //
        () -> assertThat(cat.get().getSentenceEmbedding()).hasSize(384 * Float.BYTES));
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testKnnImageSimilaritySearch() {
    Product cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<Product> stream = entityStream.of(Product.class);

    List<Product> results = stream //
        .filter(Product$.IMAGE_EMBEDDING.knn(K, cat.getImageEmbedding())) //
        .sorted(Product$._IMAGE_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(5).map(Product::getName).containsExactly( //
        "cat", "cat2", "catdog", "face", "face2" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
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
        "cat", "cat2", "catdog", "face", "face2" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testKnnHybridSentenceSimilaritySearch() {
    Product cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<Product> stream = entityStream.of(Product.class);

    List<Product> results = stream //
        .filter(Product$.NAME.startsWith("cat")) //
        .filter(Product$.SENTENCE_EMBEDDING.knn(K, cat.getSentenceEmbedding())) //
        .sorted(Product$._SENTENCE_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(3).map(Product::getName).containsExactly( //
        "cat", "cat2", "catdog" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testKnnSentenceSimilaritySearchWithScores() {
    Product cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<Product> stream = entityStream.of(Product.class);

    List<Pair<Product, Double>> results = stream //
        .filter(Product$.SENTENCE_EMBEDDING.knn(K, cat.getSentenceEmbedding())) //
        .sorted(Product$._SENTENCE_EMBEDDING_SCORE) //
        .limit(K) //
        .map(Fields.of(Product$._THIS, Product$._SENTENCE_EMBEDDING_SCORE)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(5).map(Pair::getFirst).map(Product::getName)
            .containsExactly("cat", "cat2", "catdog", "face", "face2") //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testEmbedderCanVectorizeSentence() {
    Optional<Product> maybeCat = repository.findFirstByName("cat");
    assertThat(maybeCat).isPresent();
    Product cat = maybeCat.get();
    var catEmbedding = cat.getSentenceEmbedding();
    List<byte[]> embeddings = embedder.getTextEmbeddingsAsBytes(List.of(cat.getDescription()), Product$.DESCRIPTION);
    assertAll( //
        () -> assertThat(embeddings).isNotEmpty(), //
        () -> assertThat(embeddings.get(0)).isEqualTo(catEmbedding));
  }

  @Test
  @EnabledIf(
          expression = "#{@featureExtractor.isReady()}", //
          loadContext = true //
  )
  void testBulkEmbedMapEmbeddingsCorrectly() {
    List<Product> saveAllEntities = repository.saveAll(List.of(
                    Product.of("cat-saveAll", "classpath:/images/cat.jpg",
                            "The cat (Felis catus) is a domestic species of small carnivorous mammal."),
                    Product.of("cat2-saveAll", "classpath:/images/cat2.jpg",
                            "It is the only domesticated species in the family Felidae and is commonly referred to as the domestic cat or house cat"),
                    Product.of("catdog-saveAll", "classpath:/images/catdog.jpg", "This is a picture of a cat and a dog together. And this sentence is just making the whole text longer."),
                    Product.of("face-saveAll", "classpath:/images/face.jpg", "Three years later, the coffin was still full of Jello. And this sentence is just making the whole text longer."),
                    Product.of("face2-saveAll", "classpath:/images/face2.jpg", "The person box was packed with jelly many dozens of months later. And this sentence is just making the whole text longer.")
            )
    );

    saveAllEntities.forEach(saveAllEntity -> {
      Optional<Product> cat = repository.findFirstByName(saveAllEntity.getName().replace("-saveAll", ""));
      Optional<Product> catSaveAll = repository.findFirstByName(saveAllEntity.getName());

      assertAll( //
              () -> assertThat(cat).isPresent(), //
              () -> assertThat(cat.get().getSentenceEmbedding())
                      .withFailMessage("Sentence embeddings aren't the same for: " + saveAllEntity.getName() + " and " + cat.get().getName() + "\n" +
                              "Embedding 1: " + Arrays.toString(catSaveAll.get().getSentenceEmbedding()) + "\n" +
                              "Embedding 2: " + Arrays.toString(cat.get().getSentenceEmbedding()))
                      .isEqualTo(saveAllEntity.getSentenceEmbedding()),
              () -> assertThat(cat.get().getImageEmbedding())
                      .withFailMessage("Image embeddings aren't the same for: " + saveAllEntity.getName() + " and " + cat.get().getName())
                      .isEqualTo(saveAllEntity.getImageEmbedding())
      );
    });
  }
}
