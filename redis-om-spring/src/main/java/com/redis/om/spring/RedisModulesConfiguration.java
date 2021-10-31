package com.redis.om.spring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.mapping.RedisMappingContext;

import com.redis.om.spring.annotations.Bloom;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.annotations.GeoIndexed;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.annotations.TextIndexed;
import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.pds.BloomOperations;
import com.redis.om.spring.ops.search.SearchOperations;

import io.redisearch.FieldName;
import io.redisearch.Schema;
import io.redisearch.Schema.Field;
import io.redisearch.Schema.FieldType;
import io.redisearch.Schema.TextField;
import io.redisearch.client.Client;
import io.redisearch.client.Client.IndexOptions;
import io.redisearch.client.IndexDefinition;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RedisProperties.class)
@EnableAspectJAutoProxy
@ComponentScan("com.redis.spring.bloom")
public class RedisModulesConfiguration extends CachingConfigurerSupport {

  @Bean(name = "redisModulesClient")
  RedisModulesClient redisModulesClient(JedisConnectionFactory jedisConnectionFactory) {
    return new RedisModulesClient(jedisConnectionFactory);
  }

  @Bean(name = "redisModulesOperations")
  RedisModulesOperations<?, ?> redisModulesOperations(RedisModulesClient rmc) {
    return new RedisModulesOperations<>(rmc);
  }

  @Bean(name = "redisJSONOperations")
  JSONOperations<?> redisJSONOperations(RedisModulesOperations<?, ?> redisModulesOperations) {
    return redisModulesOperations.opsForJSON();
  }

  @Bean(name = "redisBloomOperations")
  BloomOperations<?> redisBloomOperations(RedisModulesOperations<?, ?> redisModulesOperations) {
    return redisModulesOperations.opsForBloom();
  }

  @Bean(name = "redisTemplate")
  @Primary
  public RedisTemplate<?, ?> redisTemplate(JedisConnectionFactory connectionFactory) {
    RedisTemplate<?, ?> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    return template;
  }

  @Bean(name = "redisJSONKeyValueAdapter")
  RedisJSONKeyValueAdapter getRedisJSONKeyValueAdapter(RedisOperations<?, ?> redisOps,
      JSONOperations<?> redisJSONOperations) {
    return new RedisJSONKeyValueAdapter(redisOps, redisJSONOperations);
  }

  @Bean(name = "redisJSONKeyValueTemplate")
  public CustomRedisKeyValueTemplate getRedisJSONKeyValueTemplate(RedisOperations<?, ?> redisOps,
      JSONOperations<?> redisJSONOperations) {
    RedisMappingContext mappingContext = new RedisMappingContext();
    return new CustomRedisKeyValueTemplate(getRedisJSONKeyValueAdapter(redisOps, redisJSONOperations), mappingContext);
  }

  @Bean(name = "redisCustomKeyValueTemplate")
  public CustomRedisKeyValueTemplate getKeyValueTemplate(RedisOperations<?, ?> redisOps,
      JSONOperations<?> redisJSONOperations) {
    RedisMappingContext mappingContext = new RedisMappingContext();
    return new CustomRedisKeyValueTemplate(new RedisEnhancedKeyValueAdapter(redisOps), mappingContext);
  }

