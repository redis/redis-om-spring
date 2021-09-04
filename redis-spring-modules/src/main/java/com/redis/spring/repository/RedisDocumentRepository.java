package com.redis.spring.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface RedisDocumentRepository<T, ID> extends PagingAndSortingRepository<T, ID> {
}
