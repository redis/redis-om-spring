package com.redis.om.spring.repository.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery;
import org.springframework.data.keyvalue.repository.query.SpelQueryCreator;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.MappingConfiguration;
import org.springframework.data.redis.core.index.IndexConfiguration;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.PersistentEntityInformation;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ValueExpressionDelegate;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.mapping.RedisEnhancedMappingContext;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.repository.query.RedisEnhancedQuery;
import com.redis.om.spring.vectorize.Embedder;

/**
 * Factory for creating Redis enhanced repositories.
 * <p>
 * This factory extends Spring Data's RepositoryFactorySupport to create repository instances
 * specifically designed for Redis hash storage with enhanced search capabilities. It integrates
 * with Redis OM Spring's features including search indexing, query execution, and AI-powered embeddings.
 * </p>
 * <p>
 * Unlike the document repository factory which stores entities as Redis JSON documents,
 * this factory creates repositories that store entities as Redis hashes while providing
 * the same powerful search and indexing capabilities through RediSearch.
 * </p>
 * <p>
 * The factory handles the creation of repository proxies that support:
 * <ul>
 * <li>Redis hash operations with enhanced mapping</li>
 * <li>RediSearch index management for hash fields</li>
 * <li>Custom query execution via SpEL</li>
 * <li>Vector similarity search capabilities</li>
 * <li>Automatic embedding generation</li>
 * </ul>
 *
 * @see org.springframework.data.repository.core.support.RepositoryFactorySupport
 * @see com.redis.om.spring.repository.RedisEnhancedRepository
 * @since 0.1.0
 */
public class RedisEnhancedRepositoryFactory extends RepositoryFactorySupport {

  private static final Class<SpelQueryCreator> DEFAULT_QUERY_CREATOR = SpelQueryCreator.class;

  private final KeyValueOperations keyValueOperations;
  private final RedisOperations<?, ?> redisOperations;
  private final RedisModulesOperations<?> rmo;
  private final RediSearchIndexer indexer;
  private final Class<? extends AbstractQueryCreator<?, ?>> queryCreator;
  private final Class<? extends RepositoryQuery> repositoryQueryType;
  private final Embedder embedder;
  private final RedisOMProperties properties;
  private final RedisEnhancedMappingContext enhancedContext;

  /**
   * Creates a new {@link KeyValueRepositoryFactory} for the given
   * {@link KeyValueOperations}.
   *
   * @param keyValueOperations must not be {@literal null}.
   * @param redisOperations    must not be {@literal null}.
   * @param rmo                must not be {@literal null}.
   * @param indexer            must not be {@literal null}.
   * @param embedder           must not be {@literal null}.
   * @param properties         must not be {@literal null}.
   */
  public RedisEnhancedRepositoryFactory( //
      KeyValueOperations keyValueOperations, //
      RedisOperations<?, ?> redisOperations, //
      RedisModulesOperations<?> rmo, //
      RediSearchIndexer indexer, //
      Embedder embedder, //
      RedisOMProperties properties) {
    this(keyValueOperations, redisOperations, rmo, indexer, embedder, DEFAULT_QUERY_CREATOR, properties);
  }

  /**
   * Creates a new {@link KeyValueRepositoryFactory} for the given
   * {@link KeyValueOperations} and {@link AbstractQueryCreator}-type.
   *
   * @param keyValueOperations must not be {@literal null}.
   * @param redisOperations    must not be {@literal null}.
   * @param rmo                must not be {@literal null}.
   * @param indexer            must not be {@literal null}.
   * @param embedder           must not be {@literal null}.
   * @param queryCreator       must not be {@literal null}.
   * @param properties         must not be {@literal null}.
   */
  public RedisEnhancedRepositoryFactory( //
      KeyValueOperations keyValueOperations, //
      RedisOperations<?, ?> redisOperations, //
      RedisModulesOperations<?> rmo, //
      RediSearchIndexer indexer, //
      Embedder embedder, //
      Class<? extends AbstractQueryCreator<?, ?>> queryCreator, //
      RedisOMProperties properties) {

    this( //
        keyValueOperations, //
        redisOperations, //
        rmo, //
        indexer, //
        embedder, //
        queryCreator, //
        RedisEnhancedQuery.class, //
        properties //
    ); //
  }

