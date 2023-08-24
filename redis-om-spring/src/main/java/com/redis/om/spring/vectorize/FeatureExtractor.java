package com.redis.om.spring.vectorize;

import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.io.InputStream;

public interface FeatureExtractor {
  byte[] getImageEmbeddingsAsByteArrayFor(InputStream is);

  float[] getImageEmbeddingsAsFloatArrayFor(InputStream is);

  byte[] getFacialImageEmbeddingsAsByteArrayFor(InputStream is) throws IOException, TranslateException;

  float[] getFacialImageEmbeddingsAsFloatArrayFor(InputStream is) throws IOException, TranslateException;

  byte[] getSentenceEmbeddingsAsByteArrayFor(String text);

  float[] getSentenceEmbeddingAsFloatArrayFor(String text);

  void processEntity(Object item);

  boolean isReady();
}
