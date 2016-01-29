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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Container for registered content type engines. The main purpose of this
 * object is to gracefully handle mapping a suffix, complex content-type,
 * or an accept header to an engine.
 *
 * @author James Moger
 */
public class ContentTypeEngines {

    private static final Logger log = LoggerFactory.getLogger(ContentTypeEngines.class);

    private final Map<String, ContentTypeEngine> engines;

    private final Map<String, ContentTypeEngine> suffixes;

    public ContentTypeEngines() {
        this.engines = new TreeMap<>();
        this.suffixes = new TreeMap<>();
    }

    /**
     * Returns true if there is an engine registered for the content type or content type suffix.
     *
     * @param contentTypeOrSuffix
     * @return true if there is an engine for the content type
     */
    public boolean hasContentTypeEngine(String contentTypeOrSuffix) {
        String sanitizedTypes = sanitizeContentTypes(contentTypeOrSuffix);
        String[] types = sanitizedTypes.split(",");
        for (String type : types) {
            if (engines.containsKey(type)) {
                return true;
            }
        }

        return suffixes.containsKey(contentTypeOrSuffix.toLowerCase());
    }

    /**
     * Returns the list of registered content types.
     *
     * @return the list of registered content types
     */
    public List<String> getContentTypes() {
        return Collections.unmodifiableList(new ArrayList<>(engines.keySet()));
    }

    /**
     * Returns the list of registered content type suffixes.
     *
     * @return the list of registered content type suffixes
     */
    public List<String> getContentTypeSuffixes() {
        return Collections.unmodifiableList(new ArrayList<>(suffixes.keySet()));
    }

    /**
     * Registers a content type engine if no other engine has been registered
     * for the content type.
     *
     * @param engineClass
     * @return the engine instance, if it is registered
     */
    public ContentTypeEngine registerContentTypeEngine(Class<? extends ContentTypeEngine> engineClass) {
        ContentTypeEngine engine;
        try {
            engine = engineClass.newInstance();
        } catch (Exception e) {
            throw new PippoRuntimeException(e, "Failed to instantiate '{}'", engineClass.getName());
        }
        if (!engines.containsKey(engine.getContentType())) {
            setContentTypeEngine(engine);
            return engine;
        } else {
            log.debug("'{}' content engine already registered, ignoring '{}'", engine.getContentType(),
                engineClass.getName());
            return null;
        }
    }

    /**
     * Returns the first matching content type engine for a content type suffix (json, xml, yaml), a
     * simple content type (application/json) or a complex accept header like:
     * <p/>
     * <pre>
     * text/html,application/xhtml+xml,application/xml;q=0.9,image/webp
     * </pre>
     *
     * @param contentTypeOrSuffix
     * @return null or the first matching content type engine
     */
    public ContentTypeEngine getContentTypeEngine(String contentTypeOrSuffix) {
        if (StringUtils.isNullOrEmpty(contentTypeOrSuffix)) {
            return null;
        }

        String sanitizedTypes = sanitizeContentTypes(contentTypeOrSuffix);
        String[] types = sanitizedTypes.split(",");
        for (String type : types) {
            ContentTypeEngine engine = engines.get(type);
            if (engine != null) {
                return engine;
            }
        }

        return suffixes.get(contentTypeOrSuffix.toLowerCase());
    }

    /**
     * Sets the engine for it's specified content type. An suffix for the engine is also registered based on the
     * specific type; extension types are supported and the leading "x-" is trimmed out.
     *
     * @param engine
     */
    public void setContentTypeEngine(ContentTypeEngine engine) {
        String contentType = engine.getContentType();
        String suffix = StringUtils.removeStart(contentType.substring(contentType.lastIndexOf('/') + 1), "x-");

        engines.put(engine.getContentType(), engine);
        suffixes.put(suffix.toLowerCase(), engine);

        log.debug("'{}' content engine is '{}'", engine.getContentType(), engine.getClass().getName());
    }

    /**
     * Cleans a complex content-type or accept header value by removing the
     * quality scores.
     * <p/>
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
