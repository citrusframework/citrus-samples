# Feature: sample-playwright module

## Overview
Add a new `sample-playwright` Maven module that demonstrates UI testing of the todo-list
application with Microsoft Playwright through Citrus, following the existing
`sample-selenium` pattern (endpoint config, IT tests, page objects, README, root POM entry).

## User Stories
**As a** Citrus user evaluating browser automation
**I want to** run a Playwright-based sample against the todo-list app
**So that** I can learn the Playwright endpoint without reading framework source code.

**As a** maintainer of citrus-samples
**I want to** have the Playwright sample mirror the Selenium sample structure
**So that** both samples stay comparable and reviewable side by side.

**As a** maintainer releasing this sample
**I want to** have an explicit record that the sample requires Citrus 5.1.0+
**So that** nobody tries to build it against an older Citrus release that lacks the
`citrus-playwright` Maven module.

## Acceptance Criteria
- [ ] AC1: `sample-playwright/` module builds with `mvn clean verify
      -Dsystem.under.test.mode=embedded` (or documents why it cannot yet, if blocked on
      the 5.1.0 release)
- [ ] AC2: Sample contains an endpoint config (Playwright browser + HTTP client +
      after-suite/after-test lifecycle), two IT classes (raw-locator flow and page-object
      flow), and two page classes, mirroring `sample-selenium`
- [ ] AC3: Sample declares `org.citrusframework:citrus-playwright` (version 5.1.0) and the
      root `pom.xml` lists the `sample-playwright` module
- [ ] AC4: `sample-playwright/README.md` documents objectives, key snippets, and run
      instructions, including the 5.1.0 prerequisite note
- [ ] AC5: Root `README.md` sample table links the new Playwright sample
- [ ] AC6: `capspec.py validate` passes on the spec and capability files

## Constraints
- **Technical**: mirror `sample-selenium` structure (TestNG + Spring Java config,
  `citrus-application.properties` pointing at `EndpointConfig`, `todo-app` as system
  under test, failsafe/surefire setup); Java 17; no changes to shared `todo-app` code
- **Performance**: no measurable target — sample only; embedded Jetty startup dominates
- **Security**: no secrets; tests run against local/embedded todo-list only
- **Compatibility**: `citrus-playwright` exists only in Citrus 5.1.0+ — the sample's
  `<citrus.version>` MUST be 5.1.0 (not the repo-wide 5.0.0); maintainer MUST bump the
  version once 5.1.0 is released

## Out of Scope
- Changes to the `citrus-playwright` endpoint implementation itself (framework repo)
- JUnit5/Cucumber variants of the Playwright sample
- Migrating or modifying `sample-selenium`
- CI workflow changes beyond what sibling samples already use

## Success Metrics
- A new contributor can check out the repo, follow `sample-playwright/README.md`, and get
  green UI tests against the embedded todo-list app
- `sample-playwright` file tree diffs against `sample-selenium` show only the expected
  Playwright-for-Selenium substitutions plus the version note

## Capability delta: docs/specs/_capabilities/playwright-sample.md — archived 2026-10-01

### ADDED

#### PW-001: Playwright UI sample module exists
The system SHALL provide a `sample-playwright` Maven module that demonstrates Playwright
UI testing of the todo-list application and mirrors the structure of the `sample-selenium`
module (endpoint configuration, integration tests, page objects, README, root POM entry).

##### Scenario: module layout mirrors selenium sample
- **GIVEN** a checkout of the repository
- **WHEN** a reader lists `sample-playwright/` and `sample-selenium/`
- **THEN** both contain a `pom.xml`, a `README.md`, an endpoint configuration, two
  `*IT` test classes, two page classes, and `citrus-application.properties` plus
  `log4j2.properties` test resources

#### PW-002: Sample drives the todo-list UI with Playwright
The system SHALL start a Playwright browser, navigate to the todo-list application, and
perform the same user journeys as the Selenium sample: open the index page and follow the
"Run application" link, and clear the list via the REST API then add a todo entry through
the browser form and assert it appears in the list.

##### Scenario: index page journey
- **GIVEN** the todo-list application is running (embedded or standalone)
- **WHEN** the index-page test runs
- **THEN** the browser opens the app root, the "Run application" entry point is enabled
  and visible, and activating it shows the "TODO list" page

##### Scenario: add-entry journey
- **GIVEN** an empty todo list (cleared via `DELETE /api/todolist`)
- **WHEN** the add-entry test fills the title/description form in the browser and submits it
- **THEN** the new entry becomes visible in the list and its title matches the entered value

#### PW-003: Sample supports the page-object style
The system SHALL provide page objects for the welcome page and the todo-list page with
validate and action operations (start the app; submit a new entry), and at least one test
flow SHALL exercise them instead of raw locators.

##### Scenario: page-object flow
- **GIVEN** the todo-list application is running
- **WHEN** the page-object test runs
- **THEN** the welcome page validates, the start-app page action navigates to the todo
  page, the todo page validates, and submitting via the page action creates the entry

#### PW-004: Browser lifecycle is managed by the sample
The system SHALL stop the Playwright browser after the test suite and pause briefly after
each test so the browser activity remains observable, matching the Selenium sample's
after-suite/after-test behavior.

##### Scenario: suite teardown
- **GIVEN** a finished test suite run
- **WHEN** the after-suite hook executes
- **THEN** the Playwright browser is stopped and no browser process is left running

#### PW-005: Sample declares the Citrus 5.1.0 prerequisite
The system SHALL set `<citrus.version>` to 5.1.0 in `sample-playwright/pom.xml`, depend on
the `org.citrusframework:citrus-playwright` Maven module, and document that the sample
requires Citrus 5.1.0+ because earlier releases do not ship the `citrus-playwright` module.

##### Scenario: maintainer reads the prerequisite
- **GIVEN** the `sample-playwright` README and POM
- **WHEN** a maintainer checks the required Citrus version
- **THEN** both state 5.1.0 as the minimum and name `citrus-playwright` as the reason

#### PW-006: Sample is registered in the build and docs
The system SHALL register `sample-playwright` as a `<module>` in the root `pom.xml` and
list it in the root `README.md` sample table next to `sample-selenium`.

##### Scenario: reactor build includes the sample
- **GIVEN** the root POM
- **WHEN** a reader inspects its `<modules>` section
- **THEN** `sample-playwright` is listed

##### Scenario: sample table links the sample
- **GIVEN** the root README sample table
- **WHEN** a reader looks for the Playwright sample row
- **THEN** a `sample-playwright` entry with a short description is present
