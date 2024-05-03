package com.redis.om.spring.annotations.hash.fixtures;

import org.springframework.beans.factory.annotation.Value;

public interface HashProjection {
  String getName();

  @Value("#{target.name + ' ' + target.test}")
  String getSpelTest();
}
