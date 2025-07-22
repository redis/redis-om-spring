package com.redis.om.spring.fixtures.document.model;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.TagIndexed;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Test model for demonstrating GitHub issue #400:
 * NumericField lacks methods to check if a numeric array contains specific numbers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class NumericArrayTestData {
    @Id
    private String id;
    
    @TagIndexed
    private String name;
    
    @NumericIndexed
    private List<Double> measurements;
    
    @NumericIndexed
    private List<Long> counts;
    
    @NumericIndexed
    private List<Integer> ratings;
    
    @TagIndexed
    private List<String> tags; // For comparison - this works with TagField.in()
}