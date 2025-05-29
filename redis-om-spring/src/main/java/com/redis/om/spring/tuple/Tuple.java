package com.redis.om.spring.tuple;

import java.util.Collections;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A specialized tuple interface that extends {@link GenericTuple} with Object as the element type.
 * This interface provides a convenient abstraction for working with tuples containing heterogeneous
 * objects and includes default implementations for common streaming operations.
 * 
 * <p>Tuple extends GenericTuple&lt;Object&gt; and provides additional functionality for streaming
 * all elements and working with mixed-type tuples. It's the primary interface used throughout
 * the Redis OM Spring library for handling structured data returned from Redis operations.</p>
 * 
 * <p>The default implementations provide efficient streaming capabilities while maintaining
 * compatibility with the base GenericTuple contract.</p>
 * 
 * @since 1.0
 * @see GenericTuple
 */
public interface Tuple extends GenericTuple<Object> {

  /**
   * Returns a sequential Stream of all elements in this tuple.
   * This method provides a convenient way to process all tuple elements using Stream operations.
   * 
   * <p>The stream is created by mapping each index position to its corresponding element.
   * The ordering of elements in the stream matches their index ordering in the tuple.</p>
   * 
   * @return a sequential stream of all elements in this tuple, never null
   */
  default Stream<Object> stream() {
    return IntStream.range(0, size()).mapToObj(this::get);
  }

  /**
   * {@inheritDoc}
   * 
   * <p>This implementation uses the {@link #stream()} method to filter elements by type,
   * providing an efficient way to access type-safe elements from a mixed-type tuple.</p>
   */
  @Override
  default <T> Stream<T> streamOf(Class<T> clazz) {
    return stream().filter(clazz::isInstance).map(clazz::cast);
  }

  /**
   * {@inheritDoc}
   * 
   * <p>The default implementation returns an empty map. Concrete implementations may override
   * this method to provide meaningful label-to-value mappings based on their specific
   * labeling schemes.</p>
   * 
   * @return an empty immutable map by default
   */
  @Override
  default Map<String, Object> labelledMap() {
    return Collections.emptyMap();
  }
}
