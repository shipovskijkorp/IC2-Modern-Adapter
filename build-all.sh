#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CONFIG="$ROOT/build.properties"
OUT="$ROOT/build/libs"

if [ ! -f "$CONFIG" ]; then
    echo "Missing shared build configuration: $CONFIG" >&2
    exit 1
fi

SOURCE_FAMILIES=$(sed -n 's/^sourceFamilies[[:space:]]*=[[:space:]]*//p' "$CONFIG" | tail -n 1)
if [ -z "$SOURCE_FAMILIES" ]; then
    echo "Missing sourceFamilies in $CONFIG" >&2
    exit 1
fi

FAMILIES=$(printf '%s' "$SOURCE_FAMILIES" | tr ',' ' ')

rm -rf "$OUT"
mkdir -p "$OUT"

for family in $FAMILIES; do
    FAMILY_DIR="$ROOT/$family"
    if [ ! -d "$FAMILY_DIR" ]; then
        echo "Configured source family does not exist: $FAMILY_DIR" >&2
        exit 1
    fi

    echo "==> Building $family family"
    (
        cd "$FAMILY_DIR"
        ./gradlew buildAndCollect --no-daemon
    )
done

for family in $FAMILIES; do
    FAMILY_OUT="$ROOT/$family/build/libs"
    if [ -d "$FAMILY_OUT" ]; then
        find "$FAMILY_OUT" -maxdepth 1 -type f -name '*.jar' -exec cp {} "$OUT"/ \;
    fi
done

echo "==> Collected artifacts:"
find "$OUT" -maxdepth 1 -type f -name '*.jar' -printf '    %f\n' | sort
