package org.tdar.functional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.linkText;
import static org.openqa.selenium.By.name;
import static org.openqa.selenium.By.partialLinkText;
import static org.openqa.selenium.By.tagName;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
import static org.tdar.functional.util.TdarExpectedConditions.bootstrapModalGone;
import static org.tdar.functional.util.TdarExpectedConditions.locatedElementCountEquals;
import static org.tdar.functional.util.TdarExpectedConditions.locatedElementCountGreaterThan;
import static org.tdar.functional.util.TdarExpectedConditions.textToBePresentInElementsLocated;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.tdar.functional.util.ByLabelText;
import org.tdar.functional.util.WebElementSelection;

public class IntegrationSeleniumWebITCase extends AbstractBasicSeleniumWebITCase {

    private static final String TEST_INTEGRATION = "test integration";
    private static final String SPITAL_DT_ID = "3091";
    private static final String ALEX_DT_ID = "3104";
    private static final String SPITALFIELD_CHECKBOX = "cbResult" + SPITAL_DT_ID;
    private static final String ALEXANDRIA_CHECKBOX = "cbResult" + ALEX_DT_ID;

    private static final By saveButton = id("btnSave");
    By aves = id("cbont_64870");
    By rabbit = id("cbont_63000");
    By sheep = id("cbont_62580");

    @Before
    public void setupIntegration() {
        login();
        find(By.partialLinkText("Integrate")).click();
        Assert.assertTrue(getText().contains("Data Integration"));
        Assert.assertTrue(getCurrentUrl().contains("/workspace/list"));
        find(linkText("Start Now")).click();
        Assert.assertTrue(getCurrentUrl().contains("/workspace/integrate"));
    }

    @Test
    public void testInvalidIntegrate() throws InterruptedException {
        // add three datasets that don't work, remove one, assert that we get back to an integratable state

        setFieldByName("integration.title", TEST_INTEGRATION);
        // assert save enabled
        openDatasetsModal();
        // uncheck
        find(name("searchFilter.integrationCompatible")).click();
        // note that IDs are dataTable ids
        findAndClickDataset("Knowth", "cbResult3089");
        // re-check
        find(name("searchFilter.integrationCompatible")).click();
        // note that IDs are dataTable ids
        findAndClickDataset("Spitalf", SPITALFIELD_CHECKBOX);
        // note that IDs are dataTable ids
        findAndClickDataset("Alexandria", ALEXANDRIA_CHECKBOX);
        // add selected items
        find(className("btn-primary")).click();
        waitFor(bootstrapModalGone());

        // wait for modal to disappear and dataset list to populate
        waitFor(locatedElementCountEquals(className("sharedOntologies"), 0));
        waitFor(locatedElementCountGreaterThan(cssSelector("#selDatasets option"), 1));

        removeDatasetByPartialName("Knowth");
        assertThat(find(".sharedOntologies").toList(), hasSize(2));
        assertThat(getText(), not(containsString("Knowth")));
        waitFor(elementToBeClickable(linkText("Add Integration Column")));
    }

