/*
 * Copyright (C) 2018 the original author or authors.
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
package ro.pippo.core.compress;

import ro.pippo.core.util.HttpUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * {@code CompressFilter} will check the need of compression in request’s headers ‘Accept-Encoding’,
 *  and chooses the {@link HttpServletResponseWrapper} to compress the data in response.
 *
 * @author Decebal Suiu
 */
public class CompressFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        HttpServletResponseWrapper wrappedResponse = HttpUtils.getHttpResponseWrapper(response, request);
        chain.doFilter(request, wrappedResponse);
        if (wrappedResponse instanceof CompressedResponseWrapper) {
            ((CompressedResponseWrapper) wrappedResponse).finish();
        }
    }

    @Override
    public void destroy() {
        // do nothing
    }

}
