package de.kleiber.test;

import java.io.File;

import java.net.URISyntaxException;
import java.net.URL;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


/** Basisklasse, von der alle Tests abgeleitet werden. */
public class SeleniumTest {

    private static Logger log4j;
    protected UITestDriver driverInstance;
    /*
     * Ausführungsreihenfolge:
     *
     * @Rule
     *   evaluate
     *   try
     *      @Before
     *      @Test
     *      @After
     *   catch
     *     Screenshot
     *   finally
     *      after()
     */

    /**
     * Inititalisierung vor Aufruf der Testklasse.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        setLog4jConfigFile();
        log4j = LogManager.getLogger(SeleniumTest.class.getName());
    }

    /**
     * Konfiguration des Loggings.
     */
    private static void setLog4jConfigFile() {
        final URL url = SeleniumTest.class.getProtectionDomain().getCodeSource().getLocation();
        File path = null;
        try {
            path = new File(url.toURI());
        } catch (URISyntaxException e) {
            path = new File(url.getPath());
        }
        String neu = SeleniumTest.class.getResource("log4j2-test.xml").toString();
        System.setProperty("log4j.configurationFile", neu);
    }

    /**
     * Inititalisierung vor Aufruf der Testmethode.
     */
    @Before
    public void setUp() {
        log4j.entry();
        driverInstance = new UITestDriver(getClass());
        log4j.exit();
    }


    @Rule
    public SeleniumTest.ScreenshotTestRule screenshotTestRule = new SeleniumTest.ScreenshotTestRule();

    private class ScreenshotTestRule implements TestRule {

        public Statement apply(final Statement statement, final Description description) {
            log4j.entry(new Object[] { statement, description });
            return new Statement() {
                String testClassNameWithPackage = description.getTestClass().toString();
                String testClassName =
                    testClassNameWithPackage.substring(testClassNameWithPackage.lastIndexOf(".") + 1);

                @Override
                public void evaluate() throws Throwable {
                    log4j.entry();
                    statement.evaluate();
                    try {
                    } catch (Throwable t) {
                        log4j.catching(Level.ERROR, t);
                        captureScreenshot(false);
                        throw t;
                        //                    } finally {
                        //                        after();
                    }
                    log4j.exit();
                }

                public void captureScreenshot(final boolean suceed) {
                    log4j.entry(suceed);
                    String fileName = testClassName + "_" + description.getMethodName();

                    if (suceed) {
                        fileName += "_OK";
                    } else {
                        fileName += "_F";
                    }
                    log4j.trace("Screenshot FileName= {}", fileName);
                    if (driverInstance != null) {
                        driverInstance.captureScreenshot(fileName, suceed);
                    }
                    log4j.exit();
                }
            };
        }
    }


    /**
     * Aufräumaktionen nach Aufruf der Testmethode.
     */
    @After
    public void tearDown() {
        log4j.entry();
        tearDownDriverInstance();
        log4j.exit();
    }

    public void tearDownDriverInstance() {
        log4j.entry();
        //Herunterfahren des Browsers, nach jedem Test
        if (driverInstance != null) {
            log4j.debug("Tear Down driverInstance");
            driverInstance.tearDown();
        }
        log4j.exit();
    }

    /**
     * Aufräumaktionen nach Aufruf der Testmethode.
     */
    @AfterClass
    public static void tearDownAfterClass() {
    }

}
