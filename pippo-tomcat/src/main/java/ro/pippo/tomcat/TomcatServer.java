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

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.pippo.core.AbstractWebServer;
import ro.pippo.core.PippoFilter;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.StringUtils;

/**
 * @author Daniel Jipa
 */
public class TomcatServer extends AbstractWebServer {

	private static final Logger log = LoggerFactory
			.getLogger(TomcatServer.class);

	private Tomcat server;

	@Override
	public PippoFilter getPippoFilter() {
		return null;
	}

	@Override
	public void start() {
		server = new Tomcat();

		Connector connector = new Connector();
		connector.setPort(settings.getPort());
		server.setConnector(connector);
		StandardHost host = new StandardHost();
		host.setName(settings.getHost());
		server.setHost(host);

		try {
			Context context = server.addWebapp(settings.getContextPath(), ".");
			FilterDef filterDef = new FilterDef();
		    filterDef.setFilterName("pippo");
		    filterDef.setFilterClass(pippoFilter.getClass().getName());
			context.addFilterDef(filterDef);
			
			FilterMap filterMap = new FilterMap();
		    filterMap.setFilterName("pippo");
		    
		    if (StringUtils.isNullOrEmpty(pippoFilterPath)) {
	            pippoFilterPath = "/*"; // default value
	        }
		    
		    filterMap.addURLPattern(pippoFilterPath);
			context.addFilterMap(filterMap);
					
			String version = server.getClass().getPackage()
					.getImplementationVersion();
			log.info("Starting Tomcat Server {} on port {}", version,
					settings.getPort());
			server.start();
			server.getServer().await();
		} catch (Exception e) {
			throw new PippoRuntimeException(e);
		}

	}

	@Override
	public void stop() {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				throw new PippoRuntimeException("Cannot stop Tomcat Server", e);
			}
		}
	}

}
