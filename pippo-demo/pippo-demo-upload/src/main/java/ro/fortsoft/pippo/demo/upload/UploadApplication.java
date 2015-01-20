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
package ro.fortsoft.pippo.demo.upload;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.FileItem;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;

import java.io.File;
import java.io.IOException;

/**
 * @author Decebal Suiu
 */
public class UploadApplication extends Application {

    @Override
    public void init() {
        super.init();

//        setMaximumUploadSize(100 * 1024); // 100k

        GET("/", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                response.render("upload");
            }

        });

        POST("/upload", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                String submitter = request.getParameter("submitter").toString();
                System.out.println("submitter = " + submitter);

                // retrieves the value for 'file'
                FileItem file = request.getFile("file");
                System.out.println("file = " + file);
                try {
                    // write to disk
//                    file.write(file.getSubmittedFileName());
                    File uploadedFile = new File(file.getSubmittedFileName());
                    file.write(uploadedFile);

                    // send response
                    response.send("Uploaded file to '" + uploadedFile + "'");
                } catch (IOException e) {
                    throw new PippoRuntimeException(e); // to display the error stack as response
                }
            }

        });
    }

}
