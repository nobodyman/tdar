package org.tdar.web.functional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.tdar.core.dao.external.auth.AuthenticationResult;

/**
 * This is a rewrite of org.tdar.web.LoginWebITCase.
 * 
 * @author jimdevos
 * 
 */
public class LoginSeleniumITCase extends AbstractSeleniumWebITCase {

    @Before
    public void setup() {
        login();
    }

    @After
    public void teardown() {
        logout();
    }
    
    @Test
    public void testAbstractLogin() {
        assertTrue(getText().contains("Welcome back,"));
    }

    @Test
    public void testSecondLogin() {
        gotoPage("/login");
        assertTrue(getText().contains("Featured Content"));
    }

    @Test
    public void testInvalidLogin() {
        logout();
        login("BADUSERNAME", "BADPASSWORD");
        logger.info(getDom());
        assertTrue(getDom().contains(AuthenticationResult.INVALID_PASSWORD.getMessage()));
        assertFalse(getText().contains("Your submitted projects"));
    }

    @Test
    public void testLogout() {
        logout();
        assertTrue(getText().contains("Log In"));
    }
    
    @Test
    public void testClickSomething() {
        find(By.linkText("edit")).first().click(); //one way of getting to a page
        gotoPage("/dashboard");  //another way
    }
    

}