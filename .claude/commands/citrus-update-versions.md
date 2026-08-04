# Update Citrus Versions

Update dependency and plugin versions in all Maven pom.xml files in this repository to align with a specific Citrus release.

## Arguments

The input `$ARGUMENTS` is the Citrus release version (e.g. `4.11.0`). It MUST be provided — if empty, ask the user for the version and stop.

## Instructions

1. **Fetch the Citrus release POM.** Use WebFetch to retrieve the raw POM from:
   ```
   https://raw.githubusercontent.com/citrusframework/citrus/refs/tags/v$ARGUMENTS/pom.xml
   ```
   Extract ALL `<properties>` entries that define version numbers (any property whose value looks like a version — digits, dots, optional qualifiers). Build a map of `propertyName → version` from the Citrus release POM. Ask WebFetch to return the full `<properties>` block as-is so you can parse every version property.
   If the user provides a local POM file path instead, read that file and skip the WebFetch.

2. **Read every `pom.xml` in this repository** (excluding `target/` directories). Use `find . -name "pom.xml" -not -path "*/target/*"` to locate them. Read each one.

3. **Compare versions.** For each version property found in the Citrus release POM, check whether the same property name (or a clearly equivalent one) exists in the local POM files. Classify each match into one of:
   - **Preserved** — the local version should not be changed due to known issues with the sample code. These will NOT be updated but will be reported. Currently, the libraries that are classified as preserved are: `jetty`
   - **Outdated** — the local version is older than the Citrus release version. These will be updated.
   - **Newer** — the local version is strictly newer than the Citrus release version. These will NOT be updated but will be reported.
   - **Same** — versions match. No action needed.

   Use semantic version comparison (split on `.` and compare segments numerically, then qualifiers like `-SNAPSHOT`, `-alpha`, `-beta`, `-RC` etc.).

4. **Resolve unmatched local version properties.** After comparing against the Citrus POM, identify all version properties in the local POMs that have NO equivalent in the Citrus POM (e.g. `quarkus-artemis.version`). For each unmatched property:
   - Determine which platform or framework version it must be compatible with (e.g. `quarkus-artemis.version` must match the Quarkus Platform version).
   - Use WebSearch to find the latest compatible version of that dependency for the platform version being used. For Quarkiverse extensions, search for the compatibility matrix on the Quarkiverse GitHub or Maven Central.
   - If the local version is outdated compared to the compatible version found, update it.
   - Report these as a separate "Resolved unmatched versions" section in the summary.

5. **Update outdated versions.** For each outdated property (from both step 3 and step 4), use the Edit tool to update the version value in every local `pom.xml` where the property is defined. Also update the `<citrus.version>` property to `$ARGUMENTS`.

6. **Report results.** Print a summary:
   - A table of all updated versions: property name, old version → new version.
   - A separate section listing any properties where this repository has a NEWER version than the Citrus release POM, showing: property name, local version, Citrus release version.
   - A separate section listing resolved unmatched versions with the compatibility rationale.

7. **Verify with a full Maven build.** Run `mvn verify -Dsystem.under.test.mode=embedded` from the repository root to verify that all version changes are compatible. This uses the embedded Jetty server to auto-start the system under test during the build. If the build fails:
   - Analyze the failure output to identify the root cause.
   - Use available tools (e.g. Camel MCP catalog for component/header changes, WebSearch for API migrations) to diagnose breaking changes introduced by the version upgrades.
   - Apply the necessary code fixes (e.g. updated header names, renamed APIs, changed method signatures).
   - Re-run the `verify` command and repeat until the build succeeds.
   - Report all code fixes applied as a separate "Build fixes" section in the summary.

## Important

- Only update version properties in `<properties>` blocks. Do not change hardcoded version strings elsewhere unless they correspond to a property you are updating.
- Do NOT downgrade versions — if this repo has a newer version, leave it and report it.
- Do NOT commit changes — just make the edits, verify the build, and report. The user will review and commit.
- The `<citrus.version>` property should always be set to `$ARGUMENTS`.
