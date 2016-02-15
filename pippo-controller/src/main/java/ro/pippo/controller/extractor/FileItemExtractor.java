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
import ro.pippo.core.FileItem;

/**
 * @author James Moger
 */
public class FileItemExtractor implements NamedExtractor, TypedExtractor, ArgumentExtractor {

    private String name;

    @Override
    public void setObjectType(Class<?> objectType) {
        // TODO resolve
//        Preconditions.checkArgument(FileItem.class == objectType, "'{}' is not a valid target type", objectType.getName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public FileItem extract(Context context) {
        return context.getRequest().getFile(name);
    }

}
