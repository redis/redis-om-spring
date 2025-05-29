package com.redis.om.spring.search.stream;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.convert.DtoInstantiatingConverter;
import org.springframework.data.domain.*;
import org.springframework.data.mapping.model.EntityInstantiators;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.redis.om.spring.annotations.Dialect;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.MetamodelUtils;
import com.redis.om.spring.ops.search.SearchOperations;

import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

/**
 * Redis-specific implementation of Spring Data's {@link FetchableFluentQuery} that provides
 * fluent Query By Example (QBE) capabilities for Redis entities using RediSearch.
 * 
 * <p>This implementation leverages RediSearch's powerful querying capabilities to execute
 * Query By Example operations against Redis data structures (both JSON documents and Hash
 * structures). It provides a fluent API for building, executing, and transforming query
 * results with support for projections, sorting, pagination, and result type conversion.
 * 
 * <p>Key features:
 * <ul>
 * <li><strong>Query By Example</strong>: Executes queries based on example entity instances</li>
 * <li><strong>Result Projections</strong>: Supports both interface-based and DTO projections</li>
 * <li><strong>Flexible Sorting</strong>: Integrates with Spring Data's Sort abstraction</li>
 * <li><strong>Pagination Support</strong>: Provides efficient pagination using RediSearch LIMIT</li>
 * <li><strong>Type Safety</strong>: Maintains type safety through generic parameters</li>
 * <li><strong>Stream Integration</strong>: Seamlessly integrates with Redis OM Spring's EntityStream</li>
 * </ul>
 * 
 * <p>The implementation supports two operational modes:
 * <ul>
 * <li><strong>Direct Mode</strong>: Results are returned as the target entity type</li>
 * <li><strong>Projection Mode</strong>: Results are converted to DTOs or interface projections</li>
 * </ul>
 * 
 * <p>Usage example:
 * <pre>{@code
 * // Create an example person
 * Person example = new Person();
 * example.setLastName("Smith");
 * example.setAge(25);
 * 
 * // Execute query by example
 * List<Person> people = repository.findBy(Example.of(example), query ->
 *     query.sortBy(Sort.by("firstName"))
 *          .project("firstName", "lastName")
 *          .all());
 * }</pre>
 * 
 * <p>This class integrates with Redis OM Spring's search infrastructure including
 * {@link SearchOperations}, {@link EntityStream}, and {@link SearchStream} to provide
 * high-performance search capabilities backed by Redis's native search engine.
 * 
 * @param <T> the domain type being queried
 * @param <S> the example type (subtype of T)
 * @param <R> the result type (can be same as T or a projection)
 * 
 * @author Redis OM Spring Team
 * @since 1.0.0
 * @see FetchableFluentQuery
 * @see Example
 * @see SearchOperations
 * @see EntityStream
 * @see SearchStream
 */
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

  /**
   * Constructs a new RedisFluentQueryByExample for direct entity querying without projections.
   * 
   * <p>This constructor creates a query instance where the result type is the same as the
   * domain type, meaning no type conversion or projection will be applied to the results.
   * The query will use unsorted ordering by default.
   * 
   * @param example        the example entity to use as query criteria, must not be {@literal null}
   * @param resultType     the expected result type (same as domain type), must not be {@literal null}
   * @param entityStream   the entity stream for accessing Redis OM Spring's query capabilities
   * @param searchOps      the search operations for executing RediSearch queries
   * @param mappingContext the Redis mapping context for entity metadata access
   */
  public RedisFluentQueryByExample( //
      Example<S> example, //
      Class<R> resultType, //
      EntityStream entityStream, //
      SearchOperations<String> searchOps, //
      RedisMappingContext mappingContext) {
    this(example, Sort.unsorted(), resultType, resultType, entityStream, searchOps, mappingContext);
  }

  /**
   * Constructs a new RedisFluentQueryByExample for projection-based querying.
   * 
   * <p>This constructor creates a query instance configured for projections, where the
   * result type differs from the domain type. Results will be converted from the domain
   * type to the specified result type using either interface projections or DTO conversion.
   * 
   * <p>This constructor is typically used internally when {@link #as(Class)} is called
   * to transform the result type of an existing query.
   * 
   * @param example        the example entity to use as query criteria, must not be {@literal null}
   * @param sort           the sort specification for result ordering, must not be {@literal null}
   * @param domainType     the original domain entity type being queried
   * @param resultType     the target result type for projections, must not be {@literal null}
   * @param entityStream   the entity stream for accessing Redis OM Spring's query capabilities
   * @param searchOps      the search operations for executing RediSearch queries
   * @param searchStream   the parent search stream containing the original query results
   * @param mappingContext the Redis mapping context for entity metadata and conversion
   */
  public RedisFluentQueryByExample( //
      Example<S> example, //
      Sort sort, //
      Class<?> domainType, //
      Class<R> resultType, //
      EntityStream entityStream, //
      SearchOperations<String> searchOps, //
      SearchStream<?> searchStream, //
      RedisMappingContext mappingContext) {
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

  /**
   * Constructs a new RedisFluentQueryByExample with full configuration for direct querying.
   * 
   * <p>This is the primary constructor that initializes a complete query instance with
   * sorting capabilities and prepares the underlying SearchStream with the Query By Example
   * criteria. The query is configured to use RediSearch dialect 2 for optimal compatibility
   * with modern Redis Stack installations.
   * 
   * <p>This constructor sets up the query execution pipeline by:
   * <ul>
   * <li>Creating a SearchStream for the specified result type</li>
   * <li>Configuring RediSearch dialect 2 for advanced query features</li>
   * <li>Applying the example entity as a filter to the search stream</li>
   * <li>Preparing projection factory for potential result transformations</li>
   * </ul>
   * 
   * @param example        the example entity to use as query criteria, must not be {@literal null}
   * @param sort           the sort specification for result ordering, must not be {@literal null}
   * @param domainType     the domain entity type being queried
   * @param resultType     the expected result type, must not be {@literal null}
   * @param entityStream   the entity stream for accessing Redis OM Spring's query capabilities
   * @param searchOps      the search operations for executing RediSearch queries
   * @param mappingContext the Redis mapping context for entity metadata access
   */
  public RedisFluentQueryByExample( //
      Example<S> example, //
      Sort sort, //
      Class<?> domainType, //
      Class<R> resultType, //
      EntityStream entityStream, //
      SearchOperations<String> searchOps, //
      RedisMappingContext mappingContext) {
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
    searchStream.filter((Example<R>) example);
  }

  @Override
  public FetchableFluentQuery<R> sortBy(Sort sort) {
    searchStream.sorted(sort);
    return this;
  }

  @Override
  public <R1> FetchableFluentQuery<R1> as(Class<R1> resultType) {
    return new RedisFluentQueryByExample<>(example, sort, domainType, resultType, this.entityStream, this.searchOps,
        searchStream, mappingContext);
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

    var pageContents = searchStream.limit(pageable.getPageSize()).skip(pageable.getOffset()).collect(Collectors
        .toList());
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

    DtoInstantiatingConverter converter = new DtoInstantiatingConverter(targetType, this.mappingContext,
        entityInstantiators);

    return o -> (P) converter.convert(o);
  }
}
