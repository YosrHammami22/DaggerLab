#!/bin/bash
# PostToolUse hook (Edit|Write): formats the edited Kotlin file with ktlint.
# Reads the hook JSON from stdin; never blocks the tool call.

file=$(python3 -c 'import sys, json
d = json.load(sys.stdin)
print(d.get("tool_input", {}).get("file_path") or d.get("tool_response", {}).get("filePath") or "")')

case "$file" in
  *.kt | *.kts) ;;
  *) exit 0 ;;
esac
case "$file" in
  */build/*) exit 0 ;;
esac
[ -f "$file" ] || exit 0

if ! command -v ktlint >/dev/null 2>&1; then
  echo "format-kotlin: ktlint not installed (brew install ktlint), skipping" >&2
  exit 0
fi

# -F fixes what it can; leftover lint violations are not an error for this hook.
ktlint -F --relative "$file" >/dev/null 2>&1
exit 0