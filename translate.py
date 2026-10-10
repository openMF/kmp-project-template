#!/usr/bin/env python3
"""
Android String Resource Translator

Production-ready translation of Android string resources using Google Gemini API.

Features:
- Comment preservation (copies comments from source file exactly)
- Spacing preservation (maintains blank lines and structure from source)
- Placeholder preservation with validation (%s, %1$s, etc.)
- Markup tag preservation with validation (<b>, <xliff:g>, etc.)
- Token order validation (not just presence)
- Source attribute propagation (formatted, product, tools:*)
- Conditional placeholder handling for formatted="false" strings
- Whitespace preservation (no stripping of source text)
- HTML entity conversion (case-insensitive)
- Android special character escaping
- Proper xliff namespace handling for AAPT2 compatibility
- Batch translation with individual fallback
- Better 503/overload error handling
- Change detection via snapshot tracking (re-translates modified strings)
- Comprehensive validation and error handling

Usage:
    python translate.py --mode check --locales es,de,fr
    python translate.py --mode apply --locales es,de,fr
    python translate.py --mode apply --locales ar --model gemma-3-27b-it --batch-size 15

Environment:
    GEMINI_API_KEY=your_api_key_here
"""

from __future__ import annotations

import argparse
import copy
import hashlib
import json
import logging
import os
import re
import sys
import time
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, FrozenSet, List, Optional, Set, Tuple

from lxml import etree as ET
from google import genai
from google.genai import types

# ============================================================================
# Logging Configuration
# ============================================================================

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
logger = logging.getLogger(__name__)

# ============================================================================
# Constants & Namespaces
# ============================================================================

XML_PARSER = ET.XMLParser(
    remove_blank_text=False,
    remove_comments=False,
    strip_cdata=False,
)

XLIFF_NAMESPACE = "urn:oasis:names:tc:xliff:document:1.2"
TOOLS_NAMESPACE = "http://schemas.android.com/tools"

ET.register_namespace("xliff", XLIFF_NAMESPACE)
ET.register_namespace("tools", TOOLS_NAMESPACE)

DEFAULT_EXCLUDE_DIRS: FrozenSet[str] = frozenset({
    ".git", ".gradle", "build", ".idea", "node_modules",
    "__pycache__", "venv", ".venv", ".svn", ".hg", "target",
    "bin", "obj", ".dart_tool", ".pub-cache",
})

PROPAGATE_ATTRIBUTES: FrozenSet[str] = frozenset({
    "formatted",
    "product",
})

ALLOWED_TAGS: FrozenSet[str] = frozenset({
    "b", "i", "u", "s", "strike", "del", "ins",
    "strong", "em", "cite", "dfn", "code", "samp", "kbd", "var",
    "big", "small", "sup", "sub", "tt",
    "a", "font", "annotation", "span",
    "xliff:g", "g",
})

SNAPSHOT_DIR_NAME = ".translation_snapshots"

# ============================================================================
# Regex Patterns
# ============================================================================

PLACEHOLDER_PATTERNS = [
    r"%%",
    r"%n",
    r"\\n",
    r"\\t",
    r"%(?:\d+\$)?[-+# 0,(]*\d*(?:\.\d+)?[sdbBhHoOxXeEfgGaAcC]",
    r"%(?:\d+\$)?[-+# 0,(]*\d*(?:\.\d+)?t[HIklMSLNpzZsQBbhAaCYyjmdeRTrDFc]",
]
PLACEHOLDER_RE = re.compile("|".join(PLACEHOLDER_PATTERNS))

XLIFF_TAG_RE = re.compile(
    r"<xliff:g[^>]*>.*?</xliff:g>|<xliff:g[^>]*/\s*>",
    re.DOTALL | re.IGNORECASE,
)

MARKUP_TAG_RE = re.compile(r"</?[a-zA-Z][^>]*>")
MARKUP_PATTERN = re.compile(r"</?[a-zA-Z][^>]*>")
TAG_NAME_PATTERN = re.compile(r"</?([a-zA-Z][a-zA-Z0-9:]*)")

BARE_AMPERSAND_PATTERN = re.compile(
    r"&(?!(amp|lt|gt|quot|apos);|#\d+;|#x[0-9a-fA-F]+;)"
)

HTML_ENTITY_PATTERN = re.compile(
    r"&(nbsp|copy|reg|trade|mdash|ndash|hellip|bull|euro|pound|yen|cent);?",
    re.IGNORECASE,
)

HTML_ENTITY_TO_NUMERIC: Dict[str, str] = {
    "nbsp": "&#160;",
    "copy": "&#169;",
    "reg": "&#174;",
    "trade": "&#8482;",
    "mdash": "&#8212;",
    "ndash": "&#8211;",
    "hellip": "&#8230;",
    "bull": "&#8226;",
    "euro": "&#8364;",
    "pound": "&#163;",
    "yen": "&#165;",
    "cent": "&#162;",
}

TOKEN_SEQUENCE_RE = re.compile(r"$$\[(?:PH|TAG)_\d+$$\]")

# ============================================================================
# Exceptions
# ============================================================================


class TranslationError(Exception):
    """Raised when translation API call fails."""
    pass


class XmlWriteError(Exception):
    """Raised when writing XML fails validation."""
    pass


class ValidationError(Exception):
    """Raised when translation validation fails."""
    pass


# ============================================================================
# Data Structures
# ============================================================================


@dataclass
class Config:
    """Application configuration."""
    repo_root: Path
    mode: str
    locales: List[str]
    model: str
    batch_size: int
    api_key: str
    exclude_dirs: FrozenSet[str] = DEFAULT_EXCLUDE_DIRS
    max_retries: int = 5
    base_retry_delay: float = 10.0
    request_delay: float = 2.0
    validate_output: bool = True
    warn_unknown_tags: bool = True


@dataclass
class StringEntry:
    """A string resource entry with text and attributes."""
    key: str
    text: str
    attributes: Dict[str, str] = field(default_factory=dict)

    @property
    def is_formatted(self) -> bool:
        return self.attributes.get("formatted", "true").lower() != "false"

    def get_propagated_attributes(self) -> Dict[str, str]:
        """Get attributes to propagate to translation (name, formatted, product, tools:*)."""
        result = {"name": self.key}
        for attr in PROPAGATE_ATTRIBUTES:
            if attr in self.attributes:
                result[attr] = self.attributes[attr]
        for key, value in self.attributes.items():
            if key.startswith(f"{{{TOOLS_NAMESPACE}}}") or key.startswith("tools:"):
                result[key] = value
        return result

@dataclass
class StringArrayEntry:
    """A string-array resource entry with ordered items."""
    key: str
    items: List[str]
    attributes: Dict[str, str] = field(default_factory=dict)

    def flat_entries(self) -> List[StringEntry]:
        """Flatten into individual StringEntry objects for translation."""
        return [
            StringEntry(
                key=f"{self.key}__item_{i}",
                text=text,
                attributes=self.attributes,
            )
            for i, text in enumerate(self.items)
            if text and text.strip()
        ]

    def content_for_hash(self) -> str:
        """Combined content for snapshot hashing."""
        return "||".join(self.items)


@dataclass
class PluralsEntry:
    """A plurals resource entry with quantity variants."""
    key: str
    items: Dict[str, str]  # quantity -> text
    attributes: Dict[str, str] = field(default_factory=dict)

    def flat_entries(self) -> List[StringEntry]:
        """Flatten into individual StringEntry objects for translation."""
        return [
            StringEntry(
                key=f"{self.key}__plural_{quantity}",
                text=text,
                attributes=self.attributes,
            )
            for quantity, text in self.items.items()
            if text and text.strip()
        ]

    def content_for_hash(self) -> str:
        """Combined content for snapshot hashing."""
        return "||".join(f"{q}={t}" for q, t in sorted(self.items.items()))


