/*
 * Copyright (C) 2017 the original author or authors.
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
package ro.pippo.pac4j;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.HttpActionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A implementation of {@link HttpActionAdapter}.
 * It's used when a {@link org.pac4j.core.config.Config} is created.
 *
 * {@code
 * Config config = new Config(clients);
 * config.setHttpActionAdapter(new PippoHttpActionAdapter());
 * }
 *
 * @author Decebal Suiu
 */
public class PippoHttpActionAdapter implements HttpActionAdapter<Void, PippoWebContext> {

    private static final Logger log = LoggerFactory.getLogger(Pac4jSecurityHandler.class);

    @Override
    public Void adapt(int code, PippoWebContext context) {
        log.debug("requires HTTP action: {}", code);
        if (code == HttpConstants.UNAUTHORIZED) {
            context.getResponse().unauthorized().send("authentication required");
        } else if (code == HttpConstants.FORBIDDEN) {
            context.getResponse().forbidden().send("forbidden");
        } else if (code == HttpConstants.TEMP_REDIRECT) {
            String location = context.getResponse().getHeader(HttpConstants.LOCATION_HEADER);
            context.getResponse().redirect(location);
        } else if (code == HttpConstants.BAD_REQUEST) {
            context.getResponse().badRequest().send("bad request");
        } else if (code == HttpConstants.OK) {
            context.getResponse().ok().html();
        } else {
            String message = "Unsupported HTTP action: " + code;
            log.error(message);
            throw new TechnicalException(message);
        }

        return null;
    }

}
