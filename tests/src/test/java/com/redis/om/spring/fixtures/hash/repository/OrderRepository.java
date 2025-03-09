package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.Order;
import com.redis.om.spring.fixtures.hash.model.OrderId;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface OrderRepository extends RedisEnhancedRepository<Order, OrderId> {
}
