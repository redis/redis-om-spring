package com.redis.om.spring.vectorize;

import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.io.InputStream;

public class NoopFeatureExtractor implements FeatureExtractor {
  @Override
  public void processEntity(byte[] redisKey, Object item) {
    // NOOP
  }

  @Override
  public byte[] getImageEmbeddingsFor(InputStream is) {
    return new byte[0];
  }

  @Override
  public byte[] getFacialImageEmbeddingsFor(InputStream is) throws IOException, TranslateException {
    return new byte[0];
  }

  @Override
  public byte[] getSentenceEmbeddingsFor(String text) {
    return new byte[0];
  }

  @Override
  public void processEntity(Object item) {
    // NOOP
  }

  @Override
  public boolean isReady() {
    return false;
  }
}
