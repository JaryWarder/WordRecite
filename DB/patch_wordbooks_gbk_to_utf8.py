from __future__ import annotations

from dataclasses import dataclass
import re
from pathlib import Path


@dataclass(frozen=True)
class Job:
    source_file: str
    output_file: str
    target_table: str


INSERT_ID_PREFIX_RE = re.compile(
    r"(?is)^\s*insert\s+into\s+(?P<table>[a-zA-Z0-9_]+)\s+values\s*\(\s*'[^']*'\s*,\s*(?P<rest>.*)\)\s*;\s*$"
)
INSERT_GENERIC_RE = re.compile(
    r"(?is)^\s*insert\s+into\s+(?P<table>[a-zA-Z0-9_]+)\b(?P<tail>.*)$"
)
CREATE_TABLE_START_RE = re.compile(r"(?is)^\s*create\s+table\b")
DROP_TABLE_RE = re.compile(r"(?is)^\s*drop\s+table\b")
USE_DB_RE = re.compile(r"(?is)^\s*use\s+")


def read_text_gb(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8", errors="strict")
    except UnicodeDecodeError:
        try:
            return path.read_text(encoding="gb18030", errors="strict")
        except UnicodeDecodeError:
            return path.read_text(encoding="gbk", errors="strict")


def build_header(db_name: str, target_table: str) -> list[str]:
    return [
        "SET NAMES utf8mb4;",
        "SET CHARACTER_SET_CLIENT = utf8mb4;",
        "SET CHARACTER_SET_RESULTS = utf8mb4;",
        "SET collation_connection = utf8mb4_unicode_ci;",
        f"USE {db_name};",
        "SET FOREIGN_KEY_CHECKS=0;",
        f"TRUNCATE TABLE {target_table};",
        "SET FOREIGN_KEY_CHECKS=1;",
    ]


def patch_lines(raw_lines: list[str], target_table: str, db_name: str) -> tuple[list[str], dict[str, int]]:
    output: list[str] = []
    stats = {
        "insert_converted": 0,
        "insert_replaced_only": 0,
        "skipped_use_drop_create": 0,
        "kept_other": 0,
    }

    output.extend(build_header(db_name=db_name, target_table=target_table))

    in_create_block = False
    for line in raw_lines:
        if in_create_block:
            if line.strip().endswith(");") or line.strip() == ")":
                in_create_block = False
            stats["skipped_use_drop_create"] += 1
            continue

        if USE_DB_RE.match(line) or DROP_TABLE_RE.match(line):
            stats["skipped_use_drop_create"] += 1
            continue

        if CREATE_TABLE_START_RE.match(line):
            in_create_block = True
            stats["skipped_use_drop_create"] += 1
            continue

        m = INSERT_ID_PREFIX_RE.match(line)
        if m:
            rest = m.group("rest")
            output.append(
                f"INSERT IGNORE INTO {target_table} (word, phonetic, pron, poses, sentences) VALUES ({rest});"
            )
            stats["insert_converted"] += 1
            continue

        m2 = INSERT_GENERIC_RE.match(line)
        if m2:
            tail = m2.group("tail")
            output.append(f"INSERT IGNORE INTO {target_table}{tail}")
            stats["insert_replaced_only"] += 1
            continue

        stripped = line.strip()
        if not stripped:
            continue

        output.append(line.rstrip("\r\n"))
        stats["kept_other"] += 1

    return output, stats


def main() -> None:
    base_dir = Path(__file__).resolve().parent
    jobs = [
        Job("CET6_new.sql", "CET6_patched_v2.sql", "CET6WordBook"),
        Job("GRE_new.sql", "GRE_patched_v2.sql", "GREWordBook"),
        Job("TOEFL_new.sql", "TOEFL_patched_v2.sql", "TOEFLWordBook"),
    ]
    db_name = "bs_project"

    for job in jobs:
        src = base_dir / job.source_file
        dst = base_dir / job.output_file
        raw_text = read_text_gb(src)
        raw_lines = raw_text.splitlines()
        patched_lines, stats = patch_lines(raw_lines=raw_lines, target_table=job.target_table, db_name=db_name)
        dst.write_text("\n".join(patched_lines) + "\n", encoding="utf-8", newline="\n")
        print(
            f"{job.output_file}: inserts_converted={stats['insert_converted']} inserts_replaced={stats['insert_replaced_only']} skipped={stats['skipped_use_drop_create']}"
        )


if __name__ == "__main__":
    main()
