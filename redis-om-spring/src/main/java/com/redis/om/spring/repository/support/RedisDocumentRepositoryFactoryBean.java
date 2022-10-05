package com.redis.om.spring.repository.support;

import com.google.gson.Gson;
import com.redis.om.spring.KeyspaceToIndexMap;
import com.redis.om.spring.ops.RedisModulesOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactoryBean;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class RedisDocumentRepositoryFactoryBean<T extends Repository<S, ID>, S, ID>
    extends KeyValueRepositoryFactoryBean<T, S, ID> {

  @Autowired
  private @Nullable RedisModulesOperations<String> rmo;
  @Autowired
  private @Nullable KeyspaceToIndexMap keyspaceToIndexMap;
  @Autowired
  private @Nullable RedisMappingContext mappingContext;
  @Autowired
  private Gson gson;

  /**
   * Creates a new {@link RedisDocumentRepositoryFactoryBean} for the given repository
   * interface.
   *
   * @param repositoryInterface must not be {@literal null}.
   */
  public RedisDocumentRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
    super(repositoryInterface);
  }

  @Override
  protected final RedisDocumentRepositoryFactory createRepositoryFactory(
          KeyValueOperations operations,
          Class<? extends AbstractQueryCreator<?, ?>> queryCreator, Class<? extends RepositoryQuery> repositoryQueryType
  ) {
    return new RedisDocumentRepositoryFactory(operations, rmo, keyspaceToIndexMap, queryCreator, repositoryQueryType, this.mappingContext, this.gson);
  }

  @Override
  public void afterPropertiesSet() {
    Assert.notNull(rmo, "RedisModulesOperations must not be null!");

    super.afterPropertiesSet();
  }
}