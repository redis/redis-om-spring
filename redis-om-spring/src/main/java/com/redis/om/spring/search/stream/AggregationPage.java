package com.redis.om.spring.search.stream;

import com.google.gson.Gson;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.util.ObjectUtils;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.Assert;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationResult;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class AggregationPage<E> implements Page<E>, Serializable {
  private final transient Pageable pageable;
  private final transient Gson gson;
  private final Class<E> entityClass;
  private final boolean isDocument;
  private final transient MappingRedisOMConverter mappingConverter;
  private final SearchOperations<String> search;
  private List<E> content;
  private transient AggregationStream<E> aggregationStream;
  private long cursorId = -1;
  private AggregationResult aggregationResult;
  private Long totalElementCount;

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

  @SuppressWarnings("unchecked")
  protected <U> List<U> getConvertedContent(Function<? super E, ? extends U> converter) {

    Assert.notNull(converter, "Function must not be null");

    return (List<U>) this.stream().map(converter).toList();
  }

  @SuppressWarnings("unchecked")
  List<E> toEntityList(AggregationResult aggregationResult) {
    if (isDocument) {
      return aggregationResult.getResults().stream().map(d -> gson.fromJson(d.get("$").toString(), entityClass))
          .toList();
    } else {
      return aggregationResult.getResults().stream()
          .map(h -> (E) ObjectUtils.mapToObject(h, entityClass, mappingConverter)).toList();
    }
  }
}
