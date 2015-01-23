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
package ro.pippo.core;

import ro.pippo.core.util.StringUtils;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author James Moger
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Error implements Serializable {

    private static final long serialVersionUID = 1L;

    public int statusCode;
    public String statusMessage;
    public String requestMethod;
    public String requestUri;
    public String message;
    public String stacktrace;

    public Error() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(statusMessage).append(" (").append(statusCode).append(")\n");
        sb.append(requestMethod).append(' ').append(requestUri).append('\n');

        if (!StringUtils.isNullOrEmpty(message)) {
            sb.append(message).append('\n');
        }

        if (!StringUtils.isNullOrEmpty(stacktrace)) {
            sb.append('\n').append(stacktrace).append('\n');
        }

        return sb.toString();
    }

    /**
     * Get the Error as a Map.
     *
     * @return the error as a map
     */
    public Map<String, Object> asMap() {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("statusCode", statusCode);
        map.put("statusMessage", statusMessage);
        map.put("requestMethod", requestMethod);
        map.put("requestUri", requestUri);

        if (!StringUtils.isNullOrEmpty(message)) {
            map.put("message", message);
        }
        if (!StringUtils.isNullOrEmpty(stacktrace)) {
            map.put("stacktrace", stacktrace);
        }

        return map;
    }

}