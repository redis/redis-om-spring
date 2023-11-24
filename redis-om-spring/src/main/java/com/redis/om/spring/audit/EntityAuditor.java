package com.redis.om.spring.audit;

import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class EntityAuditor<S> {
    private final RedisOperations<?, ?> redisOperations;

    // Constructor to initialize the EntityAuditor with RedisOperations
    public EntityAuditor(RedisOperations<?, ?> redisOperations) {
        this.redisOperations = redisOperations;
    }

    // Method to process an entity, taking a Redis key and the entity object
    public void processEntity(S redisKey, Object item) {
        // Create an instance of EntityStatusChecker for checking entity status (new or existing)
        EntityStatusChecker<S> statusChecker = new EntityStatusChecker<>(redisOperations);

        // Determine whether the entity is new or existing
        boolean isNew = statusChecker.isNewEntity(redisKey);

        // Update audit fields based on entity status
        updateAuditFields(item, isNew);
    }

    // Private method to update audit fields of an entity
    private void updateAuditFields(Object item, boolean isNew) {
        // Determine the annotation class for audit fields based on entity status
        var auditClass = isNew ? CreatedDate.class : LastModifiedDate.class;

        // Get a list of fields annotated with CreatedDate or LastModifiedDate
        List<Field> fields = com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(item.getClass(), auditClass);

        // If there are annotated fields, update them
        if (!fields.isEmpty()) {
            // Create a PropertyAccessor for the entity object
            PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(item);

            // Update each annotated field with the current timestamp
            fields.forEach(f -> updateAuditField(accessor, f));
        }
    }

    // Private method to update a single audit field
    private void updateAuditField(PropertyAccessor accessor, Field field) {
        // Determine the value to set based on the field type
        Object value = determineAuditFieldValue(field);

        // Set the value of the audit field in the entity object
        accessor.setPropertyValue(field.getName(), value);
    }

    // Private method to determine the value for an audit field based on its type
    private Object determineAuditFieldValue(Field field) {
        if (field.getType() == Date.class) {
            return new Date(System.currentTimeMillis());
        } else if (field.getType() == LocalDateTime.class) {
            return LocalDateTime.now();
        } else if (field.getType() == LocalDate.class) {
            return LocalDate.now();
        }
        return null;
    }
}

// Class responsible for checking the status of an entity (new or existing)
class EntityStatusChecker<S> {
    private final RedisOperations<?, ?> redisOperations;

    // Constructor to initialize the EntityStatusChecker with RedisOperations
    public EntityStatusChecker(RedisOperations<?, ?> redisOperations) {
        this.redisOperations = redisOperations;
    }

    // Method to check if an entity is new based on its Redis key
    public boolean isNewEntity(S redisKey) {
        // Convert the generic Redis key to byte[] (replace with actual conversion logic)
        byte[] keyBytes = convertKeyToBytes(redisKey);

        // Execute a Redis command to check if the key exists
        return (boolean) redisOperations
                .execute((RedisCallback<Object>) connection -> !connection.keyCommands().exists(keyBytes));
    }

    // Helper method to convert the generic Redis key to byte[] (replace with actual conversion logic)
    private byte[] convertKeyToBytes(S redisKey) {
        return (byte[]) redisKey;
    }
}
