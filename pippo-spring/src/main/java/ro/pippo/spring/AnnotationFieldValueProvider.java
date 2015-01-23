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
package ro.pippo.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.ioc.FieldValueProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Decebal Suiu
 */
public class AnnotationFieldValueProvider implements FieldValueProvider {

    private static final Logger log = LoggerFactory.getLogger(AnnotationFieldValueProvider.class);

    private final ConcurrentMap<Class, String> beanNameCache = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    public AnnotationFieldValueProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object getFieldValue(Field field, Object fieldOwner) {
        if (!supportsField(field)) {
            return null;
        }

        Named named = field.getAnnotation(Named.class);
        String name = (named != null) ? named.value() : "";

        log.debug("Search bean for field '{}' of '{}'", field.getName(), fieldOwner);

        String beanName = getBeanName(field, name);
        if (beanName == null) {
            log.warn("Bean name is null");
            return null;
        }

        Class<?> beanType = field.getType();

        log.debug("Retrieve bean with name '{}' and type '{}'", beanName, beanType);
        Object bean = applicationContext.getBean(beanName, beanType);
        log.debug("Found bean '{}'", bean);

        return bean;
    }

    @Override
    public boolean supportsField(Field field) {
        return field.isAnnotationPresent(Inject.class);
    }

    private String getBeanName(Field field, String name) {
        if ((name == null) || name.isEmpty()) {
            Class<?> fieldType = field.getType();
            // check cache
            name = beanNameCache.get(fieldType);
            if (name == null) {
                name = getBeanNameOfClass(fieldType);
                if (name != null) {
                    String tmpName = beanNameCache.putIfAbsent(fieldType, name);
                    if (tmpName != null) {
                        name = tmpName;
                    }
                }
            }
        }

        return name;
    }

    private String getBeanNameOfClass(Class<?> clazz) {
        // get the list of all possible matching beans
        List<String> names = Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext, clazz));

        // filter out beans that are not candidates for auto wiring
        if (applicationContext instanceof AbstractApplicationContext) {
            Iterator<String> iterator = names.iterator();
            while (iterator.hasNext()) {
                String possibility = iterator.next();
                BeanDefinition beanDef = getBeanDefinition(
                        ((AbstractApplicationContext) applicationContext).getBeanFactory(), possibility);
                if (BeanFactoryUtils.isFactoryDereference(possibility) ||
                        possibility.startsWith("scopedTarget.") ||
                        (beanDef != null && !beanDef.isAutowireCandidate())) {
                    iterator.remove();
                }
            }
        }

        if (names.isEmpty()) {
            return null;
        }

        if (names.size() > 1) {
            if (applicationContext instanceof AbstractApplicationContext) {
                List<String> primaries = new ArrayList<>();
                for (String name : names) {
                    BeanDefinition beanDef = getBeanDefinition(
                            ((AbstractApplicationContext) applicationContext).getBeanFactory(), name);
                    if (beanDef instanceof AbstractBeanDefinition) {
                        if (beanDef.isPrimary()) {
                            primaries.add(name);
                        }
                    }
                }
                if (primaries.size() == 1) {
                    return primaries.get(0);
                }
            }
            StringBuilder message = new StringBuilder();
            message.append("More than one bean of type [");
            message.append(clazz.getName());
            message.append("] found, you have to specify the name of the bean ");
            message.append("(@Named(\"foo\") if using @javax.inject classes) in order to resolve this conflict. ");
            message.append("Matched beans: ");
            message.append(names);
            throw new PippoRuntimeException(message.toString());
        }

        return names.get(0);
    }

    private BeanDefinition getBeanDefinition(ConfigurableListableBeanFactory beanFactory, String name) {
        if (beanFactory.containsBeanDefinition(name)) {
            return beanFactory.getBeanDefinition(name);
        }

        BeanFactory parent = beanFactory.getParentBeanFactory();
        if ((parent != null) && (parent instanceof ConfigurableListableBeanFactory)) {
            return getBeanDefinition((ConfigurableListableBeanFactory) parent, name);
        }

        return null;
    }

}
