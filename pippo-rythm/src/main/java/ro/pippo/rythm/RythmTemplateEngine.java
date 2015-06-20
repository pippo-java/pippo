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
package ro.pippo.rythm;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.rythmengine.Rythm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.pippo.core.Application;
import ro.pippo.core.Languages;
import ro.pippo.core.Messages;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.TemplateEngine;
import ro.pippo.core.route.Router;
import ro.pippo.core.util.StringUtils;

/**
 * @author Daniel Jipa
 */
public class RythmTemplateEngine implements TemplateEngine {

	private static final Logger LOG = LoggerFactory.getLogger(RythmTemplateEngine.class);
	
 	private Languages languages;
	private Messages messages;
	private Router router;
	private String pathPrefix;

    public static final String HTML = "html";
    public static final String FILE_SUFFIX = "." + HTML;

	@Override
	public void init(Application application) {
		this.languages = application.getLanguages();
		this.messages = application.getMessages();
		this.router = application.getRouter();

		PippoSettings pippoSettings = application.getPippoSettings();
		pathPrefix = pippoSettings.getString(
				PippoConstants.SETTING_TEMPLATE_PATH_PREFIX, null);
		if (StringUtils.isNullOrEmpty(pathPrefix)) {
			pathPrefix = TemplateEngine.DEFAULT_PATH_PREFIX;
		}
		pathPrefix = StringUtils.removeStart(pathPrefix, "/");
        pathPrefix = StringUtils.removeEnd(pathPrefix, "/");
		Map<String, Object> conf = new HashMap<String, Object>();
		conf.put("home.template", pathPrefix);
		Rythm.init(conf);
	}

	@Override
	public void renderString(String templateContent, Map<String, Object> model,
			Writer writer) {
		try {
			String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);
	        if (StringUtils.isNullOrEmpty(language)) {
	            language = languages.getLanguageOrDefault(language);
	        }
	        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
	        if (locale == null) {
	            locale = languages.getLocaleOrDefault(language);
	        }
			model.put("pippo", new PippoHelper(messages, language, locale, router));
			writer.write(Rythm.engine().render(templateContent, model));
			writer.flush();
		} catch (IOException e) {
			throw new PippoRuntimeException(e);
		}
	}

	@Override
	public void renderResource(String templateName, Map<String, Object> model,
			Writer writer) {
		try {
			String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);
	        if (StringUtils.isNullOrEmpty(language)) {
	            language = languages.getLanguageOrDefault(language);
	        }
	        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
	        if (locale == null) {
	            locale = languages.getLocaleOrDefault(language);
	        }
            if (templateName.indexOf('.') == -1) {
                templateName += FILE_SUFFIX;
            }
			model.put("pippo", new PippoHelper(messages, language, locale, router));
			String path = pathPrefix + "/" + templateName;
			String fullPath = Thread.currentThread().getContextClassLoader().getResource(path).getFile();
			LOG.debug("Template location {}" , fullPath);
			writer.write(Rythm.engine().render(fullPath, model));
			writer.flush();
		} catch (IOException e) {
			throw new PippoRuntimeException(e);
		}
	}

}
