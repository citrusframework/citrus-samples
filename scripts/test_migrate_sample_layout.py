import contextlib
import io
import os
import subprocess
import tempfile
import unittest
from pathlib import Path

import migrate_sample_layout as m

REPO_ROOT = Path(__file__).resolve().parent.parent

POM = """<project>
  <modelVersion>4.0.0</modelVersion>
  <parent><groupId>g</groupId><artifactId>parent-artifact</artifactId><version>1</version></parent>
  <artifactId>{artifact}</artifactId>
  {packaging}
</project>
"""


def entry(source, target, group="core", kind="move", note="n"):
    return m.Entry(source=source, target=target, group=group, kind=kind, note=note)


class TempRepo:
    def __init__(self):
        self._tmp = tempfile.TemporaryDirectory()
        self.root = Path(self._tmp.name)
        self.git("init", "-q")

    def git(self, *args):
        return subprocess.run(["git", *args], cwd=self.root, check=True,
                              capture_output=True, text=True).stdout

    def write(self, rel, content="x\n", add=True):
        path = self.root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content)
        if add:
            self.git("add", rel)
        return path

    def pom(self, rel_dir, artifact, packaging=""):
        self.write(f"{rel_dir}/pom.xml" if rel_dir else "pom.xml",
                   POM.format(artifact=artifact, packaging=packaging))

    def cleanup(self):
        self._tmp.cleanup()


class MappingTest(unittest.TestCase):
    def test_mapping_has_unique_sources_and_targets(self):
        self.assertEqual([], m.validate_mapping(m.MAPPING))

    def test_every_move_has_target_and_every_other_kind_has_reason(self):
        for e in m.MAPPING:
            if e.kind == "move":
                self.assertTrue(e.target, e.source)
            else:
                self.assertFalse(e.target, e.source)
                self.assertTrue(e.note, e.source)

    def test_targets_are_not_nested(self):
        targets = [e.target for e in m.MAPPING if e.target]
        for a in targets:
            for b in targets:
                if a != b:
                    self.assertFalse(b.startswith(a + "/"), f"{b} nested in {a}")

    def test_known_targets(self):
        by_source = {e.source: e for e in m.MAPPING}
        self.assertEqual("endpoints/http/basic", by_source["samples-http/sample-http"].target)
        self.assertEqual("showcases/quarkus", by_source["demo/sample-quarkus"].target)
        self.assertEqual("getting-started", by_source["sample-groovy"].target)
        self.assertEqual("build-tools/gradle", by_source["sample-gradle"].target)
        self.assertEqual("apps/todo-app", by_source["todo-app"].target)
        self.assertEqual("connectors/openapi", by_source["samples-http/sample-swagger"].target)
        self.assertEqual("gen", by_source["gen"].source)
        self.assertIsNone(by_source["gen"].target)

    def test_duplicate_target_rejected(self):
        errors = m.validate_mapping([entry("a", "t/x"), entry("b", "t/x")])
        self.assertTrue(any("duplicate target" in e for e in errors), errors)

    def test_duplicate_source_rejected(self):
        errors = m.validate_mapping([entry("a", "t/x"), entry("a", "t/y")])
        self.assertTrue(any("duplicate source" in e for e in errors), errors)

    def test_removal_without_reason_rejected(self):
        errors = m.validate_mapping([entry("a", None, kind="remove", note="")])
        self.assertTrue(any("reason" in e for e in errors), errors)

    def test_unknown_group_rejected(self):
        errors = m.validate_mapping([entry("a", "t/x", group="bogus")])
        self.assertTrue(any("group" in e for e in errors), errors)

    def test_group_selection(self):
        entries = [entry("a", "t/a", group="http"), entry("b", "t/b", group="ws"),
                   entry("c", "t/c", group="endpoints"), entry("d", "t/d", group="core")]
        self.assertEqual(["a"], [e.source for e in m.select(entries, "http")])
        self.assertEqual(["a", "b", "c"], [e.source for e in m.select(entries, "endpoints")])
        self.assertEqual(["a", "b", "c", "d"], [e.source for e in m.select(entries, None)])
        with self.assertRaises(ValueError):
            m.select(entries, "bogus")


