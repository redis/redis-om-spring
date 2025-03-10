package com.redis.om.spring.fixtures.document.repository;

import org.springframework.beans.factory.annotation.Value;

public interface DocumentProjection {
  String getName();

  @Value("#{target.name + ' ' + target.test}")
  String getSpelTest();

}
