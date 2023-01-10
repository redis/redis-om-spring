package com.redis.om.spring.repository.support;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.serialization.gson.GsonListOfType;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.redis.om.spring.RediSearchIndexer;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.id.ULIDIdentifierGenerator;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.om.spring.util.ObjectUtils;
import com.redislabs.modules.rejson.Path;

import io.redisearch.Query;
import io.redisearch.SearchResult;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public class SimpleRedisDocumentRepository<T, ID> extends SimpleKeyValueRepository<T, ID>
    implements RedisDocumentRepository<T, ID> {

  private final Gson gson;
  protected RedisModulesOperations<String> modulesOperations;
  protected EntityInformation<T, ID> metadata;
  protected KeyValueOperations operations;
  protected RediSearchIndexer indexer;
  protected MappingRedisOMConverter mappingConverter;
  private final ULIDIdentifierGenerator generator;

  private RedisMappingContext mappingContext;

  @SuppressWarnings("unchecked")
  public SimpleRedisDocumentRepository( //
      EntityInformation<T, ID> metadata, //
      KeyValueOperations operations, //
      @Qualifier("redisModulesOperations") RedisModulesOperations<?> rmo, //
      RediSearchIndexer keyspaceToIndexMap, //
      RedisMappingContext mappingContext,
      Gson gson) {
    super(metadata, operations);
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.metadata = metadata;
    this.operations = operations;
    this.indexer = keyspaceToIndexMap;
    this.mappingConverter = new MappingRedisOMConverter(null,
        new ReferenceResolverImpl(modulesOperations.getTemplate()));
    this.generator = ULIDIdentifierGenerator.INSTANCE;
    this.gson = gson;
    this.mappingContext = mappingContext;
  }

  @Override
  public Iterable<ID> getIds() {
    String keyspace = indexer.getKeyspaceForEntityClass(metadata.getJavaType());
    Optional<String> maybeSearchIndex = indexer.getIndexName(keyspace);
    List<ID> result = List.of();
    if (maybeSearchIndex.isPresent()) {
      SearchOperations<String> searchOps = modulesOperations.opsForSearch(maybeSearchIndex.get());
      Optional<Field> maybeIdField = ObjectUtils.getIdFieldForEntityClass(metadata.getJavaType());
      String idField = maybeIdField.isPresent() ? maybeIdField.get().getName() : "id";

      Query query = new Query("*");
      query.returnFields(idField);
      SearchResult searchResult = searchOps.search(query);

      result = searchResult.docs.stream()
          .map(d -> gson.fromJson(d.get(idField).toString(), metadata.getIdType()))
          .collect(Collectors.toList());
    }

    return result;
  }

  @Override
  public Page<ID> getIds(Pageable pageable) {
    List<ID> ids = Lists.newArrayList(getIds());

    int fromIndex = Long.valueOf(pageable.getOffset()).intValue();
    int toIndex = fromIndex + pageable.getPageSize();

    return new PageImpl<ID>((List<ID>) ids.subList(fromIndex, toIndex), pageable, ids.size());
  }

  @Override
  public void deleteById(ID id, Path path) {
    modulesOperations.opsForJSON().del(getKey(id), path);
  }

  @Override
  public void updateField(T entity, MetamodelField<T, ?> field, Object value) {
    modulesOperations.opsForJSON().set(getKey(metadata.getId(entity)), value,
        Path.of("$." + field.getSearchAlias()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <F> Iterable<F> getFieldsByIds(Iterable<ID> ids, MetamodelField<T, F> field) {
    String[] keys = StreamSupport.stream(ids.spliterator(), false).map(this::getKey).toArray(String[]::new);
    return (Iterable<F>) modulesOperations.opsForJSON()
        .mget(Path.of("$." + field.getSearchAlias()), List.class, keys).stream().flatMap(List::stream)
        .collect(Collectors.toList());
  }

  @Override
  public Long getExpiration(ID id) {
    @SuppressWarnings("unchecked")
    RedisTemplate<String, String> template = (RedisTemplate<String, String>) modulesOperations.getTemplate();
    return template.getExpire(getKey(id));
  }

  @Override
  public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
    Assert.notNull(entities, "The given Iterable of entities must not be null!");
    List<S> saved = new ArrayList<>();

    try (Jedis jedis = modulesOperations.getClient().getJedis()) {
      Pipeline pipeline = jedis.pipelined();

      for (S entity : entities) {
        boolean isNew = metadata.isNew(entity);

        KeyValuePersistentEntity<?, ?> keyValueEntity = mappingConverter.getMappingContext()
            .getRequiredPersistentEntity(ClassUtils.getUserClass(entity));
        Object id = isNew ? generator.generateIdentifierOfType(keyValueEntity.getIdProperty().getTypeInformation())
            : (String) keyValueEntity.getPropertyAccessor(entity).getProperty(keyValueEntity.getIdProperty());
        keyValueEntity.getPropertyAccessor(entity).setProperty(keyValueEntity.getIdProperty(), id);

        String keyspace = keyValueEntity.getKeySpace();
        byte[] objectKey = createKey(keyspace, id.toString());

        processAuditAnnotations(objectKey, entity, isNew);
        Optional<Long> maybeTtl = getTTLForEntity(entity);

        RedisData rdo = new RedisData();
        mappingConverter.write(entity, rdo);

        List<byte[]> args = new ArrayList<>(4);
        args.add(objectKey);
        args.add(SafeEncoder.encode(Path.ROOT_PATH.toString()));
        args.add(SafeEncoder.encode(this.gson.toJson(entity)));
        pipeline.sendCommand(Command.SET, args.toArray(new byte[args.size()][]));

        if (maybeTtl.isPresent()) {
          pipeline.expire(objectKey, maybeTtl.get());
        }

        saved.add(entity);
      }
      pipeline.sync();
    }

    return saved;
  }

  @Override public Iterable<T> bulkLoad(String file) throws IOException {
    Reader reader = null;
    try {
      reader = Files.newBufferedReader(Paths.get(file));
      List<T> entities = gson.fromJson(reader, new GsonListOfType<T>(metadata.getJavaType()));
      return saveAll(entities);
    } finally {
      reader.close();
    }
  }

  private String getKeyspace() {
    return indexer.getKeyspaceForEntityClass(metadata.getJavaType());
  }

  private String getKey(Object id) {
    return getKeyspace() + id.toString();
  }

  public byte[] createKey(String keyspace, String id) {
    return this.mappingConverter.toBytes(keyspace + ":" + id);
  }

  private void processAuditAnnotations(byte[] redisKey, Object item, boolean isNew) {
    var auditClass = isNew ? CreatedDate.class : LastModifiedDate.class;

    List<Field> fields = com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(item.getClass(), auditClass);
    if (!fields.isEmpty()) {
      PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(item);
      fields.forEach(f -> {
        if (f.getType() == Date.class) {
          accessor.setPropertyValue(f.getName(), new Date(System.currentTimeMillis()));
        } else if (f.getType() == LocalDateTime.class) {
          accessor.setPropertyValue(f.getName(), LocalDateTime.now());
        } else if (f.getType() == LocalDate.class) {
          accessor.setPropertyValue(f.getName(), LocalDate.now());
        }
      });
    }
  }

  private Optional<Long> getTTLForEntity(Object entity) {
    KeyspaceConfiguration keyspaceConfig = mappingContext.getMappingConfiguration().getKeyspaceConfiguration();
    if (keyspaceConfig.hasSettingsFor(entity.getClass())) {
      var settings = keyspaceConfig.getKeyspaceSettings(entity.getClass());

      if (org.springframework.util.StringUtils.hasText(settings.getTimeToLivePropertyName())) {
        Method ttlGetter;
        try {
          Field fld = ReflectionUtils.findField(entity.getClass(), settings.getTimeToLivePropertyName());
          ttlGetter = ObjectUtils.getGetterForField(entity.getClass(), fld);
          Long ttlPropertyValue = ((Number) ReflectionUtils.invokeMethod(ttlGetter, entity)).longValue();

          ReflectionUtils.invokeMethod(ttlGetter, entity);

          if (ttlPropertyValue != null) {
            TimeToLive ttl = fld.getAnnotation(TimeToLive.class);
            if (!ttl.unit().equals(TimeUnit.SECONDS)) {
              return Optional.of(TimeUnit.SECONDS.convert(ttlPropertyValue, ttl.unit()));
            } else {
              return Optional.of(ttlPropertyValue);
            }
          }
        } catch (SecurityException | IllegalArgumentException e) {
          return Optional.empty();
        }
      } else if (settings != null && settings.getTimeToLive() != null && settings.getTimeToLive() > 0) {
        return Optional.of(settings.getTimeToLive());
      }
    }
    return Optional.empty();
  }

  private enum Command implements ProtocolCommand {
    SET("JSON.SET");

    private final byte[] raw;

    Command(String alt) {
      this.raw = SafeEncoder.encode(alt);
    }

    public byte[] getRaw() {
      return this.raw;
    }
  }
}
