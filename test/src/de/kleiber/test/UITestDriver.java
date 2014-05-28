package de.kleiber.test;


import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.junit.Assert.fail;

import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;


/**
 * Generischer Treiber für UI Tests mit Selenium.
 * Ermöglich sowohl den lokalen Test auf dem Entwickler-PC als auch den Test in Selenium Grid.
 * Weiterhin werden auch verschiedene Browser unterstützt.
 */
public class UITestDriver {
    /** WebDriver-Instanz. */
    private static WebDriver driver;

    /** Start-URL der Anwendung im Browser. */
    private static String baseUrl;

    /** Browser, auf dem der Test erfolgen soll. */
    private static String browserName;

    /** Betriebssytem Plattform, auf dem der Test erfolgen soll. */
    private static Platform platform;

    /** Browser Firefox. */
    public static final String FIREFOX_DRIVER = "firefox";

    /** Browser Internet Explorer. */
    public static final String IE_DRIVER = "internet explorer";

    /** Pfad zur Dateiablage. */
    private static String basePath;

    /** Zu testende Klasse. */
    private static String testClass;

    /** TIMESTAMP-Format. */
    private static final String DATE_FORMAT = "yyMMdd_HHmmss";

    /** URL des Selenium Grid Hub Server. */
    private static final String SELENIUM_GRID = "http://localhost:4444/wd/hub";

    /** Standard-Timeout, nach dem Selenium-Aktionen abgebrochen werden. */
    public static final int STD_DRIVER_TIMEOUT = 30;

    /** Standard-Timeout, nach dem AJAX-Aktionen abgebrochen werden. */
    private static int timeoutAjax = STD_DRIVER_TIMEOUT;

    /** Standard-Wartezeit, nach dem bei AJAX-Aktionen das Element erneut untersucht wird. */
    private static final int STD_AJAX_LOOP_WAIT = 1000;

    /** Testtreiber der Umgebung.*/
    public static final String TEST_DRIVER = System.getenv("TEST_DRIVER");

    /** Testtreiberversion der Umgebung. */
    public static final String TEST_DRIVER_VERSION = System.getenv("TEST_DRIVER_VERSION");

    /** Testtreiberplattform der Umgebung. */
    public static final String TEST_DRIVER_PLATFORM = System.getenv("TEST_DRIVER_PLATFORM");

    /** Basisurl des Testsystem der Umgebung. */
    public static final String TEST_BASIS_URL_ENV = System.getenv("TEST_BASIS_URL");

    /** Basisurl des Testsystem des JDevelopers. */
    public static final String TEST_BASIS_URL_LOCAL = ("http://localhost:7101/");

    /** Erfolgt der Test gegen Selenium Grid? */
    public static final boolean TEST_GRID = (TEST_BASIS_URL_ENV != null);

    /** Basisurl des Testsystem. */
    public static final String TEST_BASIS_URL = TEST_GRID ? TEST_BASIS_URL_ENV : TEST_BASIS_URL_LOCAL;

    /** Logger. */
    private static Logger log4j = LogManager.getLogger(UITestDriver.class.getName());

    /** Avoid mozilla dialog to warn the user when visiting URLs that use the username and password syntax. */
    private static final int DISABLE_MOZILLA_WARNING = 255;

    /**
     * UITestDriver des DEFAULT-Types.
     * @param testClass Zu testende Klasse.
     */
    public UITestDriver(final Class testClass) {
        log4j.entry();
        log4j.debug("Classpath= {}", System.getProperty("java.class.path"));
        log4j.debug("testClass=" + testClass);
        setUp(testClass);
        log4j.exit();
    }

