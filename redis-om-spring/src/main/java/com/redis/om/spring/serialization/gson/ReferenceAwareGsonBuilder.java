package com.redis.om.spring.serialization.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.ops.json.JSONOperations;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Reference;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.redis.om.spring.util.ObjectUtils.*;

@Component
public class ReferenceAwareGsonBuilder {
    private static final Log logger = LogFactory.getLog(ReferenceAwareGsonBuilder.class);
    private final List<Type> processedClasses = new ArrayList<>();
    private final GsonBuilder builder;
    private Gson gson;
    private JSONOperations<?> ops;
    private final ApplicationContext ac;
    private boolean rebuildGson = false;

    public ReferenceAwareGsonBuilder(GsonBuilder builder, ApplicationContext ac) {
        this.builder = builder;
        this.gson = builder.create();
        this.ac = ac;
    }
    public <T> void processEntity(Class<T> clazz) {
        if (!processedClasses.contains(clazz)) {
            ops = ac.getBean("redisJSONOperations", JSONOperations.class);
            final List<java.lang.reflect.Field> allClassFields = getDeclaredFieldsTransitively(clazz);
            for (java.lang.reflect.Field field : allClassFields) {
                if (field.isAnnotationPresent(Reference.class)) {
                    logger.debug(String.format("Registering reference type adapter for %s", field.getType().getName()));
                    processField(field);
                }
            }
            processedClasses.add(clazz);
        }
    }

    public Gson gson() {
        if (rebuildGson) {
            gson = builder.create();
            rebuildGson = false;
        }
        return gson;
    }
    private void processField(Field field) {
        TypeToken<?> typeToken;
        if (isCollection(field)) {
            var maybeCollectionElementType = getCollectionElementType(field);
            if (maybeCollectionElementType.isPresent()) {
                typeToken = TypeToken.getParameterized(field.getType(), maybeCollectionElementType.get());
            } else {
                typeToken = TypeToken.get(field.getType());
            }
        } else {
            typeToken = TypeToken.get(field.getType());
        }
        builder.registerTypeAdapter(typeToken.getType(), new ReferenceDeserializer(field, ops));
        rebuildGson = true;

        processEntity(field.getType());
    }
}
