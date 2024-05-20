package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.PizzaOrder;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface PizzaOrderRepository extends RedisDocumentRepository<PizzaOrder, Integer> {
}
