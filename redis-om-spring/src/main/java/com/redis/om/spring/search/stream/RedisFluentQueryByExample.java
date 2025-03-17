package com.redis.om.spring.search.stream;

import com.redis.om.spring.annotations.Dialect;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.MetamodelUtils;
import com.redis.om.spring.ops.search.SearchOperations;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.convert.DtoInstantiatingConverter;
import org.springframework.data.domain.*;
import org.springframework.data.mapping.model.EntityInstantiators;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RedisFluentQueryByExample<T, S extends T, R> implements FetchableFluentQuery<R> {

  private final Example<S> example;
  private final Sort sort;
  private final Class<?> domainType;
  private final Class<R> resultType;

  private final SearchOperations<String> searchOps;
  private final SearchStream<R> searchStream;
  private SearchStream<?> parentSearchStream;
  private final EntityStream entityStream;
  private boolean isProjection = false;
  private final SpelAwareProxyProjectionFactory projectionFactory;
  private final RedisMappingContext mappingContext;
  private final EntityInstantiators entityInstantiators = new EntityInstantiators();
  private Function<Object, R> conversionFunction;

  public RedisFluentQueryByExample( //
      Example<S> example, //
      Class<R> resultType, //
      EntityStream entityStream, //
      SearchOperations<String> searchOps, //
      RedisMappingContext mappingContext
  ) {
    this(example, Sort.unsorted(), resultType, resultType, entityStream, searchOps, mappingContext);
  }

  public RedisFluentQueryByExample( //
      Example<S> example, //
      Sort sort, //
      Class<?> domainType, //
      Class<R> resultType, //
      EntityStream entityStream, //
      SearchOperations<String> searchOps, //
      SearchStream<?> searchStream, //
      RedisMappingContext mappingContext
  ) {
    this.example = example;
    this.sort = sort;
    this.domainType = domainType;
    this.resultType = resultType;
    this.entityStream = entityStream;
    this.searchOps = searchOps;
    this.searchStream = null;
    this.parentSearchStream = searchStream;
    this.isProjection = true;
    this.projectionFactory = new SpelAwareProxyProjectionFactory();
    this.mappingContext = mappingContext;
    this.conversionFunction = getConversionFunction(domainType, resultType);
  }

  public RedisFluentQueryByExample( //
      Example<S> example, //
      Sort sort, //
      Class<?> domainType, //
      Class<R> resultType, //
      EntityStream entityStream, //
      SearchOperations<String> searchOps, //
      RedisMappingContext mappingContext
  ) {
    this.example = example;
    this.sort = sort;
    this.domainType = domainType;
    this.resultType = resultType;
    this.entityStream = entityStream;
    this.searchOps = searchOps;
    this.searchStream = entityStream.of(resultType);
    this.projectionFactory = new SpelAwareProxyProjectionFactory();
    this.mappingContext = mappingContext;
    searchStream.dialect(Dialect.TWO.getValue());
    searchStream.filter((Example<R>)example);
  }

  @Override
  public FetchableFluentQuery<R> sortBy(Sort sort) {
    searchStream.sorted(sort);
    return this;
  }

  @Override
  public <R1> FetchableFluentQuery<R1> as(Class<R1> resultType) {
    return new RedisFluentQueryByExample<>(example, sort, domainType, resultType, this.entityStream, this.searchOps, searchStream, mappingContext);
  }

  @Override
  public FetchableFluentQuery<R> project(Collection<String> properties) {
    List<MetamodelField<?, ?>> metamodelFields = MetamodelUtils.getMetamodelFieldsForProperties(resultType, properties);
    metamodelFields.forEach(mmf -> searchStream.project((MetamodelField<? super R, ?>) mmf));
    return this;
  }

  @Nullable
  @Override
  public R oneValue() {
    Iterator<R> iterator = !isProjection ? searchStream.iterator() : (Iterator<R>) parentSearchStream.iterator();

    R result = null;
    if (iterator.hasNext()) {
      result = iterator.next();
      if (iterator.hasNext()) {
        throw new IncorrectResultSizeDataAccessException("Query returned non unique result", 1);
      }
    }

    return !isProjection ? result : conversionFunction.apply(result);
  }

  @Nullable
  @Override
  public R firstValue() {
    var result = !isProjection ? searchStream.findFirst().orElse(null) : parentSearchStream.findFirst().orElse(null);
    return !isProjection ? (R) result : conversionFunction.apply(result);
  }

  @Override
  public List<R> all() {
    if (!isProjection) {
      return searchStream.collect(Collectors.toList());
    } else {
      return parentSearchStream.collect(Collectors.toList()).stream().map(this.conversionFunction).toList();
    }
  }

  @Override
  public Page<R> page(Pageable pageable) {
    Assert.notNull(pageable, "Pageable must not be null");

    long count = -1;
    if (!searchStream.backingQuery().isBlank()) {
      Query query = new Query(searchStream.backingQuery());
      query.dialect(Dialect.TWO.getValue());
      query.limit(0, 0);
      SearchResult searchResult = searchOps.search(query);

      count = searchResult.getTotalResults();
    } else {
      var info = searchOps.getInfo();
      count = (long) info.get("num_docs");
    }

    var pageContents = searchStream.limit(pageable.getPageSize()).skip(pageable.getOffset())
        .collect(Collectors.toList());
    return new PageImpl<>(pageContents, pageable, count);
  }

  @Override
  public Stream<R> stream() {
    return all().stream();
  }

  @Override
  public long count() {
    return searchStream.count();
  }

  @Override
  public boolean exists() {
    return count() > 0;
  }

  private <P> Function<Object, P> getConversionFunction(Class<?> inputType, Class<P> targetType) {

    if (targetType.isAssignableFrom(inputType)) {
      return (Function<Object, P>) Function.identity();
    }

    if (targetType.isInterface()) {
      return o -> projectionFactory.createProjection(targetType, o);
    }

    DtoInstantiatingConverter converter = new DtoInstantiatingConverter(targetType,
        this.mappingContext, entityInstantiators);

    return o -> (P) converter.convert(o);
  }
}
