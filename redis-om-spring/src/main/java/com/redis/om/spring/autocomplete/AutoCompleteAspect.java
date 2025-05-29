package com.redis.om.spring.autocomplete;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.redis.om.spring.annotations.AutoComplete;
import com.redis.om.spring.annotations.AutoCompletePayload;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;

/**
 * Aspect that automatically manages autocomplete suggestions for Redis entities.
 * This aspect intercepts repository operations (save, delete) and maintains
 * autocomplete dictionaries based on fields annotated with {@link AutoComplete}.
 * 
 * <p>The aspect monitors CRUD operations on entities and automatically:
 * <ul>
 * <li>Adds suggestions when entities are saved</li>
 * <li>Removes suggestions when entities are deleted</li>
 * <li>Manages bulk operations for efficiency</li>
 * <li>Handles payload associations for enhanced suggestions</li>
 * </ul>
 * 
 * <p>AutoComplete suggestions are stored in Redis using the RediSearch autocomplete
 * feature, allowing for fast prefix-based searches with optional scoring and payload.</p>
 * 
 * <p>Example entity configuration:</p>
 * <pre>{@code
 * @Document
 * public class Product {
 * 
 * @AutoComplete(name = "product_names")
 *                    private String name;
 * 
 * @AutoCompletePayload
 *                      private String category;
 *                      }
 *                      }</pre>
 * 
 * @since 1.0
 * @see AutoComplete
 * @see AutoCompletePayload
 */
@Aspect
@Component
public class AutoCompleteAspect implements Ordered {
  private static final Log logger = LogFactory.getLog(AutoCompleteAspect.class);
  final StringRedisTemplate template;
  private final Gson gson;
  private final RedisModulesOperations<String> rmo;

  /**
   * Constructs a new AutoCompleteAspect with the required dependencies.
   * 
   * @param rmo      the Redis modules operations for executing RediSearch commands
   * @param gson     the JSON serializer for handling payload data
   * @param template the Redis template for basic Redis operations
   */
  public AutoCompleteAspect(RedisModulesOperations<String> rmo, Gson gson, StringRedisTemplate template) {
    this.rmo = rmo;
    this.gson = gson;
    this.template = template;
  }

  /**
   * Pointcut that matches save operations on CrudRepository implementations.
   * This pointcut captures entity save operations to trigger suggestion updates.
   */
  @Pointcut(
    "execution(public * org.springframework.data.repository.CrudRepository+.save(..))"
  )
  public void inCrudRepositorySave() {
  }

  /**
   * Pointcut that matches save operations on RedisDocumentRepository implementations.
   * This provides additional coverage for Redis-specific repository operations.
   */
  @Pointcut(
    "execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.save(..))"
  )
  public void inRedisDocumentRepositorySave() {
  }

  @Pointcut(
    "inCrudRepositorySave() || inRedisDocumentRepositorySave()"
  )
  private void inSaveOperation() {
  }

  /**
   * Processes autocomplete suggestions after an entity save operation.
   * This method extracts autocomplete fields from the saved entity and updates
   * the corresponding suggestion dictionaries in Redis.
   * 
   * @param jp     the join point providing method execution context
   * @param entity the entity that was saved
   */
  @AfterReturning(
    "inSaveOperation() && args(entity,..)"
  )
  public void addSuggestion(JoinPoint jp, Object entity) {
    processSuggestionsForEntity(entity);
  }

  /**
   * Pointcut that matches saveAll operations on CrudRepository implementations.
   * This captures bulk save operations for efficient batch processing.
   */
  @Pointcut(
    "execution(public * org.springframework.data.repository.CrudRepository+.saveAll(..))"
  )
  public void inCrudRepositorySaveAll() {
  }

  /**
   * Pointcut that matches saveAll operations on RedisDocumentRepository implementations.
   * This provides additional coverage for Redis-specific bulk save operations.
   */
  @Pointcut(
    "execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.saveAll(..))"
  )
  public void inRedisDocumentRepositorySaveAll() {
  }

  @Pointcut(
    "inCrudRepositorySaveAll() || inRedisDocumentRepositorySaveAll()"
  )
  private void inSaveAllOperation() {
  }

  /**
   * Processes autocomplete suggestions after a bulk save operation.
   * This method iterates through all saved entities and updates their
   * corresponding suggestion dictionaries in Redis.
   * 
   * @param jp       the join point providing method execution context
   * @param entities the list of entities that were saved
   */
  @AfterReturning(
    "inSaveAllOperation() && args(entities,..)"
  )
  public void addAllSuggestions(JoinPoint jp, List<Object> entities) {
    for (Object entity : entities) {
      processSuggestionsForEntity(entity);
    }
  }

