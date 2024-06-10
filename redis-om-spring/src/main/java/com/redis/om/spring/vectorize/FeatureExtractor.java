package com.redis.om.spring.vectorize;

public interface FeatureExtractor {

  void processEntity(Object item);

  boolean isReady();
}
