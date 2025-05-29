package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation for marking a field as containing the document's search score.
 * <p>
 * When applied to a field in a Redis OM Spring entity, this annotation indicates
 * that the field should be populated with the document's search relevance score
 * when the entity is retrieved as part of a search operation. This allows
 * applications to access ranking information directly within entity objects.
 * </p>
 * <p>
 * The annotated field should be of a numeric type (typically {@code double})
 * that can hold the search score value. The score represents the relevance
 * of the document to the search query, with higher scores indicating better matches.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Document
 * public class Article {
 * 
 * @Id
 *     private String id;
 * 
 * @Searchable
 *             private String title;
 * 
 * @DocumentScore
 *                private double searchScore;
 * 
 *                // getters and setters...
 *                }
 * 
 *                // When searching for articles, the searchScore field will be populated
 *                List<Article> results = repository.search("technology");
 *                double relevance = results.get(0).getSearchScore();
 *                }</pre>
 *                <p>
 *                This annotation is particularly useful for:
 *                <ul>
 *                <li>Displaying search relevance to users</li>
 *                <li>Implementing custom result ranking logic</li>
 *                <li>Filtering results based on minimum score thresholds</li>
 *                <li>Analytics and debugging of search quality</li>
 *                </ul>
 *
 * @see com.redis.om.spring.annotations.Document
 * @see com.redis.om.spring.annotations.Searchable
 * @since 0.1.0
 */
@Documented
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.FIELD, ElementType.ANNOTATION_TYPE }
)
public @interface DocumentScore {

}
