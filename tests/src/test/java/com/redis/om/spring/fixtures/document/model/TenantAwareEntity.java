package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.IndexCreationMode;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.Indexed;

@Document
@IndexingOptions(
    indexName = "#{@tenantResolver.currentTenant}_entities",
    keyPrefix = "#{@tenantResolver.currentTenant}:",
    creationMode = IndexCreationMode.DROP_AND_RECREATE
)
public class TenantAwareEntity {
    @Id
    private String id;

    @Searchable
    private String name;

    @Indexed
    private String tenantId;

    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}