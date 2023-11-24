package com.redis.om.spring.id;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.keyvalue.core.IdentifierGenerator;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

interface IdentifierGenerationStrategy<T> {
  T generate();
}

class UlidIdentifierGenerationStrategy implements IdentifierGenerationStrategy<Ulid> {
  @Override
  public Ulid generate() {
    return UlidCreator.getMonotonicUlid();
  }
}

class StringIdentifierGenerationStrategy implements IdentifierGenerationStrategy<String> {
  @Override
  public String generate() {
    return UlidCreator.getMonotonicUlid().toString();
  }
}

class IntegerIdentifierGenerationStrategy implements IdentifierGenerationStrategy<Integer> {
  @Override
  public Integer generate() {
    return ULIDIdentifierGenerator.getSecureRandom().nextInt();
  }
}

class LongIdentifierGenerationStrategy implements IdentifierGenerationStrategy<Long> {
  @Override
  public Long generate() {
    return ULIDIdentifierGenerator.getSecureRandom().nextLong();
  }
}

public enum ULIDIdentifierGenerator implements IdentifierGenerator {
  INSTANCE;

  private final Map<Class<?>, IdentifierGenerationStrategy<?>> strategies = new HashMap<>();
  final AtomicReference<SecureRandom> secureRandom = new AtomicReference<>(null);

  private ULIDIdentifierGenerator() {
    strategies.put(Ulid.class, new UlidIdentifierGenerationStrategy());
    strategies.put(String.class, new StringIdentifierGenerationStrategy());
    strategies.put(Integer.class, new IntegerIdentifierGenerationStrategy());
    strategies.put(Long.class, new LongIdentifierGenerationStrategy());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T generateIdentifierOfType(TypeInformation<T> identifierType) {
    Class<?> type = identifierType.getType();

    if (strategies.containsKey(type)) {
      IdentifierGenerationStrategy<T> strategy = (IdentifierGenerationStrategy<T>) strategies.get(type);
      return strategy.generate();
    }

    throw new InvalidDataAccessApiUsageException(
            String.format("Identifier cannot be generated for %s. Supported types are: ULID, String, Integer, and Long.",
                    type.getName()));
  }

  public static SecureRandom getSecureRandom() {
    SecureRandom sr = INSTANCE.secureRandom.get();
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

    INSTANCE.secureRandom.compareAndSet(null, sr);

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



