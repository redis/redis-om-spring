package com.redis.spring.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.repository.config.QueryCreatorType;
import org.springframework.data.redis.core.RedisKeyValueAdapter.EnableKeyspaceEvents;
import org.springframework.data.redis.core.RedisKeyValueAdapter.ShadowCopy;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.index.IndexConfiguration;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.repository.support.RedisRepositoryFactoryBean;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;

import com.redis.spring.repository.configuration.RedisJSONRepositoriesRegistrar;
import com.redis.spring.repository.query.RediSearchQuery;
import com.redis.spring.repository.query.RediSearchQueryCreator;
import com.redis.spring.repository.support.RedisDocumentRepositoryFactoryBean;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(RedisJSONRepositoriesRegistrar.class)
@QueryCreatorType(value = RediSearchQueryCreator.class, repositoryQueryType = RediSearchQuery.class)
public @interface EnableRedisDocumentRepositories {

  /**
   * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
   * {@code @EnableRedisRepositories("org.my.pkg")} instead of
   * {@code @EnableRedisRepositories(basePackages="org.my.pkg")}.
   */
  @AliasFor("basePackages")
  String[] value() default {};

  /**
   * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually exclusive with) this
   * attribute. Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
   */
  @AliasFor("value")
  String[] basePackages() default {};

  /**
   * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for annotated components. The
   * package of each class specified will be scanned. Consider creating a special no-op marker class or interface in
   * each package that serves no purpose other than being referenced by this attribute.
   */
  Class<?>[] basePackageClasses() default {};

  /**
   * Specifies which types are not eligible for component scanning.
   */
  Filter[] excludeFilters() default {};

  /**
   * Specifies which types are eligible for component scanning. Further narrows the set of candidate components from
   * everything in {@link #basePackages()} to everything in the base packages that matches the given filter or filters.
   */
  Filter[] includeFilters() default {};

  /**
   * Returns the postfix to be used when looking up custom repository implementations. Defaults to {@literal Impl}. So
   * for a repository named {@code PersonRepository} the corresponding implementation class will be looked up scanning
   * for {@code PersonRepositoryImpl}.
   *
   * @return
   */
  String repositoryImplementationPostfix() default "Impl";

  /**
   * Configures the location of where to find the Spring Data named queries properties file.
   *
   * @return
   */
  String namedQueriesLocation() default "";

  /**
   * Returns the key of the {@link QueryLookupStrategy} to be used for lookup queries for query methods. Defaults to
   * {@link Key#CREATE_IF_NOT_FOUND}.
   *
   * @return
   */
  Key queryLookupStrategy() default Key.CREATE_IF_NOT_FOUND;

  /**
   * Returns the {@link FactoryBean} class to be used for each repository instance. Defaults to
   * {@link RedisRepositoryFactoryBean}.
   *
   * @return
   */
  Class<?> repositoryFactoryBeanClass() default RedisDocumentRepositoryFactoryBean.class;

  /**
   * Configure the repository base class to be used to create repository proxies for this particular configuration.
   *
   * @return
   */
  Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

  /**
   * Configures the name of the {@link KeyValueOperations} bean to be used with the repositories detected.
   *
   * @return
   */
  String keyValueTemplateRef() default "redisKeyValueTemplate";

  /**
   * Configures whether nested repository-interfaces (e.g. defined as inner classes) should be discovered by the
   * repositories infrastructure.
   */
  boolean considerNestedRepositories() default false;

  /**
   * Configures the bean name of the {@link RedisOperations} to be used. Defaulted to {@literal redisTemplate}.
   *
   * @return
   */
  String redisTemplateRef() default "redisTemplate";

  /**
   * Set up index patterns using simple configuration class.
   *
   * @return
   */
  Class<? extends IndexConfiguration> indexConfiguration() default IndexConfiguration.class;

  /**
   * Set up keyspaces for specific types.
   *
   * @return
   */
  Class<? extends KeyspaceConfiguration> keyspaceConfiguration() default KeyspaceConfiguration.class;

  /**
   * Configure usage of {@link KeyExpirationEventMessageListener}.
   *
   * @return
   * @since 1.8
   */
  EnableKeyspaceEvents enableKeyspaceEvents() default EnableKeyspaceEvents.OFF;

  /**
   * Configuration flag controlling storage of phantom keys (shadow copies) of expiring entities to read them later when
   * publishing {@link org.springframework.data.redis.core.RedisKeyspaceEvent keyspace events}.
   *
   * @return
   * @since 2.4
   */
  ShadowCopy shadowCopy() default ShadowCopy.DEFAULT;

  /**
   * Configure the {@literal notify-keyspace-events} property if not already set. <br />
   * Use an empty {@link String} to keep (<b>not</b> alter) existing server configuration.
   *
   * @return {@literal Ex} by default.
   * @since 1.8
   */
  String keyspaceNotificationsConfigParameter() default "Ex";
}
