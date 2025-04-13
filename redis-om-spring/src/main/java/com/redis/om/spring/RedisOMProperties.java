package com.redis.om.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.geo.Metrics;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(
    prefix = "redis.om.spring", ignoreInvalidFields = true
)
public class RedisOMProperties {
  public static final String ROMS_VERSION = "0.9.13-SNAPSHOT";
  public static final int MAX_SEARCH_RESULTS = 10000;
  public static final double DEFAULT_DISTANCE = 0.0005;
  public static final Metrics DEFAULT_DISTANCE_METRIC = Metrics.MILES;
  // repository properties
  private final Repository repository = new Repository();
  private final References references = new References();
  // Entra ID Authentication
  private final Authentication authentication = new Authentication();

  public Authentication getAuthentication() {
    return authentication;
  }


  public Repository getRepository() {
    return repository;
  }

  public References getReferences() {
    return references;
  }

  public static class Authentication {
    private EntraId entraId = new EntraId();

    public EntraId getEntraId() {
      return entraId;
    }

    public void setEntraId(EntraId entraId) {
      this.entraId = entraId;
    }
  }

  public static class EntraId {
    private boolean enabled = false;
    private String tenantId;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getTenantId() {
      return tenantId;
    }

    public void setTenantId(String tenantId) {
      this.tenantId = tenantId;
    }
  }

  public static class Repository {
    private final Query query = new Query();
    private boolean dropAndRecreateIndexOnDeleteAll = false;
    private int deleteBatchSize = 500;

    public Query getQuery() {
      return query;
    }

    public boolean isDropAndRecreateIndexOnDeleteAll() {
      return dropAndRecreateIndexOnDeleteAll;
    }

    public void setDropAndRecreateIndexOnDeleteAll(boolean dropAndRecreateIndexOnDeleteAll) {
      this.dropAndRecreateIndexOnDeleteAll = dropAndRecreateIndexOnDeleteAll;
    }

    public int getDeleteBatchSize() {
      return deleteBatchSize;
    }

    public void setDeleteBatchSize(int deleteBatchSize) {
      this.deleteBatchSize = deleteBatchSize;
    }

    public static class Query {
      private int limit = MAX_SEARCH_RESULTS;
      private double defaultDistance = DEFAULT_DISTANCE;
      private Metrics defaultDistanceMetric = DEFAULT_DISTANCE_METRIC;

      public int getLimit() {
        return limit;
      }

      public void setLimit(int limit) {
        this.limit = limit;
      }

      public double getDefaultDistance() {
        return defaultDistance;
      }

      public void setDefaultDistance(double defaultDistance) {
        this.defaultDistance = defaultDistance;
      }

      public Metrics getDefaultDistanceMetrics() {
        return defaultDistanceMetric;
      }

      public void setDefaultDistanceMetric(Metrics defaultDistanceMetric) {
        this.defaultDistanceMetric = defaultDistanceMetric;
      }
    }
  }

  public static class References {
    private String cacheName = "roms-reference-cache";
    private List<String> cachedReferenceClasses = new ArrayList<>();

    public String getCacheName() {
      return cacheName;
    }

    public void setCacheName(String cacheName) {
      this.cacheName = cacheName;
    }

    public List<String> getCachedReferenceClasses() {
      return cachedReferenceClasses;
    }

    public void setCachedReferenceClasses(List<String> cachedReferenceClasses) {
      this.cachedReferenceClasses = cachedReferenceClasses;
    }
  }
}
