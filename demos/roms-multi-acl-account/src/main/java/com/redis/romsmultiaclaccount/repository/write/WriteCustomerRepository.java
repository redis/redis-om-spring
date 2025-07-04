package com.redis.romsmultiaclaccount.repository.write;

import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.romsmultiaclaccount.model.Customer;

public interface WriteCustomerRepository extends RedisDocumentRepository<Customer, String> {
}