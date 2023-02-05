/*
 * Copyright (C) 2014-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.core.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.ContentTypeEngine;
import ro.pippo.core.ContentTypeEngines;
import ro.pippo.core.ParamField;
import ro.pippo.core.ParameterValue;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.Request;
import ro.pippo.core.converters.Converter;
import ro.pippo.core.util.ClassUtils;
import ro.pippo.core.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Decebal Suiu
 */
public class DefaultEntityRequestEngine implements EntityRequestEngine {

    private static final Logger log = LoggerFactory.getLogger(DefaultEntityRequestEngine.class);

    private final ContentTypeEngines contentTypeEngines;

    public DefaultEntityRequestEngine(ContentTypeEngines contentTypeEngines) {
        this.contentTypeEngines = contentTypeEngines;
    }

    @Override
    public <T> T createEntityFromParameters(Class<T> entityClass, Request request) {
        T entity;
        try {
            entity = entityClass.newInstance();
        } catch (Exception e) {
            log.error("Cannot create new instance of class '{}'", entityClass.getName(), e);
            return null;
        }

        updateEntityFromParameters(entity, request);

        return entity;
    }

    @Override
    public <T> T createEntityFromBody(Class<T> entityClass, Request request) {
        try {
            String body = request.getBody();
            if (StringUtils.isNullOrEmpty(body)) {
                log.warn("Can not create entity '{}' from null or empty request body!", entityClass.getName());
                return null;
            }

            // try to determine the body content-type
            String contentType = request.getContentType();
            if (StringUtils.isNullOrEmpty(contentType)) {
                // sloppy client, try to accept header
                contentType = request.getAcceptType();
            }

            if (StringUtils.isNullOrEmpty(contentType)) {
                throw new PippoRuntimeException(
                    "Failed to create entity '{}' from request body because 'content-type' is not specified!",
                    entityClass.getName());
            }

            ContentTypeEngine engine = contentTypeEngines.getContentTypeEngine(contentType);
            if (engine == null) {
                throw new PippoRuntimeException(
                    "Failed to create entity '{}' from request body because a content engine for '{}' could not be found!",
                    entityClass.getName(), contentType);
            }

            return engine.fromString(body, entityClass);
        } catch (PippoRuntimeException e) {
            // pass-through PippoRuntimeExceptions
            throw e;
        } catch (Exception e) {
            // capture and re-throw all exceptions
            throw new PippoRuntimeException(e, "Failed to create entity '{}' from request body!", entityClass.getName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, X> T updateEntityFromParameters(T entity, Request request) {
        Map<String, ParameterValue> parameters = request.getParameters();
        for (Field field : ClassUtils.getAllFields(entity.getClass())) {
            String parameterName = field.getName();
            ParamField parameter = field.getAnnotation(ParamField.class);
            if (parameter != null) {
                parameterName = parameter.value();
            }

            if (parameters.containsKey(parameterName)) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                String pattern = (parameter != null) ? parameter.pattern() : null;
                Class<? extends Converter<T>> converterClass = (parameter != null && void.class != parameter.converterClass()) ? parameter.converterClass() : null;

                try {
                    Class<?> fieldClass = field.getType();
                    Object value;
                    if (converterClass == null) {
                        if (Collection.class.isAssignableFrom(fieldClass)) {
                            Type parameterType = field.getGenericType();
                            if (!ParameterizedType.class.isAssignableFrom(parameterType.getClass())) {
                                throw new PippoRuntimeException("Please specify a generic parameter type for field '{}' {}",
                                    field.getName(), fieldClass.getName());
                            }
                            ParameterizedType parameterizedType = (ParameterizedType) parameterType;
                            Class<X> genericClass;
                            try {
                                genericClass = (Class<X>) parameterizedType.getActualTypeArguments()[0];
                            } catch (ClassCastException e) {
                                throw new PippoRuntimeException("Please specify a generic parameter type for field '{}' {}",
                                    field.getName(), fieldClass.getName());
                            }

                            if (Set.class == fieldClass) {
                                value = parameters.get(parameterName).toSet(genericClass, pattern);
                            } else if (List.class == fieldClass) {
                                value = parameters.get(parameterName).toList(genericClass, pattern);
                            } else if (fieldClass.isInterface()) {
                                throw new PippoRuntimeException("Field '{}' collection '{}' is not a supported type!",
                                    field.getName(), fieldClass.getName());
                            } else {
                                Class<? extends Collection<?>> collectionClass = (Class<? extends Collection<?>>) fieldClass;
                                value = parameters.get(parameterName).toCollection(collectionClass, genericClass, pattern);
                            }
                        } else {
                            value = parameters.get(parameterName).to(fieldClass, pattern);
                        }
                    } else {
                        value = parameters.get(parameterName).convert(converterClass, pattern);
                    }
                    field.set(entity, value);
                } catch (IllegalAccessException e) {
                    log.error("Cannot set value for field '{}' from parameter '{}'", field.getName(), parameterName, e);
                } catch (PippoRuntimeException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        return entity;
    }

}
