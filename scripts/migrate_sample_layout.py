#!/usr/bin/env python3
"""Checked old-to-new mapping for the citrus-samples layout migration.

Modes (exactly one required):
  --check     validate accounting and report pending or incomplete migrations
  --dry-run   print the ordered `git mv` operations without changing anything
  --apply     perform the validated `git mv` operations (never edits or commits)

The mapping is normative and mirrors docs/specs/restructure-sample-catalog/sdd/02-architecture.md.
Python 3 standard library only.
"""
from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, List, Optional, Sequence

EXIT_PENDING = 1
EXIT_INVALID = 2
EXIT_INCOMPLETE = 3

GROUPS = (
    "apps", "core", "runtimes", "http", "ws", "endpoints", "connectors",
    "validation", "showcases", "build-tools", "getting-started",
)
ENDPOINT_SLICES = ("http", "ws", "endpoints")

LEAF_MARKERS = ("pom.xml", "build.gradle", "build.gradle.kts", "settings.gradle")
LEGACY_ROOT = re.compile(r"^(sample-.*|samples-.*|demo|todo-app|gen)$")
SOURCE_SUFFIXES = (".java", ".groovy", ".kt")
SKIPPED_DIRS = {"target", "node_modules", ".git", "build", ".gradle"}
PACKAGE_RE = re.compile(r"^\s*package\s+([\w.]+)\s*;?", re.MULTILINE)
PACKAGING_POM_RE = re.compile(r"<packaging>\s*pom\s*</packaging>")
PARENT_RE = re.compile(r"<parent>.*?</parent>", re.DOTALL)
ARTIFACT_RE = re.compile(r"<artifactId>\s*([^<\s]+)\s*</artifactId>")

TODO_TARGET = "apps/todo-app"
MULTI_DSL_LEAVES = ("getting-started", "endpoints/http/basic", "endpoints/ws/basic")


@dataclass(frozen=True)
class Entry:
    source: str
    target: Optional[str]
    group: str
    kind: str = "move"  # move | remove | deleted
    note: str = ""
    groovy_reuse: Optional[str] = None


def _moves(group: str, rows: Iterable[Sequence[str]], note: str = "Relocate") -> List[Entry]:
    return [Entry(src, dst, group, "move", note) for src, dst in rows]


