Playwright sample ![Logo][1]
==============

This sample shows how to use [Playwright](https://playwright.dev/java/) in order to create UI testing with Citrus.
Why would somebody combine Citrus with Playwright?
On the one hand Citrus provides a Java fluent API integration for Playwright tasks so you do not need to write Playwright code yourself.
Secondly you might want to combine UI testing with Citrus messaging capabilities such as deleting all entries on a Http REST service first and then
creating a new entry via UI interaction in a browser. Read about this feature in the [reference guide][4].

> **NOTE:** This sample requires **Citrus 5.1.0 or higher**. The `citrus-playwright` Maven module is new in
> Citrus 5.1.0 and does not exist in earlier releases. The sample POM therefore pins
> `<citrus.version>5.1.0</citrus.version>`. Maintainer: bump this version once Citrus 5.1.0 is released.
> Until then you can build the sample against a local `5.1.0-SNAPSHOT` framework build with
> `mvn verify -Dcitrus.version=5.1.0-SNAPSHOT` (see `citrus-endpoint-catalog` note below).

Objectives
---------

The [todo-list](../todo-app/README.md) sample application provides a REST API for managing todo entries.
We open the browser and perform some user interaction with the todo app user interface. Besides that we call the Http REST API in order to clean up
all todo entries up front. The Playwright integration is also used to validate HTML elements and page objects in our test cases.
This sample mirrors [sample-selenium](../sample-selenium/README.md) — same journeys, same page objects — so you can compare both browser integrations side by side.

First of all we add a Playwright browser component as Spring bean.

```java
@Bean
public PlaywrightBrowser browser() {
    return PlaywrightEndpoints
        .playwright()
            .browser()
            .browserType(PlaywrightBrowserType.CHROMIUM)
            .headless(true)
            .defaultTimeout(5000L)
        .build();
}
```

As you can see the browser component in Citrus manages the Playwright runtime for you. You can pick the target browser engine
(`CHROMIUM`, `FIREFOX`, `WEBKIT`), run headless or headed, and add launch, context, and observability options as you like.
Unlike Selenium there is no per-action timeout to set: Playwright auto-waits for elements, governed by the `defaultTimeout` above.

In addition to that basic browser component we add a normal Http client component for calling the Http REST API on the todo server.

```java
@Bean
public HttpClient todoClient() {
    return CitrusEndpoints
        .http()
            .client()
            .requestUrl("http://localhost:8080")
        .build();
}
```

Also we can use a new after suite hook in order to close the browser when all tests are finished:

```java
@Bean
@DependsOn("browser")
public AfterSuite afterSuite(PlaywrightBrowser browser) {
    return new SequenceAfterSuite.Builder()
            .actions(PlaywrightActionBuilder.playwright().browser(browser).stop())
            .build();
}
```

For demonstration purpose we add a `sleep` action in a new after test hook so we see actually what the browser is doing.

```java
@Bean
public AfterTest afterTest() {
    return new SequenceAfterTest.Builder()
            .actions(actions.sleep().milliseconds(500L))
            .build();
}
```

In a test case we can now use the Playwright browser component in the Citrus fluent API.

```java
@Test
@CitrusTest
public void testAddEntry() {
    variable("todoName", "todo_citrus:randomNumber(4)");
    variable("todoDescription", "Description: ${todoName}");

    $(http()
        .client(todoClient)
        .send()
        .delete("/api/todolist"));

    $(http()
        .client(todoClient)
        .receive()
        .response(HttpStatus.OK));

    $(playwright()
        .browser(browser)
        .start());

    $(playwright()
        .browser(browser)
        .open()
        .url(todoClient.getEndpointConfiguration().getRequestUrl() + "/todolist"));

    $(playwright()
        .browser(browser)
        .verify()
        .locator(LocatorSpec.text("No todos found"))
        .text("No todos found"));

    $(playwright()
        .browser(browser)
        .fill()
        .locator("input[name=title]")
        .value("${todoName}"));

    $(playwright()
        .browser(browser)
        .fill()
        .locator("textarea[name=description]")
        .value("${todoDescription}"));

    $(playwright()
        .browser(browser)
        .click()
        .locator("form button[type=submit]"));

    $(playwright()
        .browser(browser)
        .waitFor()
        .locator(LocatorSpec.text("${todoName}"))
        .visible());

    $(playwright()
        .browser(browser)
        .verify()
        .locator(LocatorSpec.text("${todoName}"))
        .text("${todoName}"));
}
```

We call the `DELETE` option on the `/todolist` Http resource in order to have an empty todo list that we can operate on. The Playwright
browser is started and the user navigates to the todo list application. The testcase validates the empty todo list display and fills out the todo HTML form
with a sample entry. The form is submitted and a final validation checks that the todo entry is added and displayed in the list.

Locators are CSS selectors by default; richer variants are available via `LocatorSpec`:

```java
$(playwright()
    .browser(browser)
    .click()
    .locator(LocatorSpec.text("Run application")));

$(playwright()
    .browser(browser)
    .click()
    .locator(LocatorSpec.role("button").name("Submit")));
```

Page objects
---------

Using page objects that represent an application page with its elements and action capabilities is a good practice in UI testing.
Citrus Playwright supports plain Java POJOs as page objects: the `Page` is constructor-injected and actions are regular methods.

```java
public class TodoPage implements PlaywrightPage {

    private final Page page;

    public TodoPage(Page page) {
        this.page = page;
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
        @Override
        public void validate(TodoPage webPage, PlaywrightBrowser browser, TestContext context) {
            Assert.assertEquals(webPage.getPage().locator("h1").textContent().trim(), "TODO list");
            Assert.assertEquals(webPage.getPage().getByText("No todos found").textContent().trim(), "No todos found");
        }
    }
}
```

The page object listed above represents the todo list page with its elements and actions such as `submit`. A nested `Validator`
adds validation methods for checking that the rendered page is as expected (validators are instantiated separately by Citrus, so they live in their own class).

In a test case we can use these page objects for user interaction and validation:

```java
@Test
@CitrusTest
public void testAddEntry() {
    variable("todoName", "todo_citrus:randomNumber(4)");
    variable("todoDescription", "Description: ${todoName}");

    [...]

    $(playwright()
        .browser(browser)
        .pageObject()
        .type(TodoPage.class)
        .validate(TodoPage.Validator.class));

    $(playwright()
        .browser(browser)
        .pageObject()
        .type(TodoPage.class)
        .arguments("${todoName}", "${todoDescription}")
        .execute("submit"));

    [...]
}
```

Run
---------

**NOTE:** This test depends on the [todo-app](../todo-app/) WAR which must have been installed into your local maven repository using `mvn clean install` beforehand.
You also need **Citrus 5.1.0+** (see the note at the top of this file). Until 5.1.0 is released you can verify the sample against a
local framework snapshot build with `mvn verify -Dcitrus.version=5.1.0-SNAPSHOT` — this additionally requires a locally installed
`citrus-endpoint-catalog:5.1.0-SNAPSHOT`, otherwise pin that single dependency to `5.0.0` for the local run.

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.

     mvn clean verify -Dsystem.under.test.mode=embedded

This executes the complete Maven build lifecycle. The embedded option automatically starts a Jetty web
container before the integration test phase. The todo-list system under test is automatically deployed in this phase.
After that the Citrus test cases are able to interact with the todo-list application in the integration test phase.

The build also installs the Playwright Chromium browser binaries automatically (via an `exec-maven-plugin` step bound to
`generate-test-resources`, same approach as the `playwright-runtimes` profile in `citrus-playwright` itself), so a plain
`mvn verify` just works. To use a different engine, set `-Dplaywright.browser=firefox` (or `webkit`).

During the build you will see Citrus performing some integration tests.
After the tests are finished the embedded Jetty web container and the todo-list application are automatically stopped.

System under test
---------

The sample uses a small todo list application as system under test. The application is a web application
that you can deploy on any web container. You can find the todo-list sources [here](../todo-app). Up to now we have started an
embedded Jetty web container with automatic deployments during the Maven build lifecycle. This approach is fantastic
when running automated tests in a continuous build.

Unfortunately the Jetty server and the sample application automatically get stopped when the Maven build is finished.
There may be times we want to test against a standalone todo-list application.

You can start the sample todo list application in Jetty with this command.

     mvn jetty:run

This starts the Jetty web container and automatically deploys the todo list app. Point your browser to

    http://localhost:8080/todolist/

You will see the web UI of the todo list and add some new todo entries.

Now we are ready to execute some Citrus tests in a separate JVM.

Citrus test
---------

Once the sample application is deployed and running you can execute the Citrus test cases.
Open a separate command line terminal and navigate to the sample folder.

Execute all Citrus tests by calling

     mvn verify

You can also pick a single test by calling

     mvn verify -Dit.test=<testname>

You should see Citrus performing several tests with lots of debugging output in both terminals (sample application server
and Citrus test client). And of course green tests at the very end of the build.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.

Further information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
 [4]: https://citrusframework.org/reference/html#playwright
