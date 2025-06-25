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

/**
 * Abstract support class for Redis Stream consumer bean definition registrars.
 * <p>
 * This class provides the basic infrastructure for registering Redis Stream consumer beans
 * based on annotations. It implements the Spring {@link ImportBeanDefinitionRegistrar} interface
 * to allow for programmatic registration of bean definitions, as well as {@link ResourceLoaderAware}
 * and {@link EnvironmentAware} to provide access to the Spring environment and resource loader.
 * <p>
 * Subclasses must implement the {@link #getAnnotation()} method to specify which annotation
 * triggers the registration process, and the
 * {@link #registerBeanDefinitions(AnnotationMetadata, BeanDefinitionRegistry)}
 * method to define the actual registration logic.
 * 
 * @see ImportBeanDefinitionRegistrar
 * @see ResourceLoaderAware
 * @see EnvironmentAware
 */
public abstract class RedisStreamConsumerBeanDefinitionRegistrarSupport implements ImportBeanDefinitionRegistrar,
    ResourceLoaderAware, EnvironmentAware {
  /**
   * The Spring ResourceLoader, injected by Spring.
   */
  @NonNull
  private ResourceLoader resourceLoader;

  /**
   * The Spring Environment, injected by Spring.
   */
  @NonNull
  private Environment environment;

  /**
   * Sets the ResourceLoader.
   * This method is called by Spring to inject the ResourceLoader.
   *
   * @param resourceLoader the Spring ResourceLoader
   */
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  /**
   * Sets the Environment.
   * This method is called by Spring to inject the Environment.
   *
   * @param environment the Spring Environment
   */
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  /**
   * Registers bean definitions based on the given metadata.
   * This method is called by Spring when processing the import annotation.
   * It validates the input parameters and delegates to the child class implementation
   * if the annotation specified by {@link #getAnnotation()} is present.
   *
   * @param metadata  the annotation metadata of the importing class
   * @param registry  the bean definition registry
   * @param generator the bean name generator
   */
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

  /**
   * Returns the annotation class that triggers the registration process.
   * Subclasses must implement this method to specify which annotation
   * should be detected to activate the bean registration.
   *
   * @return the annotation class
   */
  protected abstract Class<? extends Annotation> getAnnotation();

}
