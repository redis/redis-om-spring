package com.redis.om.permits.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

import com.google.gson.annotations.JsonAdapter;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.DocumentScore;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.serialization.gson.SetToStringAdapter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/*
{
"_score": 0.8,
"permit_timestamp": 1254391200,
"address": {
  "street":"Bennelong Point",
  "city":"Sydney"
},
"description": "Fix the facade",
"building_type": "Residential",
"work_type": "construction;design",
"construction_value": 210000,
"location": "151.22, -33.87"
}

with an index like:

FT.CREATE permits
 ON JSON
    PREFIX 1 "tst:"
    SCORE_FIELD "$._score"
 SCHEMA
   $.permit_timestamp AS permit_timestamp NUMERIC SORTABLE
   $.address.street AS address TEXT NOSTEM
   $.address.city AS city TAG
   $.description AS description TEXT
   $.building_type AS building_type TEXT WEIGHT 20 NOSTEM SORTABLE
   $.work_type AS work_type TAG SEPARATOR ";"
   $.construction_value AS construction_value NUMERIC SORTABLE
   $.location AS location GEO
   $.status_log.[-1] as status TAG   # Index the last element of the array as "status"
*/
@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "tst")
public class Permit {
  @Id
  private String id;

  @DocumentScore
  private double score;

  @NonNull
  @Indexed(sortable = true)
  private LocalDateTime permitTimestamp = LocalDateTime.now();

  @NonNull
  @Indexed
  private Address address;

  @NonNull
  @Searchable
  private String description;

  @NonNull
  @Searchable(sortable = true, nostem = true, weight = 20.0)
  private String buildingType;

  @NonNull
  @Indexed(separator = ",")
  @JsonAdapter(SetToStringAdapter.class)
  private Set<String> workType;

  @NonNull
  @Indexed(sortable = true)
  private Long constructionValue;

  @NonNull
  @Indexed
  private Point location;

  @NonNull
  @Indexed(alias = "status", arrayIndex = -1)
  private List<String> statusLog;
}
