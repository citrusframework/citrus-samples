#!/usr/bin/env python3
"""Generate and check the root sample catalog using restricted sample.yaml files.

Only Python's standard library is required. The generated README block is bounded by
SAMPLE-CATALOG markers; all curated prose outside those markers is preserved.
"""
from __future__ import annotations

import argparse
import re
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Sequence

try:
    from scripts.migrate_sample_layout import MAPPING
except ModuleNotFoundError:  # Direct execution puts scripts/, not repository root, on sys.path.
    from migrate_sample_layout import MAPPING

START_MARKER = "<!-- SAMPLE-CATALOG:START -->"
END_MARKER = "<!-- SAMPLE-CATALOG:END -->"
DSL_ORDER = ("java", "xml", "yaml", "groovy")
LEVELS = ("getting-started", "intermediate", "advanced", "showcase")
RUNTIMES = (
    "testng", "junit4", "junit-jupiter", "cucumber", "quarkus", "gradle", "jbang",
)
MODULES = (
    "citrus-core", "citrus-http", "citrus-ws", "citrus-jms", "citrus-kafka",
    "citrus-mail", "citrus-rmi", "citrus-camel", "citrus-ftp", "citrus-websocket",
    "citrus-docker", "citrus-kubernetes", "citrus-selenium", "citrus-playwright",
    "citrus-openapi", "citrus-sql", "citrus-validation-xml", "citrus-validation-json",
    "citrus-validation-hamcrest", "citrus-validation-binary", "showcase-bakery",
    "showcase-bookstore", "showcase-flight-booking", "showcase-greeting",
    "showcase-incident", "showcase-quarkus", "gradle",
)
EXPECTED_SAMPLE_ROOTS = tuple(
    entry.target for entry in MAPPING
    if entry.kind == "move" and entry.target != "apps/todo-app"
)
KEYS = ("module", "runtime", "level", "dsl")
VALUE_RE = re.compile(r"^[a-z0-9][a-z0-9-]*$")
DSL_FILE_RE = re.compile(r"\.citrus(?:\.it)?\.(xml|yaml|groovy)$")


@dataclass(frozen=True)
class Metadata:
    module: str
    runtime: str
    level: str
    dsl: tuple[str, ...]


@dataclass(frozen=True)
class Sample:
    path: str
    metadata: Metadata


def parse_metadata(text: str) -> Metadata:
    """Parse exactly four restricted YAML fields, without a third-party YAML parser."""
    values: dict[str, str] = {}
    for line_number, raw_line in enumerate(text.splitlines(), 1):
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        if ":" not in line:
            raise ValueError(f"line {line_number}: expected key: value")
        key, value = (part.strip() for part in line.split(":", 1))
        if key not in KEYS:
            raise ValueError(f"line {line_number}: unknown key {key!r}")
        if key in values:
            raise ValueError(f"line {line_number}: duplicate key {key!r}")
        values[key] = value
    missing = [key for key in KEYS if key not in values]
    if missing:
        raise ValueError(f"missing keys: {', '.join(missing)}")

    for key in ("module", "runtime", "level"):
        if not VALUE_RE.fullmatch(values[key]):
            raise ValueError(f"invalid {key}: {values[key]!r}")
    if values["module"] not in MODULES:
        raise ValueError(f"unknown module: {values['module']!r}")
    if values["runtime"] not in RUNTIMES:
        raise ValueError(f"unknown runtime: {values['runtime']!r}")
    if values["level"] not in LEVELS:
        raise ValueError(f"unknown level: {values['level']!r}")

    dsl_text = values["dsl"]
    if not (dsl_text.startswith("[") and dsl_text.endswith("]")):
        raise ValueError("dsl must be an inline list")
    items = [item.strip() for item in dsl_text[1:-1].split(",")]
    if not items or any(item not in DSL_ORDER for item in items):
        raise ValueError("dsl contains an unknown value")
    if len(items) != len(set(items)):
        raise ValueError("dsl contains a duplicate value")
    if "java" not in items:
        raise ValueError("dsl must contain java")
    if tuple(item for item in DSL_ORDER if item in items) != tuple(items):
        raise ValueError(f"dsl must use order: {', '.join(DSL_ORDER)}")
    return Metadata(values["module"], values["runtime"], values["level"], tuple(items))


