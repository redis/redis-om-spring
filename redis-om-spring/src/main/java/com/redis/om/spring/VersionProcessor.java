package com.redis.om.spring;

import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.util.ObjectUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.annotation.Version;
import redis.clients.jedis.json.Path;

import java.lang.reflect.Field;
import java.util.List;

import static com.redis.om.spring.util.ObjectUtils.isPrimitiveOfType;

public class VersionProcessor {

    private final JSONOperations<?> redisJSONOperations;

    public VersionProcessor (JSONOperations<?> redisJSONOperations) {
        this.redisJSONOperations = redisJSONOperations;

    }

    public void processVersion (String key, Object item) {
        List<Field> fields = ObjectUtils.getFieldsWithAnnotation (item.getClass (), Version.class);
        if (fields.size () == 1) {
            BeanWrapper wrapper = new BeanWrapperImpl (item);
            Field versionField = fields.get (0);
            String property = versionField.getName ();
            if ((versionField.getType () == Integer.class || isPrimitiveOfType (versionField.getType (), Integer.class)) ||
                    (versionField.getType () == Long.class || isPrimitiveOfType (versionField.getType (), Long.class))) {
                Number version = (Number) wrapper.getPropertyValue (property);
                Number dbVersion = getEntityVersion (key, property);

                if (dbVersion != null && version != null && dbVersion.longValue () != version.longValue ()) {
                    throw new OptimisticLockingFailureException (
                            String.format ("Cannot insert/update entity %s with version %s as it already exists", item,
                                    version));
                } else {
                    Number nextVersion = version == null ? 0 : version.longValue () + 1;
                    try {
                        wrapper.setPropertyValue (property, nextVersion);
                    } catch (NotWritablePropertyException nwpe) {
                        versionField.setAccessible (true);
                        try {
                            versionField.set (item, nextVersion);
                        } catch (IllegalAccessException iae) {
                            throw new RuntimeException (nwpe);
                        }
                    }
                }
            }
        }
    }

    private Number getEntityVersion (String key, String versionProperty) {
        JSONOperations<String> ops = (JSONOperations<String>) redisJSONOperations;
        Class<?> type = new TypeToken<Long[]> () {
        }.getRawType ();
        Long[] dbVersionArray = (Long[]) ops.get (key, type, Path.of ("$." + versionProperty));
        return dbVersionArray != null ? dbVersionArray[0] : null;
    }
}
