package com.redis.om.amr.entraid.model;

import com.redis.om.spring.annotations.Document;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class MyData {

    @Id
    private String id;
    private String name;
    private String description;
    private int year;

}
