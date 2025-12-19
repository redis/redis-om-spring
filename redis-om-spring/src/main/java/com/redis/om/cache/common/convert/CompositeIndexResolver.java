package com.redis.om.cache.common.convert;

import java.util.*;

import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Composite {@link IndexResolver} implementation that iterates over a given collection of delegate
 * {@link IndexResolver} instances. <br />
 * <br />
 * <strong>NOTE</strong> {@link IndexedData} created by an {@link IndexResolver} can be overwritten by subsequent
 * {@link IndexResolver}.
 *
 */
public class CompositeIndexResolver implements IndexResolver {

  private final List<IndexResolver> resolvers;

  /**
   * Create new {@link CompositeIndexResolver}.
   *
   * @param resolvers must not be {@literal null}.
   */
  public CompositeIndexResolver(Collection<IndexResolver> resolvers) {

    Assert.notNull(resolvers, "Resolvers must not be null");
    if (CollectionUtils.contains(resolvers.iterator(), null)) {
      throw new IllegalArgumentException("Resolvers must no contain null values");
    }
    this.resolvers = new ArrayList<>(resolvers);
  }

  @Override
  public Set<IndexedData> resolveIndexesFor(TypeInformation<?> typeInformation, @Nullable Object value) {

    if (resolvers.isEmpty()) {
      return Collections.emptySet();
    }

    Set<IndexedData> data = new LinkedHashSet<>();
    for (IndexResolver resolver : resolvers) {
      data.addAll(resolver.resolveIndexesFor(typeInformation, value));
    }
    return data;
  }

  @Override
  public Set<IndexedData> resolveIndexesFor(String keyspace, String path, TypeInformation<?> typeInformation,
      @Nullable Object value) {

    Set<IndexedData> data = new LinkedHashSet<>();
    for (IndexResolver resolver : resolvers) {
      data.addAll(resolver.resolveIndexesFor(keyspace, path, typeInformation, value));
    }
    return data;
  }

}
