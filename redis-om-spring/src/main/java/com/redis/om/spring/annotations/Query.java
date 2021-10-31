package com.redis.om.spring.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface Query {
  String value() default "*";
  String[] returnFields() default {};
  
  //returnFields(returnFields);
  //limit(null, null)
  //addFilter(null)
  //highlightFields(null)
  //limitFields(String...)
  //limitKeys()
  //returnFields()
  //setLanguage()
  //setNoContent()
  //setNoStopwords()
  //setPayload()
  //setScorer()
  //setSortBy(, )
  //setVerbatim()
  //setWithPayload()
  //setWithScores()
}
