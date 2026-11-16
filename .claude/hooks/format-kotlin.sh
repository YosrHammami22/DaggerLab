#!/bin/bash
# PostToolUse hook (Edit|MultiEdit|Write): formats the edited Kotlin file with ktlint.
# Reads the hook JSON from stdin; never blocks the tool call.

input=$(cat)

# Cheap pre-filter so non-Kotlin edits never start python3.
case "$input" in
  *'.kt"'* | *'.kts"'*) ;;
  *) exit 0 ;;
esac

# Fail closed: without a project dir we can't tell which files are ours.
if [ -z "$CLAUDE_PROJECT_DIR" ]; then
  echo '{"systemMessage": "format-kotlin: CLAUDE_PROJECT_DIR not set, Kotlin file not formatted"}'
  exit 0
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo '{"systemMessage": "format-kotlin: python3 not installed, Kotlin file not formatted"}'
  exit 0
fi

# Real JSON parsing (handles escapes) of the exact key, with the old fallback.
if ! file=$(printf '%s' "$input" | python3 -I -c 'import sys, json
d = json.load(sys.stdin)
print(d.get("tool_input", {}).get("file_path") or d.get("tool_response", {}).get("filePath") or "")' 2>/dev/null); then
  echo '{"systemMessage": "format-kotlin: could not parse hook input JSON, Kotlin file not formatted"}'
  exit 0
fi

case "$file" in
  *.kt | *.kts) ;;
  *) exit 0 ;;
esac
case "$file" in
  "$CLAUDE_PROJECT_DIR"/*) ;;
  *) exit 0 ;;
esac
# Match build dirs only inside the project, not in the path above it.
case "${file#"$CLAUDE_PROJECT_DIR"/}" in
  build/* | */build/*) exit 0 ;;
esac
[ -f "$file" ] || exit 0

if ! command -v ktlint >/dev/null 2>&1; then
  # systemMessage is shown to the user, unlike stderr on exit 0.
  echo '{"systemMessage": "format-kotlin: ktlint not installed (brew install ktlint), Kotlin file not formatted"}'
  exit 0
fi

# -F fixes what it can; leftover violations and ktlint's log noise are not errors for this hook.
ktlint -F "$file" >/dev/null 2>&1
exit 0