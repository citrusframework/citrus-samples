# Architecture: sample-playwright module

## Research: Existing Patterns
- `sample-selenium/pom.xml` — module POM pattern: `citrus-sample-selenium` artifact, Java 17,
  surefire+failsafe (TestNG provider), spring-boot-maven-plugin with
  `TodoApplication` main class, `citrus-sample-todo` dependency, test-scoped
  `citrus-base/spring/endpoint-catalog/testng/http/validation-xml` + `citrus-selenium`,
  all pinned to `${citrus.version}` (currently 5.0.0). Reuse verbatim, swapping the
  browser artifact and bumping the version property.
- `sample-selenium/src/test/java/.../EndpointConfig.java` — Spring Java config pattern:
  `@Import(TodoAppAutoConfiguration.class)`, browser bean via
  `CitrusEndpoints.selenium().browser().type(...)`, `HttpClient` bean for the todo REST
  API, `SequenceAfterSuite` browser stop + `SequenceAfterTest` sleep. Reuse with the
  Playwright endpoint factory.
- `sample-selenium` IT + page classes (`TodoListIT`, `TodoPagesIT`, `page/TodoPage`,
  `page/WelcomePage`) + `citrus-application.properties`
  (`citrus.spring.java.config=...EndpointConfig`) — the two-flow structure (raw locators
  + page objects) to mirror.
- Repo convention: root `pom.xml` `<modules>` lists every sample; root `README.md` table
  links each sample; no `docs/specs` convention existed before this feature (default SDD
  layout adopted).
- Capability spec touched: `docs/specs/_capabilities/playwright-sample.md` (new, empty
  baseline; PW-001…PW-006 added by this feature).

## Approach 1: Mirror-for-mirror port of sample-selenium (same shape, Playwright endpoint)
**Description**: Copy the `sample-selenium` tree to `sample-playwright` and apply only the
expected substitutions: `citrus-selenium` → `citrus-playwright`, Selenium browser bean →
Playwright browser bean (Chromium, headless-capable), Selenium DSL calls →
Playwright DSL equivalents, `Browser.HTMLUNIT` default → Chromium launch options, plus
`<citrus.version>5.1.0</citrus.version>` and the 5.1.0 prerequisite note in POM/README.
Class names, package, test flow order, and locators stay identical so the two samples diff
cleanly.
**Changes**:
- **Modules / layers**: new top-level Maven module `sample-playwright`; root POM + root
  README registration; no changes to `todo-app` or other samples.
- **Key components**: `EndpointConfig` (Playwright browser + `todoClient` + lifecycle
  hooks), `TodoListIT` (raw-locator journeys), `TodoPagesIT` (page-object journeys),
  `page/TodoPage`, `page/WelcomePage`, `citrus-application.properties`,
  `log4j2.properties`, module `pom.xml`, module `README.md`.
- **Data / storage**: none (tests use the live todo-list app; cleanup via REST DELETE).
**Pros**: comparable with Selenium sample (explicit success metric); smallest design risk;
reviewers can verify by diffing the two trees; matches repo conventions exactly.
**Cons**: inherits any Selenium-sample warts; exact Playwright DSL method names unconfirmed
until Citrus 5.1.0 ships (small rename risk).
**Complexity**: Medium

## Approach 2: Minimal single-flow sample (one IT class, no page objects)
**Description**: New module with only a POM, one endpoint config, and a single `TodoIT`
covering the add-entry journey with raw locators; no page-object flow, no welcome-page
journey, shorter README.
**Changes**:
- **Modules / layers**: same new-module + registration wiring as Approach 1, fewer files.
- **Key components**: `EndpointConfig`, one `*IT`, module POM + README.
- **Data / storage**: none.
**Pros**: less code; faster to write; fewer API unknowns (smaller Playwright DSL surface).
**Cons**: fails PW-003 and the "mirrors sample-selenium" success metric; a second
page-object follow-up would re-touch the same files; less useful as learning material.
**Complexity**: Low