class AccountingTest(unittest.TestCase):
    def setUp(self):
        self.repo = TempRepo()
        self.addCleanup(self.repo.cleanup)

    def test_missing_source_is_error(self):
        errors = m.validate_accounting(self.repo.root, [entry("gone", "core/gone")])
        self.assertTrue(any("missing source" in e for e in errors), errors)

    def test_existing_target_with_source_is_error(self):
        self.repo.pom("src", "citrus-sample-core-x")
        self.repo.pom("core/x", "citrus-sample-core-x")
        errors = m.validate_accounting(self.repo.root, [entry("src", "core/x")])
        self.assertTrue(any("target exists" in e for e in errors), errors)

    def test_unaccounted_leaf_is_error(self):
        self.repo.pom("known", "a")
        self.repo.pom("stray", "b")
        errors = m.validate_accounting(self.repo.root, [entry("known", "core/known")])
        self.assertTrue(any("unaccounted" in e and "stray" in e for e in errors), errors)

    def test_aggregator_poms_are_not_leaves(self):
        self.repo.pom("known", "a")
        self.repo.pom("", "root", "<packaging>pom</packaging>")
        self.repo.pom("samples-group", "grp", "<packaging>pom</packaging>")
        self.assertEqual([], m.validate_accounting(self.repo.root, [entry("known", "core/known")]))

    def test_children_of_mapped_root_are_accounted(self):
        self.repo.pom("big", "big", "<packaging>pom</packaging>")
        self.repo.pom("big/child", "child")
        self.assertEqual([], m.validate_accounting(self.repo.root, [entry("big", "showcases/big")]))

    def test_deleted_entry_with_tracked_files_is_error(self):
        self.repo.write("old/thing.txt")
        errors = m.validate_accounting(
            self.repo.root, [entry("old", None, kind="deleted", note="husk")])
        self.assertTrue(any("tracked" in e for e in errors), errors)

    def test_moved_target_is_accounted(self):
        self.repo.pom("core/x", "citrus-sample-core-x")
        self.assertEqual([], m.validate_accounting(self.repo.root, [entry("src", "core/x")]))

    def test_gradle_root_is_a_leaf(self):
        self.repo.write("stray/build.gradle")
        errors = m.validate_accounting(self.repo.root, [])
        self.assertTrue(any("stray" in e for e in errors), errors)


class ApplyTest(unittest.TestCase):
    def setUp(self):
        self.repo = TempRepo()
        self.addCleanup(self.repo.cleanup)
        self.repo.pom("one", "one")
        self.repo.write("one/src/A.java", "package x;\n")
        self.repo.pom("two", "two")
        self.repo.git("-c", "user.name=t", "-c", "user.email=t@t", "commit", "-qm", "init")
        self.entries = [entry("one", "core/one"), entry("two", "core/two")]

    def test_dry_run_prints_ops_and_changes_nothing(self):
        out = io.StringIO()
        with contextlib.redirect_stdout(out):
            code = m.run(self.repo.root, self.entries, mode="dry-run")
        self.assertEqual(0, code)
        self.assertIn("git mv one core/one", out.getvalue())
        self.assertIn("git mv two core/two", out.getvalue())
        self.assertTrue((self.repo.root / "one/pom.xml").exists())
        self.assertFalse((self.repo.root / "core").exists())

    def test_apply_moves_with_git_mv(self):
        with contextlib.redirect_stdout(io.StringIO()):
            code = m.run(self.repo.root, self.entries, mode="apply")
        self.assertEqual(0, code)
        self.assertTrue((self.repo.root / "core/one/src/A.java").exists())
        self.assertFalse((self.repo.root / "one").exists())
        status = self.repo.git("status", "--short")
        self.assertIn("R  one/src/A.java -> core/one/src/A.java", status)

    def test_apply_selected_group_only(self):
        entries = [entry("one", "core/one", group="core"), entry("two", "apps/two", group="apps")]
        with contextlib.redirect_stdout(io.StringIO()):
            code = m.run(self.repo.root, entries, mode="apply", group="apps")
        self.assertEqual(0, code)
        self.assertTrue((self.repo.root / "apps/two").exists())
        self.assertTrue((self.repo.root / "one").exists())

    def test_apply_refuses_invalid_mapping(self):
        self.repo.pom("stray", "stray")
        with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
            code = m.run(self.repo.root, self.entries, mode="apply")
        self.assertEqual(m.EXIT_INVALID, code)
        self.assertTrue((self.repo.root / "one/pom.xml").exists())
        self.assertFalse((self.repo.root / "core").exists())

    def test_apply_refuses_when_target_exists(self):
        self.repo.pom("core/one", "citrus-sample-core-one")
        with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
            code = m.run(self.repo.root, self.entries, mode="apply")
        self.assertEqual(m.EXIT_INVALID, code)
        self.assertTrue((self.repo.root / "two/pom.xml").exists())

    def test_apply_skips_already_moved_entries(self):
        with contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(0, m.run(self.repo.root, self.entries, mode="apply"))
            self.assertEqual(0, m.run(self.repo.root, self.entries, mode="apply"))


