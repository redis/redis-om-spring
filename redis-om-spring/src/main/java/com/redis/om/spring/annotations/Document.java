package com.redis.om.spring.annotations;

import java.lang.annotation.*;

import org.springframework.core.annotation.AliasFor;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.keyvalue.annotation.KeySpace;

import com.redis.om.spring.repository.query.SearchLanguage;

/**
 * Annotation used to mark a class as a Redis JSON document entity.
 * <p>
 * This annotation configures a class to be stored as a JSON document in Redis,
 * enabling automatic serialization/deserialization and RediSearch indexing.
 * It extends Spring Data's KeySpace functionality with Redis-specific features
 * for document storage and search capabilities.
 * </p>
 * <p>
 * Key features enabled by this annotation:
 * <ul>
 * <li>JSON document storage using RedisJSON module</li>
 * <li>Automatic RediSearch index creation and maintenance</li>
 * <li>Full-text search capabilities across document fields</li>
 * <li>Secondary indexing for efficient querying</li>
 * <li>Time-to-live (TTL) support for automatic expiration</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * \@Document(value = "users", indexName = "user-idx", timeToLive = 3600)
 * public class User {
 *     \@Id
 *     private String id;
 *     
 *     \@Indexed
 *     private String name;
 *     
 *     \@Searchable
 *     private String description;
 * }
 * }
 * </pre>
 *
 * @see org.springframework.data.keyvalue.annotation.KeySpace
 * @see com.redis.om.spring.repository.RedisDocumentRepository
 * @see com.redis.om.spring.annotations.Indexed
 * @see com.redis.om.spring.annotations.Searchable
 * @since 0.1.0
 */
@Persistent
@Inherited
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.TYPE }
)
@KeySpace
public @interface Document {

  /**
   * The collection name for the Redis documents. If not specified,
   * the simple class name of the entity will be used.
   * This value is aliased to the KeySpace annotation's value attribute.
   * 
   * @return the collection name for storing documents
   */
  @AliasFor(
      annotation = KeySpace.class, attribute = "value"
  )
  String value() default "";

  /**
   * The name of the RediSearch index to create for this document type.
   * If not specified, a default index name will be generated.
   * 
   * @return the RediSearch index name
   */
  String indexName() default "";

  /**
   * Whether to perform index operations asynchronously.
   * When true, index updates will be performed in the background.
   * 
   * @return true for async indexing, false for synchronous indexing
   */
  boolean async() default false;

  /**
   * Optional key prefixes to use when creating the RediSearch index.
   * <p>
   * Prefixes define which Redis keys should be included in the search index.
   * If not specified, a default prefix based on the document type will be used.
   * Multiple prefixes can be specified to include documents with different
   * key patterns in the same index.
   * </p>
   * 
   * @return array of key prefixes for index inclusion
   */
  String[] prefixes() default {};

  /**
   * Optional filter expression to apply when creating the RediSearch index.
   * <p>
   * The filter is used to conditionally include documents in the index based
   * on their field values. Only documents matching the filter criteria will
   * be indexed and searchable. The filter syntax follows RediSearch filter
   * expression rules.
   * </p>
   * 
   * @return filter expression for conditional indexing
   */
  String filter() default "";

  /**
   * Optional field name that contains the document's language identifier.
   * <p>
   * When specified, this field will be used to determine the language-specific
   * text processing rules for search operations. The field should contain
   * a language code (e.g., "en", "es", "fr") that corresponds to supported
   * RediSearch language analyzers.
   * </p>
   * 
   * @return field name containing language identifier
   */
  String languageField() default "";

  /**
   * Default language for text analysis and search operations.
   * <p>
   * This setting determines which language-specific stemming, stopword filtering,
   * and tokenization rules will be applied during indexing and search. If a
   * languageField is specified, this serves as the fallback language when
   * the field value is empty or invalid.
   * </p>
   * 
   * @return default language for search operations
   */
  SearchLanguage language() default SearchLanguage.ENGLISH;

  /**
   * Default document score for search result ranking.
   * <p>
   * This value provides a base score for documents when no other scoring
   * factors are available. Higher scores will rank documents higher in
   * search results. Individual documents can override this score using
   * the {@link DocumentScore} annotation.
   * </p>
   * 
   * @return default document score (1.0 by default)
   */
  double score() default 1.0;

  /**
   * Time before expire in seconds.
   *
   * @return positive number when expiration should be applied.
   */
  long timeToLive() default -1L;
}
