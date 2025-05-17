package com.redis.om.spring.metamodel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.redis.om.spring.util.ObjectUtils;

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

    String metamodelClassName = getMetamodelClassName(entityClass);

    try {
      Class<?> metamodel = Class.forName(metamodelClassName);
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

  private static String getMetamodelClassName(Class<?> entityClass) {
    if (entityClass.isMemberClass()) {
      // For both static and non-static nested classes
      Class<?> enclosingClass = entityClass.getEnclosingClass();
      String enclosingClassName = enclosingClass.getSimpleName();
      String entityClassName = entityClass.getSimpleName();
      return entityClass.getPackage().getName() + "." + enclosingClassName + "_" + entityClassName + "$";
    } else {
      // For top-level classes
      return entityClass.getName() + "$";
    }
  }
}
