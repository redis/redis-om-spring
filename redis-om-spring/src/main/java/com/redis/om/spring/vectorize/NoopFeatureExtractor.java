package com.redis.om.spring.vectorize;

import ai.djl.translate.TranslateException;

import java.io.IOException;
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
  public byte[] getFacialImageEmbeddingsAsByteArrayFor(InputStream is) throws IOException, TranslateException {
    return new byte[0];
  }

  @Override
  public float[] getFacialImageEmbeddingsAsFloatArrayFor(InputStream is) throws IOException, TranslateException {
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
