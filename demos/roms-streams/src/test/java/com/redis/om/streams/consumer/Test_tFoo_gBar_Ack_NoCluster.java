package com.redis.om.streams.consumer;

import com.redis.om.streams.annotation.RedisStreamConsumer;

@RedisStreamConsumer(topicName = "topicFoo", groupName = "groupBar", autoAck = false, consumerName = "", cluster = false)
public class Test_tFoo_gBar_Ack_NoCluster extends RedisStreamsConsumer {}