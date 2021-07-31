package com.redislabs.spring.repository.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.repository.cdi.CdiBean;
import org.springframework.data.redis.repository.cdi.RedisKeyValueAdapterBean;
import org.springframework.util.Assert;

import com.redislabs.spring.RedisJSONKeyValueAdapter;
import com.redislabs.spring.ops.json.JSONOperations;

public class RedisJSONKeyValueAdapterBean extends CdiBean<RedisJSONKeyValueAdapter> {
  
  private final Bean<RedisOperations<?, ?>> redisOperations;
  private final Bean<JSONOperations<?>> redisJSONOperations;
  

  /**
   * Creates a new {@link RedisKeyValueAdapterBean}.
   *
   * @param redisOperations must not be {@literal null}.
   * @param qualifiers must not be {@literal null}.
   * @param beanManager must not be {@literal null}.
   */
  public RedisJSONKeyValueAdapterBean(Bean<RedisOperations<?, ?>> redisOperations, Bean<JSONOperations<?>> redisJSONOperations, Set<Annotation> qualifiers,
      BeanManager beanManager) {

    super(qualifiers, RedisJSONKeyValueAdapter.class, beanManager);
    Assert.notNull(redisOperations, "RedisOperations Bean must not be null!");
    this.redisOperations = redisOperations;
    Assert.notNull(redisJSONOperations, "RedisJSONOperations Bean must not be null!");
    this.redisJSONOperations = redisJSONOperations;
  }

  @Override
  public RedisJSONKeyValueAdapter create(CreationalContext<RedisJSONKeyValueAdapter> creationalContext) {
    Type beanType = getBeanType();

    return new RedisJSONKeyValueAdapter(getDependencyInstance(this.redisOperations, beanType), getDependencyInstance(this.redisJSONOperations, beanType));
  }

  private Type getBeanType() {

    for (Type type : this.redisOperations.getTypes()) {
      if (type instanceof Class<?> && RedisOperations.class.isAssignableFrom((Class<?>) type)) {
        return type;
      }

      if (type instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (parameterizedType.getRawType() instanceof Class<?>
            && RedisOperations.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
          return type;
        }
      }
    }
    throw new IllegalStateException("Cannot resolve bean type for class " + RedisOperations.class.getName());
  }

}
