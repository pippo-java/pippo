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

import ro.pippo.controller.Context;
import ro.pippo.controller.Float;

/**
 * @author James Moger
 */
public class FloatExtractor implements ArgumentExtractor, NamedExtractor, ConfigurableExtractor<Float> {

    private String name;
    private float defaultValue;

    @Override
    public Class<Float> getAnnotationClass() {
        return Float.class;
    }

    @Override
    public void configure(Float annotation) {
        setName(annotation.value());
        setDefaultValue(annotation.defaultValue());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public float getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(float defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public Object extract(Context context) {
        return context.getParameter(name).toFloat(defaultValue);
    }

}
