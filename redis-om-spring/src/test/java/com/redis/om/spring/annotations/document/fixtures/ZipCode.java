package com.redis.om.spring.annotations.document.fixtures;

import com.google.gson.annotations.SerializedName;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Document
/**
 * See http://media.mongodb.org/zips.json
 *
 * Sample:
 * {
 *   "_id" : "01001",
 *   "city" : "AGAWAM",
 *   "loc" : [ -72.622739, 42.070206 ],
 *   "pop" : 15338,
 *   "state" : "MA"
 * }
 */
public class ZipCode {

  @Id
  @SerializedName(value = "_id")
  private String id;

  @Indexed(sortable = true)
  private String city;

  @Indexed(sortable = true)
  private Point loc;

  @Indexed(sortable = true)
  private Integer pop;

  @Indexed(sortable = true)
  private String state;
}
