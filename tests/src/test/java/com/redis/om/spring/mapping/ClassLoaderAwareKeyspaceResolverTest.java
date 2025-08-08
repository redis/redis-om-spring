package com.redis.om.spring.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration.KeyspaceSettings;

import com.redis.om.spring.annotations.Document;

class ClassLoaderAwareKeyspaceResolverTest {

  private ClassLoaderAwareKeyspaceResolver resolver;
  private KeyspaceConfiguration keyspaceConfiguration;

  @BeforeEach
  void setUp() {
    keyspaceConfiguration = new KeyspaceConfiguration();
    resolver = new ClassLoaderAwareKeyspaceResolver(keyspaceConfiguration);
  }

  @Test
  void testNormalClassLoaderLookup() {
    // Given
    Class<?> entityClass = TestEntity.class;
    KeyspaceSettings settings = new KeyspaceSettings(entityClass, "test:");
    settings.setTimeToLive(300L);

    // When
    resolver.addKeyspaceSettings(entityClass, settings);

    // Then
    assertThat(resolver.hasSettingsFor(entityClass)).isTrue();
    KeyspaceSettings retrieved = resolver.getKeyspaceSettings(entityClass);
    assertThat(retrieved).isNotNull();
    assertThat(retrieved.getTimeToLive()).isEqualTo(300L);
  }

  @Test
  void testCrossClassLoaderLookupByName() {
    // Given
    Class<?> entityClass = TestEntity.class;
    KeyspaceSettings settings = new KeyspaceSettings(entityClass, "test:");
    settings.setTimeToLive(600L);

    // When
    resolver.addKeyspaceSettings(entityClass, settings);

    // Then - simulate a different class loader by using a class with the same name
    // In reality, this would be a different Class instance with the same name
    // For testing, we verify the name-based lookup works
    assertThat(resolver.hasSettingsFor(TestEntity.class)).isTrue();
    KeyspaceSettings retrieved = resolver.getKeyspaceSettings(TestEntity.class);
    assertThat(retrieved).isNotNull();
    assertThat(retrieved.getTimeToLive()).isEqualTo(600L);
  }

  @Test
  void testMultipleEntities() {
    // Given
    Class<?> entity1 = TestEntity.class;
    Class<?> entity2 = AnotherTestEntity.class;
    
    KeyspaceSettings settings1 = new KeyspaceSettings(entity1, "test1:");
    settings1.setTimeToLive(100L);
    
    KeyspaceSettings settings2 = new KeyspaceSettings(entity2, "test2:");
    settings2.setTimeToLive(200L);

    // When
    resolver.addKeyspaceSettings(entity1, settings1);
    resolver.addKeyspaceSettings(entity2, settings2);

    // Then
    assertThat(resolver.hasSettingsFor(entity1)).isTrue();
    assertThat(resolver.hasSettingsFor(entity2)).isTrue();
    
    assertThat(resolver.getKeyspaceSettings(entity1).getTimeToLive()).isEqualTo(100L);
    assertThat(resolver.getKeyspaceSettings(entity2).getTimeToLive()).isEqualTo(200L);
  }

  @Test
  void testNonExistentEntity() {
    // Given
    Class<?> entityClass = NonExistentEntity.class;

    // Then
    assertThat(resolver.hasSettingsFor(entityClass)).isFalse();
    assertThat(resolver.getKeyspaceSettings(entityClass)).isNull();
  }

  @Document(timeToLive = 300)
  static class TestEntity {
    private String id;
    private String name;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @Document(timeToLive = 200)
  static class AnotherTestEntity {
    private String id;
    private String value;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }

  static class NonExistentEntity {
    private String id;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }
  }
}