package com.redis.om.spring.search.stream;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * A specialized Pageable implementation that supports cursor-based pagination
 * for Redis aggregation operations. This class extends standard pagination
 * with cursor functionality for efficient processing of large result sets.
 * 
 * <p>Cursor-based pagination is particularly useful for aggregation queries
 * that return large amounts of data, as it allows for streaming processing
 * without loading all results into memory at once.</p>
 * 
 * <p>The cursor ID tracks the position in the result set, allowing for
 * efficient continuation of pagination across multiple requests.</p>
 * 
 * @since 1.0
 * @see Pageable
 * @see AggregationStream#cursor(int, java.time.Duration)
 */
public class AggregationPageable implements Pageable {

  private final Pageable pageable;
  private final long cursorId;

  /**
   * Creates a new AggregationPageable with cursor support.
   * 
   * @param pageable the underlying pageable with standard pagination parameters
   * @param cursorId the cursor ID for tracking position in the result set
   */
  public AggregationPageable(Pageable pageable, long cursorId) {
    this.pageable = pageable;
    this.cursorId = cursorId;
  }

  /**
   * Returns the cursor ID for this pageable instance.
   * The cursor ID tracks the current position in the aggregation result set.
   * 
   * @return the cursor ID, or -1 if no cursor is active
   */
  public long getCursorId() {
    return cursorId;
  }

  @Override
  public int getPageNumber() {
    return pageable.getPageNumber();
  }

  @Override
  public int getPageSize() {
    return pageable.getPageSize();
  }

  @Override
  public long getOffset() {
    return pageable.getOffset();
  }

  @Override
  public Sort getSort() {
    return pageable.getSort();
  }

  @Override
  public Pageable next() {
    return null;
  }

  @Override
  public Pageable previousOrFirst() {
    return null;
  }

  @Override
  public Pageable first() {
    return null;
  }

  @Override
  public Pageable withPage(int pageNumber) {
    return null;
  }

  @Override
  public boolean hasPrevious() {
    return false;
  }
}
