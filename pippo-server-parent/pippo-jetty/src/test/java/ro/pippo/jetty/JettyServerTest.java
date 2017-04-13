package ro.pippo.jetty;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import ro.pippo.core.Application;
import ro.pippo.core.Pippo;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.RuntimeMode;


public class JettyServerTest {
    @Rule
    public TestRule globalTimeout = new Timeout(1000);

    @Test
    public void testStartInTest() {
        startPippo(RuntimeMode.TEST);
    }

    @Test
    public void testStartInProd() {
        startPippo(RuntimeMode.PROD);
    }

    private static void startPippo(final RuntimeMode runtimeMode) {
        Pippo pippo = null;

        try {
            final PippoSettings pippoSettings = new PippoSettings(runtimeMode);
            pippo = new Pippo(new Application(pippoSettings));
            pippo.start();
        } finally {
            if (pippo != null) {
                pippo.stop();
            }
        }
    }
}