  /**
   * Pointcut that matches delete operations on RedisDocumentRepository implementations.
   * This captures entity deletion operations to trigger suggestion removal.
   */
  @Pointcut(
    "execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.delete(..))"
  )
  public void inRedisDocumentRepositoryDelete() {
  }

  /**
   * Removes autocomplete suggestions after an entity deletion operation.
   * This method extracts autocomplete fields from the deleted entity and removes
   * the corresponding suggestions from Redis dictionaries.
   * 
   * @param jp     the join point providing method execution context
   * @param entity the entity that was deleted
   */
  @AfterReturning(
    "inRedisDocumentRepositoryDelete() && args(entity,..)"
  )
  public void deleteSuggestion(JoinPoint jp, Object entity) {
    deleteSuggestionsForEntity(entity);
  }

  /**
   * Pointcut that matches deleteAll operations on RedisDocumentRepository implementations.
   * This captures bulk deletion operations for clearing suggestion dictionaries.
   */
  @Pointcut(
    "execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.deleteAll())"
  )
  public void inRedisDocumentRepositoryDeleteAll() {
  }

  /**
   * Removes all autocomplete suggestions after a deleteAll operation.
   * This method clears all suggestion dictionaries associated with the repository's entity type.
   * 
   * @param jp the join point providing method execution context
   */
  @AfterReturning(
    "inRedisDocumentRepositoryDeleteAll()"
  )
  public void deleteAllSuggestions(JoinPoint jp) {
    Repository<?, ?> repository = (Repository<?, ?>) jp.getTarget();
    var typeArguments = GenericTypeResolver.resolveTypeArguments(repository.getClass(), Repository.class);
    if (typeArguments != null && typeArguments.length > 0) {
      Class<?> entityClass = typeArguments[0];
      for (Field field : com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(entityClass)) {
        if (field.isAnnotationPresent(AutoComplete.class)) {
          String key = String.format(Suggestion.KEY_FORMAT_STRING, entityClass.getSimpleName(), field.getName());
          template.delete(key);
        }
      }
    }
  }

  /**
   * Pointcut that matches deleteAll operations with entity parameters on RedisDocumentRepository implementations.
   * This captures bulk deletion operations for specific entities.
   */
  @Pointcut(
    "execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.deleteAll(..))"
  )
  public void inRedisDocumentRepositoryDeleteAllEntities() {
  }

  /**
   * Removes autocomplete suggestions for a list of specific entities after deletion.
   * This method iterates through the provided entities and removes their corresponding
   * suggestions from Redis dictionaries.
   * 
   * @param jp       the join point providing method execution context
   * @param entities the list of entities that were deleted
   */
  @AfterReturning(
    "inRedisDocumentRepositoryDeleteAllEntities() && args(entities,..)"
  )
  public void deleteAllSuggestionsFromEntities(JoinPoint jp, List<Object> entities) {
    for (Object entity : entities) {
      for (Field field : com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(entity.getClass())) {
        if (field.isAnnotationPresent(AutoComplete.class)) {
          AutoComplete suggestible = field.getAnnotation(AutoComplete.class);
          String key = !ObjectUtils.isEmpty(suggestible.name()) ?
              suggestible.name() :
              String.format(Suggestion.KEY_FORMAT_STRING, entity.getClass().getSimpleName(), field.getName());

          String payLoadKey = !ObjectUtils.isEmpty(suggestible.name()) ?
              suggestible.name() :
              String.format(Suggestion.PAYLOAD_KEY_FORMAT_STRING, entity.getClass().getSimpleName(), field.getName());

          try {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
            SearchOperations<String> ops = rmo.opsForSearch(key);
            String suggestion = pd.getReadMethod().invoke(entity).toString();
            ops.deleteSuggestion(key, suggestion);
            template.opsForHash().delete(payLoadKey, suggestion);
          } catch (IllegalArgumentException | IntrospectionException | IllegalAccessException |
                   InvocationTargetException e) {
            logger.error("Error while deleting suggestions...", e);
          }
        }
      }
    }
  }

  /**
   * Pointcut that matches deleteById operations on RedisDocumentRepository implementations.
   * This captures deletion by ID operations to trigger suggestion removal.
   */
  @Pointcut(
    "execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.deleteById(..))"
  )
  public void inRedisDocumentRepositoryDeleteById() {
  }

