package ro.pippo.demo.template;

import ro.pippo.core.Pippo;
import ro.pippo.rythm.RythmTemplateEngine;

public class RythmDemo {
	public static void main(String[] args) {
        Pippo pippo = new Pippo(new TemplateApplication(new RythmTemplateEngine(), "rythm/hello.html"));
        pippo.start();
    }
}
