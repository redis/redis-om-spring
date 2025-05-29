package com.redis.om.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.geo.Metrics;

/**
 * Configuration properties for Redis OM Spring.
 * <p>
 * This class provides centralized configuration for Redis OM Spring functionality,
 * including repository behavior, authentication settings, query parameters, and
 * reference caching options. Properties can be configured using the prefix
 * {@code redis.om.spring} in application configuration files.
 * </p>
 * <p>
 * Example configuration:
 * <pre>
 * redis:
 * om:
 * spring:
 * repository:
 * query:
 * limit: 1000
 * defaultDistance: 0.001
 * dropAndRecreateIndexOnDeleteAll: true
 * authentication:
 * entraId:
 * enabled: true
 * tenantId: "your-tenant-id"
 * references:
 * cacheName: "custom-cache"
 * </pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(
    prefix = "redis.om.spring", ignoreInvalidFields = true
)
public class RedisOMProperties {
  /**
   * The version of Redis OM Spring.
   */
  public static final String ROMS_VERSION = "1.0.0-RC.1";

  /**
   * The maximum number of search results that can be returned by default.
   * This value is used as the default limit for search operations.
   */
  public static final int MAX_SEARCH_RESULTS = 10000;

  /**
   * Default constructor for Redis OM Spring properties.
   * <p>
   * This constructor is used by Spring Boot's configuration property
   * binding mechanism to create and populate the properties instance.
   */
  public RedisOMProperties() {
    // Default constructor for Spring configuration properties
  }

  /**
   * The default distance value used for geospatial queries.
   * This represents a small distance of 0.0005 miles for proximity searches.
   */
  public static final double DEFAULT_DISTANCE = 0.0005;

  /**
   * The default distance metric used for geospatial calculations.
   * Defaults to {@link Metrics#MILES}.
   */
  public static final Metrics DEFAULT_DISTANCE_METRIC = Metrics.MILES;
  // repository properties
  /**
   * Repository configuration settings.
   */
  private final Repository repository = new Repository();

  /**
   * Reference caching configuration settings.
   */
  private final References references = new References();

  // Entra ID Authentication
  /**
   * Authentication configuration settings.
   */
  private final Authentication authentication = new Authentication();

  /**
   * Gets the authentication configuration.
   *
   * @return the authentication configuration
   */
  public Authentication getAuthentication() {
    return authentication;
  }

  /**
   * Gets the repository configuration.
   *
   * @return the repository configuration
   */
  public Repository getRepository() {
    return repository;
  }

  /**
   * Gets the references configuration.
   *
   * @return the references configuration
   */
  public References getReferences() {
    return references;
  }

  /**
   * Configuration properties for authentication settings.
   * <p>
   * This class encapsulates authentication-related configuration options,
   * including Microsoft Entra ID (formerly Azure Active Directory) integration.
   * </p>
   */
  public static class Authentication {
    /**
     * Microsoft Entra ID configuration.
     */
    private EntraId entraId = new EntraId();

    /**
     * Default constructor for Authentication configuration.
     */
    public Authentication() {
      // Default constructor for Spring configuration binding
    }

    /**
     * Gets the Microsoft Entra ID configuration.
     *
     * @return the Entra ID configuration
     */
    public EntraId getEntraId() {
      return entraId;
    }

    /**
     * Sets the Microsoft Entra ID configuration.
     *
     * @param entraId the Entra ID configuration to set
     */
    public void setEntraId(EntraId entraId) {
      this.entraId = entraId;
    }
  }

  /**
   * Configuration properties for Microsoft Entra ID authentication.
   * <p>
   * Microsoft Entra ID (formerly Azure Active Directory) is Microsoft's
   * cloud-based identity and access management service. This configuration
   * allows Redis OM Spring to integrate with Entra ID for authentication.
   * </p>
   */
  public static class EntraId {
    /**
     * Whether Entra ID authentication is enabled.
     */
    private boolean enabled = false;

    /**
     * The tenant ID for the Entra ID instance.
     */
    private String tenantId;

    /**
     * Default constructor for EntraId configuration.
     */
    public EntraId() {
      // Default constructor for Spring configuration binding
    }

    /**
     * Checks if Entra ID authentication is enabled.
     *
     * @return {@code true} if Entra ID authentication is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Sets whether Entra ID authentication is enabled.
     *
     * @param enabled {@code true} to enable Entra ID authentication, {@code false} to disable
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Gets the tenant ID for the Entra ID instance.
     *
     * @return the tenant ID, or {@code null} if not configured
     */
    public String getTenantId() {
      return tenantId;
    }

    /**
     * Sets the tenant ID for the Entra ID instance.
     *
     * @param tenantId the tenant ID to set
     */
    public void setTenantId(String tenantId) {
      this.tenantId = tenantId;
    }
  }

  /**
   * Configuration properties for Redis OM Spring repository behavior.
   * <p>
   * This class contains settings that control how repositories behave,
   * including query settings, index management, and batch operation configurations.
   * </p>
   */
  public static class Repository {
    /**
     * Query configuration settings.
     */
    private final Query query = new Query();

    /**
     * Whether to drop and recreate indexes when performing deleteAll operations.
     * This can improve performance for large delete operations but requires
     * re-indexing afterwards.
     */
    private boolean dropAndRecreateIndexOnDeleteAll = false;

    /**
     * The batch size to use for delete operations.
     * Larger batch sizes can improve performance but may consume more memory.
     */
    private int deleteBatchSize = 500;

    /**
     * Default constructor for Repository configuration.
     */
    public Repository() {
      // Default constructor for Spring configuration binding
    }

    /**
     * Gets the query configuration.
     *
     * @return the query configuration
     */
    public Query getQuery() {
      return query;
    }

    /**
     * Checks if indexes should be dropped and recreated during deleteAll operations.
     *
     * @return {@code true} if indexes should be dropped and recreated, {@code false} otherwise
     */
    public boolean isDropAndRecreateIndexOnDeleteAll() {
      return dropAndRecreateIndexOnDeleteAll;
    }

    /**
     * Sets whether indexes should be dropped and recreated during deleteAll operations.
     *
     * @param dropAndRecreateIndexOnDeleteAll {@code true} to drop and recreate indexes,
     *                                        {@code false} to keep them
     */
    public void setDropAndRecreateIndexOnDeleteAll(boolean dropAndRecreateIndexOnDeleteAll) {
      this.dropAndRecreateIndexOnDeleteAll = dropAndRecreateIndexOnDeleteAll;
    }

    /**
     * Gets the batch size for delete operations.
     *
     * @return the delete batch size
     */
    public int getDeleteBatchSize() {
      return deleteBatchSize;
    }

    /**
     * Sets the batch size for delete operations.
     *
     * @param deleteBatchSize the delete batch size to set (must be positive)
     */
    public void setDeleteBatchSize(int deleteBatchSize) {
      this.deleteBatchSize = deleteBatchSize;
    }

    /**
     * Configuration properties for query behavior.
     * <p>
     * This class contains settings that control default query behavior,
     * including result limits and geospatial distance settings.
     * </p>
     */
    public static class Query {
      /**
       * The maximum number of results to return from search queries.
       */
      private int limit = MAX_SEARCH_RESULTS;

      /**
       * The default distance used for geospatial queries.
       */
      private double defaultDistance = DEFAULT_DISTANCE;

      /**
       * The default distance metric used for geospatial calculations.
       */
      private Metrics defaultDistanceMetric = DEFAULT_DISTANCE_METRIC;

      /**
       * Default constructor for Query configuration.
       */
      public Query() {
        // Default constructor for Spring configuration binding
      }

      /**
       * Gets the maximum number of results to return from search queries.
       *
       * @return the search result limit
       */
      public int getLimit() {
        return limit;
      }

      /**
       * Sets the maximum number of results to return from search queries.
       *
       * @param limit the search result limit to set (must be positive)
       */
      public void setLimit(int limit) {
        this.limit = limit;
      }

      /**
       * Gets the default distance used for geospatial queries.
       *
       * @return the default distance value
       */
      public double getDefaultDistance() {
        return defaultDistance;
      }

      /**
       * Sets the default distance used for geospatial queries.
       *
       * @param defaultDistance the default distance value to set (must be positive)
       */
      public void setDefaultDistance(double defaultDistance) {
        this.defaultDistance = defaultDistance;
      }

      /**
       * Gets the default distance metric used for geospatial calculations.
       *
       * @return the default distance metric
       */
      public Metrics getDefaultDistanceMetrics() {
        return defaultDistanceMetric;
      }

      /**
       * Sets the default distance metric used for geospatial calculations.
       *
       * @param defaultDistanceMetric the default distance metric to set
       */
      public void setDefaultDistanceMetric(Metrics defaultDistanceMetric) {
        this.defaultDistanceMetric = defaultDistanceMetric;
      }
    }
  }

  /**
   * Configuration properties for reference caching behavior.
   * <p>
   * This class contains settings that control how entity references are cached,
   * which can improve performance by reducing database lookups for frequently
   * accessed referenced entities.
   * </p>
   */
  public static class References {
    /**
     * The name of the cache used for storing entity references.
     */
    private String cacheName = "roms-reference-cache";

    /**
     * List of fully qualified class names that should have their references cached.
     */
    private List<String> cachedReferenceClasses = new ArrayList<>();

    /**
     * Default constructor for References configuration.
     */
    public References() {
      // Default constructor for Spring configuration binding
    }

    /**
     * Gets the name of the cache used for storing entity references.
     *
     * @return the cache name
     */
    public String getCacheName() {
      return cacheName;
    }

    /**
     * Sets the name of the cache used for storing entity references.
     *
     * @param cacheName the cache name to set
     */
    public void setCacheName(String cacheName) {
      this.cacheName = cacheName;
    }

    /**
     * Gets the list of class names that should have their references cached.
     *
     * @return the list of cached reference class names
     */
    public List<String> getCachedReferenceClasses() {
      return cachedReferenceClasses;
    }

    /**
     * Sets the list of class names that should have their references cached.
     *
     * @param cachedReferenceClasses the list of cached reference class names to set
     */
    public void setCachedReferenceClasses(List<String> cachedReferenceClasses) {
      this.cachedReferenceClasses = cachedReferenceClasses;
    }
  }
}
