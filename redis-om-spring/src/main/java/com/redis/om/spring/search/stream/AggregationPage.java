package com.redis.om.spring.search.stream;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.Assert;

import com.google.gson.Gson;
import com.redis.om.spring.annotations.Dialect;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationResult;

/**
 * Implementation of Spring Data's Page interface for Redis aggregation results.
 * This class provides pagination support for aggregation queries, allowing large
 * result sets to be processed in manageable chunks.
 * 
 * <p>AggregationPage supports both cursor-based and offset-based pagination
 * depending on the underlying aggregation configuration. It handles the conversion
 * of raw Redis aggregation results into typed entities.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 * <li>Lazy loading of aggregation results</li>
 * <li>Support for cursor-based pagination for large datasets</li>
 * <li>Automatic conversion from Redis results to entity objects</li>
 * <li>Integration with Spring Data pagination abstractions</li>
 * </ul>
 * 
 * @param <E> the entity type of the page content
 * 
 * @since 1.0
 * @see Page
 * @see AggregationStream
 */
public class AggregationPage<E> implements Page<E>, Serializable {
  private final transient Pageable pageable;
  private final transient Gson gson;
  /** The class type of the entities in this page. */
  private final Class<E> entityClass;
  /** Flag indicating whether entities are stored as JSON documents (true) or hashes (false). */
  private final boolean isDocument;
  private final transient MappingRedisOMConverter mappingConverter;
  /** The search operations instance used for executing aggregation queries. */
  private final SearchOperations<String> search;
  /** The actual content of this page, lazily populated from aggregation results. */
  private List<E> content;
  private transient AggregationStream<E> aggregationStream;
  /** The cursor ID for pagination, -1 indicates no cursor. */
  private long cursorId = -1;
  /** The raw aggregation result from Redis, used for content extraction. */
  private AggregationResult aggregationResult;
  /** The total number of elements across all pages, cached after first calculation. */
  private Long totalElementCount;

  /**
   * Creates a new AggregationPage from an aggregation stream.
   * This constructor is used for lazy evaluation where the aggregation
   * is executed when the page content is first accessed.
   * 
   * @param aggregationStream the aggregation stream to execute
   * @param pageable          the pagination parameters
   * @param entityClass       the class of entities in this page
   * @param gson              the JSON serializer for entity conversion
   * @param mappingConverter  the Redis mapping converter
   * @param isDocument        whether entities are document-based or hash-based
   * @param search            the search operations for executing queries
   */
  public AggregationPage(AggregationStream<E> aggregationStream, Pageable pageable, Class<E> entityClass, Gson gson,
      MappingRedisOMConverter mappingConverter, boolean isDocument, SearchOperations<String> search) {
    this.aggregationStream = aggregationStream;
    this.pageable = pageable;
    this.entityClass = entityClass;
    this.gson = gson;
    this.isDocument = isDocument;
    this.mappingConverter = mappingConverter;
    this.search = search;
  }

  /**
   * Creates a new AggregationPage from pre-executed aggregation results.
   * This constructor is used when the aggregation has already been executed
   * and the results are available.
   * 
   * @param aggregationResult the pre-executed aggregation results
   * @param pageable          the pagination parameters
   * @param entityClass       the class of entities in this page
   * @param gson              the JSON serializer for entity conversion
   * @param mappingConverter  the Redis mapping converter
   * @param isDocument        whether entities are document-based or hash-based
   * @param search            the search operations for executing queries
   */
  public AggregationPage(AggregationResult aggregationResult, Pageable pageable, Class<E> entityClass, Gson gson,
      MappingRedisOMConverter mappingConverter, boolean isDocument, SearchOperations<String> search) {
    this.aggregationResult = aggregationResult;
    this.pageable = pageable;
    this.entityClass = entityClass;
    this.gson = gson;
    this.cursorId = aggregationResult.getCursorId();
    this.isDocument = isDocument;
    this.mappingConverter = mappingConverter;
    this.search = search;
  }

  @Override
  public int getNumber() {
    return pageable.getPageNumber();
  }

