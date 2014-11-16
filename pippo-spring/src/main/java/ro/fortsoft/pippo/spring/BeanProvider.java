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

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * @author Decebal Suiu
 */
public class BeanProvider {

    private String beanName;
    private Class<?> beanType;
    private ApplicationContext applicationContext;

    private Boolean singletonCache;

    public BeanProvider(String beanName, Class<?> beanType, ApplicationContextProvider applicationContextProvider) {
        this.beanName = beanName;
        this.beanType = beanType;

        applicationContext = applicationContextProvider.getApplicationContext();
        if (applicationContext == null) {
            throw new IllegalStateException("Spring application context provider returned null");
        }
    }

    public boolean isSingletonBean() {
        if (singletonCache == null) {
            singletonCache = applicationContext.isSingleton(beanName);
        }

        return singletonCache;
    }

    /**
     * Looks up a bean by its name and class. Throws IllegalStateException if bean not found.
     */
    private Object getBean(String name, Class<?> clazz) {
        try {
            return (name != null) ?  applicationContext.getBean(name, clazz) : applicationContext.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalStateException("Bean with name [" + name + "] and class [" +
                    clazz.getName() + "] not found", e);
        }
    }

}