  /**
   * Removes autocomplete suggestions before an entity is deleted by ID.
   * This method retrieves the entity by ID first, then removes its suggestions.
   * Uses @Before to ensure the entity still exists when suggestions are removed.
   * 
   * @param jp the join point providing method execution context
   * @param id the ID of the entity to be deleted
   */
  @SuppressWarnings(
    { "rawtypes", "unchecked" }
  )
  @Before(
    "inRedisDocumentRepositoryDeleteById() && args(id)"
  )
  public void deleteSuggestionById(JoinPoint jp, Object id) {
    CrudRepository repository = (CrudRepository) jp.getTarget();
    Optional<Object> maybeEntity = repository.findById(id.toString());
    if (maybeEntity.isPresent()) {
      Object entity = maybeEntity.get();
      deleteSuggestionsForEntity(entity);
    }
  }

  /**
   * Pointcut that matches deleteAllById operations on RedisDocumentRepository implementations.
   * This captures bulk deletion by ID operations to trigger suggestion removal.
   */
  @Pointcut(
    "execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.deleteAllById(..))"
  )
  public void inRedisDocumentRepositoryDeleteAllById() {
  }

  /**
   * Removes autocomplete suggestions before entities are deleted by their IDs.
   * This method retrieves each entity by ID first, then removes their suggestions.
   * Uses @Before to ensure the entities still exist when suggestions are removed.
   * 
   * @param jp  the join point providing method execution context
   * @param ids the list of IDs of entities to be deleted
   */
  @SuppressWarnings(
    { "rawtypes", "unchecked" }
  )
  @Before(
    "inRedisDocumentRepositoryDeleteAllById() && args(ids,..)"
  )
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
    final List<Field> entityClassFields = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(entity
        .getClass());
    for (Field field : entityClassFields) {
      if (field.isAnnotationPresent(AutoComplete.class)) {
        String suggestion = "";
        Map<String, Object> payload = null;

        AutoComplete suggestible = field.getAnnotation(AutoComplete.class);
        String key = !ObjectUtils.isEmpty(suggestible.name()) ?
            suggestible.name() :
            String.format(Suggestion.KEY_FORMAT_STRING, entity.getClass().getSimpleName(), field.getName());
        SearchOperations<String> ops = rmo.opsForSearch(key);
        try {
          PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
          suggestion = pd.getReadMethod().invoke(entity).toString();
        } catch (IllegalArgumentException | IntrospectionException | IllegalAccessException |
                 InvocationTargetException e) {
          logger.error("Error while processing suggestions...", e);
        }

        for (Field field2 : entityClassFields) {
          if (field2.isAnnotationPresent(AutoCompletePayload.class)) {
            AutoCompletePayload suggestiblePayload = field2.getAnnotation(AutoCompletePayload.class);
            boolean inPayload = (!suggestiblePayload.value().isBlank() && suggestiblePayload.value().equalsIgnoreCase(
                field.getName())) || (Arrays.asList(suggestiblePayload.fields()).contains(field.getName()));
            if (inPayload) {
              try {
                payload = payload == null ? new HashMap<>() : payload;
                PropertyDescriptor pd = new PropertyDescriptor(field2.getName(), entity.getClass());
                payload.put(field2.getName(), pd.getReadMethod().invoke(entity));
              } catch (IllegalArgumentException | IntrospectionException | IllegalAccessException |
                       InvocationTargetException e) {
                logger.error("Error while processing suggestions...", e);
              }
            }
          }
        }
        if (payload != null && !payload.isEmpty()) {
          String payLoadKey = !ObjectUtils.isEmpty(suggestible.name()) ?
              suggestible.name() :
              String.format(Suggestion.PAYLOAD_KEY_FORMAT_STRING, entity.getClass().getSimpleName(), field.getName());
          template.opsForHash().put(payLoadKey, suggestion, gson.toJson(payload));
        }

        ops.addSuggestion(key, suggestion);
      }
    }
  }

  private void deleteSuggestionsForEntity(Object entity) {
    for (Field field : com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(entity.getClass())) {
      if (field.isAnnotationPresent(AutoComplete.class)) {
        AutoComplete suggestible = field.getAnnotation(AutoComplete.class);
        String key = !ObjectUtils.isEmpty(suggestible.name()) ?
            suggestible.name() :
            String.format(Suggestion.KEY_FORMAT_STRING, entity.getClass().getSimpleName(), field.getName());
        try {
          PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
          SearchOperations<String> ops = rmo.opsForSearch(key);

          String suggestion = pd.getReadMethod().invoke(entity).toString();

          ops.deleteSuggestion(key, suggestion);

          String payLoadKey = !ObjectUtils.isEmpty(suggestible.name()) ?
              suggestible.name() :
              String.format("sugg:payload:%s:%s", entity.getClass().getSimpleName(), field.getName());
          template.opsForHash().delete(payLoadKey, suggestion);
        } catch (IllegalArgumentException | IntrospectionException | IllegalAccessException |
                 InvocationTargetException e) {
          logger.error("Error while deleting suggestions...", e);
        }
      }
    }
  }
}
