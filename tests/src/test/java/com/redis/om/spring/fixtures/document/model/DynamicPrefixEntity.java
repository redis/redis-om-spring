package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.IndexCreationMode;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.annotations.Indexed;

@Document
@IndexingOptions(
    indexName = "dynamic_prefix_idx",
    keyPrefix = "#{@tenantResolver.currentTenant}:",
    creationMode = IndexCreationMode.DROP_AND_RECREATE
)
public class DynamicPrefixEntity {
    @Id
    private String id;

    @Indexed
    private String category;

    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}