    @Test
    public void testValidIntegrate() throws InterruptedException {
        // add two datasets that work, assert that we get back to an integrate-able state

        setupSpitalfieldsAlexandriaForTest();
        Assert.assertEquals(2, find(className("sharedOntologies")).size());

        find(id("btnAddDisplayColumn")).click();
        By tab1 = id("tab0");
        waitFor(tab1).isDisplayed();
        // dt_ + tabid + _ + data_table_id
        By spital_select = id("dt_0_" + SPITAL_DT_ID);
        chooseSelectByName("Site Code", spital_select);
        By alex_select = id("dt_0_" + ALEX_DT_ID);
        chooseSelectByName("LOCATION", alex_select);
        takeScreenshot();
        find(linkText("Add Integration Column")).click();
        find(linkText("Fauna Taxon Ontology")).click();
        // wait for tab visible
        waitFor(id("tabtab1")).isDisplayed();
        // wait for tab contents is visible
        waitFor(id("tab1")).isDisplayed();
        find(ByLabelText.byLabelText("Aves")).click();// this is really slow, so do it once
        assertTrue(ExpectedConditions.elementSelectionStateToBe(id("cbont_64870"), true).apply(getDriver()).booleanValue());

        find(rabbit).click();
        assertTrue(ExpectedConditions.elementSelectionStateToBe(rabbit, true).apply(getDriver()).booleanValue());
        find(sheep).click();
        waitFor(elementToBeClickable(saveButton));

        assertTrue("Perca flavescens node should be visible for spitalfields", find(id("cbx-32450-60600")).isDisplayed());
        assertTrue("Coding error should be visible for alexandria", find(id("cbx-31710-56490")).isDisplayed());
        assertTrue(ExpectedConditions.elementSelectionStateToBe(sheep, true).apply(getDriver()).booleanValue());

        // click the integrate button and wait for results
        find(id("btnIntegrate")).click();
        waitFor(visibilityOfElementLocated(id("divResultContainer")));
        takeScreenshot();
        clearPageCache();
        String pivotTableText = find("#divResultContainer tbody").first().getText();
        find(linkText("Preview")).click();

        // Capture the contents of the preview table body.
        String previewTableText = find("#divResultContainer tbody").last().getText();
        logger.debug("previewTableText: {}", previewTableText);
        logger.debug("pivotTableText: {}", pivotTableText);

        // test ontology present
        // todo: jtd: I don't think we show the names of the ontologies in the new integration preview (are we supposed to?). commenting out for now.
        // assertTrue(pivotTableText.contains("Fauna Taxon"));

        // test nodes present
        assertThat(pivotTableText, containsString("Aves"));
        assertThat(pivotTableText.toLowerCase(), containsString("sheep"));
        assertThat(pivotTableText.toLowerCase(), containsString("rabbit"));

        // test databases present
        assertThat(previewTableText, containsString("Spitalfields"));
        assertThat(previewTableText, containsString("Alexandria"));
    }

    @Test
    public void testSaveAndReload() throws InterruptedException {
        // add two datasets that work, assert that we get back to an integratable state

        setupSpitalfieldsAlexandriaForTest();
        Assert.assertEquals(2, find(className("sharedOntologies")).size());

        find(id("btnAddDisplayColumn")).click();
        By tab1 = id("tab0");
        waitFor(tab1).isDisplayed();
        // dt_ + tabid + _ + data_table_id
        By spital_select = id("dt_0_" + SPITAL_DT_ID);
        chooseSelectByName("Site Code", spital_select);
        By alex_select = id("dt_0_" + ALEX_DT_ID);
        chooseSelectByName("LOCATION", alex_select);

        takeScreenshot();
        find(linkText("Add Integration Column")).click();
        find(linkText("Fauna Taxon Ontology")).click();
        // wait for tab visible
        waitFor(id("tabtab1")).isDisplayed();
        // wait for tab contents is visible
        waitFor(id("tab1")).isDisplayed();
        logger.debug(getText());

        find(aves).click();
        assertThat(find(aves).isSelected(), is(true));
        find(rabbit).click();
        assertThat(find(rabbit).isSelected(), is(true));
        find(sheep).click();
        waitFor(elementToBeClickable(saveButton));

        assertThat(find(sheep).isSelected(), is(true));
        waitFor(saveButton).click();
        waitFor(textToBePresentInElementLocated(id("divStatusMessage"), "Saved"), 20);
        // waitFor(4);
        gotoPage("/workspace/list");
        logger.debug(getText());
        clearPageCache();
        find(partialLinkText(TEST_INTEGRATION)).first().click();
        waitForPageload();

        waitFor(partialLinkText("Fauna Taxon Ontology")).click();
        waitFor(".nodechild1");
        logger.trace(getText());
        takeScreenshot("expecting populated fauna taxon pane");

        assertEquals(find(name("integration.title")).val(), TEST_INTEGRATION);

        assertThat("the ontology pane should contain these words", getText().toLowerCase(), allOf(
                containsString("aves"),
                containsString("rabbit"),
                containsString("taxon"),
                containsString("element"),
                containsString("spitalfield"),
                containsString("alexandria")
                ));

        assertThat(find(aves).isSelected(), is(true));
        assertThat(find(rabbit).isSelected(), is(true));
        assertThat(find(sheep).isSelected(), is(true));
    }

