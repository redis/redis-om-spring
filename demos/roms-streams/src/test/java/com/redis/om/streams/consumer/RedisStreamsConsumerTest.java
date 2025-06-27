package com.redis.om.streams.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.om.streams.Producer;
import com.redis.om.streams.TopicEntry;
import com.redis.om.streams.TopicEntryId;
import com.redis.om.streams.annotation.RedisStreamConsumer;
import com.redis.om.streams.command.serial.TopicProducer;
import com.redis.om.streams.model.TextData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.JedisPooled;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class RedisStreamsConsumerTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:8.0.2-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.data.redis.username", () -> "");
        registry.add("spring.data.redis.password", () -> "");
        registry.add("redis.streams.fixed-delay", () -> "1000");
    }

    @Autowired
    private Test_tFoo_gBar_Ack_NoCluster test_tFoo_gBar_ack_noCluster;
    @Autowired
    private Test_tFoo_gBar_NoAck_NoCluster test_tFoo_gBar_noAck_noCluster;
    @Autowired
    private Test_tFoo_gFoo_Ack_NoCluster test_tFoo_gFoo_ack_noCluster;
    @Autowired
    private Test_tFoo_gFoo_NoAck_NoCluster test_tFoo_gFoo_noAck_noCluster;

    @Autowired
    private JedisPooled jedisPooled;

    private Producer producerFoo;
    private Producer producerBar;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        producerFoo = new TopicProducer(jedisPooled, "topicFoo");
        producerBar = new TopicProducer(jedisPooled, "topicFoo");
        objectMapper = new ObjectMapper();
    }

    @Test
    void testConsumerInitialization() {
        assertNotNull(test_tFoo_gBar_ack_noCluster, "Consumer should be initialized");
        assertNotNull(test_tFoo_gBar_noAck_noCluster, "Consumer should be initialized");
        assertNotNull(test_tFoo_gFoo_ack_noCluster, "Consumer should be initialized");
        assertNotNull(test_tFoo_gFoo_noAck_noCluster, "Consumer should be initialized");
    }

    @Test
    void testProduce_tFoo_gFoo_noAck_noCluster() throws Exception {
        TextData textData = TextData.of();
        textData.setId(1);
        textData.setName("Test Name 1");
        textData.setDescription("Test Description 1");
        Map<String, String> payload = objectMapper.convertValue(textData, new TypeReference<>() {});
        TopicEntryId topicEntryId = producerFoo.produce(payload);
        System.out.println(topicEntryId);
        assertNotNull(topicEntryId, "TopicEntryId should not be null");

        TopicEntry topicEntry = test_tFoo_gFoo_noAck_noCluster.consume();
        assertNotNull(topicEntry, "TopicEntry should not be null");
        System.out.println(topicEntry);
        assert topicEntry.getId().equals(topicEntryId);

        boolean ack = test_tFoo_gFoo_noAck_noCluster.acknowledge(topicEntry);
        assertFalse(ack, "TopicEntry should not be acknowledged");
    }

    @Test
    void testProduce_tFoo_gFoo_ack_noCluster() throws Exception {
        TextData textData = TextData.of();
        textData.setId(1);
        textData.setName("Test Name 1");
        textData.setDescription("Test Description 1");
        Map<String, String> payload = objectMapper.convertValue(textData, new TypeReference<>() {});
        TopicEntryId topicEntryId = producerFoo.produce(payload);
        System.out.println(topicEntryId);
        assertNotNull(topicEntryId, "TopicEntryId should not be null");

        TopicEntry topicEntry = test_tFoo_gFoo_ack_noCluster.consume();
        assertNotNull(topicEntry, "TopicEntry should not be null");
        System.out.println(topicEntry);
        assert topicEntry.getId().equals(topicEntryId);

        boolean ack = test_tFoo_gFoo_ack_noCluster.acknowledge(topicEntry);
        assertTrue(ack, "TopicEntry should be acknowledged");
    }

    /**
     * FIXME:
     * I produce a message on a topic named <code>topicFoo</code>.
     * Then I have two consumers of type {@link com.redis.om.streams.command.serial.ConsumerGroup},
     * one belonging to <code>groupFoo</code>, and the other one belonging to <code>groupBar</code>.
     * Both consume the exact same message from the exact same topic:
     * <pre>
     * Consumer for Group Foo:TopicEntry(streamName=__rsj:topic:stream:topicFoo:0, groupName=groupFoo, id=1750954394888-0-0, message={name=Test Name 1, description=Test Description 1, id=1})
     * Consumer for Group Bar:TopicEntry(streamName=__rsj:topic:stream:topicFoo:0, groupName=groupBar, id=1750954394888-0-0, message={name=Test Name 1, description=Test Description 1, id=1})
     * </pre>
     *
     * Now, if I acknowledge the <code>TopicEntry</code> coming from the consumer of <code>groupFoo</code>
     * with the consumer of <code>groupBar</code>, I would expect an error, an exception, something...
     * Instead, the <code>TopicEntry</code> gets acknowledged despite the mismatch on the consumer group paternity.
     */