  @Override
  public int getSize() {
    return resolveAggregation().getResults().size();
  }

  @Override
  public int getNumberOfElements() {
    return getSize();
  }

  @Override
  public List<E> getContent() {
    return resolveContent();
  }

  @Override
  public boolean hasContent() {
    return !resolveContent().isEmpty();
  }

  @Override
  public Sort getSort() {
    return pageable.getSort();
  }

  @Override
  public boolean isFirst() {
    return getNumber() == 0;
  }

  @Override
  public boolean isLast() {
    return resolveCursorId() == 0;
  }

  @Override
  public boolean hasNext() {
    //    return cursorId == -1 || resolveCursorId() != 0;
    return aggregationStream != null ? getNumber() + 1 < getTotalPages() : cursorId == -1 || resolveCursorId() != 0;
  }

  @Override
  public Pageable nextPageable() {
    Pageable next = PageRequest.of(getNumber() + 1, pageable.getPageSize(), pageable.getSort());
    return hasNext() ? new AggregationPageable(next, resolveAggregation().getCursorId()) : Pageable.unpaged();
  }

  @Override
  public int getTotalPages() {
    return (getTotalElements() == 0 || pageable.getPageSize() == 0) ?
        0 :
        (int) Math.ceil((double) getTotalElements() / (double) pageable.getPageSize());
  }

  @Override
  public long getTotalElements() {
    if (totalElementCount == null) {
      if (aggregationStream != null) {
        String baseQuery = aggregationStream.backingQuery();
        Query countQuery = (baseQuery.isBlank()) ? new Query() : new Query(baseQuery);
        countQuery.setNoContent();
        countQuery.dialect(Dialect.TWO.getValue());
        for (Order order : pageable.getSort()) {
          countQuery.setSortBy(order.getProperty(), order.isAscending());
        }
        SearchResult searchResult = search.search(countQuery);
        totalElementCount = searchResult.getTotalResults();
      } else {
        totalElementCount = aggregationResult.getTotalResults(); // not quite sure about this shit
      }
    }
    return totalElementCount;
  }

  @Override
  public <U> Page<U> map(Function<? super E, ? extends U> converter) {
    return new PageImpl<>(getConvertedContent(converter));
  }

  @Override
  public Iterator<E> iterator() {
    return resolveContent().iterator();
  }

  // Unsupported operations - there is no backwards navigation with RediSearch Cursors
  @Override
  public boolean hasPrevious() {
    return false;
  }

  @Override
  public Pageable previousPageable() {
    return Pageable.unpaged();
  }
  // END - Unsupported operations

  private AggregationResult resolveAggregation() {
    if (aggregationResult == null) {
      aggregationResult = aggregationStream.aggregate();
    }
    return aggregationResult;
  }

  private List<E> resolveContent() {
    if (content == null) {
      this.content = toEntityList(resolveAggregation());
    }
    return content;
  }

  private long resolveCursorId() {
    if (cursorId != -1) {
      cursorId = resolveAggregation().getCursorId();
    }
    return cursorId;
  }

  /**
   * Converts the page content using the provided converter function.
   * This method applies the converter to each element in the page and returns
   * a new list with the converted elements.
   * 
   * @param <U>       the target type for conversion
   * @param converter the function to apply to each element for conversion
   * @return a list of converted elements
   * @throws IllegalArgumentException if converter is null
   */
  @SuppressWarnings(
    "unchecked"
  )
  protected <U> List<U> getConvertedContent(Function<? super E, ? extends U> converter) {

    Assert.notNull(converter, "Function must not be null");

    return (List<U>) this.stream().map(converter).toList();
  }

  @SuppressWarnings(
    "unchecked"
  )
  List<E> toEntityList(AggregationResult aggregationResult) {
    if (isDocument) {
      return aggregationResult.getResults().stream().map(d -> gson.fromJson(d.get("$").toString(), entityClass))
          .toList();
    } else {
      return aggregationResult.getResults().stream().map(h -> (E) ObjectUtils.mapToObject(h, entityClass,
          mappingConverter)).toList();
    }
  }
}
