package com.consol.citrus.samples.todolist.page;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.model.PageValidator;
import com.consol.citrus.selenium.model.WebPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

/**
 * @author Christoph Deppisch
 */
public class WelcomePage implements WebPage, PageValidator<WelcomePage> {

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
