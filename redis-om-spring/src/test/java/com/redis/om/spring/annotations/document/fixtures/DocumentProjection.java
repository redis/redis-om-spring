package com.redis.om.spring.annotations.document.fixtures;

import org.springframework.beans.factory.annotation.Value;

public interface DocumentProjection {
  String getName();

  @Value("#{target.name + ' ' + target.test}")
  String getSpelTest();

}
