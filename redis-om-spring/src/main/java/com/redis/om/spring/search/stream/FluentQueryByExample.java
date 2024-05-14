package com.redis.om.spring.search.stream;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.MetamodelUtils;
import com.redis.om.spring.ops.search.SearchOperations;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FluentQueryByExample<T> implements FluentQuery.FetchableFluentQuery<T> {
  private final SearchStream<T> searchStream;
  private final Class<T> probeType;

  private final SearchOperations<String> searchOps;

  public FluentQueryByExample( //
    Example<T> example, //
    Class<T> probeType, //
    EntityStream entityStream, //
    SearchOperations<String> searchOps //
  ) {
    this.probeType = probeType;
    this.searchOps = searchOps;
    this.searchStream = entityStream.of(probeType);
    searchStream.filter(example);
  }

  @Override
  public FetchableFluentQuery<T> sortBy(Sort sort) {
    searchStream.sorted(sort);
    return this;
  }

  @Override
  public <R> FetchableFluentQuery<R> as(Class<R> resultType) {
    throw new UnsupportedOperationException("`as` is not supported on a Redis Repositories");
  }

  @Override
  @SuppressWarnings("unchecked")
  public FetchableFluentQuery<T> project(Collection<String> properties) {
    List<MetamodelField<?, ?>> metamodelFields = MetamodelUtils.getMetamodelFieldsForProperties(probeType, properties);
    metamodelFields.forEach(mmf -> searchStream.project((MetamodelField<? super T, ?>) mmf));
    return this;
  }

  @Override
  public T oneValue() {
    var result = searchStream.collect(Collectors.toList());

    if (org.springframework.util.ObjectUtils.isEmpty(result)) {
      return null;
    }

    if (result.size() > 1) {
      throw new IncorrectResultSizeDataAccessException("Query returned non unique result", 1);
    }

    return result.iterator().next();
  }

  @Override
  public T firstValue() {
    return searchStream.findFirst().orElse(null);
  }

  @Override
  public List<T> all() {
    return searchStream.collect(Collectors.toList());
  }

  @Override
  public Page<T> page(Pageable pageable) {
    Query query = (searchStream.backingQuery().isBlank()) ? new Query() : new Query(searchStream.backingQuery());
    query.limit(0, 0);
    SearchResult searchResult = searchOps.search(query);
    var count = searchResult.getTotalResults();
    var pageContents = searchStream.limit(pageable.getPageSize()).skip(pageable.getOffset())
      .collect(Collectors.toList());
    return new PageImpl<>(pageContents, pageable, count);
  }

  @Override
  public Stream<T> stream() {
    return all().stream();
  }

  @Override
  public long count() {
    return searchStream.count();
  }

  @Override
  public boolean exists() {
    return searchStream.count() > 0;
  }
}
