package com.redis.om.streams.config;

import java.lang.annotation.Annotation;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

public abstract class RedisStreamConsumerBeanDefinitionRegistrarSupport implements ImportBeanDefinitionRegistrar,
    ResourceLoaderAware, EnvironmentAware {
  @NonNull
  private ResourceLoader resourceLoader;
  @NonNull
  private Environment environment;

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
      BeanNameGenerator generator) {
    Assert.notNull(metadata, "AnnotationMetadata must not be null");
    Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
    Assert.notNull(this.resourceLoader, "ResourceLoader must not be null");
    if (metadata.getAnnotationAttributes(this.getAnnotation().getName()) != null) {
      // Delegate to the child class implementation
      registerBeanDefinitions(metadata, registry);
    }
  }

  protected abstract Class<? extends Annotation> getAnnotation();

}
