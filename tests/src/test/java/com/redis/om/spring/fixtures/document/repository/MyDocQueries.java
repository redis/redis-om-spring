package com.redis.om.spring.fixtures.document.repository;

import java.util.Optional;

import com.redis.om.spring.fixtures.document.model.MyDoc;

public interface MyDocQueries {
  Optional<MyDoc> findByTitle(String title);
}
