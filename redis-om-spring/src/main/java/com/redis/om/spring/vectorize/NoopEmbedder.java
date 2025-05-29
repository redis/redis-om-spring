package com.redis.om.spring.vectorize;

import java.lang.reflect.Field;
import java.util.List;

import com.redis.om.spring.metamodel.MetamodelField;

/**
 * A no-operation implementation of the {@link Embedder} interface that provides
 * empty implementations for all embedding operations. This embedder is used as
 * a fallback when no actual embedding provider is configured or available.
 *
 * <p>This implementation:</p>
 * <ul>
 * <li>Does not process any entities for embedding generation</li>
 * <li>Reports as not ready ({@code isReady()} returns false)</li>
 * <li>Returns empty lists for all embedding requests</li>
 * <li>Provides safe defaults when vector embedding is not required</li>
 * </ul>
 *
 * <p>The NoopEmbedder is particularly useful during development, testing,
 * or in scenarios where vector embeddings are optional and no embedding
 * provider has been configured.</p>
 *
 * @see Embedder
 */
public class NoopEmbedder implements Embedder {

  /**
   * Default constructor for the no-operation embedder.
   */
  public NoopEmbedder() {
  }

  /**
   * No-operation implementation that does not process the entity.
   * This method performs no embedding operations on the provided item.
   *
   * @param item the entity to process (ignored)
   */
  @Override
  public void processEntity(Object item) {
    // NOOP
  }

  /**
   * No-operation implementation that does not process any entities.
   * This method performs no embedding operations on the provided items.
   *
   * @param <S>   the type of entities
   * @param items the entities to process (ignored)
   */
  @Override
  public <S> void processEntities(Iterable<S> items) {
    // NOOP
  }

  /**
   * Returns false indicating this embedder is not ready for operations.
   * Since this is a no-operation implementation, it never reports as ready.
   *
   * @return always false
   */
  @Override
  public boolean isReady() {
    return false;
  }

  /**
   * Returns an empty list instead of generating text embeddings as bytes.
   * This no-operation implementation does not perform any embedding generation.
   *
   * @param texts the text inputs to embed (ignored)
   * @param field the field context for embedding (ignored)
   * @return an empty list
   */
  @Override
  public List<byte[]> getTextEmbeddingsAsBytes(List<String> texts, Field field) {
    return List.of();
  }

  /**
   * Returns an empty list instead of generating text embeddings as floats.
   * This no-operation implementation does not perform any embedding generation.
   *
   * @param texts the text inputs to embed (ignored)
   * @param field the field context for embedding (ignored)
   * @return an empty list
   */
  @Override
  public List<float[]> getTextEmbeddingsAsFloats(List<String> texts, Field field) {
    return List.of();
  }

  /**
   * Returns an empty list instead of generating text embeddings as bytes.
   * This no-operation implementation does not perform any embedding generation.
   *
   * @param description the text inputs to embed (ignored)
   * @param field       the metamodel field context for embedding (ignored)
   * @return an empty list
   */
  @Override
  public List<byte[]> getTextEmbeddingsAsBytes(List<String> description, MetamodelField<?, ?> field) {
    return List.of();
  }

  /**
   * Returns an empty list instead of generating text embeddings as floats.
   * This no-operation implementation does not perform any embedding generation.
   *
   * @param texts the text inputs to embed (ignored)
   * @param field the metamodel field context for embedding (ignored)
   * @return an empty list
   */
  @Override
  public List<float[]> getTextEmbeddingsAsFloats(List<String> texts, MetamodelField<?, ?> field) {
    return List.of();
  }

}
