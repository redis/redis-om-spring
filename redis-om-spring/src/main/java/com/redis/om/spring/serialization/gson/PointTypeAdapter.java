package com.redis.om.spring.serialization.gson;

import com.google.gson.*;
import org.springframework.data.geo.Point;

import java.lang.reflect.Type;
import java.util.StringTokenizer;

public class PointTypeAdapter implements JsonSerializer<Point>, JsonDeserializer<Point> {

  public static PointTypeAdapter getInstance() {
    return new PointTypeAdapter();
  }

  @Override
  public JsonElement serialize(Point src, Type typeOfSrc, JsonSerializationContext context) {
    String lonlat = src.getX() + "," + src.getY();
    return new JsonPrimitive(lonlat);
  }

  @Override
  public Point deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    String lon;
    String lat;
    if (json.isJsonArray()) {
      JsonArray latlon = json.getAsJsonArray();
      lon = latlon.get(0).getAsString();
      lat = latlon.get(1).getAsString();
    } else {
      String latlon = json.getAsString();
      StringTokenizer st = new StringTokenizer(latlon, ",");
      lon = st.nextToken();
      lat = st.nextToken();
    }

    return new Point(Double.parseDouble(lon), Double.parseDouble(lat));
  }
}
