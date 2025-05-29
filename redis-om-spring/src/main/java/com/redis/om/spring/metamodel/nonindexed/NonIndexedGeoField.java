package com.redis.om.spring.metamodel.nonindexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;

/**
 * Represents a non-indexed geographic field in the Redis OM metamodel system.
 * This class provides a base for geospatial fields that are stored in Redis
 * but not included in search indexes. Geographic fields typically store
 * coordinate data such as longitude/latitude pairs or Point objects.
 * 
 * @param <E> the entity type that contains this field
 * @param <T> the field value type (typically Point or coordinate data)
 */
public class NonIndexedGeoField<E, T> extends MetamodelField<E, T> {
  /**
   * Constructs a new NonIndexedGeoField.
   * 
   * @param field   the search field accessor for this field
   * @param indexed whether this field is indexed (should be false for non-indexed fields)
   */
  public NonIndexedGeoField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }
}
