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
package ro.pippo.demo.upload;

import ro.pippo.core.Application;
import ro.pippo.core.FileItem;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;

import java.io.File;
import java.io.IOException;

/**
 * @author Decebal Suiu
 */
public class UploadApplication extends Application {

    @Override
    protected void onInit() {

//        setMaximumUploadSize(100 * 1024); // 100k

        GET("/", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                routeContext.render("upload");
            }

        });

        POST("/upload", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                String submitter = routeContext.getParameter("submitter").toString();
                System.out.println("submitter = " + submitter);

                // retrieves the value for 'file'
                FileItem file = routeContext.getRequest().getFile("file");
                System.out.println("file = " + file);
                try {
                    // write to disk
//                    file.write(file.getSubmittedFileName());
                    File uploadedFile = new File(file.getSubmittedFileName());
                    file.write(uploadedFile);

                    // send response
                    routeContext.send("Uploaded file to '" + uploadedFile + "'");
                } catch (IOException e) {
                    throw new PippoRuntimeException(e); // to display the error stack as response
                }
            }

        });
    }

}
