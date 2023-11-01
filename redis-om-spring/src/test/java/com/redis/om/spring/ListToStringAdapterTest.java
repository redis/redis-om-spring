package com.redis.om.spring;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.redis.om.spring.serialization.gson.ListToStringAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ListToStringAdapterTest {

    private ListToStringAdapter adapter;
    private JsonWriter mockJsonWriter;
    private JsonReader mockJsonReader ;

    @BeforeEach
    public void setUp() {
        adapter = new ListToStringAdapter();
        mockJsonWriter = mock(JsonWriter.class);
        mockJsonReader = mock(JsonReader.class);
    }

    @Test
    public void testWrite() throws IOException {
        List<String> inputList = Arrays.asList("item1", "item2", "item3");
        StringWriter stringWriter = new StringWriter();

        when(mockJsonWriter.value(anyString())).thenReturn(mockJsonWriter);
        when(mockJsonWriter.value("item1|item2|item3")).thenReturn(mockJsonWriter);

        adapter.write(mockJsonWriter, inputList);

        verify(mockJsonWriter).value("item1|item2|item3");
    }

    @Test
    public void testWriteWithNullList() throws IOException {
        StringWriter stringWriter = new StringWriter();

        when(mockJsonWriter.nullValue()).thenReturn(mockJsonWriter);

        adapter.write(mockJsonWriter, null);

        verify(mockJsonWriter).nullValue();
    }

    @Test
    public void testWriteWithEmptyList() throws IOException {
        List<String> inputList = Collections.emptyList();

        StringWriter stringWriter = new StringWriter();

        when(mockJsonWriter.nullValue()).thenReturn(mockJsonWriter);

        adapter.write(mockJsonWriter, inputList);

        verify(mockJsonWriter).nullValue();
    }

    @Test
    public void testRead() throws IOException {
        String inputJson = "item1|item2|item3";
        when(mockJsonReader.peek()).thenReturn(JsonToken.STRING);
        when(mockJsonReader.nextString()).thenReturn(inputJson);

        List<?> result = adapter.read(mockJsonReader);

        assertEquals(Arrays.asList("item1", "item2", "item3"), result);
    }

    @Test
    public void testReadWithNullValue() throws IOException {
        when(mockJsonReader.peek()).thenReturn(JsonToken.NULL);

        List<?> result = adapter.read(mockJsonReader);

        assertEquals(Collections.emptyList(), result);
    }

    @Test
    public void testReadWithEmptyString() throws IOException {
        String inputJson = "";
        when(mockJsonReader.peek()).thenReturn(JsonToken.STRING);
        when(mockJsonReader.nextString()).thenReturn(inputJson);

        List<String> result = adapter.read(mockJsonReader);
        assertEquals(List.of(""), result);
    }
}
