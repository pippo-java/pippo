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
package ro.pippo.core;

import java.io.Writer;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public interface TemplateEngine {

    public final static String DEFAULT_PATH_PREFIX = "/templates";

    public final static String BAD_REQUEST_400 = "pippo/400badRequest";
    public final static String UNAUTHORIZED_401 = "pippo/401unauthorized";
    public final static String PAYMENT_REQUIRED_402 = "pippo/402paymentRequired";
    public final static String FORBIDDEN_403 = "pippo/403forbidden";
    public final static String NOT_FOUND_404 = "pippo/404notFound";
    public final static String METHOD_NOT_ALLOWED_405 = "pippo/405methodNotAllowed";
    public final static String CONFLICT_409 = "pippo/409conflict";
    public final static String GONE_410 = "pippo/410gone";
    public final static String INTERNAL_ERROR_500 = "pippo/500internalError";
    public final static String NOT_IMPLEMENTED_501 = "pippo/501notImplemented";
    public final static String OVERLOADED_502 = "pippo/502overloaded";
    public final static String SERVICE_UNAVAILABLE_503 = "pippo/503serviceUnavailable";

    void init(Application application);

    void renderString(String templateContent, Map<String, Object> model, Writer writer);

    void renderResource(String templateName, Map<String, Object> model, Writer writer);

    void setFileExtension(String extension);

}
