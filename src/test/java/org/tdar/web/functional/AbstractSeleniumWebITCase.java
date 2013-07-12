package org.tdar.web.functional;

import static org.tdar.TestConstants.DEFAULT_BASE_URL;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.phantom.resolver.ResolvingPhantomJSDriverService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;

import javax.annotation.Nullable;

public abstract class AbstractSeleniumWebITCase {

    public static String REGEX_DOCUMENT_VIEW = ".+\\/document\\/\\d+$";
    public static Pattern PATTERN_DOCUMENT_VIEW = Pattern.compile(REGEX_DOCUMENT_VIEW);

    private boolean ignoreJavascriptErrors = false;
    private String pageText = null;

    WebDriver driver;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    // Predicates that can be used as arguments for FluentWait.until.
    private Predicate<WebDriver> pageReady = new Predicate<WebDriver>() {
        @Override
        public boolean apply(@Nullable WebDriver webDriver) {
            String readyState = (String) ((JavascriptExecutor) webDriver).executeScript("return document.readyState");
            return "complete".equals(readyState);
        }
    };

    private Predicate<WebDriver> pageNotReady = Predicates.not(pageReady);

    // FIXME: it seems silly that you can't hook into some kind of "
    private WebDriverEventListener eventListener = new WebDriverEventListener() {
        public void afterNavigateTo(String url, WebDriver driver) {
        }

        public void beforeNavigateBack(WebDriver driver) {
        }

        public void afterNavigateBack(WebDriver driver) {
        }

        public void beforeNavigateForward(WebDriver driver) {
        }

        public void afterNavigateForward(WebDriver driver) {
        }

        public void beforeFindBy(By by, WebElement element, WebDriver driver) {
        }

        public void afterFindBy(By by, WebElement element, WebDriver driver) {
        }

        public void afterClickOn(WebElement element, WebDriver driver) {
        }

        public void beforeChangeValueOf(WebElement element, WebDriver driver) {
        }

        public void afterChangeValueOf(WebElement element, WebDriver driver) {
        }

        public void beforeScript(String script, WebDriver driver) {
        }

        public void afterScript(String script, WebDriver driver) {
        }

        public void onException(Throwable throwable, WebDriver driver) {
        }

        public void beforeClickOn(WebElement element, WebDriver driver) {
            if (elementCausesNavigation(element)) {
                beforePageChange();
            }
        }

        private boolean elementCausesNavigation(WebElement element) {
            String tag = element.getTagName();
            return (tag.equals("a"))
                    || (tag.equals("input") && "submit".equals(element.getAttribute("type")))
                    || (tag.equals("button") && "submit".equals(element.getAttribute("type")));
        }

        public void beforeNavigateTo(String url, WebDriver driver) {
            beforePageChange();
        }
    };

    private void beforePageChange() {
        reportJavascriptErrors();
        clearPageCache();
    }

    private void afterPageChange() {

    }

    private void clearPageCache() {
        pageText = null;
    }

    private enum Browser {
        FIREFOX, CHROME, SAFARI, IE, PHANTOMJS;
    }

