package com.redis.om.cache.common.mapping;

import java.io.IOException;
import java.io.Serial;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.cache.support.NullValue;
import org.springframework.core.KotlinDetector;
import org.springframework.data.util.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.redis.om.cache.common.RedisStringMapper;
import com.redis.om.cache.common.SerializationException;

/**
 * Implementation of {@link RedisStringMapper} that uses Jackson's {@link ObjectMapper} to convert objects to and from
 * JSON.
 * This class provides various constructors to configure the Jackson mapper with different type handling options.
 * It supports default typing, custom type hint property names, and custom object readers and writers.
 */
public class GenericJackson2JsonMapper implements RedisStringMapper {

  private final JacksonObjectReader reader;

  private final JacksonObjectWriter writer;

  private final Lazy<Boolean> defaultTypingEnabled;

  private final ObjectMapper mapper;

  private final TypeResolver typeResolver;

  /**
   * Creates {@link GenericJackson2JsonMapper} initialized with an
   * {@link ObjectMapper} configured for default typing.
   */
  public GenericJackson2JsonMapper() {
    this((String) null);
  }

  /**
   * Creates {@link GenericJackson2JsonMapper} initialized with an
   * {@link ObjectMapper} configured for default typing using the given
   * {@link String name}.
   * <p>
   * In case {@link String name} is {@literal empty} or {@literal null}, then
   * {@link JsonTypeInfo.Id#CLASS} will be used.
   *
   * @param typeHintPropertyName {@link String name} of the JSON property holding
   *                             type information; can be {@literal null}.
   * @see ObjectMapper#activateDefaultTypingAsProperty(PolymorphicTypeValidator,
   *      DefaultTyping, String)
   * @see ObjectMapper#activateDefaultTyping(PolymorphicTypeValidator,
   *      DefaultTyping, As)
   */
  public GenericJackson2JsonMapper(@Nullable String typeHintPropertyName) {
    this(typeHintPropertyName, JacksonObjectReader.create(), JacksonObjectWriter.create());
  }

  /**
   * Creates {@link GenericJackson2JsonMapper} initialized with an
   * {@link ObjectMapper} configured for default typing using the given
   * {@link String name} along with the given, required
   * {@link JacksonObjectReader} and {@link JacksonObjectWriter} used to
   * read/write {@link Object Objects} de/serialized as JSON.
   * <p>
   * In case {@link String name} is {@literal empty} or {@literal null}, then
   * {@link JsonTypeInfo.Id#CLASS} will be used.
   *
   * @param typeHintPropertyName {@link String name} of the JSON property holding
   *                             type information; can be {@literal null}.
   * @param reader               {@link JacksonObjectReader} function to read
   *                             objects using {@link ObjectMapper}.
   * @param writer               {@link JacksonObjectWriter} function to write
   *                             objects using {@link ObjectMapper}.
   * @see ObjectMapper#activateDefaultTypingAsProperty(PolymorphicTypeValidator,
   *      DefaultTyping, String)
   * @see ObjectMapper#activateDefaultTyping(PolymorphicTypeValidator,
   *      DefaultTyping, As)
   * @since 3.0
   */
  public GenericJackson2JsonMapper(@Nullable String typeHintPropertyName, JacksonObjectReader reader,
      JacksonObjectWriter writer) {

    this(new ObjectMapper(), reader, writer, typeHintPropertyName);

    registerNullValueSerializer(this.mapper, typeHintPropertyName);

    this.mapper.setDefaultTyping(createDefaultTypeResolverBuilder(getObjectMapper(), typeHintPropertyName));
  }

  /**
   * Setting a custom-configured {@link ObjectMapper} is one way to take further
   * control of the JSON serialization process. For example, an extended
   * {@link SerializerFactory} can be configured that provides custom serializers
   * for specific types.
   *
   * @param mapper must not be {@literal null}.
   */
  public GenericJackson2JsonMapper(ObjectMapper mapper) {
    this(mapper, JacksonObjectReader.create(), JacksonObjectWriter.create());
  }

  /**
   * Setting a custom-configured {@link ObjectMapper} is one way to take further
   * control of the JSON serialization process. For example, an extended
   * {@link SerializerFactory} can be configured that provides custom serializers
   * for specific types.
   *
   * @param mapper must not be {@literal null}.
   * @param reader the {@link JacksonObjectReader} function to read objects using
   *               {@link ObjectMapper}.
   * @param writer the {@link JacksonObjectWriter} function to write objects using
   *               {@link ObjectMapper}.
   * @since 3.0
   */
  public GenericJackson2JsonMapper(ObjectMapper mapper, JacksonObjectReader reader, JacksonObjectWriter writer) {

    this(mapper, reader, writer, null);
  }