@dataclass
class SourceResources:
    """All translatable resources from a single source file."""
    strings: List[StringEntry] = field(default_factory=list)
    string_arrays: List[StringArrayEntry] = field(default_factory=list)
    plurals: List[PluralsEntry] = field(default_factory=list)

    @property
    def total_count(self) -> int:
        return (
            len(self.strings)
            + sum(len(a.items) for a in self.string_arrays)
            + sum(len(p.items) for p in self.plurals)
        )

    @property
    def is_empty(self) -> bool:
        return not self.strings and not self.string_arrays and not self.plurals

    def all_flat_entries(self) -> List[StringEntry]:
        """All translatable items flattened into StringEntry list."""
        entries: List[StringEntry] = list(self.strings)
        for arr in self.string_arrays:
            entries.extend(arr.flat_entries())
        for plu in self.plurals:
            entries.extend(plu.flat_entries())
        return entries

    def all_keys_for_snapshot(self) -> Dict[str, str]:
        """Build key -> hash mapping for snapshot tracking."""
        data: Dict[str, str] = {}
        for s in self.strings:
            data[s.key] = content_hash(s.text)
        for a in self.string_arrays:
            data[f"__array__{a.key}"] = content_hash(a.content_for_hash())
        for p in self.plurals:
            data[f"__plurals__{p.key}"] = content_hash(p.content_for_hash())
        return data

@dataclass
class ExistingKeys:
    """Track which keys already exist in a target file."""
    strings: Set[str] = field(default_factory=set)
    string_arrays: Set[str] = field(default_factory=set)
    plurals: Set[str] = field(default_factory=set)

    @property
    def all_string_keys(self) -> Set[str]:
        return self.strings

@dataclass
class FrozenText:
    """Text with placeholders and markup tags replaced by tokens."""
    original: str
    frozen: str
    placeholders: List[str]
    tags: List[str]

    def unfreeze(self, translated_frozen: str) -> str:
        result = translated_frozen
        for i, ph in enumerate(self.placeholders):
            result = result.replace(f"[[PH_{i}]]", ph)
        for i, tag in enumerate(self.tags):
            result = result.replace(f"[[TAG_{i}]]", tag)
        return result

    def validate(self, translated_frozen: str) -> Tuple[bool, List[str]]:
        errors: List[str] = []
        for i, ph in enumerate(self.placeholders):
            token = f"[[PH_{i}]]"
            if token not in translated_frozen:
                errors.append(f"Missing placeholder {token} (was: {ph})")
        for i, tag in enumerate(self.tags):
            token = f"[[TAG_{i}]]"
            if token not in translated_frozen:
                tag_preview = tag[:40] + "..." if len(tag) > 40 else tag
                errors.append(f"Missing tag {token} (was: {tag_preview})")
        expected_tokens = TOKEN_SEQUENCE_RE.findall(self.frozen)
        actual_tokens = TOKEN_SEQUENCE_RE.findall(translated_frozen)
        if expected_tokens != actual_tokens and not errors:
            errors.append(f"Token order changed: expected {expected_tokens}, got {actual_tokens}")
        return len(errors) == 0, errors

    @property
    def has_tokens(self) -> bool:
        return bool(self.placeholders or self.tags)

    @property
    def token_count(self) -> int:
        return len(self.placeholders) + len(self.tags)


@dataclass
class LocaleResult:
    """Translation results for a single locale and source file."""
    locale: str
    source_path: Path
    target_path: Path
    total_source: int = 0
    already_translated: int = 0
    newly_translated: int = 0
    changed_count: int = 0
    failed: int = 0
    errors: List[str] = field(default_factory=list)

    @property
    def missing_before(self) -> int:
        return self.total_source - self.already_translated + self.changed_count


@dataclass
class ProcessingResult:
    """Overall processing results across all files and locales."""
    locale_results: List[LocaleResult] = field(default_factory=list)

    @property
    def total_missing_before(self) -> int:
        return sum(r.missing_before for r in self.locale_results)

    @property
    def total_translated(self) -> int:
        return sum(r.newly_translated for r in self.locale_results)

    @property
    def total_changed(self) -> int:
        return sum(r.changed_count for r in self.locale_results)

    @property
    def total_failed(self) -> int:
        return sum(r.failed for r in self.locale_results)

    @property
    def has_missing(self) -> bool:
        return (self.total_missing_before - self.total_translated) > 0

    @property
    def has_failures(self) -> bool:
        return self.total_failed > 0


# ============================================================================
# Snapshot Tracking Functions (Minimal Hash-Only)
# ============================================================================


def content_hash(text: str) -> str:
    """Generate short hash of string content for change detection."""
    return hashlib.sha256(text.encode('utf-8')).hexdigest()[:12]


def get_snapshot_path(source_xml: Path, repo_root: Path) -> Path:
    """Get snapshot file path at module level."""
    parts = source_xml.parts

    if "src" in parts:
        src_index = parts.index("src")
        module_root = Path(*parts[:src_index])
        relative_parts = parts[src_index:]
        safe_name = "_".join(relative_parts)
        return module_root / ".translation_snapshots" / f"{safe_name}.json"

    try:
        relative = source_xml.relative_to(repo_root)
        safe_name = str(relative).replace("/", "_").replace("\\", "_")
    except ValueError:
        safe_name = source_xml.name
    return repo_root / ".translation_snapshots" / f"{safe_name}.json"


def load_snapshot(snapshot_path: Path) -> Dict[str, str]:
    """Load snapshot: key -> hash mapping."""
    if not snapshot_path.exists():
        return {}
    try:
        content = snapshot_path.read_text(encoding='utf-8')
        data = json.loads(content)
        if isinstance(data, dict):
            return {str(k): str(v) for k, v in data.items()}
        return {}
    except (json.JSONDecodeError, IOError, OSError) as e:
        logger.warning(f"Failed to load snapshot {snapshot_path}: {e}")
        return {}


def save_snapshot_full(
    snapshot_path: Path, source_resources: SourceResources
) -> None:
    """Save snapshot for all resource types."""
    try:
        snapshot_path.parent.mkdir(parents=True, exist_ok=True)
        data = source_resources.all_keys_for_snapshot()
        snapshot_path.write_text(
            json.dumps(data, sort_keys=True, separators=(",", ":")),
            encoding="utf-8",
        )
    except (IOError, OSError) as e:
        logger.warning(f"Failed to save snapshot {snapshot_path}: {e}")


def find_changed_resources(
    source_resources: SourceResources,
    snapshot: Dict[str, str],
    existing_keys: ExistingKeys,
) -> List[StringEntry]:
    """
    Find ALL changed entries (strings, array items, plural items)
    returned as flat StringEntry list for translation.
    """
    changed: List[StringEntry] = []

    # Regular strings
    for entry in source_resources.strings:
        if entry.key not in snapshot:
            continue
        if entry.key not in existing_keys.strings:
            continue
        if snapshot[entry.key] != content_hash(entry.text):
            changed.append(entry)

    # String arrays
    for arr in source_resources.string_arrays:
        snap_key = f"__array__{arr.key}"
        if snap_key not in snapshot:
            continue
        if arr.key not in existing_keys.string_arrays:
            continue
        if snapshot[snap_key] != content_hash(arr.content_for_hash()):
            changed.extend(arr.flat_entries())

    # Plurals
    for plu in source_resources.plurals:
        snap_key = f"__plurals__{plu.key}"
        if snap_key not in snapshot:
            continue
        if plu.key not in existing_keys.plurals:
            continue
        if snapshot[snap_key] != content_hash(plu.content_for_hash()):
            changed.extend(plu.flat_entries())

    return changed


def _snapshot_needs_update_full(
    snapshot: Dict[str, str],
    source_resources: SourceResources,
) -> bool:
    """Check if snapshot needs update based on ALL resource types."""
    if not snapshot:
        return True

    current_data = source_resources.all_keys_for_snapshot()

    if set(current_data.keys()) != set(snapshot.keys()):
        return True

    for key, current_hash in current_data.items():
        if snapshot.get(key) != current_hash:
            return True

    return False


# ============================================================================
# Text Freezing Functions
# ============================================================================


