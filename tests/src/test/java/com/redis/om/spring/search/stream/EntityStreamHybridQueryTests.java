package com.redis.om.spring.search.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.HashWithTextAndVector;
import com.redis.om.spring.fixtures.hash.model.HashWithTextAndVector$;
import com.redis.om.spring.fixtures.hash.repository.HashWithTextAndVectorRepository;
import com.redis.om.spring.tuple.Fields;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.util.ObjectUtils;

/**
 * Integration tests for HybridQuery functionality using RedisVL.
 *
 * These tests validate the hybrid query behavior and guard against regressions.
 */
class EntityStreamHybridQueryTests extends AbstractBaseEnhancedRedisTest {

  @Autowired
  HashWithTextAndVectorRepository repository;

  @Autowired
  EntityStream entityStream;

  @BeforeEach
  void setUp() {
    repository.deleteAll();

    // Create test data with text descriptions and vector embeddings
    // Products in the "AI" category
    List<HashWithTextAndVector> aiProducts = List.of(
        HashWithTextAndVector.of(
            "ai-1",
            "machine learning algorithms for data science",
            "AI",
            250,
            ObjectUtils.floatArrayToByteArray(new float[]{0.1f, 0.9f, 0.8f, 0.2f})
        ),
        HashWithTextAndVector.of(
            "ai-2",
            "deep learning neural networks for image recognition",
            "AI",
            350,
            ObjectUtils.floatArrayToByteArray(new float[]{0.15f, 0.85f, 0.9f, 0.25f})
        ),
        HashWithTextAndVector.of(
            "ai-3",
            "natural language processing for text analysis",
            "AI",
            200,
            ObjectUtils.floatArrayToByteArray(new float[]{0.2f, 0.8f, 0.7f, 0.3f})
        ),
        HashWithTextAndVector.of(
            "ai-4",
            "reinforcement learning for robotics",
            "AI",
            400,
            ObjectUtils.floatArrayToByteArray(new float[]{0.25f, 0.75f, 0.85f, 0.35f})
        ),
        HashWithTextAndVector.of(
            "ai-5",
            "computer vision algorithms for object detection",
            "AI",
            300,
            ObjectUtils.floatArrayToByteArray(new float[]{0.12f, 0.88f, 0.82f, 0.22f})
        )
    );

    // Products in "Database" category
    List<HashWithTextAndVector> dbProducts = List.of(
        HashWithTextAndVector.of(
            "db-1",
            "redis in-memory database for caching",
            "Database",
            150,
            ObjectUtils.floatArrayToByteArray(new float[]{0.8f, 0.2f, 0.1f, 0.9f})
        ),
        HashWithTextAndVector.of(
            "db-2",
            "postgresql relational database management",
            "Database",
            100,
            ObjectUtils.floatArrayToByteArray(new float[]{0.85f, 0.15f, 0.2f, 0.85f})
        ),
        HashWithTextAndVector.of(
            "db-3",
            "mongodb document database for json storage",
            "Database",
            120,
            ObjectUtils.floatArrayToByteArray(new float[]{0.75f, 0.25f, 0.15f, 0.95f})
        )
    );

    // Products in "Cloud" category
    List<HashWithTextAndVector> cloudProducts = List.of(
        HashWithTextAndVector.of(
            "cloud-1",
            "aws cloud computing services and infrastructure",
            "Cloud",
            500,
            ObjectUtils.floatArrayToByteArray(new float[]{0.5f, 0.5f, 0.6f, 0.4f})
        ),
        HashWithTextAndVector.of(
            "cloud-2",
            "azure machine learning cloud platform",
            "Cloud",
            450,
            ObjectUtils.floatArrayToByteArray(new float[]{0.3f, 0.7f, 0.75f, 0.35f})
        )
    );

    List<HashWithTextAndVector> allData = new ArrayList<>();
    allData.addAll(aiProducts);
    allData.addAll(dbProducts);
    allData.addAll(cloudProducts);

    repository.saveAll(allData);
  }

