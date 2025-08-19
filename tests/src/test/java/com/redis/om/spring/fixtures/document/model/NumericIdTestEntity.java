package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.TagIndexed;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class NumericIdTestEntity {
    @Id
    @NumericIndexed  // Testing @NumericIndexed on @Id field
    private Long id;
    
    @NonNull
    @TagIndexed
    private String name;
    
    @NonNull
    @NumericIndexed
    private Integer value;
}