package ro.pippo.demo.less4j;

import ro.pippo.core.Pippo;

/**
 * Created by cory on 20.06.2015.
 */
public class CssCompilerDemo {
    public static void main(String[] args) {
        Pippo pippo = new Pippo(new CssCompilerApplication());
        pippo.start();
    }

}
