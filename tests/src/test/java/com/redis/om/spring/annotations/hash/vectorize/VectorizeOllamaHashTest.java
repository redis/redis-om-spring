package com.redis.om.spring.annotations.hash.vectorize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.HashWithOllamaEmbedding;
import com.redis.om.spring.fixtures.hash.model.HashWithOllamaEmbedding$;
import com.redis.om.spring.fixtures.hash.repository.HashWithOllamaEmbeddingRepository;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.tuple.Fields;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.vectorize.Embedder;

@DisabledIfEnvironmentVariable(
    named = "GITHUB_ACTIONS", matches = "true"
)
@DisabledIf(
    expression = "#{!T(com.redis.om.spring.util.Utils).isOllamaRunning()}",
    reason = "Disabled if Ollama is not running locally"
)
class VectorizeOllamaHashTest extends AbstractBaseEnhancedRedisTest {
  @Autowired
  HashWithOllamaEmbeddingRepository repository;

  @Autowired
  EntityStream entityStream;

  @Autowired
  Embedder embedder;

  @BeforeEach
  void loadTestData() throws IOException {
    if (repository.count() == 0) {
      repository.save(HashWithOllamaEmbedding.of("cat",
          "The cat is a small domesticated carnivorous mammal with soft fur, a short snout, and retractable claws."));
      repository.save(HashWithOllamaEmbedding.of("dog",
          "A dog is a domesticated mammal of the family Canidae, characterized by its loyalty, playfulness, and friendly demeanor."));
      repository.save(HashWithOllamaEmbedding.of("lion",
          "The lion is a large cat of the genus Panthera native to Africa and India, known for its muscular body, deep roar, and mane on the male."));
      repository.save(HashWithOllamaEmbedding.of("elephant",
          "Elephants are the largest existing land animals, characterized by their long trunk, tusks, and large ears."));
      repository.save(HashWithOllamaEmbedding.of("giraffe",
          "The giraffe is an African even-toed ungulate mammal, the tallest living terrestrial animal, and the largest ruminant."));
    }
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testSentenceIsVectorized() {
    Optional<HashWithOllamaEmbedding> cat = repository.findFirstByName("cat");
    assertAll( //
        () -> assertThat(cat).isPresent(), //
        () -> assertThat(cat.get()).extracting("textEmbedding").isNotNull(), //
        () -> assertThat(cat.get().getTextEmbedding()).hasSize(4096 * Float.BYTES) //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testKnnSentenceSimilaritySearch() {
    HashWithOllamaEmbedding cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<HashWithOllamaEmbedding> stream = entityStream.of(HashWithOllamaEmbedding.class);

    List<HashWithOllamaEmbedding> results = stream //
        .filter(HashWithOllamaEmbedding$.TEXT_EMBEDDING.knn(K, cat.getTextEmbedding())) //
        .sorted(HashWithOllamaEmbedding$._TEXT_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(5);
    assertThat(results.get(0).getName()).isEqualTo("cat"); // Exact match should be first
    assertThat(results).map(HashWithOllamaEmbedding::getName).containsExactlyInAnyOrder( //
        "cat", "dog", "lion", "elephant", "giraffe" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testKnnHybridSentenceSimilaritySearch() {
    HashWithOllamaEmbedding cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<HashWithOllamaEmbedding> stream = entityStream.of(HashWithOllamaEmbedding.class);

    List<HashWithOllamaEmbedding> results = stream //
        .filter(HashWithOllamaEmbedding$.NAME.in("cat", "lion", "dog")) //
        .filter(HashWithOllamaEmbedding$.TEXT_EMBEDDING.knn(K, cat.getTextEmbedding())) //
        .sorted(HashWithOllamaEmbedding$._TEXT_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(3).map(HashWithOllamaEmbedding::getName).containsExactly( //
        "cat", "dog", "lion" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testKnnSentenceSimilaritySearchWithScores() {
    HashWithOllamaEmbedding cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<HashWithOllamaEmbedding> stream = entityStream.of(HashWithOllamaEmbedding.class);

    List<Pair<HashWithOllamaEmbedding, Double>> results = stream //
        .filter(HashWithOllamaEmbedding$.TEXT_EMBEDDING.knn(K, cat.getTextEmbedding())) //
        .sorted(HashWithOllamaEmbedding$._TEXT_EMBEDDING_SCORE) //
        .limit(K) //
        .map(Fields.of(HashWithOllamaEmbedding$._THIS, HashWithOllamaEmbedding$._TEXT_EMBEDDING_SCORE)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(5), //
        () -> assertThat(results.get(0).getFirst().getName()).isEqualTo("cat"), // Exact match should be first
        () -> assertThat(results).map(Pair::getFirst).map(HashWithOllamaEmbedding::getName).containsExactlyInAnyOrder(
            "cat", "dog", "lion", "elephant", "giraffe"), //
        () -> assertThat(results.get(0).getSecond()).isCloseTo(0.0, within(0.001)) // Cat should have ~0 distance to itself
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testEmbedderCanVectorizeSentence() {
    Optional<HashWithOllamaEmbedding> maybeCat = repository.findFirstByName("cat");
    assertThat(maybeCat).isPresent();
    HashWithOllamaEmbedding cat = maybeCat.get();
    var catEmbedding = cat.getTextEmbedding();
    List<byte[]> embeddings = embedder.getTextEmbeddingsAsBytes(List.of(cat.getText()), HashWithOllamaEmbedding$.TEXT);
    assertAll( //
        () -> assertThat(embeddings).isNotEmpty(), //
        () -> assertThat(embeddings.get(0).length).isEqualTo(catEmbedding.length), //
        () -> assertThat(embeddings.get(0)).isEqualTo(catEmbedding));
  }
}
