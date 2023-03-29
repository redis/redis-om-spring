package com.redis.om.spring.repository;

import com.redis.om.spring.annotations.Document;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Document
public class SimpleDocument {
    @Id
    private String id;

    public SimpleDocument() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
