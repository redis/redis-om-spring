package com.redis.om.cache.common.mapping;

import java.util.Collections;
import java.util.Map;

import org.springframework.data.convert.CustomConversions;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.redis.om.cache.common.RedisHashMapper;
import com.redis.om.cache.common.convert.*;

/**
 * {@link RedisHashMapper} based on {@link MappingRedisConverter}. Supports
 * nested properties and simple types like {@link String}.
 *
 * <pre>
 * <code>
 * class Person {
 *
 * String firstname;
 * String lastname;
 *
 * List&lt;String&gt; nicknames;
 * List&lt;Person&gt; coworkers;
 *
 * Address address;
 * }
 * </code>
 * </pre>
 *
 * The above is represented as:
 *
 * <pre>
 * <code>
 * _class=org.example.Person
 * firstname=rand
 * lastname=al'thor
 * coworkers.[0].firstname=mat
 * coworkers.[0].nicknames.[0]=prince of the ravens
 * coworkers.[1].firstname=perrin
 * coworkers.[1].address.city=two rivers
 * </code>
 * </pre>
 *
 */
public class ObjectHashMapper implements RedisHashMapper {

  @Nullable
  private volatile static ObjectHashMapper sharedInstance;

  private final RedisConverter converter;

  /**
   * Creates new {@link ObjectHashMapper}.
   */
  public ObjectHashMapper() {
    this(new RedisCustomConversions());
  }

  /**
   * Creates a new {@link ObjectHashMapper} using the given {@link RedisConverter}
   * for conversion.
   *
   * @param converter must not be {@literal null}.
   * @throws IllegalArgumentException if the given {@literal converter} is
   *                                  {@literal null}.
   * @since 2.4
   */
  public ObjectHashMapper(RedisConverter converter) {

    Assert.notNull(converter, "Converter must not be null");
    this.converter = converter;
  }

  /**
   * Creates new {@link ObjectHashMapper}.
   *
   * @param customConversions can be {@literal null}.
   * @since 2.0
   */
  public ObjectHashMapper(@Nullable CustomConversions customConversions) {

    MappingRedisConverter mappingConverter = new MappingRedisConverter(new RedisMappingContext(),
        new NoOpReferenceResolver());
    mappingConverter.setCustomConversions(customConversions == null ? new RedisCustomConversions() : customConversions);
    mappingConverter.afterPropertiesSet();

    converter = mappingConverter;
  }

  /**
   * Return a shared default {@link ObjectHashMapper} instance, lazily building it
   * once needed.
   * <p>
   * <b>NOTE:</b> We highly recommend constructing individual
   * {@link ObjectHashMapper} instances for customization purposes. This accessor
   * is only meant as a fallback for code paths which need simple type coercion
   * but cannot access a longer-lived {@link ObjectHashMapper} instance any other
   * way.
   *
   * @return the shared {@link ObjectHashMapper} instance (never {@literal null}).
   * @since 2.4
   */
  public static ObjectHashMapper getSharedInstance() {

    ObjectHashMapper cs = sharedInstance;
    if (cs == null) {
      synchronized (ObjectHashMapper.class) {
        cs = sharedInstance;
        if (cs == null) {
          cs = new ObjectHashMapper();
          sharedInstance = cs;
        }
      }
    }
    return cs;
  }

  @Override
  public Map<byte[], byte[]> toHash(Object source) {
    if (source == null) {
      return Collections.emptyMap();
    }
    RedisData sink = new RedisData();
    converter.write(source, sink);
    return sink.getBucket().rawMap();
  }

  @Override
  public Object fromHash(Map<byte[], byte[]> hash) {
    if (hash == null || hash.isEmpty()) {
      return null;
    }
    return converter.read(Object.class, new RedisData(hash));
  }

  /**
   * Convert a {@code hash} (map) to an object and return the casted result.
   *
   * @param hash the hash map containing the object data to convert
   * @param type the target class type to convert the hash to
   * @param <T>  the generic type of the returned object
   * @return the converted object of type T
   */
  public <T> T fromHash(Map<byte[], byte[]> hash, Class<T> type) {
    return type.cast(fromHash(hash));
  }

  /**
   * {@link ReferenceResolver} implementation always returning an empty
   * {@link Map}.
   *
   */
  private static class NoOpReferenceResolver implements ReferenceResolver {

    private static final Map<byte[], byte[]> NO_REFERENCE = Collections.emptyMap();

    @Override
    public Map<byte[], byte[]> resolveReference(Object id, String keyspace) {
      return NO_REFERENCE;
    }
  }

}
