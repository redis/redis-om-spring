package com.redis.om.spring.repository.configuration;

import java.lang.annotation.Annotation;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;

public class RedisJSONRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

  /* (non-Javadoc)
   * 
   * @see org.springframework.data.repository.config.
   * RepositoryBeanDefinitionRegistrarSupport#getAnnotation() */
  @Override
  protected Class<? extends Annotation> getAnnotation() {
    return EnableRedisDocumentRepositories.class;
  }

  /* (non-Javadoc)
   * 
   * @see org.springframework.data.repository.config.
   * RepositoryBeanDefinitionRegistrarSupport#getExtension() */
  @Override
  protected RepositoryConfigurationExtension getExtension() {
    return new RedisJSONRepositoryConfigurationExtension();
  }

}
