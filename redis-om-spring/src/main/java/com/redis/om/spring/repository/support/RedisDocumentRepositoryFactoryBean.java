package com.redis.om.spring.repository.support;

import com.redis.om.spring.ops.RedisModulesOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactoryBean;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class RedisDocumentRepositoryFactoryBean<T extends Repository<S, ID>, S, ID>
    extends KeyValueRepositoryFactoryBean<T, S, ID> {

  @Autowired
  private @Nullable RedisModulesOperations<String> rmo;

  /**
   * Creates a new {@link RedisDocumentRepositoryFactoryBean} for the given repository
   * interface.
   *
   * @param repositoryInterface must not be {@literal null}.
   */
  public RedisDocumentRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
    super(repositoryInterface);
  }

  /**
   * Configures the {@link RedisModulesOperations} to be used for the repositories.
   *
   * @param rmo must not be {@literal null}.
   */
  public void setRedisModulesOperations(RedisModulesOperations<String> rmo) {
    Assert.notNull(rmo, "RedisModulesOperations must not be null!");

    this.rmo = rmo;
  }

  @Override
  public void setMappingContext(MappingContext<?, ?> mappingContext) {
    super.setMappingContext(mappingContext);
  }

  @Override
  protected final RedisDocumentRepositoryFactory createRepositoryFactory(KeyValueOperations operations,
      Class<? extends AbstractQueryCreator<?, ?>> queryCreator, Class<? extends RepositoryQuery> repositoryQueryType) {

    return new RedisDocumentRepositoryFactory(operations, rmo, queryCreator, repositoryQueryType);
  }

  @Override
  public void afterPropertiesSet() {
    Assert.notNull(rmo, "RedisModulesOperations must not be null!");

    super.afterPropertiesSet();
  }
}