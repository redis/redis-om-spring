package com.redis.om.spring.search.stream;

import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.*;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.indexed.NumericField;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.search.stream.predicates.SearchFieldPredicate;
import com.redis.om.spring.tuple.Pair;

import redis.clients.jedis.search.aggr.SortedField.SortOrder;

/**
 * A stream interface that provides search capabilities for Redis OM entities.
 * <p>
 * SearchStream extends the Java 8 Stream API with Redis Search specific operations,
 * allowing for fluent and composable search queries against Redis indexes. It supports
 * various search operations including filtering, sorting, aggregation, and pagination.
 * </p>
 * <p>
 * Key features include:
 * <ul>
 * <li>Field-based and full-text search filtering</li>
 * <li>Vector similarity search (KNN)</li>
 * <li>Query-by-example (QBE) operations</li>
 * <li>Result projection and summarization</li>
 * <li>Highlighting and sorting capabilities</li>
 * <li>Integration with Spring Data pagination</li>
 * </ul>
 *
 * @param <E> the entity type being searched
 */
public interface SearchStream<E> extends BaseStream<E, SearchStream<E>> {

  /**
   * Filters the search stream using a search field predicate.
   * 
   * @param predicate the search field predicate to apply
   * @return a new SearchStream with the filter applied
   */
  SearchStream<E> filter(SearchFieldPredicate<? super E, ?> predicate);

  /**
   * Filters the search stream using a standard Java predicate.
   * 
   * @param predicate the predicate to apply for filtering
   * @return a new SearchStream with the filter applied
   */
  SearchStream<E> filter(Predicate<?> predicate);

  /**
   * Filters the search stream using free text search.
   * 
   * @param freeText the free text query string
   * @return a new SearchStream with the text filter applied
   */
  SearchStream<E> filter(String freeText);

  /**
   * Filters the search stream using a Spring Data Example.
   * 
   * @param example the example object to match against
   * @return a new SearchStream with the example filter applied
   */
  SearchStream<E> filter(Example<E> example);

  /**
   * Applies a filter predicate only if the value is not null.
   *
   * @param value             the value to check
   * @param predicateSupplier a supplier that creates a predicate
   * @param <T>               the type of the value
   * @return this SearchStream instance
   */
  <T> SearchStream<E> filterIfNotNull(T value, Supplier<SearchFieldPredicate<? super E, ?>> predicateSupplier);

  /**
   * Applies a filter predicate only if the string value is not null and not blank.
   *
   * @param value             the string value to check
   * @param predicateSupplier a supplier that creates a predicate
   * @return this SearchStream instance
   */
  SearchStream<E> filterIfNotBlank(String value, Supplier<SearchFieldPredicate<? super E, ?>> predicateSupplier);

  /**
   * Applies a filter predicate only if the optional value is present.
   *
   * @param value             the optional value to check
   * @param predicateSupplier a supplier that creates a predicate
   * @param <T>               the type of the optional value
   * @return this SearchStream instance
   */
  <T> SearchStream<E> filterIfPresent(Optional<T> value,
      Supplier<SearchFieldPredicate<? super E, ?>> predicateSupplier);

  /**
   * Maps each element in the stream using the provided function.
   * 
   * @param field the mapping function to apply
   * @param <R>   the return type of the mapping function
   * @return a new SearchStream with mapped elements
   */
  <R> SearchStream<R> map(Function<? super E, ? extends R> field);

  /**
   * Returns the entity class for this search stream.
   * 
   * @return the Class object representing the entity type
   */
  Class<E> getEntityClass();

  /**
   * Maps each element to a Long value and returns a Stream of Longs.
   * 
   * @param mapper the function to map elements to Long values
   * @return a Stream of Long values
   */
  Stream<Long> map(ToLongFunction<? super E> mapper);

  /**
   * Maps each element to an int value and returns an IntStream.
   * 
   * @param mapper the function to map elements to int values
   * @return an IntStream of mapped values
   */
  IntStream mapToInt(ToIntFunction<? super E> mapper);

