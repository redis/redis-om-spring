package com.redis.om.spring.annotations.hash.vectorize;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.HashWithCustomModelOpenAIEmbedding;
import com.redis.om.spring.fixtures.hash.model.HashWithOpenAIEmbedding;
import com.redis.om.spring.fixtures.hash.model.HashWithOpenAIEmbedding$;
import com.redis.om.spring.fixtures.hash.repository.HashWithCustomModelOpenAIEmbeddingRepository;
import com.redis.om.spring.fixtures.hash.repository.HashWithOpenAIEmbeddingRepository;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.tuple.Fields;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.vectorize.Embedder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisabledIfEnvironmentVariable(named = "GITHUB_ACTIONS", matches = "true")
@DisabledIf(
    expression = "#{systemEnvironment['OPENAI_API_KEY'] == null}",
    reason = "Disabled if OPENAI_API_KEY environment variable is not set"
)
class VectorizeOpenAIHashTest extends AbstractBaseEnhancedRedisTest {
  @Autowired
  HashWithOpenAIEmbeddingRepository repository;

  @Autowired
  HashWithCustomModelOpenAIEmbeddingRepository repository2;

  @Autowired
  EntityStream entityStream;

  @Autowired
  Embedder embedder;

  @BeforeEach
  void loadTestData() throws IOException {
    if (repository.count() == 0) {
      repository.save(
          HashWithOpenAIEmbedding.of("cat", "The cat (Felis catus) is a domestic species of small carnivorous mammal."));
      repository.save(HashWithOpenAIEmbedding.of("cat2",
          "It is the only domesticated species in the family Felidae and is commonly referred to as the domestic cat or house cat"));
      repository.save(HashWithOpenAIEmbedding.of("catdog", "This is a picture of a cat and a dog together"));
      repository.save(HashWithOpenAIEmbedding.of("face", "Three years later, the coffin was still full of Jello."));
      repository.save(
          HashWithOpenAIEmbedding.of("face2", "The person box was packed with jelly many dozens of months later."));
    }

    if (repository2.count() == 0) {
      repository2.save(
          HashWithCustomModelOpenAIEmbedding.of("cat", "The cat (Felis catus) is a domestic species of small carnivorous mammal."));
      repository2.save(HashWithCustomModelOpenAIEmbedding.of("cat2",
          "It is the only domesticated species in the family Felidae and is commonly referred to as the domestic cat or house cat"));
      repository2.save(HashWithCustomModelOpenAIEmbedding.of("catdog", "This is a picture of a cat and a dog together"));
      repository2.save(HashWithCustomModelOpenAIEmbedding.of("face", "Three years later, the coffin was still full of Jello."));
      repository2.save(
          HashWithCustomModelOpenAIEmbedding.of("face2", "The person box was packed with jelly many dozens of months later."));
    }
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testSentenceIsVectorized() {
    Optional<HashWithOpenAIEmbedding> cat = repository.findFirstByName("cat");
    assertAll( //
        () -> assertThat(cat).isPresent(), //
        () -> assertThat(cat.get()).extracting("textEmbedding").isNotNull(), //
        () -> assertThat(cat.get().getTextEmbedding()).hasSize(1536*Float.BYTES) //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testSentenceIsVectorizedWithCustomModel() {
    Optional<HashWithCustomModelOpenAIEmbedding> cat = repository2.findFirstByName("cat");
    assertAll( //
        () -> assertThat(cat).isPresent(), //
        () -> assertThat(cat.get()).extracting("textEmbedding").isNotNull(), //
        () -> assertThat(cat.get().getTextEmbedding()).hasSize(3072*Float.BYTES) //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testKnnSentenceSimilaritySearch() {
    HashWithOpenAIEmbedding cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<HashWithOpenAIEmbedding> stream = entityStream.of(HashWithOpenAIEmbedding.class);

    List<HashWithOpenAIEmbedding> results = stream //
        .filter(HashWithOpenAIEmbedding$.TEXT_EMBEDDING.knn(K, cat.getTextEmbedding())) //
        .sorted(HashWithOpenAIEmbedding$._TEXT_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(5).map(HashWithOpenAIEmbedding::getName).containsExactly( //
        "cat", "cat2", "catdog", "face2", "face" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testKnnHybridSentenceSimilaritySearch() {
    HashWithOpenAIEmbedding cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<HashWithOpenAIEmbedding> stream = entityStream.of(HashWithOpenAIEmbedding.class);

    List<HashWithOpenAIEmbedding> results = stream //
        .filter(HashWithOpenAIEmbedding$.NAME.startsWith("cat")) //
        .filter(HashWithOpenAIEmbedding$.TEXT_EMBEDDING.knn(K, cat.getTextEmbedding())) //
        .sorted(HashWithOpenAIEmbedding$._TEXT_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(3).map(HashWithOpenAIEmbedding::getName).containsExactly( //
        "cat", "cat2", "catdog" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testKnnSentenceSimilaritySearchWithScores() {
    HashWithOpenAIEmbedding cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<HashWithOpenAIEmbedding> stream = entityStream.of(HashWithOpenAIEmbedding.class);

    List<Pair<HashWithOpenAIEmbedding, Double>> results = stream //
        .filter(HashWithOpenAIEmbedding$.TEXT_EMBEDDING.knn(K, cat.getTextEmbedding())) //
        .sorted(HashWithOpenAIEmbedding$._TEXT_EMBEDDING_SCORE) //
        .limit(K) //
        .map(Fields.of(HashWithOpenAIEmbedding$._THIS, HashWithOpenAIEmbedding$._TEXT_EMBEDDING_SCORE)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(5).map(Pair::getFirst).map(HashWithOpenAIEmbedding::getName)
            .containsExactly("cat", "cat2", "catdog", "face2", "face"), //
        () -> assertThat(results).hasSize(5).map(Pair::getSecond).usingElementComparator(closeToComparator)
            .containsExactly(7.15255737305E-7, 0.0800130963326, 0.163947761059, 0.261719405651, 0.288997769356) //
    );
  }
}
