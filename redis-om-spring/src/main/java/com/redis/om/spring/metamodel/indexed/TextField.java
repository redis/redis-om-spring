package com.redis.om.spring.metamodel.indexed;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.StrLengthAction;
import com.redis.om.spring.search.stream.actions.StringAppendAction;
import com.redis.om.spring.search.stream.predicates.fulltext.*;

/**
 * Represents a text field in the Redis OM Spring metamodel that supports
 * full-text search operations and predicates. This class provides methods for
 * creating various text-based search predicates and string operations.
 * 
 * <p>TEXT fields in RediSearch are designed for full-text search capabilities.
 * Unlike TAG fields, TEXT fields undergo tokenization, stemming, and other
 * text processing operations, making them ideal for:
 * <ul>
 * <li>Natural language content (e.g., descriptions, comments, articles)</li>
 * <li>Fields requiring partial matching or fuzzy search</li>
 * <li>Content where word order and proximity matter</li>
 * <li>Multi-language text with stemming support</li>
 * </ul>
 * 
 * <p>Key differences from TAG fields:
 * <ul>
 * <li>Full tokenization - text is split into searchable terms</li>
 * <li>Case-insensitive matching by default</li>
 * <li>Supports stemming for language-specific word variations</li>
 * <li>Enables phrase searches and proximity queries</li>
 * </ul>
 * 
 * @param <E> the entity type that contains this text field
 * @param <T> the type of the field (typically String)
 * 
 * @see com.redis.om.spring.annotations.TextIndexed
 * @see com.redis.om.spring.annotations.Searchable
 * @see com.redis.om.spring.metamodel.indexed.TagField
 * @see com.redis.om.spring.metamodel.MetamodelField
 * @author Redis OM Spring Team
 * @since 0.1.0
 */
public class TextField<E, T> extends MetamodelField<E, T> {

  /**
   * Constructs a TextField with the specified field accessor and indexing status.
   * 
   * @param field   the search field accessor for this text field
   * @param indexed whether this field is indexed for search operations
   */
  public TextField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  /**
   * Constructs a TextField for the specified target class and field name.
   * 
   * @param targetClass the class containing this text field
   * @param fieldName   the name of the text field
   */
  public TextField(Class<E> targetClass, String fieldName) {
    super(targetClass, fieldName);
  }

  /**
   * Creates an equality predicate for exact text matching.
   * <p>
   * Note: For TEXT fields, this performs a full-text search for the exact phrase.
   * For exact string matching without tokenization, consider using a TAG field.
   * </p>
   * 
   * @param value the text value to match
   * @return an EqualPredicate that matches entities where this field equals the specified value
   */
  public EqualPredicate<E, T> eq(T value) {
    return new EqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a not-equal predicate for text fields.
   * 
   * @param value the text value to exclude
   * @return a NotEqualPredicate that matches entities where this field does not equal the specified value
   */
  public NotEqualPredicate<E, T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a starts-with predicate for prefix matching.
   * <p>
   * This predicate matches text fields that begin with the specified prefix.
   * The matching is performed after tokenization, so it matches tokens that
   * start with the given prefix.
   * </p>
   * 
   * @param value the prefix to match
   * @return a StartsWithPredicate that matches entities where this field starts with the specified value
   */
  public StartsWithPredicate<E, T> startsWith(T value) {
    return new StartsWithPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates an ends-with predicate for suffix matching.
   * <p>
   * This predicate matches text fields that end with the specified suffix.
   * The matching is performed on the tokenized content.
   * </p>
   * 
   * @param value the suffix to match
   * @return an EndsWithPredicate that matches entities where this field ends with the specified value
   */
  public EndsWithPredicate<E, T> endsWith(T value) {
    return new EndsWithPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a like predicate for pattern matching with wildcards.
   * <p>
   * Supports wildcard patterns where '%' matches any sequence of characters
   * and '_' matches a single character. This provides SQL-like pattern matching
   * capabilities for text fields.
   * </p>
   * 
   * @param value the pattern to match (e.g., "hello%world")
   * @return a LikePredicate that matches entities where this field matches the pattern
   */
  public LikePredicate<E, T> like(T value) {
    return new LikePredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a not-like predicate for excluding pattern matches.
   * 
   * @param value the pattern to exclude
   * @return a NotLikePredicate that matches entities where this field does not match the pattern
   */
  public NotLikePredicate<E, T> notLike(T value) {
    return new NotLikePredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a containing predicate for substring matching.
   * <p>
   * This predicate matches text fields that contain the specified substring
   * anywhere within the field content. The search is performed on the
   * tokenized text.
   * </p>
   * 
   * @param value the substring to search for
   * @return a ContainingPredicate that matches entities where this field contains the specified value
   */
  public ContainingPredicate<E, T> containing(T value) {
    return new ContainingPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a not-containing predicate for excluding substring matches.
   * 
   * @param value the substring to exclude
   * @return a NotContainingPredicate that matches entities where this field does not contain the specified value
   */
  public NotContainingPredicate<E, T> notContaining(T value) {
    return new NotContainingPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates an in predicate for matching any of the specified values.
   * <p>
   * For text fields, this creates a search that matches documents containing
   * any of the specified text values. Each value is processed according to
   * the text field's tokenization and stemming rules.
   * </p>
   * 
   * @param values the values to match against
   * @return an InPredicate that matches entities where this field contains any of the specified values
   */
  @SuppressWarnings(
    "unchecked"
  )
  public InPredicate<E, ?> in(T... values) {
    return new InPredicate<>(searchFieldAccessor, Arrays.asList(values));
  }

  /**
   * Creates a string append action for this text field.
   * <p>
   * This action appends the specified string value to the existing field content
   * when applied to an entity. Useful for building up text content incrementally.
   * </p>
   * 
   * @param value the string to append
   * @return a Consumer that appends the value to this field
   */
  public Consumer<E> append(String value) {
    return new StringAppendAction<>(searchFieldAccessor, value);
  }

  /**
   * Creates a string length function for this text field.
   * <p>
   * Returns a function that calculates the length of the string value
   * in this field for a given entity.
   * </p>
   * 
   * @return a ToLongFunction that returns the length of this field's string value
   */
  public ToLongFunction<E> length() {
    return new StrLengthAction<>(searchFieldAccessor);
  }

}