//    @Test
    void testAckWrongGroups() throws Exception {
        TextData textData = TextData.of();
        textData.setId(1);
        textData.setName("Test Name 1");
        textData.setDescription("Test Description 1");
        Map<String, String> payload = objectMapper.convertValue(textData, new TypeReference<>() {});
        TopicEntryId topicEntryId = producerFoo.produce(payload);
        System.out.println(topicEntryId);
        assertNotNull(topicEntryId, "TopicEntryId should not be null");

        TopicEntry topicEntryGroupFoo;
        TopicEntry topicEntryGroupBar;
        boolean ack;
        topicEntryGroupFoo = test_tFoo_gFoo_ack_noCluster.consume();
        System.out.println("Consumer for Group Foo:" + topicEntryGroupFoo);
        assertNotNull(topicEntryGroupFoo, "TopicEntry should not be null");
        assert topicEntryGroupFoo.getId().equals(topicEntryId);

        topicEntryGroupBar = test_tFoo_gBar_ack_noCluster.consume();
        System.out.println("Consumer for Group Bar:" + topicEntryGroupBar);
        assertNotNull(topicEntryGroupBar, "TopicEntry should not be null");
        assert topicEntryGroupBar.getId().equals(topicEntryId);

        ack = test_tFoo_gFoo_ack_noCluster.acknowledge(topicEntryGroupBar);
        assertFalse(ack, "TopicEntry should not be acknowledged because of consumer wrong group");
        ack = test_tFoo_gBar_ack_noCluster.acknowledge(topicEntryGroupFoo);
        assertFalse(ack, "TopicEntry should not be acknowledged because of consumer wrong group");

        ack = test_tFoo_gFoo_ack_noCluster.acknowledge(topicEntryGroupBar);
        assertTrue(ack, "TopicEntry should be acknowledged because of consumer right group");
        ack = test_tFoo_gBar_ack_noCluster.acknowledge(topicEntryGroupFoo);
        assertTrue(ack, "TopicEntry should be acknowledged because of consumer right group");
    }

    @Test
    void testProduce_tFoo_gFoo_gFoo_ack_noAck_noCluster() throws Exception {
        TextData textData = TextData.of();
        textData.setId(1);
        textData.setName("Test Name 1");
        textData.setDescription("Test Description 1");
        Map<String, String> payload = objectMapper.convertValue(textData, new TypeReference<>() {});
        TopicEntryId topicEntryId = producerFoo.produce(payload);
        System.out.println(topicEntryId);
        assertNotNull(topicEntryId, "TopicEntryId should not be null");

        TopicEntry topicEntry;
        boolean ack;

        topicEntry = test_tFoo_gFoo_ack_noCluster.consume();
        System.out.println("Consumer for Group Foo:" + topicEntry);
        assertNotNull(topicEntry, "TopicEntry should not be null");
        ack = test_tFoo_gFoo_ack_noCluster.acknowledge(topicEntry);
        assertTrue(ack, "TopicEntry should be acknowledged");

        topicEntry = test_tFoo_gFoo_noAck_noCluster.consume();
        System.out.println("Consumer for Group Foo:" + topicEntry);
        assertNull(topicEntry, "TopicEntry should be null");
        ack = test_tFoo_gFoo_noAck_noCluster.acknowledge(topicEntry);
        assertFalse(ack, "TopicEntry should not be acknowledged");

    }

}