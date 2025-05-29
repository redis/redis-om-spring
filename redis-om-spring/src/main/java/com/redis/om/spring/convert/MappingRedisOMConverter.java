package com.redis.om.spring.convert;

import static com.redis.om.spring.util.ObjectUtils.getCollectionElementClass;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mapping.*;
import org.springframework.data.mapping.model.EntityInstantiator;
import org.springframework.data.mapping.model.EntityInstantiators;
import org.springframework.data.mapping.model.PersistentEntityParameterValueProvider;
import org.springframework.data.mapping.model.PropertyValueProvider;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.redis.core.PartialUpdate;
import org.springframework.data.redis.core.PartialUpdate.PropertyUpdate;
import org.springframework.data.redis.core.PartialUpdate.UpdateCommand;
import org.springframework.data.redis.core.convert.*;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.redis.core.mapping.RedisPersistentProperty;
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.*;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.mapping.RedisEnhancedMappingContext;
import com.redis.om.spring.repository.query.QueryUtils;

/**
 * Redis Object Mapper converter that provides enhanced mapping capabilities between
 * Java objects and Redis data structures. This converter extends Spring Data Redis
 * with support for complex object mappings, JSON documents, and search indexing.
 */
public class MappingRedisOMConverter implements RedisConverter, InitializingBean {

  private static final String INVALID_TYPE_ASSIGNMENT = "Value of type %s cannot be assigned to property %s of type %s.";

  private final RedisMappingContext mappingContext;
  private final GenericConversionService conversionService;
  private final EntityInstantiators entityInstantiators;
  private final RedisTypeMapper typeMapper;
  private final Comparator<String> listKeyComparator = Comparator.nullsLast(NaturalOrderingKeyComparator.INSTANCE);
  private final ProjectionFactory projectionFactory;
  private @Nullable ReferenceResolver referenceResolver;
  private CustomConversions customConversions;

  /**
   * Creates a new {@link MappingRedisOMConverter} with default configuration.
   * This constructor initializes the converter with a null mapping context,
   * which will be set up with default values during initialization.
   */
  public MappingRedisOMConverter() {
    this(null);
  }

  /**
   * Creates new {@link MappingRedisOMConverter}.
   *
   * @param context can be {@literal null}.
   */
  public MappingRedisOMConverter(RedisMappingContext context) {
    this(context, null, null);
  }

  /**
   * Creates new {@link MappingRedisOMConverter} and defaults
   * {@link RedisMappingContext} when {@literal null}.
   *
   * @param mappingContext    can be {@literal null}.
   * @param referenceResolver can be not be {@literal null}.
   */
  public MappingRedisOMConverter(@Nullable RedisMappingContext mappingContext,
      @Nullable ReferenceResolver referenceResolver) {
    this(mappingContext, referenceResolver, null);
  }

  /**
   * Creates new {@link MappingRedisOMConverter} and defaults
   * {@link RedisMappingContext} when {@literal null}.
   *
   * @param mappingContext    can be {@literal null}.
   * @param referenceResolver can be {@literal null}.
   * @param typeMapper        can be {@literal null}.
   */
  public MappingRedisOMConverter(@Nullable RedisMappingContext mappingContext,
      @Nullable ReferenceResolver referenceResolver, @Nullable RedisTypeMapper typeMapper) {

    this.mappingContext = mappingContext != null ? mappingContext : new RedisEnhancedMappingContext();

    this.entityInstantiators = new EntityInstantiators();
    this.conversionService = new DefaultConversionService();
    this.customConversions = new RedisOMCustomConversions();
    this.typeMapper = typeMapper != null ?
        typeMapper :
        new DefaultRedisTypeMapper(DefaultRedisTypeMapper.DEFAULT_TYPE_KEY, this.mappingContext);
    this.projectionFactory = new SpelAwareProxyProjectionFactory();
    this.referenceResolver = referenceResolver;
    afterPropertiesSet();
  }

  private static boolean isByteArray(RedisPersistentProperty property) {
    return !property.getType().equals(byte[].class);
  }

  private static boolean isByteArray(TypeInformation<?> type) {
    return type.getType().equals(byte[].class);
  }

  /* (non-Javadoc)
   *
   * @see org.springframework.data.convert.EntityReader#read(java.lang.Class,
   * java.lang.Object) */
  @Override
  @SuppressWarnings(
    "unchecked"
  )
  public <R> R read(Class<R> type, RedisData source) {
    TypeInformation<?> readType = typeMapper.readType(source.getBucket().getPath(), TypeInformation.of(type));

    if (readType.isCollectionLike()) {
      return (R) readCollectionOrArray(type, "", ArrayList.class, Object.class, source.getBucket());
    }

    //    if (type.isInterface()) {
    //      return (R) projectionFactory.createProjection(type, result);
    //    }

    return readInternal(type, "", type, source);
  }

  @Nullable
  private <R> R readInternal(Class<?> entityClass, String path, Class<R> type, RedisData source) {
    return source.getBucket().isEmpty() ? null : doReadInternal(entityClass, path, type, source);
  }