    @Test
    public void testIntegrateRetainCheckboxOnClearAll() throws InterruptedException {
        // add two datasets that work, assert that we get back to an integratable state

        setupSpitalfieldsAlexandriaForTest();
        Assert.assertEquals(2, find(className("sharedOntologies")).size());

        // add integration column with a few check boxes
        takeScreenshot();
        find(linkText("Add Integration Column")).click();
        find(linkText("Fauna Taxon Ontology")).click();
        // wait for tab visible
        waitFor(id("tabtab0")).isDisplayed();
        // wait for tab contents is visible
        waitFor(id("tab0")).isDisplayed();

        // click on aves, rabbit, and sheep in the 'Fauna Taxon Ontology' integration column
        find(aves).click();
        find(rabbit).click();
        find(sheep).click();

        assertThat(find(aves).isSelected(), is(true));
        assertThat(find(rabbit).isSelected(), is(true));
        assertThat(find(sheep).isSelected(), is(true));

        waitFor(elementToBeClickable(saveButton));

        // remove the datasets
        removeDatasetByPartialName("Spital");
        removeDatasetByPartialName("Alexandria");

        // add one back
        openDatasetsModal();
        findAndClickDataset("Spitalf", SPITALFIELD_CHECKBOX);

        find(id("btnModalAdd")).click();

        // wait for the browser to render the checkboxes (as soon as 'aves' shows up, the rest should be found as well)
        waitFor(aves);

        // make sure the checkboxes are still there
        assertThat(find(aves).isSelected(), is(true));
        assertThat(find(rabbit).isSelected(), is(true));
        assertThat(find(sheep).isSelected(), is(true));

    }

    private void setupSpitalfieldsAlexandriaForTest() throws InterruptedException {
        setFieldByName("integration.title", TEST_INTEGRATION);
        // assert save enabled
        openDatasetsModal();
        findAndClickDataset("Spitalf", SPITALFIELD_CHECKBOX);
        // note that IDs are dataTable ids
        findAndClickDataset("Alexandria", ALEXANDRIA_CHECKBOX);
        // add selected items
        find(className("btn-primary")).click();

        waitFor(locatedElementCountGreaterThan(className("sharedOntologies"), 2));
    }

    private void removeDatasetByPartialName(String name) {
        By selector = id("selDatasets");
        chooseSelectByName(name, selector);
        find(id("rmDatasetBtn")).click();
    }

    private void chooseSelectByName(String name, By selector) {
        List<WebElement> findElements = find(selector).first().findElements(tagName("option"));
        for (WebElement el : findElements) {
            if (el.getText().contains(name)) {
                el.click();
                break;
            }
        }
    }

    /**
     * Enter specified text into "add datasets..." popup's search filter, and wait for element with specified
     * ID to appear in results.
     * 
     * @param text
     * @param cbid
     */
    private void findAndClickDataset(String text, String cbid) {
        WebElementSelection currentResults = find("#modalResults tbody tr");
        find(name("searchFilter.title")).val(text);
        if (!currentResults.isEmpty()) {
            waitFor(ExpectedConditions.stalenessOf(currentResults.last()));
        }
        // wait for response ... would be nice to not use this, but we could already have the checkbox, and have issues when the ajax cycles back
        // fixme: this wait seems to be necessary for some reason
        // waitFor(2);
        // wait until the one of the rows contains the specified text in the 'title' column
        waitFor(textToBePresentInElementsLocated(cssSelector("#modalResults tbody tr>td:nth-child(2)"), text));
        // note that IDs are dataTable ids
        By checkbox = id(cbid);
        waitFor(elementToBeClickable(checkbox));
        find(checkbox).click();
    }

    private void openDatasetsModal() throws InterruptedException {
        // wait for modal to load
        find(id("btnAddDataset")).click();
        // wait for results table
        waitFor(visibilityOfElementLocated(className("table-striped")));
    }
}