package com.consol.citrus.samples.todolist.page;

import org.citrusframework.actions.selenium.PageValidator;
import org.citrusframework.actions.selenium.WebPage;
import org.citrusframework.context.TestContext;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

/**
 * @author Christoph Deppisch
 */
public class WelcomePage implements WebPage, PageValidator<WelcomePage, SeleniumBrowser> {

    @FindBy(tagName = "h1")
    private WebElement heading;

    @FindBy(linkText = "Run application")
    private WebElement runButton;

    /**
     * Starts application by clicking respective button on page.
     */
    public void startApp() {
        runButton.click();
    }

    @Override
    public void validate(WelcomePage webPage, SeleniumBrowser browser, TestContext context) {
        Assert.assertEquals("TODO List", heading.getText());
        Assert.assertTrue(runButton.isEnabled());
    }
}