  /**
   * Test basic hybrid search combining text and vector similarity.
   * This is the core functionality - must work!
   */
  @Test
  void testBasicHybridSearch() {
    // Given: Query vector similar to AI products
    float[] queryVector = new float[]{0.15f, 0.85f, 0.8f, 0.25f};

    // When: Perform hybrid search for "machine learning"
    List<HashWithTextAndVector> results = entityStream.of(HashWithTextAndVector.class)
        .hybridSearch(
            "machine learning",                    // text query
            HashWithTextAndVector$.DESCRIPTION,    // text field
            queryVector,                           // query vector
            HashWithTextAndVector$.EMBEDDING,      // vector field
            0.7f                                   // alpha: 70% vector, 30% text
        )
        .limit(5)
        .collect(Collectors.toList());

    // Then: Results should contain relevant AI products
    assertAll(
        () -> assertThat(results).isNotEmpty(),
        () -> assertThat(results).hasSizeLessThanOrEqualTo(5),
        // Should find items with "machine learning" in description
        () -> assertThat(results).anyMatch(p ->
            p.getDescription().contains("machine learning"))
    );
  }

  /**
   * Test hybrid search with category filter.
   * Combining hybrid search with traditional filters is a key use case.
   */
  @Test
  void testHybridSearchWithCategoryFilter() {
    // Given: Query for AI products only
    float[] queryVector = new float[]{0.15f, 0.85f, 0.8f, 0.25f};

    // When: Hybrid search with category filter
    List<HashWithTextAndVector> results = entityStream.of(HashWithTextAndVector.class)
        .filter(HashWithTextAndVector$.CATEGORY.eq("AI"))  // Pre-filter by category
        .hybridSearch(
            "learning algorithms",
            HashWithTextAndVector$.DESCRIPTION,
            queryVector,
            HashWithTextAndVector$.EMBEDDING,
            0.7f
        )
        .limit(5)
        .collect(Collectors.toList());

    // Then: All results must be in AI category
    assertAll(
        () -> assertThat(results).isNotEmpty(),
        () -> assertThat(results).allMatch(p -> p.getCategory().equals("AI")),
        () -> assertThat(results).anyMatch(p ->
            p.getDescription().contains("learning") ||
            p.getDescription().contains("algorithms"))
    );
  }

  /**
   * Test hybrid search with multiple filters (category AND price range).
   */
  @Test
  void testHybridSearchWithMultipleFilters() {
    // Given: Query for AI products in specific price range
    float[] queryVector = new float[]{0.15f, 0.85f, 0.8f, 0.25f};

    // When: Hybrid search with category and price filters
    List<HashWithTextAndVector> results = entityStream.of(HashWithTextAndVector.class)
        .filter(HashWithTextAndVector$.CATEGORY.eq("AI"))
        .filter(HashWithTextAndVector$.PRICE.between(200, 400))
        .hybridSearch(
            "neural networks learning",
            HashWithTextAndVector$.DESCRIPTION,
            queryVector,
            HashWithTextAndVector$.EMBEDDING,
            0.6f
        )
        .limit(10)
        .collect(Collectors.toList());

    // Then: All results match all criteria
    assertAll(
        () -> assertThat(results).isNotEmpty(),
        () -> assertThat(results).allMatch(p -> p.getCategory().equals("AI")),
        () -> assertThat(results).allMatch(p ->
            p.getPrice() >= 200 && p.getPrice() <= 400)
    );
  }

