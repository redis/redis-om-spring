package com.redis.om.multitenant.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for managing tenant context in a multi-tenant application.
 *
 * <p>Uses ThreadLocal to store the current tenant, ensuring thread-safe
 * tenant isolation in multi-threaded environments like web requests.
 *
 * <p>This service is referenced by SpEL expressions in entity annotations:
 * <pre>
 * {@literal @}IndexingOptions(indexName = "products_#{@tenantService.getCurrentTenant()}_idx")
 * </pre>
 */
@Service
public class TenantService {

  private static final Logger log = LoggerFactory.getLogger(TenantService.class);
  private static final String DEFAULT_TENANT = "default";

  private final ThreadLocal<String> currentTenant = ThreadLocal.withInitial(() -> DEFAULT_TENANT);

  /**
   * Gets the current tenant ID for the executing thread.
   *
   * @return the current tenant ID
   */
  public String getCurrentTenant() {
    return currentTenant.get();
  }

  /**
   * Sets the current tenant for the executing thread.
   *
   * @param tenantId the tenant ID to set
   */
  public void setCurrentTenant(String tenantId) {
    log.info(">>> Switching tenant context to: {}", tenantId);
    currentTenant.set(tenantId);
  }

  /**
   * Clears the tenant context for the current thread.
   */
  public void clearTenant() {
    log.info(">>> Clearing tenant context");
    currentTenant.remove();
  }

  /**
   * Executes a runnable within a specific tenant context.
   *
   * @param tenantId the tenant ID
   * @param runnable the code to execute
   */
  public void executeAsTenant(String tenantId, Runnable runnable) {
    String previousTenant = currentTenant.get();
    try {
      setCurrentTenant(tenantId);
      runnable.run();
    } finally {
      if (previousTenant != null) {
        setCurrentTenant(previousTenant);
      } else {
        clearTenant();
      }
    }
  }
}
