package com.redis.om.streams.config;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import com.redis.om.streams.annotation.EnableRedisStreams;
import com.redis.om.streams.annotation.RedisStreamConsumer;
import com.redis.om.streams.command.noack.NoAckConsumerGroup;
import com.redis.om.streams.command.serial.ConsumerGroup;
import com.redis.om.streams.command.serial.SerialTopicConfig;
import com.redis.om.streams.command.serial.TopicManager;
import com.redis.om.streams.command.singleclusterpel.SingleClusterPelConsumerGroup;

import jakarta.annotation.PostConstruct;

/**
 * Registrar for Redis Stream consumers that scans for classes annotated with {@link RedisStreamConsumer}
 * and registers them as Spring beans. This class is responsible for setting up the necessary infrastructure
 * for Redis Streams integration, including topic configurations, consumer groups, and the actual consumer beans.
 * <p>
 * This registrar is activated by the {@link EnableRedisStreams} annotation and will scan the specified base
 * packages for consumer classes.
 * 
 * @see EnableRedisStreams
 * @see RedisStreamConsumer
 * @see RedisStreamConsumerBeanDefinitionRegistrarSupport
 */
public class RedisStreamConsumerRegistrar extends RedisStreamConsumerBeanDefinitionRegistrarSupport {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Default constructor for the RedisStreamConsumerRegistrar.
   */
  public RedisStreamConsumerRegistrar() {
  }

  /**
   * Initialization method called after the bean has been constructed.
   * Logs an informational message indicating that the registrar has been initialized.
   */
  @PostConstruct
  private void init() {
    logger.info("RedisStreamConsumerRegistrar init");
  }

