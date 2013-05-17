package org.tdar.web.functional;

import static org.junit.Assert.fail;
import static org.tdar.TestConstants.DEFAULT_BASE_URL;

import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;

public abstract class FunctionalWebTestCase {

    WebDriver driver;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void before() throws MalformedURLException {
        /*
         * DesiredCapabilities abilities = DesiredCapabilities.firefox();
         * // abilities.setCapability("version", "16");
         * // abilities.setCapability("platform", Platform.WINDOWS);
         * abilities.setCapability("name", "Testing Selenium-2 Remote WebDriver");
         * 
         * driver = new RemoteWebDriver( new URL("http://localhost:4444/wd/hub"), abilities);
         * // driver = new RemoteWebDriver(remoteAddress, desiredCapabilities)
         */
        FirefoxBinary fb = new FirefoxBinary();
        String xvfbPropsFile = System.getProperty("display.port");
        if (StringUtils.isNotBlank(xvfbPropsFile)) {
            fb.setEnvironmentProperty("DISPLAY", xvfbPropsFile);
//            fb.set
        }
        driver = new FirefoxDriver(fb, new FirefoxProfile());
    }

    /*
     * Shutdown Selenium
     */
    @After
    public void after() {
        logger.debug("after");
        try {
            driver.close();
            driver = null;
        } catch (Exception ex) {
            logger.error("Could not close selenium driver: {}", ex);
        }
    }

    /*
     * createObsoluteUrl
     */
    public String url(String path) {
        return String.format("%s%s", DEFAULT_BASE_URL, path);
    }

    public void gotoPage(String path) {
        String url = url(path);
        logger.debug("going to {}", url);
        driver.get(url(path));
    }

    // TODO: find out if this is necessary for repeatrow buttons. Supposedly selenium will wait until domready is complete.
    public WebElement waitFor(String selector) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(selector)));
        WebElement result = null;
        if (!elements.isEmpty()) {
            result = elements.get(0);
        }
        return result;
    }

    /*
     * abrin:Find All?  
     * 
     * jtd:I concede your it is inconsistent (e.g. with Dao#find),  but I want the most frequently used functions to be the easiest to type. 
     * 
     * jtd: perhaps select()?
     */
    public WebElementSelection find(String selector) {
        return find(By.cssSelector(selector));
    }
    
    public WebElementSelection find(By by) {
        WebElementSelection selection = new WebElementSelection(driver.findElements(by));
        logger.debug("criteria:{}\t  size:{}", by, selection.size());
        return selection;
    }

    /*
     * Find First?
     */
    public WebElement findOne(String selector) {
        return find(selector).iterator().next();
    }

    
    public WebDriver getDriver() {
        return driver;
    }

    public void login() {
        login(TestConstants.USERNAME, TestConstants.PASSWORD);
    }

    public void login(String username, String password) {
        gotoPage("/login");
        find("#loginUsername").sendKeys(username);
        find("#loginPassword").sendKeys(password);
        find("#btnLogin").click();
    }

    public void logout() {
        gotoPage("/logout");
    }

    public String getSource() {
        return driver.getPageSource();
    }
    
    public String getDom() {
        return find("body").getHtml();
    }
    
    public String getText() {
        return find("body").getText();
    }
}
