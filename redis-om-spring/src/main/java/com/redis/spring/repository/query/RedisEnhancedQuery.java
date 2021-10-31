package com.redis.spring.repository.query;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.keyvalue.repository.query.KeyValuePartTreeQuery;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.util.Assert;

import com.redis.spring.annotations.Bloom;
import com.redis.spring.ops.RedisModulesOperations;
import com.redis.spring.ops.pds.BloomOperations;

public class RedisEnhancedQuery extends KeyValuePartTreeQuery {

  public static final String EXISTS_BY_PREFIX = "existsBy";

  public RedisModulesOperations<String, String> rmo;

  @SuppressWarnings("unchecked")
  public RedisEnhancedQuery(QueryMethod queryMethod, RepositoryMetadata metadata,
      QueryMethodEvaluationContextProvider evaluationContextProvider, KeyValueOperations keyValueOperations,
      RedisModulesOperations<?, ?> rmo, Class<? extends AbstractQueryCreator<?, ?>> queryCreator) {

    super(queryMethod, evaluationContextProvider, keyValueOperations, queryCreator);
    this.rmo = (RedisModulesOperations<String, String>) rmo;
    System.out.println(">>>> IN RedisEnhancedQuery#new...");
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public RedisEnhancedQuery(QueryMethod queryMethod, QueryMethodEvaluationContextProvider evaluationContextProvider,
      KeyValueOperations keyValueOperations, RedisModulesOperations<?, ?> rmo,
      QueryCreatorFactory queryCreatorFactory) {
    super(queryMethod, evaluationContextProvider, keyValueOperations, queryCreatorFactory);
    this.rmo = (RedisModulesOperations<String, String>) rmo;
  }

  @Override
  protected Object doExecute(Object[] parameters, KeyValueQuery<?> query) {
    System.out.println(">>>> In doExecute: " + getQueryMethod());
    Optional<String> maybeBloomFilter = getBloomFilter();
    if (maybeBloomFilter.isPresent()) {
      System.out.println(">>>> maybeBloomFilter.isPresent()");
      return executeBloomQuery(parameters, maybeBloomFilter.get());
    } else {
      //TODO can I build query parts here?
      return super.doExecute(parameters, query);
    }
  }

  private Optional<String> getBloomFilter() {
    String methodName = getQueryMethod().getName();
    boolean hasExistByPrefix = methodName.startsWith(EXISTS_BY_PREFIX);

    if (hasExistByPrefix && boolean.class.isAssignableFrom(getQueryMethod().getReturnedObjectType())) {
      String targetProperty = firstToLowercase(methodName.substring(EXISTS_BY_PREFIX.length(), methodName.length()));
      System.out.println(">>>> targetProperty : " + targetProperty);
      Class<?> entityClass = getQueryMethod().getEntityInformation().getJavaType();

      try {
        Field field = entityClass.getDeclaredField(targetProperty);
        if (field.isAnnotationPresent(Bloom.class)) {
          Bloom bloom = field.getAnnotation(Bloom.class);
          return Optional.of(!ObjectUtils.isEmpty(bloom.name()) ? bloom.name()
              : String.format("bf:%s:%s", entityClass.getSimpleName(), field.getName()));
        }
      } catch (NoSuchFieldException e) {
        // NO-OP
      } catch (SecurityException e) {
        // NO-OP
      }
    }
    return Optional.empty();
  }

  public Object executeBloomQuery(Object[] parameters, String bloomFilter) {
    System.out.println(
        String.format(">>>> executeBloomQuery filter:%s, params:%s", bloomFilter, Arrays.toString(parameters)));
    BloomOperations<String> ops = rmo.opsForBloom();
    return ops.exists(bloomFilter, parameters[0].toString());
  }

  /**
   * Configures the {@link RedisModulesOperations} to be used for the
   * repositories.
   *
   * @param operations must not be {@literal null}.
   */
  @SuppressWarnings("unchecked")
  public void setRedisModulesOperations(RedisModulesOperations<?, ?> rmo) {
    System.out.println(">>>> IN RedisEnhancedRepositoryFactoryBean#setRedisModulesOperations...");

    Assert.notNull(rmo, "RedisModulesOperations must not be null!");

    this.rmo = (RedisModulesOperations<String, String>) rmo;
  }

  private String firstToLowercase(String string) {
    char c[] = string.toCharArray();
    c[0] = Character.toLowerCase(c[0]);
    return new String(c);
  }
}
