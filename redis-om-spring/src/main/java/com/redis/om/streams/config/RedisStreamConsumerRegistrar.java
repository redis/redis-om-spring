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

public class RedisStreamConsumerRegistrar extends RedisStreamConsumerBeanDefinitionRegistrarSupport {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public RedisStreamConsumerRegistrar() {
  }

  @PostConstruct
  private void init() {
    logger.info("RedisStreamConsumerRegistrar init");
  }

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

  private void registerSerialTopicConfig(String topicName, BeanDefinitionRegistry registry) {
    String beanName = topicName + "SerialTopicConfig";
    if (!registry.containsBeanDefinition(beanName)) {
      BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SerialTopicConfig.class);
      builder.addConstructorArgValue(topicName);
      registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
      logger.info("Registered SerialTopicConfig bean: {}", beanName);
    }
  }

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

  private void registerConsumerClass(Class<?> consumerClass, BeanDefinitionRegistry registry) {
    String beanName = StringUtils.uncapitalize(consumerClass.getSimpleName());
    if (!registry.containsBeanDefinition(beanName)) {
      BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(consumerClass);
      registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
      logger.info("Registered consumer class bean: {}", beanName);
    }
  }

  @Override
  protected Class<? extends Annotation> getAnnotation() {
    return EnableRedisStreams.class;
  }

}