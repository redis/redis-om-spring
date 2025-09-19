package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.IndexCreationMode;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.annotations.Searchable;

@Document
@IndexingOptions(
    indexName = "#{@environment.getProperty('app.tenant')}_idx",
    creationMode = IndexCreationMode.DROP_AND_RECREATE
)
public class EnvironmentBasedIndexEntity {
    @Id
    private String id;

    @Searchable
    private String content;

    private String metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}