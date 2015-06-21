package ro.pippo.sasscompiler;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssStylesheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.CssGenerator;

import java.io.*;
import java.net.URL;

/**
 * Created by cory on 21.06.2015.
 */
public abstract class SassCompiler implements CssGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(SassCompiler.class);
    @Override
    public String getCss() {
        try{
            URL url = getResource();
            ScssContext.UrlMode urlMode = ScssContext.UrlMode.ABSOLUTE;
            ScssStylesheet scss = ScssStylesheet.get(url.getFile());
            if (scss == null) {
                LOG.error("The scss file {} could not be found", url.getFile());
                return null;
            }
            scss.compile(urlMode);
            return scss.printState();
        }catch(Exception e){
            return null;
        }
    }


}
