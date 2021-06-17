package com.redislabs.spring.mapping;

import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import com.redislabs.spring.annotations.Document;

public class BasicRedisDocumentPersistentEntity<T> extends BasicPersistentEntity<T, RedisDocumentPersistentProperty>
    implements RedisDocumentPersistentEntity<T> {

  private final String collection;
  private final @Nullable Expression expression;
  private static final SpelExpressionParser PARSER = new SpelExpressionParser();
  
  public BasicRedisDocumentPersistentEntity(TypeInformation<T> information) {
    super(information);

    Class<?> rawType = information.getType();
    String fallback = StringUtils.uncapitalize(rawType.getSimpleName());
    

    if (this.isAnnotationPresent(Document.class)) {
      Document document = this.getRequiredAnnotation(Document.class);

      this.collection = StringUtils.hasText(document.collection()) ? document.collection() : fallback;
      this.expression = detectExpression(document.collection());
    } else {

      this.collection = fallback;
      this.expression = null;
    }
  }

  @Override
  public String getCollection() {
    return expression == null //
        ? collection //
        : expression.getValue(getEvaluationContext(null), String.class);
  }
  
  /**
   * Returns a SpEL {@link Expression} if the given {@link String} is actually an expression that does not evaluate to a
   * {@link LiteralExpression} (indicating that no subsequent evaluation is necessary).
   *
   * @param potentialExpression can be {@literal null}
   * @return can be {@literal null}.
   */
  @Nullable
  private static Expression detectExpression(@Nullable String potentialExpression) {

    if (!StringUtils.hasText(potentialExpression)) {
      return null;
    }

    Expression expression = PARSER.parseExpression(potentialExpression, ParserContext.TEMPLATE_EXPRESSION);
    return expression instanceof LiteralExpression ? null : expression;
  }

}
