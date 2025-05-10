package com.foogaro.modeling.model;

import com.redis.om.spring.annotations.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class TextData {
    @Id
    private String id;
    @TagIndexed
    private String name;
    @TextIndexed(indexMissing = true, indexEmpty = true)
    private String description;
    @Indexed
    private int year;
    @NumericIndexed
    private double score;
//    @Indexed
    @NumericIndexed
    private List<Double> measurements;
}
