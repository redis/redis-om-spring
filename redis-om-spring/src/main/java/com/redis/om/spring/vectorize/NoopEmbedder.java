package com.redis.om.spring.vectorize;

import java.lang.reflect.Field;
import java.util.List;

import com.redis.om.spring.metamodel.MetamodelField;

public class NoopEmbedder implements Embedder {

  @Override
  public void processEntity(Object item) {
    // NOOP
  }

  @Override
  public <S> void processEntities(Iterable<S> items) {
    // NOOP
  }

  @Override
  public boolean isReady() {
    return false;
  }

  @Override
  public List<byte[]> getTextEmbeddingsAsBytes(List<String> texts, Field field) {
    return List.of();
  }

  @Override
  public List<float[]> getTextEmbeddingsAsFloats(List<String> texts, Field field) {
    return List.of();
  }

  @Override
  public List<byte[]> getTextEmbeddingsAsBytes(List<String> description, MetamodelField<?, ?> field) {
    return List.of();
  }

  @Override
  public List<float[]> getTextEmbeddingsAsFloats(List<String> texts, MetamodelField<?, ?> field) {
    return List.of();
  }

}
