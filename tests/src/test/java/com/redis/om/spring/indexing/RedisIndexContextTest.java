package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.redis.om.spring.indexing.RedisIndexContext;

public class RedisIndexContextTest {

    @BeforeEach
    void setUp() {
        // Clear any existing context before each test
        RedisIndexContext.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        RedisIndexContext.clearContext();
    }

    @Test
    void testContextCreation() {
        // Given: Context attributes
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("region", "us-east-1");
        attributes.put("tier", "premium");

        // When: Creating a context
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("tenant123")
            .environment("production")
            .attributes(attributes)
            .build();

        // Then: Context should have all properties set
        assertThat(context).isNotNull();
        assertThat(context.getTenantId()).isEqualTo("tenant123");
        assertThat(context.getEnvironment()).isEqualTo("production");
        assertThat(context.getAttribute("region")).isEqualTo("us-east-1");
        assertThat(context.getAttribute("tier")).isEqualTo("premium");
        assertThat(context.getAttribute("nonexistent")).isNull();
    }

    @Test
    void testThreadLocalStorage() {
        // Given: A context
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("tenant456")
            .environment("staging")
            .build();

        // When: Setting the context in thread-local
        RedisIndexContext.setContext(context);

        // Then: The same context should be retrievable
        RedisIndexContext retrieved = RedisIndexContext.getContext();
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getTenantId()).isEqualTo("tenant456");
        assertThat(retrieved.getEnvironment()).isEqualTo("staging");
    }

    @Test
    void testClearContext() {
        // Given: A context is set
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("tenant789")
            .build();
        RedisIndexContext.setContext(context);
        assertThat(RedisIndexContext.getContext()).isNotNull();

        // When: Clearing the context
        RedisIndexContext.clearContext();

        // Then: Context should be null
        assertThat(RedisIndexContext.getContext()).isNull();
    }

    @Test
    void testContextIsolationAcrossThreads() throws InterruptedException {
        // Given: Two different contexts
        RedisIndexContext context1 = RedisIndexContext.builder()
            .tenantId("tenant1")
            .environment("dev")
            .build();

        RedisIndexContext context2 = RedisIndexContext.builder()
            .tenantId("tenant2")
            .environment("prod")
            .build();

        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<String> thread1TenantId = new AtomicReference<>();
        AtomicReference<String> thread2TenantId = new AtomicReference<>();

        // When: Setting different contexts in different threads
        Thread thread1 = new Thread(() -> {
            RedisIndexContext.setContext(context1);
            try {
                Thread.sleep(100); // Simulate some work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            thread1TenantId.set(RedisIndexContext.getContext().getTenantId());
            latch.countDown();
        });

        Thread thread2 = new Thread(() -> {
            RedisIndexContext.setContext(context2);
            try {
                Thread.sleep(100); // Simulate some work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            thread2TenantId.set(RedisIndexContext.getContext().getTenantId());
            latch.countDown();
        });

        thread1.start();
        thread2.start();

        // Then: Each thread should have its own context
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(thread1TenantId.get()).isEqualTo("tenant1");
        assertThat(thread2TenantId.get()).isEqualTo("tenant2");
    }

    @Test
    void testContextWithNullValues() {
        // Given: A context with some null values
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId(null)
            .environment("test")
            .build();

        // When: Setting and retrieving the context
        RedisIndexContext.setContext(context);
        RedisIndexContext retrieved = RedisIndexContext.getContext();

        // Then: Null values should be preserved
        assertThat(retrieved.getTenantId()).isNull();
        assertThat(retrieved.getEnvironment()).isEqualTo("test");
    }

    @Test
    void testContextAttributeOperations() {
        // Given: A context with attributes
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");

        RedisIndexContext context = RedisIndexContext.builder()
            .attributes(attributes)
            .build();

        // When: Adding and retrieving attributes
        context.setAttribute("key2", "value2");
        context.setAttribute("key3", 123);

        // Then: All attributes should be accessible
        assertThat(context.getAttribute("key1")).isEqualTo("value1");
        assertThat(context.getAttribute("key2")).isEqualTo("value2");
        assertThat(context.getAttribute("key3")).isEqualTo(123);
        assertThat(context.getAttributes()).hasSize(3);
    }

    @Test
    void testContextWithRunnable() {
        // Given: A context and a runnable
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("runnable-tenant")
            .build();

        AtomicReference<String> capturedTenant = new AtomicReference<>();

        // When: Running with context
        context.runWithContext(() -> {
            capturedTenant.set(RedisIndexContext.getContext().getTenantId());
        });

        // Then: Context should be available within runnable and cleared after
        assertThat(capturedTenant.get()).isEqualTo("runnable-tenant");
        assertThat(RedisIndexContext.getContext()).isNull();
    }

    @Test
    void testContextWithCallable() throws Exception {
        // Given: A context and a callable
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("callable-tenant")
            .environment("qa")
            .build();

        // When: Calling with context
        String result = context.callWithContext(() -> {
            RedisIndexContext ctx = RedisIndexContext.getContext();
            return ctx.getTenantId() + ":" + ctx.getEnvironment();
        });

        // Then: Context should be available within callable and result returned
        assertThat(result).isEqualTo("callable-tenant:qa");
        assertThat(RedisIndexContext.getContext()).isNull();
    }

    @Test
    void testContextPropagationInNestedCalls() {
        // Given: A context
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("nested-tenant")
            .setAttribute("level", 1)
            .build();

        AtomicReference<String> nestedResult = new AtomicReference<>();

        // When: Making nested calls with context
        context.runWithContext(() -> {
            RedisIndexContext ctx = RedisIndexContext.getContext();
            ctx.setAttribute("level", 2);

            // Nested call
            ctx.runWithContext(() -> {
                RedisIndexContext nestedCtx = RedisIndexContext.getContext();
                nestedResult.set(nestedCtx.getTenantId() + ":" + nestedCtx.getAttribute("level"));
            });
        });

        // Then: Context should propagate through nested calls
        assertThat(nestedResult.get()).isEqualTo("nested-tenant:2");
    }

    @Test
    void testBuilderValidation() {
        // When/Then: Building context without required fields should work (all fields optional)
        RedisIndexContext context = RedisIndexContext.builder().build();
        assertThat(context).isNotNull();
        assertThat(context.getTenantId()).isNull();
        assertThat(context.getEnvironment()).isNull();
        assertThat(context.getAttributes()).isEmpty();
    }

    @Test
    void testConcurrentContextOperations() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(10);

        // When: Multiple threads set and get contexts concurrently
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready

                    RedisIndexContext context = RedisIndexContext.builder()
                        .tenantId("tenant-" + threadId)
                        .build();

                    RedisIndexContext.setContext(context);
                    Thread.sleep(10); // Simulate work

                    RedisIndexContext retrieved = RedisIndexContext.getContext();
                    assertThat(retrieved.getTenantId()).isEqualTo("tenant-" + threadId);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    RedisIndexContext.clearContext();
                    doneLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Then: All threads should complete successfully
        assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();
    }

    @Test
    void testAutoCloseableWithTryWithResources() {
        // Given: A context
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("auto-close-tenant")
            .environment("prod")
            .build();

        // When: Using try-with-resources via activate()
        try (RedisIndexContext ctx = context.activate()) {
            // Then: Context should be active inside the block
            assertThat(RedisIndexContext.getContext()).isNotNull();
            assertThat(RedisIndexContext.getContext().getTenantId()).isEqualTo("auto-close-tenant");
            assertThat(ctx).isSameAs(context);
        }

        // Then: Context should be cleared after the block
        assertThat(RedisIndexContext.getContext()).isNull();
    }

    @Test
    void testAutoCloseableRestoresPreviousContext() {
        // Given: An outer context that is already active
        RedisIndexContext outerContext = RedisIndexContext.builder()
            .tenantId("outer-tenant")
            .build();
        RedisIndexContext.setContext(outerContext);

        // When: Using a nested context with try-with-resources
        RedisIndexContext innerContext = RedisIndexContext.builder()
            .tenantId("inner-tenant")
            .build();

        try (RedisIndexContext ctx = innerContext.activate()) {
            // Then: Inner context should be active
            assertThat(RedisIndexContext.getContext().getTenantId()).isEqualTo("inner-tenant");
        }

        // Then: Outer context should be restored after the block
        assertThat(RedisIndexContext.getContext()).isNotNull();
        assertThat(RedisIndexContext.getContext().getTenantId()).isEqualTo("outer-tenant");
    }

    @Test
    void testAutoCloseableOnException() {
        // Given: A context
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("exception-tenant")
            .build();

        // When: An exception occurs inside try-with-resources
        assertThrows(RuntimeException.class, () -> {
            try (RedisIndexContext ctx = context.activate()) {
                assertThat(RedisIndexContext.getContext().getTenantId()).isEqualTo("exception-tenant");
                throw new RuntimeException("simulated failure");
            }
        });

        // Then: Context should still be cleaned up
        assertThat(RedisIndexContext.getContext()).isNull();
    }

    @Test
    void testThreadPoolContextLeakPrevention() throws Exception {
        // Given: A single-thread executor (simulates thread reuse in a pool)
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // When: First task uses activate() with try-with-resources
        Future<String> firstTask = executor.submit(() -> {
            RedisIndexContext context = RedisIndexContext.builder()
                .tenantId("tenant-A")
                .build();
            try (RedisIndexContext ctx = context.activate()) {
                return RedisIndexContext.getContext().getTenantId();
            }
        });

        // Wait for first task to complete
        assertThat(firstTask.get(5, TimeUnit.SECONDS)).isEqualTo("tenant-A");

        // When: Second task runs on the same thread (thread reuse)
        Future<RedisIndexContext> secondTask = executor.submit(() -> {
            // Should NOT see tenant-A's context
            return RedisIndexContext.getContext();
        });

        // Then: Second task should not see any leaked context
        assertThat(secondTask.get(5, TimeUnit.SECONDS)).isNull();

        executor.shutdown();
    }

    @SuppressWarnings("deprecation")
    @Test
    void testDeprecatedSetContextStillWorks() {
        // Given: A context
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("deprecated-tenant")
            .build();

        // When: Using the deprecated setContext method
        RedisIndexContext.setContext(context);

        // Then: It should still function correctly
        assertThat(RedisIndexContext.getContext()).isNotNull();
        assertThat(RedisIndexContext.getContext().getTenantId()).isEqualTo("deprecated-tenant");

        // Cleanup
        RedisIndexContext.clearContext();
        assertThat(RedisIndexContext.getContext()).isNull();
    }
}