  private GenericJackson2JsonMapper(ObjectMapper mapper, JacksonObjectReader reader, JacksonObjectWriter writer,
      @Nullable String typeHintPropertyName) {

    Assert.notNull(mapper, "ObjectMapper must not be null");
    Assert.notNull(reader, "Reader must not be null");
    Assert.notNull(writer, "Writer must not be null");

    this.mapper = mapper;
    this.reader = reader;
    this.writer = writer;

    this.defaultTypingEnabled = Lazy.of(() -> mapper.getSerializationConfig().getDefaultTyper(null) != null);

    this.typeResolver = newTypeResolver(mapper, typeHintPropertyName, this.defaultTypingEnabled);
  }

  private static TypeResolver newTypeResolver(ObjectMapper mapper, @Nullable String typeHintPropertyName,
      Lazy<Boolean> defaultTypingEnabled) {

    Lazy<TypeFactory> lazyTypeFactory = Lazy.of(mapper::getTypeFactory);

    Lazy<String> lazyTypeHintPropertyName = typeHintPropertyName != null ?
        Lazy.of(typeHintPropertyName) :
        newLazyTypeHintPropertyName(mapper, defaultTypingEnabled);

    return new TypeResolver(lazyTypeFactory, lazyTypeHintPropertyName);
  }

  private static Lazy<String> newLazyTypeHintPropertyName(ObjectMapper mapper, Lazy<Boolean> defaultTypingEnabled) {

    Lazy<String> configuredTypeDeserializationPropertyName = getConfiguredTypeDeserializationPropertyName(mapper);

    Lazy<String> resolvedLazyTypeHintPropertyName = Lazy.of(() -> defaultTypingEnabled.get() ?
        null :
        configuredTypeDeserializationPropertyName.get());

    return resolvedLazyTypeHintPropertyName.or("@class");
  }

  private static Lazy<String> getConfiguredTypeDeserializationPropertyName(ObjectMapper mapper) {

    return Lazy.of(() -> {

      DeserializationConfig deserializationConfig = mapper.getDeserializationConfig();

      JavaType objectType = mapper.getTypeFactory().constructType(Object.class);

      TypeDeserializer typeDeserializer = deserializationConfig.getDefaultTyper(null).buildTypeDeserializer(
          deserializationConfig, objectType, Collections.emptyList());

      return typeDeserializer.getPropertyName();
    });
  }

  private static StdTypeResolverBuilder createDefaultTypeResolverBuilder(ObjectMapper objectMapper,
      @Nullable String typeHintPropertyName) {

    StdTypeResolverBuilder typer = TypeResolverBuilder.forEverything(objectMapper).init(JsonTypeInfo.Id.CLASS, null)
        .inclusion(As.PROPERTY);

    if (StringUtils.hasText(typeHintPropertyName)) {
      typer = typer.typeProperty(typeHintPropertyName);
    }
    return typer;
  }

  /**
   * Factory method returning a {@literal Builder} used to construct and configure
   * a {@link GenericJackson2JsonMapper}.
   *
   * @return new
   *         {@link GenericJackson2JsonRedisSerializerBuilder}.
   * @since 3.3.1
   */
  public static GenericJackson2JsonRedisSerializerBuilder builder() {
    return new GenericJackson2JsonRedisSerializerBuilder();
  }

  /**
   * Register {@link NullValueSerializer} in the given {@link ObjectMapper} with
   * an optional {@code typeHintPropertyName}. This method should be called by
   * code that customizes {@link GenericJackson2JsonMapper} by providing an
   * external {@link ObjectMapper}.
   *
   * @param objectMapper         the object mapper to customize.
   * @param typeHintPropertyName name of the type property. Defaults to
   *                             {@code @class} if {@literal null}/empty.
   * @since 2.2
   */
  public static void registerNullValueSerializer(ObjectMapper objectMapper, @Nullable String typeHintPropertyName) {

    // Simply setting {@code
    // mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)} does not help here
    // since we need the type hint embedded for deserialization using the default
    // typing feature.
    objectMapper.registerModule(new SimpleModule().addSerializer(new NullValueSerializer(typeHintPropertyName)));
  }

  /**
   * Gets the configured {@link ObjectMapper} used internally by this
   * {@link GenericJackson2JsonMapper} to de/serialize {@link Object objects} as
   * {@literal JSON}.
   *
   * @return the configured {@link ObjectMapper}.
   */
  protected ObjectMapper getObjectMapper() {
    return this.mapper;
  }

