package com.redis.om.spring.id;

import java.util.Arrays;
import java.util.List;

/**
 * Operating system utility class for Redis OM Spring ID generation.
 * <p>
 * This utility class provides operating system-specific functionality to support
 * secure random number generation for ULID (Universally Unique Lexicographically
 * Sortable Identifier) creation. It detects the current operating system and
 * provides appropriate secure random algorithm preferences for optimal entropy
 * generation.
 * </p>
 * <p>
 * The class maintains lists of preferred secure random algorithms for different
 * operating system families:
 * <ul>
 * <li>Linux, macOS, and Solaris: NativePRNGBlocking, NativePRNGNonBlocking, NativePRNG, SHA1PRNG</li>
 * <li>Windows: SHA1PRNG, Windows-PRNG</li>
 * </ul>
 *
 * @see java.security.SecureRandom
 * @see ULIDIdentifierGenerator
 * @since 0.1.0
 */
public class OsTools {

  /**
   * Private constructor to prevent instantiation of utility class.
   * <p>
   * This constructor is private to enforce the utility class pattern,
   * as all methods in this class are static and no instances should be created.
   * </p>
   */
  private OsTools() {
    // Private constructor to prevent instantiation
  }

  private static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name").toLowerCase();

  private static final List<String> SECURE_RANDOM_ALGORITHMS_LINUX_OSX_SOLARIS = Arrays.asList("NativePRNGBlocking",
      "NativePRNGNonBlocking", "NativePRNG", "SHA1PRNG");
  private static final List<String> SECURE_RANDOM_ALGORITHMS_WINDOWS = Arrays.asList("SHA1PRNG", "Windows-PRNG");

  /**
   * Returns the list of preferred secure random algorithm names for the current operating system.
   * <p>
   * This method detects the current operating system and returns an ordered list of
   * secure random algorithms that are most suitable for that platform. The algorithms
   * are listed in order of preference, with the most preferred algorithm first.
   * </p>
   * <p>
   * For Windows systems, the method returns algorithms optimized for Windows.
   * For all other systems (Linux, macOS, Solaris), it returns algorithms that
   * work well on Unix-like operating systems.
   * </p>
   *
   * @return a List of secure random algorithm names in order of preference
   */
  static List<String> secureRandomAlgorithmNames() {
    return OPERATING_SYSTEM_NAME.contains("win") ?
        SECURE_RANDOM_ALGORITHMS_WINDOWS :
        SECURE_RANDOM_ALGORITHMS_LINUX_OSX_SOLARIS;
  }
}
