package ro.fortsoft.pippo.demo;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.FileItem;
import ro.fortsoft.pippo.core.Pippo;
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
public class UploadDemo {

    public static void main(String[] args) {
        Pippo pippo = new Pippo();
//        pippo.getServer().getSettings().staticFilesLocation("/public");

        Application application = pippo.getApplication();
//        application.setMaximumUploadSize(100 * 1024); // 100k

        application.GET("/", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                response.render("upload.ftl");
            }

        });

        application.POST("/upload", new RouteHandler() {

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

        pippo.start();
    }

}