MAPPING: List[Entry] = [
    Entry("todo-app", "apps/todo-app", "apps", "move", "Move SUT; preserve behavior"),
    *_moves("core", [
        ("sample-annotation-config", "core/annotation-config"),
        ("sample-javaconfig", "core/java-config"),
        ("sample-behaviors", "core/behaviors"),
        ("sample-dictionaries", "core/data-dictionaries"),
        ("sample-message-store", "core/message-store"),
        ("sample-dynamic-endpoints", "core/dynamic-endpoints"),
        ("sample-reporting", "core/reporting"),
    ]),
    *_moves("runtimes", [
        ("samples-testng/sample-testng", "runtimes/testng"),
        ("samples-testng/sample-dataprovider", "runtimes/testng-dataprovider"),
        ("samples-junit/sample-junit", "runtimes/junit4"),
        ("samples-junit/sample-junit5", "runtimes/junit-jupiter"),
        ("samples-cucumber/sample-cucumber", "runtimes/cucumber"),
        ("samples-cucumber/sample-cucumber-spring", "runtimes/cucumber-spring"),
    ]),
    Entry("samples-http/sample-http", "endpoints/http/basic", "http", "move",
          "Retain Java/XML/YAML and add the missing Groovy variant"),
    *_moves("http", [
        ("samples-http/sample-http-basic-auth", "endpoints/http/basic-auth"),
        ("samples-http/sample-http-form-data", "endpoints/http/form-data"),
        ("samples-http/sample-http-query-param", "endpoints/http/query-param"),
        ("samples-http/sample-http-static-response", "endpoints/http/static-response"),
        ("samples-http/sample-https", "endpoints/http/https"),
        ("samples-http/sample-http-loadtest", "endpoints/http/load-test"),
    ]),
    Entry("samples-soap/sample-soap", "endpoints/ws/basic", "ws", "move",
          "Retain Java/XML/YAML and add the missing Groovy variant"),
    *_moves("ws", [
        ("samples-soap/sample-soap-attachment", "endpoints/ws/attachment"),
        ("samples-soap/sample-soap-mtom", "endpoints/ws/mtom"),
        ("samples-soap/sample-soap-ssl", "endpoints/ws/ssl"),
        ("samples-soap/sample-soap-static-response", "endpoints/ws/static-response"),
        ("samples-soap/sample-soap-wsaddressing", "endpoints/ws/ws-addressing"),
        ("samples-soap/sample-soap-wssecurity", "endpoints/ws/ws-security"),
        ("samples-soap/sample-wsdl", "endpoints/ws/wsdl"),
    ]),
    *_moves("endpoints", [
        ("sample-jms", "endpoints/jms"),
        ("sample-kafka", "endpoints/kafka"),
        ("sample-mail", "endpoints/mail"),
        ("sample-rmi", "endpoints/rmi"),
        ("sample-camel-context", "endpoints/camel"),
        ("samples-ftp/sample-ftp", "endpoints/ftp/ftp"),
        ("samples-ftp/sample-scp", "endpoints/ftp/scp"),
        ("samples-ftp/sample-sftp", "endpoints/ftp/sftp"),
        ("samples-websocket/sample-websocket-client", "endpoints/websocket/client"),
        ("samples-websocket/sample-websocket-server", "endpoints/websocket/server"),
    ]),
    Entry("sample-docker", "connectors/docker", "connectors", "move",
          "Opt-in environment-samples profile only"),
    Entry("sample-kubernetes", "connectors/kubernetes", "connectors", "move",
          "Opt-in environment-samples profile only"),
    Entry("sample-selenium", "connectors/selenium", "connectors", "move", "Relocate"),
    Entry("sample-playwright", "connectors/playwright", "connectors", "move",
          "Opt-in environment-samples profile only; browser installation excluded from default CI"),
    Entry("samples-http/sample-swagger", "connectors/openapi", "connectors", "move",
          "Reclassify from HTTP to OpenAPI connector"),
    Entry("samples-db/sample-sql", "connectors/sql", "connectors", "move",
          "Reclassify from JDBC endpoint to SQL connector"),
    *_moves("validation", [
        ("samples-xml/sample-xml", "validation/xml/xml"),
        ("samples-xml/sample-xhtml", "validation/xml/xhtml"),
        ("samples-xml/sample-oxm", "validation/xml/oxm"),
        ("samples-json/sample-json", "validation/json/json"),
        ("samples-json/sample-databind", "validation/json/databind"),
        ("sample-hamcrest", "validation/hamcrest"),
        ("sample-binary", "validation/binary"),
    ]),
    *_moves("showcases", [
        ("demo/sample-bakery", "showcases/bakery"),
        ("demo/sample-bookstore", "showcases/bookstore"),
        ("demo/sample-flightbooking", "showcases/flight-booking"),
        ("demo/sample-greeting", "showcases/greeting"),
        ("demo/sample-incident", "showcases/incident"),
        ("demo/sample-quarkus", "showcases/quarkus"),
    ]),
    Entry("sample-gradle", "build-tools/gradle", "build-tools", "move",
          "Keep outside Maven reactor"),
    Entry("sample-groovy", "getting-started", "getting-started", "move",
          "Move the four-DSL project; reuse its Groovy scenario in endpoints/http/basic",
          groovy_reuse="endpoints/http/basic"),
    Entry("gen", None, "build-tools", "remove", "Generated output; remove if present"),
    Entry("samples-db/sample-jdbc", None, "endpoints", "deleted",
          "Deleted source husk; do not recreate endpoints/jdbc/basic"),
    Entry("samples-db/sample-jdbc-callable-statements", None, "endpoints", "deleted",
          "Deleted source husk; no target module"),
    Entry("samples-db/sample-jdbc-transactions", None, "endpoints", "deleted",
          "Deleted source husk; no target module"),
    Entry("samples-remote/sample-test-jar", None, "runtimes", "deleted",
          "Deleted source husk; remove stale README link"),
    Entry("samples-remote/sample-test-war", None, "runtimes", "deleted",
          "Deleted source husk; remove stale README link"),
    Entry("demo/sample-javaee", None, "showcases", "deleted",
          "Deleted source husk; do not recreate a Jakarta EE showcase"),
]


def artifact_id(target: str) -> str:
    return "citrus-sample-" + target.replace("/", "-")


def package_prefix(target: str) -> str:
    return "org.citrusframework.samples." + ".".join(
        segment.replace("-", "") for segment in target.split("/"))


def select(entries: Sequence[Entry], group: Optional[str]) -> List[Entry]:
    if group is None:
        return list(entries)
    if group not in GROUPS:
        raise ValueError(f"unknown group '{group}'; expected one of {', '.join(GROUPS)}")
    slices = ENDPOINT_SLICES if group == "endpoints" else (group,)
    return [e for e in entries if e.group in slices]


