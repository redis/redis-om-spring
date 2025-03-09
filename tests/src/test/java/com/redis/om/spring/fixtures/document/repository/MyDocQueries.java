package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.MyDoc;

import java.util.Optional;

public interface MyDocQueries {
  Optional<MyDoc> findByTitle(String title);
}
