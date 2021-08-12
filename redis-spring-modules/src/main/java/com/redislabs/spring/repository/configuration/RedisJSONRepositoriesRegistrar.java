package com.redislabs.spring.repository.configuration;

import java.lang.annotation.Annotation;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationDelegate;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import com.redislabs.spring.annotations.EnableRedisDocumentRepositories;


public class RedisJSONRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {
  
  private @NonNull ResourceLoader myResourceLoader;
  private @NonNull Environment myEnvironment;
  
  /*
   * (non-Javadoc)
   * @see org.springframework.context.ResourceLoaderAware#setResourceLoader(org.springframework.core.io.ResourceLoader)
   */
  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.myResourceLoader = resourceLoader;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.context.EnvironmentAware#setEnvironment(org.springframework.core.env.Environment)
   */
  @Override
  public void setEnvironment(Environment environment) {
    this.myEnvironment = environment;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getAnnotation()
   */
  @Override
  protected Class<? extends Annotation> getAnnotation() {
    return EnableRedisDocumentRepositories.class;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getExtension()
   */
  @Override
  protected RepositoryConfigurationExtension getExtension() {
    return new RedisJSONRepositoryConfigurationExtension();
  }
  
  /*
   * (non-Javadoc)
   * @see org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry, org.springframework.beans.factory.support.BeanNameGenerator)
   */
  @Override
  public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
      BeanNameGenerator generator) {

    Assert.notNull(metadata, "AnnotationMetadata must not be null!");
    Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");
    Assert.notNull(myResourceLoader, "ResourceLoader must not be null!");

    AnnotationRepositoryConfigurationSource configurationSource = new AnnotationRepositoryConfigurationSource(metadata,
        getAnnotation(), myResourceLoader, myEnvironment, registry, generator);

    RepositoryConfigurationExtension extension = getExtension();
    RepositoryConfigurationUtils.exposeRegistration(extension, registry, configurationSource);

    RepositoryConfigurationDelegate delegate = new RepositoryConfigurationDelegate(configurationSource, myResourceLoader,
        myEnvironment);

    delegate.registerRepositoriesIn(registry, extension);
  }

}