    /**
     * Aufsetzen des Drivers, falls noch keiner existiert.
     * @param testClass Zu testende Klasse.
     */
    private void setUp(final Class testClass) {

        log4j.entry();
        log4j.info("testClass=" + testClass);
        setBasePath(getResultDirectory(testClass));
        setTestClass(testClass);
        setBrowserName(TEST_DRIVER != null ? TEST_DRIVER : FIREFOX_DRIVER);
        setPlatform(TEST_DRIVER_PLATFORM);
        setBaseUrl(TEST_BASIS_URL);
        if (driver == null) {
            log4j.debug("driver is null");
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setBrowserName(browserName);
            log4j.debug("BrowserName= {} ", browserName);
            capabilities.setPlatform(platform);
            log4j.debug("Platform= {}", platform);

            if (FIREFOX_DRIVER.equals(browserName)) {
                log4j.debug("Setting up FirefoxDriver Profile");
                FirefoxProfile profile = new FirefoxProfile();
                profile.setPreference("network.http.phishy-userpass-length", DISABLE_MOZILLA_WARNING);
                capabilities.setCapability(FirefoxDriver.PROFILE, profile);
            } else if (IE_DRIVER.equals(browserName)) {
                log4j.debug("Setting up IEDriver Profile");
                capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,
                                           true);
                if (!TEST_GRID) {
                    log4j.debug("Test runs NOT against GRID");
                    /*
                    File path =
                        new File(InternetExplorerDriver.class.getResource("InternetExplorerDriver.class").getPath());
*/
                    final URL url = InternetExplorerDriver.class.getProtectionDomain().getCodeSource().getLocation();
                    File fPath = null;
                    try {
                        fPath = new File(url.toURI());
                    } catch (URISyntaxException e) {
                        fPath = new File(url.getPath());
                    }
                    String sPath = fPath.toString();
                    log4j.debug("Path to InternetExplorerDriver.class is: " + sPath);
                    String locationIeDriver = sPath.substring(0, sPath.lastIndexOf("\\") + 1) + "IEDriverServer.exe";
                    System.setProperty("webdriver.ie.driver", locationIeDriver);
                    log4j.debug("Setting webdriver.ie.driver to {}", locationIeDriver);
                }
            }
            if (TEST_GRID) {
                log4j.debug("Test runs against GRID");
                if (TEST_DRIVER_VERSION != null) {
                    capabilities.setVersion(TEST_DRIVER_VERSION);
                }
                log4j.debug("DriverVersion= {}", capabilities.getVersion());
                try {
                    driver = new RemoteWebDriver(new URL(SELENIUM_GRID), capabilities);
                } catch (MalformedURLException e) {
                    fail(e.getMessage());
                }

            } else {
                if (FIREFOX_DRIVER.equals(browserName)) {
                    driver = new FirefoxDriver(capabilities);
                } else if (IE_DRIVER.equals(browserName)) {
                    driver = new InternetExplorerDriver(capabilities);
                }
            }
            driver.manage().timeouts().implicitlyWait(STD_DRIVER_TIMEOUT, TimeUnit.SECONDS);
            log4j.debug("ImplicityWait= {}", STD_DRIVER_TIMEOUT);
            driver.manage().timeouts().pageLoadTimeout(STD_DRIVER_TIMEOUT, TimeUnit.SECONDS);
            log4j.debug("pageLoadTimeout= {}", STD_DRIVER_TIMEOUT);
            driver.manage().timeouts().setScriptTimeout(STD_DRIVER_TIMEOUT, TimeUnit.SECONDS);
            log4j.debug("ScriptTimeout= {}", STD_DRIVER_TIMEOUT);

            if (driver == null) {
                final String errorMessage = "Driver is still null";
                log4j.error(errorMessage);
                fail(errorMessage);
            }
        }

