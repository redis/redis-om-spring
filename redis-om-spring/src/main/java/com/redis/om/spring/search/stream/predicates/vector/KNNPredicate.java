package com.redis.om.spring.search.stream.predicates.vector;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;

import redis.clients.jedis.search.querybuilder.Node;

/**
 * A vector similarity predicate that performs K-Nearest Neighbors (KNN) search
 * to find entities with vectors most similar to a query vector.
 * 
 * <p>This predicate is designed for use with fields annotated with {@code @VectorIndexed}
 * and enables vector similarity search using machine learning embeddings. It supports
 * both byte arrays and float arrays as vector representations.</p>
 * 
 * <p>The predicate generates Redis KNN queries in the format:</p>
 * <pre>{@code (base_query)=>[KNN $K @field $blob_param]}</pre>
 * 
 * <p>KNN search finds the K most similar vectors based on the configured distance
 * metric (cosine, euclidean, etc.) and is commonly used for:</p>
 * <ul>
 * <li>Semantic search with text embeddings</li>
 * <li>Image similarity search</li>
 * <li>Recommendation systems</li>
 * <li>Content similarity matching</li>
 * </ul>
 * 
 * <p>Example usage in entity streams:</p>
 * <pre>
 * // Find 10 most similar products based on description embeddings
 * float[] queryVector = embeddingService.embed("wireless headphones");
 * entityStream.filter(Product$.DESCRIPTION_EMBEDDING.knn(10, queryVector))
 * 
 * // Find similar images using byte array embeddings
 * byte[] imageEmbedding = imageProcessor.extractFeatures(image);
 * entityStream.filter(Image$.FEATURES.knn(5, imageEmbedding))
 * </pre>
 * 
 * @param <E> the entity type being filtered
 * @param <T> the field type (typically byte[] or float[] vectors)
 * 
 * @since 1.0
 * @see BaseAbstractPredicate
 * @see com.redis.om.spring.annotations.VectorIndexed
 */
public class KNNPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  /** The number of nearest neighbors to find */
  private final int k;

  /** The query vector as byte array (used for byte-based embeddings) */
  private final byte[] blob;

  /** The query vector as float array (used for float-based embeddings) */
  private final float[] floats;

  /**
   * Creates a new KNNPredicate for byte array vectors.
   * 
   * @param field the field accessor for the target vector field
   * @param k     the number of nearest neighbors to find
   * @param blob  the query vector as a byte array
   */
  public KNNPredicate(SearchFieldAccessor field, int k, byte[] blob) {
    super(field);
    this.k = k;
    this.blob = blob;
    this.floats = null;
  }

  /**
   * Creates a new KNNPredicate for float array vectors.
   * 
   * @param field  the field accessor for the target vector field
   * @param k      the number of nearest neighbors to find
   * @param floats the query vector as a float array
   */
  public KNNPredicate(SearchFieldAccessor field, int k, float[] floats) {
    super(field);
    this.k = k;
    this.blob = null;
    this.floats = floats;
  }

  /**
   * Returns the number of nearest neighbors to find.
   * 
   * @return the K value for KNN search
   */
  public int getK() {
    return k;
  }

  /**
   * Returns the query vector as a byte array.
   * 
   * @return the byte array vector, or null if using float array
   */
  public byte[] getBlobAttribute() {
    return blob;
  }

  /**
   * Returns the query vector as a float array.
   * 
   * @return the float array vector, or null if using byte array
   */
  public float[] getDoublesAttribute() {
    return floats;
  }

  /**
   * Returns the parameter name for the blob attribute in the KNN query.
   * 
   * @return the blob parameter name used in Redis KNN queries
   */
  public String getBlobAttributeName() {
    return String.format("%s_blob", getSearchAlias());
  }

  /**
   * Applies this KNN predicate to the given query node.
   * 
   * <p>This method generates a Redis KNN search query that finds the K most
   * similar vectors to the provided query vector. The query is formatted as:</p>
   * <pre>{@code (base_query)=>[KNN $K @field $blob_param]}</pre>
   * 
   * <p>The method creates a custom Node implementation that properly formats
   * the KNN query syntax required by RediSearch vector similarity search.</p>
   * 
   * @param root the base query node to apply this predicate to
   * @return a specialized Node containing the KNN query syntax
   */
  @Override
  public Node apply(Node root) {
    String query = String.format("(%s)=>[KNN $K @%s $%s]", root.toString().isBlank() ? "*" : root.toString(),
        getSearchAlias(), getBlobAttributeName());

    return new Node() {
      @Override
      public String toString() {
        return query;
      }

      @Override
      public String toString(Parenthesize mode) {
        return query;
      }
    };
  }

}
