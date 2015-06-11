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
package ro.pippo.tomcat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.pippo.core.AbstractWebServer;
import ro.pippo.core.Application;
import ro.pippo.core.PippoFilter;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.StringUtils;

/**
 * @author Daniel Jipa
 */
public class TomcatServer extends AbstractWebServer {

	private static final Logger log = LoggerFactory
			.getLogger(TomcatServer.class);

	private Application application;

	private Tomcat tomcat;

//	private PippoServlet pippoServlet;


	@Override
	public void setPippoFilter(PippoFilter pippoFilter) {
		super.setPippoFilter(pippoFilter);
		application = pippoFilter.getApplication();
	}
	
	@Override
	public void start() {
		if (StringUtils.isNullOrEmpty(pippoFilterPath)){
			pippoFilterPath = "/*";
		}
//		pippoServlet = new PippoServlet();
//		pippoServlet.setApplication(application);
		tomcat = new Tomcat();
		tomcat.setPort(settings.getPort());
		File base = new File(System.getProperty("java.io.tmpdir"));
		Context rootCtx = tomcat.addContext(settings.getContextPath(), base.getAbsolutePath());
		rootCtx.addFilterDef(createFilterDef("pippoFilter", pippoFilter));
		rootCtx.addFilterMap(createFilterMap("pippoFilter", pippoFilterPath));
//		Tomcat.addServlet(rootCtx, "pippoServlet", pippoServlet);
//		rootCtx.addServletMapping("/*", "pippoServlet");
		try {
			String version = tomcat.getClass().getPackage()
					.getImplementationVersion();
			log.info("Starting Tomcat Server {} on port {}", version,
					settings.getPort());
			tomcat.start();
		} catch (LifecycleException e) {
			throw new PippoRuntimeException(e);
		}
		tomcat.getServer().await();
	}
	
	private FilterDef createFilterDef(String filterName, Filter filter) {
		FilterDef filterDef = new FilterDef();
		filterDef.setFilterName(filterName);
		filterDef.setFilter(filter);
		return filterDef;
	}

	private FilterMap createFilterMap(String filterName, String urlPattern) {
		FilterMap filterMap = new FilterMap();
		filterMap.setFilterName(filterName);
		filterMap.addURLPattern(urlPattern);
		return filterMap;
	}
	
	
	@Override
	public void stop() {
		if (tomcat != null) {
			try {
				tomcat.stop();
			} catch (Exception e) {
				throw new PippoRuntimeException("Cannot stop Tomcat Server", e);
			}
		}
	}

}