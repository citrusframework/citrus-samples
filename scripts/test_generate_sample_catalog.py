#!/usr/bin/env python3
"""Tests for the restricted sample metadata catalog generator."""
from __future__ import annotations

import tempfile
import unittest
from pathlib import Path

from scripts import generate_sample_catalog as catalog


VALID_METADATA = """module: citrus-http
runtime: testng
level: intermediate
dsl: [java, xml, yaml, groovy]
"""


class MetadataTest(unittest.TestCase):
    def test_expected_root_registry_covers_samples_and_excludes_sut(self) -> None:
        self.assertEqual(59, len(catalog.EXPECTED_SAMPLE_ROOTS))
        self.assertIn("getting-started", catalog.EXPECTED_SAMPLE_ROOTS)
        self.assertIn("endpoints/http/basic", catalog.EXPECTED_SAMPLE_ROOTS)
        self.assertIn("build-tools/gradle", catalog.EXPECTED_SAMPLE_ROOTS)
        self.assertNotIn("apps/todo-app", catalog.EXPECTED_SAMPLE_ROOTS)

    def test_parse_restricted_schema(self) -> None:
        metadata = catalog.parse_metadata(VALID_METADATA)
        self.assertEqual("citrus-http", metadata.module)
        self.assertEqual("testng", metadata.runtime)
        self.assertEqual("intermediate", metadata.level)
        self.assertEqual(("java", "xml", "yaml", "groovy"), metadata.dsl)

    def test_rejects_unknown_keys_values_and_malformed_lists(self) -> None:
        invalid = (
            VALID_METADATA + "owner: unknown\n",
            VALID_METADATA.replace("testng", "unknown"),
            VALID_METADATA.replace("intermediate", "expert"),
            VALID_METADATA.replace("citrus-http", "unknown"),
            VALID_METADATA.replace("groovy", "kotlin"),
            VALID_METADATA.replace("[java, xml, yaml, groovy]", "java, xml"),
            VALID_METADATA.replace("[java, xml, yaml, groovy]", "[xml, yaml]"),
        )
        for text in invalid:
            with self.subTest(text=text), self.assertRaises(ValueError):
                catalog.parse_metadata(text)

    def test_missing_metadata_is_an_error(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            samples, errors = catalog.load_catalog(root, ("missing/sample",))
            self.assertEqual([], samples)
            self.assertEqual(["missing/sample: missing sample.yaml"], errors)


class SourceDetectionTest(unittest.TestCase):
    def test_dsl_declaration_must_match_conventional_sources(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            sample = root / "sample"
            (sample / "src/test/java").mkdir(parents=True)
            (sample / "src/test/java/TodoIT.java").write_text("class TodoIT {}\n")
            resources = sample / "src/test/resources"
            resources.mkdir()
            (resources / "todo.citrus.xml").write_text("<test/>\n")
            (sample / "sample.yaml").write_text(
                VALID_METADATA.replace("[java, xml, yaml, groovy]", "[java, xml]")
            )
            samples, errors = catalog.load_catalog(root, ("sample",))
            self.assertEqual([], errors)
            self.assertEqual(("java", "xml"), samples[0].metadata.dsl)

            (sample / "sample.yaml").write_text(VALID_METADATA)
            _, errors = catalog.load_catalog(root, ("sample",))
            self.assertEqual(1, len(errors))
            self.assertIn("DSL mismatch", errors[0])
            self.assertIn("declared=java,xml,yaml,groovy", errors[0])
            self.assertIn("detected=java,xml", errors[0])

    def test_it_suffix_and_groovy_sources_are_detected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            sample = Path(directory) / "sample"
            (sample / "src/test/java").mkdir(parents=True)
            (sample / "src/test/java/TodoIT.java").write_text("class TodoIT {}\n")
            resources = sample / "src/test/resources"
            resources.mkdir()
            for suffix in ("xml", "yaml", "groovy"):
                (resources / f"todo.citrus.it.{suffix}").write_text("test\n")
            self.assertEqual(
                {"java", "xml", "yaml", "groovy"}, catalog.detect_dsls(sample)
            )


class ReadmeGenerationTest(unittest.TestCase):
    def sample(self) -> catalog.Sample:
        return catalog.Sample(
            "endpoints/http/basic",
            catalog.parse_metadata(VALID_METADATA),
        )

    def test_generated_block_is_replaced_without_touching_curated_text(self) -> None:
        original = (
            "before\n<!-- SAMPLE-CATALOG:START -->\nold\n"
            "<!-- SAMPLE-CATALOG:END -->\nafter\n"
        )
        updated = catalog.replace_catalog_block(
            original, catalog.render_catalog_block((self.sample(),))
        )
        self.assertTrue(updated.startswith("before\n"))
        self.assertTrue(updated.endswith("after\n"))
        self.assertNotIn("\nold\n", updated)
        self.assertIn("[endpoints/http/basic](endpoints/http/basic/)", updated)
        self.assertEqual(
            updated,
            catalog.replace_catalog_block(
                updated, catalog.render_catalog_block((self.sample(),))
            ),
        )

    def test_check_reports_stale_generated_output(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            sample = root / "sample"
            sample.mkdir()
            (sample / "src/test/java").mkdir(parents=True)
            (sample / "src/test/java/TodoIT.java").write_text("class TodoIT {}\n")
            (sample / "sample.yaml").write_text(
                "module: citrus-core\nruntime: testng\nlevel: intermediate\ndsl: [java]\n"
            )
            (root / "README.md").write_text(
                "<!-- SAMPLE-CATALOG:START -->\nstale\n<!-- SAMPLE-CATALOG:END -->\n"
            )
            errors = catalog.validate_repository(root, ("sample",))
            self.assertIn("README.md: generated sample catalog is stale", errors)


if __name__ == "__main__":
    unittest.main()
