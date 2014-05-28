package de.kleiber.test;

import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Abstrakte Klasse zur Implementierung generischer Tests von ResourceBundles.
 * <p>
 * Es werden nur ResourceBundles des Typs "Properties Bundle" unterstützt.
 * <p>
 * Anwendung:
 * <p>
 * -1- Die ableitende Klasse muss <code>initBundleNames()</code> implementieren und dort die
 * Liste bundleNames mit den Namen der zu testenden ResourceBundles erweitern.
 * <p>
 * -2- Der Konstruktor von <code>AbstractBundleTest</code> muss aufgerufen
 * werden, da er <code>initBundleNames()</code> aufruft.
 * <p>
 * -3- Testmethoden müssen in der ableitenden Klasse mit <code>@Test</code>
 * getagged werden. Diese können <code>checkResourceBundles()</code> der Oberklasse aufrufen.
 * <p>
 * Codebeispiel:
 * <p><code>
 * import java.io.IOException;
 * import org.junit.Test;
 *
 * public class BundleTest extends AbstractBundleTest {
 *
 * public BundleTest() {
 *     super();
 * }
 *
 * @Test
 * public void checkResourceBundles() throws IOException {
 *     super.checkResourceBundles();
 * }
 *
 * protected void initBundleNames() {
 *     bundleNames.add("de.ikb.adf.kreda.per.bankverb.model.bundle.modelBundle");
 * }
 * }
 *
 * </code>
 */
public abstract class AbstractBundleTest {

    /**
     * enthält die Namen der Bundles
     */
    protected List<String> bundleNames = new ArrayList<String>();

    /**
     * Konstruktor; initialisiert <code>bundleNames</code>
     */
    protected AbstractBundleTest() {
        initBundleNames();
    }
    
    protected abstract void initBundleNames();

    /**
     * Prüft, ob die Keys der Properties Files der ResourceBundles in allen 
     * anderen Properties Files übersetzt wurden. Es werden dann Fehler 
     * ausgewiesen, wenn ein Key in einer Properties Datei fehlt oder kein Wert 
     * hinterlegt ist.
     *
     * @throws IOException
     * 
     **/
    public void checkResourceBundles() throws IOException {
        int countDifferences = 0;
        // alle hinterlegten ResourceBundle Names durchgehen
        for (String resourceBundleName : bundleNames) {
            System.out.println("resourceBundleName:" + resourceBundleName);

            Map<String, Properties> bundleProperties =
                new HashMap<String, Properties>();
            // Liste der Properties Files beschaffen
            String resourceRegEx = resourceBundleName + ".*\\.properties";
            ResourceList propFileList = new ResourceList(resourceRegEx);
            Enumeration en = propFileList.getResources();
            // Properties Objekte erstellen
            while (en.hasMoreElements()) {
                URL resourceURL = (URL)en.nextElement();
                Properties prop = new Properties();
                prop.load(resourceURL.openStream());
                bundleProperties.put(resourceURL.toString(), prop);
                System.out.println("ResourceURL: " + resourceURL);
            }
            // abbrechen, wenn es zum Bundle keine Properties Files gibt
            if (bundleProperties.size() == 0) {
                fail("Keine Properties Files zu Resource Bundle gefunden");
            }

            // Alle Bundle Properties Files mit den jeweils anderen vergleichen
            for (String propFile : bundleProperties.keySet()) {

                Properties properties = bundleProperties.get(propFile);
                for (Object key : properties.keySet()) {
                    //                           System.out.println("Key: " + key);
                    // key auf leeren Inhalt pruefen
                    Object value = properties.get(key);
                    if (null == value || ((String)value).length() == 0) {
                        countDifferences++;
                        System.out.println(propFile + ": Key >>" + key +
                                           "<< ist leer");
                    }
                    // key mit keys anderer Properties Files vergleichen
                    for (String otherPropFile : bundleProperties.keySet()) {
                        // Properties File nicht mit sich selbst vergleichen
                        if (propFile != otherPropFile) {
                            // alle keys durchgehen
                            Properties otherProperties =
                                bundleProperties.get(otherPropFile);
                            if (!otherProperties.containsKey(key)) {
                                countDifferences++;
                                System.out.println(propFile + ": Key >>" +
                                                   key +
                                                   "<< fehlt in Bundle File " +
                                                   otherPropFile);
                            }
                        }
                    }
                }
            }
        }

        assertEquals("Anzahl Unterschiede", 0, countDifferences);
    }
}