def validate_mapping(entries: Sequence[Entry]) -> List[str]:
    errors: List[str] = []
    sources, targets = set(), set()
    for e in entries:
        if e.group not in GROUPS:
            errors.append(f"unknown group '{e.group}' for {e.source}")
        if e.source in sources:
            errors.append(f"duplicate source {e.source}")
        sources.add(e.source)
        if e.kind == "move":
            if not e.target:
                errors.append(f"move without target: {e.source}")
            elif e.target in targets:
                errors.append(f"duplicate target {e.target}")
            else:
                targets.add(e.target)
        else:
            if e.target:
                errors.append(f"{e.kind} entry must not have a target: {e.source}")
            if not e.note:
                errors.append(f"{e.kind} entry needs a reason: {e.source}")
    return errors


def list_files(root: Path) -> List[str]:
    out = subprocess.run(
        ["git", "ls-files", "--cached", "--others", "--exclude-standard"],
        cwd=root, check=True, capture_output=True, text=True).stdout
    return [line for line in out.splitlines() if line and (root / line).exists()]


def _covered(directory: str, paths: Iterable[str]) -> bool:
    return any(directory == p or directory.startswith(p + "/") for p in paths)


def validate_accounting(root: Path, entries: Sequence[Entry]) -> List[str]:
    errors: List[str] = []
    files = list_files(root)
    mapped = [p for e in entries for p in (e.source, e.target) if p]

    for e in entries:
        src_exists = (root / e.source).exists()
        dst_exists = bool(e.target) and (root / e.target).exists()
        if e.kind == "move":
            if src_exists and dst_exists:
                errors.append(f"target exists while source remains: {e.source} -> {e.target}")
            elif not src_exists and not dst_exists:
                errors.append(f"missing source: {e.source} (target {e.target} also absent)")
        elif e.kind == "deleted":
            if any(f == e.source or f.startswith(e.source + "/") for f in files):
                errors.append(f"deleted entry has tracked files: {e.source}")

    for f in sorted(files):
        name = os.path.basename(f)
        if name not in LEAF_MARKERS:
            continue
        directory = os.path.dirname(f)
        if not directory or _covered(directory, mapped):
            continue
        if name == "pom.xml" and PACKAGING_POM_RE.search((root / f).read_text(errors="replace")):
            continue
        errors.append(f"unaccounted leaf: {directory}")
    return errors


def _state(root: Path, e: Entry) -> str:
    src_exists = (root / e.source).exists()
    if e.kind == "move":
        return "pending" if src_exists else "done"
    if e.kind == "remove":
        return "pending" if src_exists else "done"
    return "done"


def _root_artifact(pom: Path) -> Optional[str]:
    text = PARENT_RE.sub("", pom.read_text(errors="replace"))
    match = ARTIFACT_RE.search(text)
    return match.group(1) if match else None


def _source_files(base: Path) -> Iterable[Path]:
    for dirpath, dirnames, filenames in os.walk(base):
        dirnames[:] = [d for d in dirnames if d not in SKIPPED_DIRS]
        if "src" not in Path(dirpath).relative_to(base).parts:
            continue
        for name in filenames:
            if name.endswith(SOURCE_SUFFIXES):
                yield Path(dirpath) / name


def identity_errors(root: Path, e: Entry) -> List[str]:
    errors: List[str] = []
    base = root / e.target
    pom = base / "pom.xml"
    if e.target != TODO_TARGET and pom.exists():
        actual, expected = _root_artifact(pom), artifact_id(e.target)
        if actual != expected:
            errors.append(f"{e.target}/pom.xml: artifactId '{actual}' must be '{expected}'")
    prefix = package_prefix(e.target)
    for src in _source_files(base):
        match = PACKAGE_RE.search(src.read_text(errors="replace"))
        if not match:
            continue
        pkg = match.group(1)
        if pkg != prefix and not pkg.startswith(prefix + "."):
            legacy = " (legacy com.consol.citrus package)" if pkg.startswith("com.consol.citrus") else ""
            errors.append(
                f"{src.relative_to(root)}: package {pkg} must start with {prefix}{legacy}")
    if e.groovy_reuse:
        reuse = root / e.groovy_reuse
        if not any(p.suffix == ".groovy" for p in reuse.rglob("*") if "target" not in p.parts):
            errors.append(f"{e.groovy_reuse}: missing reused groovy scenario variant")
    return errors


