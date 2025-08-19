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
public class NumericInTestEntity {
    @Id
    private String id;
    
    @NonNull
    @TagIndexed
    private String name;
    
    @NonNull
    @NumericIndexed
    private Integer age;
    
    @NonNull
    @NumericIndexed  
    private Long score;
    
    @NonNull
    @NumericIndexed
    private Double rating;
    
    @NumericIndexed
    private Integer level;
}