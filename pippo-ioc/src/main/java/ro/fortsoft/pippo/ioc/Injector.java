/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.ioc;

import ro.fortsoft.pippo.core.PippoRuntimeException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Injector scans fields of an object instance and checks if the specified
 * {@link FieldValueProvider} can provide a value for a field; if it can, the field is set to that
 * value. Injector will ignore all non-null fields.
 *
 * @author Decebal Suiu
 */
public abstract class Injector {

    private final ConcurrentMap<Class, Field[]> fieldsCache = new ConcurrentHashMap<>();

    public static Injector get() {
        // TODO
//        return Application.get().getLocals.get(KEY);
        return null;
    }

    /**
     * Injects the specified object. This method is usually implemented by delegating to
     * {@link #inject(Object, FieldValueProvider)} with some {@link FieldValueProvider}
     *
     * @see #inject(Object, FieldValueProvider)
     */
    public abstract void inject(Object object);

    /**
     * Traverse fields in the class hierarchy of the object and set their value with a value
     * provided by a {@link FieldValueProvider}.
     */
    protected void inject(Object object, FieldValueProvider provider) {
        Class<?> clazz = object.getClass();

        // try cache
        Field[] fields = fieldsCache.get(clazz);
        if (fields == null) {
            // cache miss, discover fields
            fields = findFields(clazz, provider);

            // write to cache
            fieldsCache.put(clazz, fields);
        }

        for (Field field : fields) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            try {
                if (field.get(object) == null) {
                    Object value = provider.getFieldValue(field, object);
                    if (value != null) {
                        field.set(object, value);
                    }
                }
            } catch (Exception e) {
                throw new PippoRuntimeException("Error while injecting object [" + object.toString() +
                        "] of type [" + object.getClass().getName() + "]", e);
            }
        }
    }

    /**
     * Returns an array of fields that can be injected using the given field value provider.
     */
    private Field[] findFields(Class<?> clazz, FieldValueProvider provider) {
        List<Field> matched = new ArrayList<>();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (provider.supportsField(field)) {
                    matched.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }

        return matched.toArray(new Field[matched.size()]);
    }

}