  /**
   * Maps each element to a long value and returns a LongStream.
   * 
   * @param mapper the function to map elements to long values
   * @return a LongStream of mapped values
   */
  LongStream mapToLong(ToLongFunction<? super E> mapper);

  /**
   * Maps each element to a double value and returns a DoubleStream.
   * 
   * @param mapper the function to map elements to double values
   * @return a DoubleStream of mapped values
   */
  DoubleStream mapToDouble(ToDoubleFunction<? super E> mapper);

  /**
   * Maps each element to a stream and flattens the result into a single stream.
   * 
   * @param mapper the function to map elements to streams
   * @param <R>    the type of elements in the resulting stream
   * @return a new SearchStream with flattened elements
   */
  <R> SearchStream<R> flatMap(Function<? super E, ? extends Stream<? extends R>> mapper);

  /**
   * Maps each element to an IntStream and flattens the result.
   * 
   * @param mapper the function to map elements to IntStreams
   * @return a flattened IntStream
   */
  IntStream flatMapToInt(Function<? super E, ? extends IntStream> mapper);

  /**
   * Maps each element to a LongStream and flattens the result.
   * 
   * @param mapper the function to map elements to LongStreams
   * @return a flattened LongStream
   */
  LongStream flatMapToLong(Function<? super E, ? extends LongStream> mapper);

  /**
   * Maps each element to a DoubleStream and flattens the result.
   * 
   * @param mapper the function to map elements to DoubleStreams
   * @return a flattened DoubleStream
   */
  DoubleStream flatMapToDouble(Function<? super E, ? extends DoubleStream> mapper);

  /**
   * Sorts the stream elements using the provided comparator.
   * 
   * @param comparator the comparator to use for sorting
   * @return a new SearchStream with sorted elements
   */
  SearchStream<E> sorted(Comparator<? super E> comparator);

  /**
   * Sorts the stream elements using the provided comparator and sort order.
   * 
   * @param comparator the comparator to use for sorting
   * @param order      the sort order (ascending or descending)
   * @return a new SearchStream with sorted elements
   */
  SearchStream<E> sorted(Comparator<? super E> comparator, SortOrder order);

  /**
   * Sorts the stream elements using Spring Data Sort specifications.
   * 
   * @param sort the Spring Data Sort specification
   * @return a new SearchStream with sorted elements
   */
  SearchStream<E> sorted(Sort sort);

  /**
   * Performs the provided action on each element as they are consumed from the stream.
   * 
   * @param action the action to perform on each element
   * @return a new SearchStream with the peek operation applied
   */
  SearchStream<E> peek(Consumer<? super E> action);

  /**
   * Limits the stream to at most the specified number of elements.
   * 
   * @param maxSize the maximum number of elements to include
   * @return a new SearchStream limited to the specified size
   */
  SearchStream<E> limit(long maxSize);

  /**
   * Skips the first n elements of the stream.
   * 
   * @param n the number of elements to skip
   * @return a new SearchStream with the first n elements skipped
   */
  SearchStream<E> skip(long n);

  /**
   * Performs the provided action on each element of the stream.
   * 
   * @param action the action to perform on each element
   */
  void forEach(Consumer<? super E> action);

  /**
   * Performs the provided action on each element of the stream in encounter order.
   * 
   * @param action the action to perform on each element
   */
  void forEachOrdered(Consumer<? super E> action);

  /**
   * Returns an array containing all elements in the stream.
   * 
   * @return an array of all stream elements
   */
  Object[] toArray();

  /**
   * Returns an array containing all elements in the stream using the provided generator.
   * 
   * @param generator the function to generate the array
   * @param <A>       the type of the array elements
   * @return an array of all stream elements
   */
  <A> A[] toArray(IntFunction<A[]> generator);

  /**
   * Performs a reduction on the stream elements using the provided identity and accumulator.
   * 
   * @param identity    the identity value for the reduction
   * @param accumulator the function for combining two values
   * @return the result of the reduction
   */
  E reduce(E identity, BinaryOperator<E> accumulator);

  /**
   * Performs a reduction on the stream elements using the provided accumulator.
   * 
   * @param accumulator the function for combining two values
   * @return an Optional containing the result of the reduction
   */
  Optional<E> reduce(BinaryOperator<E> accumulator);

