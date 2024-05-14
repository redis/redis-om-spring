package com.redis.om.spring.metamodel;

import com.redis.om.spring.util.ObjectUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MetamodelUtils {
  public static MetamodelField<?, ?> getMetamodelForIdField(Class<?> entityClass) {
    Optional<Field> idField = ObjectUtils.getIdFieldForEntityClass(entityClass);
    if (idField.isPresent()) {
      try {
        Class<?> metamodel = Class.forName(entityClass.getName() + "$");
        String metamodelField = ObjectUtils.staticField(idField.get().getName());
        Field field = metamodel.getField(metamodelField);
        return (MetamodelField<?, ?>) field.get(null);
      } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  public static List<MetamodelField<?, ?>> getMetamodelFieldsForProperties(Class<?> entityClass,
    Collection<String> properties) {
    List<MetamodelField<?, ?>> result = new ArrayList<>();
    try {
      Class<?> metamodel = Class.forName(entityClass.getName() + "$");
      for (var property : properties) {
        try {
          result.add((MetamodelField<?, ?>) metamodel.getField(ObjectUtils.staticField(property)).get(null));
        } catch (IllegalAccessException | NoSuchFieldException e) {
          // NOOP
        }
      }
    } catch (ClassNotFoundException e) {
      // NOOP
    }
    return result;
  }
}
