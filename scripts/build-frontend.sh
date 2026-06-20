#!/usr/bin/env bash
#
# Builds the React frontend and stages the production bundle into the Maven
# resources directory so it gets packaged inside the plugin jar.
#
# The Ledger plugin serves its web UI from the classpath resource path
# "frontend" (see com.ledger.api.utils.ResourceRetriever /
# com.ledger.api.HttpServer#createFrontEndRoutes). That directory is generated,
# not committed (.gitignore), so it must be produced before `mvn package` or the
# plugin fails at startup with:
#
#   java.io.FileNotFoundException: Cannot find resource at 'frontend'
#
set -euo pipefail

# Resolve the repo root relative to this script so it works from any CWD.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

FRONTEND_DIR="$ROOT_DIR/frontend"
BUILD_DIR="$FRONTEND_DIR/build"
RESOURCES_DIR="$ROOT_DIR/src/main/resources/frontend"

# create-react-app treats warnings as errors when CI is set, which makes the
# build brittle. Default to a non-CI build but let callers override.
export CI="${CI:-false}"

echo "==> Installing frontend dependencies"
if [ -f "$FRONTEND_DIR/package-lock.json" ]; then
  npm --prefix "$FRONTEND_DIR" ci
else
  npm --prefix "$FRONTEND_DIR" install
fi

echo "==> Building frontend production bundle"
npm --prefix "$FRONTEND_DIR" run build

if [ ! -f "$BUILD_DIR/index.html" ]; then
  echo "ERROR: frontend build did not produce $BUILD_DIR/index.html" >&2
  exit 1
fi

echo "==> Staging bundle into src/main/resources/frontend"
rm -rf "$RESOURCES_DIR"
mkdir -p "$RESOURCES_DIR"
cp -R "$BUILD_DIR/." "$RESOURCES_DIR/"

echo "==> Frontend staged ($(find "$RESOURCES_DIR" -type f | wc -l | tr -d ' ') files)"
