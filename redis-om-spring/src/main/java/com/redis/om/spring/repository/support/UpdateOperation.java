package com.redis.om.spring.repository.support;

import com.redis.om.spring.metamodel.MetamodelField;

public class UpdateOperation {
  final String key;
  final MetamodelField<?, ?> field;
  final Object value;

  UpdateOperation(String key, MetamodelField<?, ?> field, Object value) {
    this.key = key;
    this.field = field;
    this.value = value;
  }
}
