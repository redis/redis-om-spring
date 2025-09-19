package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.IndexCreationMode;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.annotations.Indexed;

@Document
@IndexingOptions(
    indexName = "tenant_#{@tenantResolver.currentTenant}_idx",
    creationMode = IndexCreationMode.DROP_AND_RECREATE
)
public class BeanReferenceIndexEntity {
    @Id
    private String id;

    @Indexed
    private String code;

    private String data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}