  /**
   * Performs a reduction on the stream elements using identity, accumulator and combiner functions.
   * 
   * @param identity    the identity value for the reduction
   * @param accumulator the function for incorporating elements into the result
   * @param combiner    the function for combining partial results
   * @param <U>         the type of the reduction result
   * @return the result of the reduction
   */
  <U> U reduce(U identity, BiFunction<U, ? super E, U> accumulator, BinaryOperator<U> combiner);

  /**
   * Performs a mutable reduction operation on the stream elements.
   * 
   * @param supplier    the supplier providing a new result container
   * @param accumulator the function incorporating an element into a result
   * @param combiner    the function combining two result containers
   * @param <R>         the type of the result
   * @return the result of the reduction
   */
  <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super E> accumulator, BiConsumer<R, R> combiner);

  /**
   * Performs a mutable reduction operation using a Collector.
   * 
   * @param collector the Collector describing the reduction
   * @param <R>       the type of the result
   * @param <A>       the intermediate accumulation type of the Collector
   * @return the result of the reduction
   */
  <R, A> R collect(Collector<? super E, A, R> collector);

  /**
   * Returns the minimum element according to the provided comparator.
   * 
   * @param comparator the comparator to use for comparison
   * @return an Optional containing the minimum element, or empty if the stream is empty
   */
  Optional<E> min(Comparator<? super E> comparator);

  /**
   * Returns the maximum element according to the provided comparator.
   * 
   * @param comparator the comparator to use for comparison
   * @return an Optional containing the maximum element, or empty if the stream is empty
   */
  Optional<E> max(Comparator<? super E> comparator);

  /**
   * Returns the count of elements in the stream.
   * 
   * @return the number of elements in the stream
   */
  long count();

  /**
   * Returns whether any elements of the stream match the provided predicate.
   * 
   * @param predicate the predicate to apply to elements
   * @return true if any elements match, false otherwise
   */
  boolean anyMatch(Predicate<? super E> predicate);

  /**
   * Returns whether all elements of the stream match the provided predicate.
   * 
   * @param predicate the predicate to apply to elements
   * @return true if all elements match or the stream is empty, false otherwise
   */
  boolean allMatch(Predicate<? super E> predicate);

  /**
   * Returns whether no elements of the stream match the provided predicate.
   * 
   * @param predicate the predicate to apply to elements
   * @return true if no elements match or the stream is empty, false otherwise
   */
  boolean noneMatch(Predicate<? super E> predicate);

  /**
   * Returns an Optional describing the first element of the stream.
   * 
   * @return an Optional describing the first element, or empty if the stream is empty
   */
  Optional<E> findFirst();

  /**
   * Returns an Optional describing any element of the stream.
   * 
   * @return an Optional describing any element, or empty if the stream is empty
   */
  Optional<E> findAny();

  /**
   * Returns the first element of the stream, or a supplied alternative if empty.
   * 
   * @param supplier the supplier providing an alternative value
   * @return a SearchStream containing the first element or the supplied alternative
   */
  SearchStream<E> findFirstOrElse(Supplier<? extends E> supplier);

  /**
   * Maps the stream elements to labelled maps containing field names and values.
   * 
   * @return a Stream of Maps with string keys and object values
   */
  Stream<Map<String, Object>> mapToLabelledMaps();

  /**
   * Groups the stream elements by the specified metamodel fields.
   * 
   * @param fields the fields to group by
   * @param <R>    the type of the aggregation result
   * @return an AggregationStream with grouped elements
   */
  @SuppressWarnings(
    "unchecked"
  )
  <R> AggregationStream<R> groupBy(MetamodelField<E, ?>... fields);

  /**
   * Applies an expression with an alias to create an aggregation stream.
   * 
   * @param expression the aggregation expression to apply
   * @param alias      the alias for the expression result
   * @param <R>        the type of the aggregation result
   * @return an AggregationStream with the applied expression
   */
  <R> AggregationStream<R> apply(String expression, String alias);

  /**
   * Loads specified fields for aggregation operations.
   * 
   * @param fields the metamodel fields to load
   * @param <R>    the type of the aggregation result
   * @return an AggregationStream with the loaded fields
   */
  @SuppressWarnings(
    "unchecked"
  )
  <R> AggregationStream<R> load(MetamodelField<E, ?>... fields);

  /**
   * Loads all fields for aggregation operations.
   * 
   * @param <R> the type of the aggregation result
   * @return an AggregationStream with all fields loaded
   */
  <R> AggregationStream<R> loadAll();

  /**
   * Returns the minimum element according to the specified numeric field.
   * 
   * @param field the numeric field to compare
   * @return an Optional containing the minimum element, or empty if the stream is empty
   */
  Optional<E> min(NumericField<E, ?> field);

  /**
   * Returns the maximum element according to the specified numeric field.
   * 
   * @param field the numeric field to compare
   * @return an Optional containing the maximum element, or empty if the stream is empty
   */
  Optional<E> max(NumericField<E, ?> field);

  /**
   * Sets the RediSearch dialect version for the query.
   * 
   * @param dialect the dialect version to use
   * @return a new SearchStream with the specified dialect
   */
  SearchStream<E> dialect(int dialect);

  /**
   * Creates an aggregation stream with cursor support for large result sets.
   * 
   * @param i        the cursor batch size
   * @param duration the cursor timeout duration
   * @param <R>      the type of the aggregation result
   * @return an AggregationStream with cursor support
   */
  <R> AggregationStream<R> cursor(int i, Duration duration);

  /**
   * Returns the search operations instance used by this stream.
   * 
   * @return the SearchOperations instance
   */
  SearchOperations<String> getSearchOperations();

  /**
   * Returns a Page of results using the provided Pageable specification.
   * 
   * @param pageable the pagination information
   * @return a Page containing the results
   */
  Page<E> getPage(Pageable pageable);

  /**
   * Projects the stream elements using the provided function.
   * 
   * @param field the function to project elements
   * @param <R>   the type of the projected field
   * @return a new SearchStream with projected elements
   */
  <R> SearchStream<E> project(Function<? super E, ? extends R> field);

  /**
   * Projects the stream elements using the provided metamodel fields.
   * 
   * @param field the metamodel fields to project
   * @param <R>   the type of the projected fields
   * @return a new SearchStream with projected elements
   */
  @SuppressWarnings(
    "unchecked"
  )
  <R> SearchStream<E> project(MetamodelField<? super E, ? extends R>... field);

  /**
   * Returns the underlying RediSearch query string.
   * 
   * @return the query string that will be executed
   */
  String backingQuery();

  /**
   * Adds summarization for the specified field in search results.
   * 
   * @param field the function to extract the field to summarize
   * @param <R>   the type of the field
   * @return a new SearchStream with summarization applied
   */
  <R> SearchStream<E> summarize(Function<? super E, ? extends R> field);

  /**
   * Adds summarization for the specified field with custom parameters.
   * 
   * @param field  the function to extract the field to summarize
   * @param params the summarization parameters
   * @param <R>    the type of the field
   * @return a new SearchStream with summarization applied
   */
  <R> SearchStream<E> summarize(Function<? super E, ? extends R> field, SummarizeParams params);

  /**
   * Adds highlighting for the specified field in search results.
   * 
   * @param field the function to extract the field to highlight
   * @param <R>   the type of the field
   * @return a new SearchStream with highlighting applied
   */
  <R> SearchStream<E> highlight(Function<? super E, ? extends R> field);

  /**
   * Adds highlighting for the specified field with custom tags in search results.
   * 
   * @param field the function to extract the field to highlight
   * @param tags  the pair of opening and closing tags for highlighting
   * @param <R>   the type of the field
   * @return a new SearchStream with highlighting applied
   */
  <R> SearchStream<E> highlight(Function<? super E, ? extends R> field, Pair<String, String> tags);

  /**
   * Returns whether this stream operates on JSON documents or hash structures.
   * 
   * @return true if operating on JSON documents, false for hash structures
   */
  boolean isDocument();
}