  /**
   * Registers bean definitions for Redis Stream consumers.
   * This method scans for classes annotated with {@link RedisStreamConsumer} in the specified base packages
   * and registers them as Spring beans along with the necessary infrastructure components.
   *
   * @param importingClassMetadata metadata about the importing class, which contains the {@link EnableRedisStreams}
   *                               annotation
   * @param registry               the bean definition registry to register beans with
   */
  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableRedisStreams.class.getName());
    String[] basePackages = (String[]) attributes.get("basePackages");

    if (basePackages == null || basePackages.length == 0) {
      logger.warn("No base packages specified for @EnableRedisStreams, using default package");
      basePackages = new String[] { "com.redis.om.streams" };
    }

    ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(RedisStreamConsumer.class));

    for (String basePackage : basePackages) {
      if (StringUtils.hasText(basePackage)) {
        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
        for (BeanDefinition beanDefinition : candidateComponents) {
          try {
            Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
            RedisStreamConsumer annotation = clazz.getAnnotation(RedisStreamConsumer.class);
            if (annotation != null) {
              registerConsumerBeans(clazz, annotation, registry);
            }
          } catch (ClassNotFoundException e) {
            logger.error("Could not load class: {}", beanDefinition.getBeanClassName(), e);
          }
        }
      }
    }
  }

  /**
   * Registers all the necessary beans for a Redis Stream consumer.
   * This includes the topic configuration, topic manager, consumer group, and the consumer class itself.
   *
   * @param consumerClass the class annotated with {@link RedisStreamConsumer}
   * @param annotation    the {@link RedisStreamConsumer} annotation instance
   * @param registry      the bean definition registry to register beans with
   */
  private void registerConsumerBeans(Class<?> consumerClass, RedisStreamConsumer annotation,
      BeanDefinitionRegistry registry) {
    String topicName = annotation.topicName();
    String groupName = annotation.groupName();
    String consumerName = annotation.consumerName();
    boolean autoAck = annotation.autoAck();
    boolean cluster = annotation.cluster();

    logger.info("Registering beans for consumer: {} with topic: {}, group: {}, autoAck: {}, cluster: {}", consumerClass
        .getSimpleName(), topicName, groupName, autoAck, cluster);

    // Register SerialTopicConfig
    registerSerialTopicConfig(topicName, registry);

    // Register TopicManager
    registerTopicManager(topicName, registry);

    // Register ConsumerGroup based on configuration
    if (cluster) {
      registerSingleClusterPelConsumerGroup(topicName, groupName, consumerClass, registry);
    } else if (autoAck) {
      registerConsumerGroup(topicName, groupName, consumerClass, registry);
    } else {
      registerNoAckConsumerGroup(topicName, groupName, consumerClass, registry);
    }

    // Register the consumer class itself as a bean
    registerConsumerClass(consumerClass, registry);
  }

  /**
   * Registers a SerialTopicConfig bean for the specified topic.
   * This bean contains the configuration for a Redis Stream topic.
   *
   * @param topicName the name of the Redis Stream topic
   * @param registry  the bean definition registry to register the bean with
   */
  private void registerSerialTopicConfig(String topicName, BeanDefinitionRegistry registry) {
    String beanName = topicName + "SerialTopicConfig";
    if (!registry.containsBeanDefinition(beanName)) {
      BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SerialTopicConfig.class);
      builder.addConstructorArgValue(topicName);
      registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
      logger.info("Registered SerialTopicConfig bean: {}", beanName);
    }
  }

  /**
   * Registers a TopicManager bean for the specified topic.
   * The TopicManager is responsible for creating and managing the Redis Stream topic.
   *
   * @param topicName the name of the Redis Stream topic
   * @param registry  the bean definition registry to register the bean with
   */
  private void registerTopicManager(String topicName, BeanDefinitionRegistry registry) {
    String beanName = topicName + "TopicManager";
    if (!registry.containsBeanDefinition(beanName)) {
      BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TopicManager.class);
      builder.setFactoryMethod("createTopic");
      builder.addConstructorArgReference("jedisPooled");
      builder.addConstructorArgReference(topicName + "SerialTopicConfig");
      registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
      logger.info("Registered TopicManager bean: {}", beanName);
    } else {
      logger.info("TopicManager bean already exists for topic: {}", topicName);
    }
  }

  /**
   * Registers a ConsumerGroup bean for the specified topic and group.
   * This consumer group automatically acknowledges messages after processing.
   *
   * @param topicName     the name of the Redis Stream topic
   * @param groupName     the name of the consumer group
   * @param consumerClass the class that will consume messages from this group
   * @param registry      the bean definition registry to register the bean with
   */
  private void registerConsumerGroup(String topicName, String groupName, Class<?> consumerClass,
      BeanDefinitionRegistry registry) {
    String beanName = groupName + "ConsumerGroup";
    if (!registry.containsBeanDefinition(beanName)) {
      BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ConsumerGroup.class);
      builder.addConstructorArgReference("jedisPooled");
      builder.addConstructorArgValue(topicName);
      builder.addConstructorArgValue(groupName);
      registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
      logger.info("Registered ConsumerGroup bean: {}", beanName);
    } else {
      logger.info("ConsumerGroup bean already exists for group: {}", groupName);
    }
  }

  /**
   * Registers a NoAckConsumerGroup bean for the specified topic and group.
   * This consumer group does not automatically acknowledge messages after processing,
   * requiring explicit acknowledgment by the consumer.
   *
   * @param topicName     the name of the Redis Stream topic
   * @param groupName     the name of the consumer group
   * @param consumerClass the class that will consume messages from this group
   * @param registry      the bean definition registry to register the bean with
   */
  private void registerNoAckConsumerGroup(String topicName, String groupName, Class<?> consumerClass,
      BeanDefinitionRegistry registry) {
    String beanName = groupName + "NoAckConsumerGroup";
    if (!registry.containsBeanDefinition(beanName)) {
      BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(NoAckConsumerGroup.class);
      builder.addConstructorArgReference("jedisPooled");
      builder.addConstructorArgValue(topicName);
      builder.addConstructorArgValue(groupName);
      registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
      logger.info("Registered NoAckConsumerGroup bean: {}", beanName);
    } else {
      logger.info("NoAckConsumerGroup bean already exists for group: {}", groupName);
    }
  }

  /**
   * Registers a SingleClusterPelConsumerGroup bean for the specified topic and group.
   * This consumer group is designed for use in a clustered environment, where multiple
   * instances of the application can consume messages from the same stream.
   *
   * @param topicName     the name of the Redis Stream topic
   * @param groupName     the name of the consumer group
   * @param consumerClass the class that will consume messages from this group
   * @param registry      the bean definition registry to register the bean with
   */
  private void registerSingleClusterPelConsumerGroup(String topicName, String groupName, Class<?> consumerClass,
      BeanDefinitionRegistry registry) {
    String beanName = groupName + "SingleClusterPelConsumerGroup";
    if (!registry.containsBeanDefinition(beanName)) {
      BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SingleClusterPelConsumerGroup.class);
      builder.addConstructorArgReference("jedisPooled");
      builder.addConstructorArgValue(topicName);
      builder.addConstructorArgValue(groupName);
      registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
      logger.info("Registered SingleClusterPelConsumerGroup bean: {}", beanName);
    } else {
      logger.info("SingleClusterPelConsumerGroup bean already exists for group: {}", groupName);
    }
  }

  /**
   * Registers the consumer class itself as a Spring bean.
   * This allows the consumer to be autowired into other components.
   *
   * @param consumerClass the class to register as a bean
   * @param registry      the bean definition registry to register the bean with
   */
  private void registerConsumerClass(Class<?> consumerClass, BeanDefinitionRegistry registry) {
    String beanName = StringUtils.uncapitalize(consumerClass.getSimpleName());
    if (!registry.containsBeanDefinition(beanName)) {
      BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(consumerClass);
      registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
      logger.info("Registered consumer class bean: {}", beanName);
    }
  }

  /**
   * Returns the annotation class that this registrar is looking for.
   * In this case, it's the {@link EnableRedisStreams} annotation.
   *
   * @return the annotation class
   */
  @Override
  protected Class<? extends Annotation> getAnnotation() {
    return EnableRedisStreams.class;
  }

}
