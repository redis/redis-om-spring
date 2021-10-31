package com.redis.om.spring.annotations.bloom.fixtures;

import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.pds.BloomOperations;

public class EmailTakenImpl implements EmailTaken {
  
  @Autowired
  RedisModulesOperations<String, String> modulesOperations;

  @Override
  public boolean isEmailTaken(String email) {
    BloomOperations<String> ops = modulesOperations.opsForBloom();
    return ops.exists("bf_person_email", email);
  }

}
