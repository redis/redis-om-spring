package com.redis.om.spring;

import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.annotation.Reference;
import redis.clients.jedis.json.Path;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReferenceProcessor {

    private final JSONOperations<?> redisJSONOperations;
    private final RediSearchIndexer indexer;

    public ReferenceProcessor (JSONOperations<?> redisJSONOperations, RediSearchIndexer indexer) {
        this.redisJSONOperations = redisJSONOperations;
        this.indexer = indexer;
    }

    public void processReferences (String key, Object item) {
        List<Field> fields = ObjectUtils.getFieldsWithAnnotation (item.getClass (), Reference.class);
        if (!fields.isEmpty ()) {
            JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
            PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess (item);
            fields.forEach (f -> {
                var referencedValue = accessor.getPropertyValue (f.getName ());
                if (referencedValue != null) {
                    if (referencedValue instanceof Collection<?> referenceValues) {
                        List<String> referenceKeys = new ArrayList<> ();
                        referenceValues.forEach (r -> {
                            Object id = ObjectUtils.getIdFieldForEntity (r);
                            if (id != null) {
                                String referenceKey = indexer.getKeyspaceForEntityClass (r.getClass ()) + id;
                                referenceKeys.add (referenceKey);
                            }
                        });
                        ops.set (key, referenceKeys, Path.of ("$." + f.getName ()));
                    } else {
                        Object id = ObjectUtils.getIdFieldForEntity (referencedValue);
                        if (id != null) {
                            String referenceKey = indexer.getKeyspaceForEntityClass (f.getType ()) + id;
                            ops.set (key, referenceKey, Path.of ("$." + f.getName ()));
                        }
                    }
                }
            });
        }
    }
}
