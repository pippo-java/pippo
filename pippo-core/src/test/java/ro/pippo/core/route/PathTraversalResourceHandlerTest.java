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
package ro.pippo.core.route;

import org.junit.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PathTraversalResourceHandlerTest {

    @Test
    public void classpathResourceHandlerTest() throws URISyntaxException {
        ClasspathResourceHandler handler = new ClasspathResourceHandler("/", "/public");

        URL resourceUrl = handler.getResourceUrl("VISIBLE");
        assertNotNull(resourceUrl);
        Path visibleFile = Paths.get(resourceUrl.toURI());
        assertNotNull(visibleFile);
        Path basePath = visibleFile.getParent();
        URL url = handler.getResourceUrl("../HIDDEN");
        if (url != null) {
            assertTrue("Path traversal security issue", Paths.get(url.toURI()).startsWith(basePath));
        }
    }

    @Test
    public void webjarResourceHandlerTest() throws URISyntaxException {
        WebjarsResourceHandler handler = new WebjarsResourceHandler();

        URL url = handler.getResourceUrl("../../../HIDDEN");
        assertNull(url);
    }

    @Test
    public void publicResourceHandlerTest() throws URISyntaxException {
        PublicResourceHandler handler = new PublicResourceHandler();

        URL resourceUrl = handler.getResourceUrl("VISIBLE");
        assertNotNull(resourceUrl);
        Path visibleFile = Paths.get(resourceUrl.toURI());
        assertNotNull(visibleFile);
        Path basePath = visibleFile.getParent();
        URL url = handler.getResourceUrl("../HIDDEN");
        if (url != null) {
            assertTrue("Path traversal security issue", Paths.get(url.toURI()).startsWith(basePath));
        }
    }

}
