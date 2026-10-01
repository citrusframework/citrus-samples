# Tasks: sample-playwright module

## Dependency Graph
T1 → T2 → T3 → T4 → T5 → T6 → T7
T1 → T5 (POM reference needed for registration)

## Task List

### T1: Module POM + root build registration
- **Complexity**: Low
- **Files**: `sample-playwright/pom.xml` (create), `pom.xml` (modify)
- **Depends on**: none
- **Covers**: AC3, PW-001, PW-005, PW-006
- **Steps**:
  1. Copy `sample-selenium/pom.xml` to `sample-playwright/pom.xml`, rename
     artifactId to `citrus-sample-playwright`, name to `Citrus Samples:: Playwright`.
  2. Set `<citrus.version>5.1.0</citrus.version>` with an XML comment noting the
     `citrus-playwright` module ships only in 5.1.0+.
  3. Replace the `citrus-selenium` dependency with `citrus-playwright` (same scope/version
     property).
  4. Add `<module>sample-playwright</module>` to the root `pom.xml` after
     `sample-selenium`.
- **Verification**:
  - [ ] `mvn -pl sample-playwright validate` parses the reactor (fails only on missing
        5.1.0 artifacts, not on POM syntax)
  - [ ] `diff sample-selenium/pom.xml sample-playwright/pom.xml` shows only the expected
        substitutions

### T2: Endpoint configuration (browser + HTTP client + lifecycle)
- **Complexity**: Medium
- **Files**: `sample-playwright/src/test/java/com/consol/citrus/samples/todolist/EndpointConfig.java` (create)
- **Depends on**: T1
- **Covers**: AC2, PW-002, PW-004
- **Steps**:
  1. Port `sample-selenium/.../EndpointConfig.java`, swapping the Selenium browser bean
     for the Playwright browser bean (Chromium) via the `citrus-playwright` endpoint
     factory.
  2. Keep `todoClient` HttpClient, `SequenceAfterSuite` browser stop, and
     `SequenceAfterTest` 500 ms sleep unchanged.
  3. Adjust imports to the `citrus-playwright` packages once the 5.1.0 API is confirmed;
     record any deviation from the assumed Selenium-symmetric names.
- **Verification**:
  - [ ] `mvn -pl sample-playwright compile` (full: test-compile once 5.1.0 resolves)
  - [ ] Bean names `browser`, `todoClient`, after-suite/test hooks present by inspection

### T3: Raw-locator test flow (TodoListIT)
- **Complexity**: Medium
- **Files**: `sample-playwright/src/test/java/com/consol/citrus/samples/todolist/TodoListIT.java` (create)
- **Depends on**: T2
- **Covers**: AC2, PW-002
- **Steps**:
  1. Port `TodoListIT` (`testIndexPage`, `testAddEntry`) replacing `selenium()` action
     builders with the Playwright equivalents, keeping flow order, locators, and
     assertions identical.
  2. Keep the REST `DELETE /api/todolist` setup and `HttpStatus.OK` expectation.
- **Verification**:
  - [ ] `mvn -pl sample-playwright verify -Dit.test=TodoListIT -Dsystem.under.test.mode=embedded`
        (green once 5.1.0 resolves; before that, structural diff review vs selenium)

### T4: Page objects + page-object test flow (TodoPagesIT)
- **Complexity**: Medium
- **Files**: `sample-playwright/src/test/java/com/consol/citrus/samples/todolist/page/TodoPage.java` (create), `.../page/WelcomePage.java` (create), `.../TodoPagesIT.java` (create)
- **Depends on**: T3
- **Covers**: AC2, PW-003
- **Steps**:
  1. Port `TodoPage`/`WelcomePage` (elements, `submit`, `startApp`, `validate`) to the
     Playwright browser/page types.
  2. Port `TodoPagesIT` (`testIndexPage`, `testAddEntry`) to the Playwright page-action
     style, keeping arguments and wait/verify steps.
- **Verification**:
  - [ ] `mvn -pl sample-playwright verify -Dit.test=TodoPagesIT -Dsystem.under.test.mode=embedded`
  - [ ] Both page classes expose the same public operations as their Selenium twins

### T5: Test resources (Spring config pointer + logging)
- **Complexity**: Low
- **Files**: `sample-playwright/src/test/resources/citrus-application.properties` (create), `sample-playwright/src/test/resources/log4j2.properties` (create)
- **Depends on**: T2
- **Covers**: AC2, PW-001
- **Steps**:
  1. Copy both resource files from `sample-selenium` unchanged (same
     `citrus.spring.java.config` value — package is identical by design).
- **Verification**:
  - [ ] `diff -r sample-selenium/src/test/resources sample-playwright/src/test/resources`
        is empty
  - [ ] Spring context loads the Playwright `EndpointConfig` in a test run

### T6: Module README + root README table entry
- **Complexity**: Low
- **Files**: `sample-playwright/README.md` (create), `README.md` (modify)
- **Depends on**: T4
- **Covers**: AC4, AC5, PW-005, PW-006
- **Steps**:
  1. Port `sample-selenium/README.md`, swapping snippets to the Playwright config/test
     calls and adding a prominent note: requires Citrus 5.1.0+ for `citrus-playwright`.
  2. Add the `sample-playwright` row to the root README sample table next to
     `sample-selenium`.
- **Verification**:
  - [ ] README contains "5.1.0" and "citrus-playwright" prerequisite note
  - [ ] Root table links `sample-playwright` correctly

### T7: Full verification + SDD docs (Stage 5/6 handover)
- **Complexity**: Low
- **Files**: `docs/specs/sample-playwright/sdd/04-implementation.md` (create), `docs/specs/sample-playwright/sdd/05-documentation.md` (create)
- **Depends on**: T6
- **Covers**: AC1, AC6
- **Steps**:
  1. Run the full module build `mvn -pl sample-playwright clean verify
     -Dsystem.under.test.mode=embedded` (or record the 5.1.0 blocker precisely).
  2. Run `capspec.py validate` on the spec + capability files.
  3. Write `04-implementation.md` progress notes (including any API deviations) and
     `05-documentation.md`, then archive the delta (Stage 6) before merge.
- **Verification**:
  - [ ] `mvn -pl sample-playwright clean verify -Dsystem.under.test.mode=embedded` green
        (or blocker documented with the exact missing-artifact error)
  - [ ] `python3 <skill-dir>/scripts/capspec.py validate` reports 0 errors

## Execution Strategy
- **Phase 1: Foundation** — T1 (POM + registration), T2 (endpoint config), T5 (resources)
- **Phase 2: Integration** — T3 (raw-locator flow), T4 (page objects + flow)
- **Phase 3: Documentation** — T6 (READMEs), T7 (verification + Stage 5 docs, then Stage 6 archive)

## Rollback Plan
- After Phase 1: delete `sample-playwright/` and revert the root POM `<module>` line.
- After Phase 2: same as Phase 1 (no other modules touched; `todo-app` untouched).
- After Phase 3: additionally revert both README edits.
