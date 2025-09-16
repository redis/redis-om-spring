package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import org.springframework.data.annotation.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class RxDocument {
    @Id
    private String id;

    @Indexed
    private String rxNumber;

    @Indexed(indexEmpty = true, indexMissing = true)
    private String lock;

    @Indexed
    private String status;
}