def freeze_text(text: str, freeze_placeholders: bool = True) -> FrozenText:
    """Replace placeholders and markup tags with tokens for safe translation."""
    frozen = text
    placeholders: List[str] = []
    tags: List[str] = []

    def freeze_xliff(match: re.Match) -> str:
        tags.append(match.group(0))
        return f"[[TAG_{len(tags) - 1}]]"

    frozen = XLIFF_TAG_RE.sub(freeze_xliff, frozen)

    def freeze_tag(match: re.Match) -> str:
        tags.append(match.group(0))
        return f"[[TAG_{len(tags) - 1}]]"

    frozen = MARKUP_TAG_RE.sub(freeze_tag, frozen)

    if freeze_placeholders:
        def freeze_ph(match: re.Match) -> str:
            placeholders.append(match.group(0))
            return f"[[PH_{len(placeholders) - 1}]]"
        frozen = PLACEHOLDER_RE.sub(freeze_ph, frozen)

    return FrozenText(original=text, frozen=frozen, placeholders=placeholders, tags=tags)


# ============================================================================
# Text Sanitization Functions
# ============================================================================


def convert_html_entities_to_numeric(text: str) -> str:
    """Convert HTML named entities to XML numeric entities."""
    def replace_entity(match: re.Match) -> str:
        name = match.group(1).lower()
        return HTML_ENTITY_TO_NUMERIC.get(name, match.group(0))
    return HTML_ENTITY_PATTERN.sub(replace_entity, text)


def fix_bare_ampersands(text: str) -> str:
    """Replace bare ampersands with &amp; for XML validity."""
    return BARE_AMPERSAND_PATTERN.sub("&amp;", text)


def sanitize_for_xml_parse(text: str) -> str:
    """Prepare text for XML parsing."""
    result = convert_html_entities_to_numeric(text)
    return fix_bare_ampersands(result)


def escape_android_string(text: str) -> str:
    """Escape Android special characters in string resources."""
    if not text:
        return text
    result: List[str] = []
    i = 0
    length = len(text)
    while i < length:
        char = text[i]
        if char == '\\' and i + 1 < length:
            next_char = text[i + 1]
            if next_char in ("'", '"', '\\', 'n', 't', 'r', '@', '?'):
                result.append(char)
                result.append(next_char)
                i += 2
                continue
            if next_char == 'u' and i + 5 <= length:
                hex_chars = text[i + 2:i + 6]
                if len(hex_chars) == 4 and all(c in '0123456789abcdefABCDEF' for c in hex_chars):
                    result.append(text[i:i + 6])
                    i += 6
                    continue
        if char == "'":
            result.append("\\'")
        elif char == '@' and i == 0:
            result.append('\\@')
        elif char == '?' and i == 0:
            result.append('\\?')
        else:
            result.append(char)
        i += 1
    return ''.join(result)


def escape_android_text_nodes(element: ET._Element) -> None:
    """Recursively escape Android special characters in text and tail content."""
    if element.text:
        element.text = escape_android_string(element.text)
    for child in element:
        if not callable(child.tag):
            escape_android_text_nodes(child)
            if child.tail:
                child.tail = escape_android_string(child.tail)


def validate_allowed_tags(value: str) -> Tuple[bool, List[str]]:
    """Check if all markup tags in value are in the allowlist."""
    if not MARKUP_PATTERN.search(value):
        return True, []
    found = set(TAG_NAME_PATTERN.findall(value))
    unknown = [t for t in found if t.lower() not in ALLOWED_TAGS]
    return len(unknown) == 0, unknown


# ============================================================================
# XML Helper Functions
# ============================================================================


def is_comment(elem) -> bool:
    """Check if element is a comment (lxml comments have callable tag)."""
    return callable(elem.tag)


def get_comment_text(elem) -> str:
    """Get the text content of a comment element."""
    if is_comment(elem):
        return elem.text or ""
    return ""


def get_element_full_text(elem: ET._Element) -> str:
    """Get full text content including child elements as markup."""
    parts: List[str] = []
    if elem.text:
        parts.append(elem.text)
    for child in elem:
        if not is_comment(child):
            parts.append(ET.tostring(child, encoding="unicode"))
        if child.tail:
            parts.append(child.tail)
    return "".join(parts)


# ============================================================================
# XML Reading Functions
# ============================================================================


def read_source_resources(source_xml: Path) -> SourceResources:
    """Read ALL translatable resources from source XML."""
    tree = ET.parse(str(source_xml), parser=XML_PARSER)
    root = tree.getroot()
    resources = SourceResources()

    for node in root:
        if is_comment(node):
            continue

        # ── <string> ──────────────────────────────────────────
        if node.tag == "string":
            name = node.get("name")
            if not name:
                continue
            if node.get("translatable", "true").lower() == "false":
                continue
            raw_text = get_element_full_text(node)
            if not raw_text or not raw_text.strip():
                continue
            preserved = _extract_propagated_attrs(node)
            resources.strings.append(
                StringEntry(key=name, text=raw_text, attributes=preserved)
            )

        # ── <string-array> ────────────────────────────────────
        elif node.tag == "string-array":
            name = node.get("name")
            if not name:
                continue
            if node.get("translatable", "true").lower() == "false":
                continue
            items: List[str] = []
            for item_node in node.iter("item"):
                item_text = get_element_full_text(item_node)
                items.append(item_text or "")
            if not any(t.strip() for t in items):
                continue
            preserved = _extract_propagated_attrs(node)
            resources.string_arrays.append(
                StringArrayEntry(key=name, items=items, attributes=preserved)
            )

        # ── <plurals> ─────────────────────────────────────────
        elif node.tag == "plurals":
            name = node.get("name")
            if not name:
                continue
            if node.get("translatable", "true").lower() == "false":
                continue
            quantity_map: Dict[str, str] = {}
            for item_node in node.iter("item"):
                quantity = item_node.get("quantity")
                if quantity:
                    item_text = get_element_full_text(item_node)
                    if item_text:
                        quantity_map[quantity] = item_text
            if not quantity_map:
                continue
            preserved = _extract_propagated_attrs(node)
            resources.plurals.append(
                PluralsEntry(key=name, items=quantity_map, attributes=preserved)
            )

    return resources


def _extract_propagated_attrs(node: ET._Element) -> Dict[str, str]:
    """Extract attributes to propagate from a source node."""
    preserved: Dict[str, str] = {}
    for attr_key, attr_val in node.attrib.items():
        if attr_key in ("name", "translatable"):
            continue
        if attr_key in PROPAGATE_ATTRIBUTES:
            preserved[attr_key] = attr_val
        elif attr_key.startswith(f"{{{TOOLS_NAMESPACE}}}"):
            preserved[attr_key] = attr_val
    return preserved


def read_existing_keys_full(target_xml: Path) -> ExistingKeys:
    """Read existing resource keys from target file (all types)."""
    result = ExistingKeys()
    if not target_xml.exists():
        return result
    try:
        tree = ET.parse(str(target_xml), parser=XML_PARSER)
        root = tree.getroot()
        result.strings = set(root.xpath("./string/@name"))
        result.string_arrays = set(root.xpath("./string-array/@name"))
        result.plurals = set(root.xpath("./plurals/@name"))
        return result
    except ET.XMLSyntaxError:
        return result

# ============================================================================
# XML Writing Functions
# ============================================================================


def set_mixed_string_value(
    node: ET._Element,
    value: str,
    key: Optional[str] = None,
    warn_unknown_tags: bool = True,
) -> None:
    """Set string node value, preserving embedded markup."""
    node.text = None
    for child in list(node):
        node.remove(child)

    key_prefix = f"[{key}] " if key else ""

    if warn_unknown_tags and MARKUP_PATTERN.search(value):
        is_valid, unknown = validate_allowed_tags(value)
        if not is_valid:
            logger.warning(f"{key_prefix}Unknown tags (may not render): {unknown}")

    if not MARKUP_PATTERN.search(value):
        converted = convert_html_entities_to_numeric(value)
        node.text = escape_android_string(converted)
        return

    sanitized = sanitize_for_xml_parse(value)
    wrapped = f"<_root xmlns:xliff='{XLIFF_NAMESPACE}'>{sanitized}</_root>"

    try:
        fragment = ET.fromstring(wrapped.encode('utf-8'))
    except ET.XMLSyntaxError as e:
        logger.warning(f"{key_prefix}XML parse failed, using plain text: {e}")
        fallback = convert_html_entities_to_numeric(value)
        node.text = escape_android_string(fallback)
        return

    node.text = fragment.text
    for child in list(fragment):
        fragment.remove(child)
        node.append(child)

    escape_android_text_nodes(node)


