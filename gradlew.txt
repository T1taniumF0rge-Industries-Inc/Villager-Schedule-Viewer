#!/usr/bin/env bash
set -euo pipefail

MISE_JAVA_DIR="$HOME/.local/share/mise/installs/java"
for candidate in 21.0.2 21 17.0.2 17; do
  if [[ -d "$MISE_JAVA_DIR/$candidate" ]]; then
    export JAVA_HOME="$MISE_JAVA_DIR/$candidate"
    export PATH="$JAVA_HOME/bin:$PATH"
    break
  fi
done

GRADLE_VERSION="8.10.2"
DIST_DIR=".gradle-bin"
GRADLE_HOME="$DIST_DIR/gradle-$GRADLE_VERSION"

if [[ ! -x "$GRADLE_HOME/bin/gradle" ]]; then
  mkdir -p "$DIST_DIR"
  ARCHIVE="$DIST_DIR/gradle-$GRADLE_VERSION-bin.zip"
  if [[ ! -f "$ARCHIVE" ]]; then
    curl -fsSL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ARCHIVE"
  fi
  unzip -q -o "$ARCHIVE" -d "$DIST_DIR"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
