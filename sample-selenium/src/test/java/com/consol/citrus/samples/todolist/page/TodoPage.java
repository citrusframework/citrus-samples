package com.consol.citrus.samples.todolist.page;

import org.citrusframework.actions.selenium.PageValidator;
import org.citrusframework.actions.selenium.WebPage;
import org.citrusframework.context.TestContext;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.util.StringUtils;
import org.testng.Assert;

/**
 * @author Christoph Deppisch
 */
public class TodoPage implements WebPage, PageValidator<TodoPage, SeleniumBrowser> {

    @FindBy(tagName = "h1")
    private WebElement heading;

    @FindBy(tagName = "form")
    private WebElement newTodoForm;

    @FindBy(xpath = "(//li[@class='list-group-item'])[last()]")
    private WebElement lastEntry;

    /**
     * Submits new entry.
     * @param title
     * @param description
     */
    public void submit(String title, String description) {
        newTodoForm.findElement(By.id("title")).sendKeys(title);

        if (StringUtils.hasText(description)) {
            newTodoForm.findElement(By.id("description")).sendKeys(description);
        }

        newTodoForm.submit();
    }

    @Override
    public void validate(TodoPage webPage, SeleniumBrowser browser, TestContext context) {
        Assert.assertEquals(heading.getText(), "TODO list");
        Assert.assertEquals(lastEntry.getText(), "No todos found");
    }
}