def write_translations_full(
    target_xml: Path,
    translations: Dict[str, str],  # flat key -> translated text
    source_resources: SourceResources,
    source_xml: Path,
    validate: bool = True,
    warn_unknown_tags: bool = True,
) -> int:
    """
    Write translations including string-arrays and plurals.

    The `translations` dict uses flat keys:
      - "key"                  -> string translation
      - "key__item_0"          -> string-array item
      - "key__plural_one"      -> plurals quantity variant
    """
    target_xml.parent.mkdir(parents=True, exist_ok=True)

    source_tree = ET.parse(str(source_xml), parser=XML_PARSER)
    source_root = source_tree.getroot()

    if target_xml.exists():
        try:
            existing_tree = ET.parse(str(target_xml), parser=XML_PARSER)
            existing_root = existing_tree.getroot()
            return _merge_all_into_existing(
                target_xml, existing_root, translations,
                source_resources, source_root, validate, warn_unknown_tags
            )
        except ET.XMLSyntaxError as e:
            logger.warning(f"Corrupted '{target_xml}', recreating: {e}")

    return _create_from_source_full(
        target_xml, translations, source_resources,
        source_root, validate, warn_unknown_tags
    )


def _cleanup_orphaned_translations(
    target_xml: Path,
    source_resources: SourceResources,
) -> int:
    """
    Remove entries from target file that no longer exist in source.
    Returns count of removed entries.
    """
    if not target_xml.exists():
        return 0

    try:
        tree = ET.parse(str(target_xml), parser=XML_PARSER)
        root = tree.getroot()
    except ET.XMLSyntaxError:
        return 0

    source_string_keys: Set[str] = {e.key for e in source_resources.strings}
    source_array_keys: Set[str] = {a.key for a in source_resources.string_arrays}
    source_plural_keys: Set[str] = {p.key for p in source_resources.plurals}


    elements_to_remove: List[ET._Element] = []
    removed_names: List[str] = []

    for elem in list(root):
        if is_comment(elem):
            continue

        name = elem.get("name")
        if not name:
            continue

        if elem.tag == "string":
            if name not in source_string_keys:
                elements_to_remove.append(elem)
                removed_names.append(f"string:{name}")

        elif elem.tag == "string-array":
            if name not in source_array_keys:
                elements_to_remove.append(elem)
                removed_names.append(f"string-array:{name}")

        elif elem.tag == "plurals":
            if name not in source_plural_keys:
                elements_to_remove.append(elem)
                removed_names.append(f"plurals:{name}")

    if not elements_to_remove:
        return 0

    for elem in elements_to_remove:
        _remove_element_and_orphaned_comments(root, elem)

    _normalize_resource_whitespace(root)

    children = list(root)
    if children:
        for child in reversed(children):
            if not is_comment(child):
                if not child.tail or not child.tail.endswith("\n"):
                    child.tail = "\n"
                break

    ET.cleanup_namespaces(root)
    tree = ET.ElementTree(root)
    tree.write(
        str(target_xml),
        encoding="utf-8",
        xml_declaration=True,
        pretty_print=False,
    )
    _fix_xliff_namespaces_in_file(target_xml)

    for name in removed_names:
        logger.info(f"    ✕ Removed orphaned: {name}")

    return len(elements_to_remove)

def _remove_element_and_orphaned_comments(
    root: ET._Element, elem: ET._Element
) -> None:
    """
    Remove element AND any preceding comments that would become orphaned.

    Example: if removing the last string under <!-- Section --> comment,
    remove the comment too.
    """
    parent = elem.getparent()
    if parent is None:
        return

    prev = elem.getprevious()

    _remove_element_preserve_whitespace(root, elem)

    if prev is not None and is_comment(prev):
        next_sibling = prev.getnext()
        if next_sibling is None or is_comment(next_sibling):
            _remove_element_preserve_whitespace(root, prev)

def _create_from_source_full(
    target_xml: Path,
    translations: Dict[str, str],
    source_resources: SourceResources,
    source_root: ET._Element,
    validate: bool,
    warn_unknown_tags: bool,
) -> int:
    """Create new file from source, filling in all resource types."""
    root = copy.deepcopy(source_root)

    # Build lookup sets
    translated_string_keys: Set[str] = set()
    translated_array_keys: Set[str] = set()
    translated_plural_keys: Set[str] = set()

    for flat_key in translations:
        if "__item_" in flat_key:
            base_key = flat_key.rsplit("__item_", 1)[0]
            translated_array_keys.add(base_key)
        elif "__plural_" in flat_key:
            base_key = flat_key.rsplit("__plural_", 1)[0]
            translated_plural_keys.add(base_key)
        else:
            translated_string_keys.add(flat_key)

    elements_to_remove: List[ET._Element] = []
    written = 0

    for elem in list(root):
        if is_comment(elem):
            continue

        name = elem.get("name")
        if not name:
            continue

        if elem.get("translatable", "true").lower() == "false":
            continue

        if elem.tag == "string":
            if name in translated_string_keys:
                value = translations[name]
                elem.text = None
                for child in list(elem):
                    elem.remove(child)
                set_mixed_string_value(
                    elem, value, key=name,
                    warn_unknown_tags=warn_unknown_tags,
                )
                written += 1
            else:
                elements_to_remove.append(elem)

        elif elem.tag == "string-array":
            if name in translated_array_keys:
                item_nodes = list(elem.iter("item"))
                for i, item_node in enumerate(item_nodes):
                    flat_key = f"{name}__item_{i}"
                    if flat_key in translations:
                        value = translations[flat_key]
                        item_node.text = None
                        for child in list(item_node):
                            item_node.remove(child)
                        set_mixed_string_value(
                            item_node, value, key=flat_key,
                            warn_unknown_tags=warn_unknown_tags,
                        )
                        written += 1
            else:
                elements_to_remove.append(elem)

        elif elem.tag == "plurals":
            if name in translated_plural_keys:
                for item_node in elem.iter("item"):
                    quantity = item_node.get("quantity")
                    if quantity:
                        flat_key = f"{name}__plural_{quantity}"
                        if flat_key in translations:
                            value = translations[flat_key]
                            item_node.text = None
                            for child in list(item_node):
                                item_node.remove(child)
                            set_mixed_string_value(
                                item_node, value, key=flat_key,
                                warn_unknown_tags=warn_unknown_tags,
                            )
                            written += 1
            else:
                elements_to_remove.append(elem)

    for elem in elements_to_remove:
        _remove_element_preserve_whitespace(root, elem)

    _normalize_resource_whitespace(root)

    ET.cleanup_namespaces(root)
    tree = ET.ElementTree(root)
    tree.write(str(target_xml), encoding="utf-8",
               xml_declaration=True, pretty_print=False)
    _fix_xliff_namespaces_in_file(target_xml)

    if validate:
        try:
            ET.parse(str(target_xml), parser=XML_PARSER)
        except ET.XMLSyntaxError as e:
            raise XmlWriteError(f"Written file is malformed: {target_xml}: {e}")

    return written