## Approach 3: JUnit5 fresh design (new structure, not a port)
**Description**: Build the Playwright sample on `citrus-junit5` with `@CitrusSupport` and
JUnit5-style runner injection (per Citrus main-branch AGENTS.md guidance for new tests),
restructuring flows rather than copying the TestNG layout.
**Changes**:
- **Modules / layers**: new module + registration, as above.
- **Key components**: JUnit5 test classes with `@CitrusResource TestActionRunner`,
  restructured page classes, new POM test runtime.
- **Data / storage**: none.
**Pros**: aligns with where the framework is heading for new tests.
**Cons**: breaks comparability with `sample-selenium` (TestNG); every sibling todo-list
sample in this repo uses TestNG, so it violates local consistency for zero user benefit;
largest review and maintenance surface.
**Complexity**: Medium

## Recommendation: Approach 1
**Justification**: The spec's core demand is comparability ("mirror the Selenium sample"
is both a story and a success metric), and the repo's local convention is TestNG +
Spring Java config for every sibling sample. Approach 1 is the only option satisfying
both, keeps the diff reviewable, and covers all six delta requirements. Approach 2
under-delivers; Approach 3 optimizes for a different repo's conventions.
**Risks**:
- `citrus-playwright` Java API (endpoint factory, browser bean type, DSL action names)
  unconfirmed until Citrus 5.1.0 ships → Mitigation: assume a Selenium-symmetric API
  (`CitrusEndpoints.playwright().browser()...`, `playwright()` action builder), isolate
  all endpoint-specific code in `EndpointConfig` + the two IT classes, and record exact
  API adjustments as implementation deviations to fix before merge.
- Citrus 5.1.0 may not be on Maven Central when this is first built → Mitigation: keep
  `<citrus.version>5.1.0</citrus.version>` in the module POM with the prerequisite note;
  verify with `mvn -pl sample-playwright validate` structure checks that don't need the
  artifact, then run the full suite once 5.1.0 resolves.

## Contracts
- **Dependencies**: `org.citrusframework:citrus-playwright:5.1.0` (test scope, replaces
  `citrus-selenium`); `com.microsoft.playwright:playwright` transitively via the Citrus
  module (no direct version pin in the sample); everything else identical to
  `sample-selenium` (reason: minimal, reviewable port).
- **Configuration**: `src/test/resources/citrus-application.properties` key
  `citrus.spring.java.config=com.consol.citrus.samples.todolist.EndpointConfig`
  (unchanged path); new module property `<citrus.version>5.1.0</citrus.version>`
  (differs from repo-wide 5.0.0 — intentional, documented prerequisite).
- **Internal interfaces**: Spring beans `browser` (Playwright browser), `todoClient`
  (`HttpClient` @ `http://localhost:8080`), `AfterSuite` browser stop,
  `AfterTest` 500 ms sleep; page classes expose `TodoPage.submit(title, description)` and
  `WelcomePage.startApp()` with `validate(...)` assertions ("TODO list" heading, last-entry
  text, enabled "Run application" link).
- **External interfaces**: todo-list app routes `/` (welcome) and `/todolist` (list UI);
  REST `DELETE /api/todolist` for test setup, expecting `200 OK`. No new endpoints.
- **UI**: same user-visible journeys as the Selenium sample (index → run-app → todo list;
  add entry via form). No SUT UI changes.
- **Docs/run contract**: `mvn clean verify -Dsystem.under.test.mode=embedded` (full run,
  embedded Jetty); `mvn verify` against a standalone app; `mvn verify -Dit.test=<name>`
  for a single test — all documented in the module README with the 5.1.0 note.

## Cross-capability dependencies
- `todo-app` system under test (no capability spec in this repo; consumed as-is via
  `citrus-sample-todo` dependency and embedded Jetty) — no changes.
- Future `selenium-sample` capability, if baselined later, should cross-link here as the
  sibling UI sample; this feature does not create or modify it.
