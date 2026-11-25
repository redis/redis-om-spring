package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.SchemaFieldType;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.CommandArguments;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.args.Rawable;
import redis.clients.jedis.search.schemafields.SchemaField;

class IndexDefinitionBuilderNoIndexTest {

  static class Boo {
    @Indexed
    private String indexedField;

    @Indexed(noindex = true)
    private String nonIndexedField;

    @Indexed(noindex = true)
    private Integer nonIndexedCount;
  }

  static class Foo {
    @Id
    private String documentId;

    @Indexed
    private List<Boo> boos;
  }

  static class Bar {
    @Id
    private String id;

    @Indexed(noindex = true)
    private String keyword;

    @TagIndexed(noindex = true)
    private String labels;
  }

  static class Baz {
    @Id
    private String id;

    @Indexed(schemaFieldType = SchemaFieldType.NESTED)
    private List<Boo> boos;
  }

  @Test
  void nestedCollectionFieldsHonorNoIndex() {
    var fields = buildSearchFields(Foo.class);

    assertThat(schemaParams(fields, "boos_indexedField")).doesNotContain("NOINDEX");
    assertThat(schemaParams(fields, "boos_nonIndexedField")).contains("NOINDEX");
    assertThat(schemaParams(fields, "boos_nonIndexedCount")).contains("NOINDEX");
  }

  @Test
  void topLevelFieldsHonorNoIndex() {
    var fields = buildSearchFields(Bar.class);

    assertThat(schemaParams(fields, "keyword")).contains("NOINDEX");
    assertThat(schemaParams(fields, "labels")).contains("NOINDEX");
  }

  @Test
  void explicitNestedSchemaFieldsHonorNoIndex() {
    var fields = buildSearchFields(Baz.class);

    assertThat(schemaParams(fields, "boos_indexedField")).doesNotContain("NOINDEX");
    assertThat(schemaParams(fields, "boos_nonIndexedField")).contains("NOINDEX");
    assertThat(schemaParams(fields, "boos_nonIndexedCount")).contains("NOINDEX");
  }

  private static List<SearchField> buildSearchFields(Class<?> entityClass) {
    var builder = new IndexDefinitionBuilder(new SchemaFieldFactory(), new GsonBuilder(), pair -> {
    }, (entity, fieldName) -> {
    });
    return builder.buildSearchFields(entityClass, true, ObjectUtils.getDeclaredFieldsTransitively(entityClass));
  }

  private static List<String> schemaParams(List<SearchField> fields, String alias) {
    SchemaField schemaField = fields.stream() //
        .map(SearchField::getSchemaField) //
        .filter(field -> alias.equals(field.getFieldName().getAttribute())) //
        .findFirst() //
        .orElseThrow(() -> new AssertionError("No schema field for alias " + alias));

    CommandArguments args = new CommandArguments(Protocol.Command.GET);
    schemaField.addParams(args);

    List<String> params = new ArrayList<>();
    for (Rawable arg : args) {
      params.add(new String(arg.getRaw(), StandardCharsets.UTF_8));
    }
    return params;
  }
}