def _fix_xliff_namespaces_in_file(target_xml: Path) -> None:
    """
    Post-process the written XML file to fix xliff namespace issues and formatting.

    lxml may generate auto-prefixed namespaces (ns0, ns1, etc.) instead of
    using the proper 'xliff' prefix. This function:
    - Fixes XML declaration to use double quotes and lowercase encoding
    - Adds copyright header if missing
    - Replaces ns#: prefixes with xliff: for XLIFF namespace
    - Removes inline xmlns:ns# declarations for XLIFF
    - Ensures xliff namespace is declared at root level
    """
    content = target_xml.read_text(encoding='utf-8')
    original_content = content

    # Fix XML declaration: single quotes to double quotes, uppercase to lowercase
    content = re.sub(
        r"<\?xml version='1\.0' encoding='UTF-8'\?>",
        '<?xml version="1.0" encoding="utf-8"?>',
        content
    )

    content = re.sub(
            r'(-->)\s*(<resources)',
            r'\1\n\2',
            content
        )

    content = re.sub(
            r'(\?>)\s*(<resources)',
            r'\1\n\2',
            content
        )

    # Copyright header template
    copyright_header = '''<!--
    Copyright 2026 Mifos Initiative

    This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
    If a copy of the MPL was not distributed with this file,
    You can obtain one at https://mozilla.org/MPL/2.0/.

    See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
-->'''

    if '<!-- ' not in content or 'Copyright' not in content:
        content = re.sub(
            r'(<\?xml[^?]*\?>)\s*(<resources)',
            rf'\1\n{copyright_header}\n\2',
            content
        )

    ns_pattern = re.compile(r'xmlns:(ns\d+)="urn:oasis:names:tc:xliff:document:1\.2"')
    ns_matches = ns_pattern.findall(content)

    for ns_prefix in set(ns_matches):
        content = content.replace(f'<{ns_prefix}:', '<xliff:')
        content = content.replace(f'</{ns_prefix}:', '</xliff:')
        content = re.sub(
            rf'\s*xmlns:{ns_prefix}="urn:oasis:names:tc:xliff:document:1\.2"',
            '',
            content
        )

    if 'xliff:' in content and 'xmlns:xliff=' not in content:
        content = content.replace(
            '<resources',
            '<resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2"',
            1
        )

    if content != original_content:
        target_xml.write_text(content, encoding='utf-8')


def _remove_element_preserve_whitespace(root: ET._Element, elem: ET._Element) -> None:
    """Remove element and clean up associated whitespace to prevent empty line buildup."""
    parent = elem.getparent()
    if parent is None:
        return

    prev = elem.getprevious()
    next_sib = elem.getnext()

    if prev is not None:
        if next_sib is not None:
            prev.tail = "\n    "
        else:
            prev.tail = "\n"
    else:
        if next_sib is not None:
            parent.text = "\n    "
        else:
            parent.text = "\n"

    parent.remove(elem)


def _normalize_resource_whitespace(root: ET._Element) -> None:
    """Collapse multiple consecutive blank lines in element tail/text whitespace."""
    if root.text:
        root.text = re.sub(r'\n{3,}', '\n\n', root.text)
    for child in root:
        if child.tail:
            child.tail = re.sub(r'\n{3,}', '\n\n', child.tail)


def _merge_all_into_existing(
    target_xml: Path,
    existing_root: ET._Element,
    translations: Dict[str, str],
    source_resources: SourceResources,
    source_root: ET._Element,
    validate: bool,
    warn_unknown_tags: bool,
) -> int:
    """
    Merge new/updated translations into existing file for ALL resource types.

    Handles:
    - <string> entries (new + updated)
    - <string-array> entries (new + updated items)
    - <plurals> entries (new + updated quantities)
    """
    # ── Build lookup maps ──────────────────────────────────────

    array_items_map: Dict[str, Tuple[str, int]] = {}
    plural_items_map: Dict[str, Tuple[str, str]] = {}
    string_keys: Set[str] = set()

    for flat_key in translations:
        if "__item_" in flat_key:
            parts = flat_key.rsplit("__item_", 1)
            array_items_map[flat_key] = (parts[0], int(parts[1]))
        elif "__plural_" in flat_key:
            parts = flat_key.rsplit("__plural_", 1)
            plural_items_map[flat_key] = (parts[0], parts[1])
        else:
            string_keys.add(flat_key)

    array_translations: Dict[str, Dict[int, str]] = {}
    for flat_key, (arr_name, idx) in array_items_map.items():
        if arr_name not in array_translations:
            array_translations[arr_name] = {}
        array_translations[arr_name][idx] = translations[flat_key]

    plural_translations: Dict[str, Dict[str, str]] = {}
    for flat_key, (plu_name, quantity) in plural_items_map.items():
        if plu_name not in plural_translations:
            plural_translations[plu_name] = {}
        plural_translations[plu_name][quantity] = translations[flat_key]

    # ── Get existing elements ──────────────────────────────────

    existing_string_elems: Dict[str, ET._Element] = {}
    existing_array_elems: Dict[str, ET._Element] = {}
    existing_plural_elems: Dict[str, ET._Element] = {}

    for elem in existing_root:
        if is_comment(elem):
            continue
        name = elem.get("name")
        if not name:
            continue
        if elem.tag == "string":
            existing_string_elems[name] = elem
        elif elem.tag == "string-array":
            existing_array_elems[name] = elem
        elif elem.tag == "plurals":
            existing_plural_elems[name] = elem

    # ── Build source ordering ──────────────────────────────────

    source_order: List[Tuple[str, str]] = []
    source_comments: Dict[int, List[str]] = {}
    current_comments: List[str] = []

    for elem in source_root:
        if is_comment(elem):
            current_comments.append(elem.text or "")
            continue
        name = elem.get("name")
        if name and elem.tag in ("string", "string-array", "plurals"):
            idx = len(source_order)
            if current_comments:
                source_comments[idx] = list(current_comments)
                current_comments = []
            source_order.append((elem.tag, name))

    # ── Source entry map for attributes ────────────────────────

    entry_map = {e.key: e for e in source_resources.strings}
    array_entry_map = {a.key: a for a in source_resources.string_arrays}
    plural_entry_map = {p.key: p for p in source_resources.plurals}

    written = 0

    # ── 1. Process regular strings ─────────────────────────────

    for key in string_keys:
        value = translations[key]
        entry = entry_map.get(key)

        if key in existing_string_elems:
            node = existing_string_elems[key]
            node.text = None
            for child in list(node):
                node.remove(child)
            set_mixed_string_value(node, value, key=key, warn_unknown_tags=warn_unknown_tags)
            written += 1
        elif entry:
            attrs = entry.get_propagated_attributes()
            node = ET.Element("string", **attrs)
            set_mixed_string_value(node, value, key=key, warn_unknown_tags=warn_unknown_tags)
            node.tail = "\n    "
            _insert_at_source_position(
                existing_root, node, "string", key,
                source_order, existing_string_elems,
                existing_array_elems, existing_plural_elems,
            )
            existing_string_elems[key] = node
            written += 1

    # ── 2. Process string-arrays ───────────────────────────────

    for arr_name, item_translations in array_translations.items():
        arr_entry = array_entry_map.get(arr_name)
        if not arr_entry:
            continue

        if arr_name in existing_array_elems:
            arr_elem = existing_array_elems[arr_name]
            item_nodes = list(arr_elem.iter("item"))

            for idx, value in item_translations.items():
                if idx < len(item_nodes):
                    item_node = item_nodes[idx]
                    item_node.text = None
                    for child in list(item_node):
                        item_node.remove(child)
                    set_mixed_string_value(
                        item_node, value,
                        key=f"{arr_name}[{idx}]",
                        warn_unknown_tags=warn_unknown_tags,
                    )
                    written += 1
        else:
            source_arr_elem = None
            for elem in source_root:
                if elem.tag == "string-array" and elem.get("name") == arr_name:
                    source_arr_elem = elem
                    break

            if source_arr_elem is not None:
                new_arr = copy.deepcopy(source_arr_elem)
                item_nodes = list(new_arr.iter("item"))

                for idx, value in item_translations.items():
                    if idx < len(item_nodes):
                        item_node = item_nodes[idx]
                        item_node.text = None
                        for child in list(item_node):
                            item_node.remove(child)
                        set_mixed_string_value(
                            item_node, value,
                            key=f"{arr_name}[{idx}]",
                            warn_unknown_tags=warn_unknown_tags,
                        )
                        written += 1

                new_arr.tail = "\n\n    "
                _insert_at_source_position(
                    existing_root, new_arr, "string-array", arr_name,
                    source_order, existing_string_elems,
                    existing_array_elems, existing_plural_elems,
                )
                existing_array_elems[arr_name] = new_arr

    # ── 3. Process plurals ─────────────────────────────────────

    for plu_name, qty_translations in plural_translations.items():
        plu_entry = plural_entry_map.get(plu_name)
        if not plu_entry:
            continue

        if plu_name in existing_plural_elems:
            plu_elem = existing_plural_elems[plu_name]

            for item_node in plu_elem.iter("item"):
                quantity = item_node.get("quantity")
                if quantity and quantity in qty_translations:
                    value = qty_translations[quantity]
                    item_node.text = None
                    for child in list(item_node):
                        item_node.remove(child)
                    set_mixed_string_value(
                        item_node, value,
                        key=f"{plu_name}[{quantity}]",
                        warn_unknown_tags=warn_unknown_tags,
                    )
                    written += 1
        else:
            source_plu_elem = None
            for elem in source_root:
                if elem.tag == "plurals" and elem.get("name") == plu_name:
                    source_plu_elem = elem
                    break

            if source_plu_elem is not None:
                new_plu = copy.deepcopy(source_plu_elem)

                for item_node in new_plu.iter("item"):
                    quantity = item_node.get("quantity")
                    if quantity and quantity in qty_translations:
                        value = qty_translations[quantity]
                        item_node.text = None
                        for child in list(item_node):
                            item_node.remove(child)
                        set_mixed_string_value(
                            item_node, value,
                            key=f"{plu_name}[{quantity}]",
                            warn_unknown_tags=warn_unknown_tags,
                        )
                        written += 1

                new_plu.tail = "\n\n    "
                _insert_at_source_position(
                    existing_root, new_plu, "plurals", plu_name,
                    source_order, existing_string_elems,
                    existing_array_elems, existing_plural_elems,
                )
                existing_plural_elems[plu_name] = new_plu

    # ── Fix final element tail ─────────────────────────────────

    children = list(existing_root)
    if children:
        for child in reversed(children):
            if not is_comment(child):
                if not child.tail or not child.tail.endswith("\n"):
                    child.tail = "\n"
                break

    # ── Write file ─────────────────────────────────────────────

    if written > 0:
        ET.cleanup_namespaces(existing_root)
        tree = ET.ElementTree(existing_root)
        tree.write(
            str(target_xml),
            encoding="utf-8",
            xml_declaration=True,
            pretty_print=False,
        )
        _fix_xliff_namespaces_in_file(target_xml)

        if validate:
            try:
                ET.parse(str(target_xml), parser=XML_PARSER)
            except ET.XMLSyntaxError as e:
                raise XmlWriteError(f"Written file is malformed: {target_xml}: {e}")

    return written


