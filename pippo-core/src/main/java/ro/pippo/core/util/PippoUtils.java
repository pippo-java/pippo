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
package ro.pippo.core.util;

import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoRuntimeException;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author James Moger
 */
public class PippoUtils {

    public static final String PIPPO_LOGO = "\n"
        + " ____  ____  ____  ____  _____\n"
        + "(  _ \\(_  _)(  _ \\(  _ \\(  _  )\n"
        + " ) __/ _)(_  ) __/ ) __/ )(_)(   http://pippo.ro\n"
        + "(__)  (____)(__)  (__)  (_____)  {}\n";

    private PippoUtils() {}

    public static String getPippoLogo() {
        return StringUtils.format(PIPPO_LOGO, getPippoVersion());
    }

    /**
     * Simply reads a property resource file that contains the version of this
     * Pippo build. Helps to identify the Pippo version currently running.
     *
     * @return The version of Pippo. Eg. "1.6-SNAPSHOT" while developing of "1.6" when released.
     */
    public static String getPippoVersion() {
        // and the key inside the properties file.
        String pippoVersionPropertyKey = "pippo.version";

        String pippoVersion;

        try {
            Properties prop = new Properties();
            URL url = ClasspathUtils.locateOnClasspath(PippoConstants.LOCATION_OF_PIPPO_BUILTIN_PROPERTIES);
            InputStream stream = url.openStream();
            prop.load(stream);

            pippoVersion = prop.getProperty(pippoVersionPropertyKey);
        } catch (Exception e) {
            //this should not happen. Never.
            throw new PippoRuntimeException("Something is wrong with your build. Cannot find resource {}",
                PippoConstants.LOCATION_OF_PIPPO_BUILTIN_PROPERTIES);
        }

        return pippoVersion;
    }

}
