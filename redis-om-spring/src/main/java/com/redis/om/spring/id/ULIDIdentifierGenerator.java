package com.redis.om.spring.id;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.keyvalue.core.IdentifierGenerator;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ClassUtils;

public enum ULIDIdentifierGenerator implements IdentifierGenerator {

  INSTANCE;

  @SuppressWarnings("unchecked")
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

    throw new InvalidDataAccessApiUsageException(
      String.format("Identifier cannot be generated for %s. Supported types are: ULID, String, Integer, and Long.",
        identifierType.getType().getName()));
  }
}




