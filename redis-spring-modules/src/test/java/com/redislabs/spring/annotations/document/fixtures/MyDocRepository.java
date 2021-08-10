package com.redislabs.spring.annotations.document.fixtures;

import com.redislabs.spring.repository.RedisDocumentRepository;

public interface MyDocRepository extends RedisDocumentRepository<MyDoc, String>, MyDocQueries {
}
