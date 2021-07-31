package com.redislabs.spring.repository.configuration;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.repository.configuration.RedisRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

import com.redislabs.spring.annotations.Document;
import com.redislabs.spring.repository.RedisDocumentRepository;

public class RedisJSONRepositoryConfigurationExtension extends RedisRepositoryConfigurationExtension {
  private static final String REDIS_ADAPTER_BEAN_NAME = "redisJSONKeyValueAdapter";
  
  /*
   * (non-Javadoc)
   * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension#getModuleName()
   */
  @Override
  public String getModuleName() {
    return "RedisJSON";
  }
  
  /*
   * (non-Javadoc)
   * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension#getModulePrefix()
   */
  @Override
  protected String getModulePrefix() {
    return "rejson";
  }
  
  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getIdentifyingTypes()
   */
  @Override
  protected Collection<Class<?>> getIdentifyingTypes() {
    return Collections.singleton(RedisDocumentRepository.class);
  }
  
  /*
   * (non-Javadoc)
   * @see org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension#getDefaultKeyValueTemplateBeanDefinition(org.springframework.data.repository.config.RepositoryConfigurationSource)
   */
  @Override
  protected AbstractBeanDefinition getDefaultKeyValueTemplateBeanDefinition(
      RepositoryConfigurationSource configurationSource) {

    return BeanDefinitionBuilder.rootBeanDefinition(RedisKeyValueTemplate.class) //
        .addConstructorArgReference(REDIS_ADAPTER_BEAN_NAME) //
        .addConstructorArgReference(MAPPING_CONTEXT_BEAN_NAME) //
        .getBeanDefinition();
  }
  
  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getIdentifyingAnnotations()
   */
  @Override
  protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
    return Collections.singleton(Document.class);
  }
}
