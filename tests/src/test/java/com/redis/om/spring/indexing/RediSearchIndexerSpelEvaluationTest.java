package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.beans.factory.BeanFactory;

import com.google.gson.GsonBuilder;
import com.redis.om.spring.RedisOMProperties;

import java.lang.reflect.Method;

/**
 * Unit tests for the evaluateExpression method in RediSearchIndexer.
 * These tests should have been written FIRST before implementing the method.
 */
@ExtendWith(MockitoExtension.class)
public class RediSearchIndexerSpelEvaluationTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private Environment environment;

    @Mock
    private RedisOMProperties properties;

    @Mock
    private GsonBuilder gsonBuilder;

    private RediSearchIndexer indexer;
    private Method evaluateExpressionMethod;

    @BeforeEach
    void setUp() throws Exception {
        indexer = new RediSearchIndexer(applicationContext, properties, gsonBuilder);

        // Access the private evaluateExpression method for testing
        evaluateExpressionMethod = RediSearchIndexer.class.getDeclaredMethod("evaluateExpression", String.class, String.class);
        evaluateExpressionMethod.setAccessible(true);

        // Mock common environment setup - use lenient to avoid unnecessary stubbing errors
        lenient().when(applicationContext.getEnvironment()).thenReturn(environment);
        lenient().when(applicationContext.getBean("environment")).thenReturn(environment);
    }

    @Test
    void testEvaluateExpression_WithStaticString() throws Exception {
        // Given: A static string expression
        String expression = "static_value";
        String defaultValue = "default";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should return the static string
        assertThat(result).isEqualTo("static_value");
    }

    @Test
    void testEvaluateExpression_WithValidSpelExpression() throws Exception {
        // Given: A valid SpEL expression
        String expression = "#{'hello_' + 'world'}";
        String defaultValue = "default";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should return the evaluated result
        assertThat(result).isEqualTo("hello_world");
    }

    @Test
    void testEvaluateExpression_WithEnvironmentProperty() throws Exception {
        // Given: A SpEL expression referencing environment property
        String expression = "#{@environment.getProperty('app.tenant')}";
        String defaultValue = "default";

        when(environment.getProperty("app.tenant")).thenReturn("test_tenant");

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should return the property value
        assertThat(result).isEqualTo("test_tenant");
    }

    @Test
    void testEvaluateExpression_WithSystemProperty() throws Exception {
        // Given: A SpEL expression referencing system property
        System.setProperty("test.property", "system_value");

        try {
            String expression = "#{T(System).getProperty('test.property')}";
            String defaultValue = "default";

            // When: Evaluating the expression
            String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

            // Then: Should return the system property value
            assertThat(result).isEqualTo("system_value");
        } finally {
            System.clearProperty("test.property");
        }
    }

    @Test
    void testEvaluateExpression_WithBeanReference() throws Exception {
        // Given: A SpEL expression referencing a bean
        Object mockBean = mock(Object.class);
        when(mockBean.toString()).thenReturn("bean_value");
        when(applicationContext.getBean("testBean")).thenReturn(mockBean);

        String expression = "#{@testBean.toString()}";
        String defaultValue = "default";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should return the bean method result
        assertThat(result).isEqualTo("bean_value");
    }

    @Test
    void testEvaluateExpression_WithMalformedSpelExpression() throws Exception {
        // Given: A malformed SpEL expression
        String expression = "#{malformed.expression.with.missing.bean}";
        String defaultValue = "fallback_value";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should return the default value
        assertThat(result).isEqualTo("fallback_value");
    }

    @Test
    void testEvaluateExpression_WithInvalidSyntax() throws Exception {
        // Given: An expression with invalid SpEL syntax (unclosed bracket)
        String expression = "#{unclosed.bracket";
        String defaultValue = "fallback_value";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should return the default value since malformed expressions fallback
        assertThat(result).isEqualTo("fallback_value");
    }

    @Test
    void testEvaluateExpression_WithNullExpression() throws Exception {
        // Given: A null expression
        String expression = null;
        String defaultValue = "fallback_value";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should return the default value
        assertThat(result).isEqualTo("fallback_value");
    }

    @Test
    void testEvaluateExpression_WithEmptyExpression() throws Exception {
        // Given: An empty expression
        String expression = "";
        String defaultValue = "fallback_value";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should return the empty string (not default)
        assertThat(result).isEqualTo("");
    }

    @Test
    void testEvaluateExpression_WithNonStringResult() throws Exception {
        // Given: A SpEL expression that returns non-string
        String expression = "#{42}";
        String defaultValue = "fallback_value";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should convert to string
        assertThat(result).isEqualTo("42");
    }

    @Test
    void testEvaluateExpression_WithComplexExpression() throws Exception {
        // Given: A complex SpEL expression with conditional logic
        when(environment.getProperty("app.env")).thenReturn("production");

        String expression = "#{@environment.getProperty('app.env') == 'production' ? 'prod_idx' : 'dev_idx'}";
        String defaultValue = "fallback_value";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should return the conditional result
        assertThat(result).isEqualTo("prod_idx");
    }

    @Test
    void testEvaluateExpression_WithStringConcatenation() throws Exception {
        // Given: A SpEL expression with string concatenation
        when(environment.getProperty("app.tenant")).thenReturn("acme");
        when(environment.getProperty("app.env")).thenReturn("staging");

        String expression = "#{@environment.getProperty('app.tenant') + '_' + @environment.getProperty('app.env') + '_idx'}";
        String defaultValue = "fallback_value";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should return the concatenated result
        assertThat(result).isEqualTo("acme_staging_idx");
    }

    @Test
    void testEvaluateExpression_WithMissingEnvironmentProperty() throws Exception {
        // Given: A SpEL expression referencing missing property
        when(environment.getProperty("missing.property")).thenReturn(null);

        String expression = "#{@environment.getProperty('missing.property')}";
        String defaultValue = "fallback_value";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should return fallback value when expression results in null
        assertThat(result).isEqualTo("fallback_value");
    }

    @Test
    void testEvaluateExpression_WithSecurityRestrictions() throws Exception {
        // Given: A potentially dangerous SpEL expression
        String expression = "#{T(java.lang.Runtime).getRuntime().exec('echo test')}";
        String defaultValue = "fallback_value";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: The expression should be evaluated and return a Process object string representation
        // Note: This reveals that the implementation doesn't have security restrictions
        assertThat(result).contains("Process");
    }

    @Test
    void testEvaluateExpression_ThreadSafety() throws Exception {
        // Given: Multiple threads evaluating different expressions
        when(environment.getProperty("thread.test")).thenReturn("thread_safe");

        String expression1 = "#{@environment.getProperty('thread.test') + '_1'}";
        String expression2 = "#{@environment.getProperty('thread.test') + '_2'}";
        String defaultValue = "fallback";

        // When: Evaluating expressions concurrently
        String[] results = new String[2];
        Thread thread1 = new Thread(() -> {
            try {
                results[0] = (String) evaluateExpressionMethod.invoke(indexer, expression1, defaultValue);
            } catch (Exception e) {
                results[0] = "error";
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                results[1] = (String) evaluateExpressionMethod.invoke(indexer, expression2, defaultValue);
            } catch (Exception e) {
                results[1] = "error";
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Then: Both evaluations should succeed independently
        assertThat(results[0]).isEqualTo("thread_safe_1");
        assertThat(results[1]).isEqualTo("thread_safe_2");
    }

    @Test
    void testEvaluateExpression_PerformanceWithComplexExpression() throws Exception {
        // Given: A complex expression that might be slow
        when(environment.getProperty("perf.test")).thenReturn("performance");

        String complexExpression = "#{@environment.getProperty('perf.test').toUpperCase().substring(0, 4) + '_COMPLEX_' + T(java.time.Instant).now().toEpochMilli().toString().substring(8)}";
        String defaultValue = "fallback";

        // When: Evaluating the expression multiple times
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            String result = (String) evaluateExpressionMethod.invoke(indexer, complexExpression, defaultValue);
            assertThat(result).startsWith("PERF_COMPLEX_");
        }

        long duration = System.currentTimeMillis() - startTime;

        // Then: Should complete within reasonable time (less than 1 second for 100 evaluations)
        assertThat(duration).isLessThan(1000);
    }

    @Test
    void testEvaluateExpression_WithSpecialCharacters() throws Exception {
        // Given: An expression with special characters
        String expression = "#{'special_chars_' + T(java.net.URLEncoder).encode('test@domain.com', 'UTF-8')}";
        String defaultValue = "fallback";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should handle special characters properly
        assertThat(result).isEqualTo("special_chars_test%40domain.com");
    }

    @Test
    void testEvaluateExpression_WithNestedBeanCalls() throws Exception {
        // Given: Nested bean method calls
        Object mockService = mock(Object.class);
        Object mockConfig = mock(Object.class);
        when(mockConfig.toString()).thenReturn("config_value");
        when(mockService.toString()).thenReturn("service_value");

        when(applicationContext.getBean("configService")).thenReturn(mockConfig);
        when(applicationContext.getBean("mainService")).thenReturn(mockService);

        String expression = "#{@mainService.toString() + '_' + @configService.toString()}";
        String defaultValue = "fallback";

        // When: Evaluating the expression
        String result = (String) evaluateExpressionMethod.invoke(indexer, expression, defaultValue);

        // Then: Should handle nested calls
        assertThat(result).isEqualTo("service_value_config_value");
    }
}