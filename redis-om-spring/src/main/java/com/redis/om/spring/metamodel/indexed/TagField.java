package com.redis.om.spring.metamodel.indexed;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.actions.*;
import com.redis.om.spring.search.stream.predicates.tag.ContainsAllPredicate;
import com.redis.om.spring.search.stream.predicates.tag.EqualPredicate;
import com.redis.om.spring.search.stream.predicates.tag.InPredicate;
import com.redis.om.spring.search.stream.predicates.tag.NotEqualPredicate;
import com.redis.om.spring.util.ObjectUtils;

/**
 * Metamodel field representation for tag-based search fields.
 * <p>
 * This class provides predicate and array manipulation operations specifically designed for tag fields
 * in Redis OM Spring. Tag fields are optimized for exact-match searches and are typically used for
 * categorical data, enumerations, or keywords. Unlike text fields which support full-text search,
 * tag fields provide fast exact-match lookups and set operations.
 * <p>
 * Tag fields in Redis use a separator (default: "|") to tokenize values, enabling efficient
 * searches across multiple tags. Fields annotated with {@code @TagIndexed} are represented
 * by this metamodel field type.
 * <p>
 * Common use cases include:
 * <ul>
 * <li>Searching by categories, tags, or labels</li>
 * <li>Filtering by enum values or status fields</li>
 * <li>Performing set operations (contains all, contains any)</li>
 * <li>Managing collections of discrete values</li>
 * </ul>
 *
 * @param <E> the entity type that contains this tag field
 * @param <T> the type of the tag field value
 * @since 1.0.0
 */
public class TagField<E, T> extends MetamodelField<E, T> {

  /**
   * Creates a new tag field with the specified accessor and indexing configuration.
   *
   * @param field   the search field accessor for this tag field
   * @param indexed whether this tag field is indexed for search operations
   */
  public TagField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  /**
   * Creates an indexed tag field by reflecting on the target class to find the field.
   *
   * @param targetClass the target class containing the field
   * @param fieldName   the name of the field
   * @throws RuntimeException if the field cannot be found
   */
  public TagField(Class<E> targetClass, String fieldName) {
    super(targetClass, fieldName);
  }

