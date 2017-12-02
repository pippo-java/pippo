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

import org.pac4j.core.http.HttpActionAdapter;

/**
 * No-operation HTTP action adapter for the {@link PippoWebContext}.
 * It's used when a {@link org.pac4j.core.config.Config} is created.
 *
 * {@code
 * Config config = new Config(clients);
 * config.setHttpActionAdapter(PippoNopHttpActionAdapter.INSTANCE);
 * }
 *
 * @author Decebal Suiu
 */
public class PippoNopHttpActionAdapter implements HttpActionAdapter<Void, PippoWebContext> {

    public static final PippoNopHttpActionAdapter INSTANCE = new PippoNopHttpActionAdapter();

    private PippoNopHttpActionAdapter() {
        // prevent instantiation
    }

    @Override
    public Void adapt(int code, PippoWebContext context) {
        return null;
    }

}
