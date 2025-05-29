package com.redis.om.spring.repository.query;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;

import com.redis.om.spring.metamodel.MetamodelField;

/**
 * Custom extension of Spring Data's Sort class that provides Redis OM specific sorting capabilities.
 * <p>
 * This class enhances the standard Spring Data Sort functionality by providing integration with
 * Redis OM's metamodel fields. It allows sorting based on field metadata rather than raw string
 * property names, ensuring proper field alias resolution for RediSearch queries.
 * </p>
 * <p>
 * The class provides static factory methods to create Sort instances using MetamodelField
 * references, which are automatically converted to the appropriate search aliases.
 * </p>
 *
 * @see org.springframework.data.domain.Sort
 * @see com.redis.om.spring.metamodel.MetamodelField
 * @since 0.1.0
 */
public class Sort extends org.springframework.data.domain.Sort {

  @Serial
  private static final long serialVersionUID = 7789210988714363618L;

  /**
   * Constructs a new Sort instance with the given list of Sort.Order objects.
   *
   * @param orders the list of ordering instructions, must not be {@literal null}
   */
  protected Sort(List<Order> orders) {
    super(orders);
  }

  /**
   * Creates a new {@link org.springframework.data.domain.Sort} for the given {@link Order}s
   * use {@link com.redis.om.spring.metamodel.MetamodelField}
   *
   * @param direction must not be {@literal null}.
   * @param fields    must not be {@literal null}.
   * @return a Spring Sort object
   */
  public static org.springframework.data.domain.Sort by(Direction direction, MetamodelField<?, ?>... fields) {
    String[] properties = Arrays.stream(fields).map(MetamodelField::getSearchAlias).toArray(String[]::new);
    return org.springframework.data.domain.Sort.by(direction, properties);
  }

  /**
   * Creates a new {@link org.springframework.data.domain.Sort} for the given {@link MetamodelField}s.
   * <p>
   * The fields will be sorted in ascending order by default. This method extracts the search aliases
   * from the metamodel fields and creates a standard Spring Data Sort instance.
   * </p>
   *
   * @param fields the metamodel fields to sort by, must not be {@literal null}
   * @return a Spring Sort object configured with the field aliases
   */
  public static org.springframework.data.domain.Sort by(MetamodelField<?, ?>... fields) {
    String[] properties = Arrays.stream(fields).map(MetamodelField::getSearchAlias).toArray(String[]::new);
    return org.springframework.data.domain.Sort.by(properties);
  }

}