  /**
   * Creates an equality predicate for this tag field.
   * <p>
   * This predicate matches entities where the tag field contains exactly the specified value.
   * For collections, this matches if the value is present in the collection.
   *
   * @param value the tag value to search for
   * @return an equality predicate for query building
   */
  public EqualPredicate<E, T> eq(T value) {
    // For synthetic fields (like Map VALUES), use explicit TAG field type
    if (searchFieldAccessor.getField() == null) {
      return new EqualPredicate<>(searchFieldAccessor, value, redis.clients.jedis.search.Schema.FieldType.TAG);
    }
    return new EqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a not-equal predicate for this tag field.
   * <p>
   * This predicate matches entities where the tag field does not contain the specified value.
   *
   * @param value the tag value to exclude from results
   * @return a not-equal predicate for query building
   */
  public NotEqualPredicate<E, T> notEq(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a not-equal predicate for multiple string values.
   * <p>
   * This predicate matches entities where the tag field does not contain any of the specified values.
   *
   * @param values the tag values to exclude from results
   * @return a not-equal predicate for query building
   */
  public NotEqualPredicate<E, T> notEq(String... values) {
    return new NotEqualPredicate<>(searchFieldAccessor, Arrays.asList(values));
  }

  /**
   * Creates a not-equal predicate for multiple object values.
   * <p>
   * This predicate matches entities where the tag field does not contain any of the specified values.
   * Objects are converted to strings for comparison.
   *
   * @param values the values to exclude from results (will be converted to strings)
   * @return a not-equal predicate for query building
   */
  public NotEqualPredicate<E, T> notEq(Object... values) {
    return new NotEqualPredicate<>(searchFieldAccessor, Arrays.stream(values).map(Object::toString).toList());
  }

  /**
   * Creates an IN predicate for multiple string values.
   * <p>
   * This predicate matches entities where the tag field contains any of the specified values.
   * This is useful for finding entities tagged with at least one of several tags.
   *
   * @param values the tag values to search for
   * @return an IN predicate for query building
   */
  public InPredicate<E, ?> in(String... values) {
    return new InPredicate<>(searchFieldAccessor, Arrays.asList(values));
  }

  /**
   * Creates an IN predicate for multiple object values.
   * <p>
   * This predicate matches entities where the tag field contains any of the specified values.
   * Objects are converted to strings for comparison.
   *
   * @param values the values to search for (will be converted to strings)
   * @return an IN predicate for query building
   */
  public InPredicate<E, ?> in(Object... values) {
    return new InPredicate<>(searchFieldAccessor, Arrays.stream(values).map(Object::toString).toList());
  }

  /**
   * Creates a contains-all predicate for multiple string values.
   * <p>
   * This predicate matches entities where the tag field contains all of the specified values.
   * This is useful for finding entities that have been tagged with a specific combination of tags.
   *
   * @param values the tag values that must all be present
   * @return a contains-all predicate for query building
   */
  public ContainsAllPredicate<E, ?> containsAll(String... values) {
    return new ContainsAllPredicate<>(searchFieldAccessor, Arrays.asList(values));
  }

  /**
   * Creates a contains-all predicate for multiple object values.
   * <p>
   * This predicate matches entities where the tag field contains all of the specified values.
   * Objects are converted to strings for comparison.
   *
   * @param values the values that must all be present (will be converted to strings)
   * @return a contains-all predicate for query building
   */
  public ContainsAllPredicate<E, ?> containsAll(Object... values) {
    return new ContainsAllPredicate<>(searchFieldAccessor, Arrays.stream(values).map(Object::toString).toList());
  }

  /**
   * Creates a contains-none predicate for a single value.
   * <p>
   * This predicate matches entities where the tag field does not contain the specified value.
   * This is functionally equivalent to {@code notEq(value)}.
   *
   * @param value the tag value that must not be present
   * @return a not-equal predicate for query building
   */
  public NotEqualPredicate<E, T> containsNone(T value) {
    return new NotEqualPredicate<>(searchFieldAccessor, value);
  }

  /**
   * Creates a contains-none predicate for a string value.
   * <p>
   * This predicate matches entities where the tag field does not contain the specified value.
   * For non-collection types, the value is wrapped in a set before creating the predicate.
   *
   * @param value the tag value that must not be present
   * @return a not-equal predicate for query building
   */
  public NotEqualPredicate<E, ?> containsNone(String value) {
    if (!ObjectUtils.isCollection(value.getClass())) {
      return new NotEqualPredicate<>(searchFieldAccessor, Set.of(value));
    } else {
      return new NotEqualPredicate<>(searchFieldAccessor, value);
    }
  }

  /**
   * Creates an action to append a value to an array-based tag field.
   * <p>
   * This action adds a new tag to the end of an existing tag collection.
   *
   * @param value the tag value to append
   * @return a consumer that performs the append operation
   */
  public Consumer<E> add(Object value) {
    return new ArrayAppendAction<>(searchFieldAccessor, value);
  }

  /**
   * Creates an action to insert a value at a specific position in an array-based tag field.
   * <p>
   * This action inserts a new tag at the specified index in a tag collection.
   *
   * @param value the tag value to insert
   * @param index the position at which to insert the value (0-based)
   * @return a consumer that performs the insert operation
   */
  public Consumer<E> insert(Object value, Integer index) {
    return new ArrayInsertAction<>(searchFieldAccessor, value, index);
  }

  /**
   * Creates an action to prepend a value to an array-based tag field.
   * <p>
   * This action adds a new tag to the beginning of an existing tag collection.
   * This is equivalent to calling {@code insert(value, 0)}.
   *
   * @param value the tag value to prepend
   * @return a consumer that performs the prepend operation
   */
  public Consumer<E> prepend(Object value) {
    return new ArrayInsertAction<>(searchFieldAccessor, value, 0);
  }

  /**
   * Creates a function to get the length of an array-based tag field.
   * <p>
   * This function returns the number of tags in a tag collection.
   *
   * @return a function that returns the length of the tag array
   */
  public ToLongFunction<E> length() {
    return new ArrayLengthAction<>(searchFieldAccessor);
  }

  /**
   * Creates a function to find the index of a specific element in an array-based tag field.
   * <p>
   * This function returns the position of a tag within a tag collection,
   * or -1 if the tag is not found.
   *
   * @param element the tag element to search for
   * @return a function that returns the index of the element, or -1 if not found
   */
  public ToLongFunction<E> indexOf(Object element) {
    return new ArrayIndexOfAction<>(searchFieldAccessor, element);
  }

  /**
   * Creates an action to remove and return an element at a specific index from an array-based tag field.
   * <p>
   * This action removes a tag at the specified position and returns it.
   * Negative indices count from the end of the array (-1 is the last element).
   *
   * @param <R>   the return type of the popped element
   * @param index the position of the element to remove (supports negative indices)
   * @return an array pop action that removes and returns the element
   */
  public <R> ArrayPopAction<E, R> pop(Integer index) {
    return new ArrayPopAction<>(searchFieldAccessor, index);
  }

  /**
   * Creates an action to remove and return the last element from an array-based tag field.
   * <p>
   * This is equivalent to calling {@code pop(-1)}.
   *
   * @param <R> the return type of the popped element
   * @return an array pop action that removes and returns the last element
   */
  public <R> ArrayPopAction<E, R> pop() {
    return pop(-1);
  }

  /**
   * Creates an action to remove and return the first element from an array-based tag field.
   * <p>
   * This is equivalent to calling {@code pop(0)}.
   *
   * @param <R> the return type of the popped element
   * @return an array pop action that removes and returns the first element
   */
  public <R> ArrayPopAction<E, R> removeFirst() {
    return pop(0);
  }

  /**
   * Creates an action to remove and return the last element from an array-based tag field.
   * <p>
   * This is equivalent to calling {@code pop(-1)} or {@code pop()}.
   *
   * @param <R> the return type of the popped element
   * @return an array pop action that removes and returns the last element
   */
  public <R> ArrayPopAction<E, R> removeLast() {
    return pop(-1);
  }

  /**
   * Creates an action to remove and return an element at a specific index from an array-based tag field.
   * <p>
   * This is an alias for {@code pop(index)}, providing a more intuitive method name.
   *
   * @param <R>   the return type of the removed element
   * @param index the position of the element to remove (supports negative indices)
   * @return an array pop action that removes and returns the element
   */
  public <R> ArrayPopAction<E, R> remove(Integer index) {
    return pop(index);
  }

  /**
   * Creates an action to trim an array-based tag field to a specific range.
   * <p>
   * This action keeps only the elements within the specified range (inclusive)
   * and removes all others. This is useful for limiting the size of tag collections
   * or implementing sliding windows of tags.
   *
   * @param begin the starting index of the range to keep (inclusive)
   * @param end   the ending index of the range to keep (inclusive)
   * @return a consumer that performs the trim operation
   */
  public Consumer<E> trimToRange(Integer begin, Integer end) {
    return new ArrayTrimAction<>(searchFieldAccessor, begin, end);
  }
}