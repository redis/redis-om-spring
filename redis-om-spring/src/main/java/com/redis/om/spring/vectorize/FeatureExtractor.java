package com.redis.om.spring.vectorize;

import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.io.InputStream;

public interface FeatureExtractor {
    void processEntity(byte[] redisKey, Object item);

    byte[] getImageEmbeddingsFor(InputStream is);

    byte[] getFacialImageEmbeddingsFor(InputStream is) throws IOException, TranslateException;

    byte[] getSentenceEmbeddingsFor(String text);

    void processEntity(Object item);

    boolean isReady();
}
