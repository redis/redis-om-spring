package com.redis.om.spring.autocomplete;

import com.google.gson.Gson;
import com.redis.om.spring.annotations.AutoComplete;
import com.redis.om.spring.annotations.AutoCompletePayload;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Aspect
@Component
public class AutoCompleteAspect implements Ordered {
  private static final Log logger = LogFactory.getLog(AutoCompleteAspect.class);

  @Autowired
  private Gson gson;

  @Autowired StringRedisTemplate template;

  private final RedisModulesOperations<String> rmo;

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
    var typeArguments = GenericTypeResolver.resolveTypeArguments(repository.getClass(), Repository.class);
    if (typeArguments != null && typeArguments.length > 0) {
      Class<?> entityClass = typeArguments[0];
      for (Field field : entityClass.getDeclaredFields()) {
        if (field.isAnnotationPresent(AutoComplete.class)) {
          String key = String.format(Suggestion.KEY_FORMAT_STRING, entityClass.getSimpleName(), field.getName());
          template.delete(key);
        }
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
          AutoComplete suggestible = field.getAnnotation(AutoComplete.class);
          String key = !ObjectUtils.isEmpty(suggestible.name()) ? suggestible.name()
              : String.format(Suggestion.KEY_FORMAT_STRING, entity.getClass().getSimpleName(), field.getName());

          String payLoadKey = !ObjectUtils.isEmpty(suggestible.name()) ? suggestible.name()
              : String.format(Suggestion.PAYLOAD_KEY_FORMAT_STRING, entity.getClass().getSimpleName(), field.getName());

          try {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
            SearchOperations<String> ops = rmo.opsForSearch(key);
            String suggestion = pd.getReadMethod().invoke(entity).toString();
            ops.deleteSuggestion(key, suggestion);
            template.opsForHash().delete(payLoadKey, suggestion);
          } catch (IllegalArgumentException | IntrospectionException | IllegalAccessException
              | InvocationTargetException e) {
            logger.error("Error while deleting suggestions...", e);
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
        String suggestion = "";
        Map<String, Object> payload = null;

        AutoComplete suggestible = field.getAnnotation(AutoComplete.class);
        String key = !ObjectUtils.isEmpty(suggestible.name()) ? suggestible.name()
            : String.format(Suggestion.KEY_FORMAT_STRING, entity.getClass().getSimpleName(), field.getName());
        SearchOperations<String> ops = rmo.opsForSearch(key);
        try {
          PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
          suggestion = pd.getReadMethod().invoke(entity).toString();
        } catch (IllegalArgumentException | IntrospectionException | IllegalAccessException
            | InvocationTargetException e) {
          logger.error("Error while processing suggestions...", e);
        }

        for (Field field2 : entity.getClass().getDeclaredFields()) {
          if (field2.isAnnotationPresent(AutoCompletePayload.class)) {
            AutoCompletePayload suggestiblePayload = field2.getAnnotation(AutoCompletePayload.class);
            boolean inPayload = (!suggestiblePayload.value().isBlank()
                && suggestiblePayload.value().equalsIgnoreCase(field.getName()))
                || (Arrays.asList(suggestiblePayload.fields()).contains(field.getName()));
            if (inPayload) {
              try {
                payload = payload == null ? new HashMap<>() : payload;
                PropertyDescriptor pd = new PropertyDescriptor(field2.getName(), entity.getClass());
                payload.put(field2.getName(), pd.getReadMethod().invoke(entity));
              } catch (IllegalArgumentException | IntrospectionException | IllegalAccessException
                  | InvocationTargetException e) {
                logger.error("Error while processing suggestions...", e);
              }
            }
          }
        }
        if (payload != null && !payload.isEmpty()) {
          String payLoadKey = !ObjectUtils.isEmpty(suggestible.name()) ? suggestible.name()
              : String.format(Suggestion.PAYLOAD_KEY_FORMAT_STRING, entity.getClass().getSimpleName(), field.getName());
          template.opsForHash().put(payLoadKey, suggestion, gson.toJson(payload));
        }

        ops.addSuggestion(key, suggestion);
      }
    }
  }

  private void deleteSuggestionsForEntity(Object entity) {
    for (Field field : entity.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(AutoComplete.class)) {
        AutoComplete suggestible = field.getAnnotation(AutoComplete.class);
        String key = !ObjectUtils.isEmpty(suggestible.name()) ? suggestible.name()
            : String.format(Suggestion.KEY_FORMAT_STRING, entity.getClass().getSimpleName(), field.getName());
        try {
          PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
          SearchOperations<String> ops = rmo.opsForSearch(key);

          String suggestion = pd.getReadMethod().invoke(entity).toString();

          ops.deleteSuggestion(key, suggestion);

          String payLoadKey = !ObjectUtils.isEmpty(suggestible.name()) ? suggestible.name()
              : String.format("sugg:payload:%s:%s", entity.getClass().getSimpleName(), field.getName());
          template.opsForHash().delete(payLoadKey, suggestion);
        } catch (IllegalArgumentException | IntrospectionException | IllegalAccessException
            | InvocationTargetException e) {
          logger.error("Error while deleting suggestions...", e);
        }
      }
    }
  }
}
