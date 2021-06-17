package com.redislabs.spring.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BloomProbabilisticExists {

  
  @Pointcut("execution(* org.springframework.data.repository.CrudRepository+.save(..))")
  public void save() {
  }
  
  @After("save()")
  public void afterSave() {
    System.out.println(">>>> AFTER save()");
  }
  
  
//  @Pointcut("execution(* org.springframework.data.repository.CrudRepository+.save(*)) || " +
//      "execution(* org.springframework.data.repository.CrudRepository+.saveAndFlush(*))")
//public void whenSaveOrUpdate() {};
//
//@Pointcut("execution(* org.springframework.data.repository.CrudRepository+.delete(*))")
//public void whenDelete() {};
//
//@Before("whenSaveOrUpdate() && args(entity)")
//public void beforeSaveOrUpdate(JoinPoint joinPoint, BaseEntity entity) {...}

//  @Around("save()")
//  public Object aroundSave(final ProceedingJoinPoint pjp) throws Throwable {
//
//      Object[] args = pjp.getArgs();
//      
//      System.out.println(">>>> AROUND save()");
//
////      if (Iterable.class.isAssignableFrom(args[0].getClass())) {
////          //noinspection unchecked
////          Iterable<BaseEntity> entities = (Iterable<BaseEntity>) args[0];
////          entities.forEach(entity -> {
////              // set the fields here...
////          });
////      }
////
////      if (args[0] instanceof BaseEntity) {
////          BaseEntity entity = (BaseEntity) args[0];
////          // set the fields here...
////      }
//
//      return pjp.proceed(args); 
//  }
}
