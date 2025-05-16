package com.redis.om.spring.repository.configuration;

import java.lang.annotation.Annotation;

import org.springframework.data.redis.repository.configuration.RedisRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;

public class RedisEnhancedRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {
  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getAnnotation()
   */
  @Override
  protected Class<? extends Annotation> getAnnotation() {
    return EnableRedisEnhancedRepositories.class;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getExtension()
   */
  @Override
  protected RepositoryConfigurationExtension getExtension() {
    return new RedisRepositoryConfigurationExtension();
  }
}
