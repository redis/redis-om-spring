package com.redis.om.spring.repository.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.id.ULIDIdentifierGenerator;
import com.redis.om.spring.serialization.gson.GsonBuidlerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;
import org.springframework.data.repository.core.EntityInformation;

import com.redis.om.spring.KeyspaceToIndexMap;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redislabs.modules.rejson.Path;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public class SimpleRedisDocumentRepository<T, ID> extends SimpleKeyValueRepository<T, ID> implements RedisDocumentRepository<T, ID> {

  GsonBuilder gsonBuilder = GsonBuidlerFactory.getBuilder();
  private final Gson gson;
  protected RedisModulesOperations<String> modulesOperations;
  protected EntityInformation<T, ID> metadata;
  protected KeyValueOperations operations;
  protected KeyspaceToIndexMap keyspaceToIndexMap;
  protected MappingRedisOMConverter mappingConverter;
  private final ULIDIdentifierGenerator generator;

  @SuppressWarnings("unchecked")
  public SimpleRedisDocumentRepository(EntityInformation<T, ID> metadata, //
      KeyValueOperations operations, //
      @Qualifier("redisModulesOperations") RedisModulesOperations<?> rmo, //
      KeyspaceToIndexMap keyspaceToIndexMap) {
    super(metadata, operations);
    this.modulesOperations = (RedisModulesOperations<String>)rmo;
    this.metadata = metadata;
    this.operations = operations;
    this.keyspaceToIndexMap = keyspaceToIndexMap;
    this.mappingConverter = new MappingRedisOMConverter(null,
            new ReferenceResolverImpl(modulesOperations.getTemplate()));
    this.generator = ULIDIdentifierGenerator.INSTANCE;
    this.gson = this.gsonBuilder.create();
  }

  @Override
  public Iterable<ID> getIds() {
    @SuppressWarnings("unchecked")
    RedisTemplate<String,ID> template = (RedisTemplate<String,ID>)modulesOperations.getTemplate();
    SetOperations<String, ID> setOps = template.opsForSet();
    return new ArrayList<>(setOps.members(metadata.getJavaType().getName()));
  }

  @Override
  public Page<ID> getIds(Pageable pageable) {
    @SuppressWarnings("unchecked")
    RedisTemplate<String,ID> template = (RedisTemplate<String,ID>)modulesOperations.getTemplate();
    SetOperations<String, ID> setOps = template.opsForSet();
    List<ID> ids = new ArrayList<>(setOps.members(metadata.getJavaType().getName()));

    int fromIndex = Math.toIntExact(pageable.getOffset());
    int toIndex = fromIndex + pageable.getPageSize();
    
    return new PageImpl<>(ids.subList(fromIndex, toIndex), pageable, ids.size());
  }

  @Override
  public void deleteById(ID id, Path path) {
    Long deletedCount = modulesOperations.opsForJSON().del(getKey(id), path);
    
    if ((deletedCount > 0) && path.equals(Path.ROOT_PATH)) {
      @SuppressWarnings("unchecked")
      RedisTemplate<String,ID> template = (RedisTemplate<String,ID>)modulesOperations.getTemplate();
      SetOperations<String, ID> setOps = template.opsForSet();
      setOps.remove(StringUtils.chop(getKeyspace()), id);
    }
  }

  @Override
  public void updateField(T entity, MetamodelField<T, ?> field, Object value) {
    modulesOperations.opsForJSON().set(getKey(metadata.getId(entity)), value, Path.of("$." + field.getField().getName()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <F> Iterable<F> getFieldsByIds(Iterable<ID> ids, MetamodelField<T, F> field) {
    String[] keys = StreamSupport.stream(ids.spliterator(), false).map(this::getKey).toArray(String[]::new);
    return (Iterable<F>) modulesOperations.opsForJSON().mget(Path.of("$." + field.getField().getName()), List.class, keys).stream().flatMap(List::stream).collect(Collectors.toList());
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
        KeyValuePersistentEntity<?, ?> keyValueEntity = mappingConverter.getMappingContext().getRequiredPersistentEntity(ClassUtils.getUserClass(entity));
        Object id = metadata.isNew(entity) ? generator.generateIdentifierOfType(keyValueEntity.getIdProperty().getTypeInformation()) : (String) keyValueEntity.getPropertyAccessor(entity).getProperty(keyValueEntity.getIdProperty());
        keyValueEntity.getPropertyAccessor(entity).setProperty(keyValueEntity.getIdProperty(), id);

        RedisData rdo = new RedisData();
        mappingConverter.write(entity, rdo);
        byte[] objectKey = createKey(rdo.getKeyspace(), id.toString());

        pipeline.sadd(rdo.getKeyspace(), id.toString());

        List<byte[]> args = new ArrayList<>(4);
        args.add(objectKey);
        args.add(SafeEncoder.encode(Path.ROOT_PATH.toString()));
        args.add(SafeEncoder.encode(this.gson.toJson(entity)));
        pipeline.sendCommand(Command.SET, args.toArray(new byte[args.size()][]));

        if (expires(rdo)) {
          pipeline.expire(objectKey, rdo.getTimeToLive());
        }

        saved.add(entity);
      }
      pipeline.sync();
    }

    return saved;
  }
  
  private String getKeyspace() {
    return keyspaceToIndexMap.getKeyspaceForEntityClass(metadata.getJavaType());
  }
  
  private String getKey(Object id) {
    return getKeyspace() + id.toString();
  }

  public byte[] createKey(String keyspace, String id) {
    return this.mappingConverter.toBytes(keyspace + ":" + id);
  }

  private boolean expires(RedisData data) {
    return data.getTimeToLive() != null && data.getTimeToLive() > 0L;
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
