package com.redis.om.spring.id;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.keyvalue.core.IdentifierGenerator;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public enum ULIDIdentifierGenerator implements IdentifierGenerator {

  INSTANCE;

  private final AtomicReference<SecureRandom> secureRandom = new AtomicReference<>(null);

  @SuppressWarnings("unchecked")
  @Override
  public <T> T generateIdentifierOfType(TypeInformation<T> identifierType) {
    Class<?> type = identifierType.getType();

    if (ClassUtils.isAssignable(Ulid.class, type)) {
      return (T) UlidCreator.getMonotonicUlid();
    } else if (ClassUtils.isAssignable(String.class, type)) {
      return (T) UlidCreator.getMonotonicUlid().toString();
    } else if (ClassUtils.isAssignable(Integer.class, type)) {
      return (T) Integer.valueOf(SecureRandomUtils.getSecureRandom().nextInt());
    } else if (ClassUtils.isAssignable(Long.class, type)) {
      return (T) Long.valueOf(SecureRandomUtils.getSecureRandom().nextLong());
    }

    throw new InvalidDataAccessApiUsageException(
            String.format("Identifier cannot be generated for %s. Supported types are: ULID, String, Integer, and Long.",
                    identifierType.getType().getName()));
  }
}

class SecureRandomUtils {

  private static final AtomicReference<SecureRandom> secureRandom = new AtomicReference<>(null);

  private SecureRandomUtils() {
    // Private constructor to prevent instantiation.
  }

  public static SecureRandom getSecureRandom() {
    return secureRandom.updateAndGet(sr -> sr != null ? sr : createSecureRandom());
  }

  private static SecureRandom createSecureRandom() {
    for (String algorithm : OsTools.secureRandomAlgorithmNames()) {
      try {
        return SecureRandom.getInstance(algorithm);
      } catch (NoSuchAlgorithmException e) {
        // ignore and try the next algorithm.
      }
    }

    throw new InvalidDataAccessApiUsageException("Could not create SecureRandom instance for any of the specified algorithms: "
            + StringUtils.collectionToCommaDelimitedString(OsTools.secureRandomAlgorithmNames()));
  }
}

class OsTools {

  private static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name").toLowerCase();

  private static final List<String> SECURE_RANDOM_ALGORITHMS_LINUX_OSX_SOLARIS = Arrays.asList("NativePRNGBlocking",
          "NativePRNGNonBlocking", "NativePRNG", "SHA1PRNG");
  private static final List<String> SECURE_RANDOM_ALGORITHMS_WINDOWS = Arrays.asList("SHA1PRNG", "Windows-PRNG");

  static List<String> secureRandomAlgorithmNames() {
    return OPERATING_SYSTEM_NAME.contains("win") ? SECURE_RANDOM_ALGORITHMS_WINDOWS
            : SECURE_RANDOM_ALGORITHMS_LINUX_OSX_SOLARIS;
  }
}
