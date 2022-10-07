package com.redis.om.spring.autocomplete;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.redis.om.spring.annotations.AutoComplete;
import com.redis.om.spring.annotations.AutoCompletePayload;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
// import com.redis.om.spring.serialization.gson.GsonBuidlerFactory;

import io.redisearch.Suggestion;

@Aspect
@Component
public class AutoCompleteAspect implements Ordered {
  @Autowired
  private Gson gson;
  
  private RedisModulesOperations<String> rmo;

  public AutoCompleteAspect(RedisModulesOperations<String> rmo) {
    this.rmo = rmo;
  }

  @Pointcut("execution(public * org.springframework.data.repository.CrudRepository+.save(..))")
  public void inCrudRepositorySave() {
  }

  @Pointcut("execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.save(..))")
  public void inRedisDocumentRepositorySave() {
  }

  @Pointcut("inCrudRepositorySave() || inRedisDocumentRepositorySave()")
  private void inSaveOperation() {
  }

  @AfterReturning("inSaveOperation() && args(entity,..)")
  public void addSuggestion(JoinPoint jp, Object entity) {
    processSuggestionsForEntity(entity);
  }

  @Pointcut("execution(public * org.springframework.data.repository.CrudRepository+.saveAll(..))")
  public void inCrudRepositorySaveAll() {
  }

  @Pointcut("execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.saveAll(..))")
  public void inRedisDocumentRepositorySaveAll() {
  }

  @Pointcut("inCrudRepositorySaveAll() || inRedisDocumentRepositorySaveAll()")
  private void inSaveAllOperation() {
  }

  @AfterReturning("inSaveAllOperation() && args(entities,..)")
  public void addAllSuggestions(JoinPoint jp, List<Object> entities) {
    for (Object entity : entities) {
      processSuggestionsForEntity(entity);
    }
  }

  @Pointcut("execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.delete(..))")
  public void inRedisDocumentRepositoryDelete() {
  }

  @AfterReturning("inRedisDocumentRepositoryDelete() && args(entity,..)")
  public void deleteSuggestion(JoinPoint jp, Object entity) {
    deleteSuggestionsForEntity(entity);
  }

  @Pointcut("execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.deleteAll())")
  public void inRedisDocumentRepositoryDeleteAll() {
  }

  @AfterReturning("inRedisDocumentRepositoryDeleteAll()")
  public void deleteAllSuggestions(JoinPoint jp) {
    Repository<?, ?> repository = (Repository<?, ?>) jp.getTarget();
    Class<?> entityClass = GenericTypeResolver.resolveTypeArguments(repository.getClass(), Repository.class)[0];
    for (Field field : entityClass.getDeclaredFields()) {
      if (field.isAnnotationPresent(AutoComplete.class)) {
        String key = String.format("sugg:%s:%s", entityClass.getSimpleName(), field.getName());
        @SuppressWarnings("unchecked")
        RedisTemplate<String, String> template = (RedisTemplate<String, String>) rmo.getTemplate();
        template.delete(key);
      }
    }
  }

  @Pointcut("execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.deleteAll(..))")
  public void inRedisDocumentRepositoryDeleteAllEntities() {
  }

