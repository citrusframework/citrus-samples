# 00 — Brainstorming: citrus-samples folder structure

Status: **draft for maintainer discussion** (pre-Stage 1). Decisions marked **D1–D4** at the end
must be settled before `01-specification.md` is written.

## 1. Feature understanding

### Purpose
Give Citrus users a sample set that is easy to navigate, maps to the framework they are learning,
shows every supported DSL where it matters, and stays green against the current Citrus version with
little maintenance.

### Current state (surveyed on `main` @ 860f1dd7)

| Fact | Evidence |
|---|---|
| ~60 leaf samples, two naming schemes at the top level: 20 flat `sample-*` folders and 11 grouping `samples-*` folders, plus `demo/` and `todo-app/` | `ls`, root `pom.xml` modules |
| Grouping is inconsistent: `samples-db` holds `sample-sql` (a *connector* in core), `samples-http` holds `sample-swagger` (OpenAPI connector), `sample-hamcrest`/`sample-binary` are flat although they are validation modules | folder listing vs. core `connectors/`, `validation/` |
| **Practically one DSL**: every test is Java DSL; one Groovy script (`sample-groovy`); zero XML or YAML test cases | `grep` for XML testcase schema / `*.yaml` tests |
| **Practically one runtime**: TestNG + Spring, almost all against the shared Spring Boot `todo-app` SUT started by `spring-boot-maven-plugin` | `sample-jms/pom.xml`, `samples-http/sample-http/pom.xml` |
| Quarkus, JUnit 5, Cucumber, JBang, Testcontainers, Playwright, Camel are under-represented or absent although core ships them (`runtime/citrus-quarkus`, `connectors/citrus-jbang-connector`, `connectors/citrus-testcontainers`, `connectors/citrus-playwright`, `tools/jbang`) | core repo layout |
| `sample-gradle` is not in the Maven reactor (separate Gradle build) | root `pom.xml` |
| Java packages still `com.consol.citrus.samples.*` | `src/test/java/com/consol/...` |
| Version bumps are a recurring chore (dedicated `citrus-update-versions` skill exists; last commits are version-alignment) | git log |

### Reference structure: `citrusframework/citrus`
```
core/  catalog/  runtime/  endpoints/  connectors/  validation/  tools/  utils/
```
Release goes through `central-publishing-maven-plugin` in profile `release-central`, with
non-published modules listed in `<excludeArtifacts>` (e.g. `citrus-quarkus-it`, archetypes) and
`maven.deploy.skip` in `runtime/citrus-quarkus/citrus-quarkus-it/pom.xml`.

### Pain points
1. You cannot predict where a sample lives from the Citrus module you are using.
2. `sample-` / `samples-` mix and redundant prefixes (`samples-http/sample-http-basic-auth`).
3. The DSL story of Citrus 4/5 (Java, XML, YAML, Groovy) is invisible.
4. Runtime story (JUnit 5, Quarkus, JBang, Cucumber) is invisible.
5. Samples drift from core; breaks are found after a release, not on the PR that caused them.

### Constraints
- Must stay buildable with `./mvnw verify` from one root (CI).
- Samples must never be published to Maven Central.
- Users copy a single sample folder as a starter — each leaf must remain understandable alone.
- Migration should be mechanical (`git mv`) so history is preserved.

### Non-goals (v1)
- Rewriting the system under test (`todo-app`).
- Writing new samples for every core module — only placing existing ones and leaving room.
- Changing the Citrus framework itself.

---

## 2. Axis question: what is the top-level folder?

### Option A — Platform first (`spring/`, `quarkus/`, `camel/`, `jbang/`, `plain/`)
```
spring/http  spring/jms  spring/soap ...   ← ~80 % of samples
quarkus/food-market
camel/camel-context
jbang/hello-world
```
- ➕ Matches how a user *starts* ("I'm on Quarkus").
- ➖ Wildly unbalanced: almost everything lands in `spring/` because the SUT is Spring Boot, not
  because the *test* is Spring-specific.
- ➖ Platform is orthogonal to the endpoint: an HTTP sample would need duplicating per platform.
- ➖ Doesn't match core, docs or artifact names.

### Option B — Mirror the core repo (recommended)
Top-level folders = core module groups; leaf = core module (or a scenario of it).
- ➕ Zero-thought lookup: using `citrus-kafka` (core `endpoints/citrus-kafka`) → `endpoints/kafka`.
- ➕ Gaps become visible: a core module without a sample folder is an obvious TODO.
- ➕ Same mental model whether samples live here or inside the core repo (see §4).
- ➖ Runtime/platform is a secondary axis — solved by a `runtimes/` group (Spring Boot, Quarkus,
  JUnit 5, TestNG, Cucumber, Main) and a separate `jbang/` group (not a Maven build).

