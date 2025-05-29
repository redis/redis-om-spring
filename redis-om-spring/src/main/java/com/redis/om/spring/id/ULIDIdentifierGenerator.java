package com.redis.om.spring.id;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.keyvalue.core.IdentifierGenerator;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ClassUtils;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;

/**
 * ULID (Universally Unique Lexicographically Sortable Identifier) generator for Redis OM Spring.
 * <p>
 * This enum implements Spring Data's {@link IdentifierGenerator} interface to provide
 * ULID-based identifier generation for Redis entities. ULIDs are the default identifier
 * format in Redis OM Spring, offering several advantages over traditional UUIDs:
 * </p>
 * <ul>
 * <li><strong>Lexicographic Sorting:</strong> ULIDs can be sorted by their string representation,
 * which naturally orders them by creation time</li>
 * <li><strong>Time-based Component:</strong> The first 48 bits represent a timestamp,
 * allowing for time-based queries and efficient indexing</li>
 * <li><strong>Monotonic Generation:</strong> ULIDs generated within the same millisecond
 * are guaranteed to be ordered</li>
 * <li><strong>Compact Representation:</strong> 26-character string representation using
 * Crockford's base32 encoding</li>
 * <li><strong>Performance:</strong> Better performance for range queries and sorting
 * operations in Redis</li>
 * </ul>
 * <p>
 * The generator supports multiple output types:
 * </p>
 * <ul>
 * <li>{@link Ulid} - Native ULID object</li>
 * <li>{@link String} - String representation of ULID (default for @Id fields)</li>
 * <li>{@link Integer} - Random integer (falls back to SecureRandom)</li>
 * <li>{@link Long} - Random long (falls back to SecureRandom)</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Document
 * public class User {
 * 
 * @Id
 *     private String id; // Will be automatically populated with a ULID string
 * 
 *     private String name;
 *     // ... other fields
 *     }
 *     }</pre>
 *
 * @see com.github.f4b6a3.ulid.Ulid
 * @see org.springframework.data.keyvalue.core.IdentifierGenerator
 * @see UlidCreator
 * @since 0.1.0
 */
public enum ULIDIdentifierGenerator implements IdentifierGenerator {

  /**
   * Singleton instance of the ULID identifier generator.
   */
  INSTANCE;

  /**
   * Generates an identifier of the specified type.
   * <p>
   * This method generates identifiers based on the requested type:
   * </p>
   * <ul>
   * <li>For {@link Ulid} types: generates a monotonic ULID</li>
   * <li>For {@link String} types: generates a monotonic ULID and converts it to string</li>
   * <li>For {@link Integer} types: generates a secure random integer</li>
   * <li>For {@link Long} types: generates a secure random long</li>
   * </ul>
   * <p>
   * Monotonic ULIDs ensure that identifiers generated within the same millisecond
   * maintain their ordering, which is crucial for distributed systems and
   * time-series data.
   * </p>
   *
   * @param <T>            the type of identifier to generate
   * @param identifierType the type information for the identifier
   * @return the generated identifier of the requested type
   * @throws InvalidDataAccessApiUsageException if the requested type is not supported
   */
  @SuppressWarnings(
    "unchecked"
  )
  @Override
  public <T> T generateIdentifierOfType(TypeInformation<T> identifierType) {
    Class<?> type = identifierType.getType();

    if (ClassUtils.isAssignable(Ulid.class, type)) {
      return (T) UlidCreator.getMonotonicUlid();
    } else if (ClassUtils.isAssignable(String.class, type)) {
      return (T) UlidCreator.getMonotonicUlid().toString();
    } else if (ClassUtils.isAssignable(Integer.class, type)) {
      return (T) Integer.valueOf(SecureRandom.getSecureRandom().nextInt());
    } else if (ClassUtils.isAssignable(Long.class, type)) {
      return (T) Long.valueOf(SecureRandom.getSecureRandom().nextLong());
    }

    throw new InvalidDataAccessApiUsageException(String.format(
        "Identifier cannot be generated for %s. Supported types are: ULID, String, Integer, and Long.", identifierType
            .getType().getName()));
  }
}