  @SuppressWarnings(
    "unchecked"
  )
  private <R> R doReadInternal(Class<?> entityClass, String path, Class<R> type, RedisData source) {
    TypeInformation<?> readType = typeMapper.readType(source.getBucket().getPath(), TypeInformation.of(type));

    if (customConversions.hasCustomReadTarget(Map.class, readType.getType())) {
      Map<String, byte[]> partial = new HashMap<>();

      if (!path.isEmpty()) {
        for (Entry<String, byte[]> entry : source.getBucket().extract(path + ".").entrySet()) {
          partial.put(entry.getKey().substring(path.length() + 1), entry.getValue());
        }
      } else {
        partial.putAll(source.getBucket().asMap());
      }

      R instance = (R) conversionService.convert(partial, readType.getType());

      RedisPersistentEntity<?> entity = mappingContext.getPersistentEntity(readType);
      if (entity != null && instance != null && entity.hasIdProperty()) {
        PersistentPropertyAccessor<R> propertyAccessor = entity.getPropertyAccessor(instance);
        propertyAccessor.setProperty(entity.getRequiredIdProperty(), source.getId());
        instance = propertyAccessor.getBean();
      }

      return instance;
    }

    if (conversionService.canConvert(byte[].class, readType.getType())) {
      return (R) conversionService.convert(source.getBucket().get(StringUtils.hasText(path) ? path : "_raw"), readType
          .getType());
    }

    RedisPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(readType);
    EntityInstantiator instantiator = entityInstantiators.getInstantiatorFor(entity);

    Object instance;
    if (type.isInterface()) {
      instance = source.getBucket().asMap();
    } else {
      instance = instantiator.createInstance((RedisPersistentEntity<RedisPersistentProperty>) entity,
          new PersistentEntityParameterValueProvider<>(entity, new ConverterAwareParameterValueProvider(entityClass,
              path, source, conversionService), this.conversionService));
    }

    if (type.isInterface()) {
      Map<String, Object> map = new HashMap<>();
      RedisPersistentEntity<?> persistentEntity = mappingContext.getRequiredPersistentEntity(readType);
      for (Entry<String, byte[]> entry : source.getBucket().asMap().entrySet()) {
        String key = entry.getKey();
        byte[] value = entry.getValue();
        RedisPersistentProperty persistentProperty = persistentEntity.getPersistentProperty(key);
        Object convertedValue;
        if (persistentProperty != null) {
          // Convert the byte[] value to the appropriate type
          convertedValue = conversionService.convert(value, persistentProperty.getType());
        } else {
          // If the property is not found, treat the value as a String
          convertedValue = new String(value);
        }
        map.put(key, convertedValue);
      }

      // Create a proxy instance of the interface using Spring's ProxyFactory
      return projectionFactory.createProjection(type, map);
    } else {
      PersistentPropertyAccessor<Object> accessor = entity.getPropertyAccessor(instance);
      entity.doWithProperties((PropertyHandler<RedisPersistentProperty>) persistentProperty -> {
        InstanceCreatorMetadata<RedisPersistentProperty> constructor = entity.getInstanceCreatorMetadata();
        if (constructor != null && constructor.isCreatorParameter(persistentProperty)) {
          return;
        }
        Object targetValue = readProperty(entityClass, path, source, persistentProperty);
        if (targetValue != null) {
          accessor.setProperty(persistentProperty, targetValue);
        }
      });
      readAssociation(path, source, entity, accessor);
    }

    return (R) instance;
  }

  /**
   * Reads a property value from Redis data source and converts it to the appropriate type.
   *
   * @param entityClass        the entity class type
   * @param path               the property path in the Redis structure
   * @param source             the Redis data source
   * @param persistentProperty the property metadata
   * @return the converted property value or null if not found
   */
  @Nullable
  protected Object readProperty(Class<?> entityClass, String path, RedisData source,
      RedisPersistentProperty persistentProperty) {

    String currentPath = !path.isEmpty() ? path + "." + persistentProperty.getName() : persistentProperty.getName();

    TypeInformation<?> typeInformation = persistentProperty.getTypeInformation();

    if (persistentProperty.isMap()) {

      Class<?> mapValueType = persistentProperty.getMapValueType();

      if (mapValueType == null) {
        throw new IllegalArgumentException("Unable to retrieve MapValueType!");
      }

      if (conversionService.canConvert(byte[].class, mapValueType)) {
        return readMapOfSimpleTypes(currentPath, typeInformation.getType(), typeInformation.getRequiredComponentType()
            .getType(), mapValueType, source);
      }

      return readMapOfComplexTypes(entityClass, currentPath, typeInformation.getType(), typeInformation
          .getRequiredComponentType().getType(), mapValueType, source);
    }

    if (typeInformation.isCollectionLike()) {

      if (!isByteArray(typeInformation)) {
        return readCollectionOrArray(entityClass, currentPath, typeInformation.getType(), typeInformation
            .getRequiredComponentType().getType(), source.getBucket());
      }

      if (!source.getBucket().hasValue(currentPath) && isByteArray(typeInformation)) {

        return readCollectionOrArray(entityClass, currentPath, typeInformation.getType(), typeInformation
            .getRequiredComponentType().getType(), source.getBucket());
      }
    }

    if (persistentProperty.isEntity() && !conversionService.canConvert(byte[].class, typeInformation
        .getRequiredActualType().getType())) {

      Bucket bucket = source.getBucket().extract(currentPath + ".");

      RedisData newBucket = new RedisData(bucket);
      TypeInformation<?> typeToRead = typeMapper.readType(bucket.getPropertyPath(currentPath), typeInformation);

      return readInternal(entityClass, currentPath, typeToRead.getType(), newBucket);
    }

    byte[] sourceBytes = source.getBucket().get(currentPath);

    if (typeInformation.getType().isPrimitive() && sourceBytes == null) {
      return null;
    }

    if (persistentProperty.isIdProperty() && ObjectUtils.isEmpty(path.isEmpty())) {
      return sourceBytes != null ? fromBytes(sourceBytes, typeInformation.getType()) : source.getId();
    }

    if (sourceBytes == null) {
      return null;
    }

    if (customConversions.hasCustomReadTarget(byte[].class, persistentProperty.getType())) {
      return fromBytes(sourceBytes, persistentProperty.getType());
    }

    Class<?> typeToUse = getTypeHint(currentPath, source.getBucket(), persistentProperty.getType());
    return fromBytes(sourceBytes, typeToUse);
  }

