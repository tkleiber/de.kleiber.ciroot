package de.kleiber.test;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class ClientSynchedWithServer implements ExpectedCondition<Boolean> {
    // from http://www.redheap.com/2015/02/selenium-adf-partial-page-rendering.html
    // return false if AdfPage object and functions do not exist
    // if they do exist return true if page is fully loaded and ready or reason why this is not completed yet
    String js =
        "return typeof AdfPage !== 'undefined' && " + 
        "typeof AdfPage.PAGE !== 'undefined' && " +
        "typeof AdfPage.PAGE.isSynchronizedWithServer === 'function' && " +
        "(AdfPage.PAGE.isSynchronizedWithServer() || " +
        "(typeof AdfPage.PAGE.whyIsNotSynchronizedWithServer === 'function' && " +
        "AdfPage.PAGE.whyIsNotSynchronizedWithServer()))";

    @Override
    public Boolean apply(WebDriver driver) {
        Boolean ret = false;
        JavascriptExecutor jsDriver = (JavascriptExecutor)driver;

        try {
           ret = Boolean.TRUE.equals(jsDriver.executeScript(js));
        } catch (WebDriverException e) {
            // beim Laden von Seiten wird unter Umständen das Javaskript abgebrochen, oder kehrt nicht korrekt zurück
            // in diesem Fall wird ebenfalls weiter gewartet, sonst wird ein Fehler ausgegeben
            if (!e.getMessage().startsWith("JavaScript error")){
                throw e;
            }
                                     
        }

        return ret;
              
    }
}
