package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.IndexCreationMode;
import com.redis.om.spring.annotations.IndexingOptions;

@Document
@IndexingOptions(
    indexName = "#{@nonExistentBean.method()}", // This will fail and fallback
    creationMode = IndexCreationMode.DROP_AND_RECREATE
)
public class FallbackIndexEntity {
    @Id
    private String id;

    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}