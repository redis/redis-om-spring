package com.redislabs.spring.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import com.redislabs.spring.repository.impl.RedisDocumentRepositoryImpl;
import com.redislabs.spring.repository.support.RedisDocumentRepositoryFactoryBean;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableRedisRepositories
public @interface EnableRedisDocumentRepositories {
  /**
   * Returns the {@link FactoryBean} class to be used for each repository instance. Defaults to
   * {@link MongoRepositoryFactoryBean}.
   *
   * @return {@link MongoRepositoryFactoryBean} by default.
   */
  Class<?> repositoryFactoryBeanClass() default RedisDocumentRepositoryFactoryBean.class;

  /**
   * Configure the repository base class to be used to create repository proxies for this particular configuration.
   *
   * @return {@link DefaultRepositoryBaseClass} by default.
   * @since 1.8
   */
  Class<?> repositoryBaseClass() default RedisDocumentRepositoryImpl.class;
}
