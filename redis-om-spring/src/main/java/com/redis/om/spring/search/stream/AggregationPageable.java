package com.redis.om.spring.search.stream;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class AggregationPageable implements Pageable {

  private final Pageable pageable;
  private final long cursorId;

  public AggregationPageable(Pageable pageable, long cursorId) {
    this.pageable = pageable;
    this.cursorId = cursorId;
  }

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
