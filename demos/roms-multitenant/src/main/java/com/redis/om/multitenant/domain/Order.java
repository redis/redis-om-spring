package com.redis.om.multitenant.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.IndexCreationMode;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.annotations.Searchable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order entity with environment-aware dynamic indexing.
 *
 * <p>Demonstrates using environment properties in SpEL expressions for
 * index naming. This is useful for deployments where the same application
 * runs in multiple environments (dev, staging, prod) against the same
 * Redis instance.
 *
 * <p>Example for environment "production":
 * <ul>
 * <li>Index name: orders_production_idx
 * <li>Key prefix: production:orders:
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
@IndexingOptions(
    indexName = "orders_#{@environment.getProperty('app.environment')}_idx",
    keyPrefix = "#{@environment.getProperty('app.environment')}:orders:", creationMode = IndexCreationMode.SKIP_IF_EXIST
)
public class Order {

  @Id
  private String id;

  @Indexed
  private String customerId;

  @Indexed
  private String tenantId;

  @Searchable
  private String customerName;

  @Indexed
  private OrderStatus status;

  @Indexed
  private Double totalAmount;

  @Indexed
  private LocalDateTime orderDate;

  @Indexed
  private LocalDateTime shippedDate;

  private List<OrderItem> items;

  public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class OrderItem {
    private String productId;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
  }
}
