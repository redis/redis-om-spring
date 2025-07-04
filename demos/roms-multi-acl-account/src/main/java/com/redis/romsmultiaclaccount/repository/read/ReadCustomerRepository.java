package com.redis.romsmultiaclaccount.repository.read;

import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.romsmultiaclaccount.model.Customer;

public interface ReadCustomerRepository extends RedisDocumentRepository<Customer, String> {
}