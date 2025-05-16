package com.redis.om.spring.annotations.document.vectorize;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.redis.om.spring.fixtures.document.model.DocWithCustomModelOpenAIEmbedding;
import com.redis.om.spring.fixtures.document.model.DocWithOpenAIEmbedding;
import com.redis.om.spring.fixtures.document.model.DocWithOpenAIEmbedding$;
import com.redis.om.spring.fixtures.document.repository.DocWithCustomModelOpenAIEmbeddingRepository;
import com.redis.om.spring.fixtures.document.repository.DocWithOpenAIEmbeddingRepository;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.tuple.Fields;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.vectorize.Embedder;

@DisabledIfEnvironmentVariable(
    named = "GITHUB_ACTIONS", matches = "true"
)
@DisabledIf(
    expression = "#{systemEnvironment['OPENAI_API_KEY'] == null}",
    reason = "Disabled if OPENAI_API_KEY environment variable is not set"
)
class VectorizeOpenAIDocumentTest extends AbstractBaseDocumentTest {
  @Autowired
  DocWithOpenAIEmbeddingRepository repository;

  @Autowired
  DocWithCustomModelOpenAIEmbeddingRepository repository2;

  @Autowired
  EntityStream entityStream;

  @Autowired
  Embedder embedder;

  @BeforeEach
  void loadTestData() throws IOException {
    if (repository.count() == 0) {
      repository.save(DocWithOpenAIEmbedding.of("cat",
          "The cat (Felis catus) is a domestic species of small carnivorous mammal."));
      repository.save(DocWithOpenAIEmbedding.of("cat2",
          "It is the only domesticated species in the family Felidae and is commonly referred to as the domestic cat or house cat"));
      repository.save(DocWithOpenAIEmbedding.of("catdog", "This is a picture of a cat and a dog together"));
      repository.save(DocWithOpenAIEmbedding.of("face", "Three years later, the coffin was still full of Jello."));
      repository.save(DocWithOpenAIEmbedding.of("face2",
          "The person box was packed with jelly many dozens of months later."));
    }

    if (repository2.count() == 0) {
      repository2.save(DocWithCustomModelOpenAIEmbedding.of("cat",
          "The cat (Felis catus) is a domestic species of small carnivorous mammal."));
      repository2.save(DocWithCustomModelOpenAIEmbedding.of("cat2",
          "It is the only domesticated species in the family Felidae and is commonly referred to as the domestic cat or house cat"));
      repository2.save(DocWithCustomModelOpenAIEmbedding.of("catdog", "This is a picture of a cat and a dog together"));
      repository2.save(DocWithCustomModelOpenAIEmbedding.of("face",
          "Three years later, the coffin was still full of Jello."));
      repository2.save(DocWithCustomModelOpenAIEmbedding.of("face2",
          "The person box was packed with jelly many dozens of months later."));
    }
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testSentenceIsVectorized() {
    Optional<DocWithOpenAIEmbedding> cat = repository.findFirstByName("cat");
    assertAll( //
        () -> assertThat(cat).isPresent(), //
        () -> assertThat(cat.get()).extracting("textEmbedding").isNotNull(), //
        () -> assertThat(cat.get().getTextEmbedding()).hasSize(1536) //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testSentenceIsVectorizedWithCustomModel() {
    Optional<DocWithCustomModelOpenAIEmbedding> cat = repository2.findFirstByName("cat");
    assertAll( //
        () -> assertThat(cat).isPresent(), //
        () -> assertThat(cat.get()).extracting("textEmbedding").isNotNull(), //
        () -> assertThat(cat.get().getTextEmbedding()).hasSize(3072) //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testKnnSentenceSimilaritySearch() {
    DocWithOpenAIEmbedding cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<DocWithOpenAIEmbedding> stream = entityStream.of(DocWithOpenAIEmbedding.class);

    List<DocWithOpenAIEmbedding> results = stream //
        .filter(DocWithOpenAIEmbedding$.TEXT_EMBEDDING.knn(K, cat.getTextEmbedding())) //
        .sorted(DocWithOpenAIEmbedding$._TEXT_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(5).map(DocWithOpenAIEmbedding::getName).containsExactly( //
        "cat", "cat2", "catdog", "face2", "face" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testKnnHybridSentenceSimilaritySearch() {
    DocWithOpenAIEmbedding cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<DocWithOpenAIEmbedding> stream = entityStream.of(DocWithOpenAIEmbedding.class);

    List<DocWithOpenAIEmbedding> results = stream //
        .filter(DocWithOpenAIEmbedding$.NAME.startsWith("cat")) //
        .filter(DocWithOpenAIEmbedding$.TEXT_EMBEDDING.knn(K, cat.getTextEmbedding())) //
        .sorted(DocWithOpenAIEmbedding$._TEXT_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(3).map(DocWithOpenAIEmbedding::getName).containsExactly( //
        "cat", "cat2", "catdog" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
  )
  void testKnnSentenceSimilaritySearchWithScores() {
    DocWithOpenAIEmbedding cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<DocWithOpenAIEmbedding> stream = entityStream.of(DocWithOpenAIEmbedding.class);

    List<Pair<DocWithOpenAIEmbedding, Double>> results = stream //
        .filter(DocWithOpenAIEmbedding$.TEXT_EMBEDDING.knn(K, cat.getTextEmbedding())) //
        .sorted(DocWithOpenAIEmbedding$._TEXT_EMBEDDING_SCORE) //
        .limit(K) //
        .map(Fields.of(DocWithOpenAIEmbedding$._THIS, DocWithOpenAIEmbedding$._TEXT_EMBEDDING_SCORE)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(5).map(Pair::getFirst).map(DocWithOpenAIEmbedding::getName).containsExactly(
            "cat", "cat2", "catdog", "face2", "face"), //
        () -> assertThat(results).hasSize(5).map(Pair::getSecond).usingElementComparator(closeToComparator)
            .containsExactly(7.15255737305E-7, 0.0800130963326, 0.163947761059, 0.261719405651, 0.288997769356) //
    );
  }
}
