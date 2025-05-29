package com.redis.om.spring.repository.configuration;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.redis.repository.configuration.RedisRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

import com.redis.om.spring.CustomRedisKeyValueTemplate;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.repository.RedisDocumentRepository;

/**
 * Repository configuration extension for Redis JSON document repositories that provides
 * specialized configuration and setup for JSON-based repository infrastructure.
 *
 * <p>This extension extends Spring Data Redis's {@link RedisRepositoryConfigurationExtension}
 * to provide JSON document-specific repository configuration. It configures the necessary
 * infrastructure for Redis JSON document repositories, including custom adapters, templates,
 * and mapping contexts optimized for document storage and retrieval operations.
 *
 * <p>Key configuration responsibilities include:
 * <ul>
 * <li>Setting up RedisJSON-specific key-value adapters for document operations</li>
 * <li>Configuring custom Redis templates for JSON document handling</li>
 * <li>Establishing mapping contexts for document serialization/deserialization</li>
 * <li>Defining module-specific prefixes and naming conventions</li>
 * <li>Identifying document repository types and annotations</li>
 * </ul>
 *
 * <p>This extension specifically configures repositories that work with entities annotated
 * with {@link com.redis.om.spring.annotations.Document @Document}, enabling:
 * <ul>
 * <li>Native JSON document storage using RedisJSON module</li>
 * <li>Complex nested document structures and JSONPath operations</li>
 * <li>Schema-free document modeling and storage</li>
 * <li>Full-text search and indexing capabilities via RediSearch</li>
 * <li>Vector similarity search for AI/ML applications</li>
 * </ul>
 *
 * <p>Unlike Enhanced repositories that work with Redis hash structures,
 * JSON repositories provide richer document modeling capabilities with:
 * <ul>
 * <li>Atomic operations on document sub-structures</li>
 * <li>JSONPath-based queries and updates</li>
 * <li>Support for arrays, nested objects, and complex data types</li>
 * <li>Optimized storage for document-oriented use cases</li>
 * </ul>
 *
 * <p>The extension is automatically activated when using
 * {@link com.redis.om.spring.annotations.EnableRedisDocumentRepositories @EnableRedisDocumentRepositories}
 * and works in conjunction with {@link RedisJSONRepositoriesRegistrar} to provide complete
 * JSON document repository support.
 *
 * @see RedisRepositoryConfigurationExtension
 * @see RedisJSONRepositoriesRegistrar
 * @see com.redis.om.spring.annotations.EnableRedisDocumentRepositories
 * @see com.redis.om.spring.annotations.Document
 * @see RedisDocumentRepository
 * @see com.redis.om.spring.CustomRedisKeyValueTemplate
 *
 * @author Redis OM Spring Team
 * @since 1.0.0
 */
public class RedisJSONRepositoryConfigurationExtension extends RedisRepositoryConfigurationExtension {

  /**
   * Default constructor for the Redis JSON repository configuration extension.
   *
   * <p>This constructor is called by the Spring framework during repository configuration
   * to initialize the JSON document-specific repository infrastructure. It sets up the
   * necessary configuration for Redis JSON document repositories, including adapters,
   * templates, and mapping contexts.
   *
   * <p>The constructor delegates to the parent class to establish the basic Redis repository
   * infrastructure and then applies JSON document-specific configuration for:
   * <ul>
   * <li>RedisJSON module integration</li>
   * <li>Document serialization and deserialization</li>
   * <li>JSON-specific key-value operations</li>
   * <li>Document repository identification and setup</li>
   * </ul>
   */
  public RedisJSONRepositoryConfigurationExtension() {
    // Default constructor - Spring framework will call this during configuration
  }

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

    return BeanDefinitionBuilder.rootBeanDefinition(CustomRedisKeyValueTemplate.class) //
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
