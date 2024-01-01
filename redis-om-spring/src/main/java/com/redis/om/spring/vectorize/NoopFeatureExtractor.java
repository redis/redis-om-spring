package com.redis.om.spring.vectorize;

import java.io.InputStream;

public class NoopFeatureExtractor implements FeatureExtractor {

  @Override
  public byte[] getImageEmbeddingsAsByteArrayFor(InputStream is) {
    return new byte[0];
  }

  @Override
  public float[] getImageEmbeddingsAsFloatArrayFor(InputStream is) {
    return new float[0];
  }

  @Override
  public byte[] getFacialImageEmbeddingsAsByteArrayFor(InputStream is) {
    return new byte[0];
  }

  @Override
  public float[] getFacialImageEmbeddingsAsFloatArrayFor(InputStream is) {
    return new float[0];
  }

  @Override
  public byte[] getSentenceEmbeddingsAsByteArrayFor(String text) {
    return new byte[0];
  }

  @Override
  public float[] getSentenceEmbeddingAsFloatArrayFor(String text) {
    return new float[0];
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