### Option C — Learning path (`getting-started/`, `intermediate/`, `advanced/`, `showcases/`)
- ➕ Friendly for newcomers.
- ➖ Level is subjective and changes; poor for maintainers; doesn't scale.
- Better delivered as a **curated index in the README** on top of B.

### Option D — Flat + metadata
`samples/<name>/` plus `sample.yaml` (tags: area, module, dsl, runtime) and a generated README
catalogue.
- ➕ Cheapest move; tags give every axis at once.
- ➖ 60+ siblings in one folder; tooling needed to be usable.
- Worth keeping the **metadata idea** as a complement to B (generated coverage matrix).

---

## 3. Proposed tree (Option B)

Folder names drop the `sample-` prefix (the path already says it). Maven `artifactId` keeps
uniqueness: `citrus-sample-<group>-<name>`; Java package `org.citrusframework.samples.<group>.<name>`.

```
citrus-samples/
├── pom.xml                         # aggregator, deploy disabled
├── README.md                       # index, learning path, DSL × sample matrix
├── apps/                           # systems under test — not tests
│   └── todo-app/                   # ← todo-app
├── getting-started/                # one scenario, every DSL, minimal deps (new)
├── core/                           # framework concepts (core/citrus-base, citrus-spring)
│   ├── annotation-config/          # ← sample-annotation-config
│   ├── java-config/                # ← sample-javaconfig
│   ├── behaviors/                  # ← sample-behaviors
│   ├── data-dictionaries/          # ← sample-dictionaries
│   ├── message-store/              # ← sample-message-store
│   ├── dynamic-endpoints/          # ← sample-dynamic-endpoints
│   └── reporting/                  # ← sample-reporting
├── runtimes/                       # mirrors core runtime/
│   ├── testng/                     # ← samples-testng/sample-testng
│   ├── testng-dataprovider/        # ← samples-testng/sample-dataprovider
│   ├── junit4/                     # ← samples-junit/sample-junit
│   ├── junit-jupiter/              # ← samples-junit/sample-junit5
│   ├── cucumber/                   # ← samples-cucumber/sample-cucumber
│   ├── cucumber-spring/            # ← samples-cucumber/sample-cucumber-spring
│   └── quarkus/                    # ← demo/sample-quarkus
├── endpoints/                      # mirrors core endpoints/
│   ├── http/
│   │   ├── basic/                  # ← samples-http/sample-http
│   │   ├── basic-auth/  form-data/  query-param/  static-response/  https/  load-test/
│   ├── ws/                         # core name for SOAP (citrus-ws)
│   │   ├── basic/  attachment/  mtom/  ssl/  static-response/  ws-addressing/  ws-security/  wsdl/
│   ├── jms/  kafka/  mail/  rmi/  camel/   # ← sample-jms, …, sample-camel-context
│   ├── ftp/   { ftp/  scp/  sftp/ }
│   ├── jdbc/  { basic/  callable-statements/  transactions/ }
│   └── websocket/ { client/  server/ }
├── connectors/                     # mirrors core connectors/
│   ├── docker/  kubernetes/  selenium/  playwright/
│   ├── openapi/                    # ← samples-http/sample-swagger
│   ├── sql/                        # ← samples-db/sample-sql
│   └── testcontainers/             # (new, placeholder)
├── validation/                     # mirrors core validation/
│   ├── xml/   { xml/  xhtml/  oxm/ }
│   ├── json/  { json/  databind/ }
│   ├── hamcrest/                   # ← sample-hamcrest
│   └── binary/                     # ← sample-binary
├── jbang/                          # no Maven: *.citrus.yaml / .xml / .groovy run with `citrus run`
├── build-tools/
│   └── gradle/                     # ← sample-gradle (outside the Maven reactor)
└── showcases/                      # multi-module end-to-end demos (← demo/*)
    ├── bakery/  bookstore/  flight-booking/  greeting/  incident/  jakarta-ee/
```

Every intermediate folder has a `pom.xml` (packaging `pom`) so `./mvnw verify -pl endpoints -amd`
builds just one area in CI.

---

## 4. DSL question: one sample per DSL, or many DSLs per sample?

### DSL-1 — Separate sample per DSL (`http-java/`, `http-xml/`, `http-yaml/`)
- ➕ Each folder is a pure copy-paste starter for that DSL.
- ➖ ×4 folders, ×4 poms, ×4 SUT start-ups, ×4 maintenance for every version bump. Rejected.

### DSL-2 — Top-level DSL split (`java/…`, `yaml/…`, `xml/…`)
- ➖ Same duplication as DSL-1, plus it competes with the module axis. Rejected.

