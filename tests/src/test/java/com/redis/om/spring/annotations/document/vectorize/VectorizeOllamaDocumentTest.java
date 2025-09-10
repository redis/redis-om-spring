package com.redis.om.spring.annotations.document.vectorize;

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

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.DocWithOllamaEmbedding;
import com.redis.om.spring.fixtures.document.model.DocWithOllamaEmbedding$;
import com.redis.om.spring.fixtures.document.repository.DocWithOllamaEmbeddingRepository;
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
class VectorizeOllamaDocumentTest extends AbstractBaseDocumentTest {
  @Autowired
  DocWithOllamaEmbeddingRepository repository;

  @Autowired
  EntityStream entityStream;

  @Autowired
  Embedder embedder;

  @BeforeEach
  void loadTestData() throws IOException {
    if (repository.count() == 0) {
      repository.save(DocWithOllamaEmbedding.of("cat",
          "The cat is a small domesticated carnivorous mammal with soft fur, a short snout, and retractable claws."));
      repository.save(DocWithOllamaEmbedding.of("dog",
          "A dog is a domesticated mammal of the family Canidae, characterized by its loyalty, playfulness, and friendly demeanor."));
      repository.save(DocWithOllamaEmbedding.of("lion",
          "The lion is a large cat of the genus Panthera native to Africa and India, known for its muscular body, deep roar, and mane on the male."));
      repository.save(DocWithOllamaEmbedding.of("elephant",
          "Elephants are the largest existing land animals, characterized by their long trunk, tusks, and large ears."));
      repository.save(DocWithOllamaEmbedding.of("giraffe",
          "The giraffe is an African even-toed ungulate mammal, the tallest living terrestrial animal, and the largest ruminant."));
    }
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testSentenceIsVectorized() {
    Optional<DocWithOllamaEmbedding> cat = repository.findFirstByName("cat");
    assertAll( //
        () -> assertThat(cat).isPresent(), //
        () -> assertThat(cat.get()).extracting("textEmbedding").isNotNull(), //
        () -> assertThat(cat.get().getTextEmbedding()).hasSize(4096) //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testKnnSentenceSimilaritySearch() {
    DocWithOllamaEmbedding cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<DocWithOllamaEmbedding> stream = entityStream.of(DocWithOllamaEmbedding.class);

    List<DocWithOllamaEmbedding> results = stream //
        .filter(DocWithOllamaEmbedding$.TEXT_EMBEDDING.knn(K, cat.getTextEmbedding())) //
        .sorted(DocWithOllamaEmbedding$._TEXT_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(5);
    assertThat(results.get(0).getName()).isEqualTo("cat"); // Exact match should be first
    assertThat(results).map(DocWithOllamaEmbedding::getName).containsExactlyInAnyOrder( //
        "cat", "dog", "lion", "elephant", "giraffe" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testKnnHybridSentenceSimilaritySearch() {
    DocWithOllamaEmbedding cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<DocWithOllamaEmbedding> stream = entityStream.of(DocWithOllamaEmbedding.class);

    List<DocWithOllamaEmbedding> results = stream //
        .filter(DocWithOllamaEmbedding$.NAME.in("cat", "lion", "dog")) //
        .filter(DocWithOllamaEmbedding$.TEXT_EMBEDDING.knn(K, cat.getTextEmbedding())) //
        .sorted(DocWithOllamaEmbedding$._TEXT_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(3).map(DocWithOllamaEmbedding::getName).containsExactly( //
        "cat", "dog", "lion" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testKnnSentenceSimilaritySearchWithScores() {
    DocWithOllamaEmbedding cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<DocWithOllamaEmbedding> stream = entityStream.of(DocWithOllamaEmbedding.class);

    List<Pair<DocWithOllamaEmbedding, Double>> results = stream //
        .filter(DocWithOllamaEmbedding$.TEXT_EMBEDDING.knn(K, cat.getTextEmbedding())) //
        .sorted(DocWithOllamaEmbedding$._TEXT_EMBEDDING_SCORE) //
        .limit(K) //
        .map(Fields.of(DocWithOllamaEmbedding$._THIS, DocWithOllamaEmbedding$._TEXT_EMBEDDING_SCORE)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(5), //
        () -> assertThat(results.get(0).getFirst().getName()).isEqualTo("cat"), // Exact match should be first
        () -> assertThat(results).map(Pair::getFirst).map(DocWithOllamaEmbedding::getName).containsExactlyInAnyOrder(
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
    Optional<DocWithOllamaEmbedding> maybeCat = repository.findFirstByName("cat");
    assertThat(maybeCat).isPresent();
    DocWithOllamaEmbedding cat = maybeCat.get();
    var catEmbedding = cat.getTextEmbedding();
    List<float[]> embeddings = embedder.getTextEmbeddingsAsFloats(List.of(cat.getText()), DocWithOllamaEmbedding$.TEXT);
    assertAll( //
        () -> assertThat(embeddings).isNotEmpty(), //
        () -> assertThat(embeddings.get(0)).isEqualTo(catEmbedding));
  }
}
