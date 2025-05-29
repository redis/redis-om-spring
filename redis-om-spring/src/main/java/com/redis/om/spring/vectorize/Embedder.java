package com.redis.om.spring.vectorize;

import java.lang.reflect.Field;
import java.util.List;

import com.redis.om.spring.metamodel.MetamodelField;

/**
 * Interface for text embedding operations.
 */
public interface Embedder {

  /**
   * Process single entity for embeddings.
   * 
   * @param item the entity to process
   */
  void processEntity(Object item);

  /**
   * Process multiple entities for embeddings.
   * 
   * @param <S>   entity type
   * @param items the entities to process
   */
  <S> void processEntities(Iterable<S> items);

  /**
   * Check if embedder is ready for use.
   * 
   * @return true if ready
   */
  boolean isReady();

  /**
   * Get text embeddings as byte arrays.
   * 
   * @param texts the texts to embed
   * @param field the target field
   * @return list of embeddings as byte arrays
   */
  List<byte[]> getTextEmbeddingsAsBytes(List<String> texts, Field field);

  /**
   * Get text embeddings as float arrays.
   * 
   * @param texts the texts to embed
   * @param field the target field
   * @return list of embeddings as float arrays
   */
  List<float[]> getTextEmbeddingsAsFloats(List<String> texts, Field field);

  /**
   * Get text embeddings as byte arrays using metamodel field.
   * 
   * @param texts the texts to embed
   * @param field the metamodel field
   * @return list of embeddings as byte arrays
   */
  List<byte[]> getTextEmbeddingsAsBytes(List<String> texts, MetamodelField<?, ?> field);

  /**
   * Get text embeddings as float arrays using metamodel field.
   * 
   * @param texts the texts to embed
   * @param field the metamodel field
   * @return list of embeddings as float arrays
   */
  List<float[]> getTextEmbeddingsAsFloats(List<String> texts, MetamodelField<?, ?> field);
}
