package valid;

import com.redis.om.spring.annotations.*;
import org.springframework.data.annotation.Id;

@Document
public class BadBean {
  @Id
  private String id;

  @Searchable(sortable = true)
  private String name;

  @Indexed
  private Integer age;
}

