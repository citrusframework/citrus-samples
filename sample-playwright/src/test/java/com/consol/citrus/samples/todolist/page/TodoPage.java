package com.consol.citrus.samples.todolist.page;

import com.microsoft.playwright.Page;
import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.page.PlaywrightPage;
import org.citrusframework.playwright.page.PlaywrightPageValidator;
import org.springframework.util.StringUtils;
import org.testng.Assert;

/**
 * @author Christoph Deppisch
 */
public class TodoPage implements PlaywrightPage {

    private final Page page;

    public TodoPage(Page page) {
        this.page = page;
    }

    public Page getPage() {
        return page;
    }

    /**
     * Submits new entry.
     * @param title
     * @param description
     */
    public void submit(String title, String description) {
        page.fill("input[name=title]", title);

        if (StringUtils.hasText(description)) {
            page.fill("textarea[name=description]", description);
        }

        page.locator("form button[type=submit]").click();
    }

    public static class Validator implements PlaywrightPageValidator<TodoPage> {

        public Validator() {
        }

        @Override
        public void validate(TodoPage webPage, PlaywrightBrowser browser, TestContext context) {
            Assert.assertEquals(webPage.getPage().locator("h1").textContent().trim(), "TODO list");
            Assert.assertEquals(webPage.getPage().getByText("No todos found").textContent().trim(), "No todos found");
        }
    }
}
