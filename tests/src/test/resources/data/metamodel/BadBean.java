package valid;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
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

