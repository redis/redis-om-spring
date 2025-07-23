# Redis Streams Consumer Framework

This framework provides automatic bean creation for Redis Stream consumers using Spring annotations.

## Overview

The framework consists of two main annotations:
- `@EnableRedisStreams`: Enables the automatic scanning and bean creation for Redis Stream consumers
- `@RedisStreamConsumer`: Marks a class as a Redis Stream consumer with specific configuration

## Quick Start

### 1. Enable Redis Streams

Add the `@EnableRedisStreams` annotation to your configuration class:

```java
@Configuration
@EnableRedisStreams(basePackages = "com.redis.om.streams.consumer")
public class RedisStreamsConfiguration {
    // Your configuration
}
```

### 2. Create a Consumer

Create a class that extends `RedisStreamsConsumer` and annotate it with `@RedisStreamConsumer`:

```java
@RedisStreamConsumer(
    topicName = "myTopic", 
    groupName = "myGroup", 
    consumerName = "myConsumer",
    autoAck = false,
    cluster = false
)
public class MyRedisStreamsConsumer extends RedisStreamsConsumer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(fixedDelayString = "${redis.streams.fixed-delay:1000}")
    public boolean process() {
        TopicEntry topicEntry = consume();
        if (topicEntry != null) {
            logger.info("{} processing topic: {}", getClass().getSimpleName(), topicEntry);
            return true;
        }
        return false;
    }
}
```

## Annotation Configuration

### @EnableRedisStreams

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `basePackages` | String[] | `{"com.redis.om.streams"}` | Base packages to scan for `@RedisStreamConsumer` annotated classes |
| `value` | String[] | `{}` | Alias for `basePackages` |

### @RedisStreamConsumer

| Attribute | Type | Default | Description                                                    |
|-----------|------|---------|----------------------------------------------------------------|
| `topicName` | String | **Required** | Name of the Redis Stream topic                                 |
| `groupName` | String | **Required** | Name of the consumer group                                     |
| `consumerName` | String | `""` | Name of the consumer within the group                          |
| `autoAck` | boolean | `false` | Whether the consumer can acknowledge messages |
| `cluster` | boolean | `false` | Whether to use cluster mode                                    |

## Consumer Types

The framework automatically creates different types of consumer groups based on the annotation configuration:

### 1. No Acknowledgment Consumer (default)
```java
@RedisStreamConsumer(
    topicName = "topic", 
    groupName = "group",
    autoAck = false,
    cluster = false
)
```
Creates: `NoAckConsumerGroup`

### 2. Acknowledgment Consumer
```java
@RedisStreamConsumer(
    topicName = "topic", 
    groupName = "group",
    autoAck = true,
    cluster = false
)
```
Creates: `ConsumerGroup`

### 3. Cluster Consumer
```java
@RedisStreamConsumer(
    topicName = "topic", 
    groupName = "group",
    autoAck = true,
    cluster = true
)
```
Creates: `SingleClusterPelConsumerGroup`

## Automatic Bean Creation

When you use `@EnableRedisStreams`, the framework automatically creates the following beans for each consumer:

1. **SerialTopicConfig**: Configuration for the topic
2. **TopicManager**: Manages the Redis Stream topic
3. **ConsumerGroup**: The appropriate consumer group based on configuration
4. **Consumer Class**: The annotated consumer class itself

### Bean Naming Convention

- `SerialTopicConfig`: `{topicName}SerialTopicConfig`
- `TopicManager`: `{topicName}TopicManager` (unique per topic, shared between consumers of the same topic)
- `ConsumerGroup`: `{groupName}ConsumerGroup` or `{groupName}NoAckConsumerGroup` or `{groupName}SingleClusterPelConsumerGroup` (unique per group, shared between consumers of the same group)
- `Consumer Class`: `{className}` (uncapitalized)

## Requirements

### Method Requirements

Every consumer class must have a `process()` method annotated with `@Scheduled`:

```java
@Scheduled(fixedDelayString = "${redis.streams.fixed-delay:1000}")
public boolean process() {
    // Your processing logic here
    TopicEntry topicEntry = consume();
    // Process the message
    return acknowledge(topicEntry); // or return true/false
}
```

### Dependencies

Make sure you have the following dependencies in your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.redis.om</groupId>
    <artifactId>redis-om-spring</artifactId>
    <version>1.0.0-RC3</version>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

### Configuration

Ensure that `@EnableRedisStreams` is enabled in your configuration:

```java
@Configuration
@EnableRedisStreams(basePackages = "com.redis.om.streams.consumer")
public class RedisStreamsConfiguration {
    // Your configuration
}
```

## Examples

### Example 1: Basic Consumer with Consumer Name
```java
@RedisStreamConsumer(topicName = "topicFoo", groupName = "groupFoo", consumerName = "Foo")
public class FooRedisStreamsConsumer extends RedisStreamsConsumer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(fixedDelayString = "${redis.streams.fixed-delay:1000}")
    public boolean process() {
        TopicEntry topicEntry = consume();
        if (topicEntry != null) {
            logger.info("{} processing topic: {}", getClass().getSimpleName(), topicEntry);
        }
        return true;
    }
}
```

### Example 2: Acknowledgment Consumer
```java
@RedisStreamConsumer(
    topicName = "topicFoo", 
    groupName = "groupFoo",
    autoAck = true
)
public class AckRedisStreamsConsumer extends RedisStreamsConsumer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(fixedDelayString = "${redis.streams.fixed-delay:1000}")
    public boolean process() {
        TopicEntry topicEntry = consume();
        if (topicEntry != null) {
            logger.info("{} processing topic: {}", getClass().getSimpleName(), topicEntry);
            return acknowledge(topicEntry);
        }
        return false;
    }
}
```

### Example 3: No-Ack Consumer (Explicit)
```java
@RedisStreamConsumer(
    topicName = "topicFoo", 
    groupName = "groupFoo",
    autoAck = false
)
public class NoAckFooRedisStreamsConsumer extends RedisStreamsConsumer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(fixedDelayString = "${redis.streams.fixed-delay:1000}")
    public boolean process() {
        TopicEntry topicEntry = consume();
        if (topicEntry != null) {
            logger.info("{} processing topic: {}", getClass().getSimpleName(), topicEntry);
        }
        return true;
    }
}
```

## Configuration Properties

Configure your application in `application.properties`:

```properties
# Server Configuration
server.port=8080
spring.application.name=redis-om-spring-streams

# Spring Data Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.username=
spring.data.redis.password=

# Redis Streams Configuration
redis.streams.fixed-delay=5000
```

## Logging

The framework provides detailed logging for bean creation and consumer operations. You can configure logging levels in your `application.properties`:

```properties
logging.level.com.redis.om.streams.config=INFO
logging.level.com.redis.om.streams.consumer=INFO
```

## Error Handling

The framework handles various error scenarios:

- **ClassNotFoundException**: Logs error and continues with other consumers
- **InvalidTopicException**: Throws IllegalStateException during TopicManager creation
- **TopicNotFoundException**: Handled by individual consumers
- **InvalidMessageException**: Handled by producers
- **ProducerTimeoutException**: Handled by producers

## Best Practices

1. **Package Organization**: Keep your consumers in dedicated packages for better organization
2. **Bean Naming**: Use descriptive topic and group names to avoid conflicts
3. **Error Handling**: Implement proper error handling in your `process()` methods
4. **Logging**: Use appropriate log levels for debugging and monitoring
5. **Configuration**: Use environment-specific configurations for different deployment environments
6. **Scheduling**: Use configurable delays with `fixedDelayString` for easy tuning

## Troubleshooting

### Common Issues

1. **No beans created**: Check that `@EnableRedisStreams` is properly configured and base packages are correct
2. **Scheduling not working**: Ensure `@EnableScheduling` is enabled in your configuration
3. **Redis connection issues**: Verify Redis connection configuration in `application.properties`
4. **Bean conflicts**: Check for duplicate bean names, especially with topic configurations
5. **Message production issues**: Verify that the `Producer` bean is properly configured

### Debug Mode

Enable debug logging to see detailed bean creation information:

```properties
logging.level.com.redis.om.streams=DEBUG
```

## Sample project structure for this demo

```
src/main/java/com/redis/om/streams/
├── config/
│   └── RedisStreamsConfiguration.java
├── consumer/
│   ├── AckRedisStreamsConsumer.java
│   ├── FooRedisStreamsConsumer.java
│   └── NoAckFooRedisStreamsConsumer.java
├── controller/
│   └── StreamsController.java
├── model/
│   └── TextData.java
└── DemoApplication.java
```

## Running the Application

1. Start Redis server
2. Run the Spring Boot application
3. Use the REST endpoints to produce messages
4. Watch the consumer logs to see message processing 