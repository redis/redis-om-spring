package com.redis.om.spring.repository.query;

import java.util.Arrays;
import java.util.List;

import com.redis.om.spring.metamodel.MetamodelField;

public class Sort extends org.springframework.data.domain.Sort {

  protected Sort(List<Order> orders) {
    super(orders);
  }
  
  /**
   * Creates a new {@link org.springframework.data.domain.Sort} for the given {@link Order}s
   * use {@link com.redis.om.spring.metamodel.MetamodelField}
   *
   * @param direction must not be {@literal null}.
   * @param fields must not be {@literal null}.
   * @return a Spring Sort object
   */
  public static org.springframework.data.domain.Sort by(Direction direction, MetamodelField<?, ?>... fields) {
    String[] properties = Arrays.asList(fields).stream().map(metamodel ->  metamodel.getSearchAlias()).toArray(String[]::new);
    return org.springframework.data.domain.Sort.by(direction, properties);
  }

  private static final long serialVersionUID = 7789210988714363618L;

}
