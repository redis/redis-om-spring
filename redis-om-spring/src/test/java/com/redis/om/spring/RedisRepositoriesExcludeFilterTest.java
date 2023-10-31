package com.redis.om.spring;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

import java.util.Set;

import static org.junit.Assert.*;

public class RedisRepositoriesExcludeFilterTest {

    @Test
    public void testMatch() {
        RedisRepositoriesExcludeFilter filter = new RedisRepositoriesExcludeFilter();

        // Create an array of auto configuration classes
        String[] autoConfigurationClasses = {
                "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
                "com.example.MyCustomAutoConfiguration"
        };

        // Create a mock AutoConfigurationMetadata
        AutoConfigurationMetadata autoConfigurationMetadata = new AutoConfigurationMetadata() {
            @Override
            public boolean wasProcessed(String className) {
                return false;
            }

            @Override
            public Integer getInteger(String className, String key) {
                return null;
            }

            @Override
            public Integer getInteger(String className, String key, Integer defaultValue) {
                return null;
            }

            @Override
            public Set<String> getSet(String className, String key) {
                return null;
            }

            @Override
            public Set<String> getSet(String className, String key, Set<String> defaultValue) {
                return null;
            }

            @Override
            public String get(String className, String key) {
                return null;
            }

            @Override
            public String get(String className, String key, String defaultValue) {
                return null;
            }
        };

        boolean[] matches = filter.match(autoConfigurationClasses, autoConfigurationMetadata);

        // The first class should be skipped, and the second one should not be skipped
        assertFalse(matches[0]);
        assertTrue(matches[1]);
    }
}
