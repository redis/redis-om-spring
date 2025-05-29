package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.vector.KNNPredicate;

/**
 * Metamodel field representation for vector fields that support similarity search.
 * <p>
 * This class provides a type-safe way to construct vector similarity queries
 * using k-nearest neighbors (KNN) search. Vector fields are typically used
 * for storing high-dimensional embeddings that represent features of text,
 * images, or other data types for similarity-based retrieval.
 * </p>
 * <p>
 * Vector similarity search enables use cases such as:
 * <ul>
 * <li>Semantic text search using text embeddings</li>
 * <li>Image similarity search using visual embeddings</li>
 * <li>Recommendation systems based on feature similarity</li>
 * <li>Anomaly detection by finding dissimilar vectors</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Define entity with vector field
 * @Document
 * public class Product {
 * 
 * @VectorIndexed(algorithm = VectorAlgorithm.HNSW,
 *                          dimension = 768,
 *                          distanceMetric = DistanceMetric.COSINE)
 *                          private float[] embedding;
 *                          }
 * 
 *                          // Use metamodel for type-safe queries
 *                          Product$.embedding.knn(10, queryVector)
 *                          .where(Product$.category.eq("electronics"))
 *                          .sortBy(Product$.price, Order.ASC);
 *                          }</pre>
 * 
 * @param <E> the entity type containing this field
 * @param <T> the field type (typically byte[] or float[])
 * 
 * @see com.redis.om.spring.annotations.VectorIndexed
 * @see com.redis.om.spring.search.stream.predicates.vector.KNNPredicate
 * @since 0.1.0
 */
public class VectorField<E, T> extends MetamodelField<E, T> {
  /**
   * Creates a new vector field with the specified field accessor and indexing status.
   * 
   * @param field   the search field accessor containing field metadata
   * @param indexed whether this field is indexed for search
   */
  public VectorField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  /**
   * Creates a new vector field for the specified entity class and field name.
   * 
   * @param targetClass the entity class containing this field
   * @param fieldName   the name of the vector field
   */
  public VectorField(Class<E> targetClass, String fieldName) {
    super(targetClass, fieldName);
  }

  /**
   * Creates a k-nearest neighbors (KNN) search predicate for byte array vectors.
   * <p>
   * This method constructs a similarity search query that finds the k most similar
   * vectors to the provided query vector. The similarity is measured using the
   * distance metric configured in the {@link com.redis.om.spring.annotations.VectorIndexed}
   * annotation.
   * </p>
   * 
   * @param k             the number of nearest neighbors to retrieve
   * @param blobAttribute the query vector as a byte array
   * @return a KNN predicate for building vector similarity queries
   */
  public KNNPredicate<E, T> knn(int k, byte[] blobAttribute) {
    return new KNNPredicate<>(searchFieldAccessor, k, blobAttribute);
  }

  /**
   * Creates a k-nearest neighbors (KNN) search predicate for float array vectors.
   * <p>
   * This method constructs a similarity search query that finds the k most similar
   * vectors to the provided query vector. The similarity is measured using the
   * distance metric configured in the {@link com.redis.om.spring.annotations.VectorIndexed}
   * annotation.
   * </p>
   * 
   * @param k             the number of nearest neighbors to retrieve
   * @param blobAttribute the query vector as a float array
   * @return a KNN predicate for building vector similarity queries
   */
  public KNNPredicate<E, T> knn(int k, float[] blobAttribute) {
    return new KNNPredicate<>(searchFieldAccessor, k, blobAttribute);
  }
}
