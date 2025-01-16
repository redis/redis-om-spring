package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Account;
import com.redis.om.spring.fixtures.hash.model.AccountId;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface AccountRepository extends RedisDocumentRepository<Account, AccountId> {
}