  private void readAssociation(String path, RedisData source, RedisPersistentEntity<?> entity,
      PersistentPropertyAccessor<?> accessor) {

    entity.doWithAssociations((AssociationHandler<RedisPersistentProperty>) association -> {

      String currentPath = !path.isEmpty() ?
          path + "." + association.getInverse().getName() :
          association.getInverse().getName();

      if (association.getInverse().isCollectionLike()) {

        Bucket bucket = source.getBucket().extract(currentPath + ".[");

        Collection<Object> target = CollectionFactory.createCollection(association.getInverse().getType(), association
            .getInverse().getComponentType(), bucket.size());

        for (Entry<String, byte[]> entry : bucket.entrySet()) {

          String referenceKey = fromBytes(entry.getValue(), String.class);

          if (!KeyspaceIdentifier.isValid(referenceKey)) {
            continue;
          }

          KeyspaceIdentifier identifier = KeyspaceIdentifier.of(referenceKey);
          Map<byte[], byte[]> rawHash = referenceResolver.resolveReference(identifier.getId(), identifier
              .getKeyspace());

          if (!CollectionUtils.isEmpty(rawHash)) {
            target.add(read(association.getInverse().getActualType(), new RedisData(rawHash)));
          }
        }

        accessor.setProperty(association.getInverse(), target);

      } else {

        byte[] binKey = source.getBucket().get(currentPath);
        if (binKey == null || binKey.length == 0) {
          return;
        }

        String referenceKey = fromBytes(binKey, String.class);
        if (KeyspaceIdentifier.isValid(referenceKey)) {

          KeyspaceIdentifier identifier = KeyspaceIdentifier.of(referenceKey);

          Map<byte[], byte[]> rawHash = referenceResolver.resolveReference(identifier.getId(), identifier
              .getKeyspace());

          if (!CollectionUtils.isEmpty(rawHash)) {
            accessor.setProperty(association.getInverse(), read(association.getInverse().getActualType(), new RedisData(
                rawHash)));
          }
        }
      }
    });
  }

  /* (non-Javadoc)
   *
   * @see org.springframework.data.convert.EntityWriter#write(java.lang.Object,
   * java.lang.Object) */
  @Override
  @SuppressWarnings(
    { "ConstantValue" }
  )
  public void write(Object source, RedisData sink) {
    if (source == null)
      return;

    if (source instanceof PartialUpdate pu) {
      writePartialUpdate(pu, sink);
      return;
    }

    RedisPersistentEntity<?> entity = mappingContext.getPersistentEntity(source.getClass());

    if (!customConversions.hasCustomWriteTarget(source.getClass())) {
      typeMapper.writeType(ClassUtils.getUserClass(source), sink.getBucket().getPath());
    }

    writeEntity(entity, source, sink);
  }

  private void writeEntity(RedisPersistentEntity<?> entity, Object source, RedisData sink) {
    if (entity == null) {
      typeMapper.writeType(ClassUtils.getUserClass(source), sink.getBucket().getPath());
      sink.getBucket().put("_raw", conversionService.convert(source, byte[].class));
      return;
    }

    sink.setKeyspace(entity.getKeySpace());

    if (entity.getTypeInformation().isCollectionLike()) {
      writeCollection(entity.getType(), entity.getKeySpace(), "", (List) source, entity.getTypeInformation()
          .getRequiredComponentType(), sink);
    } else {
      writeInternal(entity.getKeySpace(), "", source, entity.getTypeInformation(), sink);
    }

    Object identifier = entity.getIdentifierAccessor(source).getIdentifier();

    if (identifier != null) {
      sink.setId(getConversionService().convert(identifier, String.class));
    }

    Long ttl = entity.getTimeToLiveAccessor().getTimeToLive(source);
    if (ttl != null && ttl > 0) {
      sink.setTimeToLive(ttl);
    }
  }

  /**
   * Writes a partial update to Redis, applying only the specified property changes.
   *
   * @param update the partial update containing the changes to apply
   * @param sink   the Redis data destination
   */
  protected void writePartialUpdate(PartialUpdate<?> update, RedisData sink) {

    RedisPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(update.getTarget());

    write(update.getValue(), sink);

    for (String key : sink.getBucket().keySet()) {
      if (typeMapper.isTypeKey(key)) {
        sink.getBucket().remove(key);
        break;
      }
    }

    if (update.isRefreshTtl() && !update.getPropertyUpdates().isEmpty()) {

      Long ttl = entity.getTimeToLiveAccessor().getTimeToLive(update);
      if (ttl != null && ttl > 0) {
        sink.setTimeToLive(ttl);
      }
    }

    for (PropertyUpdate pUpdate : update.getPropertyUpdates()) {

      String path = pUpdate.getPropertyPath();

      if (UpdateCommand.SET.equals(pUpdate.getCmd())) {
        writePartialPropertyUpdate(update, pUpdate, sink, entity, path);
      }
    }
  }

  /**
   * @param update
   * @param pUpdate
   * @param sink
   * @param entity
   * @param path
   */
  private void writePartialPropertyUpdate(PartialUpdate<?> update, PropertyUpdate pUpdate, RedisData sink,
      RedisPersistentEntity<?> entity, String path) {

    RedisPersistentProperty targetProperty = getTargetPropertyOrNullForPath(path, update.getTarget());

    if (targetProperty == null) {

      targetProperty = getTargetPropertyOrNullForPath(path.replaceAll("\\.\\[.*]", ""), update.getTarget());

      TypeInformation<?> ti = targetProperty == null ?
          TypeInformation.OBJECT :
          (targetProperty.isMap() ?
              (targetProperty.getTypeInformation().getMapValueType() != null ?
                  targetProperty.getTypeInformation().getRequiredMapValueType() :
                  TypeInformation.OBJECT) :
              targetProperty.getTypeInformation().getActualType());

      writeInternal(entity.getKeySpace(), pUpdate.getPropertyPath(), pUpdate.getValue(), ti, sink);
      return;
    }

    if (targetProperty.isAssociation()) {

      if (targetProperty.isCollectionLike()) {

        RedisPersistentEntity<?> ref = mappingContext.getPersistentEntity(targetProperty.getRequiredAssociation()
            .getInverse().getTypeInformation().getRequiredComponentType().getRequiredActualType());

        int i = 0;
        for (Object o : (Collection<?>) pUpdate.getValue()) {

          Object refId = ref.getPropertyAccessor(o).getProperty(ref.getRequiredIdProperty());
          if (refId != null) {
            sink.getBucket().put(pUpdate.getPropertyPath() + ".[" + i + "]", toBytes(ref.getKeySpace() + ":" + refId));
            i++;
          }
        }
      } else {

        RedisPersistentEntity<?> ref = mappingContext.getRequiredPersistentEntity(targetProperty
            .getRequiredAssociation().getInverse().getTypeInformation());

        Object refId = ref.getPropertyAccessor(pUpdate.getValue()).getProperty(ref.getRequiredIdProperty());
        if (refId != null) {
          sink.getBucket().put(pUpdate.getPropertyPath(), toBytes(ref.getKeySpace() + ":" + refId));
        }
      }
    } else if (targetProperty.isCollectionLike() && isByteArray(targetProperty)) {

      Collection<?> collection = pUpdate.getValue() instanceof Collection ?
          (Collection<?>) pUpdate.getValue() :
          Collections.singleton(pUpdate.getValue());
      writeCollection(entity.getType(), entity.getKeySpace(), pUpdate.getPropertyPath(), collection, targetProperty
          .getTypeInformation().getRequiredActualType(), sink);
    } else if (targetProperty.isMap()) {

      Map<Object, Object> map = new HashMap<>();

      if (pUpdate.getValue() instanceof Map) {
        map.putAll((Map<?, ?>) pUpdate.getValue());
      } else if (pUpdate.getValue() instanceof Entry) {
        map.put(((Entry<?, ?>) pUpdate.getValue()).getKey(), ((Entry<?, ?>) pUpdate.getValue()).getValue());
      } else {
        throw new MappingException(String.format(
            "Cannot set update value for map property '%s' to '%s'. Please use a Map or Map.Entry.", pUpdate
                .getPropertyPath(), pUpdate.getValue()));
      }

      writeMap(entity.getType(), entity.getKeySpace(), pUpdate.getPropertyPath(), targetProperty.getMapValueType(), map,
          sink);
    } else {

      writeInternal(entity.getKeySpace(), pUpdate.getPropertyPath(), pUpdate.getValue(), targetProperty
          .getTypeInformation(), sink);
    }
  }