  @AfterReturning("inRedisDocumentRepositoryDeleteAllEntities() && args(entities,..)")
  public void deleteAllSuggestionsFromEntities(JoinPoint jp, List<Object> entities) {
    for (Object entity : entities) {
      for (Field field : entity.getClass().getDeclaredFields()) {
        if (field.isAnnotationPresent(AutoComplete.class)) {
          AutoComplete suggestable = field.getAnnotation(AutoComplete.class);
          String key = !ObjectUtils.isEmpty(suggestable.name()) ? suggestable.name()
              : String.format("sugg:%s:%s", entity.getClass().getSimpleName(), field.getName());
          try {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
            SearchOperations<String> ops = rmo.opsForSearch(key);

            ops.deleteSuggestion(pd.getReadMethod().invoke(entity).toString());
          } catch (IllegalArgumentException | IntrospectionException | IllegalAccessException
              | InvocationTargetException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  @Pointcut("execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.deleteById(..))")
  public void inRedisDocumentRepositoryDeleteById() {
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Before("inRedisDocumentRepositoryDeleteById() && args(id)")
  public void deleteSuggestionById(JoinPoint jp, Object id) {
    CrudRepository repository = (CrudRepository) jp.getTarget();
    Optional<Object> maybeEntity = repository.findById(id.toString());
    if (maybeEntity.isPresent()) {
      Object entity = maybeEntity.get();
      deleteSuggestionsForEntity(entity);
    }
  }

  @Pointcut("execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.deleteAllById(..))")
  public void inRedisDocumentRepositoryDeleteAllById() {
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Before("inRedisDocumentRepositoryDeleteAllById() && args(ids,..)")
  public void deleteAllSuggestionByIds(JoinPoint jp, List<Object> ids) {
    CrudRepository repository = (CrudRepository) jp.getTarget();

    for (Object id : ids) {
      Optional<Object> maybeEntity = repository.findById(id.toString());
      if (maybeEntity.isPresent()) {
        Object entity = maybeEntity.get();
        deleteSuggestionsForEntity(entity);
      }
    }
  }

  @Override
  public int getOrder() {
    return 1;
  }

  private void processSuggestionsForEntity(Object entity) {
    for (Field field : entity.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(AutoComplete.class)) {
        Suggestion.Builder builder = Suggestion.builder();
        Map<String, Object> payload = null;

        AutoComplete suggestable = field.getAnnotation(AutoComplete.class);
        String key = !ObjectUtils.isEmpty(suggestable.name()) ? suggestable.name()
            : String.format("sugg:%s:%s", entity.getClass().getSimpleName(), field.getName());
        SearchOperations<String> ops = rmo.opsForSearch(key);
        try {
          PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
          builder.str(pd.getReadMethod().invoke(entity).toString());
        } catch (IllegalArgumentException | IntrospectionException | IllegalAccessException
            | InvocationTargetException e) {
          e.printStackTrace();
        }

        for (Field field2 : entity.getClass().getDeclaredFields()) {
          if (field2.isAnnotationPresent(AutoCompletePayload.class)) {
            AutoCompletePayload suggestablePayload = field2.getAnnotation(AutoCompletePayload.class);
            boolean inPayload = (!suggestablePayload.value().isBlank()
                && suggestablePayload.value().equalsIgnoreCase(field.getName()))
                || (Arrays.asList(suggestablePayload.fields()).contains(field.getName()));
            if (inPayload) {
              try {
                if (payload == null) {
                  payload = new HashMap<>();
                }
                PropertyDescriptor pd = new PropertyDescriptor(field2.getName(), entity.getClass());
                payload.put(field2.getName(), pd.getReadMethod().invoke(entity));
              } catch (IllegalArgumentException | IntrospectionException | IllegalAccessException
                  | InvocationTargetException e) {
                e.printStackTrace();
              }
            }
          }
        }
        if (payload != null && !payload.isEmpty()) {
          builder.payload(gson.toJson(payload));
        }

        ops.addSuggestion(builder.build(), false);
      }
    }
  }

  private void deleteSuggestionsForEntity(Object entity) {
    for (Field field : entity.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(AutoComplete.class)) {
        AutoComplete suggestable = field.getAnnotation(AutoComplete.class);
        String key = !ObjectUtils.isEmpty(suggestable.name()) ? suggestable.name()
            : String.format("sugg:%s:%s", entity.getClass().getSimpleName(), field.getName());
        try {
          PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
          SearchOperations<String> ops = rmo.opsForSearch(key);

          ops.deleteSuggestion(pd.getReadMethod().invoke(entity).toString());
        } catch (IllegalArgumentException | IntrospectionException | IllegalAccessException
            | InvocationTargetException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