  @Override
  public byte[] toString(@Nullable Object value) throws SerializationException {

    if (value == null) {
      return SerializationUtils.EMPTY_ARRAY;
    }

    try {
      return writer.write(mapper, value);
    } catch (IOException ex) {
      String message = String.format("Could not write JSON: %s", ex.getMessage());
      throw new SerializationException(message, ex);
    }
  }

  @Override
  public Object fromString(@Nullable byte[] source) throws SerializationException {
    return deserialize(source, Object.class);
  }

  /**
   * Deserialized the array of bytes containing {@literal JSON} as an
   * {@link Object} of the given, required {@link Class type}.
   *
   * @param <T>    the type of the object to deserialize to
   * @param source array of bytes containing the {@literal JSON} to deserialize;
   *               can be {@literal null}.
   * @param type   {@link Class type} of {@link Object} from which the
   *               {@literal JSON} will be deserialized; must not be
   *               {@literal null}.
   * @return {@literal null} for an empty source, or an {@link Object} of the
   *         given {@link Class type} deserialized from the array of bytes
   *         containing {@literal JSON}.
   * @throws IllegalArgumentException if the given {@link Class type} is
   *                                  {@literal null}.
   * @throws SerializationException   if the array of bytes cannot be deserialized
   *                                  as an instance of the given {@link Class
   *                                  type}
   */
  @Nullable
  @SuppressWarnings(
    "unchecked"
  )
  public <T> T deserialize(@Nullable byte[] source, Class<T> type) throws SerializationException {

    Assert.notNull(type,
        "Deserialization type must not be null;" + " Please provide Object.class to make use of Jackson2 default typing.");

    if (SerializationUtils.isEmpty(source)) {
      return null;
    }

    try {
      return (T) reader.read(mapper, source, resolveType(source, type));
    } catch (Exception ex) {
      String message = String.format("Could not read JSON:%s ", ex.getMessage());
      throw new SerializationException(message, ex);
    }
  }

  /**
   * Builder method used to configure and customize the internal Jackson
   * {@link ObjectMapper} created by this {@link GenericJackson2JsonMapper} and
   * used to de/serialize {@link Object objects} as {@literal JSON}.
   *
   * @param objectMapperConfigurer {@link Consumer} used to configure and
   *                               customize the internal {@link ObjectMapper};
   *                               must not be {@literal null}.
   * @return this {@link GenericJackson2JsonMapper}.
   * @throws IllegalArgumentException if the {@link Consumer} used to configure
   *                                  and customize the internal
   *                                  {@link ObjectMapper} is {@literal null}.
   * @since 3.1.5
   */
  public GenericJackson2JsonMapper configure(Consumer<ObjectMapper> objectMapperConfigurer) {

    Assert.notNull(objectMapperConfigurer, "Consumer used to configure and customize ObjectMapper must not be null");

    objectMapperConfigurer.accept(getObjectMapper());

    return this;
  }

  /**
   * Resolves the JavaType for deserialization based on the source bytes and target type.
   * If the target type is Object.class and default typing is enabled, attempts to resolve
   * the type from the JSON content using the type hint.
   *
   * @param source the JSON source bytes
   * @param type   the target class type
   * @return the resolved JavaType
   * @throws IOException if an error occurs during type resolution
   */
  protected JavaType resolveType(byte[] source, Class<?> type) throws IOException {

    if (!type.equals(Object.class) || !defaultTypingEnabled.get()) {
      return typeResolver.constructType(type);
    }

    return typeResolver.resolveType(source, type);
  }

  /**
   * @since 3.0
   */
  static class TypeResolver {

    // need a separate instance to bypass class hint checks
    private final ObjectMapper mapper = new ObjectMapper();

    private final Supplier<TypeFactory> typeFactory;
    private final Supplier<String> hintName;

    TypeResolver(Supplier<TypeFactory> typeFactory, Supplier<String> hintName) {

      this.typeFactory = typeFactory;
      this.hintName = hintName;
    }

    protected JavaType constructType(Class<?> type) {
      return typeFactory.get().constructType(type);
    }

    /**
     * Resolves the JavaType from the JSON source bytes by extracting the type hint.
     * If a type hint is found in the JSON, constructs the JavaType from the canonical name.
     * Otherwise, falls back to constructing the type from the provided class.
     *
     * @param source the JSON source bytes
     * @param type   the fallback class type
     * @return the resolved JavaType
     * @throws IOException if an error occurs during JSON parsing
     */
    protected JavaType resolveType(byte[] source, Class<?> type) throws IOException {

      JsonNode root = mapper.readTree(source);
      JsonNode jsonNode = root.get(hintName.get());

      if (jsonNode instanceof TextNode && jsonNode.asText() != null) {
        return typeFactory.get().constructFromCanonical(jsonNode.asText());
      }

      return constructType(type);
    }
  }

