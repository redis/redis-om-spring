package com.redis.om.spring.repository.configuration;

import java.lang.annotation.Annotation;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;

/**
 * Repository bean definition registrar for Redis JSON document repositories that enables automatic
 * registration and configuration of Redis OM Spring JSON-based repository interfaces.
 *
 * <p>This registrar is triggered by the {@link EnableRedisDocumentRepositories} annotation
 * and handles the Spring application context setup for Redis JSON document repositories. It extends
 * Spring Data's {@link RepositoryBeanDefinitionRegistrarSupport} to provide specialized
 * configuration for Redis JSON document storage with full-text search and indexing capabilities.
 *
 * <p>Key responsibilities include:
 * <ul>
 * <li>Scanning for repository interfaces annotated with {@code @EnableRedisDocumentRepositories}</li>
 * <li>Registering repository bean definitions with appropriate factory beans</li>
 * <li>Configuring the Redis JSON repository infrastructure for document operations</li>
 * <li>Setting up RediSearch indexing and RedisJSON module integration</li>
 * </ul>
 *
 * <p>The registrar works in conjunction with:
 * <ul>
 * <li>{@link RedisJSONRepositoryConfigurationExtension} for JSON-specific repository configuration</li>
 * <li>{@link EnableRedisDocumentRepositories} annotation for activation</li>
 * <li>Redis JSON document repository infrastructure beans</li>
 * <li>RedisJSON module for document storage and retrieval</li>
 * </ul>
 *
 * <p>Unlike Redis Enhanced repositories that work with hash structures, JSON repositories
 * provide native JSON document storage using the RedisJSON module, enabling:
 * <ul>
 * <li>Complex nested document structures</li>
 * <li>JSONPath-based queries and updates</li>
 * <li>Schema-free document storage</li>
 * <li>Atomic operations on document sub-structures</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * @Configuration
 * 
 * @EnableRedisDocumentRepositories(basePackages = "com.example.repositories")
 *                                               public class RedisConfig {
 *                                               // Redis configuration
 *                                               }
 *                                               }</pre>
 *
 * @see EnableRedisDocumentRepositories
 * @see RepositoryBeanDefinitionRegistrarSupport
 * @see RedisJSONRepositoryConfigurationExtension
 * @see com.redis.om.spring.repository.RedisDocumentRepository
 * @see com.redis.om.spring.annotations.Document
 *
 * @author Redis OM Spring Team
 * @since 1.0.0
 */
public class RedisJSONRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

  /**
   * Default constructor for the Redis JSON document repositories registrar.
   *
   * <p>This constructor is called by the Spring framework during application context
   * initialization when the {@link EnableRedisDocumentRepositories} annotation is detected.
   * It initializes the registrar with default settings for Redis JSON document repository
   * bean registration and configuration.
   *
   * <p>The constructor delegates to the parent class to set up the basic repository
   * infrastructure and then applies JSON document-specific configuration through
   * the associated {@link RedisJSONRepositoryConfigurationExtension}.
   *
   * <p>This registrar specifically handles repositories that work with Redis JSON documents,
   * configuring the necessary beans for RedisJSON module integration and document-based
   * storage operations.
   */
  public RedisJSONRepositoriesRegistrar() {
    // Default constructor - Spring framework will call this during initialization
  }

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
