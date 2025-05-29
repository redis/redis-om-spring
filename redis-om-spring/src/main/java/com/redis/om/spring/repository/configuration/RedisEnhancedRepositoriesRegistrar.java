package com.redis.om.spring.repository.configuration;

import java.lang.annotation.Annotation;

import org.springframework.data.redis.repository.configuration.RedisRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;

/**
 * Repository bean definition registrar for Redis Enhanced repositories that enables automatic
 * registration and configuration of Redis OM Spring repository interfaces.
 *
 * <p>This registrar is triggered by the {@link EnableRedisEnhancedRepositories} annotation
 * and handles the Spring application context setup for Redis Enhanced repositories. It extends
 * Spring Data's {@link RepositoryBeanDefinitionRegistrarSupport} to provide specialized
 * configuration for Redis hash-based repositories with enhanced search capabilities.
 *
 * <p>Key responsibilities include:
 * <ul>
 * <li>Scanning for repository interfaces annotated with {@code @EnableRedisEnhancedRepositories}</li>
 * <li>Registering repository bean definitions with appropriate factory beans</li>
 * <li>Configuring the Redis repository infrastructure for enhanced operations</li>
 * <li>Setting up RediSearch indexing and query execution capabilities</li>
 * </ul>
 *
 * <p>The registrar works in conjunction with:
 * <ul>
 * <li>{@link RedisRepositoryConfigurationExtension} for repository configuration</li>
 * <li>{@link EnableRedisEnhancedRepositories} annotation for activation</li>
 * <li>Redis Enhanced repository infrastructure beans</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * @Configuration
 * 
 * @EnableRedisEnhancedRepositories(basePackages = "com.example.repositories")
 *                                               public class RedisConfig {
 *                                               // Redis configuration
 *                                               }
 *                                               }</pre>
 *
 * @see EnableRedisEnhancedRepositories
 * @see RepositoryBeanDefinitionRegistrarSupport
 * @see RedisRepositoryConfigurationExtension
 * @see com.redis.om.spring.repository.RedisEnhancedRepository
 *
 * @author Redis OM Spring Team
 * @since 1.0.0
 */
public class RedisEnhancedRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

  /**
   * Default constructor for the Redis Enhanced repositories registrar.
   *
   * <p>This constructor is called by the Spring framework during application context
   * initialization when the {@link EnableRedisEnhancedRepositories} annotation is detected.
   * It initializes the registrar with default settings for Redis Enhanced repository
   * bean registration and configuration.
   *
   * <p>The constructor delegates to the parent class to set up the basic repository
   * infrastructure and then applies Redis Enhanced-specific configuration through
   * the associated {@link RedisRepositoryConfigurationExtension}.
   */
  public RedisEnhancedRepositoriesRegistrar() {
    // Default constructor - Spring framework will call this during initialization
  }

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
