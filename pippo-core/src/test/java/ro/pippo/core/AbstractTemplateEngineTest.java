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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.Router;

import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractTemplateEngineTest {

    private static final String EXPECTED_LANGUAGE = "en_US";
    private static final Locale EXPECTED_LOCALE = Locale.forLanguageTag(EXPECTED_LANGUAGE);
    private static final String EXPECTED_DEFAULT_FILE_EXTENSION = "test";
    private static final String EXPECTED_FILE_EXTENSION = "foo";
    private static final String EXPECTED_TEMPLATE_PATH_PREFIX = "/testTemplates";

    @Mock
    private Application mockApplication;

    @Mock
    private PippoSettings mockPippoSettings;

    @Mock
    private Languages mockLanguages;

    @Mock
    private Messages mockMessages;

    @Mock
    private Router mockRouter;

    private TestTemplateEngine templateEngine;

    @Before
    public void setupApplication() {
        MockitoAnnotations.initMocks(this);
        templateEngine = new TestTemplateEngine();

        when(mockApplication.getPippoSettings()).thenReturn(mockPippoSettings);
        when(mockApplication.getLanguages()).thenReturn(mockLanguages);
        when(mockApplication.getMessages()).thenReturn(mockMessages);
        when(mockApplication.getRouter()).thenReturn(mockRouter);
    }

    @After
    public void tearDown() {
        reset(mockApplication);
        reset(mockLanguages);
    }

    @Test
    public void shouldGetLocaleOrDefaultForSpecifiedLanguageFromApplicationLanguages() {
        when(mockLanguages.getLocaleOrDefault(EXPECTED_LANGUAGE)).thenReturn(EXPECTED_LOCALE);

        templateEngine.init(mockApplication);

        Locale locale = templateEngine.getLocaleOrDefault(EXPECTED_LANGUAGE);

        assertThat(locale, is(EXPECTED_LOCALE));

        verify(mockLanguages, times(1)).getLocaleOrDefault(EXPECTED_LANGUAGE);
        verify(mockApplication, times(1)).getLanguages();
    }

    @Test
    public void shouldGetLocaleOrDefaultForSpecifiedRouteContextFromApplicationLanguages() {
        RouteContext routeContext = mock(RouteContext.class);

        when(mockLanguages.getLocaleOrDefault(any(RouteContext.class))).thenReturn(EXPECTED_LOCALE);

        templateEngine.init(mockApplication);

        Locale locale = templateEngine.getLocaleOrDefault(routeContext);

        assertThat(locale, is(EXPECTED_LOCALE));

        verify(mockLanguages, times(1)).getLocaleOrDefault(eq(routeContext));
        verify(mockApplication, times(1)).getLanguages();
    }

    @Test
    public void shouldGetLanguageOrDefaultForSpecifiedLocaleFromApplicationLanguages() {
        when(mockLanguages.getLanguageOrDefault(EXPECTED_LANGUAGE)).thenReturn(EXPECTED_LANGUAGE);

        templateEngine.init(mockApplication);

        String language = templateEngine.getLanguageOrDefault(EXPECTED_LANGUAGE);

        assertThat(language, is(EXPECTED_LANGUAGE));

        verify(mockLanguages, times(1)).getLanguageOrDefault(EXPECTED_LANGUAGE);
        verify(mockApplication, times(1)).getLanguages();
    }

    @Test
    public void shouldGetLanguageOrDefaultForSpecifiedRouteContextFromApplicationLanguages() {
        RouteContext routeContext = mock(RouteContext.class);

        when(mockLanguages.getLanguageOrDefault(any(RouteContext.class))).thenReturn(EXPECTED_LANGUAGE);

        templateEngine.init(mockApplication);

        String language = templateEngine.getLanguageOrDefault(routeContext);

        assertThat(language, is(EXPECTED_LANGUAGE));

        verify(mockLanguages, times(1)).getLanguageOrDefault(eq(routeContext));
        verify(mockApplication, times(1)).getLanguages();
    }

    @Test
    public void shouldReturnApplicationMessages() {
        templateEngine.init(mockApplication);

        Messages messages = templateEngine.getMessages();

        assertThat(messages, is(mockMessages));

        verify(mockApplication, times(1)).getMessages();
    }

    @Test
    public void shouldReturnApplicationSettings() {
        templateEngine.init(mockApplication);

        PippoSettings pippoSettings = templateEngine.getPippoSettings();

        assertThat(pippoSettings, is(mockPippoSettings));

        verify(mockApplication, times(1)).getPippoSettings();
    }

    @Test
    public void shouldReturnApplicationRouter() {
        templateEngine.init(mockApplication);

        Router router = templateEngine.getRouter();

        assertThat(router, is(mockRouter));

        verify(mockApplication, times(1)).getRouter();
    }

    @Test
    public void shouldReturnDefaultTemplateFileExtensionIfNotConfiguredInSettings() {
        when(mockPippoSettings.getString(anyString(), anyString()))
            .then(args -> args.getArgumentAt(1, String.class));

        templateEngine.init(mockApplication);

        String fileExtension = templateEngine.getFileExtension();

        assertThat(fileExtension, is(EXPECTED_DEFAULT_FILE_EXTENSION));

        verify(mockPippoSettings, times(1))
            .getString(eq(PippoConstants.SETTING_TEMPLATE_EXTENSION), eq(EXPECTED_DEFAULT_FILE_EXTENSION));
    }

    @Test
    public void shouldReturnTemplateFileExtensionConfiguredInSettings() {
        when(mockPippoSettings.getString(anyString(), anyString())).thenReturn(EXPECTED_FILE_EXTENSION);

        templateEngine.init(mockApplication);

        String fileExtension = templateEngine.getFileExtension();

        assertThat(fileExtension, is(EXPECTED_FILE_EXTENSION));

        verify(mockPippoSettings, times(1))
            .getString(eq(PippoConstants.SETTING_TEMPLATE_EXTENSION), eq(EXPECTED_DEFAULT_FILE_EXTENSION));
    }

    @Test
    public void shouldReturnDefaultTemplatePathPrefixIfNotConfiguredInSettings() {
        when(mockPippoSettings.getString(anyString(), anyString()))
            .then(args -> args.getArgumentAt(1, String.class));

        templateEngine.init(mockApplication);

        String templatePathPrefix = templateEngine.getTemplatePathPrefix();

        assertThat(templatePathPrefix, is(TemplateEngine.DEFAULT_PATH_PREFIX));

        verify(mockPippoSettings, times(1))
            .getString(eq(PippoConstants.SETTING_TEMPLATE_PATH_PREFIX), eq(TemplateEngine.DEFAULT_PATH_PREFIX));
    }

    @Test
    public void shouldReturnTemplatePathPrefixConfiguredInSettings() {
        when(mockPippoSettings.getString(anyString(), anyString()))
            .thenReturn(EXPECTED_TEMPLATE_PATH_PREFIX);

        templateEngine.init(mockApplication);

        String templatePathPrefix = templateEngine.getTemplatePathPrefix();

        assertThat(templatePathPrefix, is(EXPECTED_TEMPLATE_PATH_PREFIX));

        verify(mockPippoSettings, times(1))
            .getString(eq(PippoConstants.SETTING_TEMPLATE_PATH_PREFIX), eq(TemplateEngine.DEFAULT_PATH_PREFIX));
    }

    private static class TestTemplateEngine extends AbstractTemplateEngine {

        @Override
        public void init(Application application) {
            super.init(application);
        }

        @Override
        public void renderString(String templateContent, Map<String, Object> model, Writer writer) {
            // do nothing
        }

        @Override
        public void renderResource(String templateName, Map<String, Object> model, Writer writer) {
            // do nothing
        }

        @Override
        protected String getDefaultFileExtension() {
            return EXPECTED_DEFAULT_FILE_EXTENSION;
        }

    }

}