  @Nullable
  RedisPersistentProperty getTargetPropertyOrNullForPath(String path, Class<?> type) {

    try {

      PersistentPropertyPath<RedisPersistentProperty> persistentPropertyPath = mappingContext.getPersistentPropertyPath(
          path, type);
      return persistentPropertyPath.getLeafProperty();
    } catch (Exception e) {
      // that's just fine
    }

    return null;
  }

  /**
   * @param keyspace
   * @param path
   * @param value
   * @param typeHint
   * @param sink
   */
  private void writeInternal(@Nullable String keyspace, String path, @Nullable Object value,
      TypeInformation<?> typeHint, RedisData sink) {

    if (value == null) {
      return;
    }

    if (customConversions.hasCustomWriteTarget(value.getClass())) {

      Optional<Class<?>> targetType = customConversions.getCustomWriteTarget(value.getClass());

      if (!StringUtils.hasText(path) && targetType.isPresent() && ClassUtils.isAssignable(byte[].class, targetType
          .get())) {
        sink.getBucket().put(StringUtils.hasText(path) ? path : "_raw", conversionService.convert(value, byte[].class));
      } else {

        if (!ClassUtils.isAssignable(typeHint.getType(), value.getClass())) {
          throw new MappingException(String.format(INVALID_TYPE_ASSIGNMENT, value.getClass(), path, typeHint
              .getType()));
        }
        writeToBucket(path, value, sink, typeHint.getType());
      }
      return;
    }

    if (value instanceof byte[] ba) {
      sink.getBucket().put(StringUtils.hasText(path) ? path : "_raw", ba);
      return;
    }

    if (value.getClass() != typeHint.getType()) {
      typeMapper.writeType(value.getClass(), sink.getBucket().getPropertyPath(path));
    }

    RedisPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(value.getClass());
    PersistentPropertyAccessor<Object> accessor = entity.getPropertyAccessor(value);

    entity.doWithProperties((PropertyHandler<RedisPersistentProperty>) persistentProperty -> {

      String propertyStringPath = (!path.isEmpty() ? path + "." : "") + persistentProperty.getName();

      Object propertyValue = accessor.getProperty(persistentProperty);
      if (persistentProperty.isIdProperty()) {

        if (propertyValue != null) {
          sink.getBucket().put(propertyStringPath, toBytes(propertyValue));
        }
        return;
      }

      if (persistentProperty.isMap()) {

        if (propertyValue != null) {
          writeMap(entity.getType(), keyspace, propertyStringPath, persistentProperty.getMapValueType(),
              (Map<?, ?>) propertyValue, sink);
        }
      } else if (persistentProperty.isCollectionLike() && isByteArray(persistentProperty)) {

        if (propertyValue == null) {
          writeCollection(entity.getType(), keyspace, propertyStringPath, null, persistentProperty.getTypeInformation()
              .getRequiredComponentType(), sink);
        } else {

          if (Iterable.class.isAssignableFrom(propertyValue.getClass())) {

            writeCollection(entity.getType(), keyspace, propertyStringPath, (Iterable<?>) propertyValue,
                persistentProperty.getTypeInformation().getRequiredComponentType(), sink);
          } else if (propertyValue.getClass().isArray()) {

            writeCollection(entity.getType(), keyspace, propertyStringPath, CollectionUtils.arrayToList(propertyValue),
                persistentProperty.getTypeInformation().getRequiredComponentType(), sink);
          } else {

            throw new RuntimeException("Don't know how to handle " + propertyValue.getClass() + " type collection");
          }
        }

      } else if (persistentProperty.isEntity()) {

        if (propertyValue != null) {
          writeInternal(keyspace, propertyStringPath, propertyValue, persistentProperty.getTypeInformation()
              .getRequiredActualType(), sink);
        }
      } else {

        if (propertyValue != null) {
          writeToBucket(propertyStringPath, propertyValue, sink, persistentProperty.getType());
        }
      }
    });

    writeAssociation(path, entity, value, sink);
  }