def _insert_at_source_position(
    root: ET._Element,
    new_elem: ET._Element,
    tag: str,
    name: str,
    source_order: List[Tuple[str, str]],
    existing_strings: Dict[str, ET._Element],
    existing_arrays: Dict[str, ET._Element],
    existing_plurals: Dict[str, ET._Element],
) -> None:
    """
    Insert element at the correct position matching source file ordering.
    Falls back to appending at end if no reference point found.
    """
    try:
        my_idx = next(
            i for i, (t, n) in enumerate(source_order)
            if t == tag and n == name
        )
    except StopIteration:
        root.append(new_elem)
        return

    for future_tag, future_name in source_order[my_idx + 1:]:
        ref_elem = None
        if future_tag == "string":
            ref_elem = existing_strings.get(future_name)
        elif future_tag == "string-array":
            ref_elem = existing_arrays.get(future_name)
        elif future_tag == "plurals":
            ref_elem = existing_plurals.get(future_name)

        if ref_elem is not None:
            new_elem.tail = "\n    "
            ref_elem.addprevious(new_elem)
            return

    for past_tag, past_name in reversed(source_order[:my_idx]):
        ref_elem = None
        if past_tag == "string":
            ref_elem = existing_strings.get(past_name)
        elif past_tag == "string-array":
            ref_elem = existing_arrays.get(past_name)
        elif past_tag == "plurals":
            ref_elem = existing_plurals.get(past_name)

        if ref_elem is not None:
            new_elem.tail = ref_elem.tail
            ref_elem.tail = "\n    "
            ref_elem.addnext(new_elem)
            return

    _append_with_indent(root, new_elem)

def _append_with_indent(root: ET._Element, new_elem: ET._Element) -> None:
    """Append element to root with proper indentation."""
    children = list(root)
    if children:
        last_child = None
        for child in reversed(children):
            if not is_comment(child):
                last_child = child
                break

        if last_child is not None:
            new_elem.tail = last_child.tail
            last_child.tail = "\n    "
        else:
            new_elem.tail = "\n"
    else:
        # First child
        if not root.text or not root.text.strip() == "":
            root.text = "\n    "
        new_elem.tail = "\n"

    root.append(new_elem)

# ============================================================================
# File Discovery
# ============================================================================


def find_source_files(repo_root: Path, exclude_dirs: FrozenSet[str]) -> List[Path]:
    """Find all source strings.xml files in the repository."""
    paths: List[Path] = []
    resource_filenames = ("strings.xml", "arrays.xml")
    patterns = []

    for fname in resource_filenames:
            patterns.append(f"src/*/res/values/{fname}")
            patterns.append(f"src/*/composeResources/values/{fname}")

    for pat in patterns:
        for p in repo_root.rglob(pat):
            if any(part in exclude_dirs for part in p.parts):
                continue
            if "src" in p.parts:
                src_set = p.parts[p.parts.index("src") + 1]
                if src_set in ("test", "androidTest"):
                    continue
            paths.append(p)
    return sorted(set(paths))


def get_target_path(source_xml: Path, locale: str) -> Path:
    """Get target path for a locale based on source path."""
    if '/' in locale or '\\' in locale or '..' in locale:
        raise ValueError(f"Invalid locale: {locale}")
    values_dir = source_xml.parent
    parent = values_dir.parent
    return parent / f"values-{locale}" / source_xml.name


def get_module_name(source_path: Path) -> str:
    """Extract module name from source strings.xml path."""
    parts = source_path.parts
    src_indexes = [i for i, p in enumerate(parts) if p == "src"]
    if src_indexes:
        first_src_index = src_indexes[0]
        if first_src_index > 0:
            return parts[first_src_index - 1]
    if len(source_path.parents) > 4:
        return source_path.parents[4].name
    return "unknown"


# ============================================================================
# Translation API
# ============================================================================


