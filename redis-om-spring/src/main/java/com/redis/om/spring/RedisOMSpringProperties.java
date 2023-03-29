package com.redis.om.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "redis.om.spring",
        ignoreInvalidFields = true
)
public class RedisOMSpringProperties {
    private final Repository repository = new Repository();

    public Repository getRepository() {
        return repository;
    }

    public static class Repository {
        private final Query query = new Query();

        public Query getQuery() {
            return query;
        }

        public static class Query {
            private int limit = 10000;

            public int getLimit() {
                return limit;
            }

            public void setLimit(int limit) {
                this.limit = limit;
            }
        }
    }
}
