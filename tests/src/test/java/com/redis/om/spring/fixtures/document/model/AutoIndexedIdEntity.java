package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.TagIndexed;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;

/**
 * Test entity with only @Id annotation (no @NumericIndexed).
 * This tests that auto-indexed ID fields work correctly with findByIdIn queries.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class AutoIndexedIdEntity {
    @Id  // Only @Id, no @NumericIndexed - should be auto-indexed as NUMERIC for Long type
    private Long id;

    @NonNull
    @TagIndexed
    private String name;

    @NonNull
    private Integer value;
}
