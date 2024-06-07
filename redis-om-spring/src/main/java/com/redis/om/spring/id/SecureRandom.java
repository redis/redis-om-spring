package com.redis.om.spring.id;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.util.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;

public class SecureRandom {
  private static final AtomicReference<java.security.SecureRandom> secureRandom = new AtomicReference<>(null);

  public static java.security.SecureRandom getSecureRandom() {
    return secureRandom.updateAndGet(sr -> sr != null ? sr : createSecureRandom());
  }

  private static java.security.SecureRandom createSecureRandom() {
    for (String algorithm : OsTools.secureRandomAlgorithmNames()) {
      try {
        return java.security.SecureRandom.getInstance(algorithm);
      } catch (NoSuchAlgorithmException e) {
        // ignore and try the next algorithm.
      }
    }

    throw new InvalidDataAccessApiUsageException(
        "Could not create SecureRandom instance for any of the specified algorithms: " + StringUtils.collectionToCommaDelimitedString(
            OsTools.secureRandomAlgorithmNames()));
  }
}
