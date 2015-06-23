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
package ro.pippo.demo.csscompilers;

import ro.pippo.core.Application;
import ro.pippo.core.TemplateHandler;
import ro.pippo.less4j.LessResourceHandler;
import ro.pippo.sasscompiler.SassResourceHandler;

/**
 * @author Daniel Jipa
 */
public class CssCompilerApplication extends Application {

    @Override
    protected void onInit() {
        getRouter().ignorePaths("/favicon.ico");

        addPublicResourceRoute();

        addResourceRoute(new LessResourceHandler("/lesscss", "public/less"));
        addResourceRoute(new SassResourceHandler("/sasscss", "public/sass"));

        GET("/", new TemplateHandler("index.html"));
    }

}
