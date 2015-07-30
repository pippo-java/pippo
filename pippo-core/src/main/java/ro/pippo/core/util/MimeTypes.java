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
package ro.pippo.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoSettings;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MimeTypes utils adapted from Play 1.2.4 & Ninja Web Framework
 */
public class MimeTypes {

    private static final Logger log = LoggerFactory.getLogger(MimeTypes.class);

    private final Properties mimetypes;
    private final Pattern extPattern;

    private final PippoSettings pippoSettings;

    public MimeTypes(PippoSettings pippoSettings) {
        this.pippoSettings = pippoSettings;
        this.extPattern = Pattern.compile("^.*\\.([^.]+)$");

        mimetypes = new Properties();
        init();
    }

    /**
     * Returns the mimetype from a file name
     *
     * @param filename the file name
     * @return the mimetype or the empty string if not found
     */
    public String getMimeType(String filename) {
        return getMimeType(filename, "");
    }

    /**
     * Returns the mimetype from a file name.<br/>
     *
     * @param filename        the file name
     * @param defaultMimeType the default mime type to return when no matching mimetype is
     *                        found
     * @return the mimetype
     */
    public String getMimeType(String filename, String defaultMimeType) {
        Matcher matcher = extPattern.matcher(filename.toLowerCase());
        String ext = "";
        if (matcher.matches()) {
            ext = matcher.group(1);
        }
        if (ext.length() > 0) {
            String mimeType = mimetypes.getProperty(ext);
            if (mimeType == null) {
                return defaultMimeType;
            }

            return mimeType;
        }

        return defaultMimeType;
    }

    /**
     * Returns the content-type from a file name. If none is found returning
     * application/octet-stream<br/>
     * For a text-based content-type, also return the encoding suffix eg.
     * <em>"text/plain; charset=utf-8"</em>
     *
     * @param filename     the file name
     * @return the content-type deduced from the file extension.
     */
    public String getContentType(String filename) {
        return getContentType(filename, "application/octet-stream");
    }

    /**
     * Returns the content-type from a file name.<br/>
     * For a text-based content-type, also return the encoding suffix eg.
     * <em>"text/plain; charset=utf-8"</em>
     *
     * @param filename           the file name
     * @param defaultContentType the default content-type to return when no matching
     *                           content-type is found
     * @return the content-type deduced from the file extension.
     */
    public String getContentType(String filename, String defaultContentType) {
        String contentType = getMimeType(filename, null);
        if (contentType == null) {
            contentType = defaultContentType;
        }
        if (contentType != null && contentType.startsWith("text/")) {
            // UTF-8 is fixed for now as Pippo only supports utf-8 in files...
            return contentType + "; charset=utf-8";
        }

        return contentType;
    }

    /**
     * Check the mimetype is referenced in the mimetypes database.
     *
     * @param mimeType the mimeType to verify
     */
    public boolean isValidMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        } else if (mimeType.indexOf(";") != -1) {
            return mimetypes.contains(mimeType.split(";")[0]);
        } else {
            return mimetypes.contains(mimeType);
        }
    }

    private void init() {
        // Load default mimetypes from the framework
        URL url = ClasspathUtils.locateOnClasspath(PippoConstants.LOCATION_OF_PIPPO_MIMETYPE_PROPERTIES);
        if (url == null) {
            log.error("Could not locate '{}'!", PippoConstants.LOCATION_OF_PIPPO_MIMETYPE_PROPERTIES);
        } else {
            try (InputStream is = url.openStream()) {
                mimetypes.load(is);

            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }

        // Load custom mimetypes from the application configuration
        List<String> settingNames = pippoSettings.getSettingNames(PippoConstants.SETTING_MIMETYPE_PREFIX);
        for (String key : settingNames) {
            String type = key.substring(PippoConstants.SETTING_MIMETYPE_PREFIX.length()).toLowerCase();
            String value = pippoSettings.getString(key, null);
            if (!StringUtils.isNullOrEmpty(value)) {
                mimetypes.setProperty(type, value);
            }
        }
    }

}
