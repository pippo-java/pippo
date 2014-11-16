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
package ro.fortsoft.pippo.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import ro.fortsoft.pippo.ioc.FieldValueProvider;

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

    private final ConcurrentMap<Class, String> beanNameCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<BeanProvider, Object> cache = new ConcurrentHashMap<>();

    private ApplicationContextProvider applicationContextProvider;

    public AnnotationFieldValueProvider(ApplicationContextProvider applicationContextProvider) {
        this.applicationContextProvider = applicationContextProvider;
    }

    @Override
    public Object getFieldValue(Field field, Object fieldOwner) {
        if (!supportsField(field)) {
            return null;
        }

        Named named = field.getAnnotation(Named.class);
        String name = (named != null) ? named.value() : "";
        boolean required = true;

        String beanName = getBeanName(field, name, required);
        if (beanName == null) {
            return null;
        }

        BeanProvider locator = new BeanProvider(beanName, field.getType(), applicationContextProvider);

        // only check the cache if the bean is a singleton
        Object cachedValue = cache.get(locator);
        if (cachedValue != null) {
            return cachedValue;
        }

        Object target = null;
        /*
        try {
            // check whether there is a bean with the provided properties
            target = locator.locateProxyTarget();
        } catch (IllegalStateException e) {
            if (required) {
                throw e;
            } else {
                return null;
            }
        }

        if (wrapInProxies) {
            target = LazyInitProxyFactory.createProxy(field.getType(), locator);
        }
        */

        // only put the proxy into the cache if the bean is a singleton
        if (locator.isSingletonBean()) {
            Object tmpTarget = cache.putIfAbsent(locator, target);
            if (tmpTarget != null) {
                target = tmpTarget;
            }
        }

        return target;
    }

    @Override
    public boolean supportsField(Field field) {
        return field.isAnnotationPresent(Inject.class);
    }

    private String getBeanName(Field field, String name, boolean required) {
        if ((name == null) || name.isEmpty()) {
            Class<?> fieldType = field.getType();
            // check cache
            name = beanNameCache.get(fieldType);
            if (name == null) {
                name = getBeanNameOfClass(fieldType, required);
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

    private String getBeanNameOfClass(Class<?> clazz, boolean required) {
        ApplicationContext applicationContext = getApplicationContext();

        // get the list of all possible matching beans
        List<String> names = new ArrayList<String>(
                Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext, clazz)));

        // filter out beans that are not candidates for autowiring
        if (applicationContext instanceof AbstractApplicationContext) {
            Iterator<String> it = names.iterator();
            while (it.hasNext()) {
                String possibility = it.next();
                BeanDefinition beanDef = getBeanDefinition(
                        ((AbstractApplicationContext) applicationContext).getBeanFactory(), possibility);
                if (BeanFactoryUtils.isFactoryDereference(possibility) ||
                        possibility.startsWith("scopedTarget.") ||
                        (beanDef != null && !beanDef.isAutowireCandidate())) {
                    it.remove();
                }
            }
        }

        if (names.isEmpty()) {
            if (required) {
                throw new IllegalStateException("Bean of type [" + clazz.getName() + "] not found");
            }

            return null;
        } else if (names.size() > 1) {
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
            StringBuilder msg = new StringBuilder();
            msg.append("More than one bean of type [");
            msg.append(clazz.getName());
            msg.append("] found, you have to specify the name of the bean ");
            msg.append("(@Named(\"foo\") if using @javax.inject classes) in order to resolve this conflict. ");
            msg.append("Matched beans: ");
            // TODO
//            msg.append(Strings.join(",", names.toArray(new String[names.size()])));
            throw new IllegalStateException(msg.toString());
        } else {
            return names.get(0);
        }
    }

    private BeanDefinition getBeanDefinition(ConfigurableListableBeanFactory beanFactory, String name) {
        if (beanFactory.containsBeanDefinition(name)) {
            return beanFactory.getBeanDefinition(name);
        } else {
            BeanFactory parent = beanFactory.getParentBeanFactory();
            if ((parent != null) && (parent instanceof ConfigurableListableBeanFactory)) {
                return getBeanDefinition((ConfigurableListableBeanFactory) parent, name);
            }
        }

        return null;
    }

    private ApplicationContext getApplicationContext() {
        return applicationContextProvider.getApplicationContext();
    }

}
