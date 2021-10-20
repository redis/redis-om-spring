package com.redis.spring.serialization.gson;

import java.lang.reflect.Type;
import java.util.StringTokenizer;

import org.springframework.data.geo.Point;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PointTypeAdapter implements JsonSerializer<Point>, JsonDeserializer<Point>{

  @Override
  public JsonElement serialize(Point src, Type typeOfSrc, JsonSerializationContext context) {
    String lonlat = src.getX() + "," + src.getY();
    return new JsonPrimitive(lonlat);
  }
  
  @Override
  public Point deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    String latlon = json.getAsString();
    StringTokenizer st = new StringTokenizer(latlon, ",");
    String lon = st.nextToken();
    String lat = st.nextToken();
    
    return new Point(Double.parseDouble(lon), Double.parseDouble(lat));
  }
  
  public static PointTypeAdapter getInstance() {
    return new PointTypeAdapter();
  }



}