def multi_dsl_errors(root: Path, leaves: Iterable[str] = MULTI_DSL_LEAVES) -> List[str]:
    """Check equivalent XML/YAML/Groovy scenarios and their single Java runner."""
    errors: List[str] = []
    for name in leaves:
        leaf = root / name
        runners = list(leaf.glob("src/test/java/**/TodoListIT.java"))
        runner_text = runners[0].read_text(errors="replace") if len(runners) == 1 else ""
        if len(runners) != 1:
            errors.append(f"{name}: expected one TodoListIT.java runner, found {len(runners)}")
        else:
            if "@CitrusTest" not in runner_text:
                errors.append(f"{name}: TodoListIT.java has no Java @CitrusTest scenario")
            for loader in ("XML", "YAML", "GROOVY"):
                if f"TestLoader.{loader}" not in runner_text:
                    errors.append(f"{name}: TodoListIT.java missing TestLoader.{loader} runner")
        for split in ("TodoListXmlIT.java", "TodoListYamlIT.java", "TodoListGroovyIT.java"):
            if list(leaf.glob(f"src/test/java/**/{split}")):
                errors.append(f"{name}: split runner {split} must merge into TodoListIT.java")

        scenarios = {}
        for dsl, source in (("xml", "resources"), ("yaml", "resources"), ("groovy", "groovy")):
            files = list(leaf.glob(f"src/test/{source}/**/*.citrus.{dsl}"))
            scenarios[dsl] = {p.name[: -len(f".citrus.{dsl}")] for p in files}
            if not files:
                errors.append(f"{name}: missing {dsl} scenarios")
            for path in files:
                if path.name not in runner_text:
                    errors.append(f"{name}: TodoListIT.java does not execute {path.name}")
        baseline = scenarios["xml"]
        for dsl in ("yaml", "groovy"):
            if scenarios[dsl] != baseline:
                errors.append(
                    f"{name}: scenario mismatch xml={sorted(baseline)} "
                    f"{dsl}={sorted(scenarios[dsl])}")
    return errors


def legacy_root_errors(root: Path) -> List[str]:
    legacy = sorted({f.split("/", 1)[0] for f in list_files(root)})
    return [f"legacy root remains: {name}" for name in legacy if LEGACY_ROOT.match(name)]


def _print_ops(root: Path, entries: Sequence[Entry]) -> None:
    for e in entries:
        if _state(root, e) != "pending":
            continue
        if e.kind == "move":
            print(f"git mv {e.source} {e.target}")
        elif e.kind == "remove":
            print(f"# manual removal required: {e.source} ({e.note})")


def _apply(root: Path, entries: Sequence[Entry]) -> None:
    for e in entries:
        if e.kind != "move" or _state(root, e) != "pending":
            continue
        (root / e.target).parent.mkdir(parents=True, exist_ok=True)
        subprocess.run(["git", "mv", e.source, e.target], cwd=root, check=True)
        print(f"moved {e.source} -> {e.target}")


def _warn(lines: Iterable[str]) -> None:
    for line in lines:
        print(line, file=sys.stderr)


def run(root: Path, entries: Sequence[Entry], mode: str,
        group: Optional[str] = None, full: bool = False) -> int:
    root = Path(root)
    try:
        selected = select(entries, group)
    except ValueError as exc:
        print(str(exc), file=sys.stderr)
        return EXIT_INVALID

    errors = validate_mapping(entries) + validate_accounting(root, entries)
    if errors:
        _warn(f"invalid mapping: {err}" for err in errors)
        return EXIT_INVALID

    pending = [e for e in selected if _state(root, e) == "pending"]

    if mode == "dry-run":
        _print_ops(root, selected)
        return 0
    if mode == "apply":
        _apply(root, selected)
        return 0

    incomplete: List[str] = []
    for e in selected:
        if e.kind == "move" and _state(root, e) == "done":
            incomplete.extend(identity_errors(root, e))
    if incomplete:
        _warn(f"incomplete: {err}" for err in incomplete)
        if not pending:
            return EXIT_INCOMPLETE
    if pending:
        _warn(f"pending: {e.source} -> {e.target or '(remove)'}" for e in pending)
        return EXIT_INCOMPLETE if incomplete else EXIT_PENDING

    if full:
        leftovers = legacy_root_errors(root)
        if leftovers:
            _warn(leftovers)
            return EXIT_INVALID
        dsl_errors = multi_dsl_errors(root)
        if dsl_errors:
            _warn(f"incomplete: {err}" for err in dsl_errors)
            return EXIT_INCOMPLETE
    return 0


def main(argv: Optional[Sequence[str]] = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    modes = parser.add_mutually_exclusive_group(required=True)
    modes.add_argument("--check", action="store_const", const="check", dest="mode")
    modes.add_argument("--dry-run", action="store_const", const="dry-run", dest="mode")
    modes.add_argument("--apply", action="store_const", const="apply", dest="mode")
    parser.add_argument("--group", choices=GROUPS)
    parser.add_argument("--root", default=str(Path(__file__).resolve().parent.parent))
    args = parser.parse_args(argv)
    return run(Path(args.root), MAPPING, args.mode, args.group, full=args.group is None)


if __name__ == "__main__":
    sys.exit(main())
