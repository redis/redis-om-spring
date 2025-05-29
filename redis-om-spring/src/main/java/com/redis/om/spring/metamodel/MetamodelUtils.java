
package com.redis.om.spring.metamodel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.redis.om.spring.util.ObjectUtils;

/**
 * Utility class for working with metamodel fields and entities.
 */
public class MetamodelUtils {

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private MetamodelUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Gets the metamodel field for the ID field of the given entity class.
   *
   * @param entityClass the entity class to get the ID metamodel field for
   * @return the metamodel field for the ID, or null if not found
   */
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

  /**
   * Gets metamodel fields for the specified properties of the given entity class.
   *
   * @param entityClass the entity class to get metamodel fields for
   * @param properties  the collection of property names to get metamodel fields for
   * @return a list of metamodel fields corresponding to the specified properties
   */
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