        log4j.debug("Maximize Browser Window");
        driver.manage().window().maximize();
        log4j.exit();
    }


    /**
     * Ermittlung des Verzeichnisses zur Ablage der Testergebnisse und Screenshots.
     * @param testClass Zu testende Klasse.
     * @return Verzeichnis zur Ablage der Testergebnisse und Screenshots
     * @throws URISyntaxException
     */
    private String getResultDirectory(final Class testClass) {
        log4j.entry(testClass);
        URL url = testClass.getProtectionDomain().getCodeSource().getLocation();
        File path = null;
        try {
            path = new File(url.toURI());
        } catch (URISyntaxException e) {
            path = new File(url.getPath());
        }
        log4j.debug("Path= {}", path);
        String neu =
            path.toString().substring(0, path.toString().lastIndexOf("\\") + 1) + "results" + File.separator +
            "selenium";
        return log4j.exit(neu);


    }
    /*
    public static void setBaseUrl(String baseUrl) {
        UITestDriver.baseUrl = baseUrl;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }
*/

    public static void setBaseUrl(final String baseUrl) {
        UITestDriver.baseUrl = baseUrl;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    /** Herunterfahren des Drivers. */
    public void tearDown() {
        log4j.entry();
/*        
        //Auf leere Seite navigieren um mögliches Alert zu erzwingen
        log4j.debug("Navigate to about:blank");
        driver.get("about:blank");
        try {
            log4j.debug("Switch to alert");
            Alert alert = driver.switchTo().alert();
            if (alert != null) {
                log4j.debug("acceptAlert");
                //Sollte das Alert kommen auf ok klicken
                alert.accept();
            }
        } catch (NoAlertPresentException e) {
            log4j.catching(Level.DEBUG, e);
            //Kein Alert  vorhanden, dann normal weitermachen
        }
*/
        log4j.debug("Quit Driver");
        driver.quit();
        driver = null;

        log4j.exit();
    }

    /**
     * Warte darauf, dass das übergebene Element hinzugefügt wird.
     * @param by Element im Dokument
     */
    public void waitForElementPresent(final By by) {
        log4j.entry(by);
        for (int second = 0; !isElementPresent(by); second++) {
            log4j.debug("Second= {}", second);
            if (second >= timeoutAjax) {
                fail("timeout");
            }
            sleep(STD_AJAX_LOOP_WAIT);
        }
        log4j.exit();
    }

    /**
     * Warte darauf, dass das übergebene Element entfernt wird.
     * @param by Element im Dokument
     */
    public void waitForElementNotPresent(final By by) {
        log4j.entry(by);
        for (int second = 0; isElementPresent(by); second++) {
            if (second >= timeoutAjax) {
                fail("timeout");
            }
            sleep(STD_AJAX_LOOP_WAIT);
        }
        log4j.exit();
    }

    /**
     * Warte darauf, dass das übergebene Element angezeigt wird.
     * @param by Element im Dokument
     */
    public void waitForVisible(final By by) {
        log4j.entry(by);
        WebElement element = findElement(by);
        waitForVisible(element);
        log4j.exit();
    }

    /**
     * Warte darauf, dass das übergebene Element angezeigt wird.
     * @param element Web-Element im Dokument
     */
    private void waitForVisible(final WebElement element) {
        log4j.entry(element);
        for (int second = 0; !element.isDisplayed(); second++) {
            if (second >= timeoutAjax) {
                fail("timeout");
            }
            /*
            if (element.isDisplayed()) {
                break;
            }
*/
            sleep(STD_AJAX_LOOP_WAIT);
        }
        log4j.exit();
    }

    /**
     * Warte darauf, dass das übergebene Element verborgen wird.
     * @param by Element im Dokument
     */
    public void waitForNotVisible(final By by) {
        log4j.entry(by);
        for (int second = 0; findElement(by).isDisplayed(); second++) {
            if (second >= timeoutAjax) {
                fail("timeout");
            }
            sleep(STD_AJAX_LOOP_WAIT);
        }
        log4j.exit();
    }

    /**
     * Ermitteln eines Attributs value des übergebenen Element ohne Abbruch bei diversen Exceptions.
     * Diese Routine ist nur für die Waitroutinen oder andere Loops gedacht, die wiederholt lesen.
     * @param by Element im Dokument
     * @param type Attributtyp
     */
    private String getAttribute(final By by, final String type) {
        log4j.entry(new Object[] { by, type });
        String sRet = "";
        try {
            sRet = findElement(by).getAttribute(type);
        } catch (StaleElementReferenceException e) {
            log4j.debug(e.getMessage());
            sRet = e.getMessage();
        } catch (InvalidSelectorException e) {
            log4j.debug(e.getMessage());
            sRet = e.getMessage();
        }
        return log4j.exit(sRet);
    }

    /**
     * Warte darauf, dass das Attribut value des übergebene Element den übergebenen Wert enthält.
     * @param by Element im Dokument
     * @param value erwarteter Wert
     */
    public void waitForValue(final By by, final String value) {
        log4j.entry(by);
        for (int second = 0; !value.equals(getAttribute(by, "value")); second++) {
            if (second >= timeoutAjax) {
                fail("timeout");
            }
            sleep(STD_AJAX_LOOP_WAIT);
        }
        log4j.exit();
    }

    /**
     * Warte darauf, dass das Attribut alt des übergebene Element den übergebenen Wert enthält.
     * @param by Element im Dokument
     * @param value erwarteter Wert
     */
    public void waitForAlt(final By by, final String value) {
        log4j.entry(by);
        for (int second = 0; !value.equals(getAttribute(by, "alt")); second++) {
            if (second >= timeoutAjax) {
                fail("timeout");
            }
            sleep(STD_AJAX_LOOP_WAIT);
        }
        log4j.exit();
    }

    /**
     * Warten.
     * @param miliseconds Anzahl der Millisekunden, die gewarten werden soll
     */
    public void sleep(final int miliseconds) {
        try {
            Thread.sleep(miliseconds);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Prüft ob das übergebene Element existiert/gefunden werden kann.
     * @param by Element im Dokument
     * @return true/false
     */
    public boolean isElementPresent(final By by) {
        log4j.entry(by);
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            log4j.catching(Level.DEBUG, e);
            return false;
        }
    }

    /**
     * Findet das erste WebElement über die übergebene Methode.
     * @param by Element im Dokument
     * @return Web-Element im Dokument
     */
    public WebElement findElement(final By by) {
        log4j.entry(by);
        try {
            return log4j.exit(driver.findElement(by));
        } catch (NoSuchElementException e) {
            log4j.catching(Level.WARN, e);
            captureScreenshot(getTestClass() + "_Element_not_found");
            throw e;
        }
    }

    /**
     * Get the source of the last loaded page.
     * If the page has been modified after loading (for example, by Javascript)
     * there is no guarentee that the returned text is that of the modified page.
     * Please consult the documentation of the particular driver being used to
     * determine whether the returned text reflects the current state of the
     * page or the text last sent by the web server.
     * The page source returned is a representation of the underlying DOM:
     * do not expect it to be formatted or escaped in the same way as the
     * response sent from the web server.
     * @return The source of the current page
     */
    public String getPageSource() {
        return log4j.exit(driver.getPageSource());
    }

    /**
     * Applikationsspezifische URL.
     * @param url Applikationsspezifische URL ohne vorangestelltes "\"
     */
    public void getRelativeUrl(final String url) {
        log4j.entry(url);
        driver.get(getBaseUrl() + url);
        log4j.exit();
    }

    /**
     * Findet das erste Vorkommen eines Strings in einer Tabelle.
     * Es wird dabei nur die jeweils übergebene Spalte durchsucht.
     *
     * @param table ID der Tabelle
     * @param column ID der Spalte
     * @param value zu findender String-Wert
     * @return Treffer: Zeile beginnend bei 0; Kein Treffer: -1
     */
    public Integer findElementInTable(final String table, final String column, final String value) {
        log4j.entry(new Object[] { table, column, value });
        Integer row = 0;
        while (this.isElementPresent(By.id(table + row + column))) {
            if (value.equals(this.getRowContent(table, row, column))) {
                return log4j.exit(row);
            }
            row++;
        }
        return log4j.exit(-1);
    }

    /**
     * Gibt den Inhalt einer Zelle zurück.
     * @param table ID der Tabelle
     * @param row Zeilennummer
     * @param column ID der Spalte
     * @return Inhalt der Zelle
     */
    public String getRowContent(final String table, final Integer row, final String column) {
        log4j.entry(new Object[] { table, row, column });
        return log4j.exit(this.findElement(By.id(table + row + column)).getText());
    }

    /**
     * Linksklick auf das erste Vorkommen eines Strings in der angegebenen Tabelle und Spalte.
     * @param table ID der Tabelle
     * @param column ID der Spalte
     * @param value zu findender String-Wert
     */
    public void clickTableItem(final String table, final String column, final String value) {
        log4j.entry(new Object[] { table, column, value });
        Integer row = 0;
        while (this.isElementPresent(By.id(table + row + column))) {
            log4j.trace("Row= {}" + row);
            if (value.equals(getRowContent(table, row, column))) {
                this.clickTableItem(table, row, column);
                break;
            }
            row++;
        }
        log4j.exit();
    }

    /**
     * Linksklick auf die mitgegebene Zelle.
     * @param table ID der Tabelle
     * @param row ID der Zeile
     * @param column ID der Spalte
     */
    public void clickTableItem(final String table, final Integer row, final String column) {
        log4j.entry(new Object[] { table, row, column });
        this.findElement(By.id(table + row + column)).click();
        log4j.exit();
    }

    /**
     * Erstellt einen Screenshot im Verzeichnis basePath.
     * @param fileName Dateiname des Screenshots
     */
    public void captureScreenshot(final String fileName) {
        log4j.entry(fileName);
        final String pathName = getBasePath() + File.separator;
        captureScreenshot(fileName, pathName);
        log4j.exit();
    }

    /**
     * Erstellt einen Screenshot im Verzeichnis basePath\<PREFIX>\.
     * @param fileName Dateiname des Screenshots
     * @param suceed Bestimmt das <PREFIX> TRUE => OK, FALSE => F
     */
    public void captureScreenshot(final String fileName, final boolean suceed) {
        log4j.entry(new Object[] { fileName, suceed });
        final String pathName;
        if (suceed) {
            pathName = getBasePath() + File.separator + "OK" + File.separator;
        } else {
            pathName = getBasePath() + File.separator + "F" + File.separator;
        }
        captureScreenshot(fileName, pathName);
        log4j.exit();
    }

    /**
     * Erstellt einen Screenshot in einem beliebigen Pfad mit beliebigen Namen.
     * @param fileName Dateiname
     * @param pathName Verzeichnis
     */
    public void captureScreenshot(final String fileName, final String pathName) {
        log4j.entry(new Object[] { fileName, pathName });
        final File dir = new File(pathName);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                fail("Screenshot Verzeichnis wurde nicht angelegt.");
            }
        }
        String file = pathName + fileName + ".png";
        WebDriver screenshotDriver = driver;
        if (screenshotDriver.getClass().getCanonicalName().equals("org.openqa.selenium.remote.RemoteWebDriver")) {
            screenshotDriver = new Augmenter().augment(driver);
        }

        File screenshotFile = ((TakesScreenshot) screenshotDriver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(screenshotFile, new File(file));
        } catch (IOException e) {
            log4j.error(e.getMessage());
            fail(e.getMessage());
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    public static void setBrowserName(final String browserName) {
        UITestDriver.browserName = browserName;
    }

    public static String getBrowserName() {
        return browserName;
    }

    public static void setPlatform(final Platform platform) {
        UITestDriver.platform = platform;
    }

    /**
     * Setzen der Betriebssystem-Platform über eine Umgebungsvariable.
     * @param platform Umgebungsvariable
     */
    public static void setPlatform(final String platform) {
        if (platform == null || platform.equals("WINDOWS")) {
            setPlatform(Platform.WINDOWS);
        } else if (platform.equals("ANDROID")) {
            setPlatform(Platform.ANDROID);
        } else if (platform.equals("ANY")) {
            setPlatform(Platform.ANY);
        } else if (platform.equals("LINUX")) {
            setPlatform(Platform.LINUX);
        } else if (platform.equals("MAC")) {
            setPlatform(Platform.MAC);
        } else if (platform.equals("UNIX")) {
            setPlatform(Platform.UNIX);
        } else if (platform.equals("VISTA")) {
            setPlatform(Platform.VISTA);
        } else if (platform.equals("WIN8")) {
            setPlatform(Platform.WIN8);
        } else if (platform.equals("XP")) {
            setPlatform(Platform.XP);
        }
    }

    public static Platform getPlatform() {
        return platform;
    }

    public String getTextPresent() {
        return driver.getPageSource();
    }

    /**
     * Überprüft, ob ein übergebener Wert sichtbar ist.
     * @param value übergebener Wert
     * @return true/false
     */
    public boolean isTextPresent(final String value) {
        //return driver.findElement(By.tagName("body")).getText().contains(str);
        return this.isElementPresent(By.xpath("//*[contains(.,'" + value + "')]"));
    }


    public static void setBasePath(final String basePath) {
        UITestDriver.basePath = basePath;
    }

    public static String getBasePath() {
        return UITestDriver.basePath;
    }

    /**
     * Setzen der Testklasse aus der zu testenden Klasse.
     * @param testClass zu testende Klasse
     */
    public static void setTestClass(final Class testClass) {
        log4j.entry(testClass);
        String className = testClass.getName().substring(testClass.getName().lastIndexOf(".") + 1);
        log4j.debug("className=" + className);
        setTestClass(className);
        log4j.exit();
    }

    public static void setTestClass(final String testClass) {
        UITestDriver.testClass = testClass;
    }

    public static String getTestClass() {
        return log4j.exit(testClass);
    }

    /**
     * Variiert den Driver-Timeout für den aktuellen Run.
     * Standart kann über resetDriverTimeout() wieder hergestellt werden.
     * @param timeout Timeout in Sekunden
     */
    public void setDriverTimeout(final Integer timeout) {
        log4j.entry(timeout);
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
        log4j.debug("ImplicityWait set to {} Seconds", timeout);
        driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);
        log4j.debug("pageLoadTimeout set to {} Seconds", timeout);
        driver.manage().timeouts().setScriptTimeout(timeout, TimeUnit.SECONDS);
        log4j.debug("ScriptTimeout set to {} Seconds", timeout);
        setTimeoutAjax(timeout);
        log4j.debug("timeoutAjax set to {} Seconds", timeout);
        log4j.exit();
    }

    /**
     * Setzt den Driver-Timeout auf den Standard-Wert zurück.
     * Der Standard-Wert ist in STD_DRIVER_TIMEOUT definiert.
     */
    public void resetDriverTimeout() {
        log4j.entry();
        driver.manage().timeouts().implicitlyWait(STD_DRIVER_TIMEOUT, TimeUnit.SECONDS);
        log4j.debug("ImplicityWait reset to {} Seconds", STD_DRIVER_TIMEOUT);
        driver.manage().timeouts().pageLoadTimeout(STD_DRIVER_TIMEOUT, TimeUnit.SECONDS);
        log4j.debug("pageLoadTimeout reset to {} Seconds", STD_DRIVER_TIMEOUT);
        driver.manage().timeouts().setScriptTimeout(STD_DRIVER_TIMEOUT, TimeUnit.SECONDS);
        log4j.debug("ScriptTimeout reset to {} Seconds", STD_DRIVER_TIMEOUT);
        setTimeoutAjax(STD_DRIVER_TIMEOUT);
        log4j.debug("timeoutAjax reset to {} Seconds", STD_DRIVER_TIMEOUT);
        log4j.exit();
    }

    /**
     * Linksklick in ein Element anhand der ID.
     * @param itemId Element ID
     */
    public void clickById(final String itemId) {
        log4j.entry(itemId);
        By element = By.id(itemId);
        clickBy(element);
        log4j.exit();
    }

    /**
     * Ermittelt den Text eines Elements.
     * @param by Element
     * @return Text des Elements
     */
    private String getTextBy(final By by) {
        log4j.entry(new Object[] { by });
        final WebElement webElement = findElement(by);
        waitForVisible(webElement);
        return log4j.exit(webElement.getText());
    }

    /**
     * Ermittelt den Text eines Elements.
     * @param itemId Element ID
     * @return Text des Elements
     */
    public String getTextById(final String itemId) {
        log4j.entry(itemId);
        final By element = By.id(itemId);
        return log4j.exit(getTextBy(element));
    }

    /**
     * Ermittelt den Wert eines Elements.
     * @param by Element
     * @return Wert des Elements
     */
    private String getAttributValueBy(final By by) {
        log4j.entry(new Object[] { by });
        final WebElement webElement = findElement(by);
        waitForVisible(webElement);
        return log4j.exit(webElement.getAttribute("value"));
    }

    /**
     * Ermittelt den Wert eines Elements.
     * @param itemId Element ID
     * @return Wert des Elements
     */
    public String getAttributValueById(final String itemId) {
        log4j.entry(itemId);
        final By by = By.id(itemId);
        return log4j.exit(getAttributValueBy(by));
    }

    /**
     * Linksklick in ein Element.
     * @param by Element
     */
    private void clickBy(final By by) {
        log4j.entry(new Object[] { by });
        final WebElement webElement = findElement(by);
        waitForVisible(webElement);
        webElement.click();
        log4j.exit();
    }

    /**
     * Linksklick in ein Element anhand der CSS-Klasse.
     * @param css CSS Klasse des Elements
     */
    public void clickByCss(final String css) {
        log4j.entry(css);
        By element = By.cssSelector(css);
        clickBy(element);
        log4j.exit();
    }

    /**
     * Linksklick in ein Element anhand des XPATH.
     * @param xpath Element XPATH
     */
    public void clickByXpath(final String xpath) {
        log4j.entry(xpath);
        By element = By.xpath(xpath);
        clickBy(element);
        log4j.exit();
    }

    /**
     * Ermittelt den Wert eines Elements.
     * @param xpath Element XPATH
     * @return Wert des Elements
     */
    public String getTextByXpath(final String xpath) {
        log4j.entry(xpath);
        final By element = By.xpath(xpath);
        return log4j.exit(getTextBy(element));
    }

    /**
     * Setzt den Wert eines Elements.
     * @param by Element
     * @param value Wert des Elements
     */
    private void sendKeysBy(final By by, final String value) {
        log4j.entry(new Object[] { by });
        WebElement webElement = findElement(by);
        waitForVisible(webElement);
        webElement.sendKeys(value);
        log4j.exit();
    }

    /**
     * Setzt den Wert eines Elements anhand der ID.
     * @param itemId Element ID
     * @param value Wert des Elements
     */
    public void sendKeysById(final String itemId, final String value) {
        log4j.entry(new Object[] { itemId, value });
        By element = By.id(itemId);
        sendKeysBy(element, value);
        log4j.exit();
    }

    /**
     * Setzt den Wert eines Elements anhand des XPATH.
     * @param xpath Element XPATH
     * @param value Wert des Elements
     */
    public void sendKeysByXpath(final String xpath, final String value) {
        log4j.entry(new Object[] { xpath, value });
        By element = By.xpath(xpath);
        sendKeysBy(element, value);
        log4j.exit();
    }

    /**
     * Ausführen einer Tastenkombination auf einem Element anhand der ID.
     * @param itemId Element ID
     * @param keys Tastenkombination
     */
    public void sendKeysById(final String itemId, final Keys keys) {
        log4j.entry(new Object[] { itemId, keys });
        By element = By.id(itemId);
        WebElement webElement = driver.findElement(element);
        waitForVisible(webElement);
        Actions actions = new Actions(driver);
        actions.sendKeys(webElement, keys);
        Action action = actions.build();
        action.perform();
    }

    /**
     * Leeren und setzen des Werts eines Elements.
     * @param by Element
     * @param value Wert des Elements
     */
    private void clearAndSendKeysBy(final By by, final String value) {
        log4j.entry(new Object[] { by });
        WebElement webElement = findElement(by);
        waitForVisible(webElement);
        webElement.clear();
        webElement.sendKeys(value);
        log4j.exit();
    }

    /**
     * Leeren und setzen des Werts eines Elements.
     * @param itemId Element ID
     * @param value Wert des Elements
     */
    public void clearAndSendKeysById(final String itemId, final String value) {
        log4j.entry(new Object[] { itemId, value });
        By element = By.id(itemId);
        clearAndSendKeysBy(element, value);
        log4j.exit();
    }

    /**
     * Auswählen eines Wertes aus einer Lov eines Elements.
     * @param itemId Element ID
     * @param value Wert des Elements
     */
    public void selectValueFromLovById(final String itemId, final String value) {

        log4j.entry(new Object[] { itemId, value });
        final WebElement element = findElement(By.id(itemId));
        waitForVisible(element);
        new Select(element).selectByVisibleText(value);
        log4j.exit();
    }

    /**
     * Liefert das aktuelle Datum im übergebenen (Simple)DateFormat.
     * @param dateFormat (Simple)DateFormat
     * @return aktuelles Datum
     */
    public String getCurrentDate(final DateFormat dateFormat) {
        log4j.entry(dateFormat);
        Calendar now = Calendar.getInstance();
        return log4j.exit(dateFormat.format(now.getTime()));
    }

    /**
     * Liefert das aktuelle Datum im Format "dd.MM.yyyy".
     * @return aktuelles Datum
     */
    public String getCurrentDate() {
        return log4j.exit(getCurrentDate(new SimpleDateFormat("dd.MM.yyyy")));
    }

    /**
     * Liefert das Datum und Zeitstempel in Relation zum übergebenen Kalenderfeld.
     * @param field Kalenderfeld, s. Klasse Calender
     * @param amount Menge an Datum oder Zeit, die zum Kalenderfeld hinzugefüft oder abgezogen werden soll
     * @return Datum
     */
    public String getDate(final int field, final int amount) {
        log4j.entry(new Object[] { field, amount });
        Calendar now = Calendar.getInstance();
        now.add(amount, field);
        return log4j.exit(new SimpleDateFormat("dd.MM.yyyy").format(now.getTime()));
    }

    /**
     * Findet eine Lov anhand der ID.
     * @param id Lov ID
     * @return Select-Element im Dokument
     */
    public Select findLovById(final String id) {
        return new Select(findElement(By.id(id)));
    }

    /**
     * Ermittelt den Wert einer selektierten Option einer Lov eines Elements.
     * @param by Element
     * @return Wert
     */
    public String getSelectedItemInLovBy(final By by) {
        final WebElement element = findElement(by);
        waitForVisible(element);
        return new Select(element).getFirstSelectedOption().getText();
    }

    /**
     * Ermittelt den Wert einer selektierten Option einer Lov eines Elements anhand der ID.
     * @param id Element ID
     * @return Wert
     */
    public String getSelectedItemInLovById(final String id) {
        final By by = By.id(id);
        return getSelectedItemInLovBy(by);
    }

    public static void setTimeoutAjax(final int timeoutAjax) {
        UITestDriver.timeoutAjax = timeoutAjax;
    }

}
