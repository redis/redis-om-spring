package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Document
public class NullTestData {

    @Id
    private String id;

    @Indexed
    private String title;

    @Searchable
    private String description;

    @Indexed
    private Integer score;

    @Indexed
    private String category;
}