  private void writeAssociation(String path, RedisPersistentEntity<?> entity, @Nullable Object value, RedisData sink) {

    if (value == null) {
      return;
    }

    PersistentPropertyAccessor<Object> accessor = entity.getPropertyAccessor(value);

    entity.doWithAssociations((AssociationHandler<RedisPersistentProperty>) association -> {

      Object refObject = accessor.getProperty(association.getInverse());
      if (refObject == null) {
        return;
      }

      if (association.getInverse().isCollectionLike()) {

        RedisPersistentEntity<?> ref = mappingContext.getRequiredPersistentEntity(association.getInverse()
            .getTypeInformation().getRequiredComponentType().getRequiredActualType());

        String keyspace = ref.getKeySpace();
        String propertyStringPath = (!path.isEmpty() ? path + "." : "") + association.getInverse().getName();

        int i = 0;
        for (Object o : (Collection<?>) refObject) {

          Object refId = ref.getPropertyAccessor(o).getProperty(ref.getRequiredIdProperty());
          if (refId != null) {
            sink.getBucket().put(propertyStringPath + ".[" + i + "]", toBytes(keyspace + ":" + refId));
            i++;
          }
        }

      } else {

        RedisPersistentEntity<?> ref = mappingContext.getRequiredPersistentEntity(association.getInverse()
            .getTypeInformation());
        String keyspace = ref.getKeySpace();

        if (keyspace != null) {
          Object refId = ref.getPropertyAccessor(refObject).getProperty(ref.getRequiredIdProperty());

          if (refId != null) {
            String propertyStringPath = (!path.isEmpty() ? path + "." : "") + association.getInverse().getName();
            sink.getBucket().put(propertyStringPath, toBytes(keyspace + ":" + refId));
          }
        }
      }
    });
  }

  /**
   * @param entityClass
   * @param keyspace
   * @param path
   * @param values
   * @param typeHint
   * @param sink
   */
  private void writeCollection(Class<?> entityClass, @Nullable String keyspace, String path,
      @Nullable Iterable<?> values, TypeInformation<?> typeHint, RedisData sink) {

    if (values == null) {
      return;
    }

    Field field = null;
    Class<?> collectionElementType = null;
    Indexed indexed = null;
    TagIndexed tagIndexed = null;
    try {
      field = ReflectionUtils.findField(entityClass, path);
      if (field != null) {
        Optional<Class<?>> maybeCollectionElementType = getCollectionElementClass(field);
        collectionElementType = maybeCollectionElementType.orElse(null);
        if (field.isAnnotationPresent(Indexed.class)) {
          indexed = field.getAnnotation(Indexed.class);
        } else if (field.isAnnotationPresent(TagIndexed.class)) {
          tagIndexed = field.getAnnotation(TagIndexed.class);
        }
      }
    } catch (SecurityException | NoSuchElementException e1) {
      // it's ok, move on!
    }

    if ((field != null) && (collectionElementType != null) && (indexed != null || tagIndexed != null) && CharSequence.class
        .isAssignableFrom(collectionElementType)) {
      @SuppressWarnings(
        "null"
      ) String separator = indexed != null ? indexed.separator() : tagIndexed.separator();
      String value = StreamSupport.stream(values.spliterator(), false).map(Object::toString).map(QueryUtils::escape)
          .collect(Collectors.joining(separator));
      writeInternal(keyspace, path, value, typeHint, sink);
    } else {
      int i = 0;
      for (Object value : values) {

        if (value == null) {
          break;
        }

        String currentPath = path + (path.isEmpty() ? "" : ".") + "[" + i + "]";

        if (!ClassUtils.isAssignable(typeHint.getType(), value.getClass())) {
          throw new MappingException(String.format(INVALID_TYPE_ASSIGNMENT, value.getClass(), currentPath, typeHint
              .getType()));
        }

        if (customConversions.hasCustomWriteTarget(value.getClass())) {
          writeToBucket(currentPath, value, sink, typeHint.getType());
        } else {
          writeInternal(keyspace, currentPath, value, typeHint, sink);
        }
        i++;
      }
    }
  }

