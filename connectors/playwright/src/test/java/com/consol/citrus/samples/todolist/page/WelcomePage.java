package com.consol.citrus.samples.todolist.page;

import com.microsoft.playwright.Page;
import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.page.PlaywrightPage;
import org.citrusframework.playwright.page.PlaywrightPageValidator;
import org.testng.Assert;

/**
 * Page object for the welcome page. Starts the todo-list application.
 */
public class WelcomePage implements PlaywrightPage {

    private final Page page;

    public WelcomePage(Page page) {
        this.page = page;
    }

    public Page getPage() {
        return page;
    }

    /**
     * Starts application by clicking respective button on page.
     */
    public void startApp() {
        page.locator("text=Run application").click();
    }

    public static class Validator implements PlaywrightPageValidator<WelcomePage> {

        public Validator() {
        }

        @Override
        public void validate(WelcomePage webPage, PlaywrightBrowser browser, TestContext context) {
            Assert.assertEquals(webPage.getPage().locator("h1").textContent().trim(), "TODO List");
            Assert.assertTrue(webPage.getPage().locator("text=Run application").isEnabled());
        }
    }
}