  @EventListener(ContextRefreshedEvent.class)
  public void ensureIndexesAreCreated(ContextRefreshedEvent cre) {
    //System.out.println(">>>> On ContextRefreshedEvent ... Creating Indexes......");

    ApplicationContext ac = cre.getApplicationContext();

    @SuppressWarnings("unchecked")
    RedisModulesOperations<String, String> rmo = (RedisModulesOperations<String, String>) ac
        .getBean("redisModulesOperations");

    Set<BeanDefinition> beanDefs = new HashSet<BeanDefinition>();
    beanDefs.addAll(getBeanDefinitionsFor(ac, Document.class));
    beanDefs.addAll(getBeanDefinitionsFor(ac, RedisHash.class));

    for (BeanDefinition beanDef : beanDefs) {
      String indexName = "";
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        indexName = cl.getSimpleName() + "Idx";
        //System.out.printf(">>>> Found @Document annotated class: %s\n", cl.getSimpleName());

        List<Field> fields = new ArrayList<Field>();
        for (java.lang.reflect.Field field : cl.getDeclaredFields()) {
          // Text
          if (field.isAnnotationPresent(TextIndexed.class)) {
            TextIndexed ti = field.getAnnotation(TextIndexed.class);

            FieldName fieldName = FieldName.of("$." + field.getName());
            if (!ObjectUtils.isEmpty(ti.alias())) {
              fieldName = fieldName.as(ti.alias());
            } else {
              fieldName = fieldName.as(field.getName());
            }
            String phonetic = ObjectUtils.isEmpty(ti.phonetic()) ? null : ti.phonetic();
            TextField tf = new TextField(fieldName, ti.weight(), ti.sortable(), ti.nostem(), ti.noindex(), phonetic);

            fields.add(tf);
          }
          // Tag
          if (field.isAnnotationPresent(TagIndexed.class)) {
            TagIndexed ti = field.getAnnotation(TagIndexed.class);

            FieldName fieldName = FieldName.of("$." + field.getName() + "[*]");
            if (!ObjectUtils.isEmpty(ti.alias())) {
              fieldName = fieldName.as(ti.alias());
            } else {
              fieldName = fieldName.as(field.getName());
            }
            
            Field tf = new Field(fieldName, FieldType.Tag, ti.sortable(), ti.noindex());

            fields.add(tf);
          }
          // Geo
          if (field.isAnnotationPresent(GeoIndexed.class)) {
            GeoIndexed gi = field.getAnnotation(GeoIndexed.class);

            FieldName fieldName = FieldName.of("$." + field.getName());
            if (!ObjectUtils.isEmpty(gi.alias())) {
              fieldName = fieldName.as(gi.alias());
            } else {
              fieldName = fieldName.as(field.getName());
            }
            
            Field gf = new Field(fieldName, FieldType.Geo);

            fields.add(gf);
          }
          // Numeric
          if (field.isAnnotationPresent(NumericIndexed.class)) {
            NumericIndexed ni = field.getAnnotation(NumericIndexed.class);

            FieldName fieldName = FieldName.of("$." + field.getName());
            if (!ObjectUtils.isEmpty(ni.alias())) {
              fieldName = fieldName.as(ni.alias());
            } else {
              fieldName = fieldName.as(field.getName());
            }
            
            Field nf = new Field(fieldName, FieldType.Numeric);

            fields.add(nf);
          }
        }

        if (!fields.isEmpty()) {
          Schema schema = new Schema();
          SearchOperations<String> opsForSearch = rmo.opsForSearch(indexName);
          for (Field field : fields) {
            schema.addField(field);
          }
          IndexDefinition def = new IndexDefinition(IndexDefinition.Type.JSON);
          def.setPrefixes(cl.getName() + ":");
          IndexOptions ops = Client.IndexOptions.defaultOptions().setDefinition(def);
          opsForSearch.createIndex(schema, ops);
        }
      } catch (Exception e) {
        System.err.println(
            String.format("Skipping index creation for %s because %s", indexName, e.getMessage()));
      }
    }

  }

  @EventListener(ContextRefreshedEvent.class)
  public void processBloom(ContextRefreshedEvent cre) {
    ApplicationContext ac = cre.getApplicationContext();
    @SuppressWarnings("unchecked")
    RedisModulesOperations<String, String> rmo = (RedisModulesOperations<String, String>) ac
        .getBean("redisModulesOperations");

    Set<BeanDefinition> beanDefs = getBeanDefinitionsFor(ac, Document.class, RedisHash.class);

    for (BeanDefinition beanDef : beanDefs) {
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        for (java.lang.reflect.Field field : cl.getDeclaredFields()) {
          // Text
          if (field.isAnnotationPresent(Bloom.class)) {
            Bloom bloom = field.getAnnotation(Bloom.class);
            BloomOperations<String> ops = rmo.opsForBloom();
            String filterName = !ObjectUtils.isEmpty(bloom.name()) ? bloom.name()
                : String.format("bf:%s:%s", cl.getSimpleName(), field.getName());
            ops.createFilter(filterName, bloom.capacity(), bloom.errorRate());
          }
        }
      } catch (Exception e) {
        System.err.println("In processBloom: Exception: " + e.getMessage());
      }
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Set<BeanDefinition> getBeanDefinitionsFor(ApplicationContext ac, Class... classes) {
    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
    for (Class cls : classes) {
      provider.addIncludeFilter(new AnnotationTypeFilter(cls));
    }

    Map<String, Object> annotatedBeans = ac.getBeansWithAnnotation(SpringBootApplication.class);
    Class<?> app = annotatedBeans.isEmpty() ? null : annotatedBeans.values().toArray()[0].getClass();
    Set<BeanDefinition> beanDefs = new HashSet<BeanDefinition>();
    if (app.isAnnotationPresent(EnableRedisDocumentRepositories.class)) {
      EnableRedisDocumentRepositories edr = (EnableRedisDocumentRepositories) app
          .getAnnotation(EnableRedisDocumentRepositories.class);
      for (String pkg : edr.basePackages()) {
        beanDefs.addAll(provider.findCandidateComponents(pkg));
      }
    }

    return beanDefs;
  }

}
