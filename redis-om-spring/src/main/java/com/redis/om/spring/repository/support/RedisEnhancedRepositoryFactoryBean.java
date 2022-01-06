package com.redis.om.spring.repository.support;

import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.config.QueryCreatorType;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.repository.support.RedisRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.repository.query.RedisEnhancedQuery;

public class RedisEnhancedRepositoryFactoryBean<T extends Repository<S, ID>, S, ID>
    extends RepositoryFactoryBeanSupport<T, S, ID> {

  private @Nullable KeyValueOperations operations;
  private @Nullable RedisModulesOperations<String, String> rmo;
  private @Nullable RedisOperations<?, ?> redisOperations;
  private @Nullable Class<? extends AbstractQueryCreator<?, ?>> queryCreator;
  private @Nullable Class<? extends RepositoryQuery> repositoryQueryType;

  /**
   * Creates a new {@link RedisRepositoryFactoryBean} for the given repository
   * interface.
   *
   * @param repositoryInterface must not be {@literal null}.
   */
  public RedisEnhancedRepositoryFactoryBean(Class<? extends T> repositoryInterface, RedisOperations<?, ?> redisOperations, RedisModulesOperations<?, ?> rmo) {
    super(repositoryInterface);
    setRedisModulesOperations(rmo);
    setRedisOperations(redisOperations);
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
  @SuppressWarnings("unchecked")
  public void setRedisModulesOperations(RedisModulesOperations<?, ?> rmo) {
    Assert.notNull(rmo, "RedisModulesOperations must not be null!");

    this.rmo = (RedisModulesOperations<String, String>) rmo;
  }
  
  /**
   * Configures the {@link RedisOperations} to be used for the
   * repositories.
   *
   * @param redisOperations must not be {@literal null}.
   */
  @SuppressWarnings("unchecked")
  public void setRedisOperations(RedisOperations<?, ?> redisOperations) {
    Assert.notNull(redisOperations, "RedisOperations must not be null!");
    this.redisOperations = (RedisOperations<String, String>)redisOperations;
  }

  /* (non-Javadoc)
   * 
   * @see
   * org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
   * #setMappingContext(org.springframework.data.mapping.context.
   * MappingContext) */
  @Override
  public void setMappingContext(MappingContext<?, ?> mappingContext) {
    super.setMappingContext(mappingContext);
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
   * @since 1.1
   */
  protected RedisEnhancedRepositoryFactory createRepositoryFactory(KeyValueOperations operations,
      Class<? extends AbstractQueryCreator<?, ?>> queryCreator, Class<? extends RepositoryQuery> repositoryQueryType) {
    return new RedisEnhancedRepositoryFactory(operations, redisOperations, rmo, queryCreator, RedisEnhancedQuery.class);
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
    Assert.notNull(queryCreator, "Query creator type must not be null!");
    Assert.notNull(repositoryQueryType, "RepositoryQueryType type type must not be null!");

    super.afterPropertiesSet();
  }

}
