package com.redis.om.spring.id;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.util.StringUtils;

/**
 * Utility class for providing secure random number generation functionality.
 * <p>
 * This class manages a singleton instance of {@link java.security.SecureRandom} that is
 * optimized for the operating system platform. It attempts to use platform-specific
 * secure random algorithms for better performance and security.
 * </p>
 * <p>
 * The implementation uses lazy initialization with atomic reference for thread-safe
 * access to the secure random instance.
 * </p>
 */
public class SecureRandom {

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private SecureRandom() {
    // Utility class - no instantiation
  }

  private static final AtomicReference<java.security.SecureRandom> secureRandom = new AtomicReference<>(null);

  /**
   * Returns a secure random instance optimized for the current platform.
   * <p>
   * This method uses lazy initialization to create a single shared instance of
   * {@link java.security.SecureRandom} using the best available algorithm for
   * the current operating system.
   * </p>
   *
   * @return a secure random instance
   * @throws InvalidDataAccessApiUsageException if no suitable secure random algorithm is available
   */
  public static java.security.SecureRandom getSecureRandom() {
    return secureRandom.updateAndGet(sr -> sr != null ? sr : createSecureRandom());
  }

  /**
   * Creates a new secure random instance using the best available algorithm.
   * <p>
   * This method iterates through platform-specific algorithms and attempts to create
   * a secure random instance using the first available algorithm. If no algorithms
   * are available, it throws an exception.
   * </p>
   *
   * @return a new secure random instance
   * @throws InvalidDataAccessApiUsageException if no suitable algorithm is found
   */
  private static java.security.SecureRandom createSecureRandom() {
    for (String algorithm : OsTools.secureRandomAlgorithmNames()) {
      try {
        return java.security.SecureRandom.getInstance(algorithm);
      } catch (NoSuchAlgorithmException e) {
        // ignore and try the next algorithm.
      }
    }

    throw new InvalidDataAccessApiUsageException(
        "Could not create SecureRandom instance for any of the specified algorithms: " + StringUtils
            .collectionToCommaDelimitedString(OsTools.secureRandomAlgorithmNames()));
  }
}
