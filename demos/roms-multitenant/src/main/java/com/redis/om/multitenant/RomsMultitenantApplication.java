package com.redis.om.multitenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;

/**
 * Multi-Tenant Redis OM Spring Demo Application.
 *
 * <p>This demo showcases the dynamic indexing features of Redis OM Spring:
 *
 * <ul>
 * <li>SpEL expressions for dynamic index naming
 * <li>Multi-tenant index isolation using RedisIndexContext
 * <li>Custom IndexResolver implementations
 * <li>Index migration with Blue-Green deployment support
 * <li>Ephemeral indexes with TTL for temporary data
 * <li>Bulk index operations
 * </ul>
 *
 * <p>Use Case: A SaaS e-commerce platform where each tenant (merchant)
 * has isolated product catalogs with their own search indexes.
 */
@SpringBootApplication
@EnableRedisDocumentRepositories(
    basePackages = "com.redis.om.multitenant"
)
public class RomsMultitenantApplication {

  public static void main(String[] args) {
    SpringApplication.run(RomsMultitenantApplication.class, args);
  }
}
