package com.redis.om.spring.repository.support;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redis.om.spring.RediSearchIndexer;
import com.redis.om.spring.RedisOMSpringProperties;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.repository.query.RediSearchQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery;
import org.springframework.data.keyvalue.repository.query.SpelQueryCreator;
import org.springframework.data.keyvalue.repository.support.KeyValueRepositoryFactory;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;

public class RedisDocumentRepositoryFactory extends KeyValueRepositoryFactory {

  private static final Class<SpelQueryCreator> DEFAULT_QUERY_CREATOR = SpelQueryCreator.class;

  private final KeyValueOperations keyValueOperations;
  private final Class<? extends AbstractQueryCreator<?, ?>> queryCreator;
  private final Class<? extends RepositoryQuery> repositoryQueryType;
  private final RedisModulesOperations<?> rmo;
  private final RediSearchIndexer indexer;
  private final GsonBuilder gsonBuilder;
  private final RedisMappingContext mappingContext;
  private final RedisOMSpringProperties properties;

  /**
   * Creates a new {@link KeyValueRepositoryFactory} for the given
   * {@link KeyValueOperations} and {@link RedisModulesOperations}.
   *
   * @param keyValueOperations must not be {@literal null}.
   * @param rmo                must not be {@literal null}.
   * @param keyspaceToIndexMap must not be {@literal null}.
   * @param mappingContext     must not be {@literal null}.
   * @param gsonBuilder        must not be {@literal null}.
   */
  public RedisDocumentRepositoryFactory( //
      KeyValueOperations keyValueOperations, //
      RedisModulesOperations<?> rmo, //
      RediSearchIndexer keyspaceToIndexMap, //
      RedisMappingContext mappingContext, //
      GsonBuilder gsonBuilder, //
      RedisOMSpringProperties properties //
  ) {
    this(keyValueOperations, rmo, keyspaceToIndexMap, DEFAULT_QUERY_CREATOR, mappingContext, gsonBuilder, properties);
  }

  /**
   * Creates a new {@link KeyValueRepositoryFactory} for the given
   * {@link KeyValueOperations} and {@link AbstractQueryCreator}-type.
   *
   * @param keyValueOperations must not be {@literal null}.
   * @param rmo                must not be {@literal null}.
   * @param keyspaceToIndexMap must not be {@literal null}.
   * @param queryCreator       must not be {@literal null}.
   * @param mappingContext     must not be {@literal null}.
   * @param gsonBuilder        must not be {@literal null}.
   */
  public RedisDocumentRepositoryFactory( //
      KeyValueOperations keyValueOperations, //
      RedisModulesOperations<?> rmo, //
      RediSearchIndexer keyspaceToIndexMap, //
      Class<? extends AbstractQueryCreator<?, ?>> queryCreator, //
      RedisMappingContext mappingContext, //
      GsonBuilder gsonBuilder, //
      RedisOMSpringProperties properties //
  ) {

    this(keyValueOperations, rmo, keyspaceToIndexMap, queryCreator, RediSearchQuery.class, mappingContext, gsonBuilder, properties);
  }

  /**
   * Creates a new {@link KeyValueRepositoryFactory} for the given
   * {@link KeyValueOperations} and {@link AbstractQueryCreator}-type.
   *
   * @param keyValueOperations  must not be {@literal null}.
   * @param rmo                 must not be {@literal null}.
   * @param keyspaceToIndexMap  must not be {@literal null}.
   * @param queryCreator        must not be {@literal null}.
   * @param repositoryQueryType must not be {@literal null}.
   * @param mappingContext      must not be {@literal null}.
   * @param gsonBuilder        must not be {@literal null}.
   */
  public RedisDocumentRepositoryFactory( //
      KeyValueOperations keyValueOperations, //
      RedisModulesOperations<?> rmo, //
      RediSearchIndexer keyspaceToIndexMap, //
      Class<? extends AbstractQueryCreator<?, ?>> queryCreator, //
      Class<? extends RepositoryQuery> repositoryQueryType, //
      RedisMappingContext mappingContext, //
      GsonBuilder gsonBuilder, //
      RedisOMSpringProperties properties //
  ) {

    super(keyValueOperations, queryCreator, repositoryQueryType);

    Assert.notNull(rmo, "RedisModulesOperations must not be null!");

    this.keyValueOperations = keyValueOperations;
    this.rmo = rmo;
    this.indexer = keyspaceToIndexMap;
    this.queryCreator = queryCreator;
    this.repositoryQueryType = repositoryQueryType;
    this.mappingContext = mappingContext;
    this.gsonBuilder = gsonBuilder;
    this.properties = properties;
  }

