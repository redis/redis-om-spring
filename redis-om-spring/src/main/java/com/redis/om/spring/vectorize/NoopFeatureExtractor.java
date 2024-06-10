package com.redis.om.spring.vectorize;

public class NoopFeatureExtractor implements FeatureExtractor {

  @Override
  public void processEntity(Object item) {
    // NOOP
  }

  @Override
  public boolean isReady() {
    return false;
  }
}