    @Before
    public void before() throws IOException {
        /*
         * We define a specific binary so when running "headless" we can specify a PORT
         */
        String fmt = " ***   RUNNING TEST: {}.{}() ***";
        logger.info(fmt, getClass().getSimpleName(), testName.getMethodName());
        WebDriver driver = null;
        Browser browser = Browser.CHROME;
        String xvfbPort = System.getProperty("display.port");
        String browser_ = System.getProperty("browser");
        if (StringUtils.isNotBlank(browser_)) {
            try {
                browser = Browser.valueOf(browser_);
                logger.debug("seting browser to: {}", browser);
            } catch (Exception e) {
                logger.error("{}", e);
            }
        }
        Map<String, String> environment = new HashMap<String, String>();
        if (StringUtils.isNotBlank(xvfbPort)) {
            environment.put("DISPLAY", xvfbPort);
        }
        switch (browser) {
            case FIREFOX:
                FirefoxBinary fb = new FirefoxBinary();
                for (String key : environment.keySet()) {
                    fb.setEnvironmentProperty(key, environment.get(key));
                }
                driver = new FirefoxDriver(fb, new FirefoxProfile());
                break;
            case CHROME:
                /* yes, this is ugly */
                /* ubuntu install instructions http://www.liberiangeek.net/2011/12/install-google-chrome-using-apt-get-in-ubuntu-11-10-oneiric-ocelot/ */
                File app = new File("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
                if (!app.exists()) {
                    app = new File("C:\\Users\\%USERNAME%\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe");
                }
                if (!app.exists()) {
                    app = new File("/usr/bin/google-chrome");
                }

                ChromeDriverService options = new ChromeDriverService.Builder().usingDriverExecutable(app).withEnvironment(environment).build();
                driver = new ChromeDriver(options);
                options.start();
                break;
            case IE:
                driver = new InternetExplorerDriver();
            case PHANTOMJS:
                driver = new PhantomJSDriver(
                        ResolvingPhantomJSDriverService.createDefaultService(), // service resolving phantomjs binary automatically
                        DesiredCapabilities.phantomjs());
                break;
        }
        EventFiringWebDriver eventFiringWebDriver = new EventFiringWebDriver(driver);
        eventFiringWebDriver.register(eventListener);

        this.driver = eventFiringWebDriver;
    }

    // /**
    // * @return firefox profile that has CSS rendering disabled.
    // */
    // public final FirefoxProfile firefoxProfileNoCss() {
    // // http://stackoverflow.com/questions/3526361/firefoxdriver-how-to-disable-javascript-css-and-make-sendkeys-type-instantly
    // FirefoxProfile profile = new FirefoxProfile();
    // profile.setPreference("permissions.default.stylesheet", 2);
    // // profile.setPreference("permissions.default.image", 2);
    // return profile;
    // }
    //

    @Rule
    public TestName testName = new TestName();
    private static boolean reindexed = false;

    /*
     * Shutdown Selenium
     */
    @After
    public final void shutdownSelenium() {
        logger.debug("after");
        takeScreenshot();
        try {
            driver.quit();
        } catch (UnhandledAlertException uae) {
            logger.error("alert modal present when trying to close driver: {}", uae.getAlertText());
            driver.switchTo().alert().dismiss();
            driver.quit();
        } catch (Exception ex) {
            logger.error("Could not close selenium driver: {}", ex);
        }
        driver = null;
        String fmt = " *** COMPLETED TEST: {}.{}() ***";
        logger.info(fmt, getClass().getCanonicalName(), testName.getMethodName());
    }

    private void takeScreenshot() {
        try {
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            // Now you can do whatever you need to do with it, for example copy somewhere
            File dir = new File("target/screenshots/" + getClass().getSimpleName() + "/" + testName.getMethodName());
            dir.mkdirs();
            FileUtils.copyFile(scrFile, new File(dir, System.currentTimeMillis() + ".png"));
        } catch (Exception e) {
            logger.error("could not take screenshot", e);
        }
    }

    /*
     * createObsoluteUrl
     */
    public String absoluteUrl(String path) {
        String currentUrl = driver.getCurrentUrl();
        logger.debug("current url: {}", currentUrl);
        if (currentUrl.length() == 0 || StringUtils.equalsIgnoreCase("about:blank", currentUrl)) {
            currentUrl = DEFAULT_BASE_URL;
        }
        URL url = null;
        try {
            url = new URL(currentUrl);
        } catch (MalformedURLException e) {
            Assert.fail("could not go to url: " + currentUrl);
        }
        String absoluteUrl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + path;
        return absoluteUrl;
    }

    public void gotoPage(String path) {
        String url = absoluteUrl(path);
        logger.debug("going to {}", url);
        takeScreenshot();
        driver.get(absoluteUrl(path));
        takeScreenshot();
    }

    public WebElement waitFor(String selector) {
        return waitFor(selector, 10);
    }

    public WebElement waitFor(String cssSelector, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, timeoutInSeconds);
        List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(cssSelector)));
        WebElement result = null;
        if (!elements.isEmpty()) {
            result = elements.get(0);
        }
        return result;
    }

    public WebElementSelection find(String selector) {
        return find(By.cssSelector(selector));
    }

    public WebElementSelection find(By by) {
        logger.trace("find start: {}", by);
        WebElementSelection selection = new WebElementSelection(driver.findElements(by));
        logger.debug("criteria:{}\t  size:{}", by, selection.size());
        logger.trace("find   end: {}", by);
        return selection;
    }

    // FIXME: select() seems more appropriate, given the ways you can select stuff. Or, since we're aping jquery.. just $()?
    /**
     * Create a selection out of one or more.
     * 
     * @param elems
     *            WebElement objects
     * @return
     */
    public WebElementSelection find(WebElement... elems) {
        return new WebElementSelection(Arrays.asList(elems));
    }

    public WebElement findFirst(String selector) {
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
        if (pageText == null) {
            logger.trace("getting body.innerText for url:{}", getDriver().getCurrentUrl());
            WebElement body = waitFor("body");
            pageText = body.getText();
        }
        return pageText;
    }

    @SuppressWarnings("unchecked")
    // this is a convenience so that callers don't have to cast. It's probably a bad idea.
    /**
     * execute a snippet of javascript in an anonymous function.  if your snippet returns a value, Selenium will attempt to cast the most "appropriate"
     * java type (String, Double, Integer, etc)  or a WebElement if you return a DOM node.  
     * @param functionBody
     * @param arguments arguments applied to the anonymous function. you can reference them in your snippet using  javascript's 
     * contextual <code>arguments</code> object.
     * @return selenium's best approximation of the value returned by your snippet, if it exists.
     */
    public <T> T executeJavascript(String functionBody, Object... arguments) {
        JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        Object result = executor.executeScript(functionBody, arguments);
        return (T) result;
    }

    /**
     * same as {@link #executeJavascript(String, Object...) but with exceptions suppressed}
     * 
     * @param functionBody
     * @param arguments
     * @return
     */
    public <T> T executeJavascriptSilently(String functionBody, Object... arguments) {
        T result = null;
        try {
            result = executeJavascript(functionBody, arguments);
        } catch (Exception ignored) {
        }
        return result;
    }

    // FIXME: implement someday. the tricky part is supporting nested properties e.g. "elem.style.position", especially when property doesn't exist yet
    public void setAttribute(WebElement elem, String property, Object value) {
        throw new RuntimeException("no");
    }

    public void setStyle(WebElement elem, String property, Object value) {
        executeJavascript("arguments[0].style[arguments[1]]=arguments[2]", elem, property, value);
    }

    public void setStyle(WebElementSelection selection, String property, Object value) {
        for (WebElement element : selection) {
            setStyle(element, property, value);
        }
    }

    /**
     * This is a hack that enables selenium to work with the Blueimp jQuery File Upload widget. Typically in selenium you "upload" a file using
     * the sendKeys() method, but this will not work when using the fileupload widget because it uses CSS styles to hide the text-entry box, and selenium
     * will not execute sendkeys() on elements that selenium determines to be invisible to the user.
     */
    public void clearFileInputStyles() {
        WebElement input = find("#fileAsyncUpload").first();
        showAsyncFileInput(input);
    }

    /**
     * This is a hack that enables selenium to work with the Blueimp jQuery File Upload widget. Typically in selenium you "upload" a file using
     * the sendKeys() method, but this will not work when using the fileupload widget because it uses CSS styles to hide the text-entry box, and selenium
     * will not execute sendkeys() on elements that selenium determines to be invisible to the user.
     * 
     * @param input
     *            the actual file input element (not the div that renders the jquery file upload widget)
     */
    public void showAsyncFileInput(WebElement input) {
        setStyle(input, "position", "static");
        setStyle(input, "top", "auto");
        setStyle(input, "right", "auto");
        setStyle(input, "margin", 0);
        setStyle(input, "opacity", 1);
        setStyle(input, "transform", "none");
        setStyle(input, "direction", "ltr");
        setStyle(input, "cursor", "auto");
    }

    /**
     * @param fieldName
     * @return true if fieldname follows struts indexed name convention (e.g. person[3].id)
     */
    public boolean isIndexedField(String fieldName) {
        String indexedNamePattern = "(.+)\\[(\\d+)\\](\\..+)?";
        return fieldName.matches(indexedNamePattern);
    }

    /**
     * @param fieldName
     *            name attribute value of field. which is expected to follow the struts naming pattern for collections
     * @return
     */
    private WebElement getZerothElement(String fieldName) {
        String indexedNamePattern = "(.+)\\[(\\d+)\\](\\..+)?";
        String zerothFieldName = fieldName.replaceAll(indexedNamePattern, "$1[0]$3");
        logger.debug("old name:\t{}", fieldName);
        logger.debug("new name:\t{}", zerothFieldName);
        WebElementSelection selection = find(By.name(zerothFieldName));
        if (selection.isEmpty()) {
            return null;
        } else {
            return selection.first();
        }
    }

    /**
     * Return a element with the specified name, which is assumed to follow tDAR's 'repeatable' element convention.
     * If the element doesn't exist, this method tries to locate the 'add another row' button and clicks it until an element with the specified
     * field name exists.
     * 
     * @param fieldName
     *            name attribute of the element to find.
     * @return The element with the specified name attribute, or null if this method could neither find the element or implicitly create it.
     */
    public WebElement findOrCreateIndexedField(String fieldName) {
        WebElement result = null;
        WebElementSelection elem = find(By.name(fieldName));
        if (elem.isEmpty()) {
            WebElement zeroElem = getZerothElement(fieldName);
            String repeatLastRowId = find(zeroElem).parentsWithClass("repeatLastRow").getAttribute("id");
            String buttonSelector = "#" + repeatLastRowId + " + .add-another-control button";
            // selector for button after container (e.g. "#resourceNotesSection + .add-another-control button")
            WebElement button = find(buttonSelector).first();
            // FIXME: create private method that takes indexed fieldname and returns index as int (or -1 if not a valid fieldname)
            int attempts = clickElementUntil(button, By.name(fieldName), 100);
            result = find(By.name(fieldName)).first();
        } else {
            result = elem.first();
        }
        return result;
    }

    /**
     * Click an element zero or more times until another element is present on the page as determined by the specified By criteria (for example, clicking a
     * tdar "add another item" button until the page generates 10 blank person records)
     * 
     * @param findBy
     *            the criteria to use to find a matching element on the page
     * @param max
     *            the maximum number of times this method should click every element if a match has not been found.
     * @return the number of clicks performed (per selected item), or -1 if a matching element was never found.
     */
    public int clickElementUntil(WebElement element, By findBy, int max) {
        int i = 0;
        while (find(findBy).size() == 0 && i < max) {
            element.click();
            i++;
        }
        return i;
    }

    public void loginAdmin() {
        login(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
    }

    public boolean hasReindexedOnce() {
        return reindexed;
    }

    public void reindex() {
        logout();
        loginAdmin();
        gotoPage("/admin/searchindex/build");
        find("#idxBtn").click();
        waitFor("#spanDone", 120);
        logout();
        reindexed = true;
    }

    /**
     * Submit a "tDAR edit page" style form (if no javascript errors since last pageload)
     * 
     * Considerations: this function assumes a the layout of a typical edit page on tDAR. For example, it expects
     * the submit button ID value is "submitButton".
     */
    public void submitForm() {
        reportJavascriptErrors();
        find("#submitButton").click();
        waitForPageload();
    }

    /**
     * Block until document loaded (readystate === true). This is usually unnecesssary since most click() actions
     * block anyway. Use it for situations where an event handler causes navigation (e.g. jquery validate success)
     * 
     * @param timeout
     */
    private void waitForPageload(int timeout) {
        // if called too soon, page navigation might not have happened yet - give it a second.
        if (pageReady.apply(getDriver())) {
            try {
                WebDriverWait wait = new WebDriverWait(getDriver(), 1, 100);
                wait.until(pageNotReady);
            } catch (Exception ignored) {
            }

        }

        WebDriverWait wait = new WebDriverWait(getDriver(), timeout);
        wait.until(pageReady);
    }

    public void waitForPageload() {
        waitForPageload(60);
    }

    public List<String> getJavascriptErrors() {
        return executeJavascript("return window.__errorMessages;");
    }

    public void setIgnoreJavascriptErrors(boolean ignoreJavascriptErrors) {
        this.ignoreJavascriptErrors = ignoreJavascriptErrors;
    }

    /**
     * If any javascript errors have occured since last pageload, log them and (if ignoreJavascriptErrors==false) fail the test.
     * 
     * Note: most actions that cause page navigation will implicitly callreportJavascriptErrors() anyway, such as formSubmit(), gotoPage(), and click events on
     * links & buttons. An example of when you might wish to explicitly call this method is when you expect a javascript function to modify the
     * <code>Window.location</code> property, or if you call {@link WebElement#submit()} rather than submitForm();
     */
    public void reportJavascriptErrors() {
        List<String> errors = getJavascriptErrors();
        if (errors == null)
            return;
        logger.trace("javascript error report for {}", driver.getCurrentUrl());
        for (String error : errors) {
            logger.error("javascript error: {}", error);
        }
        if (!errors.isEmpty() && !ignoreJavascriptErrors) {
            Assert.fail("Encountered javascript errors on page: " + driver.getCurrentUrl());
        }
    }

    protected boolean sourceContains(String substring) {
        return getSource().contains(substring);
    }

    protected boolean textContains(String substring) {
        return getText().toLowerCase().contains(substring.toLowerCase());
    }

    public void reindexOnce() {
        if (!hasReindexedOnce()) {
            reindex();
        }
    }
}
