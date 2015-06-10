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
package ro.pippo.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import ro.pippo.controller.Controller;
import ro.pippo.controller.ControllerFactory;

/**
 * A ControllerFactory implementation that delegates to the Spring container to instantiate a given class type.
 * This allows for the instance to be configured via dependency injection.
 *
 * @author Decebal Suiu
 */
public class SpringControllerFactory implements ControllerFactory {

    private static final Logger log = LoggerFactory.getLogger(SpringControllerFactory.class);

    private ApplicationContext applicationContext;
    private BeanDefinitionRegistry beanDefinitionRegistry;
    private boolean autoRegistering;

    public SpringControllerFactory(ApplicationContext applicationContext) {
        this(applicationContext, true);
    }

    public SpringControllerFactory(ApplicationContext applicationContext, boolean autoRegistering) {
        this.applicationContext = applicationContext;
        this.autoRegistering = autoRegistering;

        if (autoRegistering) {
            beanDefinitionRegistry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
        }
    }

    @Override
    public <T extends Controller> T createController(Class<T> controllerClass) {
        String beanName = controllerClass.getName();
        if (autoRegistering && !beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            log.debug("Create bean definition for '{}'", beanName);
            BeanDefinition definition = createBeanDefinition(controllerClass);
            log.debug("Register '{}' as bean", beanName);
            beanDefinitionRegistry.registerBeanDefinition(beanName, definition);
        }

        return applicationContext.getBean(controllerClass);
    }

    /**
     * Created a bean definition for a class.
     * Optionally configure all bean properties, like scope, prototype/singleton, etc using:
     * <p/>
     * <pre>
     * BeanDefinition definition = super.createBeanDefinition(beanClass);
     * definition.setScope(BeanDefinition.SCOPE_SINGLETON);
     * return definition;
     * </pre>
     *
     * @param controllerClass
     * @return
     */
    protected BeanDefinition createBeanDefinition(Class<? extends  Controller> controllerClass) {
        // optionally configure all bean properties, like scope, prototype/singleton, etc
//        return new RootBeanDefinition(beanClass);
        return new RootBeanDefinition(controllerClass, Autowire.BY_TYPE.value(), true);
    }

}
