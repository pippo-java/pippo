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
package ro.fortsoft.pippo.core;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Decebal Suiu
 */
public class DefaultExceptionHandler implements ExceptionHandler {

    @Override
    public void handle(Exception exception, Request request, Response response) {
        response.status(HttpConstants.StatusCode.INTERNAL_ERROR);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();
//            stackTrace = stackTrace.replace("\tat ", "&#09;");
//            stackTrace = stackTrace.replace("\tat ", "&nbsp;&nbsp;&nbsp;&nbsp;");
//            stackTrace = stackTrace.replace(System.getProperty("line.separator"), "<br/>\n");

        StringBuilder content = new StringBuilder();
        content.append("<html><body><pre>");
        content.append(stackTrace);
        content.append("</pre></body></html>");
        response.send(content);
    }

}
