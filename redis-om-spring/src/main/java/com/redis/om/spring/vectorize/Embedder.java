package com.redis.om.spring.vectorize;

import com.redis.om.spring.metamodel.MetamodelField;

import java.lang.reflect.Field;
import java.util.List;

public interface Embedder {

  void processEntity(Object item);

  <S> void processEntities(Iterable<S> items);

  boolean isReady();

  List<byte[]> getTextEmbeddingsAsBytes(List<String> texts, Field field);

  List<float[]> getTextEmbeddingsAsFloats(List<String> texts, Field field);

  List<byte[]> getTextEmbeddingsAsBytes(List<String> texts, MetamodelField<?, ?> field);

  List<float[]> getTextEmbeddingsAsFloats(List<String> texts, MetamodelField<?, ?> field);
}
