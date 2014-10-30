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
package ro.fortsoft.pippo.core;

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
        List<T> services = locateAll(service);
        return services.isEmpty() ? null : services.get(0);
    }

    public static <T> List<T> locateAll(Class<T> service) {
        log.debug("Locate service '{}' using ServiceLoader", service.getName());
        ServiceLoader<T> loader = ServiceLoader.load(service, service.getClassLoader());

        final List<T> services = new ArrayList<T>();
        for (T item : loader) {
            log.debug("Found '{}'", item.getClass().getName());
            services.add(item);
        }

        return services;
    }

}