  @Override
  protected Object getTargetRepository(RepositoryInformation repositoryInformation) {
    EntityInformation<?, ?> entityInformation = getEntityInformation(repositoryInformation.getDomainType());
    return super.getTargetRepositoryViaReflection(
        repositoryInformation, entityInformation, keyValueOperations, rmo, indexer, mappingContext, gsonBuilder, properties);
  }

  @Override
  protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
    return SimpleRedisDocumentRepository.class;
  }

  @Override
  protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable Key key,
      QueryMethodEvaluationContextProvider evaluationContextProvider) {
    return Optional.of(new RediSearchQueryLookupStrategy(evaluationContextProvider, this.keyValueOperations,
        this.rmo, this.properties, this.queryCreator, this.repositoryQueryType, this.gsonBuilder));
  }

  private static class RediSearchQueryLookupStrategy implements QueryLookupStrategy {

    private final QueryMethodEvaluationContextProvider evaluationContextProvider;
    private final KeyValueOperations keyValueOperations;
    private final RedisModulesOperations<?> rmo;
    private final GsonBuilder gsonBuilder;
    private final RedisOMSpringProperties properties;

    private final Class<? extends AbstractQueryCreator<?, ?>> queryCreator;
    private final Class<? extends RepositoryQuery> repositoryQueryType;

    /**
     * @param evaluationContextProvider
     * @param keyValueOperations
     * @param queryCreator
     */
    public RediSearchQueryLookupStrategy(
        QueryMethodEvaluationContextProvider evaluationContextProvider, //
        KeyValueOperations keyValueOperations, //
        RedisModulesOperations<?> rmo, //
        RedisOMSpringProperties properties, //
        Class<? extends AbstractQueryCreator<?, ?>> queryCreator, //
        Class<? extends RepositoryQuery> repositoryQueryType, //
        GsonBuilder gsonBuilder) {

      Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null!");
      Assert.notNull(keyValueOperations, "KeyValueOperations must not be null!");
      Assert.notNull(rmo, "RedisModulesOperations must not be null!");
      Assert.notNull(properties, "RedisOMSpringProperties must not be null!");
      Assert.notNull(queryCreator, "Query creator type must not be null!");
      Assert.notNull(repositoryQueryType, "RepositoryQueryType type must not be null!");

      this.evaluationContextProvider = evaluationContextProvider;
      this.keyValueOperations = keyValueOperations;
      this.rmo = rmo;
      this.properties = properties;
      this.queryCreator = queryCreator;
      this.repositoryQueryType = repositoryQueryType;
      this.gsonBuilder = gsonBuilder;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(
     * java.lang.reflect.Method,
     * org.springframework.data.repository.core.RepositoryMetadata,
     * org.springframework.data.projection.ProjectionFactory,
     * org.springframework.data.repository.core.NamedQueries)
     */
    @Override
    @SuppressWarnings("unchecked")
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
        NamedQueries namedQueries) {

      QueryMethod queryMethod = new QueryMethod(method, metadata, factory);

      Constructor<? extends KeyValuePartTreeQuery> constructor = (Constructor<? extends KeyValuePartTreeQuery>) ClassUtils
          .getConstructorIfAvailable( //
              this.repositoryQueryType, //
              QueryMethod.class, //
              RepositoryMetadata.class, //
              QueryMethodEvaluationContextProvider.class, //
              KeyValueOperations.class, //
              RedisModulesOperations.class, //
              Class.class, //
              GsonBuilder.class, //
              RedisOMSpringProperties.class
          );

      Assert.state(constructor != null, String.format(
          "Constructor %s(QueryMethod, RepositoryMetadata, EvaluationContextProvider, KeyValueOperations, RedisModulesOperations, Class, GsonBuilder, RedisOMSpringProperties) not available!",
          ClassUtils.getShortName(this.repositoryQueryType)));

      return BeanUtils.instantiateClass(constructor, queryMethod, metadata, evaluationContextProvider,
          this.keyValueOperations, this.rmo, this.queryCreator, this.gsonBuilder, this.properties);
    }
  }
}