### DSL-3 — Multi-DSL inside one sample (recommended)
Same scenario, same SUT, side by side; Java runs the others via `@CitrusTestSource`:
```
endpoints/http/basic/
├── pom.xml
├── README.md                      # shows the scenario in each DSL (tabs)
└── src/test/
    ├── java/…/TodoListIT.java      # Java DSL  + runner for the other sources
    └── resources/…/
        ├── todo-list.citrus.xml    # XML DSL
        ├── todo-list.citrus.yaml   # YAML DSL  (also runnable via JBang)
        └── todo-list.citrus.groovy # Groovy DSL
```
- ➕ One build, one SUT start, all DSLs proven equivalent on every CI run.
- ➕ Readers compare DSLs line by line — this is what users actually ask for.
- ➕ The YAML/XML files are the same ones a JBang user would run.
- ➖ Folder is no longer a "pure" single-DSL starter (mitigated: the extra files are self-contained;
  delete the ones you don't need).

**Rule of thumb**: Java DSL is mandatory everywhere; XML/YAML/Groovy are **required** in
`getting-started/` and `endpoints/*/basic`, **optional** elsewhere. A coverage matrix (generated
from file presence, optionally `sample.yaml` tags — Option D) in the root README shows what exists.

`sample-groovy` disappears as a standalone folder — its script becomes the Groovy variant of
`endpoints/http/basic`.

---

## 5. Location question: separate repo or inside `citrusframework/citrus`?

### L1 — Stay in `citrus-samples`, restructure only
- ➕ No impact on core build time or release; contributors get a small repo.
- ➖ Version drift continues; breaking changes in core are found after release.

### L2 — Move into core as `samples/`, excluded from publishing (recommended to evaluate)
```
citrus/
├── core/ runtime/ endpoints/ connectors/ validation/ tools/ …
└── samples/            # same tree as §3, parent = citrus root, version = ${project.version}
```
How it stays off Maven Central (three layers, belt and braces):
1. **Not in default modules** — added only by profile `samples` (`<module>samples</module>`), and
   CI runs `./mvnw verify -Psamples`. `release-central` never activates it, so the release reactor
   never even sees the samples. This is the important one.
2. `samples/pom.xml` sets `<maven.deploy.skip>true</maven.deploy.skip>` (install stays enabled,
   because samples depend on `apps/todo-app`).
3. If they ever enter the release reactor, a wildcard-free `excludeArtifacts` would need every
   sample listed — fragile, hence layer 1.

- ➕ Samples compile against the SNAPSHOT on every core PR → breakage caught where it is caused.
- ➕ No more version-bump PRs in a second repo.
- ➕ Reference docs (`src/manual`) can `include::` sample code directly — docs and samples cannot drift.
- ➕ Folder model of §3 is literally the core model, one level down.
- ➖ Core CI time grows (mitigate: separate GitHub job/workflow, `-Psamples` only on PRs touching
  `samples/**` or nightly; or matrix by area).
- ➖ A copied sample has `<version>${project.version}</version>` from the parent — samples' poms
  must declare explicit Citrus dependency version via the BOM so copy-out works
  (`citrus-bom` + one `citrus.version` property per sample README).
- ➖ Repo history: `git subtree add`/filter-repo needed to keep sample history.

### L3 — Hybrid: restructure here now (L1), then migrate the restructured tree into core (L2)
- ➕ Each step is reviewable alone; the structure debate isn't blocked on the location debate.
- ➕ If the maintainer later prefers L2, the tree moves unchanged (`git subtree`), then
  `citrus-samples` is archived with a README pointer.
- **Recommended path.**

---

## 6. Housekeeping that comes with the move
- Rename packages `com.consol.citrus.samples` → `org.citrusframework.samples`.
- Unify artifactIds: `citrus-sample-<group>-<name>`.
- Each leaf `README.md`: what it shows, Citrus modules used, DSLs available, how to run.
- CI matrix by top-level group for faster feedback.

---

## 7. Decisions for the maintainer

| ID | Question | Options | Proposal |
|---|---|---|---|
| **D1** | Top-level axis | A platform · **B mirror core** · C learning path · D flat+tags | **B**, with C as README index and D's metadata for the matrix |
| **D2** | DSL organisation | DSL-1 per-DSL samples · DSL-2 DSL top folders · **DSL-3 multi-DSL per sample** | **DSL-3**, Java mandatory, others required in `getting-started` + `endpoints/*/basic` |
| **D3** | Repository | L1 stay · L2 move to core · **L3 restructure then move** | **L3** |
| **D4** | Publishing guard (if L2) | profile-only · deploy-skip · excludeArtifacts | **profile `samples`** + `maven.deploy.skip` |

Once D1–D4 are agreed, Stage 1 (`01-specification.md`) captures the old→new mapping as acceptance
criteria and Stage 2 details the migration mechanics (git mv script, pom generation, CI).
