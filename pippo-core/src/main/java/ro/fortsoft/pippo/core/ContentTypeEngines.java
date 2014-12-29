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

import ro.fortsoft.pippo.core.util.StringUtils;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for registered content type engines. The main purpose of this
 * object is to gracefully handle mapping a complex content-type or accept
 * header to an engine.
 *
 * @author James Moger
 *
 */
public class ContentTypeEngines {

    private static final Logger log = LoggerFactory.getLogger(ContentTypeEngines.class);

    private final Map<String, ContentTypeEngine> engines;

    public ContentTypeEngines() {
        this.engines = new TreeMap<>();
    }

    /**
     * Returns true if there is an engine registered for the content type.
     *
     * @param contentType
     * @return true if there is an engine for the content type
     */
    public boolean hasContentTypeEngine(String contentType) {
        String sanitizedTypes = sanitizeContentTypes(contentType);
        String[] types = sanitizedTypes.split(",");
        for (String type : types) {
            if (engines.containsKey(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Registers a content type engine if no other engine has been registered
     * for the content type.
     *
     * @param engineClass
     */
    public void registerContentTypeEngine(Class<? extends ContentTypeEngine> engineClass) {
        ContentTypeEngine engine = null;
        try {
            engine = engineClass.newInstance();
        } catch (Exception e) {
            throw new PippoRuntimeException("Failed to instantiate '{}'", e, engineClass.getName());
        }
        if (!engines.containsKey(engine.getContentType())) {
            setContentTypeEngine(engine);
        } else {
            log.debug("'{}' content engine already registered, ignoring '{}'", engine.getContentType(),
                    engineClass.getName());
        }
    }

    /**
     * Returns the first matching content type engine for a simple content type
     * or a complex accept header like:
     *
     * <pre>
     * text/html,application/xhtml+xml,application/xml;q=0.9,image/webp
     * </pre>
     *
     * @param contentType
     * @return null or the first matching content type engine
     */
    public ContentTypeEngine getContentTypeEngine(String contentType) {
        if (StringUtils.isNullOrEmpty(contentType)) {
            return null;
        }

        String sanitizedTypes = sanitizeContentTypes(contentType);
        String[] types = sanitizedTypes.split(",");
        for (String type : types) {
            ContentTypeEngine engine = engines.get(type);
            if (engine != null) {
                return engine;
            }
        }

        return null;
    }

    /**
     * Sets the engine for it's specified content type.
     *
     * @param engine
     */
    public void setContentTypeEngine(ContentTypeEngine engine) {
        engines.put(engine.getContentType(), engine);
        log.debug("'{}' content engine is '{}'", engine.getContentType(), engine.getClass().getName());
    }

    /**
     * Cleans a complex content-type or accept header value by removing the
     * quality scores.
     *
     * <pre>
     * text/html,application/xhtml+xml,application/xml;q=0.9,image/webp
     * </pre>
     *
     * @param contentType
     * @return null or the first matching content type engine
     */
    protected String sanitizeContentTypes(String contentType) {
        if (StringUtils.isNullOrEmpty(contentType)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        String[] types = contentType.split(",");
        for (String type : types) {
            if (type.contains(";")) {
                // drop ;q=0.8 quality scores
                type = type.substring(0, type.indexOf(';'));
            }

            sb.append(type).append(',');
        }

        // trim trailing comma
        sb.setLength(sb.length() - 1);

        return sb.toString();
    }
}
