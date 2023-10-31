package com.redis.om.spring;

import com.google.gson.*;
import com.redis.om.spring.serialization.gson.DateTypeAdapter;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DateTypeAdapterTest {

    @Test
    public void testSerialize() {
        DateTypeAdapter dateTypeAdapter = DateTypeAdapter.getInstance();
        Date inputDate = new Date();
        JsonSerializationContext context = new JsonSerializationContext() {
            @Override
            public JsonElement serialize(Object src) {
                return null;
            }

            @Override
            public JsonElement serialize(Object src, Type typeOfSrc) {
                return null;
            }
        };

        JsonElement jsonElement = dateTypeAdapter.serialize(inputDate, Date.class, context);

        // Check that the serialized value is a number (time in milliseconds)
        assertTrue(jsonElement instanceof JsonPrimitive);
        assertEquals(inputDate.getTime(), ((JsonPrimitive) jsonElement).getAsLong());
    }

    @Test
    public void testDeserialize() {
        DateTypeAdapter dateTypeAdapter = DateTypeAdapter.getInstance();
        long timeInMillis = 1635632400000L; // Replace with your specific time value
        JsonElement jsonElement = new JsonPrimitive(timeInMillis);
        JsonDeserializationContext context = new JsonDeserializationContext() {
            @Override
            public <T> T deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
                return null;
            }
        };

        Date deserializedDate = dateTypeAdapter.deserialize(jsonElement, Date.class, context);

        // Check that the deserialized date matches the input timeInMillis
        assertEquals(timeInMillis, deserializedDate.getTime());
    }
}