class CheckTest(unittest.TestCase):
    def setUp(self):
        self.repo = TempRepo()
        self.addCleanup(self.repo.cleanup)

    def check(self, entries, group=None, full=False):
        err = io.StringIO()
        with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(err):
            code = m.run(self.repo.root, entries, mode="check", group=group, full=full)
        return code, err.getvalue()

    def test_pending_migration_fails_with_pending_code(self):
        self.repo.pom("one", "one")
        code, err = self.check([entry("one", "core/one")])
        self.assertEqual(m.EXIT_PENDING, code)
        self.assertIn("pending", err)

    def test_invalid_mapping_fails_with_invalid_code(self):
        self.repo.pom("one", "one")
        self.repo.pom("stray", "stray")
        code, _ = self.check([entry("one", "core/one")])
        self.assertEqual(m.EXIT_INVALID, code)

    def test_completed_group_passes(self):
        self.repo.pom("core/one", "citrus-sample-core-one")
        self.repo.write("core/one/src/main/java/A.java",
                        "package org.citrusframework.samples.core.one.sub;\n")
        self.assertEqual(0, self.check([entry("one", "core/one")])[0])

    def test_group_check_ignores_other_pending_groups(self):
        self.repo.pom("core/one", "citrus-sample-core-one")
        self.repo.pom("two", "two")
        entries = [entry("one", "core/one", group="core"), entry("two", "apps/two", group="apps")]
        self.assertEqual(0, self.check(entries, group="core")[0])
        self.assertEqual(m.EXIT_PENDING, self.check(entries, group="apps")[0])

    def test_wrong_artifact_id_is_reported(self):
        self.repo.pom("core/one", "citrus-sample-one")
        code, err = self.check([entry("one", "core/one")])
        self.assertEqual(m.EXIT_INCOMPLETE, code)
        self.assertIn("citrus-sample-core-one", err)

    def test_parent_artifact_id_is_not_mistaken_for_root(self):
        self.repo.pom("core/one", "citrus-sample-core-one")
        self.assertEqual(0, self.check([entry("one", "core/one")])[0])

    def test_todo_app_keeps_coordinates(self):
        self.repo.pom("apps/todo-app", "citrus-sample-todo")
        code, _ = self.check([entry("todo-app", "apps/todo-app", group="apps")])
        self.assertEqual(0, code)

    def test_legacy_package_is_reported(self):
        self.repo.pom("core/one", "citrus-sample-core-one")
        self.repo.write("core/one/src/test/java/A.java", "package com.consol.citrus.samples;\n")
        code, err = self.check([entry("one", "core/one")])
        self.assertEqual(m.EXIT_INCOMPLETE, code)
        self.assertIn("com.consol.citrus", err)

    def test_package_must_use_target_prefix(self):
        self.repo.pom("endpoints/http/basicauth", "citrus-sample-endpoints-http-basicauth")
        self.repo.write("endpoints/http/basicauth/src/test/java/A.java",
                        "package org.citrusframework.samples.http;\n")
        code, err = self.check([entry("src", "endpoints/http/basicauth", group="http")])
        self.assertEqual(m.EXIT_INCOMPLETE, code)
        self.assertIn("org.citrusframework.samples.endpoints.http.basicauth", err)

    def test_hyphens_removed_from_package_prefix(self):
        self.assertEqual("org.citrusframework.samples.endpoints.http.basicauth",
                         m.package_prefix("endpoints/http/basic-auth"))

    def test_artifact_id_for_target(self):
        self.assertEqual("citrus-sample-validation-json-databind",
                         m.artifact_id("validation/json/databind"))

    def test_full_check_rejects_legacy_roots(self):
        self.repo.pom("core/one", "citrus-sample-core-one")
        self.repo.write("sample-old/readme.md")
        code, err = self.check([entry("one", "core/one")], full=True)
        self.assertEqual(m.EXIT_INVALID, code)
        self.assertIn("sample-old", err)

    def test_full_check_requires_groovy_reuse_when_getting_started_done(self):
        self.repo.pom("getting-started", "citrus-sample-getting-started")
        self.repo.pom("endpoints/http/basic", "citrus-sample-endpoints-http-basic")
        e = [m.Entry("sample-groovy", "getting-started", "getting-started", "move", "n",
                     groovy_reuse="endpoints/http/basic"),
             entry("samples-http/sample-http", "endpoints/http/basic", group="http")]
        code, err = self.check(e)
        self.assertEqual(m.EXIT_INCOMPLETE, code)
        self.assertIn("groovy", err.lower())
        self.repo.write("endpoints/http/basic/src/test/resources/x/todo.citrus.groovy")
        self.assertEqual(0, self.check(e)[0])

    def test_removal_pending_while_present(self):
        self.repo.write("gen/x.txt")
        code, _ = self.check([entry("gen", None, group="build-tools", kind="remove", note="r")])
        self.assertEqual(m.EXIT_PENDING, code)

    def test_removal_done_when_absent(self):
        code, _ = self.check([entry("gen", None, group="build-tools", kind="remove", note="r")])
        self.assertEqual(0, code)


class RealRepositoryTest(unittest.TestCase):
    def test_mapping_accounts_for_every_live_leaf(self):
        self.assertEqual([], m.validate_mapping(m.MAPPING))
        self.assertEqual([], m.validate_accounting(REPO_ROOT, m.MAPPING))


if __name__ == "__main__":
    unittest.main()