  private void writeToBucket(String path, @Nullable Object value, RedisData sink, Class<?> propertyType) {

    if (value == null || (value instanceof Optional && ((Optional<?>) value).isEmpty())) {
      return;
    }

    if (value instanceof byte[]) {
      sink.getBucket().put(path, toBytes(value));
      return;
    }

    if (customConversions.hasCustomWriteTarget(value.getClass())) {

      Optional<Class<?>> targetType = customConversions.getCustomWriteTarget(value.getClass());

      if (!propertyType.isPrimitive() && targetType.filter(it -> ClassUtils.isAssignable(Map.class, it))
          .isEmpty() && customConversions.isSimpleType(value.getClass()) && value.getClass() != propertyType) {
        typeMapper.writeType(value.getClass(), sink.getBucket().getPropertyPath(path));
      }

      if (targetType.filter(it -> ClassUtils.isAssignable(Map.class, it)).isPresent()) {

        Map<?, ?> map = (Map<?, ?>) conversionService.convert(value, targetType.get());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
          sink.getBucket().put(path + (StringUtils.hasText(path) ? "." : "") + entry.getKey(), toBytes(entry
              .getValue()));
        }
      } else if (targetType.filter(it -> ClassUtils.isAssignable(byte[].class, it)).isPresent()) {
        sink.getBucket().put(path, toBytes(value));
      } else {
        throw new IllegalArgumentException(String.format("Cannot convert value '%s' of type %s to bytes.", value, value
            .getClass()));
      }
    }
  }

  @Nullable
  private Object readCollectionOrArray(Class<?> entityClass, String path, Class<?> collectionType, Class<?> valueType,
      Bucket bucket) {
    Field field = null;
    Class<?> collectionElementType = null;
    Indexed indexed = null;
    TagIndexed tagIndexed = null;
    try {
      field = ReflectionUtils.findField(entityClass, path);
      if (field != null) {
        Optional<Class<?>> maybeCollectionElementType = com.redis.om.spring.util.ObjectUtils.getCollectionElementClass(
            field);
        collectionElementType = maybeCollectionElementType.orElse(null);
        if (field.isAnnotationPresent(Indexed.class)) {
          indexed = field.getAnnotation(Indexed.class);
        } else if (field.isAnnotationPresent(TagIndexed.class)) {
          tagIndexed = field.getAnnotation(TagIndexed.class);
        }
      }
    } catch (SecurityException | NoSuchElementException e) {
      // it's ok, move on!
    }

    Collection<Object> target;
    boolean isArray = collectionType.isArray();

    if ((field != null) && (indexed != null || tagIndexed != null) && (collectionElementType != null && CharSequence.class
        .isAssignableFrom(collectionElementType))) {
      @SuppressWarnings(
        "null"
      ) String separator = indexed != null ? indexed.separator() : tagIndexed.separator();
      Bucket elementData = bucket.extract(path);
      TypeInformation<?> typeInformation = typeMapper.readType(elementData.getPropertyPath(path), TypeInformation.of(
          valueType));
      Class<?> collectionTypeToUse = isArray ? ArrayList.class : collectionType;
      Class<?> typeToUse = typeInformation.getType();
      String collectionAsString;
      if (conversionService.canConvert(byte[].class, typeToUse)) {
        collectionAsString = (String) fromBytes(elementData.get(path), typeToUse);
      } else {
        collectionAsString = (String) readInternal(entityClass, path, typeToUse, new RedisData(elementData));
      }
      if (collectionAsString == null) {
        collectionAsString = "";
      }
      @SuppressWarnings(
        "Annotator"
      ) List<String> values = Arrays.stream(collectionAsString.split("\\" + separator)).map(QueryUtils::unescape)
          .toList();
      target = CollectionFactory.createCollection(collectionTypeToUse, valueType, values.size());
      target.addAll(values);
    } else {
      List<String> keys = new ArrayList<>(bucket.extractAllKeysFor(path));
      keys.sort(listKeyComparator);

      Class<?> collectionTypeToUse = isArray ? ArrayList.class : collectionType;
      target = CollectionFactory.createCollection(collectionTypeToUse, valueType, keys.size());

      for (String key : keys) {

        if (typeMapper.isTypeKey(key)) {
          continue;
        }

        Bucket elementData = bucket.extract(key);

        TypeInformation<?> typeInformation = typeMapper.readType(elementData.getPropertyPath(key), TypeInformation.of(
            valueType));

        Class<?> typeToUse = typeInformation.getType();
        if (conversionService.canConvert(byte[].class, typeToUse)) {
          target.add(fromBytes(elementData.get(key), typeToUse));
        } else {
          target.add(readInternal(entityClass, key, typeToUse, new RedisData(elementData)));
        }
      }
    }

    return isArray ? toArray(target, collectionType, valueType) : (target.isEmpty() ? null : target);
  }

  /**
   * @param keyspace
   * @param path
   * @param mapValueType
   * @param source
   * @param sink
   */
  private void writeMap(Class<?> entityClass, @Nullable String keyspace, String path, Class<?> mapValueType,
      Map<?, ?> source, RedisData sink) {

    if (CollectionUtils.isEmpty(source)) {
      return;
    }

    for (Map.Entry<?, ?> entry : source.entrySet()) {

      if (entry.getValue() == null || entry.getKey() == null) {
        continue;
      }

      String currentPath = path + ".[" + mapMapKey(entry.getKey()) + "]";

      if (!ClassUtils.isAssignable(mapValueType, entry.getValue().getClass())) {
        throw new MappingException(String.format(INVALID_TYPE_ASSIGNMENT, entry.getValue().getClass(), currentPath,
            mapValueType));
      }

      if (customConversions.hasCustomWriteTarget(entry.getValue().getClass())) {
        writeToBucket(currentPath, entry.getValue(), sink, mapValueType);
      } else {
        writeInternal(keyspace, currentPath, entry.getValue(), TypeInformation.of(mapValueType), sink);
      }
    }
  }

  private String mapMapKey(Object key) {

    if (conversionService.canConvert(key.getClass(), byte[].class)) {
      return new String(conversionService.convert(key, byte[].class));
    }

    return conversionService.convert(key, String.class);
  }

  /**
   * @param path
   * @param mapType
   * @param keyType
   * @param valueType
   * @param source
   * @return
   */
  @Nullable
  private Map<?, ?> readMapOfSimpleTypes(String path, Class<?> mapType, Class<?> keyType, Class<?> valueType,
      RedisData source) {

    Bucket partial = source.getBucket().extract(path + ".[");

    Map<Object, Object> target = CollectionFactory.createMap(mapType, partial.size());

    for (Entry<String, byte[]> entry : partial.entrySet()) {

      if (typeMapper.isTypeKey(entry.getKey())) {
        continue;
      }

      Object key = extractMapKeyForPath(path, entry.getKey(), keyType);
      Class<?> typeToUse = getTypeHint(path + ".[" + key + "]", source.getBucket(), valueType);
      target.put(key, fromBytes(entry.getValue(), typeToUse));
    }

    return target.isEmpty() ? null : target;
  }

  /**
   * @param path
   * @param mapType
   * @param keyType
   * @param valueType
   * @param source
   * @return
   */
  @Nullable
  private Map<?, ?> readMapOfComplexTypes(Class<?> entityClass, String path, Class<?> mapType, Class<?> keyType,
      Class<?> valueType, RedisData source) {

    Set<String> keys = source.getBucket().extractAllKeysFor(path);

    Map<Object, Object> target = CollectionFactory.createMap(mapType, keys.size());

    for (String key : keys) {

      Bucket partial = source.getBucket().extract(key);

      Object mapKey = extractMapKeyForPath(path, key, keyType);

      TypeInformation<?> typeInformation = typeMapper.readType(source.getBucket().getPropertyPath(key), TypeInformation
          .of(valueType));

      Object o = readInternal(entityClass, key, typeInformation.getType(), new RedisData(partial));
      target.put(mapKey, o);
    }

    return target.isEmpty() ? null : target;
  }

  @Nullable
  private Object extractMapKeyForPath(String path, String key, Class<?> targetType) {

    String regex = "^(" + Pattern.quote(path) + "\\.\\[)(.*?)(])";
    Pattern pattern = Pattern.compile(regex);

    Matcher matcher = pattern.matcher(key);
    if (!matcher.find()) {
      throw new IllegalArgumentException(String.format("Cannot extract map value for key '%s' in path '%s'.", key,
          path));
    }

    Object mapKey = matcher.group(2);

    if (ClassUtils.isAssignable(targetType, mapKey.getClass())) {
      return mapKey;
    }

    return conversionService.convert(toBytes(mapKey), targetType);
  }

  private Class<?> getTypeHint(String path, Bucket bucket, Class<?> fallback) {

    TypeInformation<?> typeInformation = typeMapper.readType(bucket.getPropertyPath(path), TypeInformation.of(
        fallback));
    return typeInformation.getType();
  }

  /**
   * Convert given source to binary representation using the underlying
   * {@link ConversionService}.
   *
   * @param source the source object
   * @return a byte array representation
   * @throws ConverterNotFoundException if a specific converter cannot be found
   */
  public byte[] toBytes(Object source) {

    if (source instanceof byte[] ba) {
      return ba;
    }

    return conversionService.convert(source, byte[].class);
  }

  /**
   * Convert given binary representation to desired target type using the
   * underlying {@link ConversionService}.
   *
   * @param <T>    expected type of the source object
   * @param source the source object
   * @param type   the class to cast the object to
   * @return the target object
   * @throws ConverterNotFoundException if a specific converter cannot be found
   */
  public <T> T fromBytes(byte[] source, Class<T> type) {

    if (type.isInstance(source)) {
      return type.cast(source);
    }

    return conversionService.convert(source, type);
  }

  /**
   * Converts a given {@link Collection} into an array considering primitive
   * types.
   *
   * @param source    {@link Collection} of values to be added to the array.
   * @param arrayType {@link Class} of array.
   * @param valueType to be used for conversion before setting the actual value.
   * @return
   */
  @Nullable
  private Object toArray(Collection<Object> source, Class<?> arrayType, Class<?> valueType) {

    if (source.isEmpty()) {
      return null;
    }

    if (!ClassUtils.isPrimitiveArray(arrayType)) {
      return source.toArray((Object[]) Array.newInstance(valueType, source.size()));
    }

    Object targetArray = Array.newInstance(valueType, source.size());
    Iterator<Object> iterator = source.iterator();
    int i = 0;
    while (iterator.hasNext()) {
      Array.set(targetArray, i, conversionService.convert(iterator.next(), valueType));
      i++;
    }
    return i > 0 ? targetArray : null;
  }

  /**
   * Sets the reference resolver used for resolving object references in Redis.
   *
   * @param referenceResolver the reference resolver instance
   */
  public void setReferenceResolver(ReferenceResolver referenceResolver) {
    this.referenceResolver = referenceResolver;
  }

  /**
   * Set {@link CustomConversions} to be applied.
   *
   * @param customConversions custom convertions to be added to the pipeline
   */
  public void setCustomConversions(@Nullable CustomConversions customConversions) {
    this.customConversions = customConversions != null ? customConversions : new RedisCustomConversions();
  }

  /* (non-Javadoc)
   *
   * @see org.springframework.data.convert.EntityConverter#getMappingContext() */
  @Override
  public RedisMappingContext getMappingContext() {
    return this.mappingContext;
  }

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.redis.core.convert.RedisConverter#getIndexResolver(
   * ) */
  @Nullable
  @Override
  public IndexResolver getIndexResolver() {
    return null;
  }

  @Override
  public EntityInstantiators getEntityInstantiators() {
    return entityInstantiators;
  }

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.convert.EntityConverter#getConversionService() */
  @Override
  public ConversionService getConversionService() {
    return this.conversionService;
  }

  @Override
  public void afterPropertiesSet() {
    this.initializeConverters();
  }

  private void initializeConverters() {
    customConversions.registerConvertersIn(conversionService);
  }

  private enum NaturalOrderingKeyComparator implements Comparator<String> {

    INSTANCE;

    /* (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object) */
    public int compare(String s1, String s2) {

      int s1offset = 0;
      int s2offset = 0;

      while (s1offset < s1.length() && s2offset < s2.length()) {

        Part thisPart = extractPart(s1, s1offset);
        Part thatPart = extractPart(s2, s2offset);

        int result = thisPart.compareTo(thatPart);

        if (result != 0) {
          return result;
        }

        s1offset += thisPart.length();
        s2offset += thatPart.length();
      }

      return 0;
    }

    private Part extractPart(String source, int offset) {

      StringBuilder builder = new StringBuilder();

      char c = source.charAt(offset);
      builder.append(c);

      boolean isDigit = Character.isDigit(c);
      for (int i = offset + 1; i < source.length(); i++) {

        c = source.charAt(i);
        if ((isDigit && !Character.isDigit(c)) || (!isDigit && Character.isDigit(c))) {
          break;
        }
        builder.append(c);
      }

      return new Part(builder.toString(), isDigit);
    }

    private static class Part implements Comparable<Part> {

      private final String rawValue;
      private final @Nullable Long longValue;

      Part(String value, boolean isDigit) {

        this.rawValue = value;
        this.longValue = isDigit ? Long.valueOf(value) : null;
      }

      boolean isNumeric() {
        return longValue != null;
      }

      int length() {
        return rawValue.length();
      }

      /* (non-Javadoc)
       *
       * @see java.lang.Comparable#compareTo(java.lang.Object) */
      @Override
      public int compareTo(Part that) {

        if (this.isNumeric() && that.isNumeric()) {
          return this.longValue.compareTo(that.longValue);
        }

        return this.rawValue.compareTo(that.rawValue);
      }

      @Override
      public boolean equals(Object o) {
        // self check
        if (this == o)
          return true;
        // null check
        if (o == null)
          return false;
        // type check and cast
        if (getClass() != o.getClass())
          return false;
        Part part = (Part) o;
        // field comparison
        if (this.isNumeric() && part.isNumeric()) {
          return this.longValue.equals(part.longValue);
        }

        return this.rawValue.equals(part.rawValue);
      }

      @Override
      public int hashCode() {
        return Objects.hash(longValue);
      }
    }
  }

  /**
   * Value object representing a Redis Hash/Object identifier composed of
   * keyspace and object id in the form of {@literal keyspace:id}.
   *
   * @author Mark Paluch
   * @author Stefan Berger
   * @since 1.8.10
   */
  public static class KeyspaceIdentifier {

    /** Phantom key suffix used for temporary references */
    public static final String PHANTOM = "phantom";
    /** Delimiter separating keyspace from object ID */
    public static final String DELIMITER = ":";
    /** Complete phantom suffix combining delimiter and phantom marker */
    public static final String PHANTOM_SUFFIX = DELIMITER + PHANTOM;

    private final String keyspace;
    private final String id;
    private final boolean phantomKey;

    private KeyspaceIdentifier(String keyspace, String id, boolean phantomKey) {
      this.keyspace = keyspace;
      this.id = id;
      this.phantomKey = phantomKey;
    }

    /**
     * Parse a {@code key} into {@link KeyspaceIdentifier}.
     *
     * @param key the key representation.
     * @return {@link BinaryKeyspaceIdentifier} for binary key.
     */
    public static KeyspaceIdentifier of(String key) {

      Assert.isTrue(isValid(key), String.format("Invalid key %s", key));

      boolean phantomKey = key.endsWith(PHANTOM_SUFFIX);
      int keyspaceEndIndex = key.indexOf(DELIMITER);
      String keyspace = key.substring(0, keyspaceEndIndex);
      String id;

      if (phantomKey) {
        id = key.substring(keyspaceEndIndex + 1, key.length() - PHANTOM_SUFFIX.length());
      } else {
        id = key.substring(keyspaceEndIndex + 1);
      }

      return new KeyspaceIdentifier(keyspace, id, phantomKey);
    }

    /**
     * Check whether the {@code key} is valid, in particular whether the key
     * contains a keyspace and an id part in the form of {@literal keyspace:id}.
     *
     * @param key the key.
     * @return {@literal true} if the key is valid.
     */
    public static boolean isValid(@Nullable String key) {

      if (key == null) {
        return false;
      }

      int keyspaceEndIndex = key.indexOf(DELIMITER);

      return keyspaceEndIndex > 0;
    }

    /**
     * Gets the keyspace portion of the identifier.
     *
     * @return the keyspace string
     */
    public String getKeyspace() {
      return this.keyspace;
    }

    /**
     * Gets the ID portion of the identifier.
     *
     * @return the ID string
     */
    public String getId() {
      return this.id;
    }

    /**
     * Checks if this is a phantom key (temporary reference).
     *
     * @return true if this is a phantom key, false otherwise
     */
    public boolean isPhantomKey() {
      return this.phantomKey;
    }
  }

  /**
   * Value object representing a binary Redis Hash/Object identifier composed of
   * keyspace and object id in the form of {@literal keyspace:id}.
   *
   * @author Mark Paluch
   * @author Stefan Berger
   * @since 1.8.10
   */
  public static class BinaryKeyspaceIdentifier {

    /** Binary delimiter separating keyspace from object ID */
    public static final byte DELIMITER = ':';
    /** Binary phantom marker for temporary references */
    protected static final byte[] PHANTOM = KeyspaceIdentifier.PHANTOM.getBytes();
    /** Binary phantom suffix combining delimiter and phantom marker */
    protected static final byte[] PHANTOM_SUFFIX = ByteUtils.concat(new byte[] { DELIMITER }, PHANTOM);

    private final byte[] keyspace;
    private final byte[] id;
    private final boolean phantomKey;

    private BinaryKeyspaceIdentifier(byte[] keyspace, byte[] id, boolean phantomKey) {

      this.keyspace = keyspace;
      this.id = id;
      this.phantomKey = phantomKey;
    }

    /**
     * Parse a binary {@code key} into {@link BinaryKeyspaceIdentifier}.
     *
     * @param key the binary key representation.
     * @return {@link BinaryKeyspaceIdentifier} for binary key.
     */
    public static BinaryKeyspaceIdentifier of(byte[] key) {

      Assert.isTrue(isValid(key), String.format("Invalid key %s", new String(key)));

      boolean phantomKey = ByteUtils.startsWith(key, PHANTOM_SUFFIX, key.length - PHANTOM_SUFFIX.length);

      int keyspaceEndIndex = ByteUtils.indexOf(key, DELIMITER);
      byte[] keyspace = extractKeyspace(key, keyspaceEndIndex);
      byte[] id = extractId(key, phantomKey, keyspaceEndIndex);

      return new BinaryKeyspaceIdentifier(keyspace, id, phantomKey);
    }

    /**
     * Check whether the {@code key} is valid, in particular whether the key
     * contains a keyspace and an id part in the form of {@literal keyspace:id}.
     *
     * @param key the key.
     * @return {@literal true} if the key is valid.
     */
    public static boolean isValid(byte[] key) {

      if (key.length == 0) {
        return false;
      }

      int keyspaceEndIndex = ByteUtils.indexOf(key, DELIMITER);

      return keyspaceEndIndex > 0 && key.length > keyspaceEndIndex;
    }

    private static byte[] extractId(byte[] key, boolean phantomKey, int keyspaceEndIndex) {

      int idSize;

      if (phantomKey) {
        idSize = (key.length - PHANTOM_SUFFIX.length) - (keyspaceEndIndex + 1);
      } else {

        idSize = key.length - (keyspaceEndIndex + 1);
      }

      byte[] id = new byte[idSize];
      System.arraycopy(key, keyspaceEndIndex + 1, id, 0, idSize);

      return id;
    }

    private static byte[] extractKeyspace(byte[] key, int keyspaceEndIndex) {

      byte[] keyspace = new byte[keyspaceEndIndex];
      System.arraycopy(key, 0, keyspace, 0, keyspaceEndIndex);

      return keyspace;
    }

    /**
     * Gets the binary keyspace portion of the identifier.
     *
     * @return the keyspace as byte array
     */
    public byte[] getKeyspace() {
      return this.keyspace;
    }

    /**
     * Gets the binary ID portion of the identifier.
     *
     * @return the ID as byte array
     */
    public byte[] getId() {
      return this.id;
    }

    /**
     * Checks if this is a phantom key (temporary reference).
     *
     * @return true if this is a phantom key, false otherwise
     */
    public boolean isPhantomKey() {
      return this.phantomKey;
    }
  }

  /**
   * @author Christoph Strobl
   * @author Mark Paluch
   */
  private class ConverterAwareParameterValueProvider implements PropertyValueProvider<RedisPersistentProperty> {

    private final String path;
    private final RedisData source;
    private final ConversionService conversionService;
    private final Class<?> entityClass;

    ConverterAwareParameterValueProvider(Class<?> entityClass, String path, RedisData source,
        ConversionService conversionService) {
      this.entityClass = entityClass;
      this.path = path;
      this.source = source;
      this.conversionService = conversionService;
    }

    @Override
    @SuppressWarnings(
      "unchecked"
    )
    public <T> T getPropertyValue(RedisPersistentProperty property) {

      Object value = readProperty(entityClass, path, source, property);

      if (value == null || ClassUtils.isAssignableValue(property.getType(), value)) {
        return (T) value;
      }

      return (T) conversionService.convert(value, property.getType());
    }
  }
}