  private static class NullValueSerializer extends StdSerializer<NullValue> {

    @Serial
    private static final long serialVersionUID = 1999052150548658808L;

    private final String classIdentifier;

    /**
     * @param classIdentifier can be {@literal null} and will be defaulted to
     *                        {@code @class}.
     */
    NullValueSerializer(@Nullable String classIdentifier) {

      super(NullValue.class);
      this.classIdentifier = StringUtils.hasText(classIdentifier) ? classIdentifier : "@class";
    }

    @Override
    public void serialize(NullValue value, JsonGenerator jsonGenerator, SerializerProvider provider)
        throws IOException {

      jsonGenerator.writeStartObject();
      jsonGenerator.writeStringField(classIdentifier, NullValue.class.getName());
      jsonGenerator.writeEndObject();
    }

    @Override
    public void serializeWithType(NullValue value, JsonGenerator jsonGenerator, SerializerProvider serializers,
        TypeSerializer typeSerializer) throws IOException {

      serialize(value, jsonGenerator, serializers);
    }
  }

  /**
   * Builder class for creating {@link GenericJackson2JsonMapper} instances with various configuration options.
   * Provides methods for configuring default typing, type hint property name, ObjectMapper, reader, writer,
   * and null value serializer.
   */
  public static class GenericJackson2JsonRedisSerializerBuilder {

    private @Nullable String typeHintPropertyName;

    private JacksonObjectReader reader = JacksonObjectReader.create();

    private JacksonObjectWriter writer = JacksonObjectWriter.create();

    private @Nullable ObjectMapper objectMapper;

    private @Nullable Boolean defaultTyping;

    private boolean registerNullValueSerializer = true;

    private @Nullable StdSerializer<NullValue> nullValueSerializer;

    private GenericJackson2JsonRedisSerializerBuilder() {
    }

    /**
     * Enable or disable default typing. Enabling default typing will override
     * {@link ObjectMapper#setDefaultTyping(com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder)}
     * for a given {@link ObjectMapper}. Default typing is enabled by default if no
     * {@link ObjectMapper} is provided.
     *
     * @param defaultTyping whether to enable/disable default typing. Enabled by
     *                      default if the {@link ObjectMapper} is not provided.
     * @return this
     *         {@link GenericJackson2JsonRedisSerializerBuilder}.
     */
    public GenericJackson2JsonRedisSerializerBuilder defaultTyping(boolean defaultTyping) {
      this.defaultTyping = defaultTyping;
      return this;
    }

    /**
     * Configure a property name to that represents the type hint.
     *
     * @param typeHintPropertyName {@link String name} of the JSON property holding
     *                             type information.
     * @return this
     *         {@link GenericJackson2JsonRedisSerializerBuilder}.
     */
    public GenericJackson2JsonRedisSerializerBuilder typeHintPropertyName(String typeHintPropertyName) {

      Assert.hasText(typeHintPropertyName, "Type hint property name must bot be null or empty");

      this.typeHintPropertyName = typeHintPropertyName;
      return this;
    }

    /**
     * Configure a provided {@link ObjectMapper}. Note that the provided
     * {@link ObjectMapper} can be reconfigured with a {@link #nullValueSerializer}
     * or default typing depending on the builder configuration.
     *
     * @param objectMapper must not be {@literal null}.
     * @return this
     *         {@link GenericJackson2JsonRedisSerializerBuilder}.
     */
    public GenericJackson2JsonRedisSerializerBuilder objectMapper(ObjectMapper objectMapper) {

      Assert.notNull(objectMapper, "ObjectMapper must not be null");

      this.objectMapper = objectMapper;
      return this;
    }

    /**
     * Configure {@link JacksonObjectReader}.
     *
     * @param reader must not be {@literal null}.
     * @return this
     *         {@link GenericJackson2JsonRedisSerializerBuilder}.
     */
    public GenericJackson2JsonRedisSerializerBuilder reader(JacksonObjectReader reader) {

      Assert.notNull(reader, "JacksonObjectReader must not be null");

      this.reader = reader;
      return this;
    }

    /**
     * Configure {@link JacksonObjectWriter}.
     *
     * @param writer must not be {@literal null}.
     * @return this
     *         {@link GenericJackson2JsonRedisSerializerBuilder}.
     */
    public GenericJackson2JsonRedisSerializerBuilder writer(JacksonObjectWriter writer) {

      Assert.notNull(writer, "JacksonObjectWriter must not be null");

      this.writer = writer;
      return this;
    }