  /**
   * Creates a new {@link RedisEnhancedRepositoryFactory} for the given
   * {@link KeyValueOperations} and {@link AbstractQueryCreator}-type.
   *
   * @param keyValueOperations  must not be {@literal null}.
   * @param redisOperations     must not be {@literal null}.
   * @param rmo                 must not be {@literal null}.
   * @param indexer             must not be {@literal null}.
   * @param embedder            must not be {@literal null}.
   * @param queryCreator        must not be {@literal null}.
   * @param repositoryQueryType must not be {@literal null}.
   * @param properties          must not be {@literal null}.
   */
  public RedisEnhancedRepositoryFactory( //
      KeyValueOperations keyValueOperations, //
      RedisOperations<?, ?> redisOperations, //
      RedisModulesOperations<?> rmo, //
      RediSearchIndexer indexer, //
      Embedder embedder, //
      Class<? extends AbstractQueryCreator<?, ?>> queryCreator, //
      Class<? extends RepositoryQuery> repositoryQueryType, //
      RedisOMProperties properties) {

    Assert.notNull(keyValueOperations, "KeyValueOperations must not be null!");
    Assert.notNull(redisOperations, "RedisOperations must not be null!");
    Assert.notNull(rmo, "RedisModulesOperations must not be null!");
    Assert.notNull(queryCreator, "Query creator type must not be null!");
    Assert.notNull(repositoryQueryType, "RepositoryQueryType type must not be null!");
    Assert.notNull(embedder, "FeatureExtractor type must not be null!");
    Assert.notNull(properties, "RedisOMSpringProperties type must not be null!");

    this.keyValueOperations = keyValueOperations;
    this.redisOperations = redisOperations;
    this.rmo = rmo;
    this.indexer = indexer;
    this.queryCreator = queryCreator;
    this.repositoryQueryType = repositoryQueryType;
    this.embedder = embedder;
    this.properties = properties;
    MappingConfiguration mappingConfiguration = new MappingConfiguration(new IndexConfiguration(),
        new KeyspaceConfiguration());
    this.enhancedContext = new RedisEnhancedMappingContext(mappingConfiguration);
  }

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.repository.core.support.RepositoryFactorySupport#
   * getEntityInformation(java.lang.Class) */
  @Override
  @SuppressWarnings(
    "unchecked"
  )
  public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
    PersistentEntity<T, ?> entity = (PersistentEntity<T, ?>) enhancedContext.getRequiredPersistentEntity(domainClass);
    return new PersistentEntityInformation<>(entity);
  }

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.repository.core.support.RepositoryFactorySupport#
   * getTargetRepository(org.springframework.data.repository.core.
   * RepositoryMetadata) */
  @Override
  protected Object getTargetRepository(RepositoryInformation repositoryInformation) {
    EntityInformation<?, ?> entityInformation = getEntityInformation(repositoryInformation.getDomainType());
    return super.getTargetRepositoryViaReflection(repositoryInformation, entityInformation, keyValueOperations, rmo,
        indexer, embedder, properties);
  }

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.repository.core.support.RepositoryFactorySupport#
   * getRepositoryBaseClass(org.springframework.data.repository.core.
   * RepositoryMetadata) */
  @Override
  protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
    return SimpleRedisEnhancedRepository.class;
  }

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.repository.core.support.RepositoryFactorySupport#
   * getQueryLookupStrategy(org.springframework.data.repository.query.
   * QueryLookupStrategy.Key,
   * org.springframework.data.repository.query.EvaluationContextProvider) */
  @Override
  protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable Key key,
      ValueExpressionDelegate valueExpressionDelegate) {
    return Optional.of(new RedisEnhancedQueryLookupStrategy(key, valueExpressionDelegate, this.keyValueOperations, //
        this.redisOperations, //
        this.rmo, //
        this.indexer, //
        this.properties, //
        this.queryCreator, //
        this.repositoryQueryType //
    ));
  }

  /**
   * @author Christoph Strobl
   * @author Oliver Gierke
   */
  private static class RedisEnhancedQueryLookupStrategy implements QueryLookupStrategy {

    private final ValueExpressionDelegate valueExpressionDelegate;
    private final KeyValueOperations keyValueOperations;
    private final RedisModulesOperations<?> rmo;
    private final RedisOperations<?, ?> redisOperations;

    private final Class<? extends AbstractQueryCreator<?, ?>> queryCreator;
    private final Class<? extends RepositoryQuery> repositoryQueryType;
    private final RedisOMProperties properties;
    private final RediSearchIndexer indexer;

    /**
     * @param key
     * @param valueExpressionDelegate
     * @param keyValueOperations
     * @param queryCreator
     * @since 1.1
     */
    public RedisEnhancedQueryLookupStrategy( //
        @Nullable Key key, //
        ValueExpressionDelegate valueExpressionDelegate, //
        KeyValueOperations keyValueOperations, //
        RedisOperations<?, ?> redisOperations, //
        RedisModulesOperations<?> rmo, //
        RediSearchIndexer indexer, //
        RedisOMProperties properties, //
        Class<? extends AbstractQueryCreator<?, ?>> queryCreator, //
        Class<? extends RepositoryQuery> repositoryQueryType) {

      Assert.notNull(valueExpressionDelegate, "ValueExpressionDelegate must not be null!");
      Assert.notNull(keyValueOperations, "KeyValueOperations must not be null!");
      Assert.notNull(redisOperations, "RedisOperations must not be null!");
      Assert.notNull(rmo, "RedisModulesOperations must not be null!");
      Assert.notNull(properties, "RedisOMSpringProperties must not be null!");
      Assert.notNull(queryCreator, "Query creator type must not be null!");
      Assert.notNull(repositoryQueryType, "RepositoryQueryType type must not be null!");

      this.valueExpressionDelegate = valueExpressionDelegate;
      this.keyValueOperations = keyValueOperations;
      this.redisOperations = redisOperations;
      this.rmo = rmo;
      this.indexer = indexer;
      this.properties = properties;
      this.queryCreator = queryCreator;
      this.repositoryQueryType = repositoryQueryType;
    }

    /* (non-Javadoc)
     *
     * @see
     * org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(
     * java.lang.reflect.Method,
     * org.springframework.data.repository.core.RepositoryMetadata,
     * org.springframework.data.projection.ProjectionFactory,
     * org.springframework.data.repository.core.NamedQueries) */
    @Override
    @SuppressWarnings(
      "unchecked"
    )
    public RepositoryQuery resolveQuery( //
        Method method, //
        RepositoryMetadata metadata, //
        ProjectionFactory factory, //
        NamedQueries namedQueries //
    ) {
      QueryMethod queryMethod = new QueryMethod(method, metadata, factory);

      Constructor<? extends KeyValuePartTreeQuery> constructor = (Constructor<? extends KeyValuePartTreeQuery>) ClassUtils
          .getConstructorIfAvailable(
              //
              this.repositoryQueryType, //
              QueryMethod.class, //
              RepositoryMetadata.class, //
              RediSearchIndexer.class, //
              ValueExpressionDelegate.class, //
              KeyValueOperations.class, //
              RedisOperations.class, //
              RedisModulesOperations.class, //
              Class.class, //
              RedisOMProperties.class);

      Assert.state(constructor != null, String.format(
          "Constructor %s(QueryMethod, RepositoryMetadata, RediSearchIndexer, ValueExpressionDelegate, KeyValueOperations, RedisOperations, RedisModulesOperations, Class, RedisOMSpringProperties) not available!",
          ClassUtils.getShortName(this.repositoryQueryType)));

      return BeanUtils.instantiateClass(constructor, //
          queryMethod, //
          metadata, //
          indexer, //
          valueExpressionDelegate, //
          this.keyValueOperations, //
          this.redisOperations, //
          this.rmo, //
          this.queryCreator, //
          this.properties //
      );
    }
  }
}