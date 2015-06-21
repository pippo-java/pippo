package ro.pippo.less4j;

import ro.pippo.core.CssGenerator;

/**
 * Created by cory on 21.06.2015.
 */
public abstract class Less4jCompiler implements CssGenerator {

    @Override
    public String getCss() {
        return "body {\n" +
            "    background-color: red;\n" +
            "}";
    }
}
