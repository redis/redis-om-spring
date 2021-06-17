package com.redislabs.spring.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { FIELD, ANNOTATION_TYPE })
public @interface RedisProbExists {

}
// https://www.baeldung.com/spring-annotation-bean-pre-processor
//https://github.com/gkatzioura/egkatzioura.wordpress.com/tree/master/SpringDataJPAIntegration
