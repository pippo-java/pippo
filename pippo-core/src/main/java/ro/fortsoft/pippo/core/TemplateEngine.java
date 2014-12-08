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
package ro.fortsoft.pippo.core;

import java.io.Writer;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public abstract class TemplateEngine implements ContentTypeEngine {

    public final static String DEFAULT_PATH_PREFIX = "/templates";

    public final static String NOT_FOUND_404 = "pippo/404notFound";
    public final static String INTERNAL_ERROR_500 = "pippo/500internalError";

    public abstract void render(String templateName, Map<String, Object> model, Writer writer);

    @Override
    public String getContentType() {
        return HttpConstants.ContentType.TEXT_HTML;
    }

}
