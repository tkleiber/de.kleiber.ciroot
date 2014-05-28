package de.kleiber.test;

import oracle.adf.share.security.authentication.JAASAuthenticationService;

import oracle.jbo.ApplicationModule;
import static oracle.jbo.client.Configuration.createRootApplicationModule;
import static oracle.jbo.client.Configuration.releaseRootApplicationModule;

import org.junit.After;
import org.junit.Before;


/**
 * Abstrakte Oberklasse für AM-Tests. Diese Klasse ist verantwortlich
 * dafür, dass vor jedem Test eine Instanz des {@link ApplicationModule}
 * bereitgestellt und nach Ausführung des Tests die damit verknüpften Ressourcen
 * wieder freigegeben werden.
 */
public abstract class ServiceSetup {
    private static final String USERNAME = "s902044";
    private static final String PASSWORD = "ikb3ikb3";

    /**
     * Feld für den Zugriff auf das {@link ApplicationModule}, welches zu Test-
     * zwecken erzeugt und initialisiert wurde.
     */
    protected ApplicationModule applicationModule;

    /**
     * Überschreiben, um den vollqualifizierten Namen des zu instanziierenden
     * {@link ApplicationModule} anzugeben.
     *
     * @return vollqualifizierter Name des {@link ApplicationModule}
     */
    protected abstract String applicationModuleName();

    /**
     * Überschreiben, um den vollqualifizierten Namen der zu verwendenden
     * Konfiguration anzugeben.
     *
     * @return Konfiguration des {@link ApplicationModule}
     */
    protected abstract String applicationModuleConfiguration();

    @Before
    public void setupModule() {
        authenticateApplicationUser();
        createApplicationModule();
    }

    private void createApplicationModule() {
        applicationModule = createRootApplicationModule(applicationModuleName(), applicationModuleConfiguration());
    }

    private void authenticateApplicationUser() {
        JAASAuthenticationService jaas = new JAASAuthenticationService();
        jaas.login(USERNAME, PASSWORD);
    }

    @After
    public void cleanupModule() {
        releaseRootApplicationModule(applicationModule, true);
    }
}