class GeminiTranslator:
    """Translator using Google Gemini API with retry and rate limiting."""

    SYSTEM_PROMPT = """You are a professional translator for a mobile finance/ banking Android app.
Translate UI strings accurately and naturally for the target locale.

CRITICAL RULES — FOLLOW EXACTLY:

1. OUTPUT FORMAT:
   - Return ONLY a valid JSON object mapping keys to translated strings.
   - Do NOT include markdown, code blocks, backticks, or any commentary.

2. PLACEHOLDER TOKENS (e.g., [[PH_0]], [[PH_1]]):
   - Preserve ALL placeholder tokens EXACTLY as written.
   - Do NOT translate, modify, reorder, or remove any [[PH_N]] tokens.

3. TAG TOKENS (e.g., [[TAG_0]], [[TAG_1]]):
   - Preserve ALL tag tokens EXACTLY as written.
   - Keep tokens in the SAME ORDER as the original.

4. KEYS:
   - Every input key must appear exactly once in the output.
   - Keys may contain __item_N (array items) or __plural_QUANTITY
   - (plural forms) suffixes — translate the TEXT only, never the key.

5. PLURALS: For __plural_one, __plural_other, __plural_few, etc.,
   use the grammatically correct plural form for the target language.

5. WHITESPACE:
   - Preserve leading and trailing spaces if present in the original.

6. SPECIAL CHARACTERS:
   - Preserve newline characters (\\n) exactly as they appear.
   - Do not add or remove any escape sequences.

Example input:
{"items": [{"key": "greeting", "text": "Hello [[PH_0]]!"}]}

Example output:
{"greeting": "مرحبا [[PH_0]]!"}"""

    def __init__(self, config: Config):
        self.config = config
        self.client = genai.Client(api_key=config.api_key)
        self._last_request_time = 0.0

    def _rate_limit(self) -> None:
        """Enforce minimum delay between API requests."""
        elapsed = time.time() - self._last_request_time
        if elapsed < self.config.request_delay:
            time.sleep(self.config.request_delay - elapsed)
        self._last_request_time = time.time()

    def translate_batch(
        self,
        locale: str,
        items: List[Tuple[str, FrozenText]],
    ) -> Dict[str, str]:
        """Translate a batch of frozen texts with retry logic."""
        self._rate_limit()

        payload = {
            "target_locale": locale,
            "items": [{"key": k, "text": ft.frozen} for k, ft in items],
        }

        model_lower = self.config.model.lower()
        supports_system_instruction = not any(
            x in model_lower for x in ["gemma", "embedding", "aqa"]
        )

        if supports_system_instruction:
            user_prompt = (
                f"Translate all items from English to '{locale}'.\n"
                f"Return a JSON object mapping each key to its translation.\n"
                f"Preserve all [[PH_N]] and [[TAG_N]] tokens in the SAME ORDER.\n\n"
                f"Input:\n{json.dumps(payload, ensure_ascii=False, indent=2)}"
            )
            gen_config = types.GenerateContentConfig(
                system_instruction=self.SYSTEM_PROMPT,
                temperature=0.1,
                response_mime_type="application/json",
                max_output_tokens=4096,
            )
        else:
            user_prompt = f"""{self.SYSTEM_PROMPT}

---

Now translate all items from English to '{locale}'.
Return a JSON object mapping each key to its translation.

Input:
{json.dumps(payload, ensure_ascii=False, indent=2)}

Output (JSON only, no markdown):"""

            gen_config = types.GenerateContentConfig(
                temperature=0.1,
                max_output_tokens=4096,
            )

        last_error: Optional[Exception] = None
        requested_keys = {k for k, _ in items}

        for attempt in range(1, self.config.max_retries + 1):
            try:
                response = self.client.models.generate_content(
                    model=self.config.model,
                    contents=user_prompt,
                    config=gen_config,
                )

                text = (response.text or "").strip()

                if text.startswith("```"):
                    lines = text.split("\n")
                    if lines[0].startswith("```"):
                        lines = lines[1:]
                    if lines and lines[-1].strip() == "```":
                        lines = lines[:-1]
                    text = "\n".join(lines).strip()

                data = json.loads(text)

                if not isinstance(data, dict):
                    raise TranslationError("Response is not a JSON object")

                returned_keys = set(data.keys())
                missing_keys = requested_keys - returned_keys

                if missing_keys:
                    logger.warning(f"API omitted {len(missing_keys)} key(s)")

                return {str(k): str(v) for k, v in data.items() if k in requested_keys}

            except json.JSONDecodeError as e:
                last_error = TranslationError(f"Invalid JSON: {e}")
            except Exception as e:
                last_error = e
                error_str = str(e).lower()

                is_rate_limited = "429" in str(e) or "resource_exhausted" in error_str
                is_overloaded = (
                    "503" in str(e) or
                    "unavailable" in error_str or
                    "overloaded" in error_str
                )

                if is_rate_limited or is_overloaded:
                    match = re.search(r"retry in (\d+(?:\.\d+)?)", str(e), re.IGNORECASE)

                    if match:
                        delay = float(match.group(1)) + 5
                    elif is_overloaded:
                        delay = 30.0 * (1.5 ** (attempt - 1))
                        delay = min(delay, 180.0)
                    else:
                        delay = 60.0

                    error_type = "Rate limited" if is_rate_limited else "Model overloaded"
                    logger.warning(
                        f"{error_type}! Waiting {delay:.0f}s before retry "
                        f"{attempt}/{self.config.max_retries}..."
                    )
                    time.sleep(delay)
                    continue

            delay = self.config.base_retry_delay * (2 ** (attempt - 1))
            logger.warning(
                f"Attempt {attempt}/{self.config.max_retries} failed: {last_error}. "
                f"Retrying in {delay:.1f}s..."
            )
            time.sleep(delay)

        raise TranslationError(
            f"Failed after {self.config.max_retries} attempts: {last_error}"
        )

    def translate_single(
        self,
        locale: str,
        key: str,
        frozen_text: FrozenText,
    ) -> Optional[str]:
        """Translate a single string. Returns None on failure."""
        try:
            result = self.translate_batch(locale, [(key, frozen_text)])
            return result.get(key)
        except TranslationError as e:
            logger.error(f"Failed to translate '{key}': {e}")
            return None


# ============================================================================
# Main Processing Logic
# ============================================================================


def create_batches(
    items: List[Tuple[str, FrozenText]],
    batch_size: int,
) -> List[List[Tuple[str, FrozenText]]]:
    """Split items into batches of specified size."""
    return [items[i:i + batch_size] for i in range(0, len(items), batch_size)]


def process_locale(
    source_xml: Path,
    locale: str,
    config: Config,
    translator: Optional[GeminiTranslator],
    snapshot: Dict[str, str],
    source_resources: SourceResources,
) -> LocaleResult:
    """Process translations for a single source file and locale."""
    target_xml = get_target_path(source_xml, locale)
    result = LocaleResult(locale=locale, source_path=source_xml, target_path=target_xml)

    result.total_source = source_resources.total_count


    if source_resources.is_empty:
        logger.warning(f"No translatable strings in {source_xml}")
        return result

    if config.mode == "apply":
        removed_count = _cleanup_orphaned_translations(target_xml, source_resources)
        if removed_count > 0:
            logger.info(f"  [{locale}] Removed {removed_count} orphaned translation(s)")

    all_flat = source_resources.all_flat_entries()
    all_flat_keys = {e.key for e in all_flat}

    existing = read_existing_keys_full(target_xml)

    existing_flat_keys: Set[str] = set(existing.strings)

    for arr in source_resources.string_arrays:
        if arr.key in existing.string_arrays:
            for fe in arr.flat_entries():
                existing_flat_keys.add(fe.key)
    for plu in source_resources.plurals:
        if plu.key in existing.plurals:
            for fe in plu.flat_entries():
                existing_flat_keys.add(fe.key)

    result.already_translated = len(existing_flat_keys & all_flat_keys)

    missing_entries  = [e for e in all_flat if e.key not in existing_flat_keys]

    changed_entries  = find_changed_resources(source_resources, snapshot, existing)
    result.changed_count = len(changed_entries )

    entries_to_translate = missing_entries + changed_entries

    if not entries_to_translate:
        logger.info(f"  [{locale}] All {result.total_source} items up to date")
        return result

    log_parts = []
    if missing_entries:
        log_parts.append(f"{len(missing_entries)} new")
    if changed_entries:
        log_parts.append(f"{len(changed_entries)} changed")

    logger.info(
        f"  [{locale}] {len(entries_to_translate)} of {result.total_source} "
        f"strings need translation ({', '.join(log_parts)})"
    )

    if changed_entries and config.mode == "apply":
        for entry in changed_entries:
            logger.info(f"    ↻ {entry.key} (source text changed)")

    if config.mode == "check" or translator is None:
        return result

    frozen_map: Dict[str, FrozenText] = {}
    for entry in entries_to_translate:
        frozen_map[entry.key] = freeze_text(
            entry.text,
            freeze_placeholders=entry.is_formatted
        )

    translations: Dict[str, str] = {}
    items = [(e.key, frozen_map[e.key]) for e in entries_to_translate]
    batches = create_batches(items, config.batch_size)

    for batch_idx, batch in enumerate(batches, 1):
        logger.info(f"    Batch {batch_idx}/{len(batches)} ({len(batch)} strings)")

        try:
            batch_result = translator.translate_batch(locale, batch)
            logger.debug(f"    Batch {batch_idx} completed successfully")
        except TranslationError as e:
            logger.error(f"    Batch {batch_idx} failed: {e}")
            result.errors.append(f"Batch {batch_idx} failed: {e}")
            batch_result = {}

        for key, frozen_text in batch:
            translated_frozen = batch_result.get(key)

            if translated_frozen is None:
                logger.warning(f"    [{key}] Missing from batch, retrying individually")
                translated_frozen = translator.translate_single(locale, key, frozen_text)

            if translated_frozen is None:
                logger.error(f"    [{key}] Translation failed")
                result.failed += 1
                result.errors.append(f"Translation failed for '{key}'")
                continue

            is_valid, errors = frozen_text.validate(translated_frozen)

            if not is_valid:
                logger.warning(f"    [{key}] Validation failed: {errors}")
                retry_result = translator.translate_single(locale, key, frozen_text)
                if retry_result:
                    is_valid, errors = frozen_text.validate(retry_result)
                    if is_valid:
                        translated_frozen = retry_result
                        logger.info(f"    [{key}] Retry succeeded")

            if is_valid:
                translations[key] = frozen_text.unfreeze(translated_frozen)
            else:
                logger.error(f"    [{key}] Skipped - validation failed: {errors}")
                result.failed += 1
                result.errors.append(f"Validation failed for '{key}': {errors}")

    if translations:
        try:
            written = write_translations_full(
                target_xml=target_xml,
                translations=translations,
                source_resources=source_resources,
                source_xml=source_xml,
                validate=config.validate_output,
                warn_unknown_tags=config.warn_unknown_tags,
            )
            result.newly_translated = written
            logger.info(f"  [{locale}] Wrote {written} translations")
        except XmlWriteError as e:
            logger.error(f"  [{locale}] Write failed: {e}")
            result.errors.append(str(e))

    return result


