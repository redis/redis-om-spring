package com.redis.om.spring.services;

import org.springframework.stereotype.Component;

/**
 * Service for resolving the current tenant in a multi-tenant application.
 * This is a test fixture for demonstrating dynamic index naming based on tenant context.
 */
@Component
public class TenantResolver {

    private static final ThreadLocal<String> currentTenantHolder = ThreadLocal.withInitial(() -> "default");

    /**
     * Get the current tenant identifier.
     *
     * @return the current tenant ID
     */
    public String getCurrentTenant() {
        return currentTenantHolder.get();
    }

    /**
     * Set the current tenant identifier.
     *
     * @param tenantId the tenant ID to set
     */
    public void setCurrentTenant(String tenantId) {
        if (tenantId == null) {
            currentTenantHolder.remove();
        } else {
            currentTenantHolder.set(tenantId);
        }
    }

    /**
     * Clear the current tenant context.
     */
    public void clearTenant() {
        currentTenantHolder.remove();
    }

    /**
     * Check if a specific tenant is the current tenant.
     *
     * @param tenantId the tenant ID to check
     * @return true if the given tenant is the current tenant
     */
    public boolean isCurrentTenant(String tenantId) {
        return getCurrentTenant().equals(tenantId);
    }
}