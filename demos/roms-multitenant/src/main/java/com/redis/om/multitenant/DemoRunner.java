package com.redis.om.multitenant;

import java.time.Duration;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.redis.om.multitenant.domain.Product;
import com.redis.om.multitenant.services.TenantService;
import com.redis.om.spring.indexing.EphemeralIndexService;
import com.redis.om.spring.indexing.IndexMigrationService;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;

/**
 * Interactive demo runner that showcases all dynamic indexing features.
 *
 * <p>Run the application and watch the console for a visual walkthrough of:
 * <ol>
 * <li>SpEL-based dynamic index naming
 * <li>Multi-tenant index isolation
 * <li>Index migration with aliasing
 * <li>Ephemeral indexes with TTL
 * <li>Bulk index operations
 * </ol>
 *
 * <p>NOTE: This CLI demo is disabled by default. Set demo.cli.enabled=true to enable.
 * The web UI at http://localhost:8080 is the primary demo interface.
 */
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "demo.cli.enabled", havingValue = "true", matchIfMissing = false
)
public class DemoRunner implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoRunner.class);

  private static final String ANSI_RESET = "\u001B[0m";
  private static final String ANSI_GREEN = "\u001B[32m";
  private static final String ANSI_YELLOW = "\u001B[33m";
  private static final String ANSI_BLUE = "\u001B[34m";
  private static final String ANSI_PURPLE = "\u001B[35m";
  private static final String ANSI_CYAN = "\u001B[36m";
  private static final String ANSI_BOLD = "\u001B[1m";

  @Autowired
  private TenantService tenantService;

  @Autowired
  private RediSearchIndexer indexer;

  @Autowired
  private RedisModulesOperations<String> modulesOperations;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private Environment environment;

  @Autowired(
      required = false
  )
  private EphemeralIndexService ephemeralIndexService;

  @Autowired(
      required = false
  )
  private IndexMigrationService indexMigrationService;

  @Override
  public void run(String... args) throws Exception {
    printBanner();

    // Demo 1: SpEL-based Dynamic Index Naming
    demoSpelIndexNaming();

    // Demo 2: Multi-Tenant Index Isolation
    demoMultiTenantIsolation();

    // Demo 3: Environment-based Index Configuration
    demoEnvironmentBasedIndexing();

    // Demo 4: Bulk Index Operations
    demoBulkIndexOperations();

    // Demo 5: Ephemeral Indexes (if service is available)
    if (ephemeralIndexService != null) {
      demoEphemeralIndexes();
    }

    // Demo 6: Index Migration (if service is available)
    if (indexMigrationService != null) {
      demoIndexMigration();
    }

    printSummary();
  }

  private void printBanner() {
    println("");
    println(ANSI_CYAN + ANSI_BOLD + "╔══════════════════════════════════════════════════════════════════╗");
    println("║                                                                  ║");
    println("║     Redis OM Spring - Dynamic Indexing Features Demo            ║");
    println("║                                                                  ║");
    println("╚══════════════════════════════════════════════════════════════════╝" + ANSI_RESET);
    println("");
  }

  private void demoSpelIndexNaming() {
    printSection("1. SpEL-based Dynamic Index Naming");

    println("   Redis OM Spring now supports Spring Expression Language (SpEL) in");
    println("   @IndexingOptions annotations for dynamic index names and key prefixes.");
    println("");
    println(ANSI_YELLOW + "   Example annotation:" + ANSI_RESET);
    println("   @IndexingOptions(");
    println("       indexName = \"products_#{@tenantService.getCurrentTenant()}_idx\",");
    println("       keyPrefix = \"#{@tenantService.getCurrentTenant()}:products:\"");
    println("   )");
    println("");

    // Show what the SpEL resolves to
    tenantService.setCurrentTenant("acme_corp");
    String resolvedIndexName = "products_" + tenantService.getCurrentTenant() + "_idx";
    String resolvedKeyPrefix = tenantService.getCurrentTenant() + ":products:";

    println(ANSI_GREEN + "   Resolved values for tenant 'acme_corp':" + ANSI_RESET);
    println("   - Index Name: " + ANSI_BOLD + resolvedIndexName + ANSI_RESET);
    println("   - Key Prefix: " + ANSI_BOLD + resolvedKeyPrefix + ANSI_RESET);
    println("");
  }

  private void demoMultiTenantIsolation() throws InterruptedException {
    printSection("2. Multi-Tenant Index Isolation");

    println("   Each tenant gets their own isolated search index and key space.");
    println("   Data from one tenant is completely invisible to other tenants.");
    println("");

    String[] tenants = { "acme_corp", "globex_inc", "initech" };

    for (String tenant : tenants) {
      tenantService.setCurrentTenant(tenant);

      println(ANSI_BLUE + "   Creating index for tenant: " + ANSI_BOLD + tenant + ANSI_RESET);

      // Create index for this tenant
      indexer.createIndexFor(Product.class);

      String indexName = indexer.getIndexName(Product.class);
      String keyPrefix = indexer.getKeyspacePrefix(Product.class);

      println("   - Index: " + indexName);
      println("   - Key Prefix: " + keyPrefix);

      // Add sample product for this tenant
      Product product = Product.builder().name("Widget for " + tenant).description("A fantastic widget").category(
          "Electronics").sku("WDG-" + tenant.substring(0, 4).toUpperCase()).price(99.99).stockQuantity(100).active(true)
          .build();

      // Store product in Redis JSON
      String key = keyPrefix + "demo-product";
      modulesOperations.opsForJSON().set(key, product);
      println("   - Stored sample product at: " + ANSI_CYAN + key + ANSI_RESET);
      println("");
    }

    Thread.sleep(500); // Allow indexing

    println(ANSI_GREEN + "   Result: Each tenant has isolated data and search indexes!" + ANSI_RESET);
    println("");
  }

  private void demoEnvironmentBasedIndexing() {
    printSection("3. Environment-based Index Configuration");

    String env = environment.getProperty("app.environment", "development");

    println("   Index names can incorporate environment variables for deployment");
    println("   flexibility. This allows dev/staging/prod to share a Redis instance.");
    println("");
    println(ANSI_YELLOW + "   Example annotation:" + ANSI_RESET);
    println("   @IndexingOptions(");
    println("       indexName = \"orders_#{@environment.getProperty('app.environment')}_idx\"");
    println("   )");
    println("");
    println(ANSI_GREEN + "   Current environment: " + ANSI_BOLD + env + ANSI_RESET);
    println("   Resolved index name: " + ANSI_BOLD + "orders_" + env + "_idx" + ANSI_RESET);
    println("");
  }

  private void demoBulkIndexOperations() {
    printSection("4. Bulk Index Operations");

    println("   New methods for managing all indexes at once:");
    println("");
    println("   - " + ANSI_CYAN + "indexer.createIndexes()" + ANSI_RESET + " - Create all registered indexes");
    println("   - " + ANSI_CYAN + "indexer.dropIndexes()" + ANSI_RESET + "   - Drop all managed indexes");
    println("   - " + ANSI_CYAN + "indexer.listIndexes()" + ANSI_RESET + "   - List all managed index names");
    println("");

    // Show current indexes
    Set<String> indexes = indexer.listIndexes();
    println(ANSI_GREEN + "   Currently managed indexes (" + indexes.size() + "):" + ANSI_RESET);
    indexes.stream().limit(10).forEach(idx -> println("   - " + idx));
    if (indexes.size() > 10) {
      println("   - ... and " + (indexes.size() - 10) + " more");
    }
    println("");
  }

  private void demoEphemeralIndexes() {
    printSection("5. Ephemeral Indexes with TTL");

    println("   Create temporary indexes that auto-delete after a specified time.");
    println("   Perfect for batch processing, analytics, or temporary data.");
    println("");
    println(ANSI_YELLOW + "   Example usage:" + ANSI_RESET);
    println("   ephemeralIndexService.createEphemeralIndex(");
    println("       Product.class,");
    println("       \"temp_analytics_idx\",");
    println("       Duration.ofMinutes(30)");
    println("   );");
    println("");

    // Create an ephemeral index for demo
    String ephemeralIndexName = "temp_demo_idx";
    boolean created = ephemeralIndexService.createEphemeralIndex(Product.class, ephemeralIndexName, Duration.ofSeconds(
        60));

    if (created) {
      println(ANSI_GREEN + "   Created ephemeral index: " + ANSI_BOLD + ephemeralIndexName + ANSI_RESET);
      println("   TTL: 60 seconds (will auto-delete)");
      println("   Is ephemeral: " + ephemeralIndexService.isEphemeralIndex(ephemeralIndexName));
    }
    println("");
  }

  private void demoIndexMigration() {
    printSection("6. Index Migration with Aliasing");

    println("   Support for zero-downtime index migrations using Redis aliases.");
    println("   Enable Blue-Green deployments for schema changes.");
    println("");
    println(ANSI_YELLOW + "   Migration Strategies:" + ANSI_RESET);
    println("   - BLUE_GREEN: Create new index, switch alias atomically");
    println("   - DUAL_WRITE: Write to both old and new indexes during migration");
    println("   - IN_PLACE: Modify existing index (requires downtime)");
    println("");
    println(ANSI_YELLOW + "   Example workflow:" + ANSI_RESET);
    println("   1. Create versioned index: products_v2_idx");
    println("   2. Reindex data to new index");
    println("   3. Switch alias: products -> products_v2_idx");
    println("   4. Drop old index: products_v1_idx");
    println("");

    // Show alias operations
    println(ANSI_CYAN + "   Available alias operations:" + ANSI_RESET);
    println("   - indexer.createAlias(indexName, aliasName)");
    println("   - indexer.updateAlias(oldIndex, newIndex, aliasName)");
    println("   - indexer.removeAlias(indexName, aliasName)");
    println("");
  }

  private void printSummary() {
    println(ANSI_PURPLE + ANSI_BOLD + "╔══════════════════════════════════════════════════════════════════╗");
    println("║                         Summary                                  ║");
    println("╚══════════════════════════════════════════════════════════════════╝" + ANSI_RESET);
    println("");
    println("   The dynamic indexing features enable:");
    println("");
    println("   " + ANSI_GREEN + "✓" + ANSI_RESET + " Multi-tenant SaaS applications with isolated data");
    println("   " + ANSI_GREEN + "✓" + ANSI_RESET + " Environment-aware deployments (dev/staging/prod)");
    println("   " + ANSI_GREEN + "✓" + ANSI_RESET + " Zero-downtime schema migrations");
    println("   " + ANSI_GREEN + "✓" + ANSI_RESET + " Temporary indexes for batch/analytics workloads");
    println("   " + ANSI_GREEN + "✓" + ANSI_RESET + " Flexible runtime index configuration");
    println("");
    println("   " + ANSI_CYAN + "REST API available at:" + ANSI_RESET);
    println("   - GET  /api/tenants/{tenantId}/products");
    println("   - POST /api/tenants/{tenantId}/products");
    println("   - GET  /api/admin/indexes");
    println("   - POST /api/admin/indexes/create-all");
    println("");
    println(ANSI_YELLOW + "   See the README.md for more examples and API documentation." + ANSI_RESET);
    println("");
  }

  private void printSection(String title) {
    println("");
    println(ANSI_PURPLE + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + ANSI_RESET);
    println(ANSI_PURPLE + ANSI_BOLD + "   " + title + ANSI_RESET);
    println(ANSI_PURPLE + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" + ANSI_RESET);
    println("");
  }

  private void println(String message) {
    System.out.println(message);
  }
}
