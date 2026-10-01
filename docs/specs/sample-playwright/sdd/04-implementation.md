# Implementation Progress: sample-playwright module

## Completed
- [x] T1: Module POM + root build registration — `sample-playwright/pom.xml` created as a
  Selenium-POM port (`citrus-sample-playwright`, `citrus-playwright` dep,
  `<citrus.version>5.1.0</citrus.version>` + prerequisite comment, `exec-maven-plugin`
  Chromium install step); root `pom.xml` `<module>` added
- [x] T2: Endpoint configuration — `EndpointConfig.java` with Chromium `PlaywrightBrowser`
  bean (headless, `defaultTimeout` 5000), `todoClient`, after-suite stop + after-test sleep
- [x] T3: Raw-locator test flow — `TodoListIT.java` (`testIndexPage`, `testAddEntry`);
  Red: written before `EndpointConfig` existed (no `browser`/`todoClient` beans);
  Green: `verify` green (see Verification below)
- [x] T4: Page objects + page-object flow — `page/TodoPage.java`, `page/WelcomePage.java`
  (constructor-injected Playwright `Page` + nested `Validator`), `TodoPagesIT.java`;
  Red: `pageObject().type(...).validate(...)/.execute("submit")` had no operands;
  Green: `verify` green
- [x] T5: Test resources — both properties files in place; `diff` against selenium is empty
- [x] T6: READMEs — module README (Playwright snippets, 5.1.0 note, browser-install and
  `-Dplaywright.browser` docs) + root README table row
- [x] T7: Full verification — `mvn -pl sample-playwright verify
  -Dsystem.under.test.mode=embedded`: **4 tests, 0 failures** (see Verification);
  `capspec.py validate`: 0 errors

## In Progress
- None

## Remaining
- None

## Blockers
- **Citrus 5.1.0 is not released** (latest on Central: 5.0.2; confirmed via repo metadata
  2026-10-01). The committed POM declares `<citrus.version>5.1.0</citrus.version>`, so a
  plain `mvn verify` cannot resolve dependencies until release. Verified instead with
  `-Dcitrus.version=5.1.0-SNAPSHOT` against the maintainer's local framework build, with
  `citrus-endpoint-catalog` temporarily at `5.0.0` (no local 5.1.0-SNAPSHOT catalog jar;
  building it from framework source needs `citrus-selenium:5.1.0-SNAPSHOT` +
  `jakarta.activation:2.1.4`, unresolvable in this sandbox). Temporary pin reverted —
  committed POM is uniform `${citrus.version}` = 5.1.0.
- Maintainer action on release: run plain `mvn clean verify
  -Dsystem.under.test.mode=embedded` in `sample-playwright/` (browsers self-install);
  expect 4 green ITs.

## Deviations from Plan
- **No per-action `.timeout(...)`**: `WaitForAction.Builder` has no timeout option (unlike
  Selenium); endpoint-level `.defaultTimeout(5000L)` + Playwright auto-wait covers it.
  README documents this as an intentional difference.
- **`exec-maven-plugin` browser install in module POM** (no selenium equivalent): installs
  Chromium at `generate-test-resources` so plain `mvn verify` works; engine selectable via
  `-Dplaywright.browser`. Mirrors the `playwright-runtimes` profile approach inside
  `citrus-playwright` itself.
- **Page-object shape differs from Selenium**: the Playwright `PageObjectAction` creates the
  page via constructor injection (`TodoPage(Page)`) and instantiates validators separately
  via no-arg constructor, so validators are nested `Validator` classes
  (`TodoPage.Validator`, `WelcomePage.Validator`) instead of `@FindBy` fields +
  self-validation. Native Playwright selector strings (`"text=Run application"`) are used
  inside page objects; Citrus `LocatorSpec` is used in the DSL builders.
- **No `citrus-sample-playwright.iml`**: IntelliJ-generated file, no value in committing.
- **Locators improved over the Selenium blueprint**: review flagged the positional
  `ul.list-group > li.list-group-item:last-child` locator (mirrored from the Selenium
  sample's `(//li[@class='list-group-item'])[last()]`) as brittle — coupled to Bootstrap
  styling classes and asserting on list position instead of created data. Content assertions
  now use data-anchored text locators: `LocatorSpec.text("No todos found")` for the empty
  state and `LocatorSpec.text("${todoName}")` for the created entry (random per run, so
  the match is exact); the page-object validator uses Playwright-idiomatic
  `page.getByText(...)`. Stable semantic locators (`input[name=title]`,
  `textarea[name=description]`, `form button[type=submit]`, `h1`) kept as-is.
- **02/03 assumed a Selenium-symmetric API**; the real 5.1.0-SNAPSHOT API
  (`PlaywrightEndpoints.playwright().browser()`, `playwright().browser(b).start|open|fill|
  click|waitFor|verify|pageObject|stop`, `PlaywrightPage`/`PlaywrightPageValidator`) was
  confirmed from the local framework build and the code was written against it — no
  renames needed beyond the plan's placeholders.

## Verification
- Build: `./mvnw -pl sample-playwright -Dcitrus.version=5.1.0-SNAPSHOT verify
  -Dsystem.under.test.mode=embedded` — BUILD SUCCESS, `Tests run: 4, Failures: 0,
  Errors: 0, Skipped: 0` (TodoListIT × 2, TodoPagesIT × 2, embedded Jetty + headless Chromium)
- `python3 <skill-dir>/scripts/capspec.py validate` — 0 errors, 0 warnings
- `diff sample-selenium/pom.xml sample-playwright/pom.xml` — only expected substitutions
  (artifact/name, citrus 5.1.0 + note, playwright dep, exec plugin, new properties)
