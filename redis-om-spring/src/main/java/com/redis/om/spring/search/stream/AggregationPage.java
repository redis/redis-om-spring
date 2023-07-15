package com.redis.om.spring.search.stream;

import com.google.gson.Gson;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.util.ObjectUtils;
import org.springframework.data.domain.*;
import org.springframework.util.Assert;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.util.SafeEncoder;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class AggregationPage<E> implements Slice<E>, Serializable {
  private List<E> content;
  private final transient Pageable pageable;
  private transient AggregationStream<E> aggregationStream;
  private long cursorId = -1;
  private AggregationResult aggregationResult;
  private final transient Gson gson;
  private final Class<E> entityClass;
  private final boolean isDocument;
  private final transient MappingRedisOMConverter mappingConverter;

  public AggregationPage(AggregationStream<E> aggregationStream, Pageable pageable, Class<E> entityClass, Gson gson, MappingRedisOMConverter mappingConverter, boolean isDocument) {
    this.aggregationStream = aggregationStream;
    this.pageable = pageable;
    this.entityClass = entityClass;
    this.gson = gson;
    this.isDocument = isDocument;
    this.mappingConverter = mappingConverter;
  }

  public AggregationPage(AggregationResult aggregationResult, Pageable pageable, Class<E> entityClass, Gson gson, MappingRedisOMConverter mappingConverter, boolean isDocument) {
    this.aggregationResult = aggregationResult;
    this.pageable = pageable;
    this.entityClass = entityClass;
    this.gson = gson;
    this.cursorId = aggregationResult.getCursorId();
    this.isDocument = isDocument;
    this.mappingConverter = mappingConverter;
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
  public  List<E> getContent() {
    return resolveContent();
  }

  @Override
  public boolean hasContent() {
    return !resolveContent().isEmpty();
  }

  @Override
  public  Sort getSort() {
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
    return cursorId == -1 || resolveCursorId() != 0;
  }

  @Override
  public  Pageable nextPageable() {
    Pageable next = PageRequest.of(getNumber() + 1, pageable.getPageSize(), pageable.getSort());
    return hasNext() ? new AggregationPageable(next, resolveAggregation().getCursorId()) : Pageable.unpaged();
  }

  @Override
  public <U>  Slice<U> map(Function<? super E, ? extends U> converter) {
    return new SliceImpl<>(getConvertedContent(converter), pageable, hasNext());
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
  public  Pageable previousPageable() {
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

  protected <U> List<U> getConvertedContent(Function<? super E, ? extends U> converter) {

    Assert.notNull(converter, "Function must not be null");

    return (List<U>) this.stream().map(converter::apply).toList();
  }

  List<E> toEntityList(AggregationResult aggregationResult) {
    if (isDocument) {
      return aggregationResult.getResults().stream().map(d -> gson.fromJson(d.get("$").toString(), entityClass)).toList();
    } else {
      return aggregationResult.getResults().stream().map(h -> (E) ObjectUtils.mapToObject(h, entityClass, mappingConverter)).toList();
    }
  }
}
