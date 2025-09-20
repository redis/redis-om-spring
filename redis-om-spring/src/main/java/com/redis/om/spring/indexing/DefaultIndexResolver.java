package com.redis.om.spring.indexing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.redis.om.spring.annotations.IndexingOptions;

/**
 * Default implementation of {@link IndexResolver} that supports
 * SpEL expressions and context-based resolution.
 *
 * <p>This implementation evaluates SpEL expressions found in
 * {@link IndexingOptions} annotations and incorporates context
 * information from {@link RedisIndexContext} when available.
 *
 * @since 1.0.0
 */
public class DefaultIndexResolver implements IndexResolver {

  private final ApplicationContext applicationContext;
  private final ExpressionParser parser = new SpelExpressionParser();
  private static final Pattern SPEL_TEMPLATE_PATTERN = Pattern.compile("#\\{([^}]+)\\}");

  /**
   * Creates a new DefaultIndexResolver.
   *
   * @param applicationContext the Spring application context
   */
  public DefaultIndexResolver(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public String resolveIndexName(Class<?> entityClass, RedisIndexContext context) {
    IndexingOptions indexingOptions = entityClass.getAnnotation(IndexingOptions.class);

    if (indexingOptions != null && !indexingOptions.indexName().isEmpty()) {
      String indexName = indexingOptions.indexName();

      // Check if the index name contains SpEL expressions
      if (containsSpelExpression(indexName)) {
        String evaluated = evaluateSpelExpression(indexName, entityClass, context);
        if (evaluated != null) {
          return evaluated;
        }
        // If SpEL evaluation failed, fall back to default
      } else {
        // Plain string annotation - always return as is, regardless of context
        return indexName;
      }
    }

    // Default naming convention
    String baseName = getDefaultIndexName(entityClass);

    // Apply context if available
    if (context != null) {
      StringBuilder sb = new StringBuilder(baseName);

      // Remove the _idx suffix if present
      if (baseName.endsWith("_idx")) {
        sb.setLength(sb.length() - 4);
      } else if (baseName.endsWith("Idx")) {
        sb.setLength(sb.length() - 3);
      }

      if (context.getTenantId() != null) {
        sb.append("_").append(context.getTenantId());
      }

      if (context.getEnvironment() != null) {
        sb.append("_").append(context.getEnvironment());
      }

      // Add version attribute if present
      Object version = context.getAttribute("version");
      if (version != null) {
        sb.append("_").append(version);
      }

      sb.append("_idx");
      return sb.toString();
    }

    return baseName;
  }

  @Override
  public String resolveKeyPrefix(Class<?> entityClass, RedisIndexContext context) {
    IndexingOptions indexingOptions = entityClass.getAnnotation(IndexingOptions.class);

    if (indexingOptions != null && !indexingOptions.keyPrefix().isEmpty()) {
      String keyPrefix = indexingOptions.keyPrefix();

      // Check if the prefix contains SpEL expressions
      if (containsSpelExpression(keyPrefix)) {
        String evaluated = evaluateSpelExpression(keyPrefix, entityClass, context);
        if (evaluated != null) {
          return evaluated;
        }
        // If SpEL evaluation failed, fall back to default
      } else {
        // Plain string annotation - always return as is, regardless of context
        return keyPrefix;
      }
    }

    // Default key prefix convention
    String basePrefix = getDefaultKeyPrefix(entityClass);

    // Apply context if available
    if (context != null && context.getTenantId() != null) {
      // Prepend tenant ID to the prefix
      return context.getTenantId() + ":" + basePrefix;
    }

    return basePrefix;
  }

  /**
   * Gets the tenant ID from the context.
   * Subclasses can override this for custom tenant resolution.
   *
   * @param context the current context
   * @return the tenant ID, or null if not available
   */
  protected String getTenantId(RedisIndexContext context) {
    if (context != null) {
      return context.getTenantId();
    }
    return null;
  }

  private boolean containsSpelExpression(String value) {
    return value.contains("#{");
  }

  private String evaluateSpelExpression(String expression, Class<?> entityClass, RedisIndexContext context) {
    try {
      // Create evaluation context
      StandardEvaluationContext evalContext = new StandardEvaluationContext();

      // Add context as a variable
      if (context != null) {
        evalContext.setVariable("context", context);
      } else {
        // Create empty context to avoid null reference errors
        evalContext.setVariable("context", RedisIndexContext.builder().build());
      }

      // Add application context beans
      if (applicationContext != null) {
        evalContext.setBeanResolver((ctx, beanName) -> {
          // Special handling for environment bean
          if ("environment".equals(beanName)) {
            return applicationContext.getEnvironment();
          }
          return applicationContext.getBean(beanName);
        });

        // Also add environment as a variable for direct access
        if (applicationContext.getEnvironment() != null) {
          evalContext.setVariable("environment", applicationContext.getEnvironment());
        }
      }

      // Process template expressions - replace #{...} with evaluated values
      StringBuffer result = new StringBuffer();
      Matcher matcher = SPEL_TEMPLATE_PATTERN.matcher(expression);
      boolean hasFailedExpressions = false;

      while (matcher.find()) {
        String spelExpression = matcher.group(1);
        try {
          Expression exp = parser.parseExpression(spelExpression);
          Object evalResult = exp.getValue(evalContext);
          if (evalResult != null) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(evalResult.toString()));
          } else {
            hasFailedExpressions = true;
            break;
          }
        } catch (Exception e) {
          // Expression evaluation failed
          hasFailedExpressions = true;
          break;
        }
      }

      if (hasFailedExpressions) {
        return null;
      }

      matcher.appendTail(result);
      return result.toString();

    } catch (Exception e) {
      // Fall back to default if SpEL evaluation fails
      return null;
    }
  }

  private String getDefaultIndexName(Class<?> entityClass) {
    // Use the simple name converted to snake_case
    String className = entityClass.getSimpleName();
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < className.length(); i++) {
      char c = className.charAt(i);
      if (Character.isUpperCase(c) && i > 0) {
        sb.append("_");
      }
      sb.append(Character.toLowerCase(c));
    }

    sb.append("_idx");
    return sb.toString();
  }

  private String getDefaultKeyPrefix(Class<?> entityClass) {
    // Use the simple name converted to lowercase with colons
    String className = entityClass.getSimpleName();
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < className.length(); i++) {
      char c = className.charAt(i);
      if (Character.isUpperCase(c) && i > 0) {
        sb.append(":");
      }
      sb.append(Character.toLowerCase(c));
    }

    sb.append(":");
    return sb.toString();
  }
}