    /**
     * Register a {@link StdSerializer serializer} for {@link NullValue}.
     *
     * @param nullValueSerializer the {@link StdSerializer} to use for
     *                            {@link NullValue} serialization, must not be
     *                            {@literal null}.
     * @return this
     *         {@link GenericJackson2JsonRedisSerializerBuilder}.
     */
    public GenericJackson2JsonRedisSerializerBuilder nullValueSerializer(StdSerializer<NullValue> nullValueSerializer) {

      Assert.notNull(nullValueSerializer, "Null value serializer must not be null");

      this.nullValueSerializer = nullValueSerializer;
      return this;
    }

    /**
     * Configure whether to register a {@link StdSerializer serializer} for
     * {@link NullValue} serialization. The default serializer considers
     * {@link #typeHintPropertyName(String)}.
     *
     * @param registerNullValueSerializer {@code true} to register the default
     *                                    serializer; {@code false} otherwise.
     * @return this
     *         {@link GenericJackson2JsonRedisSerializerBuilder}.
     */
    public GenericJackson2JsonRedisSerializerBuilder registerNullValueSerializer(boolean registerNullValueSerializer) {
      this.registerNullValueSerializer = registerNullValueSerializer;
      return this;
    }

    /**
     * Creates a new instance of {@link GenericJackson2JsonMapper} with
     * configuration options applied. Creates also a new {@link ObjectMapper} if
     * none was provided.
     *
     * @return a new instance of {@link GenericJackson2JsonMapper}.
     */
    public GenericJackson2JsonMapper build() {

      ObjectMapper objectMapper = this.objectMapper;
      boolean providedObjectMapper = objectMapper != null;

      if (objectMapper == null) {
        objectMapper = new ObjectMapper();
      }

      if (registerNullValueSerializer) {
        objectMapper.registerModule(new SimpleModule("GenericJackson2JsonRedisSerializerBuilder").addSerializer(
            this.nullValueSerializer != null ?
                this.nullValueSerializer :
                new NullValueSerializer(this.typeHintPropertyName)));
      }

      if ((!providedObjectMapper && (defaultTyping == null || defaultTyping)) || (defaultTyping != null && defaultTyping)) {
        objectMapper.setDefaultTyping(createDefaultTypeResolverBuilder(objectMapper, typeHintPropertyName));
      }

      return new GenericJackson2JsonMapper(objectMapper, this.reader, this.writer, this.typeHintPropertyName);
    }
  }

  @SuppressWarnings(
    "serial"
  )
  private static class TypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {

    @SuppressWarnings(
      "deprecation"
    )
    static TypeResolverBuilder forEverything(ObjectMapper mapper) {
      return new TypeResolverBuilder(DefaultTyping.EVERYTHING, mapper.getPolymorphicTypeValidator());
    }

    public TypeResolverBuilder(DefaultTyping typing, PolymorphicTypeValidator polymorphicTypeValidator) {
      super(typing, polymorphicTypeValidator);
    }

    @Override
    public ObjectMapper.DefaultTypeResolverBuilder withDefaultImpl(Class<?> defaultImpl) {
      return this;
    }

    /**
     * Method called to check if the default type handler should be used for given
     * type. Note: "natural types" (String, Boolean, Integer, Double) will never use
     * typing; that is both due to them being concrete and final, and since actual
     * serializers and deserializers will also ignore any attempts to enforce
     * typing.
     */
    public boolean useForType(JavaType javaType) {

      if (javaType.isJavaLangObject()) {
        return true;
      }

      javaType = resolveArrayOrWrapper(javaType);

      if (javaType.isEnumType() || ClassUtils.isPrimitiveOrWrapper(javaType.getRawClass())) {
        return false;
      }

      if (javaType.isFinal() && !KotlinDetector.isKotlinType(javaType.getRawClass()) && javaType.getRawClass()
          .getPackageName().startsWith("java")) {
        return false;
      }

      // [databind#88] Should not apply to JSON tree models:
      return !TreeNode.class.isAssignableFrom(javaType.getRawClass());
    }

    private JavaType resolveArrayOrWrapper(JavaType type) {

      while (type.isArrayType()) {
        type = type.getContentType();
        if (type.isReferenceType()) {
          type = resolveArrayOrWrapper(type);
        }
      }

      while (type.isReferenceType()) {
        type = type.getReferencedType();
        if (type.isArrayType()) {
          type = resolveArrayOrWrapper(type);
        }
      }

      return type;
    }
  }
}
