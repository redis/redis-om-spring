package com.redis.om.spring.annotations;

import java.lang.annotation.*;

/**
 * Annotation to configure indexing options for Redis OM entities or repositories.
 * This annotation allows customization of search index creation behavior,
 * including the index name, key prefix, filter expression, and creation mode.
 *
 * <p>When placed on a repository interface, it enables multiple indexes over the same
 * entity type with different filters, prefixes, or index names. This supports use cases
 * such as per-team filtered indexes, blue/green index aliasing, and CQRS patterns.
 *
 * <p>Supports Spring Expression Language (SpEL) for dynamic configuration:
 * <ul>
 * <li>Environment properties: #{&#64;environment.getProperty('app.tenant')}</li>
 * <li>Bean references: #{&#64;tenantResolver.currentTenant}</li>
 * <li>Method invocations: #{&#64;versionService.getVersion()}</li>
 * <li>Conditional logic: #{condition ? 'value1' : 'value2'}</li>
 * </ul>
 */
@Inherited
@Retention(
  RetentionPolicy.RUNTIME
)
@Target(
  { ElementType.TYPE }
)
public @interface IndexingOptions {

  /**
   * Specifies the custom name for the search index. If not provided,
   * a default index name will be generated based on the entity class name.
   *
   * <p>Supports SpEL expressions for dynamic index naming:
   * <pre>
   * &#64;IndexingOptions(indexName = "#{&#64;environment.getProperty('app.tenant')}_idx")
   * &#64;IndexingOptions(indexName = "users_v#{&#64;versionService.getVersion()}")
   * </pre>
   *
   * @return the custom index name, or empty string to use default naming
   */
  String indexName() default "";

  /**
   * Specifies the custom key prefix for Redis keys. If not provided,
   * uses the default prefix from the entity annotation.
   *
   * <p>Supports SpEL expressions for dynamic key prefixes:
   * <pre>
   * &#64;IndexingOptions(keyPrefix = "#{&#64;tenantResolver.currentTenant}:")
   * &#64;IndexingOptions(keyPrefix = "#{&#64;environment.getProperty('app.prefix')}:")
   * </pre>
   *
   * @return the custom key prefix, or empty string to use default
   */
  String keyPrefix() default "";

  /**
   * Specifies multiple key prefixes that this index should cover.
   * When set, the index will be created over all specified prefixes.
   * This is useful for creating indexes that span multiple key namespaces.
   *
   * <p>If both {@link #keyPrefix()} and {@code prefixes()} are specified,
   * {@code prefixes()} takes precedence.
   *
   * @return array of key prefixes for the index, or empty array to use default
   */
  String[] prefixes() default {};

  /**
   * Specifies a RediSearch filter expression for the index.
   * When set, only documents matching this filter will be included in the index.
   * This enables creating filtered/subset indexes over the same entity type.
   *
   * <p><b>Important:</b> This filter uses the RediSearch <i>aggregation expression
   * language</i> (as accepted by the {@code FT.CREATE ... FILTER} command), not the
   * {@code FT.SEARCH} query syntax. For example, to match a tag field, use
   * {@code @team=="TeamA"}, not the query form {@code @team:&#123;TeamA&#125;}.
   *
   * <p>Example usage for per-team indexes:
   * <pre>
   * &#64;IndexingOptions(indexName = "ticket_team_a_idx", filter = "&#64;team==\"TeamA\"")
   * public interface TeamATicketRepository extends RedisDocumentRepository&lt;Ticket, String&gt; {}
   * </pre>
   *
   * <p>Supports SpEL expressions for dynamic filters.
   *
   * @return the RediSearch filter expression, or empty string for no filter
   */
  String filter() default "";

  /**
   * Specifies the index creation mode that determines how the search index
   * should be created or updated.
   *
   * @return the index creation mode
   */
  IndexCreationMode creationMode() default IndexCreationMode.SKIP_IF_EXIST;
}