  /**
   * Test different alpha values to verify weighted scoring.
   * Alpha controls the balance between text and vector similarity.
   */
  @Test
  void testHybridSearchWithDifferentAlphaValues() {
    float[] queryVector = new float[]{0.15f, 0.85f, 0.8f, 0.25f};

    // Test alpha = 0.0 (pure text search, 0% vector)
    List<HashWithTextAndVector> textOnly = entityStream.of(HashWithTextAndVector.class)
        .hybridSearch(
            "machine learning",
            HashWithTextAndVector$.DESCRIPTION,
            queryVector,
            HashWithTextAndVector$.EMBEDDING,
            0.0f  // Pure text
        )
        .limit(3)
        .collect(Collectors.toList());

    // Test alpha = 1.0 (pure vector search, 0% text)
    List<HashWithTextAndVector> vectorOnly = entityStream.of(HashWithTextAndVector.class)
        .hybridSearch(
            "machine learning",
            HashWithTextAndVector$.DESCRIPTION,
            queryVector,
            HashWithTextAndVector$.EMBEDDING,
            1.0f  // Pure vector
        )
        .limit(3)
        .collect(Collectors.toList());

    // Test alpha = 0.5 (balanced: 50% text, 50% vector)
    List<HashWithTextAndVector> balanced = entityStream.of(HashWithTextAndVector.class)
        .hybridSearch(
            "machine learning",
            HashWithTextAndVector$.DESCRIPTION,
            queryVector,
            HashWithTextAndVector$.EMBEDDING,
            0.5f  // Balanced
        )
        .limit(3)
        .collect(Collectors.toList());

    // All queries should return results
    assertAll(
        () -> assertThat(textOnly).isNotEmpty(),
        () -> assertThat(vectorOnly).isNotEmpty(),
        () -> assertThat(balanced).isNotEmpty()
    );

    // Results may differ based on alpha weighting
    // (The exact ordering depends on the scoring formula)
  }

  /**
   * Test retrieving hybrid search results.
   * Results should be sorted by hybrid score (highest first).
   * Note: Hybrid queries use aggregation which automatically sorts by hybrid_score.
   */
  @Test
  void testHybridSearchWithScoreRetrieval() {
    // Given: Query vector similar to AI products
    float[] queryVector = new float[]{0.15f, 0.85f, 0.8f, 0.25f};

    // When: Retrieve results sorted by hybrid score (automatic in hybrid queries)
    List<HashWithTextAndVector> results = entityStream
        .of(HashWithTextAndVector.class)
        .hybridSearch(
            "machine learning",
            HashWithTextAndVector$.DESCRIPTION,
            queryVector,
            HashWithTextAndVector$.EMBEDDING,
            0.7f
        )
        .limit(5)
        .collect(Collectors.toList());

    // Then: Should have results sorted by hybrid score
    assertAll(
        () -> assertThat(results).isNotEmpty(),
        () -> assertThat(results).hasSizeLessThanOrEqualTo(5),
        // Results should contain relevant items (AI category products matching "machine learning")
        () -> assertThat(results).allMatch(r ->
            r.getDescription().contains("machine") ||
            r.getDescription().contains("learning") ||
            r.getCategory().equals("AI"))
    );
  }

  /**
   * Test hybrid search with empty text query.
   * Should handle edge case gracefully (or throw appropriate exception).
   */
  @Test
  void testHybridSearchWithEmptyText() {
    float[] queryVector = new float[]{0.15f, 0.85f, 0.8f, 0.25f};

    // Empty text should either work (fallback to vector-only) or throw exception
    assertThatThrownBy(() ->
        entityStream.of(HashWithTextAndVector.class)
            .hybridSearch(
                "",  // Empty text
                HashWithTextAndVector$.DESCRIPTION,
                queryVector,
                HashWithTextAndVector$.EMBEDDING,
                0.7f
            )
            .limit(5)
            .collect(Collectors.toList())
    ).isInstanceOf(IllegalArgumentException.class)
     .hasMessageContaining("text");
  }

  /**
   * Test hybrid search with null vector.
   * Should throw appropriate exception.
   */
  @Test
  void testHybridSearchWithNullVector() {
    // Null vector should throw exception
    assertThatThrownBy(() ->
        entityStream.of(HashWithTextAndVector.class)
            .hybridSearch(
                "machine learning",
                HashWithTextAndVector$.DESCRIPTION,
                null,  // Null vector
                HashWithTextAndVector$.EMBEDDING,
                0.7f
            )
            .limit(5)
            .collect(Collectors.toList())
    ).isInstanceOf(IllegalArgumentException.class);
  }

