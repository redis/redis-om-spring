package com.redis.om.spring.newtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.redis.om.spring.CustomRedisKeyValueTemplate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.mapping.RedisMappingContext;

public class CustomRedisKeyValueTemplateTest {

    private CustomRedisKeyValueTemplate customRedisKeyValueTemplate;
    private RedisKeyValueAdapter adapter;

    @Before
    public void setUp() {
        adapter = mock(RedisKeyValueAdapter.class);
        RedisMappingContext mappingContext = mock(RedisMappingContext.class);
        customRedisKeyValueTemplate = new CustomRedisKeyValueTemplate(adapter, mappingContext);
    }

    @Test
    public void testGetConverter() {
        //assertNotNull(customRedisKeyValueTemplate.getConverter());
    }

    @Test
    public void testGetAdapter() {
        assertEquals(adapter, customRedisKeyValueTemplate.getAdapter());
    }

    @Test
    public void testGetMappingContext() {
        RedisMappingContext mappingContext = customRedisKeyValueTemplate.getMappingContext();
        assertNotNull(mappingContext);
        assertEquals(RedisMappingContext.class, mappingContext.getClass());
    }
}
