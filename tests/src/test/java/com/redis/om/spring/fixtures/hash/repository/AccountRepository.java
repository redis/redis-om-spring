package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.Account;
import com.redis.om.spring.fixtures.hash.model.AccountId;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface AccountRepository extends RedisEnhancedRepository<Account, AccountId> {
}
