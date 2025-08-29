package com.redis.om.cache.common.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.mapping.model.SimpleTypeHolder;

/**
 * Value object to capture custom conversion. That is essentially a {@link List} of converters and some additional logic
 * around them.
 *
 */
public class RedisCustomConversions extends org.springframework.data.convert.CustomConversions {

  private static final StoreConversions STORE_CONVERSIONS;
  private static final List<Object> STORE_CONVERTERS;

  static {

    List<Object> converters = new ArrayList<>(35);

    converters.addAll(BinaryConverters.getConvertersToRegister());
    converters.addAll(Jsr310Converters.getConvertersToRegister());

    STORE_CONVERTERS = Collections.unmodifiableList(converters);
    STORE_CONVERSIONS = StoreConversions.of(SimpleTypeHolder.DEFAULT, STORE_CONVERTERS);
  }

  /**
   * Creates an empty {@link RedisCustomConversions} object.
   */
  public RedisCustomConversions() {
    this(Collections.emptyList());
  }

  /**
   * Creates a new {@link RedisCustomConversions} instance registering the given converters.
   *
   * @param converters list of custom converters to register
   */
  public RedisCustomConversions(List<?> converters) {
    super(STORE_CONVERSIONS, converters);
  }
}