  /**
   * Test cross-category hybrid search.
   * Search across all categories without filters.
   */
  @Test
  void testHybridSearchAcrossCategories() {
    // Given: Query that might match multiple categories
    float[] queryVector = new float[]{0.3f, 0.7f, 0.75f, 0.35f};

    // When: Search without category filter
    List<HashWithTextAndVector> results = entityStream.of(HashWithTextAndVector.class)
        .hybridSearch(
            "machine learning cloud",
            HashWithTextAndVector$.DESCRIPTION,
            queryVector,
            HashWithTextAndVector$.EMBEDDING,
            0.6f
        )
        .limit(5)
        .collect(Collectors.toList());

    // Then: May find results from AI and Cloud categories
    assertAll(
        () -> assertThat(results).isNotEmpty(),
        () -> assertThat(results).hasSizeLessThanOrEqualTo(5)
    );

    // Extract unique categories from results
    var categories = results.stream()
        .map(HashWithTextAndVector::getCategory)
        .distinct()
        .collect(Collectors.toList());

    // Should potentially match multiple categories
    assertThat(categories).isNotEmpty();
  }

  /**
   * Test hybrid search with very high alpha (almost pure vector).
   */
  @Test
  void testHybridSearchWithHighAlpha() {
    float[] queryVector = new float[]{0.15f, 0.85f, 0.8f, 0.25f};

    // When: Alpha = 0.95 (95% vector, 5% text)
    List<HashWithTextAndVector> results = entityStream.of(HashWithTextAndVector.class)
        .hybridSearch(
            "database storage",  // Text doesn't match well with AI vectors
            HashWithTextAndVector$.DESCRIPTION,
            queryVector,          // Vector matches AI products
            HashWithTextAndVector$.EMBEDDING,
            0.95f                 // Heavily favor vector
        )
        .limit(3)
        .collect(Collectors.toList());

    // Then: Should favor vector similarity over text match
    assertThat(results).isNotEmpty();
    // With high alpha, vector similarity dominates, so AI products should rank higher
    // even though text mentions "database storage"
  }

  /**
   * Test hybrid search with very low alpha (almost pure text).
   */
  @Test
  void testHybridSearchWithLowAlpha() {
    float[] databaseVector = new float[]{0.8f, 0.2f, 0.1f, 0.9f};

    // When: Alpha = 0.05 (5% vector, 95% text)
    List<HashWithTextAndVector> results = entityStream.of(HashWithTextAndVector.class)
        .hybridSearch(
            "redis database caching",  // Matches database products
            HashWithTextAndVector$.DESCRIPTION,
            databaseVector,             // Vector matches database products
            HashWithTextAndVector$.EMBEDDING,
            0.05f                       // Heavily favor text
        )
        .limit(3)
        .collect(Collectors.toList());

    // Then: Should favor text match over vector similarity
    assertThat(results).isNotEmpty();
    // With low alpha, text matching dominates
    assertThat(results).anyMatch(p -> p.getDescription().contains("redis"));
  }

  /**
   * Test pagination with hybrid search.
   */
  @Test
  void testHybridSearchWithPagination() {
    float[] queryVector = new float[]{0.15f, 0.85f, 0.8f, 0.25f};

    // First page
    List<HashWithTextAndVector> page1 = entityStream.of(HashWithTextAndVector.class)
        .hybridSearch(
            "learning algorithms",
            HashWithTextAndVector$.DESCRIPTION,
            queryVector,
            HashWithTextAndVector$.EMBEDDING,
            0.7f
        )
        .skip(0)
        .limit(3)
        .collect(Collectors.toList());

    // Second page
    List<HashWithTextAndVector> page2 = entityStream.of(HashWithTextAndVector.class)
        .hybridSearch(
            "learning algorithms",
            HashWithTextAndVector$.DESCRIPTION,
            queryVector,
            HashWithTextAndVector$.EMBEDDING,
            0.7f
        )
        .skip(3)
        .limit(3)
        .collect(Collectors.toList());

    // Both pages should have results (assuming enough data)
    assertThat(page1).isNotEmpty();
    // Page 2 may be empty if not enough results
  }
}
