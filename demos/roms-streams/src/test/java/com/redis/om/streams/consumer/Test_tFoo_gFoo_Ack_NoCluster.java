package com.redis.om.streams.consumer;

import com.redis.om.streams.annotation.RedisStreamConsumer;

@RedisStreamConsumer(topicName = "topicFoo", groupName = "groupFoo", autoAck = true, consumerName = "", cluster = false)
public class Test_tFoo_gFoo_Ack_NoCluster extends RedisStreamsConsumer {}