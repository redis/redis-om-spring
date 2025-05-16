package com.redis.om.spring.fixtures.hash.repository;

import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.pds.CountMinSketchOperations;

@SuppressWarnings(
  "ALL"
)
public class UserIdCountImpl implements UserIdCount {

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @Override
  public long getUserIdCountCustom(String userId) {
    CountMinSketchOperations<String> ops = modulesOperations.opsForCountMinSketch();
    return ops.cmsQuery("cms_user_id_count", userId).get(0);
  }
}