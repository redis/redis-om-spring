package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.IndexCreationMode;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.annotations.Searchable;

@Document
@IndexingOptions(
    indexName = "users_v#{@versionService.getMajorVersion()}_#{@versionService.getMinorVersion()}_#{@versionService.getPatchVersion()}",
    creationMode = IndexCreationMode.DROP_AND_RECREATE
)
public class VersionedIndexEntity {
    @Id
    private String id;

    @Searchable
    private String username;

    private String email;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}