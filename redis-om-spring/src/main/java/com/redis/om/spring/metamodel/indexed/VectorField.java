package com.redis.om.spring.metamodel.indexed;

import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.geo.NearPredicate;
import com.redis.om.spring.search.stream.predicates.vector.KNNPredicate;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

public class VectorField <E, T> extends MetamodelField<E, T> {
  public VectorField(SearchFieldAccessor field, boolean indexed) {
    super(field, indexed);
  }

  public KNNPredicate<? super E,T> knn(int k, byte[] blobAttribute) {
    return new KNNPredicate<>(searchFieldAccessor,k, blobAttribute);
  }
}