def detect_dsls(sample_root: Path) -> set[str]:
    """Detect test DSLs from Java sources and conventional Citrus scenario filenames."""
    detected: set[str] = set()
    for path in sample_root.rglob("*"):
        if not path.is_file() or {"target", "build", ".gradle"}.intersection(path.parts):
            continue
        posix_path = path.as_posix()
        if path.suffix == ".java" and "/src/test/java/" in posix_path:
            detected.add("java")
        if "/src/test/" in posix_path:
            match = DSL_FILE_RE.search(path.name)
            if match:
                detected.add(match.group(1))
    return detected


def load_catalog(
    root: Path, expected_roots: Sequence[str] = EXPECTED_SAMPLE_ROOTS
) -> tuple[list[Sample], list[str]]:
    samples: list[Sample] = []
    errors: list[str] = []
    for relative_path in expected_roots:
        metadata_path = root / relative_path / "sample.yaml"
        if not metadata_path.is_file():
            errors.append(f"{relative_path}: missing sample.yaml")
            continue
        try:
            metadata = parse_metadata(metadata_path.read_text(encoding="utf-8"))
        except (OSError, UnicodeError, ValueError) as error:
            errors.append(f"{relative_path}/sample.yaml: {error}")
            continue
        detected = detect_dsls(root / relative_path)
        declared_set = set(metadata.dsl)
        if declared_set != detected:
            declared = ",".join(metadata.dsl)
            found = ",".join(dsl for dsl in DSL_ORDER if dsl in detected) or "none"
            errors.append(
                f"{relative_path}: DSL mismatch: declared={declared}; detected={found}"
            )
            continue
        samples.append(Sample(relative_path, metadata))
    return samples, errors


def render_catalog_block(samples: Iterable[Sample]) -> str:
    lines = [
        START_MARKER,
        "| Sample | Module | Runtime | Level | Java | XML | YAML | Groovy |",
        "|---|---|---|---|:---:|:---:|:---:|:---:|",
    ]
    for sample in samples:
        metadata = sample.metadata
        coverage = ["Yes" if dsl in metadata.dsl else "—" for dsl in DSL_ORDER]
        lines.append(
            f"| [{sample.path}]({sample.path}/) | `{metadata.module}` | "
            f"{metadata.runtime} | {metadata.level} | " + " | ".join(coverage) + " |"
        )
    lines.append(END_MARKER)
    return "\n".join(lines) + "\n"


def replace_catalog_block(readme: str, block: str) -> str:
    if readme.count(START_MARKER) != 1 or readme.count(END_MARKER) != 1:
        raise ValueError("expected exactly one SAMPLE-CATALOG start/end marker pair")
    start = readme.index(START_MARKER)
    end = readme.index(END_MARKER, start)
    if end < start:
        raise ValueError("SAMPLE-CATALOG markers are out of order")
    end += len(END_MARKER)
    suffix = readme[end:]
    if suffix.startswith("\r\n"):
        suffix = suffix[2:]
    elif suffix.startswith("\n"):
        suffix = suffix[1:]
    return readme[:start] + block + suffix


def validate_repository(
    root: Path, expected_roots: Sequence[str] = EXPECTED_SAMPLE_ROOTS
) -> list[str]:
    samples, errors = load_catalog(root, expected_roots)
    if errors:
        return errors
    readme_path = root / "README.md"
    try:
        readme = readme_path.read_text(encoding="utf-8")
        expected = replace_catalog_block(readme, render_catalog_block(samples))
    except (OSError, UnicodeError, ValueError) as error:
        return [f"README.md: {error}"]
    if expected != readme:
        errors.append("README.md: generated sample catalog is stale")
    return errors


def main(argv: Sequence[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="check without rewriting README.md")
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    arguments = parser.parse_args(argv)
    root = arguments.root.resolve()
    samples, errors = load_catalog(root)
    if not errors:
        readme_path = root / "README.md"
        try:
            readme = readme_path.read_text(encoding="utf-8")
            updated = replace_catalog_block(readme, render_catalog_block(samples))
        except (OSError, UnicodeError, ValueError) as error:
            errors.append(f"README.md: {error}")
        else:
            if arguments.check:
                if updated != readme:
                    errors.append("README.md: generated sample catalog is stale")
            elif updated != readme:
                readme_path.write_text(updated, encoding="utf-8")
    if errors:
        for error in errors:
            print(error, file=sys.stderr)
        return 1
    print(f"Sample catalog {'is current' if arguments.check else 'generated'}: {len(samples)} samples")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
