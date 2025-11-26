package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.ProductDoc;
import com.redis.om.spring.fixtures.document.repository.ProductDocRepository;

import redis.clients.jedis.search.SearchResult;

/**
 * Tests for Issue #676: Gson JSON serialization bug with spaces in projected fields.
 *
 * When using FT.SEARCH with field projections (returnFields), a JsonSyntaxException
 * occurs if projected TEXT fields contain values with spaces (e.g., "makeup spatula premium").
 *
 * The issue is in RediSearchQuery.parseDocumentResult() which manually constructs JSON
 * strings via StringBuilder without properly quoting string values.
 *
 * @see <a href="https://github.com/redis/redis-om-spring/issues/676">Issue #676</a>
 */
class QueryProjectionJsonSerializationTest extends AbstractBaseDocumentTest {

  @Autowired
  ProductDocRepository productDocRepository;

  @BeforeEach
  void setup() {
    productDocRepository.deleteAll();

    // Create products with multi-word keywords (spaces in the value)
    productDocRepository.save(ProductDoc.of("makeup spatula premium", "beauty", 19.99));
    productDocRepository.save(ProductDoc.of("kitchen knife set", "kitchen", 49.99));
    productDocRepository.save(ProductDoc.of("wireless bluetooth headphones", "electronics", 89.99));
    productDocRepository.save(ProductDoc.of("organic green tea", "food", 12.99));
    productDocRepository.save(ProductDoc.of("simple", "beauty", 9.99)); // single word for comparison
  }

  /**
   * Test that projected fields with spaces are correctly serialized.
   * This test would fail before the fix with a JsonSyntaxException because
   * the manual JSON construction didn't quote string values properly.
   */
  @Test
  void testProjectedFieldsWithSpacesDoNotCauseJsonSyntaxException() {
    // First verify data was saved
    assertThat(productDocRepository.count()).isEqualTo(5);

    // This query returns projected fields (keyword, category) as domain objects
    // The keyword field contains spaces which should be properly quoted in JSON
    List<ProductDoc> products = productDocRepository.findByCategoryWithProjection("beauty");

    assertThat(products).hasSize(2);
    assertThat(products).extracting("keyword")
        .containsExactlyInAnyOrder("makeup spatula premium", "simple");
    assertThat(products).extracting("category")
        .containsOnly("beauty");
  }

  /**
   * Test that the raw SearchResult query works (baseline).
   */
  @Test
  void testSearchResultQueryWorks() {
    SearchResult result = productDocRepository.findByCategoryReturningSearchResult("beauty");
    assertThat(result.getTotalResults()).isEqualTo(2);
  }
}
