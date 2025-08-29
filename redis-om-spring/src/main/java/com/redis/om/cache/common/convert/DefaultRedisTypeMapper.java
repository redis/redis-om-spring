package com.redis.om.cache.common.convert;

import java.util.Collections;
import java.util.List;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.DefaultTypeMapper;
import org.springframework.data.convert.SimpleTypeInformationMapper;
import org.springframework.data.convert.TypeAliasAccessor;
import org.springframework.data.convert.TypeInformationMapper;
import org.springframework.data.mapping.Alias;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.redis.om.cache.common.convert.Bucket.BucketPropertyPath;

/**
 * Default implementation of {@link RedisTypeMapper} allowing configuration of the key to lookup and store type
 * information via {@link BucketPropertyPath} in buckets. The key defaults to {@link #DEFAULT_TYPE_KEY}. Actual
 * type-to-{@code byte[]} conversion and back is done in {@link BucketTypeAliasAccessor}.
 *
 */
public class DefaultRedisTypeMapper extends DefaultTypeMapper<BucketPropertyPath> implements RedisTypeMapper {

  /**
   * Default type key used for storing type information.
   */
  public static final String DEFAULT_TYPE_KEY = "_class";

  private final @Nullable String typeKey;

  /**
   * Create a new {@link DefaultRedisTypeMapper} using {@link #DEFAULT_TYPE_KEY} to exchange type hints.
   */
  public DefaultRedisTypeMapper() {
    this(DEFAULT_TYPE_KEY);
  }

  /**
   * Create a new {@link DefaultRedisTypeMapper} given {@code typeKey} to exchange type hints. Does not consider type
   * hints if {@code typeKey} is {@literal null}.
   *
   * @param typeKey the type key can be {@literal null} to skip type hinting.
   */
  public DefaultRedisTypeMapper(@Nullable String typeKey) {
    this(typeKey, Collections.singletonList(new SimpleTypeInformationMapper()));
  }

  /**
   * Create a new {@link DefaultRedisTypeMapper} given {@code typeKey} to exchange type hints and
   * {@link MappingContext}. Does not consider type hints if {@code typeKey} is {@literal null}. {@link MappingContext}
   * is used to obtain entity-based aliases
   *
   * @param typeKey        the type key can be {@literal null} to skip type hinting.
   * @param mappingContext must not be {@literal null}.
   * @see org.springframework.data.annotation.TypeAlias
   */
  public DefaultRedisTypeMapper(@Nullable String typeKey,
      MappingContext<? extends PersistentEntity<?, ?>, ?> mappingContext) {
    this(typeKey, new BucketTypeAliasAccessor(typeKey, getConversionService()), mappingContext, Collections
        .singletonList(new SimpleTypeInformationMapper()));
  }

  /**
   * Create a new {@link DefaultRedisTypeMapper} given {@code typeKey} to exchange type hints and {@link List} of
   * {@link TypeInformationMapper}. Does not consider type hints if {@code typeKey} is {@literal null}.
   * {@link MappingContext} is used to obtain entity-based aliases
   *
   * @param typeKey the type key can be {@literal null} to skip type hinting.
   * @param mappers must not be {@literal null}.
   */
  public DefaultRedisTypeMapper(@Nullable String typeKey, List<? extends TypeInformationMapper> mappers) {
    this(typeKey, new BucketTypeAliasAccessor(typeKey, getConversionService()), null, mappers);
  }

  private DefaultRedisTypeMapper(@Nullable String typeKey, TypeAliasAccessor<BucketPropertyPath> accessor,
      @Nullable MappingContext<? extends PersistentEntity<?, ?>, ?> mappingContext,
      List<? extends TypeInformationMapper> mappers) {

    super(accessor, mappingContext, mappers);

    this.typeKey = typeKey;
  }

  private static GenericConversionService getConversionService() {

    GenericConversionService conversionService = new GenericConversionService();
    new RedisCustomConversions().registerConvertersIn(conversionService);

    return conversionService;
  }

  public boolean isTypeKey(@Nullable String key) {
    return key != null && typeKey != null && key.endsWith(typeKey);
  }

  /**
   * {@link TypeAliasAccessor} to store aliases in a {@link Bucket}.
   *
   */
  static final class BucketTypeAliasAccessor implements TypeAliasAccessor<BucketPropertyPath> {

    private final @Nullable String typeKey;

    private final ConversionService conversionService;

    BucketTypeAliasAccessor(@Nullable String typeKey, ConversionService conversionService) {

      Assert.notNull(conversionService, "ConversionService must not be null");

      this.typeKey = typeKey;
      this.conversionService = conversionService;
    }

    public Alias readAliasFrom(BucketPropertyPath source) {

      if (typeKey == null || source instanceof List) {
        return Alias.NONE;
      }

      byte[] bytes = source.get(typeKey);

      if (bytes != null) {
        return Alias.ofNullable(conversionService.convert(bytes, String.class));
      }

      return Alias.NONE;
    }

    public void writeTypeTo(BucketPropertyPath sink, Object alias) {

      if (typeKey != null) {

        if (alias instanceof byte[] aliasBytes) {
          sink.put(typeKey, aliasBytes);
        } else {
          sink.put(typeKey, conversionService.convert(alias, byte[].class));
        }
      }
    }
  }
}
