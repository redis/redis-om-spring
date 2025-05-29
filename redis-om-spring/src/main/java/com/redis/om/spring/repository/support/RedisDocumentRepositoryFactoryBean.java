package com.redis.om.spring.repository.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactoryBean;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.MappingConfiguration;
import org.springframework.data.redis.core.index.IndexConfiguration;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.mapping.RedisEnhancedMappingContext;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.vectorize.Embedder;

/**
 * Factory bean for creating Redis document repository factory instances.
 * <p>
 * This factory bean integrates with Spring's IoC container to provide repository instances
 * for Redis JSON document storage. It extends Spring Data's KeyValueRepositoryFactoryBean
 * to add Redis OM Spring specific functionality including search indexing, vector embeddings,
 * and custom query execution.
 * </p>
 * <p>
 * The factory bean automatically configures:
 * <ul>
 * <li>Redis operations for JSON document storage</li>
 * <li>Search indexing through RediSearchIndexer</li>
 * <li>AI embedding capabilities</li>
 * <li>Mapping context for entity relationships</li>
 * <li>Query execution infrastructure</li>
 * </ul>
 *
 * @param <T>  the repository type
 * @param <S>  the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 * @see org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactoryBean
 * @see RedisDocumentRepositoryFactory
 * @since 0.1.0
 */
public class RedisDocumentRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends
    KeyValueRepositoryFactoryBean<T, S, ID> {

  @Autowired
  private @Nullable RedisModulesOperations<String> rmo;
  @Autowired
  private @Nullable RediSearchIndexer indexer;
  @Autowired
  private @Nullable RedisMappingContext mappingContext;
  @Autowired
  private GsonBuilder gsonBuilder;
  @Autowired
  private @Nullable Embedder embedder;
  @Autowired
  private RedisOMProperties properties;

  /**
   * Creates a new {@link RedisDocumentRepositoryFactoryBean} for the given
   * repository
   * interface.
   *
   * @param repositoryInterface must not be {@literal null}.
   */
  public RedisDocumentRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
    super(repositoryInterface);
  }

  @Override
  protected final RedisDocumentRepositoryFactory createRepositoryFactory( //
      KeyValueOperations operations, //
      Class<? extends AbstractQueryCreator<?, ?>> queryCreator, //
      Class<? extends RepositoryQuery> repositoryQueryType //
  ) {
    return new RedisDocumentRepositoryFactory(operations, rmo, indexer, queryCreator, repositoryQueryType,
        this.mappingContext, this.gsonBuilder, this.embedder, this.properties);
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

  @Override
  public void afterPropertiesSet() {
    Assert.notNull(rmo, "RedisModulesOperations must not be null!");

    super.afterPropertiesSet();
  }
}