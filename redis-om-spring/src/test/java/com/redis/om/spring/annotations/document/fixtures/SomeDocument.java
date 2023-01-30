package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @Document public class SomeDocument {
  @NonNull @Id @Indexed protected String id;
  @NonNull @Indexed private String name;

  @Indexed(sortable = true) private LocalDateTime documentCreationDate;
  @Searchable private String description;
  @Indexed private Source source;
  @Searchable private String category;
  @Indexed private Format format;
  @Searchable private String objectStorageKey;
  @Searchable private String searchableContent;

  @SuppressWarnings("unused") public enum Format {
    pdf,
    word,
    text,
    png,
    jpeg
  }

  @SuppressWarnings("unused") public enum Source {
    sourceA,
    sourceB
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    SomeDocument that = (SomeDocument) o;

    if (!id.equals(that.id))
      return false;
    return name.equals(that.name);
  }

  @Override public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }

}
