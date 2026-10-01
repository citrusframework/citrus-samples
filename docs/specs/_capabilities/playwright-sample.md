---
kind: capability
id-prefix: PW
coverage: partial
---
# Playwright UI Sample

This capability covers the `sample-playwright` module: a runnable Citrus sample that
demonstrates UI testing of the todo-list application with Microsoft Playwright via the
`citrus-playwright` endpoint, mirroring the existing `sample-selenium` module. Boundary:
sample wiring, tests, and docs only — the `citrus-playwright` endpoint implementation
itself lives in the Citrus framework repository (v5.1.0+), not here.

## Requirements

### PW-001: Playwright UI sample module exists
Source: sample-playwright

The system SHALL provide a `sample-playwright` Maven module that demonstrates Playwright
UI testing of the todo-list application and mirrors the structure of the `sample-selenium`
module (endpoint configuration, integration tests, page objects, README, root POM entry).

#### Scenario: module layout mirrors selenium sample
- **GIVEN** a checkout of the repository
- **WHEN** a reader lists `sample-playwright/` and `sample-selenium/`
- **THEN** both contain a `pom.xml`, a `README.md`, an endpoint configuration, two
  `*IT` test classes, two page classes, and `citrus-application.properties` plus
  `log4j2.properties` test resources

### PW-002: Sample drives the todo-list UI with Playwright
Source: sample-playwright

The system SHALL start a Playwright browser, navigate to the todo-list application, and
perform the same user journeys as the Selenium sample: open the index page and follow the
"Run application" link, and clear the list via the REST API then add a todo entry through
the browser form and assert it appears in the list.

#### Scenario: index page journey
- **GIVEN** the todo-list application is running (embedded or standalone)
- **WHEN** the index-page test runs
- **THEN** the browser opens the app root, the "Run application" entry point is enabled
  and visible, and activating it shows the "TODO list" page

#### Scenario: add-entry journey
- **GIVEN** an empty todo list (cleared via `DELETE /api/todolist`)
- **WHEN** the add-entry test fills the title/description form in the browser and submits it
- **THEN** the new entry becomes visible in the list and its title matches the entered value

### PW-003: Sample supports the page-object style
Source: sample-playwright

The system SHALL provide page objects for the welcome page and the todo-list page with
validate and action operations (start the app; submit a new entry), and at least one test
flow SHALL exercise them instead of raw locators.

#### Scenario: page-object flow
- **GIVEN** the todo-list application is running
- **WHEN** the page-object test runs
- **THEN** the welcome page validates, the start-app page action navigates to the todo
  page, the todo page validates, and submitting via the page action creates the entry

### PW-004: Browser lifecycle is managed by the sample
Source: sample-playwright

The system SHALL stop the Playwright browser after the test suite and pause briefly after
each test so the browser activity remains observable, matching the Selenium sample's
after-suite/after-test behavior.

#### Scenario: suite teardown
- **GIVEN** a finished test suite run
- **WHEN** the after-suite hook executes
- **THEN** the Playwright browser is stopped and no browser process is left running

### PW-005: Sample declares the Citrus 5.1.0 prerequisite
Source: sample-playwright

The system SHALL set `<citrus.version>` to 5.1.0 in `sample-playwright/pom.xml`, depend on
the `org.citrusframework:citrus-playwright` Maven module, and document that the sample
requires Citrus 5.1.0+ because earlier releases do not ship the `citrus-playwright` module.

#### Scenario: maintainer reads the prerequisite
- **GIVEN** the `sample-playwright` README and POM
- **WHEN** a maintainer checks the required Citrus version
- **THEN** both state 5.1.0 as the minimum and name `citrus-playwright` as the reason

### PW-006: Sample is registered in the build and docs
Source: sample-playwright

The system SHALL register `sample-playwright` as a `<module>` in the root `pom.xml` and
list it in the root `README.md` sample table next to `sample-selenium`.

#### Scenario: reactor build includes the sample
- **GIVEN** the root POM
- **WHEN** a reader inspects its `<modules>` section
- **THEN** `sample-playwright` is listed

#### Scenario: sample table links the sample
- **GIVEN** the root README sample table
- **WHEN** a reader looks for the Playwright sample row
- **THEN** a `sample-playwright` entry with a short description is present