def process_all(config: Config) -> ProcessingResult:
    """Process all source files for all configured locales."""
    result = ProcessingResult()
    sources = find_source_files(config.repo_root, config.exclude_dirs)

    if not sources:
        logger.error("No source strings.xml files found")
        return result

    logger.info(f"Found {len(sources)} source file(s)")

    translator: Optional[GeminiTranslator] = None
    if config.mode == "apply":
        translator = GeminiTranslator(config)

    for source_xml in sources:
        logger.info(f"\nProcessing: {source_xml}")

        snapshot_path = get_snapshot_path(source_xml, config.repo_root)
        snapshot = load_snapshot(snapshot_path)

        source_resources = read_source_resources(source_xml)

        snapshot_needs_update = _snapshot_needs_update_full(
            snapshot, source_resources
        )

        if snapshot_needs_update and snapshot:
            logger.debug(f"  Source strings changed since last snapshot")

        source_had_translations = False

        for locale in config.locales:
            locale_result = process_locale(
                source_xml, locale, config, translator, snapshot, source_resources
            )
            result.locale_results.append(locale_result)

            if locale_result.newly_translated > 0:
                source_had_translations = True

        should_save_snapshot = False
        save_reason = ""

        if config.mode == "apply":
            if source_had_translations:
                should_save_snapshot = True
                save_reason = "Updated"
            elif snapshot_needs_update:
                should_save_snapshot = True
                save_reason = "Synced"

        if should_save_snapshot:
            save_snapshot_full(snapshot_path, source_resources)
            logger.info(f"  {save_reason} snapshot: {snapshot_path.name}")

    return result


# ============================================================================
# Summary & CLI
# ============================================================================


def print_summary(result: ProcessingResult, mode: str) -> None:
    """Print formatted processing summary."""
    print("\n" + "=" * 70)
    print("SUMMARY")
    print("=" * 70)

    if not result.locale_results:
        print("\n  No results to display.")
        print("=" * 70)
        return

    by_source: Dict[Path, List[LocaleResult]] = {}
    for lr in result.locale_results:
        by_source.setdefault(lr.source_path, []).append(lr)

    for source_path, locale_results in sorted(by_source.items()):
        module_name = get_module_name(source_path)
        print(f"\n  {module_name}/")

        for lr in sorted(locale_results, key=lambda x: x.locale):
            remaining = lr.missing_before - lr.newly_translated
            if remaining == 0 and lr.failed == 0:
                status = "✓"
            elif lr.failed > 0:
                status = "✗"
            elif lr.newly_translated > 0:
                status = "◐"
            else:
                status = "○"

            translated_total = lr.already_translated + lr.newly_translated
            changed_info = f", {lr.changed_count} updated" if lr.changed_count > 0 else ""
            print(
                f"    [{lr.locale:5}] "
                f"{translated_total:3}/{lr.total_source:3} translated "
                f"(+{lr.newly_translated:2} new{changed_info}, {lr.failed:2} failed) {status}"
            )

    print("\n" + "-" * 70)
    print(f"  Missing (before): {result.total_missing_before:5}")
    print(f"  Translated:       {result.total_translated:5}")
    if result.total_changed > 0:
        print(f"  Changed/Updated:  {result.total_changed:5}")
    print(f"  Failed:           {result.total_failed:5}")
    print("=" * 70)


def parse_args() -> argparse.Namespace:
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description="Translate Android string resources using Gemini API",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  %(prog)s --mode check --locales es,de,fr
  %(prog)s --mode apply --locales es,de,fr
  %(prog)s --mode apply --locales ar --model gemma-3-27b-it --batch-size 10
  %(prog)s --mode apply --locales es --request-delay 4.0 --batch-size 10
  python translate.py --repo-root ./feature/transfer-process --mode apply --locales es,de,ar --model gemma-3-27b-it --batch-size 15

Environment Variables:
  GEMINI_API_KEY    API key for Google Gemini (required for apply mode)

Snapshot Tracking:
  The script tracks source string hashes in .translation_snapshots/ directory.
  When a source string is modified, it will be automatically re-translated
  in all target locales on the next run.

Exit Codes:
  0 - Success
  1 - Failure (some translations failed)
  2 - Missing (check mode: missing translations detected)
""",
    )

    parser.add_argument("--repo-root", type=Path, default=Path("."))
    parser.add_argument("--mode", choices=["check", "apply"], required=True)
    parser.add_argument("--locales", default="es,de")
    parser.add_argument("--model", default="gemini-2.0-flash")
    parser.add_argument("--batch-size", type=int, default=20)
    parser.add_argument("--api-key-env", default="GEMINI_API_KEY")
    parser.add_argument("--no-validate", action="store_true")
    parser.add_argument("--verbose", "-v", action="store_true")
    parser.add_argument("--request-delay", type=float, default=2.0)

    return parser.parse_args()


def main() -> int:
    """Main entry point."""
    args = parse_args()

    if args.verbose:
        logging.getLogger().setLevel(logging.DEBUG)

    locales = [loc.strip() for loc in args.locales.split(",") if loc.strip()]
    if not locales:
        logger.error("No locales specified")
        return 1

    api_key = ""
    if args.mode == "apply":
        api_key = os.environ.get(args.api_key_env, "").strip()
        if not api_key:
            logger.error(f"API key required. Set {args.api_key_env} environment variable")
            return 1

    request_delay = args.request_delay
    batch_size = args.batch_size
    if "gemma" in args.model.lower():
        if request_delay < 4.0:
            request_delay = 4.0
            logger.info(f"Adjusted request_delay to {request_delay}s for Gemma model")
        if batch_size > 15:
            batch_size = 15
            logger.info(f"Adjusted batch_size to {batch_size} for Gemma model")

    config = Config(
        repo_root=args.repo_root.resolve(),
        mode=args.mode,
        locales=locales,
        model=args.model,
        batch_size=batch_size,
        api_key=api_key,
        validate_output=not args.no_validate,
        request_delay=request_delay,
    )

    logger.info(f"Mode: {config.mode}")
    logger.info(f"Locales: {', '.join(config.locales)}")
    logger.info(f"Repository: {config.repo_root}")
    if config.mode == "apply":
        logger.info(f"Model: {config.model}")
        logger.info(f"Batch size: {config.batch_size}")
        logger.info(f"Request delay: {config.request_delay}s")

    result = process_all(config)
    print_summary(result, config.mode)

    if config.mode == "check":
        if result.total_missing_before > 0:
            logger.warning(f"\n⚠ Missing translations: {result.total_missing_before} string(s)")
            return 2
        logger.info("\n✓ All translations present.")
        return 0

    if result.has_failures:
        logger.warning(f"\n⚠ {result.total_failed} translation(s) failed.")
        return 1

    if result.total_translated > 0:
        logger.info(f"\n✓ Successfully translated {result.total_translated} string(s).")
        logger.info("  Validate with: ./gradlew assembleDebug")
    else:
        logger.info("\n✓ No new translations needed.")

    return 0


if __name__ == "__main__":
    sys.exit(main())
