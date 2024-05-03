package com.redis.om.spring.repository;

public interface DocumentProjection {
    String getName();
    RecursiveSummary getRecursiveProjection();

    interface RecursiveSummary {
        String getRecursiveProp1();
    }

}
