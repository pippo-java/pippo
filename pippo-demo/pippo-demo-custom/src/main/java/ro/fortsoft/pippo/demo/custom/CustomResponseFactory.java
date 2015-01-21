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
package ro.fortsoft.pippo.demo.custom;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.ResponseFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * This class constructs a CustomResponse to be passed to all handlers.
 *
 * @author James Moger
 */
public class CustomResponseFactory implements ResponseFactory<CustomResponse> {

    @Override
    public CustomResponse createResponse(HttpServletResponse httpServletResponse, Application application) {
        return new CustomResponse(httpServletResponse, application);
    }

    @Override
    public void init(Application application) {
    }

    @Override
    public void destroy(Application application) {
    }

}
