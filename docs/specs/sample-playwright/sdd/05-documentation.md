# sample-playwright module (2026-10-01)

## Overview
New `sample-playwright` Maven module demonstrating Citrus UI testing of the todo-list
application with Microsoft Playwright. It mirrors `sample-selenium` (same journeys, same
page-object coverage, same Spring/TestNG/embedded-Jetty setup) so both browser
integrations can be compared side by side.

### Problem
The samples repo had no runnable Playwright example; users evaluating browser automation
had only the Selenium sample and no mapping from Selenium idioms to the new
`citrus-playwright` endpoint (Citrus 5.1.0+).

### Solution
Mirror-for-mirror port of `sample-selenium` onto the real `citrus-playwright` Java DSL
(`PlaywrightEndpoints`, `playwright().browser(b).start|open|fill|click|waitFor|verify|
pageObject|stop`), with two IT classes (raw-locator + page-object flows), two page
objects with nested validators, module + root READMEs, and root POM registration.

## Key Components
- `sample-playwright/.../EndpointConfig.java` — Chromium `PlaywrightBrowser` bean
  (headless, `defaultTimeout` 5000), `todoClient` HttpClient, after-suite browser stop,
  after-test 500 ms sleep
- `sample-playwright/.../TodoListIT.java` — index-page + add-entry journeys with
  CSS/`LocatorSpec` locators
- `sample-playwright/.../TodoPagesIT.java` — same journeys via
  `pageObject().type().validate()/.execute().arguments()`
- `sample-playwright/.../page/TodoPage.java`, `page/WelcomePage.java` — page objects with
  constructor-injected Playwright `Page` and nested `Validator` classes

## Files Created
1. `sample-playwright/pom.xml` — module build; `<citrus.version>5.1.0</citrus.version>`
   (new `citrus-playwright` module ships in 5.1.0), `exec-maven-plugin` Chromium install
2. `sample-playwright/README.md` — objectives, snippets, run instructions, 5.1.0 note
3. `sample-playwright/src/test/java/.../EndpointConfig.java` — endpoint wiring
4. `sample-playwright/src/test/java/.../TodoListIT.java` — raw-locator flows
5. `sample-playwright/src/test/java/.../TodoPagesIT.java` — page-object flows
6. `sample-playwright/src/test/java/.../page/TodoPage.java` — todo-list page + validator
7. `sample-playwright/src/test/java/.../page/WelcomePage.java` — welcome page + validator
8. `sample-playwright/src/test/resources/citrus-application.properties` — Spring config pointer
9. `sample-playwright/src/test/resources/log4j2.properties` — logging (both identical to selenium)
10. `docs/specs/sample-playwright/sdd/01–05-*.md` — SDD feature folder
11. `docs/specs/_capabilities/playwright-sample.md` — capability spec (Stage 6)

## Files Modified
1. `pom.xml` — registered `<module>sample-playwright</module>`
2. `README.md` — sample table row for `sample-playwright` (flags Citrus 5.1.0+)

## Technical Highlights
**Playwright page objects differ structurally from Selenium's**: `PageObjectAction`
constructor-injects the Playwright `Page` and instantiates validators separately via
no-arg constructor, so validators are nested `Validator` classes rather than `@FindBy`
fields with self-validation. **No per-action timeouts**: Playwright auto-waits under the
endpoint `defaultTimeout`. **Browser binaries self-install** at `generate-test-resources`;
engine selectable with `-Dplaywright.browser=firefox|webkit`.
**5.1.0 prerequisite is load-bearing, not boilerplate**: verified 2026-10-01 that
`citrus-playwright` resolves nowhere (Central 404, latest release 5.0.2); the committed
POM pins 5.1.0 with a maintainer note, and green verification used the local
`5.1.0-SNAPSHOT` framework build.

## Verification
- Build: `./mvnw -pl sample-playwright -Dcitrus.version=5.1.0-SNAPSHOT verify
  -Dsystem.under.test.mode=embedded` — BUILD SUCCESS; Tests run: 4, Failures: 0,
  Errors: 0 (TodoListIT.testIndexPage/testAddEntry, TodoPagesIT.testIndexPage/testAddEntry,
  embedded Jetty + headless Chromium, incl. automatic browser install)
- `capspec.py validate` — 0 errors, 0 warnings
- Manual: diffed `sample-playwright/` against `sample-selenium/` — only expected
  Playwright-for-Selenium substitutions plus version note and browser-install plugin
- Not yet possible: plain `mvn verify` (needs released Citrus 5.1.0) — maintainer action recorded in 04-implementation.md Blockers

## How to Use
1. `cd sample-playwright`
2. `mvn clean verify -Dsystem.under.test.mode=embedded` (requires Citrus 5.1.0+; pre-release:
   `mvn verify -Dcitrus.version=5.1.0-SNAPSHOT` against a local framework build)
3. Single test: `mvn verify -Dit.test=TodoPagesIT`
4. Other engine: add `-Dplaywright.browser=firefox`

## Known Limitations
- Committed POM resolves only once Citrus 5.1.0 is released (see Blockers above).
- Headless Chromium is the default; headed runs need an EndpointConfig tweak
  (`headless(false)`), documented in code.
- JUnit5/Cucumber variants out of scope; TestNG matches sibling samples.

## Future Enhancements
- Re-verify with plain `mvn verify` after the 5.1.0 release; drop the SNAPSHOT workaround notes if desired.
- Optional headed-mode profile for demo purposes.

## Capability Specs
- `docs/specs/_capabilities/playwright-sample.md` — +6 added (PW-001…PW-006), archived 2026-10-01
