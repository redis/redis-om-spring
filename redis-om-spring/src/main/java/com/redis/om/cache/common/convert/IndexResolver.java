package com.redis.om.cache.common.convert;

import java.util.Set;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;

/**
 * {@link IndexResolver} extracts secondary index structures to be applied on a given path, {@link PersistentProperty}
 * and value.
 *
 */
public interface IndexResolver {

  /**
   * Resolves all indexes for given type information / value combination.
   *
   * @param typeInformation must not be {@literal null}.
   * @param value           the actual value. Can be {@literal null}.
   * @return never {@literal null}.
   */
  Set<IndexedData> resolveIndexesFor(TypeInformation<?> typeInformation, @Nullable Object value);

  /**
   * Resolves all indexes for given type information / value combination.
   *
   * @param keyspace        must not be {@literal null}.
   * @param path            must not be {@literal null}.
   * @param typeInformation must not be {@literal null}.
   * @param value           the actual value. Can be {@literal null}.
   * @return never {@literal null}.
   */
  Set<IndexedData> resolveIndexesFor(String keyspace, String path, TypeInformation<?> typeInformation,
      @Nullable Object value);

}
