package ro.pippo.less4j;

import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.CssGenerator;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Created by cory on 21.06.2015.
 */
public abstract class Less4jCompiler implements CssGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(Less4jCompiler.class);


    @Override
    public String getCss() {
        try{
            ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
            LessCompiler.CompilationResult compilationResult = compiler.compile(getResource());

            for (LessCompiler.Problem warning : compilationResult.getWarnings()) {
                LOG.warn("Line: {}, Character: {}, Message: {} ", warning.getLine(), warning.getCharacter(), warning.getMessage());
            }
            return compilationResult.getCss();
        }catch(Exception e){
            return null;
        }
    }
}
