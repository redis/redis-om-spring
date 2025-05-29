package com.redis.om.spring.repository.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.config.QueryCreatorType;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.MappingConfiguration;
import org.springframework.data.redis.core.index.IndexConfiguration;
import org.springframework.data.redis.repository.support.RedisRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.mapping.RedisEnhancedMappingContext;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.repository.query.RedisEnhancedQuery;
import com.redis.om.spring.vectorize.Embedder;

/**
 * Factory bean for creating Redis enhanced repository factory instances.
 * <p>
 * This factory bean integrates with Spring's IoC container to provide repository instances
 * for Redis hash storage with enhanced search capabilities. It extends Spring Data's
 * RepositoryFactoryBeanSupport to add Redis OM Spring specific functionality including
 * search indexing, vector embeddings, and custom query execution.
 * </p>
 * <p>
 * Unlike the document repository factory bean which creates repositories for Redis JSON
 * document storage, this factory bean creates repositories that work with Redis hashes
 * while providing the same powerful search and indexing capabilities through RediSearch.
 * </p>
 * <p>
 * The factory bean automatically configures:
 * <ul>
 * <li>Redis operations for hash storage with enhanced mapping</li>
 * <li>Search indexing through RediSearchIndexer for hash fields</li>
 * <li>AI embedding capabilities for vector search</li>
 * <li>Enhanced mapping context for entity relationships</li>
 * <li>Query execution infrastructure for hash-based entities</li>
 * </ul>
 *
 * @param <T>  the repository type
 * @param <S>  the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
 * @see RedisEnhancedRepositoryFactory
 * @since 0.1.0
 */
public class RedisEnhancedRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends
    RepositoryFactoryBeanSupport<T, S, ID> {

  @Autowired
  private @Nullable RedisModulesOperations<String> rmo;
  @Autowired
  private @Nullable RedisOperations<?, ?> redisOperations;
  @Autowired
  private @Nullable RediSearchIndexer indexer;
  @Autowired
  private @Nullable Embedder embedder;
  @Autowired
  private RedisOMProperties properties;

  private @Nullable KeyValueOperations operations;
  private @Nullable Class<? extends AbstractQueryCreator<?, ?>> queryCreator;
  private @Nullable Class<? extends RepositoryQuery> repositoryQueryType;

  /**
   * Creates a new {@link RedisRepositoryFactoryBean} for the given repository
   * interface.
   *
   * @param repositoryInterface must not be {@literal null}.
   */
  public RedisEnhancedRepositoryFactoryBean( //
      Class<? extends T> repositoryInterface) {
    super(repositoryInterface);
  }

  private void setFeatureExtractor(Embedder embedder) {
    Assert.notNull(rmo, "FeatureExtractor must not be null!");

    this.embedder = embedder;
  }

  /**
   * Configures the {@link KeyValueOperations} to be used for the repositories.
   *
   * @param operations must not be {@literal null}.
   */
  public void setKeyValueOperations(KeyValueOperations operations) {

    Assert.notNull(operations, "KeyValueOperations must not be null!");

    this.operations = operations;
  }

  /**
   * Configures the {@link RedisModulesOperations} to be used for the
   * repositories.
   *
   * @param rmo must not be {@literal null}.
   */
  @SuppressWarnings(
    "unchecked"
  )
  public void setRedisModulesOperations(RedisModulesOperations<?> rmo) {
    Assert.notNull(rmo, "RedisModulesOperations must not be null!");

    this.rmo = (RedisModulesOperations<String>) rmo;
  }

  /**
   * Configures the {@link RedisOperations} to be used for the repositories.
   *
   * @param redisOperations must not be {@literal null}.
   */
  public void setRedisOperations(RedisOperations<?, ?> redisOperations) {
    Assert.notNull(redisOperations, "RedisOperations must not be null!");
    this.redisOperations = redisOperations;
  }

  /**
   * Configures the Redis OM Spring properties for the repositories.
   *
   * @param properties must not be {@literal null}.
   */
  public void setRedisOMSpringProperties(RedisOMProperties properties) {
    Assert.notNull(properties, "RedisOMSpringProperties must not be null!");
    this.properties = properties;
  }

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
   * #setMappingContext(org.springframework.data.mapping.context.
   * MappingContext) */
  @Override
  public void setMappingContext(MappingContext<?, ?> mappingContext) {
    // Create our own mapping configuration
    MappingConfiguration mappingConfiguration = new MappingConfiguration(new IndexConfiguration(),
        new KeyspaceConfiguration());

    // Create our enhanced context
    RedisEnhancedMappingContext enhancedContext = new RedisEnhancedMappingContext(mappingConfiguration);

    // Set it to be our mapping context
    super.setMappingContext(enhancedContext);
  }

  /**
   * Configures the RediSearch indexer for managing search indexes.
   *
   * @param keyspaceToIndexMap must not be {@literal null}.
   */
  public void setKeyspaceToIndexMap(RediSearchIndexer keyspaceToIndexMap) {
    Assert.notNull(keyspaceToIndexMap, "RediSearchIndexer must not be null!");
    this.indexer = keyspaceToIndexMap;
  }

  /**
   * Configures the {@link QueryCreatorType} to be used.
   *
   * @param queryCreator must not be {@literal null}.
   */
  public void setQueryCreator(Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {

    Assert.notNull(queryCreator, "Query creator type must not be null!");

    this.queryCreator = queryCreator;
  }

  /**
   * Configures the {@link RepositoryQuery} type to be created.
   *
   * @param repositoryQueryType must not be {@literal null}.
   * @since 1.1
   */
  public void setQueryType(Class<? extends RepositoryQuery> repositoryQueryType) {
    Assert.notNull(queryCreator, "Query creator type must not be null!");
    this.repositoryQueryType = repositoryQueryType;
  }

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
   * #createRepositoryFactory() */
  @Override
  protected final RepositoryFactorySupport createRepositoryFactory() {
    return createRepositoryFactory(operations, queryCreator, repositoryQueryType);
  }

  /**
   * Create the repository factory to be used to create repositories.
   *
   * @param operations          will never be {@literal null}.
   * @param queryCreator        will never be {@literal null}.
   * @param repositoryQueryType will never be {@literal null}.
   * @return must not be {@literal null}.
   */
  protected RedisEnhancedRepositoryFactory createRepositoryFactory( //
      KeyValueOperations operations, //
      Class<? extends AbstractQueryCreator<?, ?>> queryCreator, //
      Class<? extends RepositoryQuery> repositoryQueryType //
  ) {
    return new RedisEnhancedRepositoryFactory(operations, redisOperations, rmo, indexer, embedder, queryCreator,
        RedisEnhancedQuery.class, properties);
  }

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
   * #afterPropertiesSet() */
  @Override
  public void afterPropertiesSet() {
    Assert.notNull(operations, "KeyValueOperations must not be null!");
    Assert.notNull(redisOperations, "RedisOperations must not be null!");
    Assert.notNull(rmo, "RedisModulesOperations must not be null!");
    Assert.notNull(queryCreator, "Query creator must not be null!");
    Assert.notNull(repositoryQueryType, "RepositoryQueryType must not be null!");
    Assert.notNull(indexer, "RediSearchIndexer type must not be null");
    Assert.notNull(embedder, "FeatureExtractor type must not be null!");

    super.afterPropertiesSet();
  }

}
