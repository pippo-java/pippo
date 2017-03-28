/*
 * Copyright (C) 2014 the original author or authors.
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
package ro.pippo.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Decebal Suiu
 */
public class ServiceLocator {

    private static final Logger log = LoggerFactory.getLogger(ServiceLocator.class);

    private ServiceLocator() {
    }

    public static <T> T locate(Class<T> service) {
        return locate(service, service.getClassLoader());
    }

    public static <T> T locate(Class<T> service, ClassLoader classLoader) {
        List<T> services = locateAll(service, classLoader);
        return services.isEmpty() ? null : services.get(0);
    }

    public static <T> List<T> locateAll(Class<T> service) {
        return locateAll(service, service.getClassLoader());
    }

    public static <T> List<T> locateAll(Class<T> service, ClassLoader classLoader) {
        log.debug("Locate service '{}' using ServiceLoader", service.getName());
        ServiceLoader<T> loader = ServiceLoader.load(service, classLoader);

        final List<T> services = new ArrayList<>();
        for (T item : loader) {
            log.debug("Found '{}'", item.getClass().getName());
            services.add(item);
        }

        return services;
    }

}
