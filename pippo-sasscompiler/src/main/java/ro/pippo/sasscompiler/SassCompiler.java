package ro.pippo.sasscompiler;

import ro.pippo.core.CssGenerator;

/**
 * Created by cory on 21.06.2015.
 */
public abstract class SassCompiler implements CssGenerator {
    @Override
    public String getCss() {
        return "p { background-color:#FFFFFF; }";
    }
}
