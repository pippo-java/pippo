/*
 * Copyright (C) 2015 the original author or authors.
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
package ro.pippo.controller.extractor;

import org.kohsuke.MetaInfServices;
import ro.pippo.controller.MethodParameter;
import ro.pippo.core.ParameterValue;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.util.LangUtils;
import ro.pippo.core.util.StringUtils;

import java.util.Collection;

/**
 * @author Decebal Suiu
 * @author James Moger
 */
@MetaInfServices
public class ParamExtractor implements MethodParameterExtractor {

    @Override
    public boolean isApplicable(MethodParameter parameter) {
        return parameter.isAnnotationPresent(Param.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object extract(MethodParameter parameter, RouteContext routeContext) {
        Param annotation = parameter.getAnnotation(Param.class);
        String name = getParameterName(parameter, annotation);
        String pattern = annotation.pattern();

        ParameterValue parameterValue = routeContext.getParameter(name);

        Class<?> parameterType = parameter.getParameterType();
        if (Collection.class.isAssignableFrom(parameterType)) {
            Class<? extends Collection> collectionType = (Class<? extends Collection>) parameterType;
            Class<?> objectType = parameter.getParameterGenericType();

            return parameterValue.toCollection(collectionType, objectType, pattern);
        }

        return parameterValue.to(parameterType, pattern);
    }

    private String getParameterName(MethodParameter parameter, Param annotation) {
        String name = annotation.value();
        if (StringUtils.isNullOrEmpty(name)) {
            name = parameter.getParameterName();
        }

        if (name == null) {
            throw new PippoRuntimeException(
                "Method '{}' parameter {} does not specify a name!",
                LangUtils.toString(parameter.getMethod()), parameter.getParameterIndex());
        }

        return name;
    }

}
