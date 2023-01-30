package com.redis.om.spring.id;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.keyvalue.core.IdentifierGenerator;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;

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
      return (T) Integer.valueOf(getSecureRandom().nextInt());
    } else if (ClassUtils.isAssignable(Long.class, type)) {
      return (T) Long.valueOf(getSecureRandom().nextLong());
    }

    throw new InvalidDataAccessApiUsageException(
        String.format("Identifier cannot be generated for %s. Supported types are: ULID, String, Integer, and Long.",
            identifierType.getType().getName()));
  }

  private SecureRandom getSecureRandom() {

    SecureRandom sr = this.secureRandom.get();
    if (sr != null) {
      return sr;
    }

    for (String algorithm : OsTools.secureRandomAlgorithmNames()) {
      try {
        sr = SecureRandom.getInstance(algorithm);
      } catch (NoSuchAlgorithmException e) {
        // ignore and try next.
      }
    }

    if (sr == null) {
      throw new InvalidDataAccessApiUsageException(
          String.format("Could not create SecureRandom instance for one of the algorithms '%s'.",
              StringUtils.collectionToCommaDelimitedString(OsTools.secureRandomAlgorithmNames())));
    }

    this.secureRandom.compareAndSet(null, sr);

    return sr;
  }

  private static class OsTools {

    private static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name").toLowerCase();

    private static final List<String> SECURE_RANDOM_ALGORITHMS_LINUX_OSX_SOLARIS = Arrays.asList("NativePRNGBlocking",
        "NativePRNGNonBlocking", "NativePRNG", "SHA1PRNG");
    private static final List<String> SECURE_RANDOM_ALGORITHMS_WINDOWS = Arrays.asList("SHA1PRNG", "Windows-PRNG");

    static List<String> secureRandomAlgorithmNames() {
      return OPERATING_SYSTEM_NAME.contains("win") ? SECURE_RANDOM_ALGORITHMS_WINDOWS
          : SECURE_RANDOM_ALGORITHMS_LINUX_OSX_SOLARIS;
    }
  }
}
