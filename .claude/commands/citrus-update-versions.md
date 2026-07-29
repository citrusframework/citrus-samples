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

2. **Read every `pom.xml` in this repository** (excluding `target/` directories). Use `find . -name "pom.xml" -not -path "*/target/*"` to locate them. Read each one.

3. **Compare versions.** For each version property found in the Citrus release POM, check whether the same property name (or a clearly equivalent one) exists in the local POM files. Classify each match into one of:
   - **Outdated** — the local version is older than the Citrus release version. These will be updated.
   - **Newer** — the local version is strictly newer than the Citrus release version. These will NOT be updated but will be reported.
   - **Same** — versions match. No action needed.

   Use semantic version comparison (split on `.` and compare segments numerically, then qualifiers like `-SNAPSHOT`, `-alpha`, `-beta`, `-RC` etc.).

4. **Update outdated versions.** For each outdated property, use the Edit tool to update the version value in the local `pom.xml` where the property is defined. Also update the `<citrus.version>` property to `$ARGUMENTS`.

5. **Report results.** At the end, print a summary:
   - A table of all updated versions: property name, old version → new version.
   - A separate section listing any properties where this repository has a NEWER version than the Citrus release POM, showing: property name, local version, Citrus release version.

## Important

- Only update version properties in `<properties>` blocks. Do not change hardcoded version strings elsewhere unless they correspond to a property you are updating.
- Do NOT downgrade versions — if this repo has a newer version, leave it and report it.
- Do NOT commit changes — just make the edits and report. The user will review and commit.
- The `<citrus.version>` property should always be set to `$ARGUMENTS`.
