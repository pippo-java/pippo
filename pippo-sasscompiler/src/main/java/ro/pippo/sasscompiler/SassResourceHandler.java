package ro.pippo.sasscompiler;

/**
 * Created by cory on 22.06.2015.
 */

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssStylesheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.RuntimeMode;
import ro.pippo.core.route.StaticResourceHandler;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Daniel Jipa on 22.06.2015.
 */
public class SassResourceHandler extends StaticResourceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SassResourceHandler.class);

    private final String resourceBasePath;

    private ConcurrentMap<String, String> sourceMap = new ConcurrentHashMap<>();

    public SassResourceHandler(String urlPath, String resourceBasePath) {
        super(urlPath);
        this.resourceBasePath = getNormalizedPath(resourceBasePath);
    }

        @Override
    public URL getResourceUrl(String resourcePath) {
        if (Application.get().getRuntimeMode().equals(RuntimeMode.DEV)){
            sourceMap.clear();
        }
        try{
            URL url = this.getClass().getClassLoader().getResource(getResourceBasePath() + "/" + resourcePath);
            ScssContext.UrlMode urlMode = ScssContext.UrlMode.ABSOLUTE;
            ScssStylesheet scss = ScssStylesheet.get(url.getFile());
            if (scss == null) {
                LOG.error("The scss file {} could not be found", url.getFile());
                return null;
            }
            String content = scss.toString();
            String result = sourceMap.get(content);
            if (result == null) {
                scss.compile(urlMode);
                result = scss.printState();
                sourceMap.put(content, result);
            }
            File file = new File("output.css");
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(result);
            writer.flush();
            writer.close();
            return file.toURI().toURL();
        }catch (Exception e){
            LOG.error("Error", e);
        }
        return null;

    }

    public String getResourceBasePath() {
        return resourceBasePath